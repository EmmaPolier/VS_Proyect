# Especificación del Frontend - ViralSim

## Organización de Archivos

```
frontend/
├── index.html              - Página principal (única página)
├── css/
│   ├── style.css          - Estilos principales
│   └── responsive.css     - Diseño responsivo
├── js/
│   ├── main.js            - Punto de entrada
│   ├── simulator.js       - Lógica de simulación en cliente
│   ├── ui-controller.js   - Control de componentes
│   ├── visualization.js   - Integración vis.js
│   └── api-client.js      - Comunicación backend
└── assets/
    ├── icons/
    └── images/
```

---

## Componentes HTML

### 1. **Contenedor Principal**
```
<body>
  ├── Header
  │  └── Logo + Título "ViralSim"
  └── Main Container
     ├── Panel de Control (Lateral Izquierda)
     ├── Área de Visualización (Centro)
     └── Panel de Analíticas (Lateral Derecha)
```

### 2. **Panel de Control**
**Ubicación**: Lateral izquierda
**Elementos**:
- Título: "Panel de Control"
- **Selector de Modelo** (dropdown)
  - Opciones: Viral, Cascada, Threshold
  - Evento: change → actualizará descripción del modelo
- **Selector de Nodo Origen** (dropdown o búsqueda)
  - Opciones: lista de 250 nodos
  - Evento: change → resaltará nodo en grafo
- **Slider de Velocidad** (1x, 2x, 4x, 8x)
  - Para control de animación
- **Slider de Probabilidad Global** (0.4 - 0.9)
  - Opcional: afecta riesgo de contagio
- **Botones de Control**
  - Iniciar: lanza simulación
  - Pausar: pausa en step actual
  - Continuar: reanuda desde pausa
  - Reiniciar: vuelve a paso 0
  - Limpiar: reset completo
- **Información del Modelo Activo** (texto)
  - Descripción breve del modelo seleccionado
- **Indicadores de Estado**
  - Simulación en progreso / pausada / completada
  - Paso actual: X / Y

### 3. **Área de Visualización (Centro)**
**Elemento principal**: Canvas/contenedor para vis.js
- **vis.js Network**
  - 250 nodos con colores según estado
  - Aristas conectando nodos
  - Física habilitada (opcional para UX)
  - Zoom y pan interactivos
  
**Colores de nodos**:
- 🔵 **Azul**: NO_INFORMADO
- 🟠 **Naranja**: INFORMADO_ACTIVO (parpadea/brilla)
- 🟡 **Amarillo**: INFORMADO_PASIVO
- ⚫ **Gris**: RESISTENTE

**Interacción**:
- Click en nodo → mostrar detalles en panel lateral
- Hover → tooltip con info del nodo
- Zoom/pan con rueda del ratón

### 4. **Panel de Analíticas (Lateral Derecha)**
**Secciones**:

#### A. Métricas en Tiempo Real
- **Alcance (%)**: barra de progreso + número
- **Paso Actual**: paso N de total
- **Velocidad Propagación**: "Alcanzó 50% en paso X"
- **Nodos Informados**: contador actualizando
- **Nodos Activos**: contador actualizando
- **Nodos Resistentes**: contador actualizando

#### B. Gráfica de Evolución
- **Tipo**: Gráfica de área / línea
- **Ejes**:
  - X: Paso de simulación (0 a N)
  - Y: Porcentaje de alcance (0% a 100%)
- **Actualización**: en tiempo real
- Librería: Chart.js o similar

#### C. Nodos Más Influyentes
- **Top 5 por Grado Centralidad**
  - Lista ordenada
  - Muestra: Nodo ID, Nombre, Grado, Centralidad
- **Top 5 por Betweenness Centrality**
  - Lista ordenada
  - Muestra: Nodo ID, Nombre, Betweenness, Centralidad

#### D. Detalles de Nodo Seleccionado (si hay click)
- **Información**:
  - ID
  - Nombre
  - Estado actual
  - Paso de infección
  - Número de vecinos
  - Centralidad de grado
  - Betweenness
  - Quién lo contagió (padre)

---

## Vistas Principales

### Vista 1: Inicio (Carga)
**Elementos visibles**:
- Panel de Control completamente accesible
- Área central vacía o con botón "Generar Red"
- Panel de analíticas inactivo
- Botón: "Generar Red y Nodos" (si es primera vez)

### Vista 2: Red Generada (Antes de Simulación)
**Elementos visibles**:
- Grafo completo en área central (todos nodos azules)
- Panel de Control con opciones activas
- Botón "Iniciar" habilitado
- Seleccionar modelo y nodo origen

### Vista 3: Simulación en Progreso
**Elementos visibles**:
- Grafo actualizándose en tiempo real
- Nodos cambiando de color según estado
- Contador de pasos en Panel de Control
- Métricas en Panel de Analíticas actualizando
- Gráfica de alcance creciendo
- Botones: Pausar (activo), Continuar (grisado)

### Vista 4: Simulación Pausada
**Elementos visibles**:
- Grafo "congelado" en paso actual
- Botón Continuar se habilita
- Botón Iniciar se deshabilita
- Botón Reiniciar disponible

