/*
  app.js
  Lógica de la simulación, construcción del grafo, control de UI y animación.
  Conectado con backend via APIService (api-service.js)
*/

// Variables globales iniciales
let grafoId = null;
let modeloSeleccionado = 'viral'; // Modelo por defecto
let nodes = [];
let links = [];
let adjacency = [];
let selectedNode = null;
let forceSimulation = null;
let lastSaveNotification = null; // Para controlar notificaciones de auto-save
let simulationHistory = []; // PASO 4: Historial de simulaciones

// Inicializar la aplicación de forma asíncrona
async function initializeApp() {
  try {
    // Inicializar referencias del DOM
    seedInput = document.getElementById('seed-input');
    launchButton = document.getElementById('launch-button');
    btnLaunchTop = document.getElementById('btn-launch');
    createGraphButton = document.getElementById('create-graph-button');
    grafoSelect = document.getElementById('grafo-select');
    loadGraphButton = document.getElementById('load-graph-button');
    modelButtons = document.querySelectorAll('.model-btn');
    pauseButton = document.getElementById('pause-btn');
    resetButton = document.getElementById('reset-btn');
    stepButton = document.getElementById('step-btn');
    speedButtons = document.querySelectorAll('.speed-btn');
    saveNode = document.getElementById('save-node');
    discardNode = document.getElementById('discard-node');
    nodeName = document.getElementById('node-name');
    nodeId = document.getElementById('node-id');
    nodeDegree = document.getElementById('node-degree');
    propagationProb = document.getElementById('propagation-prob');
    propagationProbValue = document.getElementById('propagation-prob-value');
    resistance = document.getElementById('resistance');
    resistanceValue = document.getElementById('resistance-value');
    socialThreshold = document.getElementById('social-threshold');
    socialThresholdValue = document.getElementById('social-threshold-value');
    initialState = document.getElementById('initial-state');
    metricReach = document.getElementById('metric-reach');
    metricStep = document.getElementById('metric-step');
    metricSpeed = document.getElementById('metric-speed');
    metricInformed = document.getElementById('metric-informed');
    metricStatus = document.getElementById('metric-status');
    barActive = document.getElementById('bar-active');
    barPassive = document.getElementById('bar-passive');
    barResistant = document.getElementById('bar-resistant');
    barUninformed = document.getElementById('bar-uninformed');
    bottomStep = document.getElementById('bottom-step');
    bottomTotal = document.getElementById('bottom-total');
    chartSvg = d3.select('#line-chart');

    // Cargar lista de grafos disponibles
    await cargarListaGrafos();

    // PASO 4: Cargar historial de simulaciones desde localStorage
    loadSimulationHistory();

    // Conectar eventos de usuario
    setupEventListeners();

    console.log('Aplicación inicializada. Esperando que el usuario seleccione un grafo.');
  } catch (error) {
    console.error('Error inicializando aplicación:', error);
    alert('Error cargando datos del backend. Revisa la consola.');
  }
}

const width = 1920;
const height = 1080;

/* Configuración del lienzo SVG y el grupo principal donde se dibuja el grafo */
const svg = d3.select('#graph');
const canvas = svg.append('g').attr('class', 'canvas');

const defs = svg.append('defs');
defs.append('filter')
  .attr('id', 'glow')
  .append('feGaussianBlur')
  .attr('stdDeviation', '2.4')
  .attr('result', 'coloredBlur');

defs.append('filter')
  .attr('id', 'shadow')
  .attr('x', '-50%')
  .attr('y', '-50%')
  .attr('width', '200%')
  .attr('height', '200%')
  .append('feGaussianBlur')
  .attr('stdDeviation', '2')
  .attr('result', 'blurOut');

let link, node;

/* Referencias a elementos del DOM para controlar la UI de la simulación */
let seedInput, launchButton, btnLaunchTop, createGraphButton, modelButtons, pauseButton, resetButton, stepButton, speedButtons;
let saveNode, discardNode, nodeName, nodeId, nodeDegree, propagationProb, propagationProbValue;
let resistance, resistanceValue, socialThreshold, socialThresholdValue, initialState;
let metricReach, metricStep, metricSpeed, metricInformed, metricStatus;
let barActive, barPassive, barResistant, barUninformed, bottomStep, bottomTotal, chartSvg;
let grafoSelect, loadGraphButton;

/* Configuración de la gráfica que muestra la evolución de la simulación */
const chartConfig = {
  width: 520,
  height: 220,
  margin: { top: 16, right: 18, bottom: 20, left: 24 }
};

/* Estado global de la simulación */
const simulation = {
  active: false,
  step: 0,
  totalSteps: 10,
  speed: 1,
  timer: null,
  series: {
    viral: [0],
    cascade: [0],
    threshold: [0]
  }
};

// Inicialización después de que el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
  initializeApp();
});

