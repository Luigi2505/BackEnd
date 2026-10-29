package br.pucpr.authserver.users

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmailIgnoreCase(email: String): User?
    fun existsByEmailIgnoreCase(email: String): Boolean
}
