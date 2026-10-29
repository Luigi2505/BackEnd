package br.pucpr.authserver.orders

import br.pucpr.authserver.products.Product
import br.pucpr.authserver.users.User
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

enum class OrderStatus {
    PENDENTE, PREPARANDO, PRONTO, ENTREGUE, CANCELADO
}

@Entity
@Table(name = "tbl_orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDENTE,

    @Column(nullable = false, precision = 10, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var items: MutableList<OrderItem> = mutableListOf()
)

@Entity
@Table(name = "tbl_order_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    var order: Order? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null,

    @Column(nullable = false)
    var quantity: Int = 1,

    @Column(nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal = BigDecimal.ZERO
)

data class CreateOrderRequest(
    @field:NotNull(message = "O ID do usuário é obrigatório")
    val userId: Long
)

data class AddItemRequest(
    @field:NotNull(message = "O ID do produto é obrigatório")
    val productId: Long,

    @field:NotNull(message = "A quantidade é obrigatória")
    @field:Min(value = 1, message = "A quantidade deve ser de no mínimo 1")
    val quantity: Int = 1
)

data class OrderItemResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal
)

data class OrderResponse(
    val id: Long,
    val createdAt: LocalDateTime,
    val status: OrderStatus,
    val userId: Long,
    val userName: String,
    val totalAmount: BigDecimal,
    val items: List<OrderItemResponse>
)

fun OrderItem.toResponse(): OrderItemResponse = OrderItemResponse(
    id = this.id ?: 0L,
    productId = this.product?.id ?: 0L,
    productName = this.product?.name ?: "",
    unitPrice = this.unitPrice,
    quantity = this.quantity,
    subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity.toLong()))
)

fun Order.toResponse(): OrderResponse = OrderResponse(
    id = this.id ?: 0L,
    createdAt = this.createdAt,
    status = this.status,
    userId = this.user?.id ?: 0L,
    userName = this.user?.name ?: "",
    totalAmount = this.totalAmount,
    items = this.items.map { it.toResponse() }
)

