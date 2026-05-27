package com.ecommerce.repository;

import com.ecommerce.config.Neo4jConfig;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {

    private final Neo4jConfig config;

    public OrderRepository() {
        this.config = Neo4jConfig.getInstance();
    }

    // ─── Consulta 9: Historial de compras de un usuario ──────────────────────
    //     Traversal: (User)-[:REALIZO]->(Order)-[:CONTIENE]->(Product)
    //     Muestra toda la cadena de relaciones en 2 saltos
    public List<Map<String, Object>> getUserPurchaseHistory(String userId) {
        String query = """
                MATCH (u:User {id: $userId})-[:REALIZO]->(o:Order)-[c:CONTIENE]->(p:Product)
                RETURN o.id AS pedidoId, o.fecha AS fecha, o.estado AS estado,
                       o.montoTotal AS montoTotal, o.metodoPago AS metodoPago,
                       p.nombre AS producto, p.marca AS marca,
                       c.cantidad AS cantidad, c.precioUnitario AS precioUnitario
                ORDER BY o.fecha DESC, p.nombre
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query, Map.of("userId", userId)).list().forEach(r -> {
                Map<String, Object> row = new HashMap<>();
                row.put("pedidoId",      r.get("pedidoId").asString());
                row.put("fecha",         r.get("fecha").asString());
                row.put("estado",        r.get("estado").asString());
                row.put("montoTotal",    r.get("montoTotal").asDouble());
                row.put("metodoPago",    r.get("metodoPago").asString());
                row.put("producto",      r.get("producto").asString());
                row.put("marca",         r.get("marca").asString());
                row.put("cantidad",      r.get("cantidad").asInt());
                row.put("precioUnitario",r.get("precioUnitario").asDouble());
                results.add(row);
            });
        }
        return results;
    }

    // ─── Crear pedido con sus items ───────────────────────────────────────────
    //     Crea nodo Order, la relación MADE desde el usuario
    //     y relaciones CONTAINS hacia cada producto con propiedades
    public void createOrder(Order order, List<OrderItem> items) {
        try (Session session = config.getSession()) {
            // 1. Crear el nodo Order y vincularlo al usuario
            String orderQuery = """
                    MATCH (u:User {id: $userId})
                    CREATE (o:Order {
                        id:             $id,
                        fecha:          $fecha,
                        estado:         $estado,
                        montoTotal:     $montoTotal,
                        metodoPago:     $metodoPago,
                        direccionEnvio: $direccionEnvio
                    })
                    CREATE (u)-[:REALIZO]->(o)
                    """;

            session.run(orderQuery, Map.of(
                    "userId",          order.getUsuarioId(),
                    "id",              order.getId(),
                    "fecha",           order.getFecha(),
                    "estado",          order.getEstado(),
                    "montoTotal",      order.getMontoTotal(),
                    "metodoPago",      order.getMetodoPago(),
                    "direccionEnvio",  order.getDireccionEnvio()
            ));

            // 2. Crear relaciones CONTAINS con cantidad y precio unitario
            String itemQuery = """
                    MATCH (o:Order {id: $orderId})
                    MATCH (p:Product {id: $productId})
                    CREATE (o)-[:CONTAINS {cantidad: $cantidad, precioUnitario: $precio}]->(p)
                    """;

            for (OrderItem item : items) {
                session.run(itemQuery, Map.of(
                        "orderId",   order.getId(),
                        "productId", item.getProductoId(),
                        "cantidad",  item.getCantidad(),
                        "precio",    item.getPrecioUnitario()
                ));
            }
        }
    }
}
