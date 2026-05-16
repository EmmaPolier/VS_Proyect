# 📁 Estructura del Frontend ViralSim

## Árbol de Carpetas

```
frontend/
├── index.html              # Página HTML principal
├── css/
│   └── style.css           # Estilos (tema oscuro)
├── js/
│   ├── api.js              # Llamadas REST al backend
│   ├── app.js              # Lógica principal de la aplicación
│   ├── vista-nodos.js      # Gestión y edición de nodos
│   ├── vista-chisme.js     # Lanzar simulaciones
│   └── vista-propagacion.js# Ver animación y métricas
├── package.json            # Información del proyecto
├── .gitignore              # Archivos a ignorar en Git
├── .env.example            # Variables de entorno de ejemplo
├── README.md               # Documentación principal
├── PRUEBA.md               # Guía de pruebas
└── ESTRUCTURA.md           # Este archivo
```

## 📄 Descripción Detallada

### `index.html` (HTML Principal)

- Estructura base con navbar y contenedor principal
- **4 pestañas (tabs)**:
  1. **Inicio** - Estadísticas y botones de acción
  2. **Editar Nodos** - Tabla y editor de nodos
  3. **Lanzar Chisme** - Selección de nodo y lanzamiento
  4. **Propagación** - Visualización de resultados

- **Librerías externas**:
  - `vis.js` - Visualización de grafos interactivos
  - `Chart.js` - Gráficas de datos
  - CSS personalizado local

- **Estructura HTML**:
  ```html
  <body>
    <nav class="navbar">...</nav>
    <main class="main-content">
      <section id="inicio" class="tab-content">...</section>
      <section id="nodos" class="tab-content">...</section>
      <section id="chisme" class="tab-content">...</section>
      <section id="propagacion" class="tab-content">...</section>
    </main>
  </body>
  ```

### `css/style.css` (Estilos)

- **Tema oscuro** con variables CSS
- **Colores principales**:
  - Primario: `#ff6b6b` (rojo coral)
  - Secundario: `#4ecdc4` (turquesa)
  - Acentos: `#ffd93d` (amarillo)
  
- **Componentes estilizados**:
  - Navbar con tabs
  - Cards y panels
  - Botones con hover effects
  - Tablas con filas alternadas
  - Modales
  - Sliders personalizados
  - Barras de progreso
  - Gráficas

- **Responsive**: Adaptado para móvil, tablet y escritorio

### `js/api.js` (API REST)

Funciones para comunicarse con el backend.

**Secciones principales**:

```javascript
// GRAFOS
crearGrafo(nombre)
obtenerGrafos()
obtenerGrafo(id)

// NODOS
obtenerNodosGrafo(grafoId)
obtenerNodo(id)
actualizarNodo(id, datos)
obtenerTopGrado(grafoId)
obtenerTopBetweenness(grafoId)

// ARISTAS
obtenerAristasGrafo(grafoId)

// SIMULACIONES
crearSimulacion(datos)
obtenerSimulaciones()
obtenerSimulacion(id)
ejecutarSimulacion(id)
obtenerMetricasSimulacion(id)
obtenerPasosSimulacion(id)
obtenerNodoSimulacion(id, paso)

// CONFIGURACIONES
crearConfiguracion(datos)
obtenerConfiguraciones()

// UTILIDADES
getEstadoColor(estadoId)
getEstadoNombre(estadoId)
getEstadoLabel(estadoId)
getModeloInfo(modeloId)
generarRedWattsStrogatz()
```

**Constantes**:
- `ESTADOS`: Mapeo de ID de estado a propiedades (color, nombre, emoji)
- `MODELOS`: Información de los 3 modelos de propagación
- `API_BASE_URL`: URL del backend

### `js/app.js` (Lógica Principal)

Orquestación de la aplicación.

**Responsabilidades**:
- Cambio de pestañas (event listeners)
- Actualización de estadísticas
- Generación de red
- Auto-actualización cada 5 segundos

**Event listeners principales**:
- Click en tabs
- Click en botón "Generar Red"
- Click en botón "Cargar Red Existente"

### `js/vista-nodos.js` (Gestión de Nodos)

Tabla paginada y editor de nodos.

**Funcionalidades**:
- Cargar nodos de un grafo
- Filtrar por búsqueda (nombre/ID)
- Filtrar por estado
- Paginación (8 nodos por página)
- Modal editor para cambiar propiedades

**Variables importantes**:
- `nodosActuales`: Lista de nodos cargados
- `paginaActual`: Página actual de paginación
- `nodoEnEdicion`: Nodo seleccionado para editar

**Funciones principales**:
```javascript
cargarNodos()              // Obtener nodos del backend
renderizarNodos()          // Dibujar tabla y actualizar paginación
editarNodo(id)             // Abrir modal para editar
guardarNodo()              // Enviar cambios al backend
cancelarEdicion()          // Cerrar modal
actualizarValoresSliders() // Sincronizar sliders con labels
```

### `js/vista-chisme.js` (Lanzar Simulación)