// Conectar eventos de usuario a las funciones de control
function setupEventListeners() {
  if (launchButton) launchButton.addEventListener('click', () => triggerLaunch());
  if (btnLaunchTop) btnLaunchTop.addEventListener('click', () => triggerLaunch());
  if (createGraphButton) createGraphButton.addEventListener('click', createNewGraph);
  if (loadGraphButton) loadGraphButton.addEventListener('click', () => {
    const grafoIdSeleccionado = grafoSelect.value;
    if (grafoIdSeleccionado) {
      cargarGrafo(parseInt(grafoIdSeleccionado));
    } else {
      alert('Por favor selecciona un grafo');
    }
  });
  if (pauseButton) pauseButton.addEventListener('click', togglePause);
  if (resetButton) resetButton.addEventListener('click', resetSimulation);
  if (stepButton) stepButton.addEventListener('click', stepSimulation);

  speedButtons.forEach(button => {
    button.addEventListener('click', () => {
      speedButtons.forEach(btn => btn.classList.remove('active'));
      button.classList.add('active');
      simulation.speed = Number(button.dataset.speed);
      if (metricSpeed) metricSpeed.textContent = `x${simulation.speed}`;
      if (simulation.active) startTimer();
    });
  });

  // Event listeners para botones de selección de modelo
  modelButtons.forEach(button => {
    button.addEventListener('click', () => {
      modelButtons.forEach(btn => btn.classList.remove('active'));
      button.classList.add('active');
      modeloSeleccionado = button.dataset.model;
      console.log('Modelo seleccionado:', modeloSeleccionado);
      
      // Reiniciar simulación cuando cambia el modelo
      resetSimulation();
    });
  });

  // Función para detectar cambios y marcar como pendientes
  const markUnsaved = () => {
    if (!selectedNode || !saveNode) return;
    
    const hasChanges = 
      selectedNode.propagationProb != propagationProb.value ||
      selectedNode.resistance != resistance.value ||
      selectedNode.threshold != socialThreshold.value ||
      selectedNode.name != nodeName.value;
    
    if (hasChanges) {
      saveNode.classList.add('unsaved');
      saveNode.textContent = '💾 Guardar *';
    } else {
      saveNode.classList.remove('unsaved');
      saveNode.textContent = 'Guardar nodo';
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

  if (saveNode) saveNode.addEventListener('click', async () => {
    if (!selectedNode) return;
    console.log('Guardando nodo', selectedNode.id);
    
    selectedNode.name = nodeName.value || selectedNode.name;
    selectedNode.propagationProb = Number(propagationProb.value);
    selectedNode.resistance = Number(resistance.value);
    selectedNode.threshold = Number(socialThreshold.value);
    selectedNode.state = initialState.value;

    try {
      // Persistir cambios en el backend
      await APIService.actualizarNodo(selectedNode.id, {
        nombre: selectedNode.name,
        probabilidad: selectedNode.propagationProb,
        resistencia: selectedNode.resistance,
        umbral: selectedNode.threshold,
        estado: selectedNode.state
      });
      console.log('Nodo actualizado en el backend');
    } catch (error) {
      console.error('Error guardando nodo:', error);
    }

    updateGraphState();
    selectNode(selectedNode);
  });

  if (discardNode) discardNode.addEventListener('click', () => selectNode(selectedNode));

  // PASO 4: Crear botón para ver historial de simulaciones
  const historyButton = document.createElement('button');
  historyButton.textContent = '📊 Historial';
  historyButton.style.cssText = `
    position: fixed;
    bottom: 20px;
    right: 20px;
    padding: 12px 16px;
    background: linear-gradient(135deg, rgba(255,193,7,0.9), rgba(255,213,79,0.85));
    color: #000;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    z-index: 999;
    box-shadow: 0 4px 12px rgba(255,193,7,0.3);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  `;
  historyButton.onmouseover = () => {
    historyButton.style.transform = 'translateY(-2px)';
    historyButton.style.boxShadow = '0 6px 16px rgba(255,193,7,0.4)';
  };
  historyButton.onmouseout = () => {
    historyButton.style.transform = '';
    historyButton.style.boxShadow = '0 4px 12px rgba(255,193,7,0.3)';
  };
  historyButton.onclick = () => showSimulationHistory();
  document.body.appendChild(historyButton);

  // PASO 5: Crear botones para exportar resultados
  const exportGrafoButton = document.createElement('button');
  exportGrafoButton.textContent = '📸 PNG';
  exportGrafoButton.style.cssText = `
    position: fixed;
    bottom: 70px;
    right: 20px;
    padding: 12px 16px;
    background: linear-gradient(135deg, rgba(76,175,80,0.9), rgba(102,187,106,0.85));
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    z-index: 999;
    box-shadow: 0 4px 12px rgba(76,175,80,0.3);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  `;
  exportGrafoButton.title = 'Descargar grafo como PNG';
  exportGrafoButton.onmouseover = () => {
    exportGrafoButton.style.transform = 'translateY(-2px)';
    exportGrafoButton.style.boxShadow = '0 6px 16px rgba(76,175,80,0.4)';
  };
  exportGrafoButton.onmouseout = () => {
    exportGrafoButton.style.transform = '';
    exportGrafoButton.style.boxShadow = '0 4px 12px rgba(76,175,80,0.3)';
  };
  exportGrafoButton.onclick = () => exportGraphAsPNG();
  document.body.appendChild(exportGrafoButton);

  const exportMetricasButton = document.createElement('button');
  exportMetricasButton.textContent = '📊 CSV';
  exportMetricasButton.style.cssText = `
    position: fixed;
    bottom: 120px;
    right: 20px;
    padding: 12px 16px;
    background: linear-gradient(135deg, rgba(33,150,243,0.9), rgba(66,165,245,0.85));
    color: white;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 600;
    font-size: 14px;
    z-index: 999;
    box-shadow: 0 4px 12px rgba(33,150,243,0.3);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  `;
  exportMetricasButton.title = 'Descargar métricas como CSV';
  exportMetricasButton.onmouseover = () => {
    exportMetricasButton.style.transform = 'translateY(-2px)';
    exportMetricasButton.style.boxShadow = '0 6px 16px rgba(33,150,243,0.4)';
  };
  exportMetricasButton.onmouseout = () => {
    exportMetricasButton.style.transform = '';
    exportMetricasButton.style.boxShadow = '0 4px 12px rgba(33,150,243,0.3)';
  };
  exportMetricasButton.onclick = () => exportMetricsAsCSV();
  document.body.appendChild(exportMetricasButton);

  // Nota: Los checkboxes de modelo en la gráfica ya no se usan porque solo mostramos el modelo seleccionado
}

/* Conexión de eventos de usuario a las funciones de control de la simulación */
function renderGraph() {
  if (nodes.length === 0) return;

  // Actualizar enlaces
  link = canvas.selectAll('.link-layer').data([null]).join('g').attr('class', 'link-layer');
  link = link.selectAll('path').data(links, (d, i) => i);
  link.exit().remove();
  link = link.enter()
    .append('path')
    .attr('class', d => {
      const sx = d.source.x;
      const sy = d.source.y;
      const tx = d.target.x;
      const ty = d.target.y;
      const dx = tx - sx;
      const dy = ty - sy;
      const distance = Math.sqrt(dx * dx + dy * dy);
      const isLongLink = distance >= 200;
      return 'link' + (isLongLink ? ' long-range' : '');
    })
    .merge(link);

  link.attr('d', d => getCurvePath(d))
      .attr('class', d => {
        const sx = d.source.x;
        const sy = d.source.y;
        const tx = d.target.x;
        const ty = d.target.y;
        const dx = tx - sx;
        const dy = ty - sy;
        const distance = Math.sqrt(dx * dx + dy * dy);
        const isLongLink = distance >= 200;
        return 'link' + (isLongLink ? ' long-range' : '');
      });

  // Actualizar nodos
  node = canvas.selectAll('.node-layer').data([null]).join('g').attr('class', 'node-layer');
  node = node.selectAll('g').data(nodes, d => d.id);
  node.exit().remove();
  
  const nodeEnter = node.enter()
    .append('g')
    .attr('class', d => `node ${d.state}${d.isSource ? ' source' : ''}`)
    .call(d3.drag()
      .on('start', dragstarted)
      .on('drag', dragged)
      .on('end', dragended))
    .on('click', (event, d) => {
      selectNode(d);
    });

  nodeEnter.append('circle').attr('r', 10);
  nodeEnter.append('text')
    .text(d => d.id)
    .attr('font-family', 'Inter, Segoe UI, sans-serif')
    .attr('font-size', 9)
    .attr('fill', 'rgba(255,255,255,0.92)')
    .attr('pointer-events', 'none');

  node = nodeEnter.merge(node);

  node.attr('transform', d => `translate(${d.x},${d.y})`)
      .attr('class', d => `node ${d.state}${d.isSource ? ' source' : ''}`);
}

function getCurvePath(d) {
  const sx = d.source.x;
  const sy = d.source.y;
  const tx = d.target.x;
  const ty = d.target.y;
  const dx = tx - sx;
  const dy = ty - sy;
  const distance = Math.sqrt(dx * dx + dy * dy);
  
  // Calcular si es un link corto (lateral, entre vecinos) o largo (diagonal)
  // Links cortos: distancia < 200 (vecinos en el anillo)
  // Links largos: distancia >= 200 (cruzan el círculo)
  const isShortLink = distance < 200;
  
  if (isShortLink) {
    // Links LATERALES: Curvatura PRONUNCIADA hacia afuera del grafo
    // Mayor control points offset para crear curva visible hacia afuera
    const offsetMultiplier = 0.15; // Mayor que antes para más curvatura
    const qx = sx + dx * 0.35 - dy * offsetMultiplier;
    const qy = sy + dy * 0.35 + dx * offsetMultiplier;
    const rx = sx + dx * 0.65 - dy * offsetMultiplier;
    const ry = sy + dy * 0.65 + dx * offsetMultiplier;
    return `M${sx},${sy}C${qx},${qy} ${rx},${ry} ${tx},${ty}`;
  } else {
    // Links DIAGONALES: Curvatura suave pero visible
    // Estos cruzan el círculo con arco elegante
    const offsetMultiplier = 0.25; // Curvatura más suave para links largos
    const midX = (sx + tx) / 2;
    const midY = (sy + ty) / 2;
    // Offset perpendicular al link para crear la curvatura
    const perpX = -dy / distance * (distance * offsetMultiplier);
    const perpY = dx / distance * (distance * offsetMultiplier);
    const qx = midX + perpX * 0.5;
    const qy = midY + perpY * 0.5;
    return `M${sx},${sy}Q${qx},${qy} ${tx},${ty}`;
  }
}

/* PASO 3: Guarda automáticamente el nodo actualmente seleccionado antes de cambiar a otro */
async function saveCurrentNode() {
  if (!selectedNode) return;
  
  // Comparar valores actuales con los del nodo
  const hasChanges = 
    selectedNode.name !== nodeName.value ||
    selectedNode.propagationProb !== Number(propagationProb.value) ||
    selectedNode.resistance !== Number(resistance.value) ||
    selectedNode.threshold !== Number(socialThreshold.value) ||
    selectedNode.state !== initialState.value;
  
  if (!hasChanges) return; // Sin cambios, no guardar
  
  // Actualizar valores en el objeto nodo
  selectedNode.name = nodeName.value || selectedNode.name;
  selectedNode.propagationProb = Number(propagationProb.value);
  selectedNode.resistance = Number(resistance.value);
  selectedNode.threshold = Number(socialThreshold.value);
  selectedNode.state = initialState.value;
  
  try {
    // Guardar en backend
    await APIService.actualizarNodo(selectedNode.id, {
      nombre: selectedNode.name,
      probabilidad: selectedNode.propagationProb,
      resistencia: selectedNode.resistance,
      umbral: selectedNode.threshold,
      estado: selectedNode.state
    });
    
    showSaveNotification(`✅ Nodo ${selectedNode.id} guardado automáticamente`);
  } catch (error) {
    console.error('Error guardando nodo automáticamente:', error);
    showSaveNotification(`❌ Error al guardar nodo ${selectedNode.id}`, 'error');
  }
}

/* Muestra notificación visual de guardado */
function showSaveNotification(message, type = 'success') {
  // Limpiar notificación anterior
  if (lastSaveNotification) {
    lastSaveNotification.remove();
  }
  
  // Crear elemento de notificación
  const notification = document.createElement('div');
  notification.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    background: ${type === 'success' ? '#4CAF50' : '#f44336'};
    color: white;
    padding: 12px 16px;
    border-radius: 4px;
    font-size: 13px;
    z-index: 10000;
    animation: slideIn 0.3s ease-out;
    max-width: 300px;
    word-wrap: break-word;
  `;
  notification.textContent = message;
  document.body.appendChild(notification);
  
  lastSaveNotification = notification;
  
  // Auto-remover después de 2 segundos
  setTimeout(() => {
    if (lastSaveNotification === notification) {
      notification.style.animation = 'slideOut 0.3s ease-out';
      setTimeout(() => notification.remove(), 300);
      lastSaveNotification = null;
    }
  }, 2000);
}

function selectNode(nodeData) {
  // Validar que nodeData existe
  if (!nodeData) {
    console.warn('selectNode: nodeData es undefined');
    return;
  }

  // PASO 3: Guardar automáticamente el nodo anterior antes de cambiar
  if (selectedNode && selectedNode.id !== nodeData.id) {
    saveCurrentNode();
  }
  
  selectedNode = nodeData;
  
  // Solo actualizar visualización si node (D3 selection) está inicializado
  if (node && node.classed) {
    node.classed('selected', d => d === nodeData);
  }
  
  nodeName.value = nodeData.name;
  nodeId.value = nodeData.id;
  nodeDegree.value = nodeData.degree;
  propagationProb.value = nodeData.propagationProb;
  propagationProbValue.textContent = `${nodeData.propagationProb}%`;
  resistance.value = nodeData.resistance;
  resistanceValue.textContent = `${nodeData.resistance}%`;
  socialThreshold.value = nodeData.threshold;
  socialThresholdValue.textContent = `${nodeData.threshold}%`;
  initialState.value = nodeData.state;
  seedInput.value = nodeData.id;
  
  // Limpiar indicador de cambios pendientes
  if (saveNode) {
    saveNode.classList.remove('unsaved');
    saveNode.textContent = '💾 Guardar';
  }
}

/* PASO 4: Guarda la simulación actual en el historial */
function saveSimulationToHistory() {
  const seedNode = nodes.find(n => n.isSource);
  if (!seedNode) return;
  
  // Calcular métricas finales
  const counts = { active: 0, passive: 0, resistant: 0, uninformed: 0 };
  nodes.forEach(node => counts[node.state] += 1);
  const informed = counts.active + counts.passive;
  const reach = Math.round((informed / nodes.length) * 100);
  
  const simulationRecord = {
    id: Date.now(), // Timestamp único
    date: new Date().toLocaleString('es-ES'),
    modelo: modeloSeleccionado,
    seedNodeId: seedNode.id,
    seedNodeName: seedNode.name,
    totalSteps: simulation.totalSteps,
    reach: reach,
    informed: informed,
    active: counts.active,
    passive: counts.passive,
    resistant: counts.resistant,
    uninformed: counts.uninformed,
    seriesData: {
      [modeloSeleccionado]: simulation.series[modeloSeleccionado] || []
    }
  };
  
  simulationHistory.push(simulationRecord);
  
  // Guardar también en localStorage para persistencia
  try {
    localStorage.setItem('viralsim_history', JSON.stringify(simulationHistory));
    console.log('Simulación guardada en historial');
  } catch (error) {
    console.error('Error guardando en localStorage:', error);
  }
  
  showSaveNotification(`📊 Simulación ${modeloSeleccionado} guardada (#${simulationHistory.length})`);
}

/* Carga el historial de simulaciones desde localStorage */
function loadSimulationHistory() {
  try {
    const stored = localStorage.getItem('viralsim_history');
    if (stored) {
      simulationHistory = JSON.parse(stored);
      console.log(`Historial cargado: ${simulationHistory.length} simulaciones`);
    }
  } catch (error) {
    console.error('Error cargando historial:', error);
    simulationHistory = [];
  }
}

/* Muestra el historial de simulaciones */
/* PASO 5: Exporta el grafo actual como PNG */
function exportGraphAsPNG() {
  // Convertir SVG a Canvas
  const svgElement = document.querySelector('#graph');
  if (!svgElement) {
    showSaveNotification('❌ No se encontró el grafo', 'error');
    return;
  }
  
  // Obtener dimensiones
  const bbox = svgElement.getBBox();
  const scale = 2; // Alta resolución
  const canvas = document.createElement('canvas');
  canvas.width = bbox.width * scale;
  canvas.height = bbox.height * scale;
  
  const ctx = canvas.getContext('2d');
  ctx.scale(scale, scale);
  ctx.fillStyle = '#0a0a0a';
  ctx.fillRect(bbox.x, bbox.y, bbox.width, bbox.height);
  
  // Serializar SVG a string
  const svgString = new XMLSerializer().serializeToString(svgElement);
  const svg = new Blob([svgString], { type: 'image/svg+xml;charset=utf-8' });
  const url = URL.createObjectURL(svg);
  
  const img = new Image();
  img.onload = () => {
    ctx.drawImage(img, 0, 0);
    
    // Descargar PNG
    canvas.toBlob(blob => {
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-');
      link.download = `viralsim_grafo_${timestamp}.png`;
      link.click();
      URL.revokeObjectURL(link.href);
      showSaveNotification('✅ Grafo descargado como PNG');
    });
  };
  img.src = url;
}

/* PASO 5: Exporta métricas actuales como CSV */
function exportMetricsAsCSV() {
  // Capturar estado actual
  const counts = { active: 0, passive: 0, resistant: 0, uninformed: 0 };
  nodes.forEach(node => counts[node.state] += 1);
  const informed = counts.active + counts.passive;
  const reach = Math.round((informed / nodes.length) * 100);
  
  // Crear CSV con datos de nodos
  let csv = 'ID,Nombre,Estado,Grado,Probabilidad,Resistencia,Umbral\n';
  nodes.forEach(node => {
    csv += `${node.id},"${node.name}",${node.state},${node.degree},${node.propagationProb},${node.resistance},${node.threshold}\n`;
  });
  
  // Agregar resumen
  csv += '\n\nRESUMEN DE SIMULACION\n';
  csv += `Paso Actual,${simulation.step}\n`;
  csv += `Total de Pasos,${simulation.totalSteps}\n`;
  csv += `Alcance,${reach}%\n`;
  csv += `Nodos Informados,${informed}/${nodes.length}\n`;
  csv += `Nodos Activos,${counts.active}\n`;
  csv += `Nodos Pasivos,${counts.passive}\n`;
  csv += `Nodos Resistentes,${counts.resistant}\n`;
  csv += `Nodos No Informados,${counts.uninformed}\n`;
  csv += `Fecha,${new Date().toLocaleString('es-ES')}\n`;
  
  // Descargar CSV
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-');
  link.download = `viralsim_metricas_${timestamp}.csv`;
  link.click();
  URL.revokeObjectURL(link.href);
  showSaveNotification('✅ Métricas descargadas como CSV');
}

/* PASO 5: Exporta el historial como CSV */
function exportHistoryAsCSV() {
  if (simulationHistory.length === 0) {
    showSaveNotification('📭 No hay historial para exportar', 'error');
    return;
  }
  
  let csv = 'Fecha,Semilla,Nombre Semilla,Pasos,Alcance (%),Activos,Pasivos,Resistentes,No Informados\n';
  simulationHistory.forEach(record => {
    csv += `"${record.date}",${record.seedNodeId},"${record.seedNodeName}",${record.totalSteps},${record.reach},${record.active},${record.passive},${record.resistant},${record.uninformed}\n`;
  });
  
  // Descargar CSV
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-');
  link.download = `viralsim_historial_${timestamp}.csv`;
  link.click();
  URL.revokeObjectURL(link.href);
  showSaveNotification('✅ Historial descargado como CSV');
}

function showSimulationHistory() {
  if (simulationHistory.length === 0) {
    showSaveNotification('📭 No hay simulaciones registradas', 'info');
    return;
  }
  
  // Crear modal para mostrar historial
  const modal = document.createElement('div');
  modal.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
  `;
  
  const content = document.createElement('div');
  content.style.cssText = `
    background: #1a1a1a;
    border: 1px solid rgba(255,255,255,0.1);
    border-radius: 12px;
    padding: 24px;
    max-width: 800px;
    max-height: 600px;
    overflow-y: auto;
    box-shadow: 0 20px 60px rgba(0,0,0,0.5);
  `;
  
  const title = document.createElement('h2');
  title.textContent = `📊 Historial de Simulaciones (${simulationHistory.length})`;
  title.style.cssText = 'color: #fff; margin-top: 0; margin-bottom: 16px;';
  content.appendChild(title);
  
  const table = document.createElement('table');
  table.style.cssText = `
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;
    color: #ccc;
  `;
  
  // Header
  const thead = document.createElement('thead');
  const headerRow = document.createElement('tr');
  ['#', 'Fecha', 'Semilla', 'Pasos', 'Alcance', 'Activos', 'Pasivos', 'Resistentes'].forEach(label => {
    const th = document.createElement('th');
    th.textContent = label;
    th.style.cssText = 'text-align: left; padding: 8px; border-bottom: 1px solid rgba(255,255,255,0.1); color: rgba(255,255,255,0.6); font-weight: 600;';
    headerRow.appendChild(th);
  });
  thead.appendChild(headerRow);
  table.appendChild(thead);
  
  // Rows
  const tbody = document.createElement('tbody');
  simulationHistory.forEach((record, index) => {
    const row = document.createElement('tr');
    row.style.cssText = 'border-bottom: 1px solid rgba(255,255,255,0.05); cursor: pointer;';
    row.onmouseover = () => row.style.background = 'rgba(255,255,255,0.08)';
    row.onmouseout = () => row.style.background = '';
    
    const cells = [
      document.createElement('td'),
      document.createElement('td'),
      document.createElement('td'),
      document.createElement('td'),
      document.createElement('td'),
      document.createElement('td'),
      document.createElement('td'),
      document.createElement('td')
    ];
    
    cells[0].textContent = index + 1;
    cells[1].textContent = record.date;
    cells[2].textContent = `N${record.seedNodeId}`;
    cells[3].textContent = record.totalSteps;
    cells[4].textContent = `${record.reach}%`;
    cells[5].textContent = record.active;
    cells[6].textContent = record.passive;
    cells[7].textContent = record.resistant;
    
    cells.forEach(cell => {
      cell.style.cssText = 'padding: 10px 8px;';
      row.appendChild(cell);
    });
    
    tbody.appendChild(row);
  });
  table.appendChild(tbody);
  content.appendChild(table);
  
  // Botones
  const buttonDiv = document.createElement('div');
  buttonDiv.style.cssText = 'display: flex; gap: 10px; margin-top: 16px; justify-content: flex-end;';
  
  const exportBtn = document.createElement('button');
  exportBtn.textContent = '📥 Exportar CSV';
  exportBtn.style.cssText = `
    padding: 10px 16px;
    background: #2196F3;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
  `;
  exportBtn.onclick = () => exportHistoryAsCSV();
  
  const clearBtn = document.createElement('button');
  clearBtn.textContent = '🗑️ Limpiar Historial';
  clearBtn.style.cssText = `
    padding: 10px 16px;
    background: #f44336;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
  `;
  clearBtn.onclick = () => {
    if (confirm('¿Descartar todo el historial?')) {
      simulationHistory = [];
      localStorage.removeItem('viralsim_history');
      modal.remove();
      showSaveNotification('🗑️ Historial borrado');
    }
  };
  
  const closeBtn = document.createElement('button');
  closeBtn.textContent = 'Cerrar';
  closeBtn.style.cssText = `
    padding: 10px 16px;
    background: #4CAF50;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
  `;
  closeBtn.onclick = () => modal.remove();
  
  buttonDiv.appendChild(exportBtn);
  buttonDiv.appendChild(clearBtn);
  buttonDiv.appendChild(closeBtn);
  content.appendChild(buttonDiv);
  
  modal.appendChild(content);
  modal.onclick = (e) => {
    if (e.target === modal) modal.remove();
  };
  
  document.body.appendChild(modal);
}

function updateGraphState() {
  node.attr('class', d => `node ${d.state}${d.isSource ? ' source' : ''}`);
  renderGraph();
}

/* Inicia la simulación usando el nodo semilla elegido por el usuario */
async function triggerLaunch() {
  const seed = Number(seedInput.value);
  if (!Number.isInteger(seed) || seed < 1 || seed > nodes.length) return;
  const seedNode = nodes.find(n => n.id === seed);
  if (!seedNode) return;

  // IMPORTANTE: Guardar automáticamente los valores editados antes de lanzar
  if (selectedNode && selectedNode.id === seedNode.id) {
    selectedNode.name = nodeName.value || selectedNode.name;
    selectedNode.propagationProb = Number(propagationProb.value);
    selectedNode.resistance = Number(resistance.value);
    selectedNode.threshold = Number(socialThreshold.value);
    selectedNode.state = initialState.value;

    try {
      // Persistir cambios en el backend
      await APIService.actualizarNodo(selectedNode.id, {
        nombre: selectedNode.name,
        probabilidad: selectedNode.propagationProb,
        resistencia: selectedNode.resistance,
        umbral: selectedNode.threshold,
        estado: selectedNode.state
      });
      console.log('Valores del nodo guardados automáticamente');
    } catch (error) {
      console.error('Error guardando nodo:', error);
    }
  }

  resetSimulation();
  
  try {
    // Mapear modelo seleccionado a ID numérico para el backend
    const modelMap = { viral: 1, cascade: 2, threshold: 3 };
    const modeloId = modelMap[modeloSeleccionado];
    
    console.log(`Ejecutando simulación con modelo: ${modeloSeleccionado} (ID: ${modeloId})`);
    
    // PASO 1: Crear simulación en el backend
    const simulacion = await APIService.crearSimulacion(grafoId, seedNode.id, modeloId);
    console.log('Simulación creada:', simulacion);
    
    // PASO 2: Ejecutar simulación
    const resultado = await APIService.ejecutarSimulacion(simulacion.id);
    console.log('Simulación ejecutada:', resultado);
    
    // PASO 3: Obtener pasos de la simulación desde la BD
    const pasos = await APIService.obtenerPasosSimulacion(simulacion.id);
    console.log('Pasos obtenidos:', pasos);
    
    // PASO 4: Obtener métricas de la simulación desde la BD
    const metricas = await APIService.obtenerMetricasSimulacion(simulacion.id);
    console.log('Métricas obtenidas:', metricas);
    
    // PASO 5: Cargar datos de la BD en la simulación local y animar
    await cargarYAnimarSimulacion(simulacion.id, pasos, metricas, seedNode);
    
  } catch (error) {
    console.error('Error ejecutando simulación:', error);
    alert('Error ejecutando simulación. Revisa la consola para más detalles.');
  }
}

/**
 * Carga una simulación desde la BD y la anima en el frontend
 * @param {number} simulacionId - ID de la simulación
 * @param {Array} pasos - Array de pasos desde la BD
 * @param {Object} metricas - Métricas de la simulación
 * @param {Object} seedNode - Nodo semilla
 */
async function cargarYAnimarSimulacion(simulacionId, pasos, metricas, seedNode) {
  // Limpiar estado anterior
  nodes.forEach(node => {
    node.state = 'uninformed';
    node.isSource = false;
  });
  
  // Marcar nodo semilla como origen
  seedNode.state = 'active';
  seedNode.isSource = true;
  
  // Configurar simulación para animación
  simulation.step = 0;
  simulation.totalSteps = pasos.length - 1;
  simulation.series = {};
  simulation.series[modeloSeleccionado] = [];
  
  // Extraer datos de alcance de los pasos para graficar
  pasos.forEach((paso, index) => {
    const alcanceActual = (paso.nodosActivos + paso.nodosPasivos) / nodes.length * 100;
    simulation.series[modeloSeleccionado].push(alcanceActual);
  });
  
  console.log(`Cargada simulación con ${pasos.length} pasos`);
  console.log('Serie de datos:', simulation.series[modeloSeleccionado]);
  
  // Actualizar UI
  selectNode(seedNode);
  updateGraphState();
  updateMetrics();
  updateChart();
  
  // Iniciar animación
  startTimer();
}

/**
 * Crea un nuevo grafo en el backend y recarga la página
 */
async function createNewGraph() {
  try {
    if (createGraphButton) {
      createGraphButton.disabled = true;
      createGraphButton.textContent = 'Creando...';
    }
    
    console.log('Creando nuevo grafo...');
    const nuevoGrafo = await APIService.crearGrafo();
    
    console.log('Grafo creado exitosamente:', nuevoGrafo);
    alert(`✓ Grafo creado exitosamente con 250 nodos`);
    
    // Recargar lista de grafos y seleccionar el nuevo
    await cargarListaGrafos();
    grafoSelect.value = nuevoGrafo.id;
    
    // Cargar el nuevo grafo automáticamente
    await cargarGrafo(nuevoGrafo.id);
  } catch (error) {
    console.error('Error creando grafo:', error);
    alert('Error creando grafo. Revisa la consola para más detalles.');
    if (createGraphButton) {
      createGraphButton.disabled = false;
      createGraphButton.textContent = 'Crear nuevo grafo';
    }
  }
}

/**
 * Carga la lista de grafos disponibles y la muestra en el selector
 */
async function cargarListaGrafos() {
  try {
    if (!grafoSelect) {
      console.warn('El selector de grafos no está inicializado aún');
      return;
    }

    const grafos = await APIService.obtenerGrafos();
    console.log('Grafos disponibles:', grafos.length);
    
    // Limpiar opciones anteriores
    grafoSelect.innerHTML = '';
    
    if (grafos.length === 0) {
      grafoSelect.innerHTML = '<option value="">No hay grafos disponibles</option>';
    } else {
      grafoSelect.innerHTML = '<option value="">-- Selecciona un grafo --</option>';
      grafos.forEach(grafo => {
        const option = document.createElement('option');
        option.value = grafo.id;
        option.textContent = `Grafo #${grafo.id} (${grafo.totalNodos || 250} nodos)`;
        grafoSelect.appendChild(option);
      });
    }
  } catch (error) {
    console.error('Error cargando lista de grafos:', error);
    if (grafoSelect) {
      grafoSelect.innerHTML = '<option value="">Error cargando grafos</option>';
    }
  }
}

