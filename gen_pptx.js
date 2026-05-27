const pptxgen = require('C:/Users/emanu/AppData/Roaming/npm/node_modules/pptxgenjs');

const OUT = 'C:/Users/emanu/IdeaProjects/BDII-TP2/presentacion_graphshop.pptx';

// ── Palette ──────────────────────────────────────────────────────────────────
const BG       = '0F172A';   // slide background (dark navy)
const CARD     = '1E293B';   // card / panel background
const BORDER   = '334155';   // subtle border
const BLUE     = '63B3ED';   // primary accent
const GREEN    = '68D391';   // success / highlight
const ORANGE   = 'F6AD55';   // warning / secondary
const PURPLE   = 'A78BFA';   // tertiary accent
const WHITE    = 'F8FAFC';   // primary text
const MUTED    = '94A3B8';   // secondary text
const RED      = 'FC8181';   // danger / con

const pres = new pptxgen();
pres.layout = 'LAYOUT_16x9';
pres.title  = 'GraphShop — E-Commerce sobre Neo4j';
pres.author = 'BDII TP2 — UTN FRC';

// ─── Helpers ──────────────────────────────────────────────────────────────────
function bg(slide) { slide.background = { color: BG }; }

function card(slide, x, y, w, h, opts = {}) {
  slide.addShape(pres.shapes.RECTANGLE, {
    x, y, w, h,
    fill: { color: opts.fill || CARD },
    line: { color: opts.border || BORDER, width: 1 },
    shadow: { type: 'outer', blur: 8, offset: 2, angle: 135, color: '000000', opacity: 0.3 }
  });
}

const MEMBER_NAMES = ['Amarilla', 'Cortez', 'Carreggio'];
function participantBadge(slide, num, label) {
  const colors = [BLUE, GREEN, ORANGE];
  const x = 0.18;
  const y = 0.12;
  slide.addShape(pres.shapes.RECTANGLE, {
    x, y, w: 3.0, h: 0.38,
    fill: { color: colors[num - 1] },
    line: { color: colors[num - 1], width: 0 }
  });
  slide.addText(`${MEMBER_NAMES[num-1]}  ·  ${label}`, {
    x, y, w: 3.0, h: 0.38,
    fontSize: 9, bold: true, color: BG, align: 'center', valign: 'middle', margin: 0
  });
}

