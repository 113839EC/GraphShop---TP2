# GraphShop — Guía de Instalación y Ejecución

Guía paso a paso para instalar, compilar y ejecutar el proyecto desde cero.

---

## Requisitos

| Herramienta | Versión mínima | Verificar con |
|---|---|---|
| Java JDK | 17 | `java -version` |
| Apache Maven | 3.6 | `mvn -version` |
| Neo4j Community | 5.x | — |

---

## 1. Instalar Neo4j

### Opción A — Instalación portable (recomendada, sin instalador)

1. Descargar **Neo4j Community Edition** desde [neo4j.com/download](https://neo4j.com/download-center/#community)
2. Descomprimir en una carpeta fija, por ejemplo:
   ```
   C:\neo4j-community\
   ```
3. Dentro de esa carpeta quedará la estructura:
   ```
   C:\neo4j-community\
   ├── bin\
   │   └── neo4j.bat
   ├── conf\
   │   └── neo4j.conf
   ├── data\
   └── plugins\
   ```

### Configurar contraseña inicial

La primera vez que se inicia Neo4j, pide cambiar la contraseña. El proyecto usa `password` como contraseña.

Para configurarla sin interfaz web, editar `conf\neo4j.conf` y agregar:

```properties
# Deshabilita autenticación inicial obligatoria (solo para desarrollo)
dbms.security.auth_enabled=true
```

Luego iniciar Neo4j (ver paso 2) y cambiar la contraseña via browser o con:

```
http://localhost:7474  →  usuario: neo4j  →  contraseña nueva: password
```

---

## 2. Iniciar Neo4j

Abrir una terminal y ejecutar:

```cmd
cd C:\neo4j-community\bin
neo4j.bat console
```

Esperar hasta ver el mensaje:

```
INFO  Remote interface available at http://localhost:7474/
INFO  Bolt enabled on localhost:7687.
INFO  Started.
```

> Neo4j debe quedar corriendo en esta terminal durante toda la sesión. **No cerrarla.**

### Verificar que está activo

Abrir en el navegador:
```
http://localhost:7474
```
Debe mostrar el **Neo4j Browser** con un formulario de login.

---

## 3. Clonar / abrir el proyecto

Si ya tenés el proyecto descargado, ir a su carpeta raíz:

```cmd
cd C:\Users\emanu\IdeaProjects\BDII-TP2
```

La estructura del proyecto es:

```
BDII-TP2\
├── pom.xml
├── README.md
├── SETUP.md
└── src\
    └── main\
        └── java\
            └── com\ecommerce\
                ├── Main.java
                ├── config\
                ├── model\
                ├── loader\
                ├── repository\
                ├── service\
                ├── report\
                └── ui\
```

---

## 4. Compilar el proyecto

Desde la carpeta raíz (`BDII-TP2\`), ejecutar:

```cmd
mvn clean package -q
```

Esto descarga las dependencias, compila y genera el fat JAR con todo incluido:

```
target\graphshop.jar
```

> La flag `-q` silencia el output verboso de Maven. Quitarla si hay errores y se necesita ver el detalle.

### Verificar que compiló correctamente

```cmd
dir target\graphshop.jar
```

Debe existir y pesar aproximadamente **8–10 MB** (incluye el driver de Neo4j).

---

## 5. Ejecutar la aplicación

### Modo consola interactiva

```cmd
java -jar target\graphshop.jar
```

Al iniciar, aparece el banner de GraphShop y el menú principal:

```
  ════════════════════════════════════════
  GRAPHSHOP  │  Sistema de E-Commerce sobre Neo4j
  BDII  |  Trabajo Práctico 2
  ════════════════════════════════════════
  1. Cargar datos de muestra
  2. Gestión de Usuarios
  3. Gestión de Productos
  4. Pedidos e Historial
  5. Recomendaciones (Graph Traversal)
  6. Reportes y Analítica
  7. Consultas Avanzadas sobre el Grafo
  8. Dashboard Visual (ventana gráfica)
  0. Salir
```

### Modo dashboard visual (ventana gráfica)

```cmd
javaw -jar target\graphshop.jar --dashboard
```

Abre directamente la ventana con los 6 paneles de analítica sin pasar por el menú de consola.

> Usar `javaw` (no `java`) para que la ventana Swing quede activa correctamente en Windows.

---

## 6. Cargar los datos de muestra

**Este paso es obligatorio antes de usar cualquier consulta.**

1. Iniciar la app en modo consola
2. Seleccionar opción **1**
3. Confirmar con **s**

El proceso carga:
- 5 categorías
- 30 productos
- 25 usuarios
- 35 pedidos con sus ítems
- 26 relaciones de visitas (`VISTO`)
- 25 reseñas (`RESENO`)

Output esperado:
```
  ✓ Datos eliminados
  ✓ Constraints e índices creados
  ✓ 5 categorías creadas
  ✓ 30 productos creados
  ✓ 25 usuarios creados
  ✓ 35 pedidos creados
  ✓ Vistas creadas
  ✓ Reseñas creadas
  ✓ Carga completa
```

> Si se ejecuta nuevamente, borra y recrea todos los datos desde cero.

---

## 7. Navegar el menú

### Opción 2 — Usuarios
```
1. Listar todos los usuarios
2. Buscar por email
3. Usuarios inactivos (sin pedidos)
```

### Opción 3 — Productos
```
1. Todos los productos con categoría     [Consulta 5]
2. Filtrar por categoría
3. Filtrar por rango de precio
4. Top productos más vendidos            [Consulta 7]
5. Quién vio pero no compró             [Consulta 8]
```

### Opción 4 — Pedidos
```
1. Historial de compras de un usuario   [Consulta 9]
```
Pide ingresar el ID del usuario (ej: `U001`, `U002`, ... `U025`).

### Opción 5 — Recomendaciones
```
1. Filtrado colaborativo                [Algoritmo complejo 1]
2. Usuarios similares (Jaccard)         [Algoritmo complejo 2]
3. Productos vistos no comprados
```
Pide ingresar el ID del usuario.

### Opción 6 — Reportes
```
1. Ingresos por categoría
2. Top clientes por gasto
3. Calificaciones por producto
4. Estadísticas generales
```

### Opción 7 — Consultas avanzadas
Muestra demostraciones de los algoritmos de grafo con usuarios precargados (`U001`, `U010`) y un resumen de las 10 consultas Cypher implementadas.

### Opción 8 — Dashboard visual
Abre la ventana gráfica con 6 pestañas de analítica (equivalente a correr con `--dashboard`).

---

## 8. Ver el grafo en Neo4j Browser

Con Neo4j corriendo, abrir en el navegador:

```
http://localhost:7474
```

**Login:** usuario `neo4j`, contraseña `password`

Una vez conectado, ejecutar en el editor de Cypher:

```cypher
MATCH (n)-[r]->(m) RETURN n, r, m LIMIT 150
```

Esto muestra el grafo completo con todos los nodos y relaciones en español:
- `REALIZO`, `CONTIENE`, `PERTENECE_A`, `VISTO`, `RESENO`

Para ver solo el esquema sin datos:

```cypher
CALL db.schema.visualization()
```

---

## 9. Configuración de conexión

Por defecto la aplicación se conecta con:

| Parámetro | Valor por defecto |
|---|---|
| URI | `bolt://localhost:7687` |
| Usuario | `neo4j` |
| Contraseña | `password` |

Para usar credenciales distintas, definir variables de entorno **antes** de ejecutar:

**Windows CMD:**
```cmd
set NEO4J_URI=bolt://localhost:7687
set NEO4J_USER=neo4j
set NEO4J_PASSWORD=miPassword
java -jar target\graphshop.jar
```

**Windows PowerShell:**
```powershell
$env:NEO4J_URI      = "bolt://localhost:7687"
$env:NEO4J_USER     = "neo4j"
$env:NEO4J_PASSWORD = "miPassword"
java -jar target\graphshop.jar
```

---

## 10. Solución de problemas comunes

### "No se pudo conectar a Neo4j"

- Verificar que Neo4j está corriendo (`neo4j.bat console` en otra terminal)
- Confirmar que el puerto 7687 está activo:
  ```cmd
  netstat -an | findstr 7687
  ```
  Debe aparecer `LISTENING`.

### "Authentication failure"

La contraseña configurada en la app no coincide con la de Neo4j. Cambiarla en el browser (`http://localhost:7474`) o ajustar la variable de entorno `NEO4J_PASSWORD`.

### El dashboard no abre

Asegurarse de usar `javaw` (no `java`) para el modo `--dashboard`:
```cmd
javaw -jar target\graphshop.jar --dashboard
```

### Los datos no aparecen en las consultas

Ejecutar primero la **opción 1** del menú para cargar los datos de muestra.

### Maven no encuentra Java 17

Verificar que `JAVA_HOME` apunta a un JDK 17:
```cmd
echo %JAVA_HOME%
java -version
```

Si es incorrecto, configurar:
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

---

## Resumen rápido

```
1.  Iniciar Neo4j        →  neo4j.bat console
2.  Compilar             →  mvn clean package -q
3a. Consola interactiva  →  java  -jar target\graphshop.jar
3b. Dashboard visual     →  javaw -jar target\graphshop.jar --dashboard
4.  Cargar datos         →  Opción 1 del menú → confirmar con 's'
5.  Ver grafo            →  http://localhost:7474  →  MATCH (n)-[r]->(m) RETURN n,r,m LIMIT 150
```
