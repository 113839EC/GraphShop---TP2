package com.ecommerce.repository;

import com.ecommerce.config.Neo4jConfig;
import com.ecommerce.model.Product;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductRepository {

    private final Neo4jConfig config;

    public ProductRepository() {
        this.config = Neo4jConfig.getInstance();
    }

    // ─── Consulta 5: Todos los productos con su categoría (traversal básico) ──
    //     Demuestra la relación BELONGS_TO: (Product)-[:PERTENECE_A]->(Category)
    public List<Map<String, Object>> getAllProductsWithCategory() {
        String query = """
                MATCH (p:Product)-[:PERTENECE_A]->(c:Category)
                RETURN p.id AS id, p.nombre AS nombre, p.marca AS marca,
                       p.precio AS precio, p.stock AS stock,
                       p.descripcion AS descripcion, c.nombre AS categoria
                ORDER BY c.nombre, p.nombre
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query).list().forEach(r -> results.add(Map.of(
                    "id",          r.get("id").asString(),
                    "nombre",      r.get("nombre").asString(),
                    "marca",       r.get("marca").asString(),
                    "precio",      r.get("precio").asDouble(),
                    "stock",       r.get("stock").asInt(),
                    "descripcion", r.get("descripcion").asString(),
                    "categoria",   r.get("categoria").asString()
            )));
        }
        return results;
    }

    // ─── Consulta 6: Productos por categoría ──────────────────────────────────
    public List<Map<String, Object>> getProductsByCategory(String categoriaId) {
        String query = """
                MATCH (p:Product)-[:PERTENECE_A]->(c:Category {id: $catId})
                RETURN p.id AS id, p.nombre AS nombre, p.marca AS marca,
                       p.precio AS precio, p.stock AS stock, c.nombre AS categoria
                ORDER BY p.precio DESC
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query, Map.of("catId", categoriaId)).list().forEach(r -> results.add(Map.of(
                    "id",       r.get("id").asString(),
                    "nombre",   r.get("nombre").asString(),
                    "marca",    r.get("marca").asString(),
                    "precio",   r.get("precio").asDouble(),
                    "stock",    r.get("stock").asInt(),
                    "categoria",r.get("categoria").asString()
            )));
        }
        return results;
    }

    // ─── Consulta 7: Productos más vendidos ───────────────────────────────────
    //     Traversal: (Order)-[:CONTIENE]->(Product)
    //     Agrega unidades vendidas solo de pedidos COMPLETADOS
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        String query = """
                MATCH (o:Order)-[c:CONTIENE]->(p:Product)
                WHERE o.estado = 'COMPLETADO'
                RETURN p.nombre AS nombre, p.marca AS marca, p.precio AS precio,
                       SUM(c.cantidad) AS totalUnidades,
                       COUNT(DISTINCT o) AS totalPedidos,
                       SUM(c.cantidad * c.precioUnitario) AS ingresos
                ORDER BY totalUnidades DESC
                LIMIT $limit
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query, Map.of("limit", limit)).list().forEach(r -> results.add(Map.of(
                    "nombre",       r.get("nombre").asString(),
                    "marca",        r.get("marca").asString(),
                    "precio",       r.get("precio").asDouble(),
                    "totalUnidades",r.get("totalUnidades").asLong(),
                    "totalPedidos", r.get("totalPedidos").asLong(),
                    "ingresos",     r.get("ingresos").asDouble()
            )));
        }
        return results;
    }

    // ─── Consulta 8: Usuarios que vieron un producto pero NO lo compraron ─────
    //     Demuestra el patrón de matching negativo con NOT EXISTS
    //     Util para campañas de retargeting en e-commerce
    public List<Map<String, Object>> getUsersWhoViewedButNotPurchased(String productId) {
        String query = """
                MATCH (u:User)-[:VISTO]->(p:Product {id: $productId})
                WHERE NOT EXISTS {
                    MATCH (u)-[:REALIZO]->(o:Order)-[:CONTIENE]->(p)
                }
                RETURN u.nombre AS nombre, u.email AS email, u.ciudad AS ciudad
                ORDER BY u.nombre
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query, Map.of("productId", productId)).list().forEach(r -> results.add(Map.of(
                    "nombre", r.get("nombre").asString(),
                    "email",  r.get("email").asString(),
                    "ciudad", r.get("ciudad").asString()
            )));
        }
        return results;
    }

    // ─── Buscar productos por rango de precio ─────────────────────────────────
    public List<Map<String, Object>> getProductsByPriceRange(double min, double max) {
        String query = """
                MATCH (p:Product)-[:PERTENECE_A]->(c:Category)
                WHERE p.precio >= $min AND p.precio <= $max
                RETURN p.id AS id, p.nombre AS nombre, p.marca AS marca,
                       p.precio AS precio, p.stock AS stock, c.nombre AS categoria
                ORDER BY p.precio ASC
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query, Map.of("min", min, "max", max)).list().forEach(r -> results.add(Map.of(
                    "id",       r.get("id").asString(),
                    "nombre",   r.get("nombre").asString(),
                    "marca",    r.get("marca").asString(),
                    "precio",   r.get("precio").asDouble(),
                    "stock",    r.get("stock").asInt(),
                    "categoria",r.get("categoria").asString()
            )));
        }
        return results;
    }

    public void createProduct(Product product, String categoryId) {
        String query = """
                MATCH (c:Category {id: $catId})
                MERGE (p:Product {id: $id})
                SET p.nombre       = $nombre,
                    p.descripcion  = $descripcion,
                    p.precio       = $precio,
                    p.stock        = $stock,
                    p.marca        = $marca
                MERGE (p)-[:PERTENECE_A]->(c)
                """;

        try (Session session = config.getSession()) {
            session.run(query, Map.of(
                    "catId",       categoryId,
                    "id",          product.getId(),
                    "nombre",      product.getNombre(),
                    "descripcion", product.getDescripcion(),
                    "precio",      product.getPrecio(),
                    "stock",       product.getStock(),
                    "marca",       product.getMarca()
            ));
        }
    }

    public void addViewedRelationship(String userId, String productId,
                                      String timestamp, int duracionSeg) {
        String query = """
                MATCH (u:User {id: $userId})
                MATCH (p:Product {id: $productId})
                MERGE (u)-[v:VISTO {timestamp: $timestamp}]->(p)
                SET v.duracionSegundos = $duracion
                """;

        try (Session session = config.getSession()) {
            session.run(query, Map.of(
                    "userId",    userId,
                    "productId", productId,
                    "timestamp", timestamp,
                    "duracion",  duracionSeg
            ));
        }
    }

    public void addReviewedRelationship(String userId, String productId,
                                        int calificacion, String comentario, String fecha) {
        String query = """
                MATCH (u:User {id: $userId})
                MATCH (p:Product {id: $productId})
                MERGE (u)-[r:RESENO]->(p)
                SET r.calificacion = $calificacion,
                    r.comentario   = $comentario,
                    r.fecha        = $fecha
                """;

        try (Session session = config.getSession()) {
            session.run(query, Map.of(
                    "userId",       userId,
                    "productId",    productId,
                    "calificacion", calificacion,
                    "comentario",   comentario,
                    "fecha",        fecha
            ));
        }
    }

    private Product mapProduct(Record r) {
        return new Product(
                r.get("id").asString(),
                r.get("nombre").asString(),
                r.get("descripcion").asString(),
                r.get("precio").asDouble(),
                r.get("stock").asInt(),
                r.get("marca").asString()
        );
    }
}