function slideTitle(slide, text, sub = '') {
  slide.addText(text, {
    x: 0.5, y: 0.2, w: 9, h: 0.6,
    fontSize: 28, bold: true, color: WHITE, fontFace: 'Calibri', align: 'left', valign: 'middle', margin: 0
  });
  if (sub) {
    slide.addText(sub, {
      x: 0.5, y: 0.78, w: 9, h: 0.28,
      fontSize: 12, color: MUTED, fontFace: 'Calibri', align: 'left', valign: 'middle', margin: 0
    });
  }
  slide.addShape(pres.shapes.LINE, {
    x: 0.5, y: 1.08, w: 9, h: 0,
    line: { color: BORDER, width: 1 }
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 1 — Carátula
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);

  // UTN FRC Logo (white card + image)
  s.addShape(pres.shapes.RECTANGLE, {
    x: 0.25, y: 0.14, w: 2.75, h: 0.58,
    fill: { color: 'FFFFFF' }, line: { color: 'E2E8F0', width: 1 }
  });
  s.addImage({ path: 'C:/Users/emanu/IdeaProjects/BDII-TP2/utn_logo_big.png',
    x: 0.3, y: 0.16, w: 2.65, h: 0.51 });

  // Graph decoration — circles connected by lines
  const nodes = [
    { x: 7.6, y: 0.7,  r: 0.55, c: BLUE   },
    { x: 8.8, y: 1.9,  r: 0.45, c: GREEN  },
    { x: 7.3, y: 2.8,  r: 0.4,  c: ORANGE },
    { x: 9.3, y: 3.2,  r: 0.38, c: PURPLE },
    { x: 8.1, y: 3.85, r: 0.32, c: BLUE   },
  ];
  const edges = [[0,1],[1,2],[1,3],[2,4],[3,4]];
  edges.forEach(([a, b]) => {
    const na = nodes[a], nb = nodes[b];
    s.addShape(pres.shapes.LINE, {
      x: na.x + na.r, y: na.y + na.r,
      w: (nb.x + nb.r) - (na.x + na.r),
      h: (nb.y + nb.r) - (na.y + na.r),
      line: { color: BORDER, width: 1.5 }
    });
  });
  nodes.forEach(n => {
    s.addShape(pres.shapes.OVAL, {
      x: n.x, y: n.y, w: n.r * 2, h: n.r * 2,
      fill: { color: n.c, transparency: 20 },
      line: { color: n.c, width: 1.5 }
    });
  });

  // Node labels
  const nodeLabels = ['User','Order','Product','Category','Neo4j'];
  nodeLabels.forEach((lbl, i) => {
    const n = nodes[i];
    s.addText(lbl, {
      x: n.x, y: n.y, w: n.r * 2, h: n.r * 2,
      fontSize: 7, bold: true, color: WHITE, align: 'center', valign: 'middle', margin: 0
    });
  });

  // Main title
  s.addText('GraphShop', {
    x: 0.5, y: 0.8, w: 6.8, h: 1.2,
    fontSize: 64, bold: true, color: BLUE, fontFace: 'Calibri', align: 'left', margin: 0
  });
  s.addText('Sistema de E-Commerce sobre Neo4j', {
    x: 0.5, y: 1.95, w: 6.8, h: 0.5,
    fontSize: 20, color: WHITE, fontFace: 'Calibri', align: 'left', margin: 0
  });
  s.addText('Bases de Datos II · Trabajo Práctico 2 · UTN FRC · 2025', {
    x: 0.5, y: 2.5, w: 6.8, h: 0.35,
    fontSize: 12, color: MUTED, fontFace: 'Calibri', align: 'left', margin: 0
  });

  // Divider
  s.addShape(pres.shapes.LINE, {
    x: 0.5, y: 3.1, w: 4.5, h: 0,
    line: { color: BORDER, width: 1 }
  });

  // Team cards
  const members = ['Amarilla, Franco', 'Cortez, Emanuel', 'Carreggio, Juan'];
  const mColors = [BLUE, GREEN, ORANGE];
  members.forEach((m, i) => {
    const cx = 0.5 + i * 1.6;
    s.addShape(pres.shapes.RECTANGLE, {
      x: cx, y: 3.3, w: 1.45, h: 0.55,
      fill: { color: mColors[i], transparency: 80 },
      line: { color: mColors[i], width: 1 }
    });
    s.addText(m, {
      x: cx, y: 3.3, w: 1.45, h: 0.55,
      fontSize: 10, bold: true, color: mColors[i],
      align: 'center', valign: 'middle', margin: 0
    });
  });

  // Tech stack pills
  const techs = ['Java 17', 'Neo4j 5.x', 'Cypher', 'Java Swing', 'Maven'];
  techs.forEach((t, i) => {
    const px = 0.5 + i * 1.82;
    s.addShape(pres.shapes.RECTANGLE, {
      x: px, y: 4.85, w: 1.65, h: 0.35,
      fill: { color: CARD },
      line: { color: BORDER, width: 1 }
    });
    s.addText(t, {
      x: px, y: 4.85, w: 1.65, h: 0.35,
      fontSize: 10, color: MUTED, align: 'center', valign: 'middle', margin: 0
    });
  });
  s.addText('Stack tecnológico', {
    x: 0.5, y: 4.6, w: 5, h: 0.25,
    fontSize: 9, color: MUTED, align: 'left', margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 2 — El Problema
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 1, 'Descripción del Problema');
  slideTitle(s, '¿Por qué un e-commerce necesita grafos?');

  // Left: 3 business questions
  const questions = [
    { icon: '🛒', q: 'Filtrado Colaborativo', d: '¿Qué otros productos compraron los usuarios que compraron lo mismo que yo?', c: BLUE },
    { icon: '👤', q: 'Similitud de Usuarios', d: '¿Qué tan similares son dos usuarios según su historial de compras?',         c: GREEN },
    { icon: '👁️',  q: 'Abandono de Carrito',  d: '¿Qué productos vio un usuario pero nunca compró? (remarketing)',              c: ORANGE },
  ];

  questions.forEach((q, i) => {
    const y = 1.28 + i * 1.32;
    card(s, 0.4, y, 4.7, 1.15, { border: q.c });
    s.addShape(pres.shapes.RECTANGLE, {
      x: 0.4, y, w: 0.06, h: 1.15,
      fill: { color: q.c }, line: { color: q.c, width: 0 }
    });
    s.addText(q.q, {
      x: 0.6, y: y + 0.08, w: 4.35, h: 0.3,
      fontSize: 13, bold: true, color: q.c, align: 'left', margin: 0
    });
    s.addText(q.d, {
      x: 0.6, y: y + 0.38, w: 4.35, h: 0.65,
      fontSize: 11, color: WHITE, align: 'left', margin: 0
    });
  });

  // Right: SQL pain vs Neo4j advantage
  card(s, 5.35, 1.28, 4.3, 4.0, { border: BORDER });
  s.addText('SQL Relacional', {
    x: 5.5, y: 1.38, w: 3.8, h: 0.35,
    fontSize: 14, bold: true, color: RED, align: 'left', margin: 0
  });
  const sqlPains = [
    'JOIN cascadeados para cada consulta compleja',
    'Rendimiento O(n×m) por cada JOIN adicional',
    'Tablas puente artificiales (pedido_items, etc.)',
    'NOT IN (SELECT ...) costoso para patrones negativos',
    'ALTER TABLE para agregar propiedades nuevas',
  ];
  s.addText(sqlPains.map((t, i) => ({
    text: t,
    options: { bullet: true, breakLine: i < sqlPains.length - 1, fontSize: 10.5, color: MUTED }
  })), { x: 5.5, y: 1.78, w: 4.0, h: 1.55 });

  s.addShape(pres.shapes.LINE, { x: 5.5, y: 3.42, w: 3.8, h: 0, line: { color: BORDER, width: 1 } });

  s.addText('Neo4j — Grafos Nativos', {
    x: 5.5, y: 3.52, w: 3.8, h: 0.35,
    fontSize: 14, bold: true, color: GREEN, align: 'left', margin: 0
  });
  const neo4jWins = [
    'Traversal nativo O(1) por salto',
    'Relaciones como ciudadanos de primera clase',
    'Consultas negativas directas (NOT EXISTS)',
    'Esquema flexible sin migraciones',
    'Cypher: legible, expresivo, conciso',
  ];
  s.addText(neo4jWins.map((t, i) => ({
    text: t,
    options: { bullet: true, breakLine: i < neo4jWins.length - 1, fontSize: 10.5, color: WHITE }
  })), { x: 5.5, y: 3.92, w: 4.0, h: 1.2 });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 3 — Diseño del Grafo — Nodos
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 1, 'Diseño de la Base de Datos');
  slideTitle(s, 'Diseño del Grafo — Nodos', '4 tipos de nodos · 95 instancias en total');

  const nodes = [
    {
      label: '(:User)', color: BLUE,
      props: ['id: String (PK)', 'nombre: String', 'email: String (único)', 'edad: Integer', 'ciudad: String', 'puntosFidelidad: Integer'],
      count: '25 usuarios'
    },
    {
      label: '(:Product)', color: GREEN,
      props: ['id: String (PK)', 'nombre: String', 'descripcion: String', 'precio: Double (ARS)', 'stock: Integer', 'marca: String'],
      count: '30 productos'
    },
    {
      label: '(:Order)', color: ORANGE,
      props: ['id: String (PK)', 'fecha: String (ISO 8601)', 'estado: COMPLETADO|PENDIENTE…', 'montoTotal: Double (ARS)', 'metodoPago: String', 'direccionEnvio: String'],
      count: '35 pedidos'
    },
    {
      label: '(:Category)', color: PURPLE,
      props: ['id: String (PK)', 'nombre: String', 'descripcion: String', '', 'Electrónica · Moda · Hogar', 'Deportes · Libros'],
      count: '5 categorías'
    },
  ];

  nodes.forEach((n, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const x = 0.35 + col * 4.85;
    const y = 1.3 + row * 2.1;
    card(s, x, y, 4.55, 1.95, { border: n.color });
    s.addShape(pres.shapes.RECTANGLE, {
      x, y, w: 4.55, h: 0.45,
      fill: { color: n.color, transparency: 75 },
      line: { color: n.color, width: 0 }
    });
    s.addText(n.label, {
      x: x + 0.12, y, w: 3.2, h: 0.45,
      fontSize: 14, bold: true, color: n.color, fontFace: 'Consolas',
      align: 'left', valign: 'middle', margin: 0
    });
    s.addText(n.count, {
      x: x + 3.2, y, w: 1.2, h: 0.45,
      fontSize: 10, color: BG, bold: true,
      align: 'center', valign: 'middle', margin: 0
    });
    n.props.filter(Boolean).slice(0, 5).forEach((p, pi) => {
      s.addText(p, {
        x: x + 0.15, y: y + 0.52 + pi * 0.27, w: 4.2, h: 0.26,
        fontSize: 9.5, color: pi < 3 ? WHITE : MUTED, fontFace: 'Consolas',
        align: 'left', margin: 0
      });
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 4 — Diseño del Grafo — Relaciones
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 1, 'Diseño de la Base de Datos');
  slideTitle(s, 'Diseño del Grafo — Relaciones', '5 tipos de relaciones · ~176 instancias');

  // Left: visual graph diagram
  card(s, 0.35, 1.22, 5.0, 4.1, {});

  // Draw graph visually
  const gNodes = [
    { x: 1.85, y: 1.85, label: 'User',     c: BLUE,   r: 0.48 },
    { x: 3.5,  y: 2.5,  label: 'Order',    c: ORANGE, r: 0.4  },
    { x: 2.6,  y: 3.7,  label: 'Product',  c: GREEN,  r: 0.45 },
    { x: 1.1,  y: 3.65, label: 'Category', c: PURPLE, r: 0.38 },
  ];

  const gEdges = [
    { from: 0, to: 1, label: 'REALIZO',     side: 'above', c: ORANGE },
    { from: 1, to: 2, label: 'CONTIENE',    side: 'right', c: GREEN  },
    { from: 2, to: 3, label: 'PERTENECE_A', side: 'below', c: PURPLE },
    { from: 0, to: 2, label: 'VISTO',       side: 'left',  c: BLUE   },
    { from: 0, to: 2, label: 'RESENO',      side: 'left2', c: MUTED  },
  ];

  // Draw edges
  gEdges.forEach(e => {
    const a = gNodes[e.from], b = gNodes[e.to];
    const ax = a.x + a.r, ay = a.y + a.r, bx = b.x + b.r, by = b.y + b.r;
    s.addShape(pres.shapes.LINE, {
      x: Math.min(ax, bx), y: Math.min(ay, by),
      w: Math.abs(bx - ax) || 0.01, h: Math.abs(by - ay) || 0.01,
      line: { color: e.c, width: 1.8 }
    });
    const lx = (ax + bx) / 2 - 0.55;
    const ly = (ay + by) / 2 - 0.15;
    s.addText(e.label, {
      x: lx, y: ly, w: 1.1, h: 0.22,
      fontSize: 7, bold: true, color: e.c, align: 'center', margin: 0
    });
  });

  // Draw nodes
  gNodes.forEach(n => {
    s.addShape(pres.shapes.OVAL, {
      x: n.x, y: n.y, w: n.r * 2, h: n.r * 2,
      fill: { color: n.c, transparency: 15 },
      line: { color: n.c, width: 2 }
    });
    s.addText(n.label, {
      x: n.x, y: n.y, w: n.r * 2, h: n.r * 2,
      fontSize: 9, bold: true, color: WHITE, align: 'center', valign: 'middle', margin: 0
    });
  });

  // Right: relationship table
  const rels = [
    { name: '[:REALIZO]',     from: 'User',    to: 'Order',    props: 'sin propiedades',              c: ORANGE },
    { name: '[:CONTIENE]',    from: 'Order',   to: 'Product',  props: 'cantidad, precioUnitario',     c: GREEN  },
    { name: '[:PERTENECE_A]', from: 'Product', to: 'Category', props: 'sin propiedades',              c: PURPLE },
    { name: '[:VISTO]',       from: 'User',    to: 'Product',  props: 'timestamp, duracionSegundos',  c: BLUE   },
    { name: '[:RESENO]',      from: 'User',    to: 'Product',  props: 'calificacion, comentario, fecha', c: MUTED },
  ];

  rels.forEach((r, i) => {
    const y = 1.32 + i * 0.78;
    card(s, 5.55, y, 4.1, 0.68, { border: r.c });
    s.addShape(pres.shapes.RECTANGLE, {
      x: 5.55, y, w: 0.05, h: 0.68,
      fill: { color: r.c }, line: { color: r.c, width: 0 }
    });
    s.addText(r.name, {
      x: 5.72, y: y + 0.04, w: 3.8, h: 0.26,
      fontSize: 11, bold: true, color: r.c, fontFace: 'Consolas', align: 'left', margin: 0
    });
    s.addText(`${r.from} → ${r.to}  ·  ${r.props}`, {
      x: 5.72, y: y + 0.33, w: 3.8, h: 0.26,
      fontSize: 9, color: MUTED, align: 'left', margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 5 — Constraints e Índices
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 1, 'Diseño de la Base de Datos');
  slideTitle(s, 'Constraints e Índices', 'Garantías de integridad y rendimiento');

  const constraints = [
    { cypher: 'CREATE CONSTRAINT unique_user_id    FOR (u:User)     REQUIRE u.id IS UNIQUE;',     why: 'Evita usuarios duplicados',            c: BLUE   },
    { cypher: 'CREATE CONSTRAINT unique_product_id FOR (p:Product)  REQUIRE p.id IS UNIQUE;',     why: 'Evita productos duplicados',           c: GREEN  },
    { cypher: 'CREATE CONSTRAINT unique_order_id   FOR (o:Order)    REQUIRE o.id IS UNIQUE;',     why: 'Evita pedidos duplicados',             c: ORANGE },
    { cypher: 'CREATE CONSTRAINT unique_category_id FOR (c:Category) REQUIRE c.id IS UNIQUE;',   why: 'Categorías sin duplicación',           c: PURPLE },
    { cypher: 'CREATE INDEX user_email_index       FOR (u:User) ON (u.email);',                  why: 'Búsqueda O(1) por email — login rápido', c: BLUE },
  ];

  constraints.forEach((c, i) => {
    const y = 1.32 + i * 0.8;
    card(s, 0.35, y, 9.3, 0.68, { border: c.c });
    s.addShape(pres.shapes.RECTANGLE, {
      x: 0.35, y, w: 0.05, h: 0.68,
      fill: { color: c.c }, line: { color: c.c, width: 0 }
    });
    // Cypher code
    s.addText(c.cypher, {
      x: 0.55, y: y + 0.04, w: 7.1, h: 0.28,
      fontSize: 9.5, bold: false, color: WHITE, fontFace: 'Consolas', align: 'left', margin: 0
    });
    // Why
    s.addShape(pres.shapes.RECTANGLE, {
      x: 7.8, y: y + 0.1, w: 1.7, h: 0.48,
      fill: { color: c.c, transparency: 82 },
      line: { color: c.c, width: 1 }
    });
    s.addText(c.why, {
      x: 7.8, y: y + 0.1, w: 1.7, h: 0.48,
      fontSize: 8.5, color: c.c, align: 'center', valign: 'middle', margin: 0
    });
  });

  // Bottom note
  s.addText('IF NOT EXISTS — los constraints son idempotentes: seguros para re-ejecutar en cada inicio de la aplicación.', {
    x: 0.35, y: 5.22, w: 9.3, h: 0.22,
    fontSize: 9, color: MUTED, align: 'center', margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 6 — Consultas Cypher
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 2, 'Consultas Cypher');
  slideTitle(s, '10 Consultas Cypher', 'Desde traversal simple hasta patrones negativos');

  // Query cards - 3 representative ones
  const queries = [
    {
      num: '4', title: 'Usuarios Inactivos', c: ORANGE,
      cypher: 'MATCH (u:User)\nWHERE NOT EXISTS {\n  MATCH (u)-[:REALIZO]->(:Order) }\nRETURN u.nombre, u.email',
      sql: 'SELECT u.* FROM usuarios u\nLEFT JOIN pedidos p ON u.id = p.user_id\nWHERE p.id IS NULL',
      note: 'Patrón negativo: detecta los 5 usuarios sin pedidos'
    },
    {
      num: '7', title: 'Top Productos Vendidos', c: GREEN,
      cypher: 'MATCH (o:Order)-[c:CONTIENE]->(p:Product)\nWHERE o.estado = \'COMPLETADO\'\nRETURN p.nombre, SUM(c.cantidad) AS total\nORDER BY total DESC LIMIT 10',
      sql: 'SELECT p.nombre, SUM(oi.cantidad)\nFROM pedidos o\nJOIN pedido_items oi ON o.id=oi.pedido_id\nJOIN productos p ON p.id=oi.producto_id\nWHERE o.estado=\'COMPLETADO\'\nGROUP BY p.nombre ORDER BY 2 DESC',
      note: 'Traversal 2 saltos con agregación sobre relación'
    },
    {
      num: '8', title: 'Vieron pero no Compraron', c: RED,
      cypher: 'MATCH (u:User)-[:VISTO]->(p:Product {id: $id})\nWHERE NOT EXISTS {\n  MATCH (u)-[:REALIZO]->(:Order)-[:CONTIENE]->(p) }\nRETURN u.nombre, u.email',
      sql: 'SELECT u.nombre FROM usuarios u\nJOIN vistas v ON u.id=v.usuario_id\nWHERE v.producto_id=?\n  AND u.id NOT IN (\n    SELECT pi.usuario_id FROM pedidos p\n    JOIN pedido_items pi ON p.id=pi.pedido_id\n    WHERE pi.producto_id=?)',
      note: 'Remarketing: combina traversal positivo + negativo'
    },
  ];

  queries.forEach((q, i) => {
    const y = 1.28 + i * 1.4;
    // Header
    s.addShape(pres.shapes.RECTANGLE, {
      x: 0.35, y, w: 9.3, h: 0.3,
      fill: { color: q.c, transparency: 80 },
      line: { color: q.c, width: 0 }
    });
    s.addText(`Consulta ${q.num}  ·  ${q.title}`, {
      x: 0.5, y, w: 5, h: 0.3,
      fontSize: 12, bold: true, color: q.c, align: 'left', valign: 'middle', margin: 0
    });
    s.addText(q.note, {
      x: 5.2, y, w: 4.3, h: 0.3,
      fontSize: 9, color: BG, align: 'right', valign: 'middle', margin: 0
    });

    // Cypher column
    card(s, 0.35, y + 0.3, 4.55, 1.05, { border: q.c });
    s.addText('Cypher', { x: 0.5, y: y + 0.35, w: 1, h: 0.22, fontSize: 8, bold: true, color: q.c, margin: 0 });
    s.addText(q.cypher, {
      x: 0.42, y: y + 0.55, w: 4.35, h: 0.72,
      fontSize: 8.5, color: WHITE, fontFace: 'Consolas', align: 'left', margin: 0
    });

    // SQL column
    card(s, 5.05, y + 0.3, 4.6, 1.05, { border: BORDER });
    s.addText('SQL equivalente', { x: 5.15, y: y + 0.35, w: 2, h: 0.22, fontSize: 8, bold: true, color: MUTED, margin: 0 });
    s.addText(q.sql, {
      x: 5.1, y: y + 0.55, w: 4.42, h: 0.72,
      fontSize: 8.5, color: MUTED, fontFace: 'Consolas', align: 'left', margin: 0
    });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 7 — Filtrado Colaborativo
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 2, 'Algoritmos de Grafo');
  slideTitle(s, 'Algoritmo 1 — Filtrado Colaborativo', 'Traversal de 4 saltos · recomendaciones personalizadas');

  // Step flow diagram
  const steps = [
    { n: '1', label: 'Target\nUser',   sub: 'U001', c: BLUE },
    { n: '↓ REALIZO', label: '', sub: '', c: MUTED },
    { n: '2', label: 'Sus\nPedidos',   sub: 'COLLECT()', c: ORANGE },
    { n: '↓ CONTIENE', label: '', sub: '', c: MUTED },
    { n: '3', label: 'Usuarios\nSimilares', sub: 'ORDER BY common DESC', c: GREEN },
    { n: '↓ REALIZO', label: '', sub: '', c: MUTED },
    { n: '4', label: 'Productos\nNuevos', sub: 'NOT IN comprados', c: PURPLE },
  ];

  steps.forEach((st, i) => {
    if (!st.label) {
      s.addText(st.n, {
        x: 0.5, y: 1.45 + i * 0.5, w: 1.8, h: 0.3,
        fontSize: 10, color: MUTED, align: 'center', margin: 0
      });
    } else {
      card(s, 0.35, 1.35 + i * 0.5, 2.0, 0.45, { border: st.c });
      s.addText(st.n + '  ' + st.label, {
        x: 0.45, y: 1.35 + i * 0.5, w: 1.8, h: 0.45,
        fontSize: 10, bold: true, color: st.c, align: 'left', valign: 'middle', margin: 0
      });
    }
  });

  // Cypher code
  card(s, 2.65, 1.32, 7.0, 3.8, { border: BLUE });
  s.addText('Cypher — Filtrado Colaborativo completo', {
    x: 2.8, y: 1.42, w: 6.6, h: 0.3,
    fontSize: 11, bold: true, color: BLUE, align: 'left', margin: 0
  });

  const code = [
    '// Paso 1: productos ya comprados por el usuario objetivo',
    'MATCH (target:User {id: $userId})-[:REALIZO]->(o)-[:CONTIENE]->(mis:Product)',
    'WITH target, COLLECT(DISTINCT mis) AS misProductos',
    '',
    '// Paso 2: usuarios similares (comparten productos)',
    'MATCH (otro:User)-[:REALIZO]->(o2)-[:CONTIENE]->(p)',
    'WHERE otro <> target AND p IN misProductos',
    'WITH target, misProductos, otro, COUNT(DISTINCT p) AS enComun',
    'ORDER BY enComun DESC LIMIT 8',
    '',
    '// Paso 3: productos que compraron los similares y el target NO tiene',
    'MATCH (otro)-[:REALIZO]->(:Order)-[:CONTIENE]->(rec:Product)',
    'WHERE NOT rec IN misProductos',
    'RETURN rec.nombre, COUNT(DISTINCT otro) AS votos',
    'ORDER BY votos DESC LIMIT 10',
  ];

  s.addText(code.join('\n'), {
    x: 2.8, y: 1.78, w: 6.7, h: 3.2,
    fontSize: 8.2, color: WHITE, fontFace: 'Consolas', align: 'left', valign: 'top', margin: 0
  });

  // Result badge
  s.addShape(pres.shapes.RECTANGLE, {
    x: 0.35, y: 5.05, w: 9.3, h: 0.35,
    fill: { color: BLUE, transparency: 85 },
    line: { color: BLUE, width: 0 }
  });
  s.addText('SQL equivalente requeriría: 4+ JOINs · 2 subconsultas correlacionadas · GROUP BY · HAVING · NOT IN (SELECT...)  — 20+ líneas', {
    x: 0.5, y: 5.05, w: 9.0, h: 0.35,
    fontSize: 9.5, color: BLUE, align: 'center', valign: 'middle', margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 8 — Similitud Jaccard
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 2, 'Algoritmos de Grafo');
  slideTitle(s, 'Algoritmo 2 — Similitud de Jaccard', 'Mide cuán parecidos son dos usuarios por sus compras');

  // Formula card
  card(s, 0.35, 1.28, 4.5, 1.1, { border: GREEN });
  s.addText('Fórmula', {
    x: 0.55, y: 1.38, w: 4.1, h: 0.3,
    fontSize: 11, bold: true, color: GREEN, align: 'left', margin: 0
  });
  s.addText('Jaccard(A, B)  =  |A ∩ B|  /  |A ∪ B|', {
    x: 0.5, y: 1.68, w: 4.2, h: 0.6,
    fontSize: 18, bold: true, color: WHITE, fontFace: 'Cambria', align: 'center', valign: 'middle', margin: 0
  });

  // Examples
  const examples = [
    { a: 1.0, desc: 'Mismos productos comprados',   c: GREEN  },
    { a: 0.5, desc: '50% de productos en común',    c: ORANGE },
    { a: 0.0, desc: 'Sin ningún producto en común', c: RED    },
  ];
  examples.forEach((e, i) => {
    card(s, 0.35, 2.52 + i * 0.62, 4.5, 0.52, { border: e.c });
    s.addText(`${e.a.toFixed(1)}`, {
      x: 0.45, y: 2.52 + i * 0.62, w: 0.8, h: 0.52,
      fontSize: 22, bold: true, color: e.c, align: 'center', valign: 'middle', margin: 0
    });
    s.addText(e.desc, {
      x: 1.3, y: 2.52 + i * 0.62, w: 3.4, h: 0.52,
      fontSize: 12, color: WHITE, align: 'left', valign: 'middle', margin: 0
    });
  });

  // Cypher code
  card(s, 5.1, 1.28, 4.55, 3.6, { border: GREEN });
  s.addText('Implementación en Cypher', {
    x: 5.25, y: 1.38, w: 4.2, h: 0.3,
    fontSize: 11, bold: true, color: GREEN, align: 'left', margin: 0
  });
  const jCode = [
    'MATCH (u1:User {id: $userId})',
    '  -[:REALIZO]->(o1)-[:CONTIENE]->(p)',
    'WITH u1, COLLECT(DISTINCT id(p)) AS idsU1',
    '',
    'MATCH (u2:User)-[:REALIZO]->(o2)-[:CONTIENE]->(p2)',
    'WHERE u2 <> u1',
    'WITH u1, idsU1, u2, COLLECT(DISTINCT id(p2)) AS idsU2',
    '',
    'WITH u1, u2,',
    '  [x IN idsU1 WHERE x IN idsU2] AS interseccion,',
    '  idsU1 + [x IN idsU2 WHERE NOT x IN idsU1] AS union',
    'WHERE SIZE(interseccion) > 0',
    '',
    'RETURN u2.nombre,',
    '  round(toFloat(SIZE(interseccion)) / SIZE(union) * 100)/100',
    '  AS jaccard',
    'ORDER BY jaccard DESC LIMIT 8',
  ];
  s.addText(jCode.join('\n'), {
    x: 5.22, y: 1.72, w: 4.3, h: 3.05,
    fontSize: 8.2, color: WHITE, fontFace: 'Consolas', align: 'left', valign: 'top', margin: 0
  });

  // Bottom use case
  card(s, 0.35, 4.85, 9.3, 0.5, { border: BORDER });
  s.addText('Uso de negocio: encontrar usuarios afines para campañas de email segmentadas · cross-selling inteligente · recomendaciones sociales', {
    x: 0.5, y: 4.85, w: 9.0, h: 0.5,
    fontSize: 10.5, color: MUTED, align: 'center', valign: 'middle', margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 9 — Neo4j vs SQL
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 2, 'Comparación Técnica');
  slideTitle(s, 'Neo4j vs SQL Relacional', 'Comparación directa para el dominio e-commerce');

  const rows = [
    { aspect: 'Navegación de relaciones', neo: 'Traversal nativo  O(1) por salto', sql: 'JOIN con costo  O(n × m)' },
    { aspect: 'Filtrado colaborativo',     neo: '~15 líneas Cypher, traversal natural', sql: '4+ JOINs, subconsultas, 20+ líneas' },
    { aspect: 'Similitud de usuarios',     neo: 'Operaciones de conjuntos nativas',   sql: 'Window functions avanzadas' },
    { aspect: 'Patrones negativos',        neo: 'NOT EXISTS { MATCH ... }  directo',  sql: 'NOT IN (SELECT ...)  costoso' },
    { aspect: 'Modelado',                  neo: 'Esquema = dominio real (sin tablas puente)', sql: 'Normalización + tablas puente artificiales' },
    { aspect: 'Agregar propiedades',       neo: 'Sin migraciones: propiedad opcional', sql: 'ALTER TABLE en producción' },
    { aspect: 'Rendimiento en grafos densos', neo: 'Traversal no degrada con nodos no relacionados', sql: 'Full table scan sin índices selectivos' },
  ];

  // Header
  s.addShape(pres.shapes.RECTANGLE, {
    x: 0.35, y: 1.28, w: 9.3, h: 0.38,
    fill: { color: CARD }, line: { color: BORDER, width: 1 }
  });
  s.addText('Aspecto', { x: 0.5, y: 1.28, w: 2.7, h: 0.38, fontSize: 11, bold: true, color: MUTED, align: 'center', valign: 'middle', margin: 0 });
  s.addShape(pres.shapes.RECTANGLE, { x: 3.25, y: 1.28, w: 3.2, h: 0.38, fill: { color: GREEN, transparency: 85 }, line: { color: GREEN, width: 0 } });
  s.addText('Neo4j (Grafos)', { x: 3.25, y: 1.28, w: 3.2, h: 0.38, fontSize: 11, bold: true, color: GREEN, align: 'center', valign: 'middle', margin: 0 });
  s.addShape(pres.shapes.RECTANGLE, { x: 6.5, y: 1.28, w: 3.15, h: 0.38, fill: { color: RED, transparency: 85 }, line: { color: RED, width: 0 } });
  s.addText('SQL Relacional', { x: 6.5, y: 1.28, w: 3.15, h: 0.38, fontSize: 11, bold: true, color: RED, align: 'center', valign: 'middle', margin: 0 });

  rows.forEach((r, i) => {
    const y = 1.66 + i * 0.53;
    const bgFill = i % 2 === 0 ? CARD : BG;
    s.addShape(pres.shapes.RECTANGLE, { x: 0.35, y, w: 9.3, h: 0.5, fill: { color: bgFill }, line: { color: BORDER, width: 0.5 } });
    s.addText(r.aspect, { x: 0.5, y, w: 2.7, h: 0.5, fontSize: 10, bold: true, color: WHITE, align: 'left', valign: 'middle', margin: { left: 4, right: 4, top: 0, bottom: 0 } });
    s.addText('✓  ' + r.neo, { x: 3.28, y, w: 3.12, h: 0.5, fontSize: 9.5, color: GREEN, align: 'left', valign: 'middle', margin: { left: 4, right: 4, top: 0, bottom: 0 } });
    s.addText('✗  ' + r.sql, { x: 6.52, y, w: 3.1, h: 0.5, fontSize: 9.5, color: RED, align: 'left', valign: 'middle', margin: { left: 4, right: 4, top: 0, bottom: 0 } });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 10 — Ventajas y Desventajas
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 2, 'Análisis Crítico');
  slideTitle(s, 'Ventajas y Desventajas de Neo4j', 'Frente a SQL relacional y otras bases NoSQL');

  // Ventajas
  card(s, 0.35, 1.28, 4.6, 4.05, { border: GREEN });
  s.addShape(pres.shapes.RECTANGLE, {
    x: 0.35, y: 1.28, w: 4.6, h: 0.42,
    fill: { color: GREEN, transparency: 75 },
    line: { color: GREEN, width: 0 }
  });
  s.addText('Ventajas', { x: 0.5, y: 1.28, w: 4.3, h: 0.42, fontSize: 15, bold: true, color: GREEN, align: 'left', valign: 'middle', margin: 0 });

  const pros = [
    'Relaciones como entidades de primera clase: sin JOINs artificiales',
    'Consultas de red (4+ saltos) expresadas en pocas líneas de Cypher',
    'Rendimiento constante en grafos densos — no depende de nodos no relacionados',
    'Esquema flexible: agregar propiedades sin migraciones',
    'Modelado intuitivo: el grafo refleja el dominio real del negocio',
    'Potente para recomendaciones, detección de fraude, redes sociales',
    'Neo4j Browser: visualización interactiva del grafo incluida',
  ];
  s.addText(pros.map((p, i) => ({
    text: p, options: { bullet: true, breakLine: i < pros.length - 1, fontSize: 10.5, color: WHITE }
  })), { x: 0.52, y: 1.78, w: 4.25, h: 3.45 });

  // Desventajas
  card(s, 5.1, 1.28, 4.55, 4.05, { border: RED });
  s.addShape(pres.shapes.RECTANGLE, {
    x: 5.1, y: 1.28, w: 4.55, h: 0.42,
    fill: { color: RED, transparency: 75 },
    line: { color: RED, width: 0 }
  });
  s.addText('Desventajas', { x: 5.25, y: 1.28, w: 4.2, h: 0.42, fontSize: 15, bold: true, color: RED, align: 'left', valign: 'middle', margin: 0 });

  const cons = [
    'Community Edition: sin clustering ni backups automáticos',
    'Menor ecosistema de herramientas vs SQL (BI, ORM, etc.)',
    'Cypher: curva de aprendizaje inicial para equipos SQL',
    'No ideal para datos altamente tabulares (contabilidad, ERP)',
    'Menor rendimiento en consultas de tipo "toda la tabla"',
    'Menor adopción empresarial vs PostgreSQL, MySQL, Oracle',
    'Almacenamiento en memoria: costoso para grafos masivos',
  ];
  s.addText(cons.map((c, i) => ({
    text: c, options: { bullet: true, breakLine: i < cons.length - 1, fontSize: 10.5, color: WHITE }
  })), { x: 5.25, y: 1.78, w: 4.25, h: 3.45 });

  // vs MongoDB note
  s.addShape(pres.shapes.RECTANGLE, {
    x: 0.35, y: 5.2, w: 9.3, h: 0.2,
    fill: { color: CARD }, line: { color: BORDER, width: 0 }
  });
  s.addText('vs MongoDB (NoSQL documental): Neo4j supera en consultas de red; MongoDB es mejor para documentos anidados sin relaciones cruzadas.', {
    x: 0.5, y: 5.2, w: 9.0, h: 0.2,
    fontSize: 8.5, color: MUTED, align: 'center', valign: 'middle', margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 11 — Arquitectura del Backend
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 3, 'Arquitectura');
  slideTitle(s, 'Arquitectura del Backend', 'Patrón de capas con patrón Singleton para la conexión Bolt');

  // Layer diagram
  const layers = [
    { name: 'Presentación',    detail: 'Main.java (consola)  ·  DashboardWindow.java (Swing)', c: BLUE },
    { name: 'Servicio',        detail: 'UserService · ProductService · OrderService · RecommendationService', c: GREEN },
    { name: 'Repositorio',     detail: 'UserRepository · ProductRepository · OrderRepository · RecommendationRepository', c: ORANGE },
    { name: 'Configuración',   detail: 'Neo4jConfig (Singleton) — conexión Bolt única en toda la app', c: PURPLE },
    { name: 'Base de Datos',   detail: 'Neo4j Community 5.x  ·  bolt://localhost:7687', c: MUTED },
  ];

  layers.forEach((l, i) => {
    const y = 1.32 + i * 0.82;
    card(s, 0.35, y, 6.0, 0.7, { border: l.c });
    s.addShape(pres.shapes.RECTANGLE, {
      x: 0.35, y, w: 0.06, h: 0.7,
      fill: { color: l.c }, line: { color: l.c, width: 0 }
    });
    s.addText(l.name, { x: 0.55, y, w: 1.6, h: 0.7, fontSize: 12, bold: true, color: l.c, align: 'left', valign: 'middle', margin: 0 });
    s.addText(l.detail, { x: 2.2, y, w: 3.95, h: 0.7, fontSize: 9.5, color: WHITE, align: 'left', valign: 'middle', margin: 0 });
    if (i < layers.length - 1) {
      s.addText('↓', { x: 3.0, y: y + 0.7, w: 0.4, h: 0.12, fontSize: 10, color: BORDER, align: 'center', margin: 0 });
    }
  });

  // Right: package structure
  card(s, 6.55, 1.32, 3.1, 4.1, { border: BORDER });
  s.addText('Estructura de paquetes', { x: 6.7, y: 1.42, w: 2.8, h: 0.3, fontSize: 10, bold: true, color: MUTED, align: 'left', margin: 0 });
  const pkg = [
    'com.ecommerce/',
    '├── config/',
    '│   └── Neo4jConfig.java',
    '├── model/',
    '│   ├── User.java',
    '│   ├── Product.java',
    '│   └── Order.java',
    '├── repository/',
    '│   └── *Repository.java',
    '├── service/',
    '│   └── *Service.java',
    '├── report/',
    '│   └── ReportService.java',
    '└── ui/',
    '    └── DashboardWindow.java',
  ];
  s.addText(pkg.join('\n'), {
    x: 6.65, y: 1.78, w: 2.9, h: 3.55,
    fontSize: 8.2, color: MUTED, fontFace: 'Consolas', align: 'left', valign: 'top', margin: 0
  });

  // Singleton code snippet
  s.addText('Neo4jConfig — Singleton', { x: 0.35, y: 5.18, w: 5, h: 0.22, fontSize: 9, bold: true, color: PURPLE, margin: 0 });
  s.addText('getInstance() · getSession() · verifyConnectivity() · AutoCloseable', {
    x: 0.35, y: 5.38, w: 6.0, h: 0.2,
    fontSize: 9, color: MUTED, fontFace: 'Consolas', margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 12 — Dashboard Visual
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 3, 'Dashboard Visual');
  slideTitle(s, 'Dashboard Visual — 6 Pestañas', 'Java Swing + Graphics2D puro · tema oscuro · sin librerías externas');

  const tabs = [
    { num: '1', name: 'Ingresos por Categoría',  viz: 'Barras horizontales + torta con %',        c: BLUE   },
    { num: '2', name: 'Top Clientes',             viz: 'Barras verticales + tabla con ranking',    c: GREEN  },
    { num: '3', name: 'Calificaciones',           viz: 'Barras con semáforo (verde/ámbar/rojo)',   c: ORANGE },
    { num: '4', name: 'Actividad del Sistema',    viz: 'Torta de estados + productos más vistos',  c: PURPLE },
    { num: '5', name: 'Métodos de Pago',          viz: 'Barras + torta + KPI cards',              c: RED    },
    { num: '6', name: 'Estadísticas + Esquema',   viz: '7 KPI cards + diagrama del grafo Neo4j',  c: MUTED  },
  ];

  tabs.forEach((t, i) => {
    const col = i % 3;
    const row = Math.floor(i / 3);
    const x = 0.35 + col * 3.22;
    const y = 1.28 + row * 2.0;

    card(s, x, y, 3.05, 1.78, { border: t.c });
    s.addShape(pres.shapes.RECTANGLE, {
      x, y, w: 3.05, h: 0.4,
      fill: { color: t.c, transparency: 78 },
      line: { color: t.c, width: 0 }
    });
    s.addText(`Tab ${t.num}  ·  ${t.name}`, {
      x: x + 0.12, y, w: 2.8, h: 0.4,
      fontSize: 10.5, bold: true, color: t.c, align: 'left', valign: 'middle', margin: 0
    });
    s.addText(t.viz, {
      x: x + 0.12, y: y + 0.48, w: 2.8, h: 0.9,
      fontSize: 10, color: WHITE, align: 'left', valign: 'top', margin: 0
    });
  });

  // Bottom tech note
  card(s, 0.35, 5.2, 9.3, 0.35, {});
  s.addText('Clases visuales propias: HBarChart · VBarChart · PieChart · RatingChart · SchemaDiagram  |  Inicio: java -jar graphshop.jar --dashboard', {
    x: 0.5, y: 5.2, w: 9.0, h: 0.35,
    fontSize: 9.5, color: MUTED, align: 'center', valign: 'middle', margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 13 — Funcionalidades para el Usuario Final
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 3, 'Funcionalidades');
  slideTitle(s, 'Funcionalidades para el Usuario Final', 'Interfaz de consola + dashboard visual');

  const features = [
    { n: '1', title: 'Cargar Datos',         desc: 'Inicializa el grafo con 25 usuarios, 30 productos y 35 pedidos realistas', c: BLUE },
    { n: '2', title: 'Gestión de Usuarios',  desc: 'Listar · buscar por email · detectar usuarios inactivos sin pedidos',    c: GREEN },
    { n: '3', title: 'Catálogo de Productos',desc: 'Filtrar por categoría o rango de precio · top más vendidos',             c: ORANGE },
    { n: '4', title: 'Pedidos e Historial',  desc: 'Historial completo de compras de cualquier usuario con detalle de items', c: PURPLE },
    { n: '5', title: 'Recomendaciones',      desc: 'Filtrado colaborativo (4 saltos) + Jaccard + products vistos no comprados', c: RED },
    { n: '6', title: 'Reportes y Analítica', desc: 'Ingresos por categoría · top clientes · calificaciones · estadísticas',  c: BLUE },
    { n: '7', title: 'Consultas Avanzadas',  desc: 'Demos interactivas de los algoritmos de grafo con Cypher explicado',     c: GREEN },
    { n: '8', title: 'Dashboard Visual',     desc: 'Abre la ventana Swing con 6 pestañas de gráficos en tiempo real',        c: ORANGE },
  ];

  features.forEach((f, i) => {
    const col = i % 2;
    const row = Math.floor(i / 2);
    const x = 0.35 + col * 4.85;
    const y = 1.28 + row * 1.03;
    card(s, x, y, 4.62, 0.88, { border: f.c });
    s.addShape(pres.shapes.OVAL, {
      x: x + 0.12, y: y + 0.17, w: 0.52, h: 0.52,
      fill: { color: f.c, transparency: 75 }, line: { color: f.c, width: 1 }
    });
    s.addText(f.n, { x: x + 0.12, y: y + 0.17, w: 0.52, h: 0.52, fontSize: 13, bold: true, color: f.c, align: 'center', valign: 'middle', margin: 0 });
    s.addText(f.title, { x: x + 0.78, y: y + 0.05, w: 3.7, h: 0.3, fontSize: 12, bold: true, color: f.c, align: 'left', margin: 0 });
    s.addText(f.desc,  { x: x + 0.78, y: y + 0.36, w: 3.7, h: 0.45, fontSize: 9.5, color: WHITE, align: 'left', margin: 0 });
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 14 — Uso de IA
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 3, 'Uso de IA');
  slideTitle(s, 'Uso de IA en el Desarrollo', 'Asistencia inteligente en cada etapa del proyecto');

  // Bar chart of AI usage
  const aiUsage = [
    { area: 'Consultas Cypher',      pct: 45, c: BLUE   },
    { area: 'Datos de Muestra',      pct: 30, c: GREEN  },
    { area: 'Estructura del Código', pct: 15, c: ORANGE },
    { area: 'Dashboard Swing',       pct: 10, c: PURPLE },
  ];

  const chartData = [{
    name: 'Uso de IA (%)',
    labels: aiUsage.map(a => a.area),
    values: aiUsage.map(a => a.pct),
  }];

  s.addChart(pres.charts.BAR, chartData, {
    x: 0.35, y: 1.3, w: 4.6, h: 3.3,
    barDir: 'bar',
    chartColors: [BLUE, GREEN, ORANGE, PURPLE],
    chartArea: { fill: { color: CARD }, roundedCorners: false },
    plotArea: { fill: { color: CARD } },
    catAxisLabelColor: WHITE,
    valAxisLabelColor: MUTED,
    valGridLine: { color: BORDER, size: 0.5 },
    catGridLine: { style: 'none' },
    showValue: true,
    dataLabelColor: WHITE,
    dataLabelPosition: 'outEnd',
    showLegend: false,
    valAxisMaxVal: 50,
  });

  // Detail cards
  const details = [
    { area: 'Consultas Cypher (45%)',      detail: 'Generación de las 10 consultas · optimización de traversal · algoritmos Jaccard y colaborativo', c: BLUE },
    { area: 'Datos de Muestra (30%)',      detail: 'Generación de 25 usuarios argentinos · 30 productos realistas · 35 pedidos con relaciones', c: GREEN },
    { area: 'Código Java (15%)',           detail: 'Estructura de servicios y repositorios · patrón Singleton · DataLoader', c: ORANGE },
    { area: 'Dashboard Swing (10%)',       detail: 'Diseño de componentes visuales · HBarChart · PieChart · RatingChart', c: PURPLE },
  ];

  details.forEach((d, i) => {
    const y = 1.3 + i * 0.82;
    card(s, 5.2, y, 4.45, 0.7, { border: d.c });
    s.addShape(pres.shapes.RECTANGLE, { x: 5.2, y, w: 0.05, h: 0.7, fill: { color: d.c }, line: { color: d.c, width: 0 } });
    s.addText(d.area, { x: 5.38, y: y + 0.03, w: 4.15, h: 0.28, fontSize: 11, bold: true, color: d.c, align: 'left', margin: 0 });
    s.addText(d.detail, { x: 5.38, y: y + 0.32, w: 4.15, h: 0.32, fontSize: 9, color: WHITE, align: 'left', margin: 0 });
  });

  // Bottom note
  card(s, 0.35, 4.9, 9.3, 0.45, {});
  s.addText('Herramienta: Claude (Anthropic)  ·  Rol: asistente de código, no autor principal — todo el código fue revisado, entendido e integrado por el equipo', {
    x: 0.5, y: 4.9, w: 9.0, h: 0.45,
    fontSize: 10, color: MUTED, align: 'center', valign: 'middle', margin: 0
  });
}

// ═══════════════════════════════════════════════════════════════════════════════
// SLIDE 15 — Conclusión
// ═══════════════════════════════════════════════════════════════════════════════
{
  const s = pres.addSlide(); bg(s);
  participantBadge(s, 3, 'Conclusión');

  // Title
  s.addText('Conclusión', {
    x: 0.5, y: 0.35, w: 9, h: 0.7,
    fontSize: 38, bold: true, color: BLUE, fontFace: 'Calibri', align: 'left', margin: 0
  });

  // 3 big takeaways
  const takes = [
    { icon: '◈', title: 'Neo4j es la herramienta correcta para este dominio', body: 'Un e-commerce moderno vive de relaciones complejas entre usuarios, productos y pedidos. Neo4j convierte consultas de 20+ líneas SQL en traversals de 4-5 líneas Cypher.', c: BLUE },
    { icon: '◈', title: 'Los algoritmos de grafo agregan valor real de negocio', body: 'Filtrado colaborativo y Jaccard permiten recomendaciones personalizadas y segmentación de clientes — funcionalidades que diferencian un e-commerce moderno.', c: GREEN },
    { icon: '◈', title: 'Dashboard + Consola = aplicación completa y presentable', body: 'La combinación de interfaz de consola interactiva y dashboard visual con gráficos en tiempo real demuestra el valor del almacenamiento y tratamiento de datos.', c: ORANGE },
  ];

  takes.forEach((t, i) => {
    const y = 1.3 + i * 1.35;
    card(s, 0.35, y, 9.3, 1.18, { border: t.c });
    s.addShape(pres.shapes.RECTANGLE, { x: 0.35, y, w: 0.06, h: 1.18, fill: { color: t.c }, line: { color: t.c, width: 0 } });
    s.addText(t.title, { x: 0.58, y: y + 0.08, w: 8.9, h: 0.35, fontSize: 14, bold: true, color: t.c, align: 'left', margin: 0 });
    s.addText(t.body,  { x: 0.58, y: y + 0.44, w: 8.9, h: 0.65, fontSize: 10.5, color: WHITE, align: 'left', margin: 0 });
  });

  // Footer
  s.addShape(pres.shapes.LINE, { x: 0.5, y: 5.2, w: 9, h: 0, line: { color: BORDER, width: 1 } });
  s.addText('GraphShop  ·  Bases de Datos II  ·  TP2 2025  ·  UTN FRC', {
    x: 0.5, y: 5.25, w: 9, h: 0.25,
    fontSize: 10, color: MUTED, align: 'center', margin: 0
  });
}

// ── Write ─────────────────────────────────────────────────────────────────────
pres.writeFile({ fileName: OUT }).then(() => {
  console.log('✓ Guardado en: ' + OUT);
}).catch(err => {
  console.error('✗ Error:', err.message);
  process.exit(1);
});
