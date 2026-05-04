# Estructura del Proyecto ViralSim

## Organización General

```
ED_Proyect/
│
├── 📄 PROYECTO_RESUMEN.md              # Índice general y visual
├── 📄 REFERENCIA_RAPIDA.md             # Guía de bolsillo
├── 📄 STRUCTURE.md                     # Este archivo
├── 📄 ESTADO_DEL_PROYECTO.md           # Estado actual
│
├── 📄 ViralSim_Entregable1.docx       # Especificaciones
├── 📄 viralsim_research_guide.html    # Guía de investigación
│
├── 📂 VS_Proyect/                      # 🎯 CÓDIGO Y DESARROLLO
│   │
│   ├── 📂 backend/                    # Motor de simulación (Java)
│   │   ├── pom.xml
│   │   └── src/main/java/com/viralsim/
│   │       ├── models/                # (Nodo, Arista, Grafo, Estado)
│   │       ├── propagation/           # (Modelos: Viral, Cascada, Threshold)
│   │       ├── engine/                # (MotorSimulacion, Eventos)
│   │       ├── metrics/               # (CalculadorMetricas)
│   │       ├── database/              # (DAOs: NodoDAO, AristaDAO, etc.)
│   │       └── utils/                 # (Generadores: WattsStrogatz, Betweenness)
│   │
│   ├── 📂 frontend/                   # Interfaz web (HTML/JS/CSS)
│   │   ├── index.html
│   │   ├── css/
│   │   │   ├── style.css
│   │   │   └── responsive.css
│   │   ├── js/
│   │   │   ├── main.js
│   │   │   ├── api-client.js
│   │   │   ├── simulator.js
│   │   │   ├── ui-controller.js
│   │   │   └── visualization.js
│   │   └── assets/
│   │       ├── icons/
│   │       └── images/
│   │
│   ├── 📂 database/                   # Esquema de BD
│   │   ├── schema.sql
│   │   └── seed-data.sql
│   │
│   └── 📂 docs/                       # Documentación técnica
│       ├── BACKEND.md
│       ├── DATABASE.md
│       ├── FRONTEND.md
│       └── ARCHITECTURE.md
│
├── 📂 code/                            # Archivos Java (legado)
│   └── Dijkstra.java, Graph.java, ...
│
└── 📂 .venv/                           # Entorno virtual Python
```

## Capas de la Arquitectura

### 1. **Capa de Modelos (Backend/Models)**
Estructuras de datos fundamentales
- `Nodo.java` - Representa un usuario/nodo
- `Arista.java` - Representa una amistad/conexión
- `Grafo.java` - Red completa de 250 nodos
- `Estado.java` - Enumeración de estados (NO_INFORMADO, INFORMADO_ACTIVO, INFORMADO_PASIVO, RESISTENTE)

### 2. **Capa de Propagación (Backend/Propagation)**
Implementación de los 3 modelos
- `ModeloPropagacion.java` - Interfaz base
- `ModeloViral.java` - Implementación modelo viral
- `ModeloCascadaIndependiente.java` - Implementación cascada
- `ModeloUmbralLineal.java` - Implementación threshold

### 3. **Capa de Motor (Backend/Engine)**
Orquestación de la simulación
- `MotorSimulacion.java` - Ejecuta la simulación paso a paso
- `SimulacionResultado.java` - Contiene resultados de una ejecución
- `Evento.java` - Eventos de la simulación

### 4. **Capa de Métricas (Backend/Metrics)**
Cálculo de indicadores
- `CalculadorMetricas.java` - Calcula métricas en tiempo real
- `Metrica.java` - Datos de una métrica

### 5. **Capa de Base de Datos (Backend/Database)**
Persistencia
- `ConexionBD.java` - Conexión a MySQL
- `NodoDAO.java` - CRUD de nodos
- `AristaDAO.java` - CRUD de aristas
- `SimulacionDAO.java` - CRUD de simulaciones
- `HistorialDAO.java` - Guardado de historial paso a paso
- `MetricaDAO.java` - Guardado de métricas finales

### 6. **Capa Utilitaria (Backend/Utils)**
Funciones auxiliares
- `GeneradorAleatorios.java` - Números aleatorios
- `GeneradorWattsStrogatz.java` - Generación de topología
- `Exportador.java` - Exporta datos a JSON para frontend

### 7. **Capa de Interfaz (Frontend)**
Presentación visual
- `index.html` - Estructura HTML
- `style.css` - Diseño
- `main.js` - Inicialización
- `visualization.js` - Integración con vis.js
- `ui-controller.js` - Eventos de UI
- `api-client.js` - Llamadas a backend

## Flujo de Datos

```
Backend:
  Modelos (Nodo, Arista, Grafo)
       ↓
  Motor de Simulación (ejecuta paso a paso)
       ↓
  Modelos de Propagación (Viral/Cascada/Threshold)
       ↓
  Calculador de Métricas (O(1) lookups con HashMap)
       ↓
  DAO (persiste en MySQL)
       ↓
  Exportador (genera JSON)
       ↓
Frontend:
  API Client (recibe JSON)
       ↓
  Simulator (procesa datos)
       ↓
  Visualization (actualiza vis.js)
       ↓
  UI Controller (actualiza gráficos/métricas)
```

## Convenciones de Nombres

- **Clases**: PascalCase (MotorSimulacion)
- **Métodos**: camelCase (calcularMetricas)
- **Constantes**: UPPER_SNAKE_CASE (ESTADO_INFORMADO)
- **Variables**: camelCase (nodoActual)
- **Paquetes**: com.viralsim.{modulo}

