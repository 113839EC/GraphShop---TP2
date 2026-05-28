package com.ecommerce.ui;

import com.ecommerce.config.Neo4jConfig;
import com.ecommerce.report.ReportService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Dashboard gráfico de reportes usando Swing + Graphics2D.
 * Sin dependencias externas — solo JDK 17.
 *
 * Pestañas:
 *   1. Ingresos por Categoría  — barra horizontal + torta
 *   2. Top Clientes            — barra vertical + tabla
 *   3. Calificaciones          — barra horizontal con colores por score
 *   4. Estadísticas Generales  — KPI cards + diagrama del grafo Neo4j
 */
public class DashboardWindow extends JFrame {

    // ─── Paleta dark-theme ────────────────────────────────────────────────────
    static final Color BG_DARK   = new Color(18,  18,  27);
    static final Color BG_PANEL  = new Color(30,  30,  46);
    static final Color BG_CARD   = new Color(40,  40,  60);
    static final Color TXT_LIGHT = new Color(240, 240, 255);
    static final Color TXT_MUTED = new Color(140, 140, 170);
    static final Color ACCENT    = new Color(99,  179, 237);

    static final Color[] PALETTE = {
        new Color(99,  179, 237),   // sky blue
        new Color(72,  187, 120),   // green
        new Color(237, 137,  54),   // orange
        new Color(246, 173,  85),   // amber
        new Color(183, 148, 246),   // violet
        new Color(252, 129, 129),   // rose
        new Color(129, 236, 236),   // cyan
        new Color(250, 177, 160),   // salmon
    };

    private final ReportService svc = new ReportService();

