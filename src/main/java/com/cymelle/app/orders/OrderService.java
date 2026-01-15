package com.cymelle.app.orders;

import com.cymelle.app.common.audit.AuditService;
import com.cymelle.app.common.crypto.Hashing;
import com.cymelle.app.common.exception.ConflictException;
import com.cymelle.app.common.exception.NotFoundException;
import com.cymelle.app.orders.dto.CreateOrderRequest;
import com.cymelle.app.orders.dto.OrderResponse;
import com.cymelle.app.orders.dto.PayOrderRequest;
import com.cymelle.app.orders.dto.UpdateOrderStatusRequest;
import com.cymelle.app.products.Product;
import com.cymelle.app.products.ProductRepository;
import com.cymelle.app.security.CurrentUser;
import com.cymelle.app.users.AppUser;
import com.cymelle.app.users.Role;
import com.cymelle.app.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // NEW
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final AuditService auditService;

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request, String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ConflictException("Missing Idempotency-Key header");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ConflictException("Order items must not be empty");
        }

        Long customerId = CurrentUser.id();
        AppUser customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Build stable request hash (sorted productId:qty)
        String canonical = request.getItems().stream()
                .map(i -> i.getProductId() + ":" + i.getQuantity())
                .sorted()
                .reduce((a, b) -> a + "|" + b)
                .orElse("");

        String requestHash = Hashing.sha256Hex(canonical);

        // 1) Idempotency replay check
        Optional<IdempotencyKey> existing = idempotencyKeyRepository
                .findByUserIdAndIdemKey(customerId, idempotencyKey);

        if (existing.isPresent()) {
            IdempotencyKey record = existing.get();

            if (!record.getRequestHash().equals(requestHash)) {
                throw new ConflictException("Idempotency-Key reuse with different request payload");
            }

            Order saved = orderRepository.findById(record.getOrderId())
                    .orElseThrow(() -> new NotFoundException("Order not found for idempotency key"));

            auditService.log("ORDER_IDEMPOTENT_REPLAY", "ORDER", saved.getId(),
                    "idemKey=" + idempotencyKey);

            return OrderResponse.from(saved);
        }

        // collect product IDs
        List<Long> productIds = request.getItems().stream()
                .map(i -> i.getProductId())
                .distinct()
                .toList();

        // lock product rows for update (prevents overselling)
        List<Product> lockedProducts = productRepository.findAllByIdInForUpdate(productIds);

        Map<Long, Product> productMap = lockedProducts.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // ensure all exist
        for (Long pid : productIds) {
            if (!productMap.containsKey(pid)) {
                throw new NotFoundException("Product not found: " + pid);
            }
        }

        // aggregate quantities per product
        Map<Long, Integer> qtyByProductId = new HashMap<>();
        request.getItems().forEach(item -> {
            int qty = Optional.ofNullable(item.getQuantity()).orElse(0);
            if (qty <= 0) throw new ConflictException("quantity must be >= 1");
            qtyByProductId.merge(item.getProductId(), qty, Integer::sum);
        });

        // check stock + compute total
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> e : qtyByProductId.entrySet()) {
            Product p = productMap.get(e.getKey());
            int requestedQty = e.getValue();

            if (p.getStockQuantity() < requestedQty) {
                throw new ConflictException("Insufficient stock for productId=" + p.getId());
            }

            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(requestedQty)));
        }

        // create order
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setTotalCost(total);

        // create items + deduct stock
        for (Map.Entry<Long, Integer> e : qtyByProductId.entrySet()) {
            Product p = productMap.get(e.getKey());
            int requestedQty = e.getValue();

            p.setStockQuantity(p.getStockQuantity() - requestedQty);

            OrderItem oi = new OrderItem();
            oi.setProduct(p);
            oi.setQuantity(requestedQty);
            oi.setUnitPriceAtPurchase(p.getPrice());
            order.addItem(oi);
        }

        Order saved = orderRepository.save(order);

        // 2) Save idempotency record AFTER order is created
        idempotencyKeyRepository.save(
                IdempotencyKey.builder()
                        .userId(customerId)
                        .idemKey(idempotencyKey)
                        .requestHash(requestHash)
                        .orderId(saved.getId())
                        .build()
        );

        // 3) Audit log
        auditService.log("ORDER_PLACED", "ORDER", saved.getId(),
                "total=" + saved.getTotalCost() + ", items=" + saved.getItems().size());

        return OrderResponse.from(saved);
    }

    public OrderResponse getOrder(Long id) {
        Long actorId = CurrentUser.id();

        AppUser actor = userRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        OrderAuthorization.requireOwnerOrAdmin(order, actor);
        return OrderResponse.from(order);
    }

    public Page<OrderResponse> searchOrders(Long userId, OrderStatus status, Pageable pageable) {
        Long actorId = CurrentUser.id();

        AppUser actor = userRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Long effectiveUserId = (actor.getRole() == Role.ADMIN) ? userId : actorId;

        if (effectiveUserId != null && status != null) {
            return orderRepository.findByCustomerIdAndStatus(effectiveUserId, status, pageable)
                    .map(OrderResponse::from);
        }

        if (effectiveUserId != null) {
            return orderRepository.findByCustomerId(effectiveUserId, pageable)
                    .map(OrderResponse::from);
        }

        if (status != null) {
            return orderRepository.findByStatus(status, pageable)
                    .map(OrderResponse::from);
        }

        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        OrderStatus old = order.getStatus();
        OrderStatusTransitions.validate(old, request.getStatus());

        order.setStatus(request.getStatus());
        orderRepository.save(order);

        auditService.log("ORDER_STATUS_CHANGED", "ORDER", order.getId(),
                "from=" + old + ", to=" + order.getStatus());

        return OrderResponse.from(order);
    }

    public OrderResponse payOrder(Long orderId, PayOrderRequest request) {
        Long actorId = CurrentUser.id();

        AppUser actor = userRepository.findById(actorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        OrderAuthorization.requireOwnerOrAdmin(order, actor);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Payment allowed only when order status is PENDING");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ConflictException("Order is already PAID");
        }

        boolean success = (request != null && request.getSuccess() != null)
                ? request.getSuccess()
                : (order.getId() % 2 == 0);

        order.setPaymentStatus(success ? PaymentStatus.PAID : PaymentStatus.FAILED);
        orderRepository.save(order);

        auditService.log("ORDER_PAYMENT", "ORDER", order.getId(),
                "paymentStatus=" + order.getPaymentStatus());

        return OrderResponse.from(order);
    }
}
