package com.cymelle.app.orders;

import com.cymelle.app.common.exception.ConflictException;
import com.cymelle.app.common.exception.NotFoundException;
import com.cymelle.app.orders.dto.CreateOrderRequest;
import com.cymelle.app.orders.dto.OrderResponse;
import com.cymelle.app.orders.dto.UpdateOrderStatusRequest;
import com.cymelle.app.products.Product;
import com.cymelle.app.products.ProductRepository;
import com.cymelle.app.security.CurrentUser;
import com.cymelle.app.users.AppUser;
import com.cymelle.app.users.UserRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ConflictException("Order items must not be empty");
        }

        Long customerId = CurrentUser.id();
        AppUser customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

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

        // aggregate quantities per product (handles duplicates in request)
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

            BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(requestedQty));
            total = total.add(line);
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

            // deduct stock (safe because locked)
            p.setStockQuantity(p.getStockQuantity() - requestedQty);

            OrderItem oi = new OrderItem();
            oi.setProduct(p);
            oi.setQuantity(requestedQty);
            oi.setUnitPriceAtPurchase(p.getPrice());
            order.addItem(oi);
        }

        // save order (cascade saves items)
        Order saved = orderRepository.save(order);

        return OrderResponse.from(saved);
    }
    public OrderResponse getOrder(Long id) {
        Long actorId = CurrentUser.id();
        Role actorRole = Role.valueOf(CurrentUser.require().getRole());

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        OrderAuthorization.requireOwnerOrAdmin(order, actorId, actorRole);
        return OrderResponse.from(order);
    }

    /**
     * GET /api/v1/orders?status=&userId=&page=&size=
     *
     * Rules:
     * - ADMIN: can pass userId and/or status (or neither to list all)
     * - CUSTOMER: userId is forced to current user, status optional
     */
    public Page<OrderResponse> searchOrders(Long userId, OrderStatus status, Pageable pageable) {
        Long actorId = CurrentUser.id();
        Role actorRole = Role.valueOf(CurrentUser.require().getRole());

        // customer cannot query other users: force to self
        Long effectiveUserId = (actorRole == Role.ADMIN) ? userId : actorId;

        // If CUSTOMER, effectiveUserId is never null
        if (effectiveUserId != null && status != null) {
            return orderRepository.findByCustomerIdAndStatus(effectiveUserId, status, pageable)
                    .map(OrderResponse::from);
        }

        if (effectiveUserId != null) {
            return orderRepository.findByCustomerId(effectiveUserId, pageable)
                    .map(OrderResponse::from);
        }

        // only ADMIN reaches below (because customer always has effectiveUserId)
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

        OrderStatusTransitions.validate(order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());
        orderRepository.save(order);

        return OrderResponse.from(order);
    }
}
