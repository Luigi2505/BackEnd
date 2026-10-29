package br.pucpr.authserver.products

import br.pucpr.authserver.categories.Category
import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Entity
@Table(name = "tbl_products")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(length = 1000)
    var description: String = "",

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    var available: Boolean = true,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category? = null
)

data class ProductRequest(
    @field:NotBlank(message = "O nome do produto é obrigatório")
    val name: String,

    val description: String = "",

    @field:NotNull(message = "O preço é obrigatório")
    @field:DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    val price: BigDecimal,

    val available: Boolean = true,

    @field:NotNull(message = "O ID da categoria é obrigatório")
    val categoryId: Long
)

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val available: Boolean,
    val categoryId: Long,
    val categoryName: String
)

fun Product.toResponse(): ProductResponse = ProductResponse(
    id = this.id ?: 0L,
    name = this.name,
    description = this.description,
    price = this.price,
    available = this.available,
    categoryId = this.category?.id ?: 0L,
    categoryName = this.category?.name ?: "Sem Categoria"
)

