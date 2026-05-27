const fs = require('fs');
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  Header, Footer, AlignmentType, HeadingLevel, BorderStyle, WidthType,
  ShadingType, VerticalAlign, PageNumber, PageBreak, LevelFormat,
  TabStopType, TabStopPosition, PositionalTab, PositionalTabAlignment,
  PositionalTabRelativeTo, PositionalTabLeader, ImageRun
} = require('C:/Users/emanu/AppData/Roaming/npm/node_modules/docx');

const OUT = 'C:/Users/emanu/IdeaProjects/BDII-TP2/informe_graphshop.docx';

// ── Constants ─────────────────────────────────────────────────────────────────
const FONT   = 'Calibri';
const COLOR_TITLE  = '1E3A5F';   // dark navy for headings
const COLOR_ACCENT = '2563EB';   // blue accent
const COLOR_MUTED  = '6B7280';   // grey for secondary text
const COLOR_WHITE  = 'FFFFFF';
const COLOR_HEADER_BG = '1E3A5F';
const COLOR_ROW_ALT   = 'EFF6FF'; // light blue for alt rows
const COLOR_PRO_BG    = 'ECFDF5'; // light green
const COLOR_CON_BG    = 'FEF2F2'; // light red

// A4 margins 2.5cm = ~1417 DXA
const MARGIN = 1417;
const PAGE_W = 11906; // A4 width DXA
const CONTENT_W = PAGE_W - MARGIN * 2; // ~9072 DXA

const cellBorder = (color = 'C7D2FE') => ({
  top:    { style: BorderStyle.SINGLE, size: 1, color },
  bottom: { style: BorderStyle.SINGLE, size: 1, color },
  left:   { style: BorderStyle.SINGLE, size: 1, color },
  right:  { style: BorderStyle.SINGLE, size: 1, color },
});

const noBorder = () => ({
  top:    { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' },
  bottom: { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' },
  left:   { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' },
  right:  { style: BorderStyle.NONE, size: 0, color: 'FFFFFF' },
});

function para(runs, opts = {}) {
  return new Paragraph({ ...opts, children: Array.isArray(runs) ? runs : [runs] });
}

function text(t, opts = {}) {
  return new TextRun({ text: t, font: FONT, ...opts });
}

function h1(content) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    children: [new TextRun({ text: content, font: FONT, size: 32, bold: true, color: COLOR_TITLE })],
    spacing: { before: 360, after: 120 },
  });
}

function h2(content) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    children: [new TextRun({ text: content, font: FONT, size: 26, bold: true, color: COLOR_ACCENT })],
    spacing: { before: 240, after: 100 },
  });
}

function bodyPara(runs, spacing = { before: 60, after: 100 }) {
  return new Paragraph({
    children: Array.isArray(runs) ? runs : [text(runs, { size: 22 })],
    spacing,
  });
}

function bullet(t) {
  return new Paragraph({
    numbering: { reference: 'bullets', level: 0 },
    children: [text(t, { size: 22 })],
    spacing: { before: 40, after: 40 },
  });
}

function spacer(pt = 120) {
  return new Paragraph({ children: [], spacing: { before: 0, after: pt } });
}

// ── Table helpers ─────────────────────────────────────────────────────────────
function headerCell(t, w, bgColor = COLOR_HEADER_BG) {
  return new TableCell({
    width: { size: w, type: WidthType.DXA },
    borders: cellBorder('1E3A5F'),
    shading: { fill: bgColor, type: ShadingType.CLEAR },
    margins: { top: 100, bottom: 100, left: 120, right: 120 },
    verticalAlign: VerticalAlign.CENTER,
    children: [new Paragraph({
      alignment: AlignmentType.CENTER,
      children: [text(t, { size: 22, bold: true, color: COLOR_WHITE })],
    })],
  });
}

function dataCell(t, w, bg = 'FFFFFF', bold = false, color = '1F2937') {
  return new TableCell({
    width: { size: w, type: WidthType.DXA },
    borders: cellBorder('C7D2FE'),
    shading: { fill: bg, type: ShadingType.CLEAR },
    margins: { top: 80, bottom: 80, left: 120, right: 120 },
    verticalAlign: VerticalAlign.CENTER,
    children: [new Paragraph({
      children: [text(t, { size: 20, bold, color })],
    })],
  });
}

// ── Build document ────────────────────────────────────────────────────────────

