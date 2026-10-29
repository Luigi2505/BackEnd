package br.pucpr.authserver.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtService(
    @Value("\${jwt.secret}") private val secretKeyString: String,
    @Value("\${jwt.expiration}") private val expirationMs: Long
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKeyString.toByteArray())
    }

    fun generateToken(email: String, role: String, id: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .claim("userId", id)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    fun extractRole(token: String): String? {
        return extractAllClaims(token).get("role", String::class.java)
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}

