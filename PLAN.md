# PLAN DE IMPLEMENTACIÓN — ViralSim
> Guía completa para agente de IA. Leer completo antes de ejecutar cualquier fase.

---

## CONTEXTO DEL PROYECTO

**ViralSim** es un simulador de propagación de rumores en redes sociales.
- Red de **250 nodos** con topología **Watts-Strogatz** (`n=250, k=6, p=0.1`)
- **3 modelos de propagación**: Viral, Cascada Independiente, Threshold
- **4 estados de nodo**: NO_INFORMADO → INFORMADO_ACTIVO → INFORMADO_PASIVO → RESISTENTE (transición unidireccional)
- Stack: **Java 25 + Spring Boot 3.5 + MySQL 8.4 (Docker puerto 3307) + HTML/CSS/JS + vis.js**

---

## ESTADO ACTUAL (ya implementado — NO recrear)

### Backend completado
- **Paquete base**: `com.viralsim`
- **Entidades JPA** en `com.viralsim.models` (9 clases):
  - `Grafo`, `Nodo`, `Arista` — representan la red
  - `EstadoCatalogo` — tabla `Estado` con ids 0,1,2,3
  - `ModeloPropagacion` — tabla con 3 modelos seed
  - `Simulacion`, `ConfiguracionSimulacion`, `PasoSimulacion`, `NodoSimulacion` — historial
- **Repositorios** en `com.viralsim.repositories` (9 interfaces JpaRepository)
- **Servicios** en `com.viralsim.services` (7 clases)
- **Controllers REST** en `com.viralsim.controllers` (7 clases):
  - `GET/POST /api/grafos`
  - `GET/POST /api/nodos`
  - `GET/POST /api/aristas`
  - `GET/POST /api/simulaciones`
  - `GET/POST /api/configuraciones`
  - `GET/POST /api/pasos`
  - `GET/POST /api/nodo-simulacion`
- **GlobalExceptionHandler** en `com.viralsim.api` — devuelve 404 con `{"error": "mensaje"}` en vez de 500
- **data.sql** en `src/main/resources/` — inserta automáticamente los 4 estados y 3 modelos con `INSERT IGNORE`
- **application.properties** configurado con `spring.sql.init.mode=always` y `spring.jpa.defer-datasource-initialization=true`

### Naming strategy crítica
```properties
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
```
Las tablas son PascalCase (`Grafo`, `Nodo`, `Arista`...). Sin esta estrategia Hibernate las convierte a minúsculas y falla.

### IDs de EstadoCatalogo en BD
| id | nombre |
|----|--------|
| 0 | NO_INFORMADO |
| 1 | INFORMADO_ACTIVO |
| 2 | INFORMADO_PASIVO |
| 3 | RESISTENTE |

### IDs de ModeloPropagacion en BD (seed)
| id | nombre |
|----|--------|
| 1 | Modelo Viral |
| 2 | Cascada Independiente |
| 3 | Modelo de Umbral |

---

## FASE 1 — Generador Watts-Strogatz

**Paquete**: `com.viralsim.utils`  
**Archivo**: `WattsStrogatzGenerator.java`

### Qué hace
Genera una red de 250 nodos con topología de mundo pequeño y la persiste en MySQL usando los repositorios existentes.

### Parámetros fijos
- `n = 250` nodos
- `k = 6` vecinos iniciales por nodo (anillo regular)
- `p = 0.1` probabilidad de reconexión aleatoria

### Algoritmo Watts-Strogatz
```
1. Crear anillo: conectar cada nodo i con sus k/2 vecinos a cada lado
2. Para cada arista (i, j) del anillo:
   - Generar r = random(0,1)
   - Si r < p: reconectar j a un nodo aleatorio k ≠ i (evitar duplicados y auto-loops)
```

### Asignación de propiedades al generar
**Nodos** — asignar aleatoriamente al crear cada nodo:
- 10% de los nodos: `estadoId = 3` (RESISTENTE), `probabilidadPropagacion` entre 0.0 y 0.3
- 30% de los nodos: `estadoId = 2` (INFORMADO_PASIVO — serán "pasivos potenciales"), `probabilidadPropagacion` entre 0.0 y 0.3
- 60% restantes: `estadoId = 0` (NO_INFORMADO), `probabilidadPropagacion` entre 0.6 y 0.9
- `umbral` aleatorio: 30% crédulos (0.1–0.3), 50% promedio (0.3–0.6), 20% escépticos (0.6–0.9)
- `centralidadGrado` y `betweenness`: inicializar en 0.0 (se calculan en Fase 3)
- `nombre`: usar nombres realistas en español (lista de 250 nombres o formato "Usuario_N")
- `padreId = null` al crear

