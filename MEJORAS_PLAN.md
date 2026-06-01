# 📋 PLAN DE MEJORAS - ViralSim

## PROBLEMA 1: Visualización del Grafo (Telaraña roja)
**Causa:** El grafo se genera con posiciones aleatorias, sin layout fuerza-dirigido de D3

### Síntomas:
- Nodos distribuidos caóticamente sin estructura
- No se ve la topología Watts-Strogatz (pequeño-mundo)
- Difícil identificar clusters o estructura de red

### Solución:
Implementar **D3 Force Simulation** con parámetros optimizados:
- Repulsión de nodos (muchos charge)
- Distancia ideal entre nodos (links)
- Separación mínima (collide)
- Gravedad hacia el centro

---

## PROBLEMA 2: Controles de Nodo (No permite cambiar valores)
**Causa:** Los valores se reinician al lanzar simulación

### Síntomas:
- Modificas un nodo (ej: prob=80%, resistencia=10%)
- Presionas "Lanzar"
- Los valores vuelven a los anteriores (prob=60%, resistencia=24%)

### Causas Potenciales:
1. `resetSimulation()` reinicia las propiedades de los nodos a valores default
2. Los valores guardados en `selectedNode` no persisten
3. Los valores que cargas desde API se sobrescriben al lanzar

### Solución:
- **Separar** reset de estados vs reset de propiedades
- Guardar los valores **antes** de lanzar (si no están guardados en BD)
- Mantener valores de sliders sin cambiar durante la simulación
- Agregar validación: si nodo no está guardado, mostrar alerta

---

## PASOS A EJECUTAR

### PASO 1: Mejorar Visualización del Grafo
**Archivo:** `frontend/js/app.js`

**Cambios:**
1. Reemplazar inicialización de posiciones random por D3 Force Layout
2. Agregar parámetros de fuerza optimizados para red de 250 nodos
3. Permitir que el layout se estabilice antes de renderizar
4. Mostrar skeleton/loader mientras se estabiliza

**Código a cambiar:**
```javascript
// ANTES (línea ~70-85):
nodes = nodosAPI.map(nodo => ({
  id: nodo.id,
  x: Math.random() * width,  // ❌ PROBLEMA
  y: Math.random() * height, // ❌ PROBLEMA
  ...
}));

// DESPUÉS:
// Inicializar con posiciones cualquiera, D3 force las calculará
nodes = nodosAPI.map(nodo => ({
  id: nodo.id,
  x: width / 2,
  y: height / 2,
  ...
}));

// Luego crear force simulation
const forceSimulation = d3.forceSimulation(nodes)
  .force('link', d3.forceLink(links).distance(80))
  .force('charge', d3.forceManyBody().strength(-300))
  .force('center', d3.forceCenter(width / 2, height / 2))
  .force('collision', d3.forceCollide().radius(20));
```

---

### PASO 2: Separar Estados vs Propiedades de Nodos
**Archivo:** `frontend/js/app.js`

**Cambios en `resetSimulation()`:**
1. Solo reiniciar `state` e `isSource`
2. NO reiniciar `propagationProb`, `resistance`, `threshold`, `name`
3. Agregar comentario clarificador

**Código a cambiar (línea ~490-510):**
```javascript
// ANTES:
function resetSimulation() {
  stopTimer();
  simulation.step = 0;
  simulation.series = { viral: [0], cascade: [0], threshold: [0] };
  nodes.forEach(node => {
    node.state = 'uninformed';     // ✓ Correcto
    node.isSource = false;          // ✓ Correcto
    // ⚠️ Aquí NO se reinician propagationProb, resistance, threshold
    // porque resetSimulation() no debería tocarlas
  });
}

// DESPUÉS: (mismo código, pero con claridad)
function resetSimulation() {
  // IMPORTANTE: Solo reinicia ESTADO de la simulación
  // NO reinicia PROPIEDADES de nodos (probabilidad, resistencia, umbral)
  stopTimer();
  simulation.step = 0;
  simulation.series = { viral: [0], cascade: [0], threshold: [0] };
  nodes.forEach(node => {
    node.state = 'uninformed';      // Reset solo estado
    node.isSource = false;
    // ✓ Mantiene: propagationProb, resistance, threshold, name
  });
  ...
}
```

---

### PASO 3: Validar Valores Antes de Lanzar
**Archivo:** `frontend/js/app.js`

**Cambios en `triggerLaunch()`:**
1. Verificar si el nodo tiene valores válidos
2. Si no fueron guardados en BD, mostrar alerta de advertencia
3. Permitir continuar o descartar cambios

