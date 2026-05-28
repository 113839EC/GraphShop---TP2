package com.ecommerce.report;

import com.ecommerce.config.Neo4jConfig;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    private final Neo4jConfig config;

    public ReportService() {
        this.config = Neo4jConfig.getInstance();
    }

    // ─── Reporte 1 — Ingresos por categoría ──────────────────────────────────
    //     Consulta: (Order)-[:CONTIENE]->(Product)-[:PERTENECE_A]->(Category)
    //     Filtra solo pedidos COMPLETADOS y suma cantidad * precioUnitario
    public List<Map<String, Object>> getRevenueByCategory() {
        String query = """
                MATCH (o:Order)-[c:CONTIENE]->(p:Product)-[:PERTENECE_A]->(cat:Category)
                WHERE o.estado = 'COMPLETADO'
                RETURN cat.nombre                              AS categoria,
                       SUM(c.cantidad * c.precioUnitario)      AS ingresos,
                       COUNT(DISTINCT o)                        AS totalPedidos,
                       SUM(c.cantidad)                          AS unidadesVendidas
                ORDER BY ingresos DESC
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query).list().forEach(r -> {
                Map<String, Object> row = new HashMap<>();
                row.put("categoria",        r.get("categoria").asString());
                row.put("ingresos",         r.get("ingresos").asDouble());
                row.put("totalPedidos",     r.get("totalPedidos").asLong());
                row.put("unidadesVendidas", r.get("unidadesVendidas").asLong());
                results.add(row);
            });
        }
        return results;
    }

    // ─── Reporte 2 — Top clientes por gasto total ─────────────────────────────
    //     Traversal: (User)-[:MADE]->(Order) con filtro de estado COMPLETADO
    public List<Map<String, Object>> getTopCustomers(int limit) {
        String query = """
                MATCH (u:User)-[:REALIZO]->(o:Order)
                WHERE o.estado = 'COMPLETADO'
                RETURN u.nombre           AS nombre,
                       u.email            AS email,
                       u.ciudad           AS ciudad,
                       u.puntosFidelidad  AS puntos,
                       SUM(o.montoTotal)  AS totalGastado,
                       COUNT(o)           AS cantidadPedidos
                ORDER BY totalGastado DESC
                LIMIT $limit
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query, Map.of("limit", limit)).list().forEach(r -> {
                Map<String, Object> row = new HashMap<>();
                row.put("nombre",           r.get("nombre").asString());
                row.put("email",            r.get("email").asString());
                row.put("ciudad",           r.get("ciudad").asString());
                row.put("puntos",           r.get("puntos").asInt());
                row.put("totalGastado",     r.get("totalGastado").asDouble());
                row.put("cantidadPedidos",  r.get("cantidadPedidos").asLong());
                results.add(row);
            });
        }
        return results;
    }

    // ─── Reporte 3 — Calificaciones promedio por producto ─────────────────────
    //     Traversal: (User)-[:REVIEWED]->(Product)
    //     Muestra promedio, mín, máx y cantidad de reseñas
    public List<Map<String, Object>> getProductRatings() {
        String query = """
                MATCH (u:User)-[r:RESENO]->(p:Product)
                MATCH (p)-[:PERTENECE_A]->(c:Category)
                RETURN p.nombre                AS nombre,
                       p.marca                 AS marca,
                       c.nombre                AS categoria,
                       AVG(r.calificacion)      AS promedio,
                       MIN(r.calificacion)      AS minCalif,
                       MAX(r.calificacion)      AS maxCalif,
                       COUNT(r)                AS cantidadResenas
                ORDER BY promedio DESC, cantidadResenas DESC
                """;

        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query).list().forEach(r -> {
                Map<String, Object> row = new HashMap<>();
                row.put("nombre",           r.get("nombre").asString());
                row.put("marca",            r.get("marca").asString());
                row.put("categoria",        r.get("categoria").asString());
                row.put("promedio",         r.get("promedio").asDouble());
                row.put("minCalif",         r.get("minCalif").asInt());
                row.put("maxCalif",         r.get("maxCalif").asInt());
                row.put("cantidadResenas",  r.get("cantidadResenas").asLong());
                results.add(row);
            });
        }
        return results;
    }

    // ─── Reporte 4 — Estadísticas generales del sistema ──────────────────────
    public Map<String, Long> getGeneralStats() {
        String query = """
                MATCH (u:User)         WITH COUNT(u) AS totalUsuarios
                MATCH (p:Product)      WITH totalUsuarios, COUNT(p) AS totalProductos
                MATCH (o:Order)        WITH totalUsuarios, totalProductos, COUNT(o) AS totalPedidos
                MATCH (o2:Order)
                WHERE o2.estado = 'COMPLETADO'
                RETURN totalUsuarios, totalProductos, totalPedidos,
                       COUNT(o2) AS pedidosCompletados
                """;

        Map<String, Long> stats = new HashMap<>();
        try (Session session = config.getSession()) {
            var result = session.run(query);
            if (result.hasNext()) {
                var r = result.next();
                stats.put("usuarios",            r.get("totalUsuarios").asLong());
                stats.put("productos",           r.get("totalProductos").asLong());
                stats.put("pedidos",             r.get("totalPedidos").asLong());
                stats.put("pedidosCompletados",  r.get("pedidosCompletados").asLong());
            }
        }
        return stats;
    }

    // ─── Reporte 5 — Pedidos por estado ──────────────────────────────────────
    public List<Map<String, Object>> getOrdersByStatus() {
        String query = """
                MATCH (o:Order)
                RETURN o.estado AS estado, COUNT(o) AS cantidad
                ORDER BY cantidad DESC
                """;
        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query).list().forEach(r -> {
                Map<String, Object> row = new HashMap<>();
                row.put("estado",   r.get("estado").asString());
                row.put("cantidad", r.get("cantidad").asLong());
                results.add(row);
            });
        }
        return results;
    }

    // ─── Reporte 6 — Productos más vistos ────────────────────────────────────
    //     Traversal: (User)-[:VISTO]->(Product)-[:PERTENECE_A]->(Category)
    public List<Map<String, Object>> getMostViewedProducts(int limit) {
        String query = """
                MATCH (u:User)-[v:VISTO]->(p:Product)-[:PERTENECE_A]->(c:Category)
                RETURN p.nombre                         AS nombre,
                       p.marca                          AS marca,
                       c.nombre                         AS categoria,
                       COUNT(v)                         AS totalVistas,
                       AVG(coalesce(v.duracionSegundos, 0)) AS avgSegundos
                ORDER BY totalVistas DESC
                LIMIT $limit
                """;
        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query, Map.of("limit", limit)).list().forEach(r -> {
                Map<String, Object> row = new HashMap<>();
                row.put("nombre",      r.get("nombre").asString());
                row.put("marca",       r.get("marca").asString());
                row.put("categoria",   r.get("categoria").asString());
                row.put("totalVistas", r.get("totalVistas").asLong());
                row.put("avgSegundos", r.get("avgSegundos").asDouble());
                results.add(row);
            });
        }
        return results;
    }

    // ─── Reporte 7 — Ingresos por método de pago ─────────────────────────────
    //     Traversal: (Order)-[:CONTIENE]->(Product), agrupa por metodoPago
    public List<Map<String, Object>> getSalesByPaymentMethod() {
        String query = """
                MATCH (o:Order)-[c:CONTIENE]->(p:Product)
                WHERE o.estado = 'COMPLETADO'
                RETURN o.metodoPago                       AS metodoPago,
                       SUM(c.cantidad * c.precioUnitario) AS ingresos,
                       COUNT(DISTINCT o)                  AS cantidadPedidos
                ORDER BY ingresos DESC
                """;
        List<Map<String, Object>> results = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query).list().forEach(r -> {
                Map<String, Object> row = new HashMap<>();
                row.put("metodoPago",      r.get("metodoPago").asString());
                row.put("ingresos",        r.get("ingresos").asDouble());
                row.put("cantidadPedidos", r.get("cantidadPedidos").asLong());
                results.add(row);
            });
        }
        return results;
    }

    // ─── Visualización: barra ASCII proporcional ──────────────────────────────
    public static String barraAscii(double valor, double maximo, int ancho) {
        int llenos = (int) Math.round((valor / maximo) * ancho);
        return "█".repeat(llenos) + "░".repeat(ancho - llenos);
    }

    public void imprimirReporteIngresos() {
        List<Map<String, Object>> datos = getRevenueByCategory();
        if (datos.isEmpty()) {
            System.out.println("  Sin datos. Cargue los datos de muestra primero.");
            return;
        }

        double maxIngresos = datos.stream()
                .mapToDouble(d -> (Double) d.get("ingresos"))
                .max().orElse(1);

        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════╗");
        System.out.println("  ║            INGRESOS POR CATEGORÍA (pedidos completados)      ║");
        System.out.println("  ╠══════════════════════════╦══════════════╦═══════════╦════════╣");
        System.out.println("  ║ Categoría                ║   Ingresos   ║  Pedidos  ║ Units  ║");
        System.out.println("  ╠══════════════════════════╬══════════════╬═══════════╬════════╣");

        for (Map<String, Object> row : datos) {
            String categoria  = String.format("%-24s", row.get("categoria"));
            String ingresos   = String.format("$%,10.2f",  (Double) row.get("ingresos"));
            String pedidos    = String.format("%9d",       ((Long)  row.get("totalPedidos")).intValue());
            String unidades   = String.format("%6d",       ((Long)  row.get("unidadesVendidas")).intValue());
            System.out.printf("  ║ %s ║ %s ║ %s ║%s ║%n",
                    categoria, ingresos, pedidos, unidades);
            System.out.printf("  ║   %s  ║              ║           ║        ║%n",
                    barraAscii((Double) row.get("ingresos"), maxIngresos, 50));
        }
        System.out.println("  ╚══════════════════════════╩══════════════╩═══════════╩════════╝");
    }

    public void imprimirReporteTopClientes(int limit) {
        List<Map<String, Object>> datos = getTopCustomers(limit);
        if (datos.isEmpty()) {
            System.out.println("  Sin datos.");
            return;
        }

        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                    TOP CLIENTES POR GASTO TOTAL                  ║");
        System.out.println("  ╠═══╦════════════════════╦═══════════════╦══════════╦══════════════╣");
        System.out.println("  ║ # ║ Nombre             ║ Ciudad        ║ Pedidos  ║ Total Gastado║");
        System.out.println("  ╠═══╬════════════════════╬═══════════════╬══════════╬══════════════╣");

        int pos = 1;
        for (Map<String, Object> row : datos) {
            String medalla = switch (pos) { case 1 -> "🥇"; case 2 -> "🥈"; case 3 -> "🥉"; default -> String.format(" %d ", pos); };
            System.out.printf("  ║%s║ %-18s ║ %-13s ║ %8d ║ $%,10.2f ║%n",
                    medalla,
                    row.get("nombre"),
                    row.get("ciudad"),
                    ((Long) row.get("cantidadPedidos")).intValue(),
                    (Double) row.get("totalGastado"));
            pos++;
        }
        System.out.println("  ╚═══╩════════════════════╩═══════════════╩══════════╩══════════════╝");
    }

    public void imprimirReporteCalificaciones() {
        List<Map<String, Object>> datos = getProductRatings();
        if (datos.isEmpty()) {
            System.out.println("  Sin datos.");
            return;
        }

        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("  ║               CALIFICACIONES PROMEDIO POR PRODUCTO               ║");
        System.out.println("  ╠════════════════════════════════════╦═══════════╦══════╦══════════╣");
        System.out.println("  ║ Producto                           ║ Promedio  ║ Min  ║ Reseñas  ║");
        System.out.println("  ╠════════════════════════════════════╬═══════════╬══════╬══════════╣");

        for (Map<String, Object> row : datos) {
            double prom = (Double) row.get("promedio");
            String estrellas = "★".repeat((int) Math.round(prom)) + "☆".repeat(5 - (int) Math.round(prom));
            System.out.printf("  ║ %-34s ║ %s %.1f ║  %d   ║   %6d ║%n",
                    truncate(row.get("nombre").toString(), 34),
                    estrellas,
                    prom,
                    (Integer) row.get("minCalif"),
                    ((Long) row.get("cantidadResenas")).intValue());
        }
        System.out.println("  ╚════════════════════════════════════╩═══════════╩══════╩══════════╝");
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
