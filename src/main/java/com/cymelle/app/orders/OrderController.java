package com.cymelle.app.orders;

import com.cymelle.app.orders.dto.OrderResponse;
import com.cymelle.app.orders.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @GetMapping("/search")
    public Page<OrderResponse> search(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable
    ) {
        return orderService.search(userId, status, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest req) {
        return orderService.updateStatus(id, req);
    }
}
