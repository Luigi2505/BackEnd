# API de Gerenciamento de Restaurante e Pedidos (PUCPR)

Projeto final de desenvolvimento backend estendendo o servidor Spring Boot com Kotlin desenvolvido ao longo da disciplina.

---

## 👥 Integrantes do Grupo
- **Luigi Bilyk de Oliveira**
- **Guilherme Zamboni**
- **Eric Juan**

---

## 📹 Link do Vídeo de Apresentação (YouTube)
> **Vídeo no YouTube:** [](https://youtu.be/H3GhmhgY0Bk)

---

## 📌 Escolha do Tema e Arquitetura

Optamos pela **Opção 1 - Estendendo os serviços**.
Essa opção foi escolhida por:
1. **Praticidade e Didática:** Demonstra evolução direta do projeto visto em sala de aula, reaproveitando as convenções de arquitetura limpa (Controller, Service, Repository, DTOs e Entities).
2. **Facilidade de Explicação no Vídeo:** O fluxo de regras de negócio de um **Restaurante e Delivery** é intuitivo de demonstrar: clientes, categorias, produtos no cardápio, criação de pedidos, adição e remoção de itens com recálculo automático de total e proteção de rotas via JWT.

---

## 🏗️ Modelagem e Relacionamentos (Critério de Entidades)

O projeto conta com **5 entidades principais**:

1. **`User` (`tbl_users`)**: Representa os usuários do sistema, suportando roles (`ROLE_USER` e `ROLE_ADMIN`) e senhas criptografadas com **BCrypt**.
2. **`Category` (`tbl_categories`)**: Categorias do cardápio (ex: Hambúrgueres, Bebidas, Sobremesas).
3. **`Product` (`tbl_products`)**: Produtos comercializados com preço, descrição e flag de disponibilidade.
4. **`Order` (`tbl_orders`)**: Pedido realizado por um usuário, contendo status (`PENDENTE`, `PREPARANDO`, `PRONTO`, `ENTREGUE`, `CANCELADO`), data e valor total.
5. **`OrderItem` (`tbl_order_items`)**: Tabela intermediária que associa `Order` e `Product`.

### Relacionamentos Implementados:
- **One-to-Many (`1:N`)**:
  - `Category` $\rightarrow$ `Product`: Uma categoria possui múltiplos produtos cadastrados (`@OneToMany` / `@ManyToOne`).
  - `User` $\rightarrow$ `Order`: Um usuário pode ter vários pedidos ao longo do tempo.
- **Many-to-Many com Tabela Intermediária (`N:N` com classe associativa)**:
  - `Order` $\leftrightarrow$ `Product` intermediado por `OrderItem`. A tabela `tbl_order_items` armazena a quantidade e o preço unitário congelado no momento da adição do item ao pedido.

---

## 🚀 Funcionalidades e Endpoints

### 🔐 1. Autenticação & Segurança (Spring Security + JWT)
- `POST /api/users`: Cadastro de usuários (público).
- `POST /api/users/login`: Login de usuários gerando Token JWT Bearer.
- `GET /api/users` e `GET /api/users/{id}`: Consulta de usuários.
- `DELETE /api/users/{id}`: Ação destrutiva protegida com `@PreAuthorize("hasRole('ADMIN')")`.

### 📂 2. CRUD de Categorias
- `GET /api/categories`: Listagem de categorias cadastradas (público).
- `GET /api/categories/{id}`: Detalhes de uma categoria por ID.
- `POST /api/categories`: Criação de categoria (requer autenticação).
- `PUT /api/categories/{id}`: Edição de categoria (requer autenticação).
- `DELETE /api/categories/{id}`: Ação destrutiva protegida exclusivamente para `ADMIN`. Possui validação que impede exclusão se houver produtos vinculados.

### 🍔 3. CRUD e Consulta de Produtos (com Filtros e Ordenação)
- `GET /api/products`: Consulta rica com suporte a **Query Parameters**:
  - `name`: Filtro textual parcial (case-insensitive).
  - `categoryId`: Filtro por categoria.
  - `minPrice` e `maxPrice`: Faixa de preço (ex: `minPrice=10&maxPrice=30`).
  - `available`: Filtro por disponibilidade (`true`/`false`).
  - `sortBy`: Campo para ordenação (`id`, `name` ou `price`).
  - `direction`: Sentido da ordenação (`asc` ou `desc`).
  - *Exemplo de Query:* `GET /api/products?minPrice=10&maxPrice=40&sortBy=price&direction=desc`
- `GET /api/products/{id}`: Detalhe do produto.
- `POST /api/products`: Cadastro de produto associado a uma categoria (requer autenticação).
- `PUT /api/products/{id}`: Atualização de produto (requer autenticação).
- `DELETE /api/products/{id}`: Exclusão restrita a `ADMIN`.

### 🛒 4. Workflow de Pedidos e Associação Entre Classes
- `POST /api/orders`: Inicia um novo pedido para um usuário.
- `POST /api/orders/{id}/items`: **Endpoint de associação** que adiciona produtos ao pedido (com quantidade informada) e recalcula o total automaticamente.
- `DELETE /api/orders/{id}/items/{itemId}`: **Endpoint de desassociação** que remove um item do pedido e deduz do total.
- `PATCH /api/orders/{id}/status?status=PRONTO`: Transição de status do pedido.
- `GET /api/orders`: Consulta pedidos (com filtros opcionais por `userId` e `status`).
- `DELETE /api/orders/{id}`: Exclusão restrita a `ADMIN`.

---

## 🛡️ Tratamento de Exceções e Logs

- **Logging:** Implementado com SLF4J / Logback nos serviços centrais (`UserService`, `CategoryService`, `ProductService`, `OrderService`), registrando entradas, operações de negócio e alertas de regra violada.
- **Exceções Customizadas:**
  - `NotFoundException` (HTTP 404): Entidade não encontrada.
  - `BadRequestException` (HTTP 400): Violação de regras (ex: alterar pedido finalizado, produto indisponível).
  - `ConflictException` (HTTP 409): Violações de unicidade (ex: e-mail ou categoria duplicada).
- **`GlobalExceptionHandler` (`@RestControllerAdvice`):** Captura todas as exceções e erros de validação (`@Valid`), retornando payloads JSON padronizados com timestamp, status HTTP e lista detalhada de mensagens.

---

## 💾 Carga Inicial de Demonstração (Seed)

Ao iniciar a aplicação, o `DatabaseInitializer` cria automaticamente dados de teste:
- **Usuário Admin:** `admin@restaurante.com` / Senha: `admin123`
- **Usuário Cliente:** `luigi@restaurante.com` / Senha: `cliente123`
- **Categorias:** Hambúrgueres, Bebidas, Sobremesas
- **Produtos:** X-Bacon Especial, Smash Burger Duplo, Suco Natural, Pudim
- **Pedido inicial:** Pedido com hambúrgueres e suco associados

---

## 🛠️ Como Executar o Projeto

### Pré-requisitos:
- Java JDK 21 ou superior (ou JDK 26)

### Comandos de execução:

1. **Clonar e acessar o diretório:**
   ```powershell
   cd BackEnd
   ```

2. **Compilar e Rodar os Testes:**
   ```powershell
   .\gradlew test
   ```

3. **Subir o Servidor:**
   ```powershell
   .\gradlew bootRun
   ```

4. **Acessar a Documentação Interativa (Swagger OpenAPI):**
   - Acesse no navegador: [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
   - Utilize o botão **Authorize** no canto superior direito para inserir o token JWT retornado no login (`Bearer <seu-token>`).

5. **Console do Banco H2:**
   - Acesse: [http://localhost:8080/api/h2-console](http://localhost:8080/api/h2-console)
   - JDBC URL: `jdbc:h2:mem:restaurantdb`
   - Usuário: `sa` (senha em branco)

---

## 📝 Roteiro Sugerido para o Vídeo de Apresentação

1. **Apresentação inicial:** Apresentar os membros do grupo e explicar a escolha da **Opção 1 (Backend de Restaurante e Pedidos)**.
2. **Estrutura de Classes e Modelagem:**
   - Mostrar os modelos: `User`, `Category`, `Product`, `Order`, `OrderItem`.
   - Destacar o relacionamento `1:N` (`Category` $\rightarrow$ `Product`) e `N:N` com tabela associativa (`Order` $\leftrightarrow$ `OrderItem` $\leftrightarrow$ `Product`).
3. **Segurança e Login:**
   - Fazer login em `POST /api/users/login` usando as credenciais do `admin` e copiar o token JWT.
   - Demonstrar o token inserido no Swagger no botão **Authorize**.
4. **CRUDs e Associação:**
   - Mostrar a listagem de produtos com query params de filtro e ordenação (`/api/products?sortBy=price&direction=asc`).
   - Criar um pedido (`POST /api/orders`) e associar itens (`POST /api/orders/{id}/items`). Mostrar o recálculo automático do valor total.
   - Demonstrar remoção de um item.
5. **Ações Destrutivas e Segurança:**
   - Tentar deletar um recurso sem token ou com usuário comum (mostrar o retorno 403 Forbidden).
   - Fazer o DELETE autenticado como Admin com retorno 204 No Content.
6. **Logs e Tratamento de Erros:**
   - Mostrar os logs no console da aplicação.
   - Forçar um erro (ex: categoria inexistente ou validação de campos) e mostrar a resposta amigável do `GlobalExceptionHandler`.
