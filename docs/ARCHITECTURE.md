# Arquitectura del Sistema - ViralSim

## Visión General

ViralSim es una aplicación cliente-servidor que modela la propagación de rumores en redes sociales. El backend en Java ejecuta simulaciones determinísticas sobre una red de 250 nodos, mientras que el frontend web visualiza los resultados en tiempo real.

---

## Diagrama de Capas

```
┌─────────────────────────────────────────────────────────────┐
│                     FRONTEND (Web)                          │
│  HTML5 + JavaScript + CSS + vis.js + Chart.js              │
│  ├─ UI Controller (manejo de eventos)                       │
│  ├─ Simulator (lógica cliente)                              │
│  ├─ Visualization (integración vis.js)                      │
│  └─ API Client (HTTP requests)                              │
└─────────────────────────────────────────────────────────────┘
                          ↕ JSON over HTTP
                    (REST API)
┌─────────────────────────────────────────────────────────────┐
│                  BACKEND (Java)                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │           Presentación / Controladores                │  │
│  │  (Servlets / REST API)                                │  │
│  └───────────────────────────────────────────────────────┘  │
│                          ↕                                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              Lógica de Negocio                        │  │
│  │  ├─ Motor de Simulación                               │  │
│  │  ├─ Modelos de Propagación (3)                        │  │
│  │  ├─ Calculador de Métricas                            │  │
│  │  └─ Generador de Red                                  │  │
│  └───────────────────────────────────────────────────────┘  │
│                          ↕                                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         Modelos de Datos / Estructuras                │  │
│  │  ├─ Nodo                                               │  │
│  │  ├─ Arista                                             │  │
│  │  ├─ Grafo                                              │  │
│  │  └─ Estado (Enumeración)                               │  │
│  └───────────────────────────────────────────────────────┘  │
│                          ↕                                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │           Acceso a Datos (DAO Pattern)                │  │
│  │  ├─ NodoDAO                                            │  │
│  │  ├─ AristaDAO                                          │  │
│  │  ├─ SimulacionDAO                                      │  │
│  │  ├─ HistorialDAO                                       │  │
│  │  └─ MetricaDAO                                         │  │
│  └───────────────────────────────────────────────────────┘  │
│                          ↕                                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │       Capa de Persistencia (MySQL)                    │  │
│  │  ├─ Conexión JDBC                                      │  │
│  │  └─ Connection Pooling                                 │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                          ↕ SQL
┌─────────────────────────────────────────────────────────────┐
│                    BASE DE DATOS                            │
│                      MySQL 8.0+                             │
│   (nodos, aristas, simulaciones, historial, métricas)      │
└─────────────────────────────────────────────────────────────┘
```

---

## Componentes Principales

### 1. **Controlador / API REST** (Backend)
**Responsabilidad**: Recibir requests HTTP, orquestar lógica, retornar JSON

**Endpoints** (propuestos):
```
POST   /api/red/generar                    - Crea la red Watts-Strogatz
GET    /api/red                            - Obtiene nodos y aristas
GET    /api/nodos                          - Lista todos los nodos
GET    /api/nodo/{id}                      - Detalles de un nodo
POST   /api/simulacion/iniciar             - Inicia una simulación
GET    /api/simulacion/{id}/progreso       - Stream de pasos
POST   /api/simulacion/{id}/pausar         - Pausa la simulación
POST   /api/simulacion/{id}/continuar      - Reanuda
GET    /api/simulacion/{id}/resultados     - Resultados finales
GET    /api/metricas/{simId}               - Datos de métricas
GET    /api/comparativa                    - Comparación de 3 modelos
```

### 2. **Motor de Simulación** (Backend)
**Responsabilidad**: Ejecutar paso a paso, aplicar modelo, actualizar estados

**Entrada**:
- Grafo completo
- Nodo origen
- Modelo seleccionado

**Proceso**:
- Marca nodo origen como INFORMADO_ACTIVO (paso 0)
- Bucle: mientras haya nodos activos
  - Obtiene nodos activos
  - Para cada activo, aplica modelo.propagar()
  - Actualiza estados
  - Calcula métricas
  - Retorna paso al cliente