Selección de nodo semilla y configuración de simulación.

**Funcionalidades**:
- Cargar y visualizar el grafo con vis.js
- Seleccionar nodo semilla haciendo clic
- Selector de modelo de propagación
- Textarea para el mensaje/chisme
- Slider para alcance máximo

**Variables importantes**:
- `nodoSemillaSeleccionado`: Nodo elegido como semilla
- `modeloSeleccionado`: Modelo de propagación (1, 2 o 3)
- `redVis`: Instancia de vis.Network

**Funciones principales**:
```javascript
cargarGrafoChisme()        // Obtener nodos y aristas
visualizarGrafo()          // Renderizar con vis.js
seleccionarNodoSemilla(id) // Marcar nodo seleccionado
lanzarSimulacion()         // Crear y ejecutar simulación
```

### `js/vista-propagacion.js` (Ver Resultados)

Visualización de la propagación con animación y métricas.

**Funcionalidades**:
- Mostrar grafo coloreado por estado
- Animación paso a paso
- Barras de estado en tiempo real
- Gráfica de propagación (Chart.js)
- Controles: pausar, siguiente paso, reiniciar

**Variables importantes**:
- `simulacionActual`: Datos de la simulación
- `pasosSimulacion`: Array con cada paso
- `pasoActual`: Paso actual en visualización
- `historicoEstados`: Datos para la gráfica
- `redPropagacion`: Instancia de vis.Network
- `chartPropagacion`: Instancia de Chart.js

**Funciones principales**:
```javascript
cargarPropagacion()       // Obtener datos de simulación
visualizarPropagacion()   // Renderizar grafo
actualizarMetricas()      // Actualizar barras y gráfica
siguientePaso()           // Avanzar a siguiente paso
autoAvanzar()             // Reproducción automática
reiniciarSimulacion()     // Volver al paso 0
```

## 🔄 Flujo de Datos

```
┌─────────────────────────────────────────┐
│ Vista: INICIO                           │
│ - Estadísticas                          │
│ - Botón "Generar Red"                   │
└────────────┬────────────────────────────┘
             │ generarRedWattsStrogatz()
             ▼
┌─────────────────────────────────────────┐
│ Backend: Crea grafo + 250 nodos         │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Vista: NODOS (opcional)                 │
│ - Tabla paginada                        │
│ - Editor modal                          │
│ - actualizarNodo()                      │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Vista: CHISME                           │
│ - Grafo vis.js                          │
│ - Seleccionar nodo                      │
│ - Seleccionar modelo                    │
│ - lanzarSimulacion()                    │
└────────────┬────────────────────────────┘
             │ crearSimulacion() + ejecutarSimulacion()
             ▼
┌─────────────────────────────────────────┐
│ Backend: Simula propagación             │
│ - Crea pasos de simulación              │
│ - Guarda estados de nodos               │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ Vista: PROPAGACIÓN                      │
│ - obtenerPasosSimulacion()              │
│ - Grafo vis.js coloreado                │
│ - Animación paso a paso                 │
│ - Gráfica Chart.js                      │
│ - Barras de estado                      │
└─────────────────────────────────────────┘
```

## 📦 Dependencias Externas

- **vis.js** (CDN) - Visualización de grafos
- **Chart.js** (CDN) - Gráficas de datos
- **CSS personalizado** - Sin framework (vanilla CSS)

## 🎨 Diseño Visual

### Paleta de Colores

```css
--primary: #ff6b6b        /* Rojo coral */
--secondary: #4ecdc4      /* Turquesa */
--accent: #ffd93d         /* Amarillo */
--bg-dark: #0a0e27        /* Fondo muy oscuro */
--bg-card: #141b2e        /* Fondo tarjetas */
--bg-input: #1a2240       /* Fondo inputs */
--border-color: #2a3f5f   /* Bordes */
--text-primary: #e8e8e8   /* Texto principal */
--text-secondary: #a0a0a0 /* Texto secundario */
--text-muted: #707070     /* Texto atenuado */
```

### Estados (Colores de Nodos)

```javascript
0: NO_INFORMADO    → #666666
1: INFORMADO_ACTIVO → #ff6b6b
2: INFORMADO_PASIVO → #ffd93d
3: RESISTENTE       → #6bcf7f
```

## 🔌 Integración con Backend

**URL Base**: `http://localhost:8080/api`

**CORS**: Debe estar habilitado para el frontend

**Endpoints esperados**: Ver `api.js` para lista completa

## 🚀 Performance

- **vis.js**: Maneja hasta 1000 nodos sin problemas
- **Chart.js**: Actualización suave con <50 puntos de dato
- **Carga inicial**: ~2-3 segundos (incluye CDN)
- **Animaciones**: 60 FPS en navegadores modernos

## 🔒 Seguridad

- Sin autenticación (aplicación de demostración)
- Sin encriptación
- Sin validación robusta (temporal)

---

**Versión**: 1.0 (Temporal)
**Última actualización**: Mayo 2026
