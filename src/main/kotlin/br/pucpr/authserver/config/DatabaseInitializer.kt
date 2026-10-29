package br.pucpr.authserver.config

import br.pucpr.authserver.categories.Category
import br.pucpr.authserver.categories.CategoryRepository
import br.pucpr.authserver.orders.AddItemRequest
import br.pucpr.authserver.orders.CreateOrderRequest
import br.pucpr.authserver.orders.OrderService
import br.pucpr.authserver.products.Product
import br.pucpr.authserver.products.ProductRepository
import br.pucpr.authserver.users.User
import br.pucpr.authserver.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import java.math.BigDecimal

@Configuration
class DatabaseInitializer {
    private val log = LoggerFactory.getLogger(DatabaseInitializer::class.java)

    @Bean
    fun initDatabase(
        userRepository: UserRepository,
        categoryRepository: CategoryRepository,
        productRepository: ProductRepository,
        orderService: OrderService,
        passwordEncoder: PasswordEncoder
    ): CommandLineRunner {
        return CommandLineRunner {
            if (userRepository.count() == 0L) {
                log.info("Inicializando banco de dados com dados de demonstração...")

                // Criar Usuários
                val admin = userRepository.save(
                    User(
                        name = "Administrador",
                        email = "admin@restaurante.com",
                        password = passwordEncoder.encode("admin123") ?: "",
                        role = "ROLE_ADMIN"
                    )
                )

                val client = userRepository.save(
                    User(
                        name = "Luigi Bilyk",
                        email = "luigi@restaurante.com",
                        password = passwordEncoder.encode("cliente123") ?: "",
                        role = "ROLE_USER"
                    )
                )

                // Criar Categorias
                val catBurgers = categoryRepository.save(
                    Category(
                        name = "Hambúrgueres",
                        description = "Burgers artesanais preparados na grelha"
                    )
                )

                val catDrinks = categoryRepository.save(
                    Category(
                        name = "Bebidas",
                        description = "Refrigerantes, sucos naturais e águas"
                    )
                )

                val catDesserts = categoryRepository.save(
                    Category(
                        name = "Sobremesas",
                        description = "Sobremesas clássicas e autorais"
                    )
                )

                // Criar Produtos
                val burger1 = productRepository.save(
                    Product(
                        name = "X-Bacon Especial",
                        description = "Pão brioche, burger 180g, bacon crocante, queijo cheddar e maionese da casa",
                        price = BigDecimal("36.50"),
                        available = true,
                        category = catBurgers
                    )
                )

                val burger2 = productRepository.save(
                    Product(
                        name = "Smash Burger Duplo",
                        description = "Dois burgers de 90g, queijo prato e picles",
                        price = BigDecimal("29.90"),
                        available = true,
                        category = catBurgers
                    )
                )

                val drink1 = productRepository.save(
                    Product(
                        name = "Suco Natural de Laranja 500ml",
                        description = "Suco de laranja natural sem açúcar adicionado",
                        price = BigDecimal("12.00"),
                        available = true,
                        category = catDrinks
                    )
                )

                val dessert1 = productRepository.save(
                    Product(
                        name = "Pudim de Leite Condensado",
                        description = "Pudim tradicional com calda de caramelo",
                        price = BigDecimal("14.50"),
                        available = true,
                        category = catDesserts
                    )
                )

                // Criar Pedido de Exemplo
                val order = orderService.createOrder(CreateOrderRequest(userId = client.id!!))
                orderService.addItem(order.id, AddItemRequest(productId = burger1.id!!, quantity = 2))
                orderService.addItem(order.id, AddItemRequest(productId = drink1.id!!, quantity = 1))

                log.info("Carga inicial de dados finalizada com sucesso! Usuários criados: admin@restaurante.com (admin123), luigi@restaurante.com (cliente123)")
            }
        }
    }
}