**Aristas**:
- 70%–90% de probabilidad: aristas entre nodos que fueron vecinos originales en el anillo ("amigos cercanos")
- 10%–30% de probabilidad: aristas reconectadas aleatoriamente ("conocidos")
- `activa = true` siempre al crear
- `peso = 1.0` por defecto

### Dependencias a inyectar
```java
@Autowired GrafoRepository grafoRepository;
@Autowired NodoRepository nodoRepository;
@Autowired AristaRepository aristaRepository;
@Autowired EstadoCatalogoRepository estadoRepository;
```

### Método público a exponer
```java
public Grafo generarYPersistir() { ... }
// Crea Grafo(totalNodos=250), luego 250 Nodos, luego las Aristas
// Devuelve el Grafo guardado con su id asignado
```

### Controller nuevo
**Archivo**: `com.viralsim.controllers.GrafoController` — agregar endpoint al existente:
```
POST /api/grafos/generar
```
Sin parámetros. Llama a `WattsStrogatzGenerator.generarYPersistir()` y devuelve el `Grafo` creado.

### Consideraciones
- No usar `grafoId` hardcodeado, siempre usar el id devuelto por el save
- Verificar que no se creen aristas duplicadas (misma pareja nodoOrigenId, nodoDestinoId) — la tabla tiene `UNIQUE(nodoOrigenId, nodoDestinoId)`
- El nodo semilla para simulación se elige después en la UI, no aquí

---

## FASE 2 — Motor de Simulación

**Paquete**: `com.viralsim.engine`

### 2A — Interfaz EstrategiaPropagacion
**Archivo**: `EstrategiaPropagacion.java`
```java
public interface EstrategiaPropagacion {
    // Recibe la lista de nodos activos del paso anterior y la red completa
    // Devuelve los nodos que cambiaron de estado en este paso
    List<Nodo> propagar(List<Nodo> nodosActivos, Map<Integer, List<Nodo>> adyacencia, Map<Integer, Arista> aristasPorPar);
}
```

### 2B — ModeloViral
**Archivo**: `ModeloViral.java` — implements `EstrategiaPropagacion`

**Lógica**:
```
Para cada nodoActivo en nodosActivos:
  Para cada vecino en adyacencia.get(nodoActivo.id):
    Si vecino.estadoId == 0 (NO_INFORMADO):
      r = random(0,1)
      Si r <= nodoActivo.probabilidadPropagacion:
        vecino.estadoId = 1 (INFORMADO_ACTIVO)
        agregar a nuevosInformados
Retornar nuevosInformados
```

### 2C — ModeloCascada
**Archivo**: `ModeloCascada.java` — implements `EstrategiaPropagacion`

**Lógica**:
```
Para cada nodoActivo en nodosActivos:
  Para cada arista activa del nodoActivo:
    vecino = el otro extremo de la arista
    Si vecino.estadoId == 0 (NO_INFORMADO):
      r = random(0,1)
      Si r <= arista.probabilidadArista:
        vecino.estadoId = 1 (INFORMADO_ACTIVO)
        agregar a nuevosInformados
      arista.activa = false  // UN SOLO INTENTO siempre, sin importar resultado
Retornar nuevosInformados
```

### 2D — ModeloThreshold
**Archivo**: `ModeloThreshold.java` — implements `EstrategiaPropagacion`

**Lógica** (recorre TODOS los nodos, no solo activos):
```
Para cada nodo en toda la red:
  Si nodo.estadoId == 0 (NO_INFORMADO):
    vecinosInformados = contar vecinos con estadoId != 0
    fraccion = vecinosInformados / total_vecinos
    Si fraccion >= nodo.umbral:
      nodo.estadoId = 1 (INFORMADO_ACTIVO)
      agregar a nuevosInformados
Retornar nuevosInformados
```

