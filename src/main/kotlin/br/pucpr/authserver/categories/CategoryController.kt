package br.pucpr.authserver.categories

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/categories")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun list(): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok(categoryService.findAll())
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<CategoryResponse> {
        return ResponseEntity.ok(categoryService.findById(id))
    }

    @PostMapping
    @SecurityRequirement(name = "BearerAuth")
    fun insert(@RequestBody @Valid request: CategoryRequest): ResponseEntity<CategoryResponse> {
        val created = categoryService.create(request)
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
        @RequestBody @Valid request: CategoryRequest
    ): ResponseEntity<CategoryResponse> {
        return ResponseEntity.ok(categoryService.update(id, request))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        categoryService.delete(id)
        return ResponseEntity.noContent().build()
    }
}

