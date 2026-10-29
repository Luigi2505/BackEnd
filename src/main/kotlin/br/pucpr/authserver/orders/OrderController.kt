package br.pucpr.authserver.orders

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) status: OrderStatus?
    ): ResponseEntity<List<OrderResponse>> {
        return ResponseEntity.ok(orderService.findAll(userId, status))
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<OrderResponse> {
        return ResponseEntity.ok(orderService.findById(id))
    }

    @PostMapping
    @SecurityRequirement(name = "BearerAuth")
    fun createOrder(@RequestBody @Valid request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val created = orderService.createOrder(request)
        val uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id)
            .toUri()
        return ResponseEntity.created(uri).body(created)
    }

    @PostMapping("/{id}/items")
    @SecurityRequirement(name = "BearerAuth")
    fun addItem(
        @PathVariable id: Long,
        @RequestBody @Valid request: AddItemRequest
    ): ResponseEntity<OrderResponse> {
        return ResponseEntity.ok(orderService.addItem(id, request))
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @SecurityRequirement(name = "BearerAuth")
    fun removeItem(
        @PathVariable orderId: Long,
        @PathVariable itemId: Long
    ): ResponseEntity<OrderResponse> {
        return ResponseEntity.ok(orderService.removeItem(orderId, itemId))
    }

    @PatchMapping("/{id}/status")
    @SecurityRequirement(name = "BearerAuth")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestParam status: OrderStatus
    ): ResponseEntity<OrderResponse> {
        return ResponseEntity.ok(orderService.updateStatus(id, status))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        orderService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