// === SECTION 1: Cover + TOC (no footer page number) ===
const coverChildren = [
  // Vertical spacing at top
  spacer(2880),

  // University logo + subject header
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [new ImageRun({
      type: 'png',
      data: fs.readFileSync('C:/Users/emanu/IdeaProjects/BDII-TP2/utn_logo_big.png'),
      transformation: { width: 320, height: 62 },
      altText: { title: 'UTN Logo', description: 'Universidad Tecnológica Nacional', name: 'UTN' }
    })],
    spacing: { before: 0, after: 60 },
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [text('Facultad Regional Córdoba', { size: 26, bold: true, color: COLOR_TITLE })],
    spacing: { before: 0, after: 60 },
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [text('Bases de Datos II  ·  Trabajo Práctico N°2', { size: 24, color: COLOR_MUTED })],
    spacing: { before: 0, after: 600 },
  }),

  // Main title
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [text('GraphShop', { size: 64, bold: true, color: COLOR_ACCENT })],
    spacing: { before: 0, after: 120 },
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [text('Sistema de E-Commerce sobre Neo4j', { size: 32, color: COLOR_TITLE })],
    spacing: { before: 0, after: 800 },
  }),

  // Year
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [text('2025', { size: 26, color: COLOR_MUTED })],
    spacing: { before: 0, after: 600 },
  }),

  // Team members
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [text('Integrantes del grupo:', { size: 22, bold: true, color: COLOR_TITLE })],
    spacing: { before: 0, after: 80 },
  }),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [text('Amarilla, Franco  ·  Cortez, Emanuel  ·  Carreggio, Juan', { size: 22, color: '374151' })],
    spacing: { before: 0, after: 80 },
  }),

  // Page break after cover
  new Paragraph({ children: [new PageBreak()] }),
];

// === TOC (manual) ===
function tocLine(label, page) {
  return new Paragraph({
    children: [
      text(label, { size: 22, color: '374151' }),
      new TextRun({
        children: [
          new PositionalTab({
            alignment: PositionalTabAlignment.RIGHT,
            relativeTo: PositionalTabRelativeTo.MARGIN,
            leader: PositionalTabLeader.DOT,
          }),
          String(page),
        ],
        font: FONT,
        size: 22,
        color: '374151',
      }),
    ],
    spacing: { before: 60, after: 60 },
  });
}

function tocLine2(label, page) {
  return new Paragraph({
    children: [
      text('      ' + label, { size: 20, color: COLOR_MUTED }),
      new TextRun({
        children: [
          new PositionalTab({
            alignment: PositionalTabAlignment.RIGHT,
            relativeTo: PositionalTabRelativeTo.MARGIN,
            leader: PositionalTabLeader.DOT,
          }),
          String(page),
        ],
        font: FONT,
        size: 20,
        color: COLOR_MUTED,
      }),
    ],
    spacing: { before: 40, after: 40 },
  });
}

const tocChildren = [
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [text('Índice', { size: 32, bold: true, color: COLOR_TITLE })],
    spacing: { before: 0, after: 400 },
  }),
  tocLine('1. Introducción', 3),
  tocLine('2. Desarrollo', 3),
  tocLine2('2.1 Descripción y Análisis del Problema', 3),
  tocLine2('2.2 Diseño de la Base de Datos', 3),
  tocLine2('2.3 Algoritmos de Grafo Complejos', 4),
  tocLine2('2.4 Tecnología Utilizada', 4),
  tocLine2('2.5 Funcionalidades para el Usuario Final', 5),
  tocLine2('2.6 Ventajas y Desventajas de Neo4j', 5),
  tocLine2('2.7 Uso de la IA en el Desarrollo', 6),
  tocLine('3. Conclusión', 6),
  new Paragraph({ children: [new PageBreak()] }),
];

