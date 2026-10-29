package br.pucpr.authserver.categories

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val productRepository: br.pucpr.authserver.products.ProductRepository
) {
    private val log = LoggerFactory.getLogger(CategoryService::class.java)

    @Transactional(readOnly = true)
    fun findAll(): List<CategoryResponse> {
        log.info("Buscando todas as categorias cadastradas")
        return categoryRepository.findAll().map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): CategoryResponse {
        log.info("Buscando categoria de ID: {}", id)
        val category = categoryRepository.findById(id)
            .orElseThrow { NotFoundException("Categoria com ID $id não foi encontrada") }
        return category.toResponse()
    }

    @Transactional
    fun create(request: CategoryRequest): CategoryResponse {
        log.info("Tentativa de criar categoria: {}", request.name)
        if (categoryRepository.existsByNameIgnoreCase(request.name)) {
            log.warn("Falha ao criar categoria: nome '{}' já existe", request.name)
            throw ConflictException("Já existe uma categoria cadastrada com o nome '${request.name}'")
        }

        val category = Category(
            name = request.name,
            description = request.description
        )
        val saved = categoryRepository.save(category)
        log.info("Categoria criada com sucesso. ID: {}", saved.id)
        return saved.toResponse()
    }

    @Transactional
    fun update(id: Long, request: CategoryRequest): CategoryResponse {
        log.info("Atualizando categoria ID: {}", id)
        val existing = categoryRepository.findById(id)
            .orElseThrow { NotFoundException("Categoria com ID $id não foi encontrada") }

        val withSameName = categoryRepository.findByNameIgnoreCase(request.name)
        if (withSameName != null && withSameName.id != id) {
            throw ConflictException("Já existe outra categoria cadastrada com o nome '${request.name}'")
        }

        existing.name = request.name
        existing.description = request.description
        val updated = categoryRepository.save(existing)
        return updated.toResponse()
    }

    @Transactional
    fun delete(id: Long) {
        log.info("Tentando excluir categoria ID: {}", id)
        val category = categoryRepository.findById(id)
            .orElseThrow { NotFoundException("Categoria com ID $id não foi encontrada") }

        val associatedProducts = productRepository.findByCategoryId(id)
        if (associatedProducts.isNotEmpty()) {
            log.warn("Exclusão rejeitada: categoria ID {} possui produtos associados", id)
            throw BadRequestException("Não é possível remover categoria que contém produtos vinculados")
        }

        categoryRepository.delete(category)
        log.info("Categoria ID: {} removida com sucesso", id)
    }
}