/**
 * Carga un grafo específico en la visualización
 * @param {number} grafoIdSeleccionado - ID del grafo a cargar
 */
async function cargarGrafo(grafoIdSeleccionado) {
  try {
    console.log(`Cargando grafo ${grafoIdSeleccionado}...`);
    
    // Obtener nodos y aristas del grafo
    const nodosAPI = await APIService.obtenerNodosPorGrafo(grafoIdSeleccionado);
    const aristasAPI = await APIService.obtenerAristasPorGrafo(grafoIdSeleccionado);

    console.log('Nodos cargados:', nodosAPI.length, 'Aristas cargadas:', aristasAPI.length);

    // Validar que hay nodos en el grafo
    if (nodosAPI.length === 0) {
      alert('El grafo no tiene nodos.');
      return;
    }

    // Calcular métricas del grafo (betweenness centrality, grado)
    await APIService.calcularMetricas(grafoIdSeleccionado);

    // Transformar datos de API a formato compatible con D3
    nodes = nodosAPI.map(nodo => ({
      id: nodo.id,
      x: width / 2,
      y: height / 2,
      state: 'uninformed',
      propagationProb: nodo.probabilidadPropagacion ? Math.round(nodo.probabilidadPropagacion * 100) : 60,
      resistance: 24, // TODO: obtener de la BD si está disponible
      threshold: nodo.umbral ? Math.round(nodo.umbral * 100) : 40,
      name: nodo.nombre || `Nodo ${nodo.id}`,
      degree: 0,
      isSource: false,
      betweenness: nodo.betweenness || 0,
      centralidadGrado: nodo.centralidadGrado || 0
    }));

    links = aristasAPI.map(arista => ({
      source: nodes.find(n => n.id === arista.nodoOrigen.id),
      target: nodes.find(n => n.id === arista.nodoDestino.id)
    })).filter(link => link.source && link.target);

    // Construir matriz de adyacencia
    adjacency = Array.from({ length: nodes.length }, () => []);
    links.forEach(link => {
      link.source.degree += 1;
      link.target.degree += 1;
      const sourceIdx = nodes.indexOf(link.source);
      const targetIdx = nodes.indexOf(link.target);
      adjacency[sourceIdx].push(targetIdx);
      adjacency[targetIdx].push(sourceIdx);
    });

    // Posicionar nodos en CÍRCULO PERFECTO (Watts-Strogatz ring layout)
    const radius = 850;
    const centerX = width / 2;
    const centerY = height / 2;
    nodes.forEach((node, i) => {
      const angle = (i / nodes.length) * 2 * Math.PI;
      node.x = centerX + radius * Math.cos(angle);
      node.y = centerY + radius * Math.sin(angle);
      node.fx = node.x;
      node.fy = node.y;
    });

    // Crear D3 Force Simulation
    forceSimulation = d3.forceSimulation(nodes)
      .force('link', d3.forceLink(links)
        .id(d => d.id)
        .distance(150)
        .strength(0.01))
      .force('radial', d3.forceRadial(radius)
        .strength(0.25))
      .force('center', d3.forceCenter(centerX, centerY))
      .alphaDecay(0.1)
      .velocityDecay(0.5);

    forceSimulation.tick(50);
    
    nodes.forEach(node => {
      node.fx = null;
      node.fy = null;
    });
    
    forceSimulation.stop();

    // Actualizar UI
    grafoId = grafoIdSeleccionado;
    document.querySelector('.top-stats').innerHTML = `Red: <strong>${nodes.length} nodos</strong>`;
    document.querySelector('.graph-badge').textContent = `${nodes.length} nodos`;

    // Inicializar visualización
    renderGraph();
    if (nodes.length > 0) {
      selectNode(nodes[0]);
    }
    updateMetrics();
    resetSimulation();
    initializeChart();

    console.log('Grafo cargado:', { grafoId, nodos: nodes.length, aristas: links.length });
  } catch (error) {
    console.error('Error cargando grafo:', error);
    alert('Error cargando grafo. Revisa la consola para más detalles.');
  }
}

