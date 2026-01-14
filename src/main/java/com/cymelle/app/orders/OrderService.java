package com.cymelle.app.orders;

import com.cymelle.app.common.exception.NotFoundException;
import com.cymelle.app.orders.dto.OrderResponse;
import com.cymelle.app.orders.dto.UpdateOrderStatusRequest;
import com.cymelle.app.security.CurrentUser;
import com.cymelle.app.users.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderResponse get(Long id) {
        AppUser actor = CurrentUser.require();
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        OrderAuthorization.requireOwnerOrAdmin(order, actor);
        return OrderResponse.from(order);
    }

    /**
     * Search rules:
     * - ADMIN: can filter by any userId + status
     * - CUSTOMER: userId is forced to self; status optional
     */
    public Page<OrderResponse> search(Long userId, OrderStatus status, Pageable pageable) {
        AppUser actor = CurrentUser.require();

        Long effectiveUserId = actor.getRole() == com.cymelle.app.users.Role.ADMIN ? userId : actor.getId();

        if (effectiveUserId != null && status != null) {
            return orderRepository.findByCustomerIdAndStatus(effectiveUserId, status, pageable).map(OrderResponse::from);
        }
        if (effectiveUserId != null) {
            return orderRepository.findByCustomerId(effectiveUserId, pageable).map(OrderResponse::from);
        }
        if (status != null) {
            // only ADMIN should reach here (customer never has null effectiveUserId)
            return orderRepository.findByStatus(status, pageable).map(OrderResponse::from);
        }
        // ADMIN listing all orders
        if (actor.getRole().name().equals("ADMIN")) {
            return orderRepository.findAll(pageable).map(OrderResponse::from);
        }
        // customer fallback (shouldn't happen)
        return orderRepository.findByCustomerId(actor.getId(), pageable).map(OrderResponse::from);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest req) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        OrderStatusTransitions.validate(order.getStatus(), req.getStatus());
        order.setStatus(req.getStatus());
        orderRepository.save(order);
        return OrderResponse.from(order);
    }
}
