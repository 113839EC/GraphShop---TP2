package com.ecommerce.loader;

import com.ecommerce.config.Neo4jConfig;
import org.neo4j.driver.Session;

import java.util.Map;

/**
 * Carga datos de muestra realistas para GraphShop.
 *
 * Nodos creados:
 *   - 5  Category
 *   - 30 Product  (6 por categoría)
 *   - 25 User     (20 activos + 5 inactivos)
 *   - 35 Order
 *
 * Relaciones creadas:
 *   - PERTENECE_A  (Product → Category)   30 relaciones
 *   - REALIZO     (User → Order)          35 relaciones
 *   - CONTIENE    (Order → Product)      ~80 relaciones  (con cantidad y precioUnitario)
 *   - VISTO       (User → Product)       ~30 relaciones  (con timestamp y duracionSegundos)
 *   - RESENO      (User → Product)       ~25 relaciones  (con calificacion, comentario, fecha)
 */
public class DataLoader {

    private final Neo4jConfig config;

    public DataLoader() {
        this.config = Neo4jConfig.getInstance();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //   PUNTO DE ENTRADA PRINCIPAL
    // ═════════════════════════════════════════════════════════════════════════

    public void cargarTodosDatos() {
        System.out.println("\n  Iniciando carga de datos...");
        limpiarDatos();
        crearConstraintsEIndices();
        crearCategorias();
        crearProductos();
        crearUsuarios();
        crearPedidos();
        crearVistas();
        crearResenas();
        System.out.println("  ✓ Carga completada exitosamente.\n");
    }

    // ─── 1. Limpiar base de datos ─────────────────────────────────────────────
    private void limpiarDatos() {
        System.out.print("    Eliminando datos anteriores... ");
        try (Session s = config.getSession()) {
            s.run("MATCH (n) DETACH DELETE n");
        }
        System.out.println("OK");
    }

    // ─── 2. Constraints e Índices ─────────────────────────────────────────────
    private void crearConstraintsEIndices() {
        System.out.print("    Creando constraints e índices... ");
        try (Session s = config.getSession()) {
            s.run("CREATE CONSTRAINT user_id     IF NOT EXISTS FOR (u:User)     REQUIRE u.id IS UNIQUE");
            s.run("CREATE CONSTRAINT product_id  IF NOT EXISTS FOR (p:Product)  REQUIRE p.id IS UNIQUE");
            s.run("CREATE CONSTRAINT category_id IF NOT EXISTS FOR (c:Category) REQUIRE c.id IS UNIQUE");
            s.run("CREATE CONSTRAINT order_id    IF NOT EXISTS FOR (o:Order)    REQUIRE o.id IS UNIQUE");
            s.run("CREATE INDEX user_email       IF NOT EXISTS FOR (u:User)     ON (u.email)");
        }
        System.out.println("OK");
    }

    // ─── 3. Categorías ───────────────────────────────────────────────────────
    private void crearCategorias() {
        System.out.print("    Creando categorías... ");
        String q = "MERGE (c:Category {id:$id}) SET c.nombre=$nombre, c.descripcion=$desc";
        Object[][] cats = {
            {"C001","Electrónica",     "Smartphones, laptops, tablets y accesorios tecnológicos"},
            {"C002","Ropa y Moda",     "Ropa para hombre, mujer y niños de las mejores marcas"},
            {"C003","Hogar y Jardín",  "Muebles, decoración, electrodomésticos y herramientas"},
            {"C004","Deportes",        "Equipamiento deportivo, ropa y accesorios para el deporte"},
            {"C005","Libros y Cultura","Libros, juegos de mesa y material educativo"},
        };
        try (Session s = config.getSession()) {
            for (Object[] c : cats) {
                s.run(q, Map.of("id", c[0], "nombre", c[1], "desc", c[2]));
            }
        }
        System.out.println("OK (5)");
    }

    // ─── 4. Productos ─────────────────────────────────────────────────────────
    private void crearProductos() {
        System.out.print("    Creando productos... ");
        String q = """
                MATCH (c:Category {id:$catId})
                MERGE (p:Product {id:$id})
                SET p.nombre=$nombre, p.descripcion=$desc, p.precio=$precio,
                    p.stock=$stock, p.marca=$marca
                MERGE (p)-[:PERTENECE_A]->(c)
                """;
        // {id, catId, nombre, desc, precio, stock, marca}
        Object[][] prods = {
            // ─── Electrónica ───────────────────────────────────────────────
            {"P001","C001","iPhone 15 Pro",         "Smartphone Apple con chip A17 Pro, cámara 48MP y pantalla OLED",              1299.99, 85,  "Apple"},
            {"P002","C001","Samsung Galaxy S24",    "Smartphone Android con IA integrada, batería 4000mAh y pantalla Dynamic AMOLED",999.99,120,  "Samsung"},
            {"P003","C001","MacBook Air M3",        "Laptop ultradelgada con chip M3, 8GB RAM, 256GB SSD y batería de 18 horas",   1499.99, 45,  "Apple"},
            {"P004","C001","iPad Pro 12.9",         "Tablet profesional con chip M2, pantalla Liquid Retina XDR y Apple Pencil 2", 1099.99, 60,  "Apple"},
            {"P005","C001","Sony WH-1000XM5",       "Auriculares inalámbricos con cancelación de ruido líder en la industria",      399.99,200,  "Sony"},
            {"P006","C001","Nintendo Switch OLED",  "Consola híbrida con pantalla OLED de 7 pulgadas y mandos Joy-Con",             349.99,150,  "Nintendo"},
            // ─── Ropa y Moda ────────────────────────────────────────────────
            {"P007","C002","Zapatillas Air Max 270","Zapatillas deportivas con tecnología Air Max para máxima comodidad",           189.99,300,  "Nike"},
            {"P008","C002","Remera Básica Pack x3", "Pack de 3 remeras básicas de algodón 100%, disponible en tallas S-XXL",        39.99,500,  "Zara"},
            {"P009","C002","Jeans 501 Slim",        "Jeans clásico Levi's en corte slim, denim premium de alta durabilidad",        89.99,250,  "Levi's"},
            {"P010","C002","Campera Thermoball",    "Campera de plumas sintéticas Thermoball para climas fríos y montaña",          299.99,100,  "The North Face"},
            {"P011","C002","Vestido Floral",        "Vestido casual con estampado floral, ideal para primavera y verano",            59.99,180,  "H&M"},
            {"P012","C002","Mochila 25L Urban",     "Mochila urbana de 25 litros con compartimento para laptop y diseño ergonómico",129.99,120,  "Samsonite"},
            // ─── Hogar y Jardín ─────────────────────────────────────────────
            {"P013","C003","Sillón Reclinable Oslo","Sillón reclinable de cuero sintético con reposapiés y posición TV",            449.99, 40,  "IKEA"},
            {"P014","C003","Sábanas Premium 500H",  "Set de sábanas de microfibra 500 hilos, suaves y resistentes al lavado",        79.99,300,  "Casa & Estilo"},
            {"P015","C003","Cafetera Espresso",     "Cafetera de cápsula Nespresso compatible con 19 bares de presión",             349.99, 75,  "De'Longhi"},
            {"P016","C003","Aspiradora Robot i7+",  "Robot aspiradora con vaciado automático y navegación con IA por cámara",       699.99, 30,  "iRobot"},
            {"P017","C003","Lámpara de Pie Hue",    "Lámpara de pie inteligente Philips Hue con 16 millones de colores y voz",      249.99, 90,  "Philips"},
            {"P018","C003","Mesa de Centro Lack",   "Mesa de centro minimalista de madera lacada, 90x55cm, varios colores",          89.99,150,  "IKEA"},
            // ─── Deportes ────────────────────────────────────────────────────
            {"P019","C004","Bicicleta Marlin 5",    "Bicicleta de montaña con cuadro de aluminio, 21 velocidades y frenos de disco",899.99, 25,  "Trek"},
            {"P020","C004","Pesas Ajustables 32kg", "Set de pesas ajustables de 2 a 32kg en 15 configuraciones, antideslizantes",  299.99, 60,  "PowerBlock"},
            {"P021","C004","Raqueta Blade 98",      "Raqueta de tenis profesional con tecnología Countervail para menos vibración",219.99, 80,  "Wilson"},
            {"P022","C004","Colchoneta de Yoga 5mm","Colchoneta antideslizante de 5mm, con correa de transporte y diseño Premium",   68.00,200,  "Lululemon"},
            {"P023","C004","Pelota Fútbol CL",      "Pelota oficial Champions League UEFA con tecnología Thermobonding",              79.99,400,  "Adidas"},
            {"P024","C004","Cinta de Correr T100",  "Cinta de correr plegable con velocidad hasta 16km/h y pantalla LCD",           599.99, 20,  "Domyos"},
            // ─── Libros y Cultura ────────────────────────────────────────────
            {"P025","C005","El Señor de los Anillos","Trilogía completa de J.R.R. Tolkien, edición especial con ilustraciones",       45.99,200,  "Minotauro"},
            {"P026","C005","Clean Code",            "Guía de principios SOLID y buenas prácticas de programación de Robert C. Martin",49.99,150,  "Prentice Hall"},
            {"P027","C005","Cien Años de Soledad",  "Obra maestra de Gabriel García Márquez, Premio Nobel de Literatura",             29.99,300,  "Sudamericana"},
            {"P028","C005","El Problema de 3 Cuerpos","Trilogía de ciencia ficción de Liu Cixin, bestseller internacional",           35.99,180,  "Nova"},
            {"P029","C005","Atomic Habits",         "Guía de James Clear para construir hábitos con pequeños cambios consistentes",   28.99,250,  "Paidós"},
            {"P030","C005","System Design Interview","Guía de Alex Xu para entrevistas de diseño de sistemas de software",            59.99,100,  "ByteByteGo"},
        };
        try (Session s = config.getSession()) {
            for (Object[] p : prods) {
                s.run(q, Map.of(
                    "id", p[0], "catId", p[1], "nombre", p[2], "desc", p[3],
                    "precio", p[4], "stock", p[5], "marca", p[6]
                ));
            }
        }
        System.out.println("OK (30)");
    }

    // ─── 5. Usuarios ─────────────────────────────────────────────────────────
    private void crearUsuarios() {
        System.out.print("    Creando usuarios... ");
        String q = """
                MERGE (u:User {id:$id})
                SET u.nombre=$nombre, u.email=$email, u.edad=$edad,
                    u.ciudad=$ciudad, u.fechaRegistro=$fecha, u.puntosFidelidad=$puntos
                """;
        // {id, nombre, email, edad, ciudad, fechaRegistro, puntosFidelidad}
        Object[][] users = {
            // Activos
            {"U001","Carlos Rodríguez",   "carlos.rodriguez@gmail.com",   35, "Buenos Aires", "2022-03-10", 4850},
            {"U002","María González",     "maria.gonzalez@gmail.com",     28, "Córdoba",       "2022-06-15", 3200},
            {"U003","Juan Martínez",      "juan.martinez@gmail.com",      42, "Rosario",       "2021-11-20", 5600},
            {"U004","Ana López",          "ana.lopez@gmail.com",          31, "Mendoza",       "2022-08-05", 1800},
            {"U005","Pedro Sánchez",      "pedro.sanchez@gmail.com",      25, "La Plata",      "2023-01-12", 2300},
            {"U006","Lucía Fernández",    "lucia.fernandez@gmail.com",    33, "Buenos Aires",  "2022-04-22", 1500},
            {"U007","Diego García",       "diego.garcia@gmail.com",       38, "Tucumán",       "2021-09-08", 3700},
            {"U008","Valentina Díaz",     "valentina.diaz@gmail.com",     22, "Salta",         "2023-03-30", 1200},
            {"U009","Martín Ruiz",        "martin.ruiz@gmail.com",        45, "Mar del Plata", "2021-07-14", 4100},
            {"U010","Florencia Torres",   "florencia.torres@gmail.com",   29, "Buenos Aires",  "2022-10-01", 2900},
            {"U011","Rodrigo Pérez",      "rodrigo.perez@gmail.com",      36, "Córdoba",       "2022-02-18", 2100},
            {"U012","Camila Romero",      "camila.romero@gmail.com",      27, "Rosario",       "2023-05-10", 950},
            {"U013","Santiago Morales",   "santiago.morales@gmail.com",   41, "Buenos Aires",  "2021-12-03", 6200},
            {"U014","Natalia Jiménez",    "natalia.jimenez@gmail.com",    30, "Mendoza",       "2022-09-25", 1650},
            {"U015","Tomás Álvarez",      "tomas.alvarez@gmail.com",      26, "Santa Fe",      "2023-02-08", 1900},
            {"U016","Agustina Castro",    "agustina.castro@gmail.com",    34, "La Plata",      "2022-07-19", 800},
            {"U017","Facundo Vargas",     "facundo.vargas@gmail.com",     39, "Buenos Aires",  "2021-10-11", 700},
            {"U018","Micaela Ramos",      "micaela.ramos@gmail.com",      23, "Córdoba",       "2023-04-05", 0},
            {"U019","Ignacio Herrera",    "ignacio.herrera@gmail.com",    47, "Tucumán",       "2021-08-22", 2400},
            {"U020","Celeste Méndez",     "celeste.mendez@gmail.com",     32, "Buenos Aires",  "2022-11-30", 3300},
            // Inactivos (nunca realizaron pedidos — usados para la Consulta 4)
            {"U021","Emilio Santos",      "emilio.santos@gmail.com",      37, "Rosario",       "2024-01-15", 0},
            {"U022","Pamela Torres",      "pamela.torres@gmail.com",      24, "Mar del Plata", "2024-02-20", 0},
            {"U023","Ricardo Molina",     "ricardo.molina@gmail.com",     43, "Salta",         "2024-03-10", 0},
            {"U024","Antonella Guerrero", "antonella.guerrero@gmail.com", 28, "Buenos Aires",  "2024-04-05", 0},
            {"U025","Leandro Blanco",     "leandro.blanco@gmail.com",     31, "Córdoba",       "2024-05-01", 0},
        };
        try (Session s = config.getSession()) {
            for (Object[] u : users) {
                s.run(q, Map.of("id",u[0],"nombre",u[1],"email",u[2],"edad",u[3],
                                "ciudad",u[4],"fecha",u[5],"puntos",u[6]));
            }
        }
        System.out.println("OK (25 — 20 activos, 5 inactivos)");
    }

    // ─── 6. Pedidos ──────────────────────────────────────────────────────────
    private void crearPedidos() {
        System.out.print("    Creando pedidos... ");

        String createOrder = """
                MATCH (u:User {id:$userId})
                CREATE (o:Order {
                    id:$id, fecha:$fecha, estado:$estado,
                    montoTotal:$total, metodoPago:$pago, direccionEnvio:$dir
                })
                CREATE (u)-[:REALIZO]->(o)
                """;

        String addItem = """
                MATCH (o:Order {id:$oId})
                MATCH (p:Product {id:$pId})
                CREATE (o)-[:CONTIENE {cantidad:$cant, precioUnitario:$precio}]->(p)
                """;

        // {orderId, userId, fecha, estado, total, pago, dir, items:[{pId,cant,precio}]}
        Object[][][] pedidos = {
            // U001 Carlos - compradores tech
            {{"O001","U001","2024-03-15","COMPLETADO", 1699.98,"Tarjeta de crédito","Av. Corrientes 1500, CABA"},
             {"P001","1","1299.99"},{"P005","1","399.99"}},
            {{"O002","U001","2024-07-20","COMPLETADO", 1499.99,"MercadoPago","Av. Corrientes 1500, CABA"},
             {"P003","1","1499.99"}},
            // U002 María
            {{"O003","U002","2024-02-10","COMPLETADO", 2399.98,"Tarjeta de crédito","B° Alberdi 340, Córdoba"},
             {"P001","1","1299.99"},{"P004","1","1099.99"}},
            {{"O004","U002","2024-08-05","ENVIADO",    1399.98,"Débito","B° Alberdi 340, Córdoba"},
             {"P002","1","999.99"},{"P005","1","399.99"}},
            // U003 Juan
            {{"O005","U003","2024-01-22","COMPLETADO", 1899.98,"Tarjeta de crédito","San Martín 820, Rosario"},
             {"P003","1","1499.99"},{"P005","1","399.99"}},
            {{"O006","U003","2024-06-18","COMPLETADO",  349.99,"MercadoPago","San Martín 820, Rosario"},
             {"P006","1","349.99"}},
            // U004 Ana - ropa
            {{"O007","U004","2024-04-12","COMPLETADO",  279.98,"Débito","Las Heras 215, Mendoza"},
             {"P007","1","189.99"},{"P009","1","89.99"}},
            {{"O008","U004","2024-09-01","COMPLETADO",  429.98,"Tarjeta de crédito","Las Heras 215, Mendoza"},
             {"P010","1","299.99"},{"P012","1","129.99"}},
            // U005 Pedro - deportes
            {{"O009","U005","2024-03-28","COMPLETADO", 1089.98,"MercadoPago","Italia 560, La Plata"},
             {"P007","1","189.99"},{"P019","1","899.99"}},
            {{"O010","U005","2024-10-05","PENDIENTE",   227.98,"Débito","Italia 560, La Plata"},
             {"P023","2","79.99"},{"P022","1","68.00"}},
            // U006 Lucía - ropa
            {{"O011","U006","2024-05-20","COMPLETADO",  239.95,"Tarjeta de crédito","Palermo 320, CABA"},
             {"P008","3","39.99"},{"P011","2","59.99"}},
            {{"O012","U006","2024-11-10","COMPLETADO",  389.98,"MercadoPago","Palermo 320, CABA"},
             {"P009","1","89.99"},{"P010","1","299.99"}},
            // U007 Diego - hogar
            {{"O013","U007","2024-02-28","COMPLETADO",  539.98,"Tarjeta de crédito","San Juan 750, Tucumán"},
             {"P013","1","449.99"},{"P018","1","89.99"}},
            {{"O014","U007","2024-07-15","COMPLETADO",  699.99,"MercadoPago","San Juan 750, Tucumán"},
             {"P016","1","699.99"}},
            // U008 Valentina - hogar
            {{"O015","U008","2024-04-05","COMPLETADO",  509.97,"Débito","Urquiza 180, Salta"},
             {"P014","2","79.99"},{"P015","1","349.99"}},
            {{"O016","U008","2024-08-22","ENVIADO",     249.99,"Tarjeta de crédito","Urquiza 180, Salta"},
             {"P017","1","249.99"}},
            // U009 Martín - hogar
            {{"O017","U009","2024-03-10","COMPLETADO",  799.98,"Tarjeta de crédito","Güemes 900, Mar del Plata"},
             {"P015","1","349.99"},{"P013","1","449.99"}},
            {{"O018","U009","2024-09-14","COMPLETADO",  339.96,"MercadoPago","Güemes 900, Mar del Plata"},
             {"P014","2","79.99"},{"P018","2","89.99"}},
            // U010 Florencia - libros
            {{"O019","U010","2024-01-15","COMPLETADO",  125.97,"Tarjeta de crédito","Recoleta 440, CABA"},
             {"P025","1","45.99"},{"P026","1","49.99"},{"P027","1","29.99"}},
            {{"O020","U010","2024-06-30","COMPLETADO",   64.98,"MercadoPago","Recoleta 440, CABA"},
             {"P028","1","35.99"},{"P029","1","28.99"}},
            // U011 Rodrigo - libros
            {{"O021","U011","2024-02-20","COMPLETADO",  109.98,"Débito","Nueva Córdoba 220, Córdoba"},
             {"P026","1","49.99"},{"P030","1","59.99"}},
            {{"O022","U011","2024-08-08","COMPLETADO",   65.98,"MercadoPago","Nueva Córdoba 220, Córdoba"},
             {"P027","1","29.99"},{"P028","1","35.99"}},
            // U012 Camila - libros
            {{"O023","U012","2024-03-05","COMPLETADO",   74.98,"Tarjeta de crédito","Pichincha 380, Rosario"},
             {"P025","1","45.99"},{"P029","1","28.99"}},
            {{"O024","U012","2024-10-20","PENDIENTE",    59.99,"MercadoPago","Pichincha 380, Rosario"},
             {"P030","1","59.99"}},
            // U013 Santiago - mixed (tech + deportes)
            {{"O025","U013","2024-04-18","COMPLETADO", 1489.98,"Tarjeta de crédito","Puerto Madero 10, CABA"},
             {"P001","1","1299.99"},{"P007","1","189.99"}},
            {{"O026","U013","2024-09-25","COMPLETADO",  949.98,"MercadoPago","Puerto Madero 10, CABA"},
             {"P019","1","899.99"},{"P026","1","49.99"}},
            // U014 Natalia
            {{"O027","U014","2024-05-10","COMPLETADO",  417.99,"Débito","Chacras 85, Mendoza"},
             {"P015","1","349.99"},{"P022","1","68.00"}},
            // U015 Tomás - deportes
            {{"O028","U015","2024-06-22","COMPLETADO",  519.98,"Tarjeta de crédito","Bv. Gálvez 612, Santa Fe"},
             {"P020","1","299.99"},{"P021","1","219.99"}},
            // U016 Agustina - ropa
            {{"O029","U016","2024-07-08","COMPLETADO",  319.93,"MercadoPago","Rivadavia 300, La Plata"},
             {"P008","5","39.99"},{"P011","2","59.99"}},
            // U017 Facundo
            {{"O030","U017","2024-08-14","COMPLETADO",  349.99,"Débito","Villa Urquiza 780, CABA"},
             {"P006","1","349.99"}},
            // U018 Micaela (cancelado)
            {{"O031","U018","2024-09-02","CANCELADO",   999.99,"Tarjeta de crédito","Bernal 200, Córdoba"},
             {"P002","1","999.99"}},
            // U019 Ignacio - deportes
            {{"O032","U019","2024-10-12","COMPLETADO",  899.98,"MercadoPago","Yerba Buena 540, Tucumán"},
             {"P024","1","599.99"},{"P020","1","299.99"}},
            // U020 Celeste - tech
            {{"O033","U020","2024-11-05","ENVIADO",    1099.99,"Tarjeta de crédito","Caballito 90, CABA"},
             {"P004","1","1099.99"}},
            // U001 Carlos - también compró un libro
            {{"O034","U001","2024-12-01","COMPLETADO",   49.99,"MercadoPago","Av. Corrientes 1500, CABA"},
             {"P026","1","49.99"}},
            // U005 Pedro - también compró pesas
            {{"O035","U005","2024-11-20","COMPLETADO",  299.99,"Débito","Italia 560, La Plata"},
             {"P020","1","299.99"}},
        };

        try (Session s = config.getSession()) {
            for (Object[][] pedido : pedidos) {
                Object[] header = pedido[0];
                s.run(createOrder, Map.of(
                    "userId", header[1], "id", header[0], "fecha", header[2],
                    "estado", header[3], "total", header[4], "pago", header[5], "dir", header[6]
                ));
                for (int i = 1; i < pedido.length; i++) {
                    Object[] item = pedido[i];
                    s.run(addItem, Map.of(
                        "oId", header[0], "pId", item[0],
                        "cant", Integer.parseInt(item[1].toString()),
                        "precio", Double.parseDouble(item[2].toString())
                    ));
                }
            }
        }
        System.out.println("OK (35 pedidos, ~80 ítems)");
    }

    // ─── 7. Relaciones VISTO ──────────────────────────────────────────────────
    private void crearVistas() {
        System.out.print("    Creando vistas de productos... ");
        String q = """
                MATCH (u:User {id:$uId})
                MATCH (p:Product {id:$pId})
                MERGE (u)-[v:VISTO {timestamp:$ts}]->(p)
                SET v.duracionSegundos = $dur
                """;
        // {userId, productId, timestamp, duracion}
        Object[][] views = {
            // U001 miró productos de electrónica que no compró
            {"U001","P002","2024-03-10T14:32:00",  145},
            {"U001","P004","2024-03-12T10:15:00",  230},
            {"U001","P006","2024-07-18T16:00:00",   90},
            // U002 miró productos
            {"U002","P003","2024-02-08T11:20:00",  310},
            {"U002","P006","2024-08-03T20:45:00",  175},
            // U003 buscó productos
            {"U003","P001","2024-01-20T09:10:00",  260},
            {"U003","P004","2024-01-21T09:30:00",  180},
            {"U003","P002","2024-06-15T17:50:00",   95},
            // U004 miró deportes también
            {"U004","P019","2024-04-10T15:00:00",  420},
            {"U004","P022","2024-09-02T12:30:00",  190},
            // U005 miró raqueta
            {"U005","P021","2024-03-25T18:40:00",  340},
            {"U005","P024","2024-11-18T10:05:00",  280},
            // U006 miró mochila y zapatillas
            {"U006","P007","2024-05-18T13:10:00",  215},
            {"U006","P012","2024-11-08T20:00:00",  160},
            // U007 miró otros productos de hogar
            {"U007","P014","2024-02-25T10:00:00",  125},
            {"U007","P015","2024-07-12T16:30:00",  200},
            // U008 miró sillón y mesa
            {"U008","P013","2024-04-03T11:45:00",  380},
            {"U008","P018","2024-08-20T14:20:00",  145},
            // U009 miró aspiradora
            {"U009","P016","2024-03-08T09:00:00",  500},
            {"U009","P017","2024-09-12T21:10:00",  230},
            // U010 miró más libros
            {"U010","P029","2024-01-12T20:00:00",  175},
            {"U010","P030","2024-06-28T22:30:00",  290},
            // U011 miró libros que no compró
            {"U011","P025","2024-02-18T19:15:00",  210},
            {"U011","P029","2024-08-05T21:30:00",  155},
            // U013 miró varios
            {"U013","P002","2024-04-15T10:00:00",  320},
            {"U013","P005","2024-09-22T15:00:00",  240},
        };
        try (Session s = config.getSession()) {
            for (Object[] v : views) {
                s.run(q, Map.of("uId",v[0],"pId",v[1],"ts",v[2],"dur",v[3]));
            }
        }
        System.out.println("OK (26 vistas)");
    }

    // ─── 8. Relaciones RESENO ─────────────────────────────────────────────────
    private void crearResenas() {
        System.out.print("    Creando reseñas... ");
        String q = """
                MATCH (u:User {id:$uId})
                MATCH (p:Product {id:$pId})
                MERGE (u)-[r:RESENO]->(p)
                SET r.calificacion=$calif, r.comentario=$com, r.fecha=$fecha
                """;
        // {userId, productId, calificacion, comentario, fecha}
        Object[][] resenas = {
            {"U001","P001", 5,"Excelente rendimiento, cámara increíble. Vale cada peso.","2024-03-25"},
            {"U001","P005", 4,"La cancelación de ruido es impresionante. Sonido premium.","2024-07-30"},
            {"U002","P001", 4,"Muy buen teléfono, aunque algo caro. Pantalla hermosa.","2024-02-20"},
            {"U002","P004", 5,"Perfecta para trabajo y diseño. El M2 es una bestia.","2024-02-22"},
            {"U003","P003", 5,"La mejor laptop que usé en mi vida. Rápida y silenciosa.","2024-02-05"},
            {"U003","P005", 5,"Uso diario en la oficina, jamás molesta el ruido externo.","2024-01-30"},
            {"U004","P007", 4,"Comodísimas para caminar todo el día. Buen amortiguamiento.","2024-04-20"},
            {"U004","P010", 3,"Abriga bien pero el cierre es frágil. Esperaba más calidad.","2024-09-10"},
            {"U005","P007", 5,"Las mejores zapatillas que compré, súper resistentes.","2024-04-05"},
            {"U005","P019", 4,"Excelente bici para trail. Cambios suaves y frenos potentes.","2024-04-10"},
            {"U006","P009", 3,"Talla bien pero el índigo destiñe al principio. Normal.","2024-11-18"},
            {"U007","P013", 5,"Super cómodo para ver series. La entrega fue rapidísima.","2024-03-08"},
            {"U007","P016", 4,"Limpia bien aunque le cuesta con alfombras gruesas.","2024-07-22"},
            {"U008","P015", 4,"Hace un café espectacular. Fácil de usar y limpiar.","2024-04-15"},
            {"U009","P015", 5,"Uso Nespresso desde hace años, esta es la mejor cafetera.","2024-03-18"},
            {"U009","P013", 4,"El sillón es enorme y muy cómodo. El reclinado es genial.","2024-03-20"},
            {"U010","P025", 5,"Tolkien es un genio. La edición especial es preciosa.","2024-01-25"},
            {"U010","P026", 5,"Cambió mi forma de programar. Lectura obligada para devs.","2024-01-28"},
            {"U010","P027", 4,"Un clásico que todo el mundo debería leer al menos una vez.","2024-01-30"},
            {"U011","P026", 4,"Muy buen libro técnico, aunque algunos conceptos son viejos.","2024-02-28"},
            {"U011","P030", 3,"Buena introducción, pero necesita más ejemplos prácticos.","2024-08-15"},
            {"U012","P025", 4,"Edición hermosa. El papel es de muy buena calidad.","2024-03-12"},
            {"U013","P001", 3,"Buen teléfono pero el precio en Argentina es abusivo.","2024-04-25"},
            {"U015","P020", 5,"Las pesas ajustables son perfectas para home gym.","2024-07-01"},
            {"U019","P024", 4,"Cinta sólida para uso doméstico. El envío fue rápido.","2024-10-20"},
        };
        try (Session s = config.getSession()) {
            for (Object[] r : resenas) {
                s.run(q, Map.of("uId",r[0],"pId",r[1],"calif",r[2],"com",r[3],"fecha",r[4]));
            }
        }
        System.out.println("OK (25 reseñas)");
    }
}
