package br.pucpr.authserver.products

import br.pucpr.authserver.categories.CategoryRepository
import br.pucpr.authserver.exceptions.NotFoundException
import jakarta.persistence.criteria.Predicate
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {
    private val log = LoggerFactory.getLogger(ProductService::class.java)

    fun findAll(
        name: String?,
        categoryId: Long?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        available: Boolean?,
        sortBy: String?,
        direction: String?
    ): List<ProductResponse> {
        log.info(
            "Consultando produtos com filtros - nome: {}, categoria: {}, minPrice: {}, maxPrice: {}, sortBy: {}, direction: {}",
            name, categoryId, minPrice, maxPrice, sortBy, direction
        )

        val sortDirection = if (direction?.lowercase() == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        val validSortProperty = when (sortBy?.lowercase()) {
            "price" -> "price"
            "name" -> "name"
            else -> "id"
        }
        val sort = Sort.by(sortDirection, validSortProperty)

        val spec = Specification<Product> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            if (!name.isNullOrBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%${name.lowercase().trim()}%"))
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get<Product>("category").get<Long>("id"), categoryId))
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice))
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice))
            }
            if (available != null) {
                predicates.add(cb.equal(root.get<Boolean>("available"), available))
            }

            cb.and(*predicates.toTypedArray())
        }

        return productRepository.findAll(spec, sort).map { it.toResponse() }
    }

    fun findById(id: Long): ProductResponse {
        log.info("Buscando produto pelo ID: {}", id)
        val product = productRepository.findById(id)
            .orElseThrow { NotFoundException("Produto com ID $id não foi encontrado") }
        return product.toResponse()
    }

    @Transactional
    fun create(request: ProductRequest): ProductResponse {
        log.info("Criando novo produto: {}", request.name)
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { NotFoundException("Categoria com ID ${request.categoryId} informada para o produto não existe") }

        val product = Product(
            name = request.name,
            description = request.description,
            price = request.price,
            available = request.available,
            category = category
        )
        val saved = productRepository.save(product)
        log.info("Produto criado com sucesso. ID: {}", saved.id)
        return saved.toResponse()
    }

    @Transactional
    fun update(id: Long, request: ProductRequest): ProductResponse {
        log.info("Atualizando produto ID: {}", id)
        val product = productRepository.findById(id)
            .orElseThrow { NotFoundException("Produto com ID $id não foi encontrado") }

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { NotFoundException("Categoria com ID ${request.categoryId} não existe") }

        product.name = request.name
        product.description = request.description
        product.price = request.price
        product.available = request.available
        product.category = category

        val updated = productRepository.save(product)
        return updated.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        log.info("Removendo produto ID: {}", id)
        val product = productRepository.findById(id)
            .orElseThrow { NotFoundException("Produto com ID $id não foi encontrado") }

        productRepository.delete(product)
        log.info("Produto ID: {} removido com sucesso", id)
    }
}

