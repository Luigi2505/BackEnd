package br.pucpr.authserver.categories

import com.fasterxml.jackson.annotation.JsonIgnore
import br.pucpr.authserver.products.Product
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "tbl_categories")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String = "",

    var description: String = "",

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    var products: MutableList<Product> = mutableListOf()
)

data class CategoryRequest(
    @field:NotBlank(message = "O nome da categoria é obrigatório")
    val name: String,
    val description: String = ""
)

data class CategoryResponse(
    val id: Long,
    val name: String,
    val description: String,
    val totalProducts: Int = 0
)

fun Category.toResponse(): CategoryResponse = CategoryResponse(
    id = this.id ?: 0L,
    name = this.name,
    description = this.description,
    totalProducts = 0
)

