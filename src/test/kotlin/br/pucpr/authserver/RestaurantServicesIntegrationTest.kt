package br.pucpr.authserver

import br.pucpr.authserver.categories.CategoryRequest
import br.pucpr.authserver.categories.CategoryResponse
import br.pucpr.authserver.categories.CategoryService
import br.pucpr.authserver.orders.*
import br.pucpr.authserver.products.ProductRequest
import br.pucpr.authserver.products.ProductService
import br.pucpr.authserver.users.LoginRequest
import br.pucpr.authserver.users.UserRequest
import br.pucpr.authserver.users.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal

@SpringBootTest
class RestaurantServicesIntegrationTest {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var categoryService: CategoryService

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var orderService: OrderService

    @Test
    fun `test user registration and login`() {
        val email = "teste.unitario@restaurante.com"
        val user = userService.register(UserRequest("Teste Unitario", email, "senha123"))
        assertNotNull(user.id)
        assertEquals(email, user.email)

        val loginRes = userService.login(LoginRequest(email, "senha123"))
        assertNotNull(loginRes.token)
        assertTrue(loginRes.token.isNotEmpty())
    }

    @Test
    fun `test category CRUD`() {
        val cat = categoryService.create(CategoryRequest("Bebidas Especiais", "Sucos e Drinks"))
        assertNotNull(cat.id)

        val found = categoryService.findById(cat.id)
        assertEquals("Bebidas Especiais", found.name)

        val updated = categoryService.update(cat.id, CategoryRequest("Bebidas Especiais 2", "Atualizado"))
        assertEquals("Bebidas Especiais 2", updated.name)

        categoryService.delete(cat.id)
        assertThrows(RuntimeException::class.java) {
            categoryService.findById(cat.id)
        }
    }

    @Test
    fun `test product filtering and query specification`() {
        // Busca produtos ordenados por preco
        val products = productService.findAll(
            name = null,
            categoryId = null,
            minPrice = null,
            maxPrice = null,
            available = null,
            sortBy = "price",
            direction = "asc"
        )
        assertTrue(products.isNotEmpty())
        for (i in 0 until products.size - 1) {
            assertTrue(products[i].price <= products[i + 1].price)
        }

        // Busca com filtro de preco maximo
        val cheapProducts = productService.findAll(
            name = null,
            categoryId = null,
            minPrice = null,
            maxPrice = BigDecimal("20.00"),
            available = true,
            sortBy = "price",
            direction = "asc"
        )
        cheapProducts.forEach {
            assertTrue(it.price <= BigDecimal("20.00"))
        }
    }

    @Test
    fun `test order workflow and associations`() {
        val user = userService.findAll().first()
        val product = productService.findAll(null, null, null, null, null, null, null).first()

        // 1. Criar pedido
        val order = orderService.createOrder(CreateOrderRequest(userId = user.id))
        assertEquals(OrderStatus.PENDENTE, order.status)
        assertEquals(BigDecimal.ZERO, order.totalAmount)

        // 2. Adicionar itens ao pedido (relacionamento many-to-many com tbl_order_items)
        val withItem = orderService.addItem(order.id, AddItemRequest(productId = product.id, quantity = 2))
        assertEquals(1, withItem.items.size)
        val expectedTotal = product.price.multiply(BigDecimal.valueOf(2))
        assertEquals(expectedTotal, withItem.totalAmount)

        // 3. Atualizar status
        val updatedStatus = orderService.updateStatus(order.id, OrderStatus.PREPARANDO)
        assertEquals(OrderStatus.PREPARANDO, updatedStatus.status)

        // 4. Voltar para PENDENTE para testar remocao
        orderService.updateStatus(order.id, OrderStatus.PENDENTE)
        val item = withItem.items.first()
        val afterRemove = orderService.removeItem(order.id, item.id)
        assertEquals(0, afterRemove.items.size)
        assertEquals(BigDecimal.ZERO, afterRemove.totalAmount)

        // 5. Excluir pedido
        orderService.delete(order.id)
        assertThrows(RuntimeException::class.java) {
            orderService.findById(order.id)
        }
    }
}

