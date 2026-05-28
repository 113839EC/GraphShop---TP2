# GraphShop — E-Commerce sobre Neo4j

**Trabajo Práctico 2 · Bases de Datos II**  
Aplicación de comercio electrónico construida sobre una base de datos de grafos (Neo4j), con interfaz de consola interactiva y dashboard de analítica visual desarrollado en Java Swing.

---

## Índice

1. [Motivación y elección de Neo4j](#1-motivación-y-elección-de-neo4j)
2. [Diseño de la base de datos](#2-diseño-de-la-base-de-datos)
3. [Consultas Cypher implementadas](#3-consultas-cypher-implementadas)
4. [Algoritmos de grafo complejos](#4-algoritmos-de-grafo-complejos)
5. [Arquitectura del backend](#5-arquitectura-del-backend)
6. [Dashboard visual (Frontend Swing)](#6-dashboard-visual-frontend-swing)
7. [Tecnologías utilizadas](#7-tecnologías-utilizadas)
8. [Cómo ejecutar el proyecto](#8-cómo-ejecutar-el-proyecto)
9. [Datos de muestra](#9-datos-de-muestra)
10. [Comparación Neo4j vs SQL Relacional](#10-comparación-neo4j-vs-sql-relacional)

---

## 1. Motivación y elección de Neo4j

### ¿Por qué una base de datos de grafos?

Un sistema de e-commerce moderno no solo almacena datos: **navega relaciones complejas entre entidades**. Las preguntas más valiosas del negocio involucran múltiples saltos:

- *"¿Qué otros productos compraron los usuarios que compraron lo mismo que yo?"*
- *"¿Qué tan similares son dos usuarios basándose en su historial de compras?"*
- *"¿Qué productos vio un usuario pero nunca compró?"*

En un modelo relacional, cada una de estas preguntas requiere múltiples `JOIN` anidados, subconsultas correlacionadas y rendimiento degradado al crecer los datos. En Neo4j, son **traversals nativos** de 2 a 4 saltos que se expresan de forma natural y escalan eficientemente.

### Ventajas específicas de Neo4j para este dominio

| Aspecto | Neo4j (Grafos) | RDBMS (Relacional) |
|---|---|---|
| Navegación de relaciones | Traversal nativo O(1) por salto | JOIN con costo O(n×m) |
| Consultas de red | Filtrado colaborativo en pocas líneas | 4+ JOINs + subconsultas |
| Similitud entre usuarios | Jaccard con operaciones de conjuntos nativas | Window functions complejas |
| Modelado intuitivo | El esquema refleja el dominio real | Normalización artificial (tablas puente) |
| Consultas negativas | `NOT EXISTS { MATCH ... }` directo | `NOT IN (SELECT ...)` costoso |
| Esquema flexible | Sin migraciones para agregar propiedades | `ALTER TABLE` para cada cambio |

### Neo4j Community Edition — configuración utilizada

- **Versión:** Neo4j Community 5.x (instalación portable)
- **Protocolo de conexión:** Bolt (`bolt://localhost:7687`)
- **Interfaz web:** Neo4j Browser (`http://localhost:7474`)
- **Credenciales:** `neo4j` / `password`
- **Driver Java:** Neo4j Java Driver 5.18.0

---

## 2. Diseño de la base de datos

### Nodos (Entidades)

#### `User` — Usuario de la plataforma
```
(:User {
    id:              String   // Identificador único (ej: "U001")
    nombre:          String   // Nombre completo
    email:           String   // Email único (con índice de unicidad)
    edad:            Integer  // Edad en años
    ciudad:          String   // Ciudad argentina
    fechaRegistro:   String   // Fecha ISO 8601
    puntosFidelidad: Integer  // Acumulado por compras completadas
})
```

#### `Product` — Producto del catálogo
```
(:Product {
    id:          String  // Identificador único (ej: "P001")
    nombre:      String  // Nombre del producto
    descripcion: String  // Descripción larga
    precio:      Double  // Precio en ARS
    stock:       Integer // Unidades disponibles
    marca:       String  // Marca del fabricante
})
```

#### `Order` — Pedido de compra
```
(:Order {
    id:             String  // Identificador único (ej: "O001")
    fecha:          String  // Fecha del pedido ISO 8601
    estado:         String  // COMPLETADO | PENDIENTE | ENVIADO | CANCELADO
    montoTotal:     Double  // Suma total del pedido en ARS
    metodoPago:     String  // Tarjeta de crédito | Débito | MercadoPago | Efectivo
    direccionEnvio: String  // Dirección de entrega
})
```

#### `Category` — Categoría de producto
```
(:Category {
    id:          String  // Identificador único (ej: "CAT1")
    nombre:      String  // Nombre de la categoría
    descripcion: String  // Descripción de la categoría
})
```

---

### Relaciones (Aristas del grafo)

#### `(User)-[:REALIZO]->(Order)` — Un usuario realiza un pedido
> Sin propiedades. La relación en sí modela el hecho de que el usuario es el autor del pedido.

#### `(Order)-[:CONTIENE {cantidad, precioUnitario}]->(Product)` — Un pedido contiene un producto
```
[:CONTIENE {
    cantidad:       Integer  // Unidades compradas de este producto
    precioUnitario: Double   // Precio al momento de la compra (puede diferir del precio actual)
}]
```
> Las propiedades en la relación permiten preservar el precio histórico independientemente de actualizaciones al nodo `Product`.

#### `(Product)-[:PERTENECE_A]->(Category)` — Un producto pertenece a una categoría
> Sin propiedades. Clasificación del producto.

#### `(User)-[:VISTO {timestamp, duracionSegundos}]->(Product)` — Un usuario vio un producto
```
[:VISTO {
    timestamp:        String   // Fecha/hora de la visita
    duracionSegundos: Integer  // Tiempo en la página del producto
}]
```
> Permite analizar comportamiento pre-compra: qué tan interesado estuvo el usuario.

#### `(User)-[:RESENO {calificacion, comentario, fecha}]->(Product)` — Un usuario reseña un producto
```
[:RESENO {
    calificacion: Integer  // 1 a 5 estrellas
    comentario:   String   // Texto de la reseña
    fecha:        String   // Fecha de publicación ISO 8601
}]
```

---

### Diagrama del grafo

```
                     ┌──────────────┐
                     │   Category   │
                     │  (CAT1..5)   │
                     └──────┬───────┘
                            │ ◄── PERTENECE_A
                     ┌──────┴───────┐
                     │   Product    │
                     │  (P001..30)  │
                     └──────┬───────┘
           RESENO ──►       │       ◄── CONTIENE
           VISTO  ──►       │
                     ┌──────┴───────┐       ┌──────────┐
                     │     User     │       │  Order   │
                     │  (U001..25)  ├──────►│(O001..35)│
                     └─────────────-┘REALIZO└──────────┘
```

---

### Constraints e índices creados

```cypher
CREATE CONSTRAINT unique_user_id    IF NOT EXISTS FOR (u:User)     REQUIRE u.id IS UNIQUE;
CREATE CONSTRAINT unique_product_id IF NOT EXISTS FOR (p:Product)  REQUIRE p.id IS UNIQUE;
CREATE CONSTRAINT unique_order_id   IF NOT EXISTS FOR (o:Order)    REQUIRE o.id IS UNIQUE;
CREATE CONSTRAINT unique_category_id IF NOT EXISTS FOR (c:Category) REQUIRE c.id IS UNIQUE;
CREATE INDEX user_email_index IF NOT EXISTS FOR (u:User) ON (u.email);
```

---

## 3. Consultas Cypher implementadas

### Consulta 1 — Listar todas las categorías
```cypher
MATCH (c:Category)
RETURN c.id AS id, c.nombre AS nombre, c.descripcion AS descripcion
ORDER BY c.nombre
```
*Traversal simple sobre nodos `Category`. Demuestra el acceso directo a nodos sin JOIN.*

---

### Consulta 2 — Listar todos los usuarios
```cypher
MATCH (u:User)
RETURN u.id, u.nombre, u.email, u.ciudad, u.puntosFidelidad
ORDER BY u.nombre
```

---

### Consulta 3 — Buscar usuario por email
```cypher
MATCH (u:User {email: $email})
RETURN u
```
*Usa el índice de unicidad sobre `email`. Búsqueda O(1).*

---

### Consulta 4 — Usuarios inactivos (sin pedidos)
```cypher
MATCH (u:User)
WHERE NOT EXISTS { MATCH (u)-[:REALIZO]->(:Order) }
RETURN u.id, u.nombre, u.email, u.fechaRegistro
ORDER BY u.fechaRegistro
```
*Patrón de **coincidencia negativa**: encuentra usuarios que no tienen ninguna arista `REALIZO` saliente. En SQL requeriría un `LEFT JOIN ... WHERE order_id IS NULL` o `NOT IN (SELECT usuario_id FROM pedidos)`.*

---

### Consulta 5 — Productos con su categoría (traversal 1 salto)
```cypher
MATCH (p:Product)-[:PERTENECE_A]->(c:Category)
RETURN p.id, p.nombre, p.precio, p.stock, p.marca, c.nombre AS categoria
ORDER BY c.nombre, p.nombre
```
*Traversal de 1 salto: `Product → Category`. Equivale a un `JOIN` entre dos tablas.*

---

### Consulta 6 — Productos por rango de precio
```cypher
MATCH (p:Product)-[:PERTENECE_A]->(c:Category)
WHERE p.precio >= $min AND p.precio <= $max
RETURN p.nombre, p.precio, p.marca, c.nombre AS categoria
ORDER BY p.precio
```

---

### Consulta 7 — Productos más vendidos (aggregation sobre traversal)
```cypher
MATCH (o:Order)-[c:CONTIENE]->(p:Product)-[:PERTENECE_A]->(cat:Category)
WHERE o.estado = 'COMPLETADO'
RETURN p.nombre AS nombre, p.marca AS marca, cat.nombre AS categoria,
       SUM(c.cantidad) AS totalVendido,
       COUNT(DISTINCT o) AS cantidadPedidos,
       SUM(c.cantidad * c.precioUnitario) AS ingresos
ORDER BY totalVendido DESC
LIMIT $limit
```
*Traversal de 2 saltos con agregación. Navega `Order → Product → Category` acumulando cantidades y revenue. Filtro de estado aplicado sobre la relación.*

---

### Consulta 8 — Usuarios que vieron pero no compraron un producto
```cypher
MATCH (u:User)-[:VISTO]->(p:Product {id: $productId})
WHERE NOT EXISTS {
    MATCH (u)-[:REALIZO]->(o:Order)-[:CONTIENE]->(p)
}
RETURN u.nombre, u.email, u.ciudad
ORDER BY u.nombre
```
*Patrón híbrido: traversal positivo `VISTO` combinado con coincidencia negativa sobre `REALIZO → CONTIENE`. Identifica oportunidades de remarketing.*

---

### Consulta 9 — Historial completo de compras de un usuario
```cypher
MATCH (u:User {id: $userId})-[:REALIZO]->(o:Order)-[c:CONTIENE]->(p:Product)
RETURN o.id AS pedidoId, o.fecha, o.estado, o.montoTotal, o.metodoPago,
       p.nombre AS producto, p.marca,
       c.cantidad, c.precioUnitario
ORDER BY o.fecha DESC, p.nombre
```
*Traversal de **2 saltos**: `User → Order → Product`. Recupera todo el historial de un usuario incluyendo propiedades de la relación `CONTIENE` (cantidad y precio unitario histórico).*

---

### Consulta 10 — Ingresos por categoría (pedidos completados)
```cypher
MATCH (o:Order)-[c:CONTIENE]->(p:Product)-[:PERTENECE_A]->(cat:Category)
WHERE o.estado = 'COMPLETADO'
RETURN cat.nombre AS categoria,
       SUM(c.cantidad * c.precioUnitario) AS ingresos,
       COUNT(DISTINCT o) AS totalPedidos,
       SUM(c.cantidad) AS unidadesVendidas
ORDER BY ingresos DESC
```
*Traversal de **3 saltos** con filtro y múltiple agregación. Equivalente SQL requeriría: `orders JOIN order_items JOIN products JOIN categories WHERE estado='COMPLETADO' GROUP BY categoria`.*

---

### Consultas adicionales de reportes

**Estadísticas generales del sistema:**
```cypher
MATCH (u:User)         WITH COUNT(u) AS totalUsuarios
MATCH (p:Product)      WITH totalUsuarios, COUNT(p) AS totalProductos
MATCH (o:Order)        WITH totalUsuarios, totalProductos, COUNT(o) AS totalPedidos
MATCH (o2:Order) WHERE o2.estado = 'COMPLETADO'
RETURN totalUsuarios, totalProductos, totalPedidos, COUNT(o2) AS pedidosCompletados
```

**Pedidos por estado:**
```cypher
MATCH (o:Order)
RETURN o.estado AS estado, COUNT(o) AS cantidad
ORDER BY cantidad DESC
```

**Productos más vistos con tiempo promedio:**
```cypher
MATCH (u:User)-[v:VISTO]->(p:Product)-[:PERTENECE_A]->(c:Category)
RETURN p.nombre, p.marca, c.nombre AS categoria,
       COUNT(v) AS totalVistas,
       AVG(coalesce(v.duracionSegundos, 0)) AS avgSegundos
ORDER BY totalVistas DESC
LIMIT $limit
```

**Ingresos por método de pago:**
```cypher
MATCH (o:Order)-[c:CONTIENE]->(p:Product)
WHERE o.estado = 'COMPLETADO'
RETURN o.metodoPago AS metodoPago,
       SUM(c.cantidad * c.precioUnitario) AS ingresos,
       COUNT(DISTINCT o) AS cantidadPedidos
ORDER BY ingresos DESC
```

**Calificaciones promedio por producto:**
```cypher
MATCH (u:User)-[r:RESENO]->(p:Product)
MATCH (p)-[:PERTENECE_A]->(c:Category)
RETURN p.nombre, p.marca, c.nombre AS categoria,
       AVG(r.calificacion) AS promedio,
       MIN(r.calificacion) AS minCalif,
       MAX(r.calificacion) AS maxCalif,
       COUNT(r) AS cantidadResenas
ORDER BY promedio DESC, cantidadResenas DESC
```

---

## 4. Algoritmos de grafo complejos

### Algoritmo 1 — Filtrado Colaborativo (Collaborative Filtering)

**Objetivo:** Recomendar productos al usuario objetivo basándose en lo que compraron usuarios con gustos similares.

**Traversal de 4 saltos:**
```
(targetUser)-[:REALIZO]->(Order)-[:CONTIENE]->(Product)
                                         <-[:CONTIENE]-(:Order)
                                         <-[:REALIZO]-(:User similar)
                                         -[:REALIZO]->(:Order)
                                         -[:CONTIENE]->(Product recomendado)
```

**Consulta Cypher completa:**
```cypher
// Paso 1: productos que ya compró el usuario objetivo
MATCH (targetUser:User {id: $userId})-[:REALIZO]->(o:Order)-[:CONTIENE]->(misProductos:Product)
WITH targetUser, COLLECT(DISTINCT misProductos) AS misProductos

// Paso 2: usuarios similares que comparten productos comprados
MATCH (otroUsuario:User)-[:REALIZO]->(o2:Order)-[:CONTIENE]->(p:Product)
WHERE otroUsuario <> targetUser
  AND p IN misProductos
WITH targetUser, misProductos, otroUsuario,
     COUNT(DISTINCT p) AS productosEnComun
ORDER BY productosEnComun DESC
LIMIT 8

// Paso 3: productos nuevos que compraron esos usuarios similares
MATCH (otroUsuario)-[:REALIZO]->(o3:Order)-[:CONTIENE]->(recomendacion:Product)
WHERE NOT recomendacion IN misProductos

// Paso 4: agregar y rankear recomendaciones
RETURN recomendacion.nombre AS nombre,
       recomendacion.marca  AS marca,
       recomendacion.precio AS precio,
       COUNT(DISTINCT otroUsuario) AS usuariosQueLoCompraron
ORDER BY usuariosQueLoCompraron DESC
LIMIT 10
```

**Equivalente SQL:** requeriría 4+ JOINs con subconsultas correlacionadas y un `NOT IN (SELECT ...)` para excluir productos ya comprados. En Neo4j el traversal es nativo y eficiente.

---

### Algoritmo 2 — Similitud de Jaccard entre usuarios

**Objetivo:** Calcular cuán similares son dos usuarios en base a los productos que compraron.

**Fórmula:**
```
Jaccard(A, B) = |A ∩ B| / |A ∪ B|
```
- Valor `1.0` → compraron exactamente los mismos productos
- Valor `0.0` → no tienen ningún producto en común

**Consulta Cypher:**
```cypher
// Recolectar IDs de productos comprados por el usuario objetivo
MATCH (u1:User {id: $userId})-[:REALIZO]->(o1:Order)-[:CONTIENE]->(p:Product)
WITH u1, COLLECT(DISTINCT id(p)) AS idsU1

// Recolectar IDs de productos comprados por cada otro usuario
MATCH (u2:User)-[:REALIZO]->(o2:Order)-[:CONTIENE]->(p2:Product)
WHERE u2 <> u1
WITH u1, idsU1, u2, COLLECT(DISTINCT id(p2)) AS idsU2

// Calcular intersección y unión
WITH u1, u2,
     [x IN idsU1 WHERE x IN idsU2]             AS interseccion,
     idsU1 + [x IN idsU2 WHERE NOT x IN idsU1] AS union_set
WHERE SIZE(interseccion) > 0

RETURN u2.nombre AS usuario, u2.ciudad, u2.email,
       SIZE(interseccion) AS productosEnComun,
       SIZE(union_set)    AS totalUnion,
       round(toFloat(SIZE(interseccion)) / SIZE(union_set) * 100) / 100 AS similitudJaccard
ORDER BY similitudJaccard DESC
LIMIT 8
```

**Por qué Neo4j:** Las operaciones de conjuntos sobre listas de nodos son nativas en Cypher. En SQL requeriría `GROUP BY` con `COUNT(CASE WHEN ...)` o window functions avanzadas.

---

### Consulta adicional — Productos vistos pero no comprados (por usuario)

```cypher
MATCH (u:User {id: $userId})-[v:VISTO]->(p:Product)
WHERE NOT EXISTS {
    MATCH (u)-[:REALIZO]->(o:Order)-[:CONTIENE]->(p)
}
MATCH (p)-[:PERTENECE_A]->(c:Category)
RETURN p.nombre, p.marca, p.precio, c.nombre AS categoria,
       v.duracionSegundos AS segundosVisita
ORDER BY v.duracionSegundos DESC
```

*Útil para estrategias de retargeting: mostrar al usuario los productos que más tiempo estuvo mirando pero no compró.*

---

## 5. Arquitectura del backend

### Estructura de paquetes

```
com.ecommerce/
├── Main.java                      ← Punto de entrada, menú interactivo
├── config/
│   └── Neo4jConfig.java           ← Singleton: driver Bolt + gestión de sesiones
├── model/
│   ├── User.java                  ← POJO: id, nombre, email, edad, ciudad...
│   ├── Product.java               ← POJO: id, nombre, precio, stock, marca...
│   ├── Order.java                 ← POJO: id, fecha, estado, montoTotal...
│   ├── OrderItem.java             ← POJO: productoId, cantidad, precioUnitario
│   └── Category.java             ← POJO: id, nombre, descripcion
├── loader/
│   └── DataLoader.java            ← Carga datos de muestra al grafo
├── repository/
│   ├── UserRepository.java        ← Consultas CRUD + queries de usuario
│   ├── ProductRepository.java     ← Consultas de producto + traversals
│   ├── OrderRepository.java       ← Historial de compras + creación de pedidos
│   ├── CategoryRepository.java    ← Consultas de categoría
│   └── RecommendationRepository.java ← Algoritmos complejos de grafo
├── service/
│   ├── UserService.java           ← Lógica de negocio de usuarios
│   ├── ProductService.java        ← Lógica de negocio de productos
│   ├── OrderService.java          ← Lógica de negocio de pedidos
│   └── RecommendationService.java ← Orquestación de recomendaciones
├── report/
│   └── ReportService.java         ← 7 reportes con consultas de analítica
└── ui/
    └── DashboardWindow.java       ← Dashboard visual Swing (6 pestañas)
```

### Patrón de arquitectura: Capas

```
┌──────────────────────────────────────┐
│         Main / DashboardWindow       │  ← Presentación
├──────────────────────────────────────┤
│        Service (UserService, etc.)   │  ← Lógica de negocio
├──────────────────────────────────────┤
│   Repository (UserRepo, ProdRepo...) │  ← Acceso a datos (Cypher)
├──────────────────────────────────────┤
│           Neo4jConfig                │  ← Conexión Bolt (singleton)
├──────────────────────────────────────┤
│         Neo4j Community              │  ← Base de datos de grafos
└──────────────────────────────────────┘
```

### Neo4jConfig — Gestión de la conexión

Implementa el patrón **Singleton** para que toda la aplicación comparta una única instancia del driver Bolt:

```java
public class Neo4jConfig implements AutoCloseable {
    private static Neo4jConfig instance;
    private final Driver driver;

    private Neo4jConfig() {
        String uri  = System.getenv().getOrDefault("NEO4J_URI",      "bolt://localhost:7687");
        String user = System.getenv().getOrDefault("NEO4J_USER",     "neo4j");
        String pass = System.getenv().getOrDefault("NEO4J_PASSWORD",  "password");
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, pass));
        this.driver.verifyConnectivity(); // Falla rápido si Neo4j no está corriendo
    }

    public static Neo4jConfig getInstance() { ... }
    public Session getSession() { return driver.session(); }
    public void close() { driver.close(); }
}
```

**Variables de entorno disponibles:**
- `NEO4J_URI` — URI del servidor (default: `bolt://localhost:7687`)
- `NEO4J_USER` — Usuario (default: `neo4j`)
- `NEO4J_PASSWORD` — Contraseña (default: `password`)

### DataLoader — Carga de datos de muestra

Inicializa el grafo con datos realistas en orden de dependencias:

1. `limpiarDatos()` — `MATCH (n) DETACH DELETE n`
2. `crearConstraintsEIndices()` — Unicidad e índice de email
3. `crearCategorias()` — 5 categorías
4. `crearProductos()` — 30 productos con relación `PERTENECE_A`
5. `crearUsuarios()` — 25 usuarios argentinos
6. `crearPedidos()` — 35 pedidos con relaciones `REALIZO` y `CONTIENE`
7. `crearVistas()` — 26 relaciones `VISTO` con timestamp y duración
8. `crearResenas()` — 25 relaciones `RESENO` con calificación y comentario

---

## 6. Dashboard visual (Frontend Swing)

### Descripción general

El dashboard es una ventana Swing (`JFrame`) con 6 pestañas que visualizan analítica del negocio en tiempo real. Está implementado con **Java2D / Graphics2D puro**, sin librerías externas de charting.

**Inicio del dashboard:**
```bash
java -jar target/graphshop.jar --dashboard
```

O desde el menú interactivo, opción **8**.

---

### Pestaña 1 — Ingresos por Categoría

**Fuente de datos:** `ReportService.getRevenueByCategory()`  
**Cypher:** `MATCH (o:Order)-[c:CONTIENE]->(p:Product)-[:PERTENECE_A]->(cat:Category) WHERE o.estado = 'COMPLETADO'`

**Visualizaciones:**
- Gráfico de barras horizontales con ingresos por categoría
- Gráfico de torta con participación porcentual
- KPI cards: total de ingresos, categoría líder, pedidos completados

---

### Pestaña 2 — Top Clientes

**Fuente de datos:** `ReportService.getTopCustomers(limit)`  
**Cypher:** `MATCH (u:User)-[:REALIZO]->(o:Order) WHERE o.estado = 'COMPLETADO'`

**Visualizaciones:**
- Gráfico de barras verticales con top 10 clientes por gasto
- Tabla rankeada con medallas (🥇🥈🥉), nombre, ciudad y total gastado

---

### Pestaña 3 — Calificaciones de Productos

**Fuente de datos:** `ReportService.getProductRatings()`  
**Cypher:** `MATCH (u:User)-[r:RESENO]->(p:Product)-[:PERTENECE_A]->(c:Category)`

**Visualizaciones:**
- Barras horizontales por promedio de calificación
- Color semafórico: verde (≥4), ámbar (3–4), rojo (<3)
- Columnas: producto, marca, categoría, promedio, mín, cantidad de reseñas

---

### Pestaña 4 — Actividad

**Fuente de datos:** `ReportService.getOrdersByStatus()` + `ReportService.getMostViewedProducts()`

**Visualizaciones:**
- Gráfico de torta de distribución de pedidos por estado (COMPLETADO, PENDIENTE, ENVIADO, CANCELADO)
- Gráfico de barras horizontales con productos más vistos
- Tabla con nombre, categoría, vistas totales y tiempo promedio de visita

---

### Pestaña 5 — Métodos de Pago

**Fuente de datos:** `ReportService.getSalesByPaymentMethod()`  
**Cypher:** `MATCH (o:Order)-[c:CONTIENE]->(p:Product) WHERE o.estado = 'COMPLETADO'`

**Visualizaciones:**
- Gráfico de barras de ingresos por método de pago
- Gráfico de torta de participación de cada método
- KPI cards con el método más usado y el de mayor facturación
- Tabla detalle con ingresos y cantidad de pedidos por método

---

### Pestaña 6 — Estadísticas Generales + Esquema del grafo

**Fuente de datos:** `ReportService.getGeneralStats()` + todas las fuentes

**Visualizaciones:**
- 7 tarjetas KPI grandes: usuarios totales, productos, pedidos totales, pedidos completados, ingresos totales, cliente top, máximo gasto individual
- **Diagrama del esquema Neo4j** dibujado con Graphics2D: nodos con sus etiquetas, relaciones con nombre y propiedades, colores por tipo de nodo

**Detalles técnicos del diagrama del esquema:**
- Nodos dibujados como óvalos con nombre de etiqueta
- Aristas con flechas y etiquetas de tipo de relación
- Propiedades de las relaciones mostradas en texto pequeño

---

### Implementación técnica del dashboard

**Componentes Swing utilizados:**
- `JFrame` con `JTabbedPane` de 6 pestañas
- `JPanel` con `paintComponent()` sobreescrito para cada gráfico
- `JTable` + `JScrollPane` para tablas de datos
- `JButton` para refresco de datos

**Clases internas de visualización:**
```
DashboardWindow
├── HBarChart     ← Gráfico de barras horizontales
├── VBarChart     ← Gráfico de barras verticales
├── PieChart      ← Gráfico de torta
├── RatingChart   ← Barras con semáforo de color
└── SchemaDiagram ← Diagrama del esquema del grafo
```

**Paleta de colores (tema oscuro):**
```java
Color[] PALETTE = {
    new Color(99,  179, 237),  // azul
    new Color(104, 211, 145),  // verde
    new Color(246, 173, 85),   // naranja
    new Color(237, 100, 166),  // rosa
    new Color(154, 117, 234),  // violeta
    new Color(237, 137, 54),   // ámbar
    new Color(72,  187, 120),  // esmeralda
    new Color(66,  153, 225),  // cielo
};
```

---

## 7. Tecnologías utilizadas

| Componente | Tecnología | Versión |
|---|---|---|
| Base de datos | Neo4j Community | 5.x |
| Lenguaje | Java | 17 |
| Driver de BD | Neo4j Java Driver | 5.18.0 |
| Build | Apache Maven | 3.x |
| UI | Java Swing + AWT | JDK 17 |
| Protocolo BD | Bolt | — |
| Empaquetado | Maven Assembly Plugin | Fat JAR |

---

## 8. Cómo ejecutar el proyecto

### Requisitos previos

- Java 17 o superior
- Neo4j Community instalado y corriendo en `localhost:7687`
- Maven 3.x

### Pasos

**1. Iniciar Neo4j**
```bash
# Windows
cd C:\neo4j-community\bin
neo4j.bat console
```

**2. Compilar y empaquetar**
```bash
cd C:\Users\emanu\IdeaProjects\BDII-TP2
mvn clean package -q
```
Genera `target/graphshop.jar` (fat JAR con todas las dependencias).

**3a. Ejecutar la interfaz de consola**
```bash
java -jar target/graphshop.jar
```

**3b. Ejecutar solo el dashboard visual**
```bash
javaw -jar target/graphshop.jar --dashboard
```

**4. Cargar datos de muestra**

Desde la consola, elegir opción `1` para cargar los datos iniciales.  
El proceso crea 4 tipos de nodos y 5 tipos de relaciones con datos realistas argentinos.

### Variables de entorno (opcional)

```bash
set NEO4J_URI=bolt://localhost:7687
set NEO4J_USER=neo4j
set NEO4J_PASSWORD=miPassword
java -jar target/graphshop.jar
```

### Visualizar el grafo en Neo4j Browser

1. Abrir `http://localhost:7474` en el navegador
2. Conectar con `neo4j` / `password`
3. Ejecutar para ver todo el grafo:
```cypher
MATCH (n)-[r]->(m) RETURN n, r, m LIMIT 150
```
4. Ejecutar para ver solo el esquema:
```cypher
CALL db.schema.visualization()
```

---

## 9. Datos de muestra

### Categorías (5)
| ID | Nombre |
|---|---|
| CAT1 | Electrónica |
| CAT2 | Moda y Ropa |
| CAT3 | Hogar y Jardín |
| CAT4 | Deportes |
| CAT5 | Libros y Cultura |

### Productos (30)
6 productos por categoría, con precios en ARS actualizados a 2026.

**Ejemplos:**
- Electrónica: iPhone 15 Pro ($1.850.000), MacBook Air M3 ($2.800.000), Sony WH-1000XM5 ($580.000)
- Ropa y Moda: Nike Air Max 270 ($285.000), Campera The North Face ($480.000)
- Hogar y Jardín: Aspiradora Robot iRobot ($980.000), Cafetera De'Longhi ($520.000)
- Deportes: Bicicleta Trek Marlin 5 ($1.580.000), Pesas Ajustables ($450.000)
- Libros: Clean Code ($28.000), El Señor de los Anillos ($22.500)

### Usuarios (25)
20 usuarios activos + 5 inactivos, distribuidos en ciudades argentinas:
Buenos Aires, Córdoba, Rosario, Mendoza, La Plata, Tucumán, Salta, Mar del Plata, San Juan.

### Pedidos (35)
Distribución de estados:
- `COMPLETADO`: ~15 pedidos
- `PENDIENTE`: ~8 pedidos
- `ENVIADO`: ~8 pedidos
- `CANCELADO`: ~4 pedidos

Métodos de pago: Tarjeta de crédito, Débito, MercadoPago, Efectivo.

### Resumen del grafo cargado

| Tipo | Cantidad |
|---|---|
| Nodos `User` | 25 |
| Nodos `Product` | 30 |
| Nodos `Order` | 35 |
| Nodos `Category` | 5 |
| Relaciones `REALIZO` | 35 |
| Relaciones `CONTIENE` | ~60 |
| Relaciones `PERTENECE_A` | 30 |
| Relaciones `VISTO` | 26 |
| Relaciones `RESENO` | 25 |
| **Total nodos** | **95** |
| **Total relaciones** | **~176** |

---

## 10. Comparación Neo4j vs SQL Relacional

### Modelo de datos equivalente en SQL

Para implementar el mismo sistema en un RDBMS se necesitarían:

```sql
-- 6 tablas en SQL vs 4 tipos de nodo en Neo4j
CREATE TABLE usuarios (id, nombre, email, edad, ciudad, fecha_registro, puntos);
CREATE TABLE productos (id, nombre, descripcion, precio, stock, marca);
CREATE TABLE categorias (id, nombre, descripcion);
CREATE TABLE pedidos (id, usuario_id, fecha, estado, monto_total, metodo_pago, direccion);
CREATE TABLE pedido_items (pedido_id, producto_id, cantidad, precio_unitario); -- tabla puente
CREATE TABLE categorias_productos (producto_id, categoria_id);                 -- tabla puente
-- Más tablas para vistas y reseñas...
CREATE TABLE vistas (usuario_id, producto_id, timestamp, duracion_segundos);
CREATE TABLE resenas (usuario_id, producto_id, calificacion, comentario, fecha);
```

En Neo4j, las "tablas puente" (`pedido_items`, `categorias_productos`) **son directamente relaciones** con propiedades, eliminando la necesidad de normalización artificial.

### Comparación de consultas clave

**Consulta 8: "¿Quién vio un producto pero no lo compró?"**

*SQL:*
```sql
SELECT u.nombre, u.email FROM usuarios u
JOIN vistas v ON u.id = v.usuario_id
WHERE v.producto_id = ?
  AND u.id NOT IN (
    SELECT DISTINCT pi.usuario_id   -- o subconsulta via pedidos
    FROM pedidos p JOIN pedido_items pi ON p.id = pi.pedido_id
    WHERE pi.producto_id = ? AND p.estado != 'CANCELADO'
  )
```

*Cypher:*
```cypher
MATCH (u:User)-[:VISTO]->(p:Product {id: $id})
WHERE NOT EXISTS { MATCH (u)-[:REALIZO]->(:Order)-[:CONTIENE]->(p) }
RETURN u.nombre, u.email
```

**Filtrado Colaborativo (4 saltos)**

*SQL:* ~20 líneas con 4 JOINs, 2 subconsultas correlacionadas, GROUP BY y HAVING.

*Cypher:* El algoritmo completo en ~15 líneas con traversal natural del grafo.

### Cuándo Neo4j supera a SQL

- Consultas de **múltiples saltos** (más de 2 JOINs encadenados)
- Algoritmos de **similitud y recomendación**
- Detección de **patrones negativos** (quién NO tiene relación con X)
- **Esquema flexible**: agregar una nueva propiedad a una relación no requiere `ALTER TABLE`
- **Rendimiento en grafos densos**: el traversal en Neo4j no degrada con la cantidad de nodos no relacionados
