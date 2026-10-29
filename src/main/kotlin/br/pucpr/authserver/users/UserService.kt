package br.pucpr.authserver.users

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    fun findAll(): List<UserResponse> {
        log.info("Listando todos os usuários")
        return userRepository.findAll().map { it.toResponse() }
    }

    fun findById(id: Long): UserResponse {
        log.info("Buscando usuário por ID: {}", id)
        val user = userRepository.findById(id)
            .orElseThrow { NotFoundException("Usuário com ID $id não foi encontrado") }
        return user.toResponse()
    }

    @Transactional
    fun register(request: UserRequest): UserResponse {
        log.info("Registrando usuário: {}", request.email)
        if (userRepository.existsByEmailIgnoreCase(request.email)) {
            log.warn("E-mail já cadastrado: {}", request.email)
            throw ConflictException("E-mail '${request.email}' já está em uso")
        }

        val role = if (request.role?.uppercase() == "ROLE_ADMIN" || request.role?.uppercase() == "ADMIN") {
            "ROLE_ADMIN"
        } else {
            "ROLE_USER"
        }

        val user = User(
            name = request.name,
            email = request.email.lowercase().trim(),
            password = passwordEncoder.encode(request.password) ?: "",
            role = role
        )
        val saved = userRepository.save(user)
        log.info("Usuário registrado com sucesso. ID: {}, Role: {}", saved.id, saved.role)
        return saved.toResponse()
    }

    fun login(request: LoginRequest): LoginResponse {
        log.info("Tentativa de login para e-mail: {}", request.email)
        val user = userRepository.findByEmailIgnoreCase(request.email.trim())
            ?: run {
                log.warn("Login falhou: e-mail não encontrado: {}", request.email)
                throw BadRequestException("Credenciais inválidas: e-mail ou senha incorretos")
            }

        if (!passwordEncoder.matches(request.password, user.password)) {
            log.warn("Login falhou: senha incorreta para: {}", request.email)
            throw BadRequestException("Credenciais inválidas: e-mail ou senha incorretos")
        }

        val token = jwtService.generateToken(user.email, user.role, user.id ?: 0L)
        log.info("Login bem-sucedido para usuário ID: {}", user.id)
        return LoginResponse(
            token = token,
            user = user.toResponse()
        )
    }

    @Transactional
    fun delete(id: Long) {
        log.info("Tentativa de exclusão do usuário ID: {}", id)
        val user = userRepository.findById(id)
            .orElseThrow { NotFoundException("Usuário com ID $id não foi encontrado") }
        userRepository.delete(user)
        log.info("Usuário ID: {} excluído com sucesso", id)
    }
}

