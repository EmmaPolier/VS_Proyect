# 🎨 Diseño UI Simplificado - ViralSim

Interfaz limpia y enfocada en los datos esenciales.

---

# 🎯 PANTALLA 1: Lanzar Chisme

**Propósito**: Seleccionar nodo semilla y disparar la propagación.

```
┌──────────────────────────────────────┐
│ ViralSim [Lanzar chisme] [...otras] │
├──────────────────────────────────────┤
│                                      │
│ Cargar Grafo:                        │
│ [Seleccionar archivo] [▶ Cargar]    │
│                                      │
│ ó Generar:                           │
│ Nodos: [50▼] Vecinos: [4▼]          │
│ [▶ Generar]                          │
│                                      │
│ ┌─ Seleccionar Nodo Semilla ──────┐ │
│ │ (Grafo interactivo)             │ │
│ │ Click en nodo para seleccionar  │ │
│ │                                 │ │
│ │ Nodo: #42 | Grado: 5           │ │
│ │                                 │ │
│ └─────────────────────────────────┘ │
│                                      │
│ Modelo: [Viral ▼]                   │
│ Probabilidad: [0.3 ◄─────────────►] │
│                                      │
│ [▶ LANZAR CHISME]                   │
│                                      │
└──────────────────────────────────────┘
```

---

# 🎯 PANTALLA 2: Propagación (Dashboard Simplificado)

**Propósito**: Ver propagación en tiempo real. Solo datos esenciales.

```
┌────────────────────────────────────────────────────┐
│ ViralSim [Lanzar chisme] [Propagación] [...]       │
├────────────────────────────────────────────────────┤
│                                                    │
│ ┌──────────────────────┐  ┌─ Paso: 7/18 ────────┐ │
│ │                      │  │                      │ │
│ │  GRAFO ANIMADO       │  │ ● Activo:      13   │ │
│ │  (D3.js)             │  │ ● Pasivo:       2   │ │
│ │                      │  │ ● Resistente:   0   │ │
│ │  🔴 = Activo         │  │ ● No informado: 8   │ │
│ │  🟠 = Pasivo         │  │                      │ │
│ │  ⚫ = Resistente     │  │ Alcance: 63%         │ │
│ │  ⚪ = No informado   │  │ Velocidad: 23p/paso │ │
│ │                      │  │                      │ │
│ │                      │  │ COMPARACIÓN MODELOS │ │
│ │                      │  │ Viral:  86%          │ │
│ │                      │  │ Cascada: 68%         │ │
│ │                      │  │ Threshold: 48%      │ │
│ │                      │  │                      │ │
│ └──────────────────────┘  └──────────────────────┘ │
│                                                    │
│ ┌─ CURVA DE PROGRESIÓN ──────────────────────────┐ │
│ │                                                │ │
│ │ %   100% ┌─────────────────────── Viral        │ │
│ │     80%  │    ╱─────────────── Cascada        │ │
│ │     60%  │   ╱  ╱──────── Threshold           │ │
│ │     40%  │  ╱  ╱                              │ │
│ │     20%  │ ╱  ╱                               │ │
│ │      0%  └─────────────────────── Pasos       │ │
│ │                                                │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ [⏮ Reset] [⏪ Anterior] [⏸] [▶] [⏭ Siguiente]    │
│ Velocidad: [Lento ◄──●──────► Rápido]             │
│                                                    │
│ Modelo: [Viral▼]                                  │
│                                                    │
└────────────────────────────────────────────────────┘
```

---

# 🎯 PANTALLA 3: Editor de Nodos

**Propósito**: Editar atributos antes de simular (opcional).

```
┌──────────────────────────────────────┐
│ ViralSim [...] [Editor nodos]        │
├──────────────────────────────────────┤
│                                      │
│ [🔍 Buscar.....................] │ │
│                                      │
│ ┌─ Tabla de Nodos ────────────────┐ │
│ │ ID | Estado | Prob | Umbral | ✎ │ │
│ ├────┼────────┼──────┼────────┼──┤ │
│ │ 1  | INFO   | 0.3  | 0.5   |   │ │
│ │ 2  | INFO   | 0.4  | 0.5   | ✎ │ │
│ │ 3  | NO     | 0.2  | 0.5   |   │ │
│ │ ... (scroll)                   │ │
│ │ 50 | NO     | 0.3  | 0.5   |   │ │
│ └────────────────────────────────┘ │
│                                      │
│ ┌─ Editar Nodo (al seleccionar) ──┐ │
│ │ Nodo #2                          │ │
│ │ Estado: [NO_INFORMADO ▼]        │ │
│ │ Probabilidad: [0.4 ◄──────────►]│ │
│ │ Umbral: [0.5 ◄──────────►]     │ │
│ │ [Guardar] [Cancelar]            │ │
│ └──────────────────────────────────┘ │
│                                      │
│ [Exportar JSON] [Importar JSON]      │
│                                      │
└──────────────────────────────────────┘
```

---

# ⏱️ Complejidad: Actualización en Tiempo Real

## Respuesta: **2/10 - Muy Simple** ✅

### Por qué es tan fácil:

1. **Backend envía datos cada paso**

   ```javascript
   // Ejemplo respuesta del backend
   GET /api/simulacion/1/estado
   {
     pasoActual: 7,
     nodos: [
       { id: 1, estado: "INFORMADO_ACTIVO" },
       { id: 2, estado: "NO_INFORMADO" },
       ...
     ],
     metricas: {
       activos: 13,
       pasivos: 2,
       resistentes: 0,
       noInformados: 8,
       alcance: 0.63
     }
   }
   ```
