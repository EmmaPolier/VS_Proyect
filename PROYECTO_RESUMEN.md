# Resumen Visual - Estructura del Proyecto ViralSim

## 📁 Árbol de Directorios

```
ED_Proyect/
│
├── 📄 Documentación (Raíz - Información General)
│  ├─ PROYECTO_RESUMEN.md .................. Índice general y visual
│  ├─ REFERENCIA_RAPIDA.md ............... Guía de referencia rápida
│  ├─ STRUCTURE.md ....................... Árbol de directorios
│  └─ ESTADO_DEL_PROYECTO.md ............ Estado actual del proyecto
│
├── 📄 Especificaciones
│  ├─ ViralSim_Entregable1.docx .......... Especificaciones del proyecto
│  └─ viralsim_research_guide.html ....... Guía de investigación
│
├── 📂 VS_Proyect/ 🎯 ..................... CÓDIGO Y DESARROLLO
│  │
│  ├── 📂 backend/ ....................... Motor de simulación Java
│  │   ├── pom.xml
│  │   └── src/main/java/com/viralsim/
│  │       ├── models/            (Nodo, Arista, Grafo, Estado)
│  │       ├── propagation/       (Modelos: Viral, Cascada, Threshold)
│  │       ├── engine/            (MotorSimulacion)
│  │       ├── metrics/           (Calculador de Métricas)
│  │       ├── database/          (DAOs)
│  │       └── utils/             (Generadores)
│  │
│  ├── 📂 frontend/ ..................... Interfaz web HTML/JS
│  │   ├── index.html .................. Página principal
│  │   ├── css/ ........................ Estilos
│  │   ├── js/  ........................ Lógica JavaScript
│  │   └── assets/  ................... Recursos estáticos
│  │
│  ├── 📂 database/ .................... Scripts de BD
│  │   ├── schema.sql ................. Creación de tablas
│  │   └── seed-data.sql ............. Datos iniciales
│  │
│  └── 📂 docs/ ........................ Documentación Técnica
│      ├── BACKEND.md ................. Especificación de clases Java
│      ├── DATABASE.md ................ Esquema y relaciones SQL
│      ├── FRONTEND.md ................ Componentes UI
│      └── ARCHITECTURE.md ............ Diagrama general del sistema
│
├── 📂 code/ (legado) .................... Archivos Java existentes
│   └── Dijkstra.java, Graph.java, etc.
│
└── 📂 .venv/ ........................... Entorno virtual Python
```

---

## 🏗️ Componentes Clave por Responsabilidad

### **MODELOS** (com.viralsim.models)
```
Estado.java
  └─ Enumeración: NO_INFORMADO, INFORMADO_ACTIVO, 
                  INFORMADO_PASIVO, RESISTENTE

Nodo.java
  ├─ id, estado, pasoInfeccion
  ├─ probabilidadPropagacion, umbral
  ├─ vecinos, padre
  └─ centralidad (grado, betweenness)

Arista.java
  ├─ nodoOrigen, nodoDestino
  ├─ probabilidadArista
  ├─ activa (para modelo Cascada)
  └─ peso

Grafo.java
  ├─ List<Nodo> nodos
  ├─ List<Arista> aristas
  ├─ HashMap<Integer, Nodo> mapaNodos (O(1) lookup)
  └─ Métodos de acceso
```

### **PROPAGACIÓN** (com.viralsim.propagation)
```
ModeloPropagacion (interfaz)
  └─ propagar(grafo, nodo): List<Nodo>

ModeloViral
  └─ Cada nodo contagia con su probabilidad

ModeloCascadaIndependiente
  └─ Probabilidad en aristas, un intento

ModeloUmbralLineal
  └─ Contagio por presión social (%)
```

### **MOTOR** (com.viralsim.engine)
```
MotorSimulacion
  ├─ iniciarSimulacion(nodo, modelo)
  ├─ ejecutarPaso(): boolean
  ├─ detenerSimulacion()
  └─ obtenerResultados()

EventoSimulacion
  ├─ paso, nodo
  ├─ estadoAnterior, estadoNuevo
  └─ timestamp

SimulacionResultado
  ├─ simulacionId, modelo, nodoOrigen
  ├─ pasoFinal, alcanceTotal, paso50%
  └─ estadosFinales, historialCompleto
```

### **MÉTRICAS** (com.viralsim.metrics)
```
CalculadorMetricas
  ├─ calcularAlcance(): double
  ├─ calcularAlcancePorEstado(): Map
  ├─ obtenerNodosMasInfluyentes(n)
  └─ calcularMetricasPaso(): Map

MetricaPaso
  ├─ paso, alcance
  ├─ nodosInformados, nodosActivos
  └─ nodosResistentes
```

### **BASE DE DATOS** (com.viralsim.database)
```
ConexionBD
  └─ Gestión de conexión MySQL

*DAO.java (6 clases)
  ├─ NodoDAO (CRUD nodos)
  ├─ AristaDAO (CRUD aristas)
  ├─ SimulacionDAO (CRUD simulaciones)
  ├─ HistorialDAO (INSERT historial)
  ├─ MetricaDAO (INSERT/SELECT métricas)
  └─ Patrón: crear(), obtener*(), actualizar(), eliminar()
```

### **UTILIDADES** (com.viralsim.utils)
```
GeneradorAleatorios
  └─ numeroAleatorio(), booleanoAleatorio(), seleccionar()

GeneradorWattsStrogatz
  └─ generar(): Grafo (250 nodos, k=6, p=0.1)

GeneradorBetweenness
  └─ Usa JGraphT para algoritmo de Brandes

Exportador
  └─ generarJSON(): String (para enviar al frontend)
```