    // ─── Constructor ─────────────────────────────────────────────────────────
    public DashboardWindow() {
        setTitle("GraphShop — Dashboard Analytics");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                Neo4jConfig.getInstance().close();
            }
        });
        setSize(1250, 780);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setIconImage(buildIcon());
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        setContentPane(root);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildTabs(),    BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
    }

    // ─── Header ──────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setPaint(new GradientPaint(0, 0, new Color(10, 10, 22), getWidth(), 0, new Color(24, 18, 44)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), 0, PALETTE[4]));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        p.setOpaque(true);
        p.setBorder(new EmptyBorder(14, 28, 14, 28));

        JLabel title = label("GRAPHSHOP  ·  Dashboard Analytics",
                new Font("Segoe UI", Font.BOLD, 22), ACCENT);
        JLabel sub   = label("Base de Datos de Grafos — Neo4j 5.x  |  Bases de Datos II TP2",
                new Font("Segoe UI", Font.PLAIN, 12), TXT_MUTED);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);

        JLabel badge = label("● Neo4j conectado", new Font("Segoe UI", Font.BOLD, 12),
                new Color(72, 187, 120));
        badge.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(left,  BorderLayout.WEST);
        p.add(badge, BorderLayout.EAST);
        return p;
    }

    // ─── Tabs ────────────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane t = new JTabbedPane(JTabbedPane.TOP);
        t.setBackground(BG_DARK);
        t.setForeground(TXT_LIGHT);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setBorder(new EmptyBorder(6, 6, 0, 6));
        t.addTab("  Ingresos por Categoría  ", buildRevenueTab());
        t.addTab("  Top Clientes  ",           buildCustomersTab());
        t.addTab("  Calificaciones  ",         buildRatingsTab());
        t.addTab("  Actividad  ",              buildActivityTab());
        t.addTab("  Métodos de Pago  ",        buildPaymentsTab());
        t.addTab("  Estadísticas  ",           buildStatsTab());
        return t;
    }

    // ─── Footer ──────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setPaint(new GradientPaint(0, 0, new Color(10, 10, 22), getWidth(), 0, new Color(24, 18, 44)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        p.setOpaque(true);
        p.setBorder(new EmptyBorder(5, 10, 5, 10));

        JButton refresh = styledBtn("↺  Actualizar", ACCENT);
        refresh.addActionListener(e -> {
            Container c = getContentPane();
            c.removeAll();
            buildUI();
            c.revalidate();
            c.repaint();
        });

        JButton close = styledBtn("✕  Cerrar", new Color(180, 50, 50));
        close.addActionListener(e -> dispose());

        p.add(refresh);
        p.add(close);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   TAB 1 — Ingresos por Categoría
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildRevenueTab() {
        List<Map<String, Object>> data = svc.getRevenueByCategory();

        JPanel outer = darkPanel(new BorderLayout(16, 16));
        outer.setBorder(new EmptyBorder(18, 22, 18, 22));

        if (data.isEmpty()) return emptyPanel("Sin datos — cargue los datos de muestra primero (opción 1 del menú).");

        String[] cats   = data.stream().map(m -> m.get("categoria").toString()).toArray(String[]::new);
        double[] ingresos = data.stream().mapToDouble(m -> (Double) m.get("ingresos")).toArray();

        // Charts row
        JPanel charts = new JPanel(new GridLayout(1, 2, 18, 0));
        charts.setOpaque(false);
        charts.add(titled(new HBarChart(cats, ingresos, PALETTE),
                "Ingresos totales por categoría  (solo pedidos COMPLETADOS)"));
        charts.add(titled(new PieChart(cats, ingresos, PALETTE),
                "Distribución porcentual de ingresos"));
        outer.add(charts, BorderLayout.CENTER);

        // KPI row below
        JPanel kpiRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 6));
        kpiRow.setOpaque(false);
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> r = data.get(i);
            long ped = (Long) r.get("totalPedidos");
            kpiRow.add(kpiCard(
                    r.get("categoria").toString(),
                    String.format("$%,.0f", (Double) r.get("ingresos")),
                    ped + " pedido" + (ped != 1 ? "s" : ""),
                    PALETTE[i % PALETTE.length]
            ));
        }
        outer.add(kpiRow, BorderLayout.SOUTH);
        return outer;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   TAB 2 — Top Clientes
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildCustomersTab() {
        List<Map<String, Object>> data = svc.getTopCustomers(10);

        JPanel outer = darkPanel(new BorderLayout(14, 14));
        outer.setBorder(new EmptyBorder(18, 22, 18, 22));

        if (data.isEmpty()) return emptyPanel("Sin datos.");

        String[] nombres = data.stream().map(m -> firstName(m.get("nombre").toString())).toArray(String[]::new);
        double[] gastos  = data.stream().mapToDouble(m -> (Double) m.get("totalGastado")).toArray();

        outer.add(titled(new VBarChart(nombres, gastos, PALETTE),
                "Top 10 Clientes por Gasto Total — pedidos COMPLETADOS"), BorderLayout.CENTER);

        // Table
        String[]   cols = {"#", "Cliente", "Ciudad", "Pedidos", "Total Gastado"};
        Object[][] rows = new Object[data.size()][];
        String[]   medals = {"🥇","🥈","🥉"};
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> m = data.get(i);
            rows[i] = new Object[]{
                i < 3 ? medals[i] : String.valueOf(i + 1),
                m.get("nombre"),
                m.get("ciudad"),
                m.get("cantidadPedidos"),
                String.format("$%,.2f", (Double) m.get("totalGastado"))
            };
        }
        JTable table = styledTable(cols, rows);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 90)));
        scroll.setPreferredSize(new Dimension(0, 190));
        outer.add(scroll, BorderLayout.SOUTH);
        return outer;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   TAB 3 — Calificaciones
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildRatingsTab() {
        List<Map<String, Object>> data = svc.getProductRatings();
        // limit to 15 products to keep chart readable
        if (data.size() > 15) data = data.subList(0, 15);

        JPanel outer = darkPanel(new BorderLayout(12, 12));
        outer.setBorder(new EmptyBorder(18, 22, 18, 22));

        if (data.isEmpty()) return emptyPanel("Sin datos.");

        String[] nombres = data.stream().map(m -> m.get("nombre").toString()).toArray(String[]::new);
        double[] proms   = data.stream().mapToDouble(m -> (Double) m.get("promedio")).toArray();
        long[]   counts  = data.stream().mapToLong(m -> (Long) m.get("cantidadResenas")).toArray();

        Color[] colors = new Color[proms.length];
        for (int i = 0; i < proms.length; i++) {
            colors[i] = proms[i] >= 4.0 ? new Color(72, 187, 120)
                      : proms[i] >= 3.0 ? new Color(246, 173, 85)
                      :                   new Color(252, 129, 129);
        }

        outer.add(titled(new RatingChart(nombres, proms, counts, colors),
                "Calificación promedio por producto  (escala 1 – 5 ★)"), BorderLayout.CENTER);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 4));
        legend.setOpaque(false);
        legend.add(legendDot("≥ 4.0 — Excelente", new Color(72,  187, 120)));
        legend.add(legendDot("≥ 3.0 — Bueno",     new Color(246, 173,  85)));
        legend.add(legendDot("< 3.0 — Regular",    new Color(252, 129, 129)));
        outer.add(legend, BorderLayout.SOUTH);
        return outer;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   TAB 4 — Actividad: pedidos por estado + productos más vistos
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildActivityTab() {
        List<Map<String, Object>> statusData  = svc.getOrdersByStatus();
        List<Map<String, Object>> viewedData  = svc.getMostViewedProducts(12);

        JPanel outer = darkPanel(new BorderLayout(18, 18));
        outer.setBorder(new EmptyBorder(18, 22, 18, 22));

        if (statusData.isEmpty() && viewedData.isEmpty())
            return emptyPanel("Sin datos — cargue los datos de muestra primero.");

        // Top row: pie de estados + hbar de más vistos
        JPanel top = new JPanel(new GridLayout(1, 2, 18, 0));
        top.setOpaque(false);

        // Pie: pedidos por estado
        if (!statusData.isEmpty()) {
            String[] estados  = statusData.stream().map(m -> m.get("estado").toString()).toArray(String[]::new);
            double[] cantidades = statusData.stream().mapToDouble(m -> ((Long) m.get("cantidad")).doubleValue()).toArray();
            Color[] statusColors = { new Color(72,187,120), new Color(99,179,237),
                                     new Color(246,173,85), new Color(252,129,129) };
            top.add(titled(new PieChart(estados, cantidades, statusColors),
                    "Distribución de pedidos por estado"));
        }

        // HBar: productos más vistos
        if (!viewedData.isEmpty()) {
            String[] nombres = viewedData.stream().map(m -> m.get("nombre").toString()).toArray(String[]::new);
            double[] vistas  = viewedData.stream().mapToDouble(m -> ((Long) m.get("totalVistas")).doubleValue()).toArray();
            top.add(titled(new HBarChart(nombres, vistas, PALETTE, false),
                    "Productos más vistos  (relación VISTO)"));
        }
        outer.add(top, BorderLayout.CENTER);

        // Bottom: tabla de productos vistos
        if (!viewedData.isEmpty()) {
            String[]   cols = {"Producto", "Marca", "Categoría", "Visitas", "Prom. segundos"};
            Object[][] rows = new Object[viewedData.size()][];
            for (int i = 0; i < viewedData.size(); i++) {
                Map<String, Object> m = viewedData.get(i);
                rows[i] = new Object[]{
                    m.get("nombre"), m.get("marca"), m.get("categoria"),
                    m.get("totalVistas"),
                    String.format("%.0f s", (Double) m.get("avgSegundos"))
                };
            }
            JTable table = styledTable(cols, rows);
            // Centrar columnas Visitas (3) y Prom. segundos (4)
            javax.swing.table.DefaultTableCellRenderer centerRender =
                new javax.swing.table.DefaultTableCellRenderer() {
                    { setHorizontalAlignment(JLabel.CENTER); }
                    @Override public java.awt.Component getTableCellRendererComponent(
                            JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                        super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                        setBackground(row % 2 == 0 ? BG_CARD : new Color(35, 35, 55));
                        setForeground(TXT_LIGHT);
                        return this;
                    }
                };
            table.getColumnModel().getColumn(3).setCellRenderer(centerRender);
            table.getColumnModel().getColumn(4).setCellRenderer(centerRender);
            javax.swing.table.DefaultTableCellRenderer centerHeader =
                new javax.swing.table.DefaultTableCellRenderer();
            centerHeader.setHorizontalAlignment(JLabel.CENTER);
            centerHeader.setBackground(new Color(22, 22, 42));
            centerHeader.setForeground(ACCENT);
            centerHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
            table.getColumnModel().getColumn(3).setHeaderRenderer(centerHeader);
            table.getColumnModel().getColumn(4).setHeaderRenderer(centerHeader);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBackground(BG_DARK);
            scroll.getViewport().setBackground(BG_CARD);
            scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 90)));
            scroll.setPreferredSize(new Dimension(0, 175));
            outer.add(scroll, BorderLayout.SOUTH);
        }
        return outer;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   TAB 5 — Métodos de Pago
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildPaymentsTab() {
        List<Map<String, Object>> data = svc.getSalesByPaymentMethod();

        JPanel outer = darkPanel(new BorderLayout(18, 18));
        outer.setBorder(new EmptyBorder(18, 22, 18, 22));

        if (data.isEmpty()) return emptyPanel("Sin datos — cargue los datos de muestra primero.");

        String[] metodos  = data.stream().map(m -> m.get("metodoPago").toString()).toArray(String[]::new);
        double[] ingresos = data.stream().mapToDouble(m -> (Double) m.get("ingresos")).toArray();

        Color[] payColors = {
            new Color(183,148,246), new Color(99,179,237),
            new Color(72,187,120),  new Color(246,173,85),
            new Color(252,129,129)
        };

        // Charts row
        JPanel charts = new JPanel(new GridLayout(1, 2, 18, 0));
        charts.setOpaque(false);
        charts.add(titled(new HBarChart(metodos, ingresos, payColors),
                "Ingresos por método de pago  (pedidos COMPLETADOS)"));
        charts.add(titled(new PieChart(metodos, ingresos, payColors),
                "Participación porcentual"));
        outer.add(charts, BorderLayout.CENTER);

        // KPI cards + table
        JPanel bottom = new JPanel(new BorderLayout(14, 0));
        bottom.setOpaque(false);

        JPanel kpiRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        kpiRow.setOpaque(false);
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> m = data.get(i);
            long ped = (Long) m.get("cantidadPedidos");
            kpiRow.add(kpiCard(
                m.get("metodoPago").toString(),
                String.format("$%,.0f", (Double) m.get("ingresos")),
                ped + " pedido" + (ped != 1 ? "s" : ""),
                payColors[i % payColors.length]
            ));
        }

        String[]   cols = {"Método de Pago", "Ingresos", "Pedidos", "Ticket Promedio"};
        Object[][] rows = new Object[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            Map<String, Object> m = data.get(i);
            double ing = (Double) m.get("ingresos");
            long   ped = (Long)   m.get("cantidadPedidos");
            rows[i] = new Object[]{
                m.get("metodoPago"),
                String.format("$%,.2f", ing),
                ped,
                String.format("$%,.2f", ped > 0 ? ing / ped : 0)
            };
        }
        JTable table = styledTable(cols, rows);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 90)));

        bottom.add(kpiRow, BorderLayout.NORTH);
        bottom.add(scroll, BorderLayout.CENTER);
        bottom.setPreferredSize(new Dimension(0, 175));
        outer.add(bottom, BorderLayout.SOUTH);
        return outer;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   TAB 6 — Estadísticas Generales
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildStatsTab() {
        Map<String, Long> stats = svc.getGeneralStats();
        List<Map<String, Object>> rev  = svc.getRevenueByCategory();
        List<Map<String, Object>> top1 = svc.getTopCustomers(1);

        double totalRev  = rev.stream().mapToDouble(m -> (Double) m.get("ingresos")).sum();
        String topNombre = top1.isEmpty() ? "—" : top1.get(0).get("nombre").toString();
        double topGasto  = top1.isEmpty() ? 0   : (Double) top1.get(0).get("totalGastado");

        JPanel outer = darkPanel(new BorderLayout(16, 20));
        outer.setBorder(new EmptyBorder(28, 36, 28, 36));

        // Row 1: 4 KPIs
        JPanel r1 = new JPanel(new GridLayout(1, 4, 18, 0));
        r1.setOpaque(false);
        r1.add(bigKpi("Usuarios",    stats.getOrDefault("usuarios",           0L).toString(), "registrados",    PALETTE[0]));
        r1.add(bigKpi("Productos",   stats.getOrDefault("productos",          0L).toString(), "en catálogo",    PALETTE[1]));
        r1.add(bigKpi("Pedidos",     stats.getOrDefault("pedidos",            0L).toString(), "realizados",     PALETTE[2]));
        r1.add(bigKpi("Completados", stats.getOrDefault("pedidosCompletados", 0L).toString(), "✓ entregados",   PALETTE[3]));

        // Row 2: 3 KPIs
        JPanel r2 = new JPanel(new GridLayout(1, 3, 18, 0));
        r2.setOpaque(false);
        r2.add(bigKpi("Ingresos Totales", String.format("$%,.0f", totalRev), "ARS en ventas",   PALETTE[4]));
        r2.add(bigKpi("Top Cliente",      firstName(topNombre),               "mayor gasto",     PALETTE[5]));
        r2.add(bigKpi("Mayor Gasto",      String.format("$%,.0f", topGasto), "un solo cliente", PALETTE[6]));

        JPanel kpis = new JPanel(new GridLayout(2, 1, 0, 14));
        kpis.setOpaque(false);
        kpis.setPreferredSize(new Dimension(0, 180));
        kpis.add(r1);
        kpis.add(r2);

        SchemaDiagram diagram = new SchemaDiagram();
        diagram.setPreferredSize(new Dimension(0, 340));
        JScrollPane diagramScroll = new JScrollPane(titled(diagram,
                "Modelo de Grafo Neo4j — Nodos y Relaciones"));
        diagramScroll.setOpaque(false);
        diagramScroll.getViewport().setOpaque(false);
        diagramScroll.getViewport().setBackground(BG_DARK);
        diagramScroll.setBorder(null);
        diagramScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        outer.add(kpis,          BorderLayout.NORTH);
        outer.add(diagramScroll, BorderLayout.CENTER);
        return outer;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   CHART PANELS (static inner classes — acceden a constantes estáticas)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Barra horizontal genérica */
    static class HBarChart extends JPanel {
        private final String[] labels;
        private final double[] values;
        private final Color[]  colors;
        private final boolean  isCurrency;

        HBarChart(String[] labels, double[] values, Color[] colors) {
            this(labels, values, colors, true);
        }

        HBarChart(String[] labels, double[] values, Color[] colors, boolean isCurrency) {
            this.labels = labels; this.values = values; this.colors = colors;
            this.isCurrency = isCurrency;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values.length == 0) return;
            Graphics2D g2 = aa(g);

            int w = getWidth(), h = getHeight();
            int pL = 150, pR = 90, pT = 16, pB = 24;
            int cW = w - pL - pR, cH = h - pT - pB;
            double max = Arrays.stream(values).max().orElse(1);

            int barH = Math.max(12, Math.min(38, cH / labels.length - 10));
            int gap  = (cH - labels.length * barH) / (labels.length + 1);

            for (int i = 0; i < labels.length; i++) {
                int y  = pT + gap + i * (barH + gap);
                int bw = (int) (values[i] / max * cW);
                Color c = colors[i % colors.length];

                // background track
                g2.setColor(new Color(50, 50, 75));
                g2.fillRoundRect(pL, y, cW, barH, 8, 8);

                // filled bar
                g2.setPaint(new GradientPaint(pL, y, c.brighter(), pL + bw, y, c));
                g2.fillRoundRect(pL, y, Math.max(bw, 6), barH, 8, 8);

                // label (left)
                g2.setColor(TXT_LIGHT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics fm = g2.getFontMetrics();
                String lbl = trunc(labels[i], 20);
                g2.drawString(lbl, pL - fm.stringWidth(lbl) - 8, y + barH / 2 + fm.getAscent() / 2 - 1);

                // value (right)
                g2.setColor(c.brighter());
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String valStr = isCurrency ? fmtVal(values[i]) : String.format("%.0f", values[i]);
                g2.drawString(valStr, pL + bw + 6, y + barH / 2 + 4);
            }

            // axis
            g2.setColor(new Color(60, 60, 90));
            g2.drawLine(pL, pT, pL, h - pB);
            g2.dispose();
        }
    }

    /** Barra vertical */
    static class VBarChart extends JPanel {
        private final String[] labels;
        private final double[] values;
        private final Color[]  colors;

        VBarChart(String[] labels, double[] values, Color[] colors) {
            this.labels = labels; this.values = values; this.colors = colors;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values.length == 0) return;
            Graphics2D g2 = aa(g);

            int w = getWidth(), h = getHeight();
            int pL = 70, pR = 16, pT = 30, pB = 120;
            int cW = w - pL - pR, cH = h - pT - pB;
            double max = Arrays.stream(values).max().orElse(1);

            int n    = labels.length;
            int barW = Math.max(18, cW / n - 8);
            int gap  = (cW - n * barW) / (n + 1);

            // grid lines
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i <= 4; i++) {
                int gy = pT + (int)(cH - i * cH / 4.0);
                g2.setColor(new Color(45, 45, 65));
                g2.drawLine(pL, gy, pL + cW, gy);
                g2.setColor(TXT_MUTED);
                String v = "$" + (int)(i * max / 4 / 1000) + "k";
                g2.drawString(v, 4, gy + 4);
            }

            for (int i = 0; i < n; i++) {
                int x  = pL + gap + i * (barW + gap);
                int bh = (int)(values[i] / max * cH);
                int y  = pT + cH - bh;
                Color c = colors[i % colors.length];

                g2.setPaint(new GradientPaint(x, y, c.brighter(), x, y + bh, c.darker()));
                g2.fillRoundRect(x, y, barW, bh, 8, 8);

                // value on top
                g2.setColor(TXT_LIGHT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String sv = "$" + (int)(values[i]/1000) + "k";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(sv, x + barW/2 - fm.stringWidth(sv)/2, y - 5);

                // label (rotated 45°)
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                AffineTransform orig = g2.getTransform();
                g2.translate(x + barW / 2 + 4, pT + cH + 12);
                g2.rotate(Math.PI / 5.5);
                g2.setColor(TXT_MUTED);
                g2.drawString(trunc(labels[i], 16), 0, 0);
                g2.setTransform(orig);
            }

            // axes
            g2.setColor(new Color(60, 60, 90));
            g2.drawLine(pL, pT, pL, pT + cH);
            g2.drawLine(pL, pT + cH, pL + cW, pT + cH);
            g2.dispose();
        }
    }

    /** Barra de calificaciones con color según score */
    static class RatingChart extends JPanel {
        private final String[] labels;
        private final double[] values;
        private final long[]   counts;
        private final Color[]  colors;

        RatingChart(String[] labels, double[] values, long[] counts, Color[] colors) {
            this.labels = labels; this.values = values;
            this.counts = counts; this.colors = colors;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values.length == 0) return;
            Graphics2D g2 = aa(g);

            int w = getWidth(), h = getHeight();
            int pL = 215, pR = 160, pT = 18, pB = 22;
            int cW = w - pL - pR, cH = h - pT - pB;

            int barH = Math.max(12, Math.min(32, cH / labels.length - 8));
            int gap  = (cH - labels.length * barH) / (labels.length + 1);

            for (int i = 0; i < labels.length; i++) {
                int y  = pT + gap + i * (barH + gap);
                int bw = (int)(values[i] / 5.0 * cW);
                Color c = colors[i];

                // track
                g2.setColor(new Color(50, 50, 75));
                g2.fillRoundRect(pL, y, cW, barH, 8, 8);

                // fill
                g2.setPaint(new GradientPaint(pL, y, c.brighter(), pL + bw, y, c));
                g2.fillRoundRect(pL, y, Math.max(bw, 6), barH, 8, 8);

                // name
                g2.setColor(TXT_LIGHT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                FontMetrics fm = g2.getFontMetrics();
                String lbl = trunc(labels[i], 28);
                g2.drawString(lbl, pL - fm.stringWidth(lbl) - 8, y + barH/2 + fm.getAscent()/2 - 1);

                // score + stars
                g2.setColor(c.brighter());
                int mid = y + barH / 2 + 5;
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String numStr = String.format("%.1f ", values[i]);
                g2.drawString(numStr, pL + bw + 8, mid);
                int numW = g2.getFontMetrics().stringWidth(numStr);
                g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 12));
                g2.drawString("★", pL + bw + 8 + numW, mid);
                int starW = g2.getFontMetrics().stringWidth("★");
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.setColor(TXT_MUTED);
                g2.drawString(String.format("  (%d reseñas)", counts[i]), pL + bw + 8 + numW + starW, mid);
            }

            // axis marks 1-5
            g2.setColor(TXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 1; i <= 5; i++) {
                int x = pL + (int)(i / 5.0 * cW);
                g2.drawString(String.valueOf(i), x - 3, h - pB + 14);
            }
            g2.setColor(new Color(60, 60, 90));
            g2.drawLine(pL, pT, pL, h - pB);
            g2.dispose();
        }
    }

    /** Gráfico de torta */
    static class PieChart extends JPanel {
        private final String[] labels;
        private final double[] values;
        private final Color[]  colors;

        PieChart(String[] labels, double[] values, Color[] colors) {
            this.labels = labels; this.values = values; this.colors = colors;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values.length == 0) return;
            Graphics2D g2 = aa(g);

            int w = getWidth(), h = getHeight();
            int legH = 40;
            int d    = Math.min(w - 40, h - legH - 40);
            int x    = (w - d) / 2, y = 20;

            double total = Arrays.stream(values).sum();
            double start = -90;

            for (int i = 0; i < values.length; i++) {
                double arc = values[i] / total * 360;
                Color c = colors[i % colors.length];

                // shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fill(new Arc2D.Double(x + 3, y + 3, d, d, start, -arc, Arc2D.PIE));

                // slice
                g2.setColor(c);
                g2.fill(new Arc2D.Double(x, y, d, d, start, -arc, Arc2D.PIE));

                // border
                g2.setColor(BG_DARK);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new Arc2D.Double(x, y, d, d, start, -arc, Arc2D.PIE));

                // percentage label inside slice (only if big enough)
                if (arc > 15) {
                    double mid = Math.toRadians(-(start + arc / 2));
                    int lx = (int)(x + d/2.0 + d/3.2 * Math.cos(mid));
                    int ly = (int)(y + d/2.0 + d/3.2 * Math.sin(mid));
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String pct = String.format("%.0f%%", values[i] / total * 100);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(pct, lx - fm.stringWidth(pct)/2, ly + 4);
                }
                start -= arc;
            }

            // legend row at bottom
            int lx = 10, ly = y + d + 14;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 0; i < labels.length; i++) {
                Color c = colors[i % colors.length];
                g2.setColor(c);
                g2.fillRoundRect(lx, ly, 11, 11, 3, 3);
                g2.setColor(TXT_LIGHT);
                String lbl = trunc(labels[i], 13);
                g2.drawString(lbl, lx + 14, ly + 10);
                lx += g2.getFontMetrics().stringWidth(lbl) + 28;
                if (lx > w - 80) { lx = 10; ly += 16; }
            }
            g2.dispose();
        }
    }

    /** Diagrama del esquema Neo4j con nodos y relaciones */
    static class SchemaDiagram extends JPanel {
        // Nodos: User, Order, Product, Category
        private static final String[] NODE_NAMES  = {"User", "Order", "Product", "Category"};
        private static final Color[]  NODE_COLORS = {
            new Color(99,  179, 237),   // blue
            new Color(183, 148, 246),   // violet
            new Color(237, 137,  54),   // orange
            new Color(72,  187, 120),   // green
        };
        // Propiedades de cada nodo
        private static final String[][] NODE_PROPS = {
            {"id, nombre, email", "edad, ciudad", "puntosFidelidad"},
            {"id, fecha, estado", "montoTotal", "metodoPago"},
            {"id, nombre, marca", "precio, stock", "descripcion"},
            {"id, nombre", "descripcion"},
        };
        // Relaciones: {desde, hacia, label, props}
        private static final Object[][] RELS = {
            {0, 1, "REALIZO",     ""},
            {1, 2, "CONTIENE",    "cantidad, precioUnitario"},
            {2, 3, "PERTENECE_A", ""},
            {0, 2, "VISTO",       "timestamp, duracionSeg"},
            {0, 2, "RESENO",      "calificacion, comentario"},
        };

        SchemaDiagram() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = aa(g);
            int w = getWidth(), h = getHeight();
            int cx = w / 2, cy = h / 2;

            // Posiciones de los nodos (relativos al centro)
            int[][] pos = {
                {cx - 30,  cy - 90},   // User  (arriba-centro)
                {cx + 220, cy + 10},   // Order (derecha)
                {cx - 30,  cy + 100},  // Product (abajo-centro)
                {cx - 280, cy + 10},   // Category (izquierda)
            };

            // Relaciones
            Color[] relColors = {PALETTE[4], PALETTE[0], PALETTE[1], PALETTE[2], PALETTE[5]};
            double[] curves   = {0.0, 0.0, 0.0, -60.0, 60.0}; // curvatura de bezier

            for (int i = 0; i < RELS.length; i++) {
                int from = (int) RELS[i][0], to = (int) RELS[i][1];
                String lbl  = (String) RELS[i][2];
                String prop = (String) RELS[i][3];
                drawRelation(g2, pos[from], pos[to], lbl, prop, relColors[i], curves[i]);
            }

            // Nodos encima de las relaciones
            int r = 44;
            for (int i = 0; i < 4; i++) {
                drawNode(g2, pos[i][0], pos[i][1], r, NODE_NAMES[i], NODE_PROPS[i], NODE_COLORS[i]);
            }

            g2.dispose();
        }

        private void drawNode(Graphics2D g2, int cx, int cy, int r,
                              String name, String[] props, Color color) {
            // shadow
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(cx - r + 4, cy - r + 4, r * 2, r * 2);

            // circle
            g2.setColor(color.darker().darker());
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2.setPaint(new RadialGradientPaint(new Point(cx - r/3, cy - r/3),
                    r * 2, new float[]{0f, 1f},
                    new Color[]{color.brighter(), color.darker()}));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);

            // border
            g2.setColor(color.brighter());
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);

            // label name
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(name, cx - fm.stringWidth(name)/2, cy + fm.getAscent()/2 - 2);

            // properties tooltip below node
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            fm = g2.getFontMetrics();
            g2.setColor(TXT_MUTED);
            int py = cy + r + 14;
            for (String p : props) {
                g2.drawString(p, cx - fm.stringWidth(p)/2, py);
                py += 12;
            }
        }

        private void drawRelation(Graphics2D g2, int[] from, int[] to,
                                   String label, String props, Color color, double curve) {
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(color.darker());

            int x1 = from[0], y1 = from[1];
            int x2 = to[0],   y2 = to[1];

            if (Math.abs(curve) < 0.5) {
                g2.drawLine(x1, y1, x2, y2);
                // arrowhead
                drawArrow(g2, x1, y1, x2, y2, color);
                // label at midpoint
                int mx = (x1+x2)/2, my = (y1+y2)/2;
                drawRelLabel(g2, mx, my, label, props, color);
            } else {
                // Quadratic bezier
                int mx = (x1+x2)/2, my = (y1+y2)/2;
                // perpendicular offset
                double dx = x2 - x1, dy = y2 - y1;
                double len = Math.sqrt(dx*dx + dy*dy);
                int cx = (int)(mx - curve * dy / len);
                int cy = (int)(my + curve * dx / len);

                QuadCurve2D qc = new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2);
                g2.draw(qc);
                drawRelLabel(g2, cx, cy, label, props, color);
            }
        }

        private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2, Color color) {
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int ax = (int)(x2 - 14 * Math.cos(angle));
            int ay = (int)(y2 - 14 * Math.sin(angle));
            int[] xs = {x2, (int)(ax + 6*Math.cos(angle + Math.PI/2)),
                            (int)(ax + 6*Math.cos(angle - Math.PI/2))};
            int[] ys = {y2, (int)(ay + 6*Math.sin(angle + Math.PI/2)),
                            (int)(ay + 6*Math.sin(angle - Math.PI/2))};
            g2.setColor(color);
            g2.fillPolygon(xs, ys, 3);
        }

        private void drawRelLabel(Graphics2D g2, int mx, int my,
                                   String label, String props, Color color) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            String full = ":" + label;
            int lw = fm.stringWidth(full);
            // pill background
            g2.setColor(new Color(18, 18, 27, 210));
            g2.fillRoundRect(mx - lw/2 - 5, my - 13, lw + 10, 15, 6, 6);
            g2.setColor(color.brighter());
            g2.drawString(full, mx - lw/2, my - 2);

            if (!props.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                fm = g2.getFontMetrics();
                g2.setColor(TXT_MUTED);
                g2.drawString("{" + props + "}", mx - fm.stringWidth("{"+props+"}")/2, my + 9);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG_DARK);
        return p;
    }

    private JPanel emptyPanel(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_DARK);
        JLabel l = label(msg, new Font("Segoe UI", Font.ITALIC, 14), TXT_MUTED);
        p.add(l);
        return p;
    }

    private JPanel titled(JComponent chart, String title) {
        JPanel p = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(BG_PANEL);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(new Color(55, 55, 85));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(ACCENT);
                g2.fillRoundRect(18, 0, 48, 4, 3, 3);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18, 18, 14, 18));
        JLabel lbl = label(title, new Font("Segoe UI", Font.BOLD, 13), ACCENT);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        p.add(lbl,   BorderLayout.NORTH);
        p.add(chart, BorderLayout.CENTER);
        return p;
    }

    private JPanel kpiCard(String title, String val, String sub, Color color) {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth() - 1, 5, 12, 12);
                g2.fillRect(0, 3, getWidth() - 1, 2);
                g2.setColor(color.darker());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 14, 8, 14));
        p.setPreferredSize(new Dimension(148, 76));
        p.add(label(title, new Font("Segoe UI", Font.PLAIN,  11), TXT_MUTED));
        p.add(label(val,   new Font("Segoe UI", Font.BOLD,   18), color));
        p.add(label(sub,   new Font("Segoe UI", Font.PLAIN,  10), TXT_MUTED));
        return p;
    }

    private JPanel bigKpi(String title, String val, String sub, Color color) {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                g2.setPaint(new GradientPaint(0, 0,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), 35),
                    0, getHeight(), BG_CARD));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth() - 1, 6, 16, 16);
                g2.fillRect(0, 4, getWidth() - 1, 2);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 160));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 14, 8, 14));
        p.add(label(title, new Font("Segoe UI", Font.PLAIN, 11), TXT_MUTED));
        p.add(label(val,   new Font("Segoe UI", Font.BOLD,  22), color));
        p.add(label(sub,   new Font("Segoe UI", Font.PLAIN, 10), TXT_MUTED));
        return p;
    }

    private JTable styledTable(String[] cols, Object[][] data) {
        Color rowAlt = new Color(35, 35, 55);
        JTable t = new JTable(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public java.awt.Component prepareRenderer(
                    javax.swing.table.TableCellRenderer renderer, int row, int col) {
                java.awt.Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? BG_CARD : rowAlt);
                    c.setForeground(TXT_LIGHT);
                }
                return c;
            }
        };
        t.setBackground(BG_CARD);
        t.setForeground(TXT_LIGHT);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(30);
        t.setGridColor(new Color(45, 45, 68));
        t.setSelectionBackground(new Color(70, 70, 130));
        t.setSelectionForeground(Color.WHITE);
        t.getTableHeader().setBackground(new Color(22, 22, 42));
        t.getTableHeader().setForeground(ACCENT);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(55, 55, 85)));
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new java.awt.Dimension(0, 1));
        return t;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = aa(g);
                Color c = getModel().isPressed()  ? bg.darker().darker() :
                          getModel().isRollover() ? bg.brighter()        : bg;
                g2.setPaint(new GradientPaint(0, 0, c.brighter(), 0, getHeight(), c));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 22, 8, 22));
        return b;
    }

    private JLabel legendDot(String text, Color color) {
        JLabel l = new JLabel("  ●  " + text);
        l.setForeground(color);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private static Image buildIcon() {
        // Simple 16×16 gradient icon
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(16, 16,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setPaint(new GradientPaint(0, 0, PALETTE[0], 16, 16, PALETTE[1]));
        g.fillOval(0, 0, 16, 16);
        g.dispose();
        return img;
    }

    // ─── Static helpers (usados por inner classes) ────────────────────────────
    static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        return g2;
    }

    static String trunc(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    static String fmtVal(double v) {
        if (v >= 1_000_000) return String.format("$%.1fM", v / 1_000_000);
        if (v >= 10_000)    return String.format("$%.0fK", v / 1_000);
        if (v >= 1_000)     return String.format("$%.1fK", v / 1_000);
        return String.format("$%.0f", v);
    }

    private static String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "—";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[0] + " " + parts[1] : parts[0];
    }

    // ─── Punto de entrada ─────────────────────────────────────────────────────
    public static void launch() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception ignored) {}
        UIManager.put("TabbedPane.background",          BG_DARK);
        UIManager.put("TabbedPane.foreground",           TXT_LIGHT);
        UIManager.put("TabbedPane.selected",             new Color(35, 70, 120));
        UIManager.put("TabbedPane.tabAreaBackground",    BG_DARK);
        UIManager.put("TabbedPane.contentAreaColor",     BG_DARK);
        UIManager.put("TabbedPane.light",                new Color(35, 70, 120));
        UIManager.put("TabbedPane.highlight",            new Color(50, 90, 150));
        UIManager.put("TabbedPane.shadow",               BG_DARK);
        UIManager.put("TabbedPane.darkShadow",           new Color(10, 10, 20));
        UIManager.put("TabbedPane.focus",                ACCENT);
        UIManager.put("TabbedPane.unselectedBackground", new Color(24, 24, 38));
        UIManager.put("TabbedPane.selectHighlight",      new Color(50, 90, 150));
        SwingUtilities.invokeLater(() -> {
            DashboardWindow w = new DashboardWindow();
            w.setVisible(true);
        });
    }
}