### 2E — MotorSimulacion
**Archivo**: `MotorSimulacion.java`  
**Anotación**: `@Service`

**Dependencias a inyectar**:
```java
NodoRepository, AristaRepository, SimulacionRepository,
PasoSimulacionRepository, NodoSimulacionRepository, EstadoCatalogoRepository
```

**Método principal**:
```java
public ResultadoSimulacion ejecutar(int simulacionId) { ... }
```

**Flujo interno**:
```
1. Cargar Simulacion por id → obtener grafoId, modeloId, nodoSemillaId
2. Cargar todos los Nodo del grafo → construir Map<Integer, Nodo> nodosPorId
3. Cargar todas las Arista del grafo → construir Map<Integer, List<Nodo>> adyacencia y Map<String, Arista> aristasPorPar (key: "min_max")
4. Seleccionar estrategia según modeloId: 1=Viral, 2=Cascada, 3=Threshold
5. Marcar nodoSemilla: estadoId = 1 (INFORMADO_ACTIVO)
6. Inicializar: nodosActivos = [nodoSemilla], paso = 1, paso50 = null
7. LOOP mientras nodosActivos no esté vacío (o para Threshold: mientras hubo cambios):
   a. nuevos = estrategia.propagar(nodosActivos, adyacencia, aristasPorPar)
   b. Guardar PasoSimulacion: {simulacionId, numeroPaso=paso, nuevosInformados=nuevos.size(), totalActivos, totalResistentes}
   c. Para cada nodo en nuevos: guardar NodoSimulacion: {simulacionId, pasoId, nodoId, estadoId=nuevo estado, pasoInfeccion=paso}
   d. Calcular alcance actual = nodos con estadoId != 0 / 250
   e. Si alcance >= 0.5 y paso50 == null: paso50 = paso
   f. nodosActivos = nuevos con estadoId == 1
   g. paso++
8. Actualizar Simulacion: totalPasos=paso-1, totalInformados=conteo final, paso50Porciento=paso50, resultado="COMPLETADA"
9. Devolver ResultadoSimulacion (objeto con métricas resumen)
```

**Clase interna o record `ResultadoSimulacion`**:
```java
public record ResultadoSimulacion(
    int simulacionId, int totalPasos, int totalInformados,
    double alcancePorcentaje, Integer paso50Porciento,
    int totalActivos, int totalPasivos, int totalResistentes, int totalNoInformados
) {}
```

### Controller nuevo
En `SimulacionController` agregar:
```
POST /api/simulaciones/{id}/ejecutar
→ devuelve ResultadoSimulacion como JSON
```

---

## FASE 3 — Métricas

**Paquete**: `com.viralsim.metrics`  
**Archivo**: `CalculadorMetricas.java`  
**Anotación**: `@Service`

### Centralidad de Grado
```java
// Para cada nodo: grado = número de aristas / (250 - 1)
double centralidadGrado = vecinos.size() / 249.0;
// Actualizar campo Nodo.centralidadGrado en BD
```
Ejecutar una sola vez al generar la red (llamar desde `WattsStrogatzGenerator` al final).

### Betweenness Centrality
Usar librería **JGraphT** (agregar dependencia en `pom.xml`):
```xml
<dependency>
    <groupId>org.jgrapht</groupId>
    <artifactId>jgrapht-core</artifactId>
    <version>1.5.2</version>
</dependency>
```
```java
// Construir SimpleGraph<Integer, DefaultEdge> con los nodos y aristas
// Usar BetweennessCentrality<Integer, DefaultEdge> de JGraphT
// Normalizar: betweenness = rawScore / ((n-1)*(n-2)/2)
// Actualizar campo Nodo.betweenness en BD
```
Ejecutar al generar la red, puede tardar ~2–5 segundos para 250 nodos.

### Endpoint métricas
En `SimulacionController` agregar:
```
GET /api/simulaciones/{id}/metricas
→ devuelve ResultadoSimulacion con todos los conteos
```

### Endpoint comparación
```
POST /api/simulaciones/comparar?grafoId=X&nodoSemillaId=Y
```
Ejecuta los 3 modelos secuencialmente sobre el mismo grafo (reseteando estados entre cada uno) y devuelve array de 3 `ResultadoSimulacion`.