/* Actualiza los indicadores de la interfaz con el estado actual de los nodos */
function updateMetrics() {
  const counts = { active: 0, passive: 0, resistant: 0, uninformed: 0 };
  nodes.forEach(node => counts[node.state] += 1);
  const informed = counts.active + counts.passive;
  metricReach.textContent = `${Math.round((informed / nodes.length) * 100)}%`;
  metricStep.textContent = simulation.step;
  metricInformed.textContent = `${informed} / ${nodes.length}`;
  metricStatus.textContent = simulation.active ? 'En progreso' : 'Inactivo';
  barActive.style.width = `${Math.round((counts.active / nodes.length) * 100)}%`;
  barPassive.style.width = `${Math.round((counts.passive / nodes.length) * 100)}%`;
  barResistant.style.width = `${Math.round((counts.resistant / nodes.length) * 100)}%`;
  barUninformed.style.width = `${Math.round((counts.uninformed / nodes.length) * 100)}%`;
  bottomStep.textContent = simulation.step;
  bottomTotal.textContent = simulation.totalSteps;
}

/* Crea la gráfica SVG destinada a mostrar las curvas de propagación */
function initializeChart() {
  const { width, height, margin } = chartConfig;
  const chart = chartSvg.attr('viewBox', `0 0 ${width} ${height}`);
  
  // Limpiar chart anterior
  chart.selectAll('*').remove();
  
  const gridGroup = chart.append('g').attr('class', 'grid');
  const rows = 5;
  for (let i = 0; i <= rows; i++) {
    const y = margin.top + ((height - margin.top - margin.bottom) / rows) * i;
    gridGroup.append('line')
      .attr('x1', margin.left)
      .attr('x2', width - margin.right)
      .attr('y1', y)
      .attr('y2', y)
      .attr('class', 'grid-line');
  }

  chart.append('rect')
    .attr('x', margin.left)
    .attr('y', margin.top)
    .attr('width', width - margin.left - margin.right)
    .attr('height', height - margin.top - margin.bottom)
    .attr('fill', 'none')
    .lower();
  
  // No crear paths aquí, se crearán dinámicamente en updateChart
  updateChart();
}

