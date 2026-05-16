/**
 * VISTA-CHISME.js - Lanzar simulación con selección de nodo semilla
 */

let grafoChisme = null;
let nodosChisme = [];
let aristasChisme = [];
let redVis = null;
let nodoSemillaSeleccionado = null;
let modeloSeleccionado = null;

const divChismeGrafo = document.getElementById('chisme-grafo');
const selectModelo = document.getElementById('select-modelo');
const modeloDesc = document.getElementById('modelo-desc');
const btnLanzarSimulacion = document.getElementById('btn-lanzar-simulacion');
const inputMensaje = document.getElementById('mensaje-chisme');
const sliderAlcance = document.getElementById('slider-alcance');
const valAlcance = document.getElementById('val-alcance');
const nodoSemillaInfo = document.getElementById('nodo-semilla-info');

// ============================================
// CARGAR Y VISUALIZAR GRAFO
// ============================================

async function cargarGrafoChisme() {
    try {
        const grafos = await obtenerGrafos();
        if (grafos.length === 0) {
            divChismeGrafo.innerHTML = '<p>No hay redes disponibles. Genera una primero.</p>';
            return;
        }

        grafoChisme = grafos[0];
        nodosChisme = await obtenerNodosGrafo(grafoChisme.id);
        aristasChisme = await obtenerAristasGrafo(grafoChisme.id);

        if (!Array.isArray(nodosChisme)) nodosChisme = [];
        if (!Array.isArray(aristasChisme)) aristasChisme = [];

        visualizarGrafo();
    } catch (error) {
        console.error('Error al cargar grafo:', error);
        divChismeGrafo.innerHTML = '<p>Error al cargar el grafo</p>';
    }
}

function visualizarGrafo() {
    // Preparar nodos para vis.js
    const nodesData = new vis.DataSet(nodosChisme.map(nodo => ({
        id: nodo.id,
        label: nodo.nombre,
        color: getEstadoColor(nodo.estadoId),
        size: 20,
        title: `${nodo.nombre}\nEstado: ${getEstadoNombre(nodo.estadoId)}\nProb: ${nodo.probabilidadPropagacion}`
    })));

    // Preparar aristas para vis.js
    const edgesData = new vis.DataSet(aristasChisme.map((arista, idx) => ({
        id: idx,
        from: arista.nodo1Id,
        to: arista.nodo2Id,
        width: 2
    })));

    // Configurar opciones
    const options = {
        physics: {
            enabled: true,
            barnesHut: { gravitationalConstant: -30000, centralGravity: 0.3, springLength: 200 }
        },
        interaction: {
            hover: true,
            navigationButtons: true,
            keyboard: true
        }
    };

    // Crear red
    const data = { nodes: nodesData, edges: edgesData };
    if (redVis) redVis.destroy();
    redVis = new vis.Network(divChismeGrafo, data, options);

    // Detector de clic en nodos
    redVis.on('click', (params) => {
        if (params.nodes.length > 0) {
            seleccionarNodoSemilla(params.nodes[0]);
        }
    });
}

function seleccionarNodoSemilla(nodoId) {
    const nodo = nodosChisme.find(n => n.id === nodoId);
    if (nodo) {
        nodoSemillaSeleccionado = nodo;
        
        nodoSemillaInfo.innerHTML = `
            <h4>Nodo Seleccionado ✅</h4>
            <p><strong>ID:</strong> ${nodo.id}</p>
            <p><strong>Nombre:</strong> ${nodo.nombre}</p>
            <p><strong>Estado:</strong> ${getEstadoLabel(nodo.estadoId)} ${getEstadoNombre(nodo.estadoId)}</p>
            <p><strong>Prob. Propagación:</strong> ${(nodo.probabilidadPropagacion || 0).toFixed(2)}</p>
            <p><strong>Grado (vecinos):</strong> ${nodo.grado || 0}</p>
        `;

        // Resaltar nodo en el grafo
        redVis.selectNodes([nodoId]);
    }
}

// ============================================
// CONFIGURACIÓN DE MODELO
// ============================================

selectModelo.addEventListener('change', (e) => {
    modeloSeleccionado = parseInt(e.target.value) || null;
    
    if (modeloSeleccionado) {
        const modelo = getModeloInfo(modeloSeleccionado);
        modeloDesc.textContent = modelo.descripcion;
    } else {
        modeloDesc.textContent = '';
    }
});

sliderAlcance.addEventListener('input', (e) => {
    valAlcance.textContent = e.target.value;
});

// ============================================
// LANZAR SIMULACIÓN
// ============================================

async function lanzarSimulacion() {
    // Validaciones
    if (!nodoSemillaSeleccionado) {
        alert('⚠️ Selecciona un nodo semilla haciendo clic en el grafo');
        return;
    }

    if (!modeloSeleccionado) {
        alert('⚠️ Selecciona un modelo de propagación');
        return;
    }

    if (!inputMensaje.value.trim()) {
        alert('⚠️ Escribe un mensaje/chisme');
        return;
    }

    try {
        btnLanzarSimulacion.disabled = true;
        btnLanzarSimulacion.textContent = '⏳ Ejecutando...';

        // Crear configuración
        const config = {
            grafoId: grafoChisme.id,
            nodoSemillaId: nodoSemillaSeleccionado.id,
            modeloPropagacionId: modeloSeleccionado,
            mensaje: inputMensaje.value,
            pasoMaximo: parseInt(sliderAlcance.value)
        };

        // Crear simulación
        const simulacion = await crearSimulacion(config);
        
        // Ejecutar simulación
        await ejecutarSimulacion(simulacion.id);

        // Guardar ID de simulación para la vista de propagación
        window.simulacionActualId = simulacion.id;

        // Cambiar a tab de propagación
        switchTab('propagacion');
        await cargarPropagacion();

        alert('✅ Simulación ejecutada. Ahora puedes ver la propagación en tiempo real.');
    } catch (error) {
        console.error('Error al lanzar simulación:', error);
        alert('❌ Error al ejecutar la simulación: ' + error.message);
    } finally {
        btnLanzarSimulacion.disabled = false;
        btnLanzarSimulacion.textContent = '🚀 Lanzar Simulación';
    }
}

btnLanzarSimulacion.addEventListener('click', lanzarSimulacion);

// ============================================
// INICIALIZACIÓN
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    const tabBtn = document.querySelector('[data-tab="chisme"]');
    if (tabBtn) {
        tabBtn.addEventListener('click', cargarGrafoChisme);
    }
});

// Función auxiliar para cambiar tabs (se define en app.js pero la necesitamos aquí)
function switchTab(tabName) {
    // Ocultar todas las tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Desactivar todos los botones de tab
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Mostrar tab seleccionada
    document.getElementById(tabName).classList.add('active');
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
}
