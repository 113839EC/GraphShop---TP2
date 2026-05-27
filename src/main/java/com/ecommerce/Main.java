package com.ecommerce;

import com.ecommerce.config.Neo4jConfig;
import com.ecommerce.loader.DataLoader;
import com.ecommerce.model.User;
import com.ecommerce.report.ReportService;
import com.ecommerce.service.*;
import com.ecommerce.ui.DashboardWindow;
import javax.swing.SwingUtilities;

import java.util.*;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static UserService           userSvc;
    private static ProductService        productSvc;
    private static OrderService          orderSvc;
    private static RecommendationService recSvc;
    private static ReportService         reportSvc;

    public static void main(String[] args) throws InterruptedException {
        if (args.length > 0 && "--dashboard".equals(args[0])) {
            launchDashboardOnly();
            return;
        }

        banner();

        try {
            Neo4jConfig.getInstance();  // Verificar conexión al inicio
        } catch (Exception e) {
            System.err.println("\n  ✗ No se pudo conectar a Neo4j. Saliendo.");
            System.err.println("  Configure NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD.");
            return;
        }

        userSvc    = new UserService();
        productSvc = new ProductService();
        orderSvc   = new OrderService();
        recSvc     = new RecommendationService();
        reportSvc  = new ReportService();

        boolean corriendo = true;
        while (corriendo) {
            menuPrincipal();
            String opcion;
            try {
                opcion = sc.nextLine().trim();
            } catch (java.util.NoSuchElementException e) {
                // stdin cerrado (ej. redirección de archivo) — dejar al EDT manejar el cierre
                return;
            }
            switch (opcion) {
                case "1" -> menuCargarDatos();
                case "2" -> menuUsuarios();
                case "3" -> menuProductos();
                case "4" -> menuPedidos();
                case "5" -> menuRecomendaciones();
                case "6" -> menuReportes();
                case "7" -> menuConsultasAvanzadas();
                case "8" -> DashboardWindow.launch();
                case "0" -> corriendo = false;
                default  -> System.out.println("  Opción inválida.");
            }
        }

        Neo4jConfig.getInstance().close();
        System.out.println("\n  Conexión cerrada. ¡Hasta luego!");
    }

    // ─── Modo dashboard directo (java -jar graphshop.jar --dashboard) ────────────
    private static void launchDashboardOnly() throws InterruptedException {
        try {
            Neo4jConfig.getInstance();
        } catch (Exception e) {
            System.err.println("  ✗ No se pudo conectar a Neo4j.");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            DashboardWindow w = new DashboardWindow();
            w.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            w.setVisible(true);
        });
        Thread.currentThread().join(); // bloquear hasta que EXIT_ON_CLOSE llame a System.exit()
    }

    // ═══════════════════════════════════════════════════════════════════════
    //   MENÚS
    // ═══════════════════════════════════════════════════════════════════════

    private static void menuPrincipal() {
        System.out.println();
        linea("═", 64);
        System.out.println("  GRAPHSHOP  —  Sistema de E-Commerce sobre Neo4j");
        System.out.println("  BDII  |  Trabajo Práctico 2");
        linea("═", 64);
        System.out.println("  1. Cargar datos de muestra (30 prod | 25 users | 35 pedidos)");
        System.out.println("  2. Gestión de Usuarios");
        System.out.println("  3. Gestión de Productos");
        System.out.println("  4. Pedidos e Historial");
        System.out.println("  5. Recomendaciones (Graph Traversal)");
        System.out.println("  6. Reportes y Analítica");
        System.out.println("  7. Consultas Avanzadas sobre el Grafo");
        System.out.println("  8. Dashboard Visual (ventana grafica)");
        System.out.println("  0. Salir");
        linea("─", 64);
        System.out.print("  Opción: ");
    }

    // ─── 1. Cargar Datos ─────────────────────────────────────────────────────
    private static void menuCargarDatos() {
        System.out.println("\n  ¡ATENCIÓN! Esto eliminará todos los datos existentes.");
        System.out.print("  ¿Confirmar carga de datos de muestra? (s/n): ");
        if ("s".equalsIgnoreCase(sc.nextLine().trim())) {
            new DataLoader().cargarTodosDatos();
            var stats = reportSvc.getGeneralStats();
            System.out.printf("  Usuarios: %d  |  Productos: %d  |  Pedidos: %d%n",
                    stats.get("usuarios"), stats.get("productos"), stats.get("pedidos"));
        } else {
            System.out.println("  Cancelado.");
        }
    }

    // ─── 2. Usuarios ─────────────────────────────────────────────────────────
    private static void menuUsuarios() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n  ── USUARIOS ──────────────────────────────────────────");
            System.out.println("  1. Listar todos los usuarios");
            System.out.println("  2. Buscar por email");
            System.out.println("  3. Usuarios inactivos (sin pedidos)   [Consulta 4]");
            System.out.println("  0. Volver");
            System.out.print("  Opción: ");

            switch (sc.nextLine().trim()) {
                case "1" -> {
                    System.out.println("\n  Lista de usuarios registrados:");
                    linea("─", 90);
                    userSvc.listarTodos().forEach(u -> System.out.println("  " + u));
                }
                case "2" -> {
                    System.out.print("  Email: ");
                    String email = sc.nextLine().trim();
                    userSvc.buscarPorEmail(email)
                           .ifPresentOrElse(
                               u -> System.out.println("  Encontrado: " + u),
                               () -> System.out.println("  Usuario no encontrado.")
                           );
                }
                case "3" -> {
                    System.out.println("\n  ┌─── Consulta 4: Usuarios registrados sin pedidos ──────────────────────┐");
                    System.out.println("  │ Cypher: MATCH (u:User) WHERE NOT EXISTS {                             │");
                    System.out.println("  │           MATCH (u)-[:REALIZO]->(:Order) }                              │");
                    System.out.println("  │         RETURN u.nombre, u.email, u.ciudad, u.fechaRegistro          │");
                    System.out.println("  └───────────────────────────────────────────────────────────────────────┘");
                    List<User> inactivos = userSvc.listarInactivos();
                    if (inactivos.isEmpty()) {
                        System.out.println("  Todos los usuarios han realizado al menos un pedido.");
                    } else {
                        System.out.printf("  %d usuarios inactivos encontrados:%n%n", inactivos.size());
                        inactivos.forEach(u -> System.out.println("  " + u));
                    }
                }
                case "0" -> volver = true;
                default  -> System.out.println("  Opción inválida.");
            }
        }
    }

    // ─── 3. Productos ────────────────────────────────────────────────────────
    private static void menuProductos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n  ── PRODUCTOS ─────────────────────────────────────────");
            System.out.println("  1. Todos los productos con categoría               [Consulta 5]");
            System.out.println("  2. Filtrar por categoría");
            System.out.println("  3. Filtrar por rango de precio");
            System.out.println("  4. Top productos más vendidos                      [Consulta 7]");
            System.out.println("  5. Quién vio este producto y no lo compró          [Consulta 8]");
            System.out.println("  0. Volver");
            System.out.print("  Opción: ");

            switch (sc.nextLine().trim()) {
                case "1" -> {
                    System.out.println("\n  ┌─── Consulta 5: MATCH (p:Product)-[:PERTENECE_A]->(c:Category) ─────────┐");
                    System.out.println("  └───────────────────────────────────────────────────────────────────────┘");
                    var lista = productSvc.listarConCategoria();
                    imprimirProductos(lista);
                }
                case "2" -> {
                    System.out.println("  Categorías: C001=Electrónica  C002=Ropa  C003=Hogar  C004=Deportes  C005=Libros");
                    System.out.print("  ID de categoría: ");
                    String catId = sc.nextLine().trim().toUpperCase();
                    var lista = productSvc.listarPorCategoria(catId);
                    imprimirProductos(lista);
                }
                case "3" -> {
                    System.out.print("  Precio mínimo: $");
                    double min = parseDouble(sc.nextLine().trim());
                    System.out.print("  Precio máximo: $");
                    double max = parseDouble(sc.nextLine().trim());
                    imprimirProductos(productSvc.listarPorPrecio(min, max));
                }
                case "4" -> {
                    System.out.print("  ¿Cuántos productos mostrar? (default 10): ");
                    int n = parseInt(sc.nextLine().trim(), 10);
                    System.out.println("\n  ┌─── Consulta 7: Top productos más vendidos (solo pedidos COMPLETADOS) ┐");
                    System.out.println("  │ Traversal: (Order)-[:CONTIENE]->(Product)                            │");
                    System.out.println("  │ Agrega cantidad usando SUM(c.cantidad)                               │");
                    System.out.println("  └───────────────────────────────────────────────────────────────────────┘");
                    var top = productSvc.topVendidos(n);
                    System.out.println();
                    System.out.printf("  %-42s %-12s %8s %10s %12s%n",
                            "Producto","Marca","Precio","Unidades","Ingresos");
                    linea("─", 90);
                    for (var row : top) {
                        System.out.printf("  %-42s %-12s $%7.2f %10d $%11.2f%n",
                                row.get("nombre"), row.get("marca"), row.get("precio"),
                                row.get("totalUnidades"), row.get("ingresos"));
                    }
                }
                case "5" -> {
                    System.out.print("  ID del producto (ej: P001): ");
                    String pid = sc.nextLine().trim().toUpperCase();
                    System.out.println("\n  ┌─── Consulta 8: Usuarios que vieron el producto y NO lo compraron ────┐");
                    System.out.println("  │ Traversal: (User)-[:VISTO]->(Product)                               │");
                    System.out.println("  │ Filtro negativo: NOT EXISTS { (User)-[:REALIZO]->(Order)-[:CONTIENE]->(Product) } │");
                    System.out.println("  └───────────────────────────────────────────────────────────────────────┘");
                    var lista = productSvc.vieronPeroNoCompraron(pid);
                    if (lista.isEmpty()) {
                        System.out.println("  Ningún usuario vio este producto sin comprarlo (o el ID no existe).");
                    } else {
                        System.out.printf("  %d usuarios visitaron " + pid + " pero no compraron:%n", lista.size());
                        lista.forEach(r -> System.out.printf("  %-25s %-35s %s%n",
                                r.get("nombre"), r.get("email"), r.get("ciudad")));
                    }
                }
                case "0" -> volver = true;
                default  -> System.out.println("  Opción inválida.");
            }
        }
    }

    // ─── 4. Pedidos ──────────────────────────────────────────────────────────
    private static void menuPedidos() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n  ── PEDIDOS ───────────────────────────────────────────");
            System.out.println("  1. Historial de compras de un usuario    [Consulta 9]");
            System.out.println("  0. Volver");
            System.out.print("  Opción: ");

            switch (sc.nextLine().trim()) {
                case "1" -> {
                    System.out.println("  Usuarios activos (ejemplos): U001 U002 U003 U004 U005 ... U020");
                    System.out.print("  ID de usuario: ");
                    String uid = sc.nextLine().trim().toUpperCase();

                    System.out.println("\n  ┌─── Consulta 9: Historial de compras ──────────────────────────────────┐");
                    System.out.println("  │ Traversal: (User)-[:REALIZO]->(Order)-[:CONTIENE]->(Product)            │");
                    System.out.println("  │ Retorna fecha, estado, monto, producto, cantidad y precio unitario    │");
                    System.out.println("  └───────────────────────────────────────────────────────────────────────┘");

                    var historial = orderSvc.historialDeCompras(uid);
                    if (historial.isEmpty()) {
                        System.out.println("  Sin pedidos para este usuario.");
                    } else {
                        String pedidoActual = "";
                        for (var row : historial) {
                            String pid = (String) row.get("pedidoId");
                            if (!pid.equals(pedidoActual)) {
                                pedidoActual = pid;
                                System.out.printf("%n  Pedido %s  |  Fecha: %s  |  Estado: %s  |  Total: $%.2f  |  Pago: %s%n",
                                        pid, row.get("fecha"), row.get("estado"),
                                        row.get("montoTotal"), row.get("metodoPago"));
                                linea("─", 80);
                            }
                            System.out.printf("    %-40s %-12s x%d  @ $%.2f%n",
                                    row.get("producto"), row.get("marca"),
                                    row.get("cantidad"), row.get("precioUnitario"));
                        }
                    }
                }
                case "0" -> volver = true;
                default  -> System.out.println("  Opción inválida.");
            }
        }
    }

    // ─── 5. Recomendaciones ──────────────────────────────────────────────────
    private static void menuRecomendaciones() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n  ── RECOMENDACIONES ───────────────────────────────────");
            System.out.println("  1. Filtrado colaborativo para un usuario  [Consulta Compleja 1]");
            System.out.println("  2. Usuarios similares (Jaccard)           [Consulta Compleja 2]");
            System.out.println("  3. Productos vistos pero no comprados");
            System.out.println("  0. Volver");
            System.out.print("  Opción: ");

            switch (sc.nextLine().trim()) {
                case "1" -> {
                    System.out.print("  ID de usuario (ej: U001): ");
                    String uid = sc.nextLine().trim().toUpperCase();
                    System.out.println("\n  ┌─── Consulta Compleja 1: Filtrado Colaborativo ────────────────────────┐");
                    System.out.println("  │ Algoritmo de 4 saltos:                                               │");
                    System.out.println("  │   (targetUser)-[:REALIZO]->(Order)-[:CONTIENE]->(Product)              │");
                    System.out.println("  │                       <-[:CONTIENE]-(:Order)<-[:REALIZO]-(:OtroUser)   │");
                    System.out.println("  │                         -[:REALIZO]->(:Order)-[:CONTIENE]->(Recomend.) │");
                    System.out.println("  │ Sin JOIN equivalente en SQL — requeriría 4+ JOINs con subconsultas  │");
                    System.out.println("  └───────────────────────────────────────────────────────────────────────┘");

                    var recs = recSvc.recomendacionesColaborativas(uid);
                    if (recs.isEmpty()) {
                        System.out.println("  Sin recomendaciones (este usuario puede no tener pedidos o ser el único).");
                    } else {
                        System.out.printf("  %d recomendaciones para %s:%n%n", recs.size(), uid);
                        System.out.printf("  %-42s %-15s %8s %10s%n", "Producto","Marca","Precio","Usuarios que compraron");
                        linea("─", 85);
                        recs.forEach(r -> System.out.printf("  %-42s %-15s $%7.2f %10d%n",
                                r.get("nombre"), r.get("marca"),
                                r.get("precio"), r.get("usuariosQueLoCompraron")));
                    }
                }
                case "2" -> {
                    System.out.print("  ID de usuario (ej: U001): ");
                    String uid = sc.nextLine().trim().toUpperCase();
                    System.out.println("\n  ┌─── Consulta Compleja 2: Similitud de Jaccard ─────────────────────────┐");
                    System.out.println("  │ Jaccard = |intersección de productos comprados| / |unión|            │");
                    System.out.println("  │ Valor 1.0 = mismos productos; 0.0 = sin coincidencias               │");
                    System.out.println("  │ Traversal: compara conjuntos de nodos Product por usuario            │");
                    System.out.println("  └───────────────────────────────────────────────────────────────────────┘");

                    var sims = recSvc.usuariosSimilares(uid);
                    if (sims.isEmpty()) {
                        System.out.println("  Sin usuarios similares.");
                    } else {
                        System.out.printf("  Usuarios similares a %s:%n%n", uid);
                        System.out.printf("  %-25s %-15s %6s %8s %12s%n",
                                "Usuario","Ciudad","En comun","Total","Jaccard");
                        linea("─", 75);
                        sims.forEach(r -> System.out.printf("  %-25s %-15s %6d %8d %12.2f%n",
                                r.get("usuario"), r.get("ciudad"),
                                r.get("productosEnComun"), r.get("totalUnion"),
                                r.get("similitudJaccard")));
                    }
                }
                case "3" -> {
                    System.out.print("  ID de usuario (ej: U001): ");
                    String uid = sc.nextLine().trim().toUpperCase();
                    var lista = recSvc.vistosNoCamprados(uid);
                    if (lista.isEmpty()) {
                        System.out.println("  Sin productos vistos pendientes de compra.");
                    } else {
                        System.out.printf("  Productos visitados pero no comprados por %s:%n", uid);
                        lista.forEach(r -> System.out.printf(
                                "  %-42s %-12s $%7.2f  [%s]  %ds en sitio%n",
                                r.get("nombre"), r.get("marca"), r.get("precio"),
                                r.get("categoria"), r.get("segundosVisita")));
                    }
                }
                case "0" -> volver = true;
                default  -> System.out.println("  Opción inválida.");
            }
        }
    }

    // ─── 6. Reportes ─────────────────────────────────────────────────────────
    private static void menuReportes() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n  ── REPORTES ──────────────────────────────────────────");
            System.out.println("  1. Ingresos por categoría (con gráfico)");
            System.out.println("  2. Top clientes por gasto total");
            System.out.println("  3. Calificaciones promedio por producto");
            System.out.println("  4. Estadísticas generales del sistema");
            System.out.println("  0. Volver");
            System.out.print("  Opción: ");

            switch (sc.nextLine().trim()) {
                case "1" -> reportSvc.imprimirReporteIngresos();
                case "2" -> {
                    System.out.print("  ¿Cuántos clientes mostrar? (default 10): ");
                    int n = parseInt(sc.nextLine().trim(), 10);
                    reportSvc.imprimirReporteTopClientes(n);
                }
                case "3" -> reportSvc.imprimirReporteCalificaciones();
                case "4" -> {
                    var stats = reportSvc.getGeneralStats();
                    System.out.println();
                    linea("═", 50);
                    System.out.println("  ESTADÍSTICAS GENERALES DEL SISTEMA");
                    linea("─", 50);
                    System.out.printf("  Usuarios registrados : %d%n", stats.get("usuarios"));
                    System.out.printf("  Productos en catálogo: %d%n", stats.get("productos"));
                    System.out.printf("  Total de pedidos     : %d%n", stats.get("pedidos"));
                    System.out.printf("  Pedidos completados  : %d%n", stats.get("pedidosCompletados"));
                    linea("═", 50);
                }
                case "0" -> volver = true;
                default  -> System.out.println("  Opción inválida.");
            }
        }
    }

    // ─── 7. Consultas Avanzadas ───────────────────────────────────────────────
    private static void menuConsultasAvanzadas() {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n  ── CONSULTAS AVANZADAS DEL GRAFO ─────────────────────");
            System.out.println("  Estas consultas demuestran la potencia de Neo4j");
            System.out.println("  frente a bases de datos relacionales.");
            System.out.println();
            System.out.println("  1. Demostración completa de Filtrado Colaborativo");
            System.out.println("     → Para U001 (Carlos): muestra similares + recomendaciones");
            System.out.println("  2. Demostración completa de Jaccard");
            System.out.println("     → Para U010 (Florencia): perfil de lector similar");
            System.out.println("  3. Ver todas las consultas con sus Cypher explicados");
            System.out.println("  0. Volver");
            System.out.print("  Opción: ");

            switch (sc.nextLine().trim()) {
                case "1" -> demoFiltradoColaborativo("U001");
                case "2" -> demoJaccard("U010");
                case "3" -> mostrarResumenConsultas();
                case "0" -> volver = true;
                default  -> System.out.println("  Opción inválida.");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //   DEMOS INTERACTIVAS
    // ═══════════════════════════════════════════════════════════════════════

    private static void demoFiltradoColaborativo(String userId) {
        System.out.println("\n  ══════════════════════════════════════════════════════════");
        System.out.println("  DEMOSTRACIÓN: Filtrado Colaborativo para " + userId);
        System.out.println("  ══════════════════════════════════════════════════════════");

        userSvc.buscarPorId(userId).ifPresent(u -> {
            System.out.println("  Usuario: " + u.getNombre() + " — " + u.getCiudad());
            System.out.println();

            System.out.println("  [PASO 1] Historial de compras del usuario:");
            var historial = orderSvc.historialDeCompras(userId);
            Set<String> productosComprados = new LinkedHashSet<>();
            historial.forEach(r -> {
                String prod = r.get("producto").toString();
                if (productosComprados.add(prod)) {
                    System.out.println("    ✓ " + prod);
                }
            });

            System.out.println();
            System.out.println("  [PASO 2] Usuarios similares encontrados y productos que compraron:");
            var similares = recSvc.usuariosSimilares(userId);
            similares.forEach(r -> System.out.printf("    → %-25s (Jaccard=%.2f, %d productos en común)%n",
                    r.get("usuario"), r.get("similitudJaccard"), r.get("productosEnComun")));

            System.out.println();
            System.out.println("  [PASO 3] Recomendaciones (productos comprados por similares que " + userId + " no tiene):");
            var recs = recSvc.recomendacionesColaborativas(userId);
            recs.forEach(r -> System.out.printf("    ⭐ %-42s $%7.2f  (%d usuarios)%n",
                    r.get("nombre"), r.get("precio"), r.get("usuariosQueLoCompraron")));
        });
    }

    private static void demoJaccard(String userId) {
        System.out.println("\n  ══════════════════════════════════════════════════════════");
        System.out.println("  DEMOSTRACIÓN: Similitud Jaccard para " + userId);
        System.out.println("  ══════════════════════════════════════════════════════════");

        userSvc.buscarPorId(userId).ifPresent(u ->
            System.out.println("  Usuario: " + u.getNombre() + " (" + u.getCiudad() + ")")
        );

        System.out.println();
        var sims = recSvc.usuariosSimilares(userId);
        System.out.println("  Resultado de la Similitud Jaccard:");
        System.out.printf("  %-25s %8s %8s %12s%n", "Usuario", "Comunes", "Union", "Jaccard");
        linea("─", 60);
        sims.forEach(r -> {
            double j = (Double) r.get("similitudJaccard");
            String barra = "█".repeat((int)(j * 20)) + "░".repeat(20 - (int)(j * 20));
            System.out.printf("  %-25s %8d %8d %12.2f  %s%n",
                    r.get("usuario"), r.get("productosEnComun"),
                    r.get("totalUnion"), j, barra);
        });
    }

    private static void mostrarResumenConsultas() {
        System.out.println();
        linea("═", 70);
        System.out.println("  RESUMEN DE LAS 10 CONSULTAS CYPHER IMPLEMENTADAS");
        linea("═", 70);
        System.out.println();
        System.out.println("  ┌─ Consulta 1: Listar categorías ─────────────────────────────────────┐");
        System.out.println("  │ MATCH (c:Category) RETURN c.nombre ORDER BY c.nombre               │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ┌─ Consulta 2: Listar usuarios ───────────────────────────────────────┐");
        System.out.println("  │ MATCH (u:User) RETURN u ORDER BY u.nombre                          │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ┌─ Consulta 3: Buscar usuario por email ─────────────────────────────┐");
        System.out.println("  │ MATCH (u:User {email: $email}) RETURN u                            │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ┌─ Consulta 4: Usuarios inactivos ────────────────────────────────────┐");
        System.out.println("  │ MATCH (u:User) WHERE NOT EXISTS {                                  │");
        System.out.println("  │   MATCH (u)-[:REALIZO]->(:Order) } RETURN u                          │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ┌─ Consulta 5: Productos con categoría ──────────────────────────────┐");
        System.out.println("  │ MATCH (p:Product)-[:PERTENECE_A]->(c:Category) RETURN p, c.nombre   │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ┌─ Consulta 6: Productos por rango de precio ─────────────────────────┐");
        System.out.println("  │ MATCH (p:Product) WHERE p.precio >= $min AND p.precio <= $max       │");
        System.out.println("  │ MATCH (p)-[:PERTENECE_A]->(c) RETURN p, c.nombre                    │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ┌─ Consulta 7: Top productos más vendidos ────────────────────────────┐");
        System.out.println("  │ MATCH (o:Order)-[c:CONTAINS]->(p:Product)                          │");
        System.out.println("  │ WHERE o.estado = 'COMPLETADO'                                      │");
        System.out.println("  │ RETURN p.nombre, SUM(c.cantidad) ORDER BY SUM(c.cantidad) DESC     │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ┌─ Consulta 8: Vieron pero no compraron ─────────────────────────────┐");
        System.out.println("  │ MATCH (u:User)-[:VISTO]->(p:Product {id:$pid})                    │");
        System.out.println("  │ WHERE NOT EXISTS {                                                  │");
        System.out.println("  │   MATCH (u)-[:REALIZO]->(o:Order)-[:CONTIENE]->(p) }                  │");
        System.out.println("  │ RETURN u.nombre, u.email                                           │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ┌─ Consulta 9: Historial de compras ─────────────────────────────────┐");
        System.out.println("  │ MATCH (u:User {id:$uid})-[:REALIZO]->(o:Order)-[c:CONTAINS]->(p)      │");
        System.out.println("  │ RETURN o.id, o.fecha, o.estado, p.nombre, c.cantidad               │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ┌─ Consulta 10: Ingresos por categoría ───────────────────────────────┐");
        System.out.println("  │ MATCH (o:Order)-[c:CONTAINS]->(p:Product)-[:PERTENECE_A]->(cat)     │");
        System.out.println("  │ WHERE o.estado='COMPLETADO'                                        │");
        System.out.println("  │ RETURN cat.nombre, SUM(c.cantidad * c.precioUnitario) AS ingresos  │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ═ CONSULTAS COMPLEJAS CON TRAVERSAL ════════════════════════════════");
        System.out.println();
        System.out.println("  ┌─ Compleja 1: Filtrado Colaborativo (4 saltos) ──────────────────────┐");
        System.out.println("  │ MATCH (target)-[:REALIZO]->(o)-[:CONTIENE]->(mis:Product)              │");
        System.out.println("  │ WITH target, COLLECT(DISTINCT mis) AS misProductos                  │");
        System.out.println("  │ MATCH (otro)-[:REALIZO]->(o2)-[:CONTIENE]->(p) WHERE p IN misProductos│");
        System.out.println("  │ WITH target, misProductos, otro, COUNT(DISTINCT p) AS enComun       │");
        System.out.println("  │ ORDER BY enComun DESC LIMIT 8                                       │");
        System.out.println("  │ MATCH (otro)-[:REALIZO]->(:Order)-[:CONTIENE]->(rec:Product)           │");
        System.out.println("  │ WHERE NOT rec IN misProductos                                       │");
        System.out.println("  │ RETURN rec.nombre, COUNT(DISTINCT otro) AS votos                    │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  ┌─ Compleja 2: Similitud Jaccard ────────────────────────────────────┐");
        System.out.println("  │ MATCH (u1)-[:REALIZO]->(o1)-[:CONTIENE]->(p) WITH u1, COLLECT(id(p)) │");
        System.out.println("  │ MATCH (u2)-[:REALIZO]->(o2)-[:CONTIENE]->(p2) WITH u1,idsU1,u2,       │");
        System.out.println("  │       COLLECT(id(p2)) AS idsU2                                     │");
        System.out.println("  │ WITH [x IN idsU1 WHERE x IN idsU2] AS interseccion, ...            │");
        System.out.println("  │ RETURN toFloat(SIZE(interseccion))/SIZE(union) AS jaccard           │");
        System.out.println("  └─────────────────────────────────────────────────────────────────────┘");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //   HELPERS DE PRESENTACIÓN
    // ═══════════════════════════════════════════════════════════════════════

    private static void banner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                              ║");
        System.out.println("  ║   ██████╗ ██████╗  █████╗ ██████╗ ██╗  ██╗███████╗██╗  ██╗║");
        System.out.println("  ║  ██╔════╝ ██╔══██╗██╔══██╗██╔══██╗██║  ██║██╔════╝██║  ██║║");
        System.out.println("  ║  ██║  ███╗██████╔╝███████║██████╔╝███████║███████╗███████║║");
        System.out.println("  ║  ██║   ██║██╔══██╗██╔══██║██╔═══╝ ██╔══██║╚════██║██╔══██║║");
        System.out.println("  ║  ╚██████╔╝██║  ██║██║  ██║██║     ██║  ██║███████║██║  ██║║");
        System.out.println("  ║   ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝     ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝║");
        System.out.println("  ║                                                              ║");
        System.out.println("  ║          Sistema de E-Commerce sobre Neo4j                  ║");
        System.out.println("  ║          Bases de Datos II — Trabajo Práctico 2             ║");
        System.out.println("  ║                                                              ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void imprimirProductos(List<Map<String, Object>> lista) {
        if (lista.isEmpty()) {
            System.out.println("  Sin productos.");
            return;
        }
        System.out.printf("%n  %-42s %-15s %8s %6s  %s%n",
                "Producto", "Marca", "Precio", "Stock", "Categoría");
        linea("─", 90);
        lista.forEach(r -> System.out.printf("  %-42s %-15s $%7.2f %6d  %s%n",
                r.get("nombre"), r.get("marca"), r.get("precio"),
                r.get("stock"), r.get("categoria")));
    }

    private static void linea(String car, int n) {
        System.out.println("  " + car.repeat(n));
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private static int parseInt(String s, int defVal) {
        try { int v = Integer.parseInt(s); return v > 0 ? v : defVal; }
        catch (Exception e) { return defVal; }
    }
}