function updateChart() {
  const { width, height, margin } = chartConfig;
  const maxPoints = simulation.totalSteps + 1;
  const xScale = d3.scaleLinear().domain([0, maxPoints - 1]).range([margin.left, width - margin.right]);
  const yScale = d3.scaleLinear().domain([0, 100]).range([height - margin.bottom, margin.top]);
  const line = d3.line()
    .x((d, i) => xScale(i))
    .y(d => yScale(d))
    .curve(d3.curveMonotoneX);

  // Limpiar líneas anteriores
  chartSvg.selectAll('.line-path').remove();
  
  // Mostrar solo el modelo seleccionado
  if (simulation.series[modeloSeleccionado]) {
    const colorMap = { viral: '#d32f2f', cascade: '#1976d2', threshold: '#388e3c' };
    const labelMap = { viral: 'Viral', cascade: 'Cascada', threshold: 'Threshold' };
    
    chartSvg.append('path')
      .attr('class', `line-path ${modeloSeleccionado}`)
      .datum(simulation.series[modeloSeleccionado])
      .attr('d', line)
      .style('stroke', colorMap[modeloSeleccionado])
      .style('stroke-width', 2.5)
      .style('fill', 'none')
      .style('opacity', 0.9);
    
    // Añadir leyenda del modelo actual
    chartSvg.selectAll('.legend').remove();
    const legend = chartSvg.append('g')
      .attr('class', 'legend')
      .attr('transform', `translate(${width - margin.right - 120}, ${margin.top + 10})`);
    
    legend.append('rect')
      .attr('width', 110)
      .attr('height', 25)
      .attr('fill', 'rgba(255,255,255,0.8)')
      .attr('stroke', colorMap[modeloSeleccionado])
      .attr('stroke-width', 2)
      .attr('rx', 4);
    
    legend.append('circle')
      .attr('cx', 10)
      .attr('cy', 12.5)
      .attr('r', 3)
      .attr('fill', colorMap[modeloSeleccionado]);
    
    legend.append('text')
      .attr('x', 20)
      .attr('y', 17)
      .attr('font-family', 'Inter, Segoe UI, sans-serif')
      .attr('font-size', '12px')
      .attr('font-weight', 'bold')
      .attr('fill', '#333')
      .text(labelMap[modeloSeleccionado]);
  }
}

