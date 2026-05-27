package com.ecommerce.repository;

import com.ecommerce.config.Neo4jConfig;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas complejas de grafo que aprovechan los traversals multi-hop
 * de Neo4j para recomendaciones — algo muy costoso en SQL relacional.
 */
public class RecommendationRepository {

    private final Neo4jConfig config;

    public RecommendationRepository() {
        this.config = Neo4jConfig.getInstance();
    }

    // ─── Consulta Compleja 1: Filtrado Colaborativo ────────────────────────────
    //
    //  Algoritmo:
    //    1. Encontrar los productos que compró el usuario objetivo
    //    2. Encontrar otros usuarios que compraron esos mismos productos
    //       (usuarios "similares")
    //    3. Recomendar los productos que compraron esos usuarios similares
    //       pero que el usuario objetivo aún NO compró
    //
    //  Traversal de 4 saltos:
    //    (targetUser)-[:REALIZO]->(Order)-[:CONTIENE]->(Product)
    //                                              <-[:CONTIENE]-(:Order)
    //                                              <-[:REALIZO]-(:User)
    //                                              -[:REALIZO]->(:Order)
    //                                              -[:CONTIENE]->(Product:recomendado)
    //
    //  Equivalente SQL requeriría 4+ JOINs y subconsultas correlacionadas
    // ──────────────────────────────────────────────────────────────────────────
    public List<Map<String, Object>> getCollaborativeRecommendations(String userId) {
        String query = """
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
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query, Map.of("userId", userId)).list().forEach(r -> {
                Map<String, Object> row = new HashMap<>();
                row.put("nombre",                 r.get("nombre").asString());
                row.put("marca",                  r.get("marca").asString());
                row.put("precio",                 r.get("precio").asDouble());
                row.put("usuariosQueLoCompraron", r.get("usuariosQueLoCompraron").asLong());
                results.add(row);
            });
        }
        return results;
    }

    // ─── Consulta Compleja 2: Similitud de Jaccard entre usuarios ─────────────
    //
    //  Mide qué tan parecidos son dos usuarios en base a sus compras.
    //  Jaccard = |Intersección| / |Unión|
    //
    //  Un Jaccard de 1.0 significa que compraron exactamente los mismos productos.
    //  Un Jaccard de 0.0 significa que no tienen ningún producto en común.
    //
    //  Traversal: calcula conjuntos de productos por usuario y los compara
    //  Este tipo de cálculo de similitud en grafos es nativo en Neo4j.
    // ──────────────────────────────────────────────────────────────────────────
    public List<Map<String, Object>> getSimilarUsersByJaccard(String userId) {
        String query = """
                // Recolectar IDs de productos comprados por el usuario objetivo
                MATCH (u1:User {id: $userId})-[:REALIZO]->(o1:Order)-[:CONTIENE]->(p:Product)
                WITH u1, COLLECT(DISTINCT id(p)) AS idsU1

                // Recolectar IDs de productos comprados por cada otro usuario
                MATCH (u2:User)-[:REALIZO]->(o2:Order)-[:CONTIENE]->(p2:Product)
                WHERE u2 <> u1
                WITH u1, idsU1, u2, COLLECT(DISTINCT id(p2)) AS idsU2

                // Calcular intersección y unión para el coeficiente de Jaccard
                WITH u1, u2,
                     [x IN idsU1 WHERE x IN idsU2]                              AS interseccion,
                     idsU1 + [x IN idsU2 WHERE NOT x IN idsU1]                  AS union_set

                WHERE SIZE(interseccion) > 0

                RETURN u2.nombre AS usuario,
                       u2.ciudad AS ciudad,
                       u2.email  AS email,
                       SIZE(interseccion) AS productosEnComun,
                       SIZE(union_set)    AS totalUnion,
                       round(toFloat(SIZE(interseccion)) / SIZE(union_set) * 100) / 100
                           AS similitudJaccard
                ORDER BY similitudJaccard DESC
                LIMIT 8
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query, Map.of("userId", userId)).list().forEach(r -> {
                Map<String, Object> row = new HashMap<>();
                row.put("usuario",          r.get("usuario").asString());
                row.put("ciudad",           r.get("ciudad").asString());
                row.put("email",            r.get("email").asString());
                row.put("productosEnComun", r.get("productosEnComun").asInt());
                row.put("totalUnion",       r.get("totalUnion").asInt());
                row.put("similitudJaccard", r.get("similitudJaccard").asDouble());
                results.add(row);
            });
        }
        return results;
    }

    // ─── Productos vistos pero no comprados (por usuario) ─────────────────────
    //     Traversal: (User)-[:VISTO]->(Product) sin (User)-[:REALIZO]->...->Product
    public List<Map<String, Object>> getViewedButNotPurchasedByUser(String userId) {
        String query = """
                MATCH (u:User {id: $userId})-[v:VISTO]->(p:Product)
                WHERE NOT EXISTS {
                    MATCH (u)-[:REALIZO]->(o:Order)-[:CONTIENE]->(p)
                }
                MATCH (p)-[:PERTENECE_A]->(c:Category)
                RETURN p.nombre AS nombre, p.marca AS marca, p.precio AS precio,
                       c.nombre AS categoria, v.duracionSegundos AS segundosVisita
                ORDER BY v.duracionSegundos DESC
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query, Map.of("userId", userId)).list().forEach(r -> {
                Map<String, Object> row = new HashMap<>();
                row.put("nombre",         r.get("nombre").asString());
                row.put("marca",          r.get("marca").asString());
                row.put("precio",         r.get("precio").asDouble());
                row.put("categoria",      r.get("categoria").asString());
                row.put("segundosVisita", r.get("segundosVisita").asInt());
                results.add(row);
            });
        }
        return results;
    }
}