**Código a cambiar (línea ~320-340):**
```javascript
// ANTES:
async function triggerLaunch() {
  const seed = Number(seedInput.value);
  if (!Number.isInteger(seed) || seed < 1 || seed > nodes.length) return;
  const seedNode = nodes.find(n => n.id === seed);
  if (!seedNode) return;

  resetSimulation();  // ❌ Se pierden cambios no guardados
  // ...
}

// DESPUÉS:
async function triggerLaunch() {
  const seed = Number(seedInput.value);
  if (!Number.isInteger(seed) || seed < 1 || seed > nodes.length) return;
  const seedNode = nodes.find(n => n.id === seed);
  if (!seedNode) return;

  // Verificar si hay cambios sin guardar
  if (selectedNode && 
      (selectedNode.propagationProb != propagationProb.value ||
       selectedNode.resistance != resistance.value ||
       selectedNode.threshold != socialThreshold.value)) {
    const confirm = window.confirm(
      'Hay cambios sin guardar en este nodo.\n' +
      'Haz clic en "Guardar nodo" primero.\n\n' +
      '¿Descartar cambios y continuar?'
    );
    if (!confirm) return;
  }

  resetSimulation();
  // ... resto del código
}
```

---

### PASO 4: Agregar Feedback Visual
**Archivo:** `frontend/network-visualization.html` y CSS

**Cambios:**
1. Mostrar indicador "Estabilizando grafo..." mientras force simulation corre
2. Deshabilitar botones durante estabilización
3. Cambiar color de botón "Guardar nodo" cuando hay cambios no guardados

**HTML a agregar (antes del SVG del grafo):**
```html
<div class="graph-stage">
  <div class="graph-loading" id="graph-loading">
    <div class="spinner"></div>
    <p>Estabilizando grafo...</p>
  </div>
  <svg id="graph" viewBox="0 0 1920 1080" ...></svg>
  ...
</div>
```

**CSS a agregar:**
```css
.graph-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.7);
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  z-index: 10;
}

.spinner {
  border: 4px solid #ddd;
  border-top: 4px solid #ff3b54;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

#save-node.unsaved {
  background: #ffa500 !important;
  animation: pulse 1s infinite;
}
```

---

### PASO 5: Mejorar Event Listeners para Detectar Cambios
**Archivo:** `frontend/js/app.js`

**Cambios en `setupEventListeners()`:**
1. Marcar botón "Guardar nodo" como "pendiente" cuando hay cambios
2. Deshabilitar "Lanzar" si hay cambios sin guardar (opcional, o solo advertir)

**Código a agregar:**
```javascript
function setupEventListeners() {
  // ... código existente ...

  // Detectar cambios en sliders
  const markUnsaved = () => {
    if (saveNode && selectedNode) {
      const hasChanges = 
        selectedNode.propagationProb != propagationProb.value ||
        selectedNode.resistance != resistance.value ||
        selectedNode.threshold != socialThreshold.value ||
        selectedNode.name != nodeName.value;
      
      if (hasChanges) {
        saveNode.classList.add('unsaved');
        saveNode.textContent = '💾 Guardar nodo *';
      } else {
        saveNode.classList.remove('unsaved');
        saveNode.textContent = 'Guardar nodo';
      }
    }
  };

  if (propagationProb) propagationProb.addEventListener('input', () => {
    if (propagationProbValue) propagationProbValue.textContent = `${propagationProb.value}%`;
    markUnsaved();
  });
  if (resistance) resistance.addEventListener('input', () => {
    if (resistanceValue) resistanceValue.textContent = `${resistance.value}%`;
    markUnsaved();
  });
  if (socialThreshold) socialThreshold.addEventListener('input', () => {
    if (socialThresholdValue) socialThresholdValue.textContent = `${socialThreshold.value}%`;
    markUnsaved();
  });
  if (nodeName) nodeName.addEventListener('input', markUnsaved);
  
  // ... resto del código ...
}
```

---

## RESUMEN DE CAMBIOS

| Paso | Archivo | Cambio | Impacto |
|------|---------|--------|--------|
| 1 | app.js | Agregar D3 Force Simulation | ✅ Grafo visibles de verdad |
| 2 | app.js | `resetSimulation()` no toca propiedades | ✅ Valores se mantienen |
| 3 | app.js | Validar antes de lanzar | ✅ Advertencia de cambios |
| 4 | HTML+CSS | Loader visual + indicadores | ✅ UX mejorado |
| 5 | app.js | Detectar cambios no guardados | ✅ Botón marca estado |

---

## ¿LISTO PARA IMPLEMENTAR?

✅ Este plan:
- Soluciona visibilidad del grafo (layout fuerza-dirigido)
- Soluciona pérdida de valores (no reinicia propiedades)
- Mejora UX (feedback visual, validaciones)
- No requiere cambios en backend
- Es reversible (sin commits a BD)

**Próximo paso:** ¿Debo implementar todos los pasos? ¿O prefieres que empiece por uno específico?