### Vista 5: Simulación Completada
**Elementos visibles**:
- Grafo final con colores finales
- Panel de Control:
  - Paso Final mostrado
  - Botones Iniciar/Reiniciar habilitados
- Panel de Analíticas:
  - Gráfica completa
  - **Tabla Comparativa** (si se ejecutaron 3 modelos)
    - Comparación lado a lado: Modelo | Alcance | Velocidad | Paso Final
  - Nodos influyentes finales

---

## Flujo de Interacción del Usuario

```
1. Carga la página (index.html)
   ↓
2. Frontend carga:
   - Configuración de servidor
   - Conecta con API backend
   ↓
3. Usuario selecciona:
   - Modelo de propagación
   - Nodo origen
   - Velocidad de animación
   ↓
4. Usuario hace click en "Iniciar"
   ↓
5. Frontend envía petición POST a backend:
   - JSON: { modelo, nodoId, velocidadAnimacion }
   ↓
6. Backend genera simulación, retorna JSON paso a paso:
   - Paso 0: nodo origen marcado
   - Paso 1: nuevos informados
   - ... hasta que termina
   ↓
7. Frontend recibe cada paso y:
   - Actualiza posición y color de nodos
   - Actualiza métricas
   - Actualiza gráficas
   - Anima transiciones
   ↓
8. Simulación termina:
   - Frontend muestra resultados finales
   - Permite comparar otros modelos
   - Permite descargar resultados (JSON/CSV)
```

---

## JavaScript - Estructura de Módulos

### `main.js` (Punto de Entrada)
```
- Inicialización del DOM
- Carga configuración
- Conecta módulos
- Event listeners globales
```

### `api-client.js` (Comunicación Backend)
```
Funciones:
- generarRed() → Promise<Grafo JSON>
- iniciarSimulacion(modelo, nodoId) → Stream JSON
- pausarSimulacion()
- reanudarSimulacion()
- obtenerResultados(simId) → Promise<Resultado JSON>
```

### `simulator.js` (Lógica de Simulación)
```
Funciones:
- procesarPasoDelBackend(dataJSON)
- actualizarEstadosLocales()
- calcularProximoPaso()
- verificarTerminacion()
```

### `visualization.js` (vis.js Integration)
```
Funciones:
- inicializarGrafo(nodos, aristas)
- actualizarColorNodo(nodoId, estado)
- resaltarNodoOrigen(nodoId)
- animarTransicion(nodoId, estadoAnterior, estadoNuevo)
- zonarEnNodo(nodoId)
```

### `ui-controller.js` (Control de Interfaz)
```
Funciones:
- crearSliderVelocidad()
- crearSelectorModelo()
- crearSelectorNodoOrigen()
- actualizarMetricasEnTiempo Real()
- mostrarDetallesNodo(nodoId)
- actualizarGraficaAlcance(datosNuevos)
- mostrarComparativaModelos(resultados)
```

---

## Comunicación Frontend ↔ Backend

### Request: Iniciar Simulación
```json
POST /api/simulacion/iniciar
{
  "modelo": "Viral",
  "nodoOrigenId": 42,
  "velocidadAnimacion": 2
}
```

### Response: Stream de Pasos
```json
{
  "paso": 0,
  "nodosActivados": [
    { "id": 42, "estado": "INFORMADO_ACTIVO" }
  ],
  "metricas": {
    "alcance": 0.4,
    "nodosInformados": 1
  }
}

{
  "paso": 1,
  "nodosActivados": [
    { "id": 15, "estado": "INFORMADO_ACTIVO" },
    { "id": 28, "estado": "INFORMADO_ACTIVO" },
    { "id": 63, "estado": "INFORMADO_ACTIVO" }
  ],
  "metricas": {
    "alcance": 1.6,
    "nodosInformados": 4
  }
}

...

{
  "evento": "SIMULACION_COMPLETADA",
  "resultadoFinal": {
    "alcanceTotal": 86.4,
    "paso50Porciento": 4,
    "pasoFinal": 12,
    "estadosFinales": {
      "NO_INFORMADO": 34,
      "INFORMADO_ACTIVO": 0,
      "INFORMADO_PASIVO": 216,
      "RESISTENTE": 0
    }
  }
}
```

---

## Estilos CSS - Estructura

### `style.css`
- Variables globales (colores, fonts)
- Layout general (grid/flexbox)
- Componentes (panel, botones, cards)
- Animaciones (transiciones de color, parpadeo)
- Tipografía

### `responsive.css`
- Media queries para mobile
- Adaptación de layout
- Ocultar/mostrar elementos según viewport

---

## Bibliotecas Externas Requeridas

- **vis.js**: Visualización de grafos
- **Chart.js** (o similar): Gráficas de evolución
- **Fetch API**: Comunicación con backend (nativo)
- **HTML5 Canvas**: Renderización (nativo)

---

## Consideraciones de Performance

- Renderizar solo cambios en el grafo (diff)
- Actualizar gráficas cada 2-3 pasos (no cada paso)
- Usar WebWorkers si es muy pesado
- Implementar virtual scrolling para lista de nodos
- Caché de resultados de BD