---

## 📊 Flujo de Datos del Sistema

```
FRONTEND (index.html)
    ├─ main.js
    │   ├─ api-client.js (HTTP → Backend)
    │   ├─ simulator.js (Procesa pasos)
    │   ├─ visualization.js (vis.js)
    │   └─ ui-controller.js (Eventos)
    └─ CSS (style.css + responsive.css)
         ↓↑ JSON over REST
         
BACKEND (Java)
    ├─ Controlador (recibe POST /api/simulacion/iniciar)
    │   ├─ MotorSimulacion.iniciarSimulacion()
    │   │   ├─ GeneradorWattsStrogatz.generar()
    │   │   └─ GeneradorBetweenness.calcularBetweenness()
    │   │
    │   └─ Bucle (ejecutarPaso x N veces)
    │       ├─ Modelo*.propagar() → nuevosNodos
    │       ├─ CalculadorMetricas.calcularPaso() → métricas
    │       ├─ Exportador.generarJSON() → respuesta
    │       └─ [BD] Guardar eventos
    │
    └─ Base de Datos (MySQL)
         ├─ nodos
         ├─ aristas
         ├─ simulaciones
         ├─ historial_infeccion
         └─ metricas
```

---

## 🗄️ Tablas de Base de Datos

| Tabla | Registros | Propósito |
|-------|-----------|-----------|
| `nodos` | 250 | Usuarios de la red |
| `aristas` | ~750 | Conexiones entre usuarios |
| `simulaciones` | Variable | Metadata de cada ejecución |
| `historial_infeccion` | Miles | Registro paso a paso (animación) |
| `metricas` | 3×N | Snapshot por paso |

---

## 🎨 Interfaz Frontend - Componentes

```
┌─────────────────────────────────────────────────────┐
│  Logo                  VIRALSIM                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │
│  │   PANEL DE   │  │    ÁREA DE    │  │  PANEL   │ │
│  │   CONTROL    │  │  VISUALIZACIÓN│  │   DE     │ │
│  │              │  │      (vis.js)  │  │ MÉTRICAS │ │
│  │ • Selector   │  │                 │  │          │ │
│  │   Modelo     │  │   [250 nodos]   │  │ • Alcance│ │
│  │              │  │                 │  │ • Paso   │ │
│  │ • Nodo Orig. │  │                 │  │ • Gráfica│ │
│  │              │  │                 │  │          │ │
│  │ • Velocidad  │  │   (animación)   │  │ • Top 5  │ │
│  │              │  │                 │  │   nodos  │ │
│  │ • Botones    │  │                 │  │          │ │
│  │  [Iniciar]   │  │                 │  │          │ │
│  │  [Pausar]    │  │                 │  │          │ │
│  │  [Continuar] │  │                 │  │          │ │
│  └──────────────┘  └──────────────┘  └──────────┘ │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Paleta de Colores
- 🔵 **Azul** (#3498db) - NO_INFORMADO
- 🟠 **Naranja** (#e74c3c) - INFORMADO_ACTIVO (con brillo)
- 🟡 **Amarillo** (#f1c40f) - INFORMADO_PASIVO
- ⚫ **Gris** (#95a5a6) - RESISTENTE

---

## 📋 Checklist de Implementación

### Backend
- [ ] Crear estructura de paquetes
- [ ] Implementar clases de modelos
- [ ] Implementar interfaz y 3 modelos
- [ ] Implementar motor de simulación
- [ ] Implementar calculador de métricas
- [ ] Implementar DAOs
- [ ] Implementar controlador REST
- [ ] Crear schema.sql
- [ ] Implementar generadores

### Frontend
- [ ] Crear index.html
- [ ] Crear style.css + responsive.css
- [ ] Implementar main.js
- [ ] Implementar api-client.js
- [ ] Implementar simulator.js
- [ ] Implementar visualization.js
- [ ] Implementar ui-controller.js
- [ ] Integrar vis.js
- [ ] Integrar Chart.js

### Base de Datos
- [ ] Crear schema.sql con 5 tablas
- [ ] Definir índices y relaciones
- [ ] Crear usuario MySQL
- [ ] Configurar connection pooling

### Testing
- [ ] Test unitarios (backend)
- [ ] Test de integración (BD)
- [ ] Test funcional (frontend)
- [ ] Test de carga (3 modelos simultáneos)

---

## 📚 Documentación Relacionada

- [STRUCTURE.md](./STRUCTURE.md) - Este documento
- [docs/BACKEND.md](./docs/BACKEND.md) - Detalles de clases Java
- [docs/DATABASE.md](./docs/DATABASE.md) - Esquema y relaciones SQL
- [docs/FRONTEND.md](./docs/FRONTEND.md) - Componentes y flujos UI
- [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) - Diagrama general del sistema

---

## 🚀 Próximos Pasos

1. **Revisar documentación** de especificaciones (ViralSim_Entregable1.docx)
2. **Crear estructura de directorios** (ya hecho ✓)
3. **Escribir interfaces y clases base** (sin implementación)
4. **Diseñar base de datos** (schema.sql)
5. **Implementar modelos de datos**
6. **Implementar motor de simulación**
7. **Implementar acceso a datos**
8. **Crear frontend básico**
9. **Integrar backend + frontend**
10. **Testing y ajustes**