**Salida**: EventoSimulacion con lista de nodos que cambiaron estado

### 3. **Modelos de Propagación** (Backend)
**Responsabilidad**: Implementar lógica de contagio

Cada modelo es una clase que implementa la interfaz `ModeloPropagacion`:
- `propagar(nodo, grafo): List<Nodo>`

**Los 3 modelos**:
1. **ModeloViral**: Por cada vecino, genera random, contagia si < prob
2. **ModeloCascadaIndependiente**: Por cada arista activa, genera random, contagia si < prob arista
3. **ModeloUmbralLineal**: Para cada nodo no informado, si %informados >= umbral, activa

### 4. **Calculador de Métricas** (Backend)
**Responsabilidad**: Calcular indicadores O(1) gracias a HashMap

**Métricas por paso**:
- Alcance actual (%)
- Nodos por estado
- Velocidad (cuándo alcanza 50%)

**Operación**: HashMap<Integer, Nodo> permite lookups O(1)

### 5. **Base de Datos** (MySQL)
**Responsabilidad**: Persistencia de red, simulaciones, historial, métricas

**Tablas clave**:
- `nodos` - 250 registros
- `aristas` - ~750 registros
- `simulaciones` - Metadata de ejecuciones
- `historial_infeccion` - Registro paso a paso
- `metricas` - Snapshot de métricas por paso

### 6. **Generador de Red** (Backend)
**Responsabilidad**: Crear topología Watts-Strogatz

**Salida**: Grafo con 250 nodos, k=6 vecinos, p=0.1 reconexión
- Cada nodo tiene prob_propagación aleatoria
- Cada arista tiene prob_arista aleatoria
- Se calculan centralidades (grado, betweenness)

### 7. **Interfaz Web** (Frontend)
**Responsabilidad**: Visualizar grafo, permitir control, mostrar métricas

**Componentes**:
- Panel de Control (selectores, botones)
- Lienzo vis.js (grafo animado)
- Panel de Analíticas (métricas, gráficas, top nodos)

---

## Flujo Completo de Ejecución

```
INICIO DEL USUARIO
│
├─ Usuario abre index.html
│  └─ Frontend carga main.js
│
├─ Frontend solicita: POST /api/red/generar
│  └─ Backend:
│     ├─ GeneradorWattsStrogatz.generar()
│     ├─ GeneradorBetweenness.calcular()
│     ├─ NodoDAO.crear() × 250
│     ├─ AristaDAO.crear() × ~750
│     └─ Retorna JSON: { nodos[], aristas[] }
│
├─ Frontend renderiza grafo con vis.js
│
├─ Usuario selecciona:
│  ├─ Modelo (Viral / Cascada / Threshold)
│  ├─ Nodo origen
│  └─ Velocidad animación
│
├─ Usuario presiona "Iniciar"
│  └─ Frontend envía: POST /api/simulacion/iniciar
│     └─ Backend:
│        ├─ MotorSimulacion.iniciarSimulacion(nodo_origen, modelo)
│        ├─ SimulacionDAO.crear() - Guarda metadata
│        └─ Inicia bucle de pasos
│
├─ Bucle: Paso = 0, 1, 2, ... N
│  ├─ Backend:
│  │  ├─ nodosActivos = Grafo.getNodos(estado==ACTIVO)
│  │  ├─ Para cada nodo activo:
│  │  │  └─ nuevosInformados = Modelo.propagar(nodo, grafo)
│  │  ├─ CalculadorMetricas.calcularMetricasPaso()
│  │  ├─ Exportador.generarJSON()
│  │  └─ Retorna: { paso, nodosActivados[], metricas }
│  │
│  ├─ Frontend recibe JSON:
│  │  ├─ Actualiza colores en vis.js
│  │  ├─ Actualiza métricas en panel
│  │  ├─ Actualiza gráfica de alcance
│  │  └─ Espera según velocidad animación
│  │
│  └─ ¿Hay nodos activos?
│     ├─ Sí → siguiente paso
│     └─ No → Fin de simulación
│
├─ Backend (Final):
│  ├─ CalculadorMetricas.calcularMetricasFinales()
│  ├─ SimulacionDAO.actualizar() - Marca como COMPLETADA
│  ├─ MetricaDAO.guardar() - Guarda métricas finales
│  └─ HistorialDAO.guardar() - Guarda cada evento
│
├─ Frontend (Final):
│  ├─ Muestra resultados finales
│  ├─ Habilita botón "Comparar otros modelos"
│  └─ Permite descargar JSON/CSV
│
└─ FIN
```