// === SECTION 2: Content (with page numbers) ===
const contentChildren = [

  // ── 1. Introducción ──────────────────────────────────────────────────────────
  h1('1. Introducción'),

  bodyPara([
    text('El comercio electrónico moderno no solo almacena datos: ', { size: 22 }),
    text('navega relaciones complejas entre entidades', { size: 22, bold: true }),
    text('. Preguntas como "¿qué compraron usuarios similares a mí?" o "¿qué productos vio un cliente sin comprar?" involucran múltiples entidades relacionadas y son costosas en bases de datos relacionales tradicionales.', { size: 22 }),
  ]),

  bodyPara([
    text('El presente trabajo implementa ', { size: 22 }),
    text('GraphShop', { size: 22, bold: true, color: COLOR_ACCENT }),
    text(', un sistema de e-commerce construido sobre Neo4j — una base de datos de grafos. La aplicación ofrece una interfaz de consola interactiva con 8 módulos y un dashboard visual desarrollado en Java Swing con 6 pestañas de analítica en tiempo real. El stack tecnológico es Java 17, Neo4j Community 5.x y Apache Maven.', { size: 22 }),
  ]),

  spacer(100),

  // ── 2. Desarrollo ─────────────────────────────────────────────────────────────
  h1('2. Desarrollo'),

  // 2.1
  h2('2.1  Descripción y Análisis del Problema'),

  bodyPara('Un sistema de e-commerce moderno debe responder eficientemente preguntas que involucran múltiples relaciones entre sus entidades:', { before: 60, after: 80 }),

  bullet('¿Qué otros productos compraron los usuarios que compraron lo mismo que yo? (Filtrado Colaborativo)'),
  bullet('¿Qué tan similares son dos usuarios en base a su historial de compras? (Similitud de Jaccard)'),
  bullet('¿Qué productos vio un usuario pero nunca compró? (Remarketing / Abandono de carrito)'),

  spacer(80),

  bodyPara([
    text('En un modelo relacional (SQL), cada una de estas preguntas requiere múltiples ', { size: 22 }),
    text('JOIN', { size: 22, bold: true, font: 'Consolas' }),
    text(' anidados, subconsultas correlacionadas y rendimiento degradado a medida que crecen los datos (complejidad O(n×m) por JOIN adicional). En Neo4j, estas mismas consultas son ', { size: 22 }),
    text('traversals nativos de 2 a 4 saltos', { size: 22, bold: true }),
    text(' que se expresan en pocas líneas de Cypher y escalan eficientemente.', { size: 22 }),
  ]),

  spacer(80),

  // 2.2
  h2('2.2  Diseño de la Base de Datos'),

  bodyPara([
    text('El esquema del grafo define ', { size: 22 }),
    text('4 tipos de nodos', { size: 22, bold: true }),
    text(' y ', { size: 22 }),
    text('5 tipos de relaciones', { size: 22, bold: true }),
    text(':', { size: 22 }),
  ]),

  spacer(60),

  // Nodes table
  new Table({
    width: { size: CONTENT_W, type: WidthType.DXA },
    columnWidths: [2200, 1700, 1400, 3772],
    rows: [
      new TableRow({ children: [
        headerCell('Nodo', 2200),
        headerCell('Instancias', 1700),
        headerCell('Propiedades clave', 1400),
        headerCell('Descripción', 3772),
      ]}),
      new TableRow({ children: [
        dataCell(':User', 2200, COLOR_ROW_ALT, true, COLOR_ACCENT),
        dataCell('25', 1700, COLOR_ROW_ALT),
        dataCell('id, nombre, email, ciudad, puntosFidelidad', 1400, COLOR_ROW_ALT),
        dataCell('Usuarios registrados en la plataforma', 3772, COLOR_ROW_ALT),
      ]}),
      new TableRow({ children: [
        dataCell(':Product', 2200, 'FFFFFF', true, COLOR_ACCENT),
        dataCell('30', 1700, 'FFFFFF'),
        dataCell('id, nombre, precio, stock, marca', 1400, 'FFFFFF'),
        dataCell('Productos del catálogo con precio en ARS', 3772, 'FFFFFF'),
      ]}),
      new TableRow({ children: [
        dataCell(':Order', 2200, COLOR_ROW_ALT, true, COLOR_ACCENT),
        dataCell('35', 1700, COLOR_ROW_ALT),
        dataCell('id, fecha, estado, montoTotal, metodoPago', 1400, COLOR_ROW_ALT),
        dataCell('Pedidos: COMPLETADO | PENDIENTE | ENVIADO | CANCELADO', 3772, COLOR_ROW_ALT),
      ]}),
      new TableRow({ children: [
        dataCell(':Category', 2200, 'FFFFFF', true, COLOR_ACCENT),
        dataCell('5', 1700, 'FFFFFF'),
        dataCell('id, nombre, descripcion', 1400, 'FFFFFF'),
        dataCell('Electrónica, Moda, Hogar, Deportes, Libros', 3772, 'FFFFFF'),
      ]}),
    ],
  }),

  spacer(120),

  bodyPara([
    text('Las ', { size: 22 }),
    text('5 relaciones', { size: 22, bold: true }),
    text(' del grafo son: (User)-[:REALIZO]->(Order), (Order)-[:CONTIENE {cantidad, precioUnitario}]->(Product), (Product)-[:PERTENECE_A]->(Category), (User)-[:VISTO {timestamp, duracionSegundos}]->(Product) y (User)-[:RESENO {calificacion, comentario}]->(Product). El grafo total contiene ~176 relaciones.', { size: 22, font: 'Calibri' }),
  ]),

  bodyPara([
    text('Se definieron 5 constraints de unicidad (uno por nodo) y un índice sobre el email del usuario para garantizar búsquedas O(1). Los constraints son idempotentes (', { size: 22 }),
    text('IF NOT EXISTS', { size: 22, bold: true, font: 'Consolas' }),
    text(') y seguros para re-ejecutar en cada inicio de la aplicación.', { size: 22 }),
  ]),

  spacer(100),

  // 2.3
  h2('2.3  Algoritmos de Grafo Complejos'),

  bodyPara([
    text('Se implementaron dos algoritmos de grafo que demuestran la potencia de Neo4j sobre SQL:', { size: 22 }),
  ]),

  bodyPara([
    text('Filtrado Colaborativo (4 saltos): ', { size: 22, bold: true }),
    text('Dado un usuario objetivo, el algoritmo (1) recolecta sus productos comprados, (2) encuentra usuarios que compraron al menos uno de esos productos, (3) los ordena por productos en común, y (4) recomienda los productos que compraron esos usuarios similares pero que el objetivo aún no tiene. El equivalente SQL requeriría 4+ JOINs y 2 subconsultas correlacionadas (20+ líneas).', { size: 22 }),
  ]),

  bodyPara([
    text('Similitud de Jaccard: ', { size: 22, bold: true }),
    text('Calcula cuán similares son dos usuarios usando la fórmula Jaccard(A, B) = |A ∩ B| / |A ∪ B|, donde A y B son conjuntos de productos comprados. Un valor de 1.0 indica perfiles idénticos; 0.0, sin coincidencias. En Neo4j, las operaciones de conjuntos sobre listas de nodos son nativas en Cypher; en SQL requeriría window functions avanzadas.', { size: 22 }),
  ]),

  spacer(100),

  // 2.4
  h2('2.4  Tecnología Utilizada'),

  spacer(60),

  new Table({
    width: { size: CONTENT_W, type: WidthType.DXA },
    columnWidths: [3000, 4000, 2072],
    rows: [
      new TableRow({ children: [
        headerCell('Componente', 3000),
        headerCell('Tecnología', 4000),
        headerCell('Versión', 2072),
      ]}),
      ...[
        ['Base de datos',        'Neo4j Community Edition',     '5.x'],
        ['Lenguaje',             'Java',                        '17'],
        ['Driver de BD',         'Neo4j Java Driver',           '5.18.0'],
        ['Interfaz de consola',  'Menú interactivo Java (stdin)','—'],
        ['Interfaz visual',      'Java Swing + AWT',            'JDK 17'],
        ['Build',                'Apache Maven',                '3.x'],
        ['Protocolo de BD',      'Bolt (bolt://localhost:7687)', '—'],
        ['Empaquetado',          'Maven Assembly Plugin (Fat JAR)', '—'],
      ].map(([c, t, v], i) => new TableRow({ children: [
        dataCell(c, 3000, i % 2 === 0 ? COLOR_ROW_ALT : 'FFFFFF', true, '374151'),
        dataCell(t, 4000, i % 2 === 0 ? COLOR_ROW_ALT : 'FFFFFF'),
        dataCell(v, 2072, i % 2 === 0 ? COLOR_ROW_ALT : 'FFFFFF'),
      ]})),
    ],
  }),

  spacer(100),

  // 2.5
  h2('2.5  Funcionalidades para el Usuario Final'),

  bodyPara('La aplicación ofrece dos interfaces complementarias:', { before: 60, after: 80 }),

  bodyPara([
    text('Consola interactiva (8 módulos): ', { size: 22, bold: true }),
    text('(1) Carga de datos de muestra, (2) Gestión de usuarios, (3) Catálogo de productos con filtros, (4) Pedidos e historial, (5) Recomendaciones por algoritmos de grafo, (6) Reportes y analítica, (7) Consultas avanzadas demostrativas, (8) Apertura del dashboard visual.', { size: 22 }),
  ]),

  bodyPara([
    text('Dashboard visual Swing (6 pestañas): ', { size: 22, bold: true }),
    text('(1) Ingresos por categoría con barras y gráfico de torta, (2) Top 10 clientes por gasto con ranking, (3) Calificaciones de productos con semáforo visual verde/ámbar/rojo, (4) Actividad del sistema con distribución de pedidos por estado, (5) Métodos de pago con KPI cards, (6) Estadísticas generales con 7 KPI cards y el diagrama del esquema del grafo dibujado con Java2D.', { size: 22 }),
  ]),

  spacer(100),

  // 2.6
  h2('2.6  Ventajas y Desventajas de Neo4j'),

  spacer(60),

  new Table({
    width: { size: CONTENT_W, type: WidthType.DXA },
    columnWidths: [Math.floor(CONTENT_W / 2), Math.ceil(CONTENT_W / 2)],
    rows: [
      new TableRow({ children: [
        headerCell('Ventajas', Math.floor(CONTENT_W / 2), '166534'),
        headerCell('Desventajas', Math.ceil(CONTENT_W / 2), '991B1B'),
      ]}),
      new TableRow({ children: [
        new TableCell({
          width: { size: Math.floor(CONTENT_W / 2), type: WidthType.DXA },
          borders: cellBorder('86EFAC'),
          shading: { fill: COLOR_PRO_BG, type: ShadingType.CLEAR },
          margins: { top: 80, bottom: 80, left: 120, right: 120 },
          children: [
            ...[
              'Traversal nativo O(1) por salto (vs JOIN O(n×m))',
              'Consultas de red complejas en pocas líneas de Cypher',
              'Relaciones con propiedades → sin tablas puente artificiales',
              'Esquema flexible: agregar propiedades sin ALTER TABLE',
              'Modelado intuitivo: el grafo refleja el dominio real',
              'Neo4j Browser: visualización interactiva del grafo incluida',
            ].map(t => new Paragraph({
              numbering: { reference: 'bullets', level: 0 },
              children: [new TextRun({ text: t, font: FONT, size: 20, color: '166534' })],
              spacing: { before: 30, after: 30 },
            })),
          ],
        }),
        new TableCell({
          width: { size: Math.ceil(CONTENT_W / 2), type: WidthType.DXA },
          borders: cellBorder('FCA5A5'),
          shading: { fill: COLOR_CON_BG, type: ShadingType.CLEAR },
          margins: { top: 80, bottom: 80, left: 120, right: 120 },
          children: [
            ...[
              'Community Edition: sin clustering ni backups automáticos',
              'Menor ecosistema vs SQL (BI, ORM maduros)',
              'Curva de aprendizaje de Cypher para equipos SQL',
              'No ideal para datos altamente tabulares (ERP, contabilidad)',
              'Menor rendimiento en scans completos de la “tabla”',
              'vs MongoDB: Neo4j supera en red; Mongo en documentos anidados',
            ].map(t => new Paragraph({
              numbering: { reference: 'bullets2', level: 0 },
              children: [new TextRun({ text: t, font: FONT, size: 20, color: '991B1B' })],
              spacing: { before: 30, after: 30 },
            })),
          ],
        }),
      ]}),
    ],
  }),

  spacer(100),

  // 2.7
  h2('2.7  Uso de la IA en el Desarrollo'),

  bodyPara([
    text('Se utilizó ', { size: 22 }),
    text('Claude (Anthropic)', { size: 22, bold: true }),
    text(' como asistente de código en cuatro etapas del desarrollo:', { size: 22 }),
  ]),

  spacer(60),

  new Table({
    width: { size: CONTENT_W, type: WidthType.DXA },
    columnWidths: [3200, 1500, 4372],
    rows: [
      new TableRow({ children: [
        headerCell('Área', 3200),
        headerCell('Proporción', 1500),
        headerCell('Detalle', 4372),
      ]}),
      ...[
        ['Consultas Cypher',         '~45 %', 'Generación y optimización de las 10 consultas, algoritmo de filtrado colaborativo y similitud Jaccard'],
        ['Datos de muestra',         '~30 %', '25 usuarios argentinos, 30 productos con marcas/precios ARS, 35 pedidos con estados y métodos de pago realistas'],
        ['Estructura del código Java', '~15 %', 'Servicios, repositorios, patrón Singleton para Neo4jConfig, DataLoader con carga en orden de dependencias'],
        ['Dashboard Swing',          '~10 %', 'Diseño de componentes gráficos HBarChart, VBarChart, PieChart, RatingChart y SchemaDiagram'],
      ].map(([area, pct, det], i) => new TableRow({ children: [
        dataCell(area, 3200, i % 2 === 0 ? COLOR_ROW_ALT : 'FFFFFF', true, '374151'),
        dataCell(pct,  1500, i % 2 === 0 ? COLOR_ROW_ALT : 'FFFFFF', true, COLOR_ACCENT),
        dataCell(det,  4372, i % 2 === 0 ? COLOR_ROW_ALT : 'FFFFFF'),
      ]})),
    ],
  }),

  spacer(100),

  bodyPara([
    text('Todo el código generado fue ', { size: 22 }),
    text('revisado, comprendido e integrado por el equipo', { size: 22, bold: true }),
    text('. La IA actuó como asistente técnico, acelerando tareas repetitivas (datos de muestra, boilerplate de repositorios) y sugiriendo consultas Cypher que luego fueron validadas manualmente en Neo4j Browser. La arquitectura, el diseño del esquema y las decisiones de negocio fueron responsabilidad del equipo.', { size: 22 }),
  ]),

  spacer(100),

  // ── 3. Conclusión ────────────────────────────────────────────────────────────
  h1('3. Conclusión'),

  bodyPara([
    text('Neo4j demostró ser la elección correcta para el dominio de e-commerce con relaciones complejas. Los ', { size: 22 }),
    text('traversals nativos del grafo', { size: 22, bold: true }),
    text(' convirtieron consultas de 20+ líneas SQL en expresiones concisas de Cypher, y los algoritmos de filtrado colaborativo y similitud Jaccard aportaron valor real de negocio (recomendaciones personalizadas, segmentación de clientes).', { size: 22 }),
  ]),

  bodyPara('La combinación de interfaz de consola interactiva y dashboard visual Swing resultó en una aplicación completa, funcional y demostrable que pone en valor el almacenamiento y tratamiento de datos sobre un grafo.'),

  bodyPara([
    text('Lección clave: ', { size: 22, bold: true }),
    text('la base de datos debe elegirse según el tipo de consulta dominante del sistema. Neo4j es la elección correcta para redes sociales, recomendaciones, detección de fraude y rutas; SQL relacional sigue siendo superior para ERP, facturación e inventario simple donde predominan consultas tabulares masivas.', { size: 22 }),
  ]),
];

