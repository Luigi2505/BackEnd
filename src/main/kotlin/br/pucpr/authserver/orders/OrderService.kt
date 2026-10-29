package br.pucpr.authserver.orders

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.products.ProductRepository
import br.pucpr.authserver.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository
) {
    private val log = LoggerFactory.getLogger(OrderService::class.java)

    fun findAll(userId: Long?, status: OrderStatus?): List<OrderResponse> {
        log.info("Listando pedidos. Filtros - userId: {}, status: {}", userId, status)
        val orders = when {
            userId != null && status != null -> orderRepository.findByUserId(userId).filter { it.status == status }
            userId != null -> orderRepository.findByUserId(userId)
            status != null -> orderRepository.findByStatus(status)
            else -> orderRepository.findAll()
        }
        return orders.map { it.toResponse() }
    }

    fun findById(id: Long): OrderResponse {
        log.info("Buscando pedido de ID: {}", id)
        val order = orderRepository.findById(id)
            .orElseThrow { NotFoundException("Pedido com ID $id não foi encontrado") }
        return order.toResponse()
    }

    @Transactional
    fun createOrder(request: CreateOrderRequest): OrderResponse {
        log.info("Iniciando criação de pedido para usuário ID: {}", request.userId)
        val user = userRepository.findById(request.userId)
            .orElseThrow { NotFoundException("Usuário com ID ${request.userId} não foi encontrado") }

        val order = Order(
            user = user,
            status = OrderStatus.PENDENTE,
            totalAmount = BigDecimal.ZERO
        )
        val saved = orderRepository.save(order)
        log.info("Pedido criado com sucesso. ID: {}", saved.id)
        return saved.toResponse()
    }

    @Transactional
    fun addItem(orderId: Long, request: AddItemRequest): OrderResponse {
        log.info("Adicionando produto ID: {} (qtd: {}) ao pedido ID: {}", request.productId, request.quantity, orderId)
        val order = orderRepository.findById(orderId)
            .orElseThrow { NotFoundException("Pedido com ID $orderId não foi encontrado") }

        if (order.status != OrderStatus.PENDENTE) {
            log.warn("Tentativa de alterar pedido com status finalizado ou em preparo: {}", order.status)
            throw BadRequestException("Não é possível adicionar itens a um pedido com status ${order.status}")
        }

        val product = productRepository.findById(request.productId)
            .orElseThrow { NotFoundException("Produto com ID ${request.productId} não foi encontrado") }

        if (!product.available) {
            log.warn("Produto ID: {} está indisponível", request.productId)
            throw BadRequestException("O produto '${product.name}' está indisponível no momento")
        }

        // Verifica se item já existe no pedido
        val existingItem = order.items.find { it.product?.id == product.id }
        if (existingItem != null) {
            existingItem.quantity += request.quantity
        } else {
            val newItem = OrderItem(
                order = order,
                product = product,
                quantity = request.quantity,
                unitPrice = product.price
            )
            order.items.add(newItem)
        }

        recalculateTotal(order)
        val saved = orderRepository.save(order)
        log.info("Item adicionado com sucesso ao pedido ID: {}. Novo total: {}", orderId, saved.totalAmount)
        return saved.toResponse()
    }

    @Transactional
    fun removeItem(orderId: Long, itemId: Long): OrderResponse {
        log.info("Removendo item ID: {} do pedido ID: {}", itemId, orderId)
        val order = orderRepository.findById(orderId)
            .orElseThrow { NotFoundException("Pedido com ID $orderId não foi encontrado") }

        if (order.status != OrderStatus.PENDENTE) {
            throw BadRequestException("Não é possível remover itens de um pedido com status ${order.status}")
        }

        val removed = order.items.removeIf { it.id == itemId }
        if (!removed) {
            throw NotFoundException("Item com ID $itemId não foi encontrado no pedido $orderId")
        }

        recalculateTotal(order)
        val saved = orderRepository.save(order)
        log.info("Item ID: {} removido do pedido ID: {}. Novo total: {}", itemId, orderId, saved.totalAmount)
        return saved.toResponse()
    }

    @Transactional
    fun updateStatus(orderId: Long, status: OrderStatus): OrderResponse {
        log.info("Atualizando status do pedido ID: {} para {}", orderId, status)
        val order = orderRepository.findById(orderId)
            .orElseThrow { NotFoundException("Pedido com ID $orderId não foi encontrado") }

        order.status = status
        val saved = orderRepository.save(order)
        return saved.toResponse()
    }

    @Transactional
    fun delete(orderId: Long) {
        log.info("Cancelando e excluindo pedido ID: {}", orderId)
        val order = orderRepository.findById(orderId)
            .orElseThrow { NotFoundException("Pedido com ID $orderId não foi encontrado") }

        orderRepository.delete(order)
        log.info("Pedido ID: {} excluído com sucesso", orderId)
    }

    private fun recalculateTotal(order: Order) {
        order.totalAmount = order.items.fold(BigDecimal.ZERO) { acc, item ->
            acc.add(item.unitPrice.multiply(BigDecimal.valueOf(item.quantity.toLong())))
        }
    }
}

