package br.pucpr.authserver.products

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.math.BigDecimal

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @RequestParam(required = false) available: Boolean?,
        @RequestParam(required = false, defaultValue = "id") sortBy: String,
        @RequestParam(required = false, defaultValue = "asc") direction: String
    ): ResponseEntity<List<ProductResponse>> {
        val products = productService.findAll(name, categoryId, minPrice, maxPrice, available, sortBy, direction)
        return ResponseEntity.ok(products)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<ProductResponse> {
        return ResponseEntity.ok(productService.findById(id))
    }

    @PostMapping
    @SecurityRequirement(name = "BearerAuth")
    fun insert(@RequestBody @Valid request: ProductRequest): ResponseEntity<ProductResponse> {
        val created = productService.create(request)
        val uri = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id)
            .toUri()
        return ResponseEntity.created(uri).body(created)
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "BearerAuth")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid request: ProductRequest
    ): ResponseEntity<ProductResponse> {
        return ResponseEntity.ok(productService.update(id, request))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