/* Ejecuta un paso de simulación desde los datos de la BD */
function stepSimulation() {
  if (simulation.step >= simulation.totalSteps) {
    stopTimer();
    return;
  }

  // Nota: La lógica de propagación se ejecuta en el backend
  // Aquí solo actualizamos la visualización basada en los datos de la BD
  simulation.step += 1;
  updateGraphState();
  updateMetrics();
  updateChart();
}

function startTimer() {
  stopTimer();
  simulation.active = true;
  metricStatus.textContent = 'En progreso';
  simulation.timer = d3.interval(() => {
    stepSimulation();
  }, 800 / simulation.speed);
}

function stopTimer() {
  if (simulation.timer) {
    simulation.timer.stop();
    simulation.timer = null;
  }
  simulation.active = false;
  metricStatus.textContent = 'Pausado';
  
  // PASO 4: Guardar simulación en historial cuando termina
  if (simulation.step >= simulation.totalSteps) {
    saveSimulationToHistory();
  }
}

function togglePause() {
  if (simulation.active) {
    stopTimer();
  } else {
    startTimer();
  }
}

function resetSimulation() {
  stopTimer();
  simulation.step = 0;
  simulation.series = {};
  simulation.series[modeloSeleccionado] = [0];
  nodes.forEach(node => {
    node.state = 'uninformed';
    node.isSource = false;
  });
  selectNode(nodes[0]);
  updateGraphState();
  updateMetrics();
  updateChart();
}

