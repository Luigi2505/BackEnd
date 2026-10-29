package br.pucpr.authserver.users

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "tbl_users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var password: String = "",

    @Column(nullable = false)
    var role: String = "ROLE_USER" // ROLE_USER or ROLE_ADMIN
)

data class UserRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    val name: String,

    @field:NotBlank(message = "O e-mail é obrigatório")
    @field:Email(message = "Formato de e-mail inválido")
    val email: String,

    @field:NotBlank(message = "A senha é obrigatória")
    @field:Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    val password: String,

    val role: String? = null
)

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
    val role: String
)

data class LoginRequest(
    @field:NotBlank(message = "O e-mail é obrigatório")
    @field:Email(message = "Formato de e-mail inválido")
    val email: String,

    @field:NotBlank(message = "A senha é obrigatória")
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserResponse
)

fun User.toResponse(): UserResponse = UserResponse(
    id = this.id ?: 0L,
    name = this.name,
    email = this.email,
    role = this.role
)