**Importante al comparar**: antes de cada modelo, resetear todos los nodos del grafo a su `estadoId` original (guardado al generar) y resetear `arista.activa = true`.

---

## FASE 4 — Endpoints adicionales necesarios para el Frontend

Agregar en los controllers existentes:

```
GET  /api/nodos/grafo/{grafoId}            → List<Nodo> (ya existe, verificar)
GET  /api/aristas/grafo/{grafoId}          → List<Arista> filtradas por grafoId
GET  /api/nodos/{id}                       → Nodo individual
PUT  /api/nodos/{id}                       → actualizar probabilidadPropagacion, umbral, estadoId
GET  /api/nodos/grafo/{grafoId}/top-grado  → top 5 nodos por centralidadGrado DESC
GET  /api/nodos/grafo/{grafoId}/top-betweenness → top 3 nodos por betweenness DESC
GET  /api/simulaciones/{id}/pasos          → List<PasoSimulacion> ordenados por numeroPaso
GET  /api/simulaciones/{id}/nodo-simulacion?paso=N → List<NodoSimulacion> del paso N
```

---

## FASE 5 — Frontend: Estructura Base

**Carpeta**: `c:\Dev\VS_Proyect\frontend\`  
**Estructura de archivos a crear**:
```
frontend/
  index.html          ← punto de entrada, carga los 3 módulos
  css/
    style.css         ← estilos globales tema oscuro
  js/
    api.js            ← todas las llamadas fetch al backend (funciones reutilizables)
    grafo-renderer.js ← lógica vis.js compartida entre las 3 vistas
    vista-chisme.js   ← lógica de "Lanzar chisme"
    vista-propagacion.js ← lógica de "Propagación"
    vista-nodos.js    ← lógica de "Editar nodos"
  assets/             ← vacío por ahora
```

### Paleta de colores (usar en style.css como variables CSS)
```css
:root {
  --bg-primary: #0D0D0D;
  --bg-panel: #1A1A1A;
  --bg-card: #222222;
  --color-activo: #E53935;
  --color-pasivo: #FDD835;
  --color-resistente: #43A047;
  --color-no-informado: #AAAAAA;
  --color-accent: #E53935;
  --color-texto: #FFFFFF;
  --color-texto-secundario: #888888;
  --color-borde: #333333;
  --color-seleccion: #1565C0;
}
```

### Navbar (presente en las 3 vistas)
```html
<nav>
  <span class="logo">● ViralSim</span>
  <div class="tabs">
    <button data-tab="chisme">Lanzar chisme</button>
    <button data-tab="propagacion" class="active">Propagación</button>
    <button data-tab="nodos">Editar nodos</button>
  </div>
  <span class="nav-info" id="nav-info">Red: 250 nodos</span>
</nav>
```

### CDN a incluir en index.html
```html
<script src="https://unpkg.com/vis-network/standalone/umd/vis-network.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
```

### api.js — funciones base
```javascript
const BASE = 'http://localhost:8080';
async function getGrafo(id) { ... }
async function getNodos(grafoId) { ... }
async function getAristas(grafoId) { ... }
async function getNodo(id) { ... }
async function updateNodo(id, data) { ... }
async function getTopGrado(grafoId) { ... }
async function getTopBetweenness(grafoId) { ... }
async function generarRed() { ... }         // POST /api/grafos/generar
async function crearSimulacion(grafoId, modeloId, nodoSemillaId) { ... }
async function ejecutarSimulacion(simId) { ... }
async function getPasos(simId) { ... }
async function getNodosPaso(simId, paso) { ... }
async function compararModelos(grafoId, nodoSemillaId) { ... }
```

---

## FASE 6 — Vista "Lanzar chisme"

**Archivo**: `js/vista-chisme.js`

### Layout (2 columnas)
- **Izquierda**: canvas vis.js + buscador + filtros "Grado alto" / "Betweenness alto"
- **Derecha**: panel configuración

### Comportamiento del grafo en esta vista
- Todos los nodos en gris (`#AAAAAA`) excepto el seleccionado en rojo (`#E53935`)
- Nodo seleccionado tiene label "Origen" y es más grande (tamaño 20 vs 10)
- Label flotante "Nodo seleccionado" al hacer hover
- Click en nodo → actualizar panel derecho con datos del nodo