2. **Frontend solo actualiza el DOM**

   ```javascript
   // Pseudo-código
   async function actualizarDashboard() {
     const datos = await fetch('/api/simulacion/1/estado');

     // Actualizar contadores (1 línea cada uno)
     document.getElementById('activos').textContent = datos.metricas.activos;
     document.getElementById('pasivos').textContent = datos.metricas.pasivos;
     document.getElementById('alcance').textContent = datos.metricas.alcance;

     // Actualizar colores en grafo (D3.js integrado)
     actualizarGrafo(datos.nodos);

     // Actualizar gráfico (Chart.js integrado)
     actualizarGrafico(datos);
   }
   ```
3. **Se ejecuta cada X milisegundos**

   - Cada botón "siguiente" llama esta función
   - O cada N ms si está en "play" automático
   - Máximo latencia: 100-200ms (imperceptible)

### Librerías que lo hacen trivial:

| Librería            | Tarea                | Dificultad |
| -------------------- | -------------------- | ---------- |
| **Chart.js**   | Actualizar gráfico  | 1/10       |
| **D3.js**      | Colorear nodos       | 2/10       |
| **Vanilla JS** | Actualizar métricas | 1/10       |

---

# 🛠️ Componentes por Pantalla

## Pantalla 1 - Lanzar Chisme

- [ ] Carga de archivo grafo
- [ ] Generador Watts-Strogatz
- [ ] Visualización interactiva nodos
- [ ] Selector modelo (dropdown)
- [ ] Slider probabilidad
- [ ] Botón lanzar

## Pantalla 2 - Propagación (LA MÁS IMPORTANTE)

- [ ] Grafo animado (D3.js)
- [ ] Box métricas (actualización en vivo)
- [ ] Gráfico de progresión (Chart.js)
- [ ] Controles simulación (play/pause/step)
- [ ] Comparación de 3 modelos
- [ ] Slider velocidad

## Pantalla 3 - Editor Nodos

- [ ] Tabla paginada
- [ ] Búsqueda/filtro
- [ ] Panel edición
- [ ] Exportar/importar

---

# 🎨 Colores

```
🔴 INFORMADO_ACTIVO  = Rojo (#FF4444)
🟠 INFORMADO_PASIVO  = Naranja (#FF9944)
⚫ RESISTENTE        = Negro (#222222)
⚪ NO_INFORMADO      = Gris claro (#CCCCCC)

Aristas = Gris oscuro (#666666)
```

---

# 📊 Datos Esenciales Únicamente

### Pantalla 1

- Nodo semilla
- Modelo seleccionado
- Probabilidad

### Pantalla 2

- Paso actual / Total pasos
- Conteos: Activos, Pasivos, Resistentes, No informados
- Alcance (%)
- Velocidad de propagación
- Gráfico de 3 modelos

### Pantalla 3

- ID nodo, Estado, Probabilidad, Umbral

**QUITADO**:

- ❌ Nodos más influyentes (complejidad innecesaria)
- ❌ Historial paso a paso (tabla grande, distrae)
- ❌ Estadísticas adicionales (betweenness, etc)

---

# 🚀 Implementación Recomendada

## Semana 2-3: Orden de Prioridad

1. ✅ **Pantalla 2** (Propagación) - La más importante

   - Grafo + Métricas vivas = 80% del valor
2. ✅ **Pantalla 1** (Lanzar) - Necesaria para iniciar

   - Setup antes de la simulación
3. ✅ **Pantalla 3** (Editor) - Nice to have

   - Útil pero no crítica

## Tech Stack

| Componente | Librería         | Razón                              |
| ---------- | ----------------- | ----------------------------------- |
| Grafo      | D3.js             | Mejor para redes grandes            |
| Gráfico   | Chart.js          | Simple, actualización rápida      |
| Tablas     | DataTables        | Paginación + búsqueda automática |
| Sliders    | HTML5 input range | Nativo, sin dependencias            |
| Tabs       | Vanilla JS        | Solo 3 pestañas, fácil            |
| Estilos    | CSS vanilla       | Responsive simple                   |

---

# 💡 Ventajas del Diseño Simplificado

✅ Interfaz clara, sin ruido
✅ Rápido de cargar
✅ Fácil de actualizar en tiempo real
✅ Datos importantes siempre visibles
✅ Responsivo en móvil (si es necesario)
✅ Bajo overhead de rendering

---

# 📱 Responsive Breakpoints

| Pantalla                   | < 768px                           | > 768px           |
| -------------------------- | --------------------------------- | ----------------- |
| **1 - Lanzar**       | Stack vertical                    | Layout horizontal |
| **2 - Propagación** | Grafo 100% ancho, métricas abajo | Lado a lado       |
| **3 - Editor**       | Tabla con scroll                  | Tabla normal      |

---

# ✨ Animaciones Mínimas

```css
/* Suave transición entre pasos */
.metrica {
  transition: color 0.3s ease;
}

/* Grafo se redibuja sin parpadeo */
.nodo {
  transition: fill 0.2s ease;
}

/* Gráfico se anima suavemente */
.chart-update {
  animation: slideIn 0.5s ease;
}
```