---

## Patrones de Diseño Utilizados

### 1. **DAO Pattern (Data Access Object)**
- Aislamiento de lógica de acceso a datos
- Facilita cambio de BD sin afectar lógica
- Clases: `*DAO.java`

### 2. **Strategy Pattern**
- Diferentes estrategias de propagación (3 modelos)
- Interfaz `ModeloPropagacion`
- Implementaciones intercambiables

### 3. **Observer Pattern**
- Frontend observa cambios en backend
- JSON como evento
- REST API como canal de comunicación

### 4. **Builder Pattern** (Opcional)
- Construcción de Grafo complejo
- `GrafoBuilder` → fluent interface

### 5. **Singleton Pattern**
- `ConexionBD` - Una sola conexión activa
- `MotorSimulacion` - Una simulación activa a la vez

---

## Flujo de Datos en Tiempo Real

```
Paso N en Backend:
┌─────────────────────────────┐
│ MotorSimulacion.ejecutarPaso│
└──────────────┬──────────────┘
               │
        ┌──────▼──────┐
        │ Modelo.*    │
        │ .propagar() │
        └──────┬──────┘
               │
        ┌──────▼──────────────┐
        │ UpdateStates()      │
        │ nodosActivos = [...] │
        └──────┬──────────────┘
               │
        ┌──────▼──────────────┐
        │ CalculadorMetricas  │
        │ .calcularPaso()     │
        └──────┬──────────────┘
               │
        ┌──────▼──────────────┐
        │ Exportador          │
        │ .generarJSON()      │
        └──────┬──────────────┘
               │
        ┌──────▼──────────────┐
        │ JSON Response       │
        └──────┬──────────────┘
               │
        ────────────────────────► Frontend JSON
                                   │
                                ┌──▼──────────────┐
                                │ simulator.js    │
                                │ procesarPaso()  │
                                └──┬──────────────┘
                                   │
                        ┌──────────┼──────────────┐
                        │          │              │
                    ┌───▼──┐   ┌───▼──┐      ┌───▼──┐
                    │ vis.js│   │Chart │   UI │Panel │
                    │Update │   │Update│   Update
                    └───────┘   └──────┘      └──────┘
                                   │
                                   ▼
                            Visual Update ✓
```

---

## Configuración del Sistema

### Backend - `config.properties`
```properties
# Database
db.url=jdbc:mysql://localhost:3306/viralsim
db.user=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver

# Simulation
sim.max_pasos=500
sim.red.nodos=250
sim.red.k=6
sim.red.p=0.1

# Server
server.port=8080
server.context=/api
```

### Frontend - `config.js` (a crear)
```javascript
const CONFIG = {
  API_BASE: 'http://localhost:8080/api',
  ANIMATION_SPEED: 500, // ms por paso
  VIS_OPTIONS: { /* vis.js config */ },
  COLORS: {
    NO_INFORMADO: '#3498db',
    INFORMADO_ACTIVO: '#e74c3c',
    INFORMADO_PASIVO: '#f1c40f',
    RESISTENTE: '#95a5a6'
  }
};
```

---

## Consideraciones de Performance

### Backend
- HashMap para O(1) lookups de nodos
- PriorityQueue para procesamiento ordenado
- Batch updates a BD cada N pasos (no cada paso)
- Connection pooling (HikariCP)

### Frontend
- Virtual scrolling para listas
- Debouncing de eventos
- Caché de datos descargados
- Actualización diferencial de grafo

### Red
- JSON compacto (solo cambios, no estado completo)
- Streaming HTTP para múltiples pasos
- Compresión GZIP (si es necesario)