### Panel derecho — secciones
1. **Nodo origen**: Avatar con iniciales (2 letras del nombre), nombre completo, "#ID — estado", stats: Amigos / Grado / Betweenness, Prob. propagación %, Resistencia %, Estado inicial badge
2. **Modelo de propagación**: dropdown con "Viral — contagio por nodo" / "Cascada — contagio por arista" / "Threshold — presión social"
3. **Contenido del chisme**: `<textarea>` editable (solo visual, no se persiste)
4. **Alcance objetivo**: slider 0–100%, muestra "X%"
5. **Botón rojo "Lanzar simulación"**: llama a `POST /api/simulaciones` con {grafoId, modeloId, nodoSemillaId} → luego `POST /api/simulaciones/{id}/ejecutar` → cambia a vista Propagación con esa simulación activa
6. **Nota informativa** bajo el botón: texto dinámico según modelo seleccionado

---

## FASE 7 — Vista "Propagación"

**Archivo**: `js/vista-propagacion.js`

### Layout (grafo central + panel derecho + controles bottom + gráfica bottom-left)

### Grafo vis.js en esta vista
- Colores por estado: `estadoId 0=#AAAAAA, 1=#E53935, 2=#FDD835, 3=#43A047`
- Nodo semilla: tamaño 25, número interior blanco (label dentro del nodo)
- Aristas activas durante transmisión: color naranja `#FF6D00`, width 3
- Aristas bloqueadas (cascada): color gris punteado
- Animación: actualizar colores cada X ms según velocidad seleccionada

### Contador top-left
```html
<div class="paso-badge">
  Paso actual<br>
  <strong id="paso-actual">7</strong> <span>/~18</span>
</div>
```

### Panel derecho — secciones
1. **Métricas en tiempo real**:
   - Alcance %: número grande + fracción "158/250" + badge velocidad (rápido/medio/lento según paso50)
   - Paso actual / total estimado
   - Velocidad: nodos/paso promedio
2. **Estado de la red**: 4 filas con barra horizontal de color y conteo:
   - 🔴 Inf. activos | 🟡 Inf. pasivos | 🟢 Resistentes | ⚪ No informados
3. **Nodos más influyentes**: top 3 betweenness con barras + valor, top 2 grado con barras + valor
4. **Comparación de modelos**: tabla con columnas Modelo/Alcance/Paso50 para los 3 modelos

### Gráfica bottom-left
- **Chart.js** tipo "line", eje X = pasos, eje Y = % alcance (0–100)
- 3 líneas: Viral (rojo sólido), Cascada (discontinua), Threshold (punteada)
- Línea horizontal en 50% marcada
- Se actualiza en tiempo real durante la animación

### Controles bottom
```
[|| Pausar] [|◄ Reiniciar] [►| Paso a paso] [Dropdown modelo] [Velocidad ●──── 3×]
```
- **Pausar/Continuar**: toggle, detiene/reanuda el `setInterval`
- **Reiniciar**: limpia estado visual, vuelve al paso 0
- **Paso a paso**: avanza un paso manualmente
- **Velocidad slider**: 1×=1000ms, 2×=500ms, 3×=300ms, 4×=150ms, 5×=50ms por paso

### Flujo de animación
```javascript
// Al cargar con simulacionId:
1. GET /api/simulaciones/{id}/pasos → array de pasos
2. Para cada paso, cuando toca animarlo:
   GET /api/simulaciones/{id}/nodo-simulacion?paso=N → estados de ese paso
   Actualizar colores de nodos en vis.js
   Actualizar métricas en panel derecho
   Agregar punto a gráfica Chart.js
3. Al terminar: mostrar tabla comparativa (si existe)
```

---

## FASE 8 — Vista "Editar nodos"

**Archivo**: `js/vista-nodos.js`

### Layout (tabla izquierda + panel editor derecho)

### Tabla paginada
- 8 nodos por página, 250/8 = 32 páginas
- Columnas: `#ID | Nombre | Estado (badge) | Prop.% | Resist.% | Umbral`
- Badges de estado: pill coloreado según estadoId
- Fila seleccionada: fondo azul `#1565C0`
- Barra herramientas: buscador texto, dropdown estados, dropdown ordenar (ID/Nombre/Grado), botones "Exportar JSON" / "Guardar cambios"
- Paginador: `← Anterior | Pág. X/32 | Siguiente →` + contador "N cambios sin guardar" en verde `#43A047`

