package br.pucpr.authserver.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .headers { headers ->
                headers.frameOptions { it.disable() } // allow H2 console
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public documentation and dev endpoints
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/health/**").permitAll()
                    // Auth and User registration / login
                    .requestMatchers(HttpMethod.POST, "/users").permitAll()
                    .requestMatchers(HttpMethod.POST, "/users/login").permitAll()
                    // Public read endpoints for restaurant catalog
                    .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/users").permitAll()
                    .requestMatchers(HttpMethod.GET, "/users/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/orders/**").permitAll()
                    // Destructive and mutating operations require authentication (ADMIN or USER)
                    .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/**").authenticated()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}