// ── Assemble ──────────────────────────────────────────────────────────────────
const doc = new Document({
  numbering: {
    config: [
      {
        reference: 'bullets',
        levels: [{
          level: 0, format: LevelFormat.BULLET, text: '•',
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 480, hanging: 240 } } },
        }],
      },
      {
        reference: 'bullets2',
        levels: [{
          level: 0, format: LevelFormat.BULLET, text: '•',
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 480, hanging: 240 } } },
        }],
      },
    ],
  },
  styles: {
    default: {
      document: { run: { font: FONT, size: 22 } },
    },
    paragraphStyles: [
      {
        id: 'Heading1', name: 'Heading 1',
        basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { size: 32, bold: true, font: FONT, color: COLOR_TITLE },
        paragraph: { spacing: { before: 360, after: 120 }, outlineLevel: 0 },
      },
      {
        id: 'Heading2', name: 'Heading 2',
        basedOn: 'Normal', next: 'Normal', quickFormat: true,
        run: { size: 26, bold: true, font: FONT, color: COLOR_ACCENT },
        paragraph: { spacing: { before: 240, after: 100 }, outlineLevel: 1 },
      },
    ],
  },
  sections: [
    // Cover + TOC (no page numbers)
    {
      properties: {
        page: {
          size: { width: 11906, height: 16838 },
          margin: { top: MARGIN, right: MARGIN, bottom: MARGIN, left: MARGIN },
        },
      },
      footers: {
        default: new Footer({ children: [new Paragraph({ children: [] })] }),
      },
      children: [...coverChildren, ...tocChildren],
    },
    // Content (with page numbers)
    {
      properties: {
        page: {
          size: { width: 11906, height: 16838 },
          margin: { top: MARGIN, right: MARGIN, bottom: MARGIN + 200, left: MARGIN },
        },
      },
      footers: {
        default: new Footer({
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [
              text('GraphShop  ·  Bases de Datos II TP2  ·  UTN FRC  ·  ', { size: 18, color: COLOR_MUTED }),
              new TextRun({ children: [PageNumber.CURRENT], font: FONT, size: 18, color: COLOR_ACCENT }),
            ],
          })],
        }),
      },
      children: contentChildren,
    },
  ],
});

Packer.toBuffer(doc).then(buf => {
  fs.writeFileSync(OUT, buf);
  console.log('✓ Guardado en: ' + OUT);
}).catch(err => {
  console.error('✗ Error:', err.message);
  process.exit(1);
});