### Panel editor derecho (al seleccionar fila)
1. Avatar con iniciales + nombre + "#N — N conexiones"
2. **Slider Prob. propagación** (0–100%) → `Nodo.probabilidadPropagacion`
3. **Slider Resistencia inicial** (0–100%) — visual, mapea a probabilidad de estadoId=3
4. **Slider Umbral social** (0.1–0.9, step 0.05) → `Nodo.umbral`
5. **Dropdown Estado inicial**: No informado (0) / Informado activo (1) / Informado pasivo (2) / Resistente (3)
6. **Dropdown Perfil usuario**: Crédulo (fuerza umbral 0.1–0.3) / Promedio (0.3–0.6) / Escéptico (0.6–0.9) — ajusta slider umbral automáticamente
7. **Texto dinámico**: descripción de comportamiento según valores actuales
8. Botones: **[Descartar]** (revierte cambios locales) / **[Guardar nodo]** (llama `PUT /api/nodos/{id}`)

### Exportar JSON
```javascript
// Descarga todos los nodos del grafo como archivo JSON
const blob = new Blob([JSON.stringify(nodos, null, 2)], {type: 'application/json'});
// Crear link temporal y click automático
```

---

## ORDEN DE EJECUCIÓN ESTRICTO

```
FASE 1  →  FASE 2  →  FASE 3  →  FASE 4
  ↓
FASE 5 (base HTML/CSS/api.js/grafo-renderer.js)
  ↓
FASE 6 (vista-chisme.js)
  ↓
FASE 7 (vista-propagacion.js)
  ↓
FASE 8 (vista-nodos.js)
```

**Regla**: No avanzar a la siguiente fase hasta que la anterior compile y funcione.  
**Test mínimo por fase**:
- Fase 1: `POST /api/grafos/generar` devuelve Grafo con 250 nodos en BD
- Fase 2: `POST /api/simulaciones/{id}/ejecutar` devuelve `ResultadoSimulacion` con datos reales
- Fase 3: nodos tienen `centralidadGrado` y `betweenness` > 0 después de generar
- Fase 4: todos los endpoints GET adicionales responden 200
- Fases 5–8: abrir `index.html` en navegador, verificar cada vista visualmente

---

## NOTAS TÉCNICAS IMPORTANTES

1. **Lombok**: configurado como `annotationProcessorPaths` en `pom.xml`. Si se agregan entidades, usar `@Data @NoArgsConstructor`.
2. **ddl-auto=validate**: Hibernate nunca modifica la BD. Si se agrega un campo a una entidad, también agregar la columna en MySQL manualmente o cambiar a `ddl-auto=update` temporalmente.
3. **Naming strategy**: `PhysicalNamingStrategyStandardImpl` — los `@Column(name="...")` y `@JoinColumn(name="...")` deben usar el nombre exacto de la columna en BD (camelCase tal como está en el schema).
4. **CORS**: Para que el frontend HTML pueda llamar al backend, agregar en `ViralSimApplication.java` o en un `@Configuration`:
   ```java
   @Bean
   public WebMvcConfigurer corsConfigurer() {
       return new WebMvcConfigurer() {
           @Override
           public void addCorsMappings(CorsRegistry registry) {
               registry.addMapping("/api/**").allowedOrigins("*");
           }
       };
   }
   ```
5. **Transaccionalidad en el motor**: anotar `MotorSimulacion.ejecutar()` con `@Transactional` para que todos los saves sean atómicos.
6. **Reconexión Watts-Strogatz**: al reconectar una arista, verificar que el nuevo destino no sea el origen (`i != k`) y que el par no exista ya en la lista de aristas construida en memoria antes de llamar al save.
7. **vis.js config base para 250 nodos**:
   ```javascript
   const options = {
     nodes: { shape: 'dot', size: 8, font: { color: '#fff', size: 11 } },
     edges: { color: '#444', width: 1 },
     physics: { stabilization: { iterations: 200 }, barnesHut: { gravitationalConstant: -3000 } },
     interaction: { hover: true, tooltipDelay: 200 }
   };
   ```