function dragstarted(event, d) {
  d3.select(this).raise();
  d.fx = d.x;
  d.fy = d.y;
}

function dragged(event, d) {
  d.fx = event.x;
  d.fy = event.y;
  d.x = event.x;
  d.y = event.y;
  renderGraph();
}

function dragended(event, d) {
  d.fx = null;
  d.fy = null;
}

/* Control de zoom y desplazamiento para el gráfico SVG */
let currentTransform = d3.zoomIdentity.translate(0, 0).scale(1);

const zoom = d3.zoom()
  .scaleExtent([0.55, 2.5])
  .on('zoom', event => {
    currentTransform = event.transform;
    canvas.attr('transform', event.transform);
  });

svg.call(zoom).call(zoom.transform, currentTransform);

// Función para cambiar zoom programáticamente
function changeZoom(factor) {
  const newScale = currentTransform.k * factor;
  
  // Limitar escala dentro de los límites
  if (newScale < 0.55 || newScale > 2.5) return;
  
  // Calcular nueva transformación centrada en el centro del SVG
  const newTransform = d3.zoomIdentity
    .translate(width / 2, height / 2)
    .scale(newScale)
    .translate(-width / 2, -height / 2);
  
  svg.transition()
    .duration(200)
    .call(zoom.transform, newTransform);
}

// Atajos de teclado: Ctrl++ para agrandar, Ctrl+- para encojer
document.addEventListener('keydown', (event) => {
  // Evitar conflicto con otros atajos
  if (!event.ctrlKey && !event.metaKey) return;
  
  if (event.key === '+' || event.key === '=') {
    event.preventDefault();
    changeZoom(1.5); // Agrandar 50%
  } else if (event.key === '-' || event.key === '_') {
    event.preventDefault();
    changeZoom(1 / 1.5); // Encojer 33%
  }
});
