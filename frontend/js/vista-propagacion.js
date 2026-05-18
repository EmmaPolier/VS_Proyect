/**
 * VISTA-PROPAGACION.js - Animación y métricas de propagación
 */

let simulacionActual = null;
let pasosSimulacion = [];
let pasoActual = 0;
let redPropagacion = null;
let enPausa = false;
let chartPropagacion = null;
let historicoEstados = [];
let timeoutAutoAvanzar = null; // SALVAVIDAS: ID para cancelar timeout recursivo

const divPropagacionGrafo = document.getElementById('propagacion-grafo');
const pasoActualEl = document.getElementById('paso-actual');
const propModelo = document.getElementById('prop-modelo');
const propSemilla = document.getElementById('prop-semilla');
const btnPausarSim = document.getElementById('btn-pausar-sim');
const btnSiguientePaso = document.getElementById('btn-siguiente-paso');
const btnReiniciarSim = document.getElementById('btn-reiniciar-sim');
const chartCanvas = document.getElementById('chart-propagacion');

// ============================================
// CARGAR SIMULACIÓN
// ============================================

async function cargarPropagacion() {
    try {
        const simulacionId = window.simulacionActualId;
        if (!simulacionId) {
            divPropagacionGrafo.innerHTML = '<p>❌ No hay simulación activa</p>';
            return;
        }

        simulacionActual = await obtenerSimulacion(simulacionId);
        if (!simulacionActual || !simulacionActual.id) {
            divPropagacionGrafo.innerHTML = '<p>❌ Error al cargar la simulación del servidor</p>';
            return;
        }

        // Validar que simulación tiene grafo ID
        if (!simulacionActual.grafoId) {
            divPropagacionGrafo.innerHTML = '<p>❌ Error: Simulación sin grafo asociado</p>';
            return;
        }

        // Cargar pasos
        pasosSimulacion = await obtenerPasosSimulacion(simulacionId);
        if (!Array.isArray(pasosSimulacion)) {
            pasosSimulacion = [];
        }

        if (pasosSimulacion.length === 0) {
            divPropagacionGrafo.innerHTML = '<p>⚠️ La simulación no produjo pasos. Verifica los parámetros.</p>';
            return;
        }

        // Usar grafo de la simulación
        const grafoId = simulacionActual.grafoId;

        // Cargar nodos y aristas
        const nodosData = await obtenerNodosGrafo(grafoId);
        const aristasData = await obtenerAristasGrafo(grafoId);

        // Validar datos
        if (!Array.isArray(nodosData) || nodosData.length === 0) {
            divPropagacionGrafo.innerHTML = '<p>❌ Error: No hay nodos en el grafo</p>';
            return;
        }
        if (!Array.isArray(aristasData)) {
            aristasData = [];
        }

        pasoActual = 0;
        enPausa = false;
        historicoEstados = [];

        // Actualizar información (usar modeloId de simulación)
        const modelo = MODELOS[simulacionActual.modeloId];
        propModelo.textContent = modelo ? modelo.nombre : 'Modelo desconocido';
        propSemilla.textContent = `ID: ${simulacionActual.nodoSemillaId || 'Desconocido'}`;

        // Inicializar visualización
        visualizarPropagacion(nodosData, aristasData);
        actualizarMetricas();

        // Crear gráfica
        crearGrafica();

        // Auto-avanzar
        autoAvanzar();
    } catch (error) {
        console.error('Error al cargar propagación:', error);
        divPropagacionGrafo.innerHTML = `<p>❌ Error: ${error.message}</p>`;
    }
}

// ============================================
// VISUALIZACIÓN DEL GRAFO
// ============================================

function visualizarPropagacion(nodos, aristas) {
    // Preparar datos para vis.js
    const nodesData = new vis.DataSet(nodos.map(nodo => ({
        id: nodo.id,
        label: nodo.nombre,
        color: getEstadoColor(nodo.estadoId),
        size: 25,
        title: `${nodo.nombre} - ${getEstadoNombre(nodo.estadoId)}`
    })));

    const edgesData = new vis.DataSet(aristas.map((arista, idx) => ({
        id: idx,
        from: arista.nodo1Id,
        to: arista.nodo2Id,
        width: 1,
        color: '#444'
    })));

    const options = {
        physics: {
            enabled: false
        },
        interaction: {
            navigationButtons: true,
            keyboard: true
        }
    };

    const data = { nodes: nodesData, edges: edgesData };
    if (redPropagacion) redPropagacion.destroy();
    redPropagacion = new vis.Network(divPropagacionGrafo, data, options);
}

// ============================================
// ACTUALIZAR MÉTRICAS
// ============================================

async function actualizarMetricas() {
    try {
        const paso = pasosSimulacion[pasoActual];
        if (!paso) return;

        pasoActualEl.textContent = paso.numeroPaso;

        // Actualizar barras de estado
        const totalNodos = 250;
        const barras = {
            'no-informado': (paso.totalInformados - paso.totalPasivos - paso.totalActivos) / totalNodos,
            'activo': paso.totalActivos / totalNodos,
            'pasivo': paso.totalPasivos / totalNodos,
            'resistente': (totalNodos - paso.totalInformados) / totalNodos
        };

        // Actualizar valores
        document.getElementById('count-no-informado').textContent = 
            totalNodos - paso.totalInformados;
        document.getElementById('count-activo').textContent = paso.totalActivos;
        document.getElementById('count-pasivo').textContent = paso.totalPasivos;
        document.getElementById('count-resistente').textContent = 
            totalNodos - paso.totalInformados;

        // Actualizar barras visuales
        document.getElementById('bar-no-informado').style.width = 
            (barras['no-informado'] * 100) + '%';
        document.getElementById('bar-activo').style.width = 
            (barras['activo'] * 100) + '%';
        document.getElementById('bar-pasivo').style.width = 
            (barras['pasivo'] * 100) + '%';
        document.getElementById('bar-resistente').style.width = 
            (barras['resistente'] * 100) + '%';

        // Guardar en histórico
        historicoEstados.push({
            paso: paso.numeroPaso,
            noInformado: totalNodos - paso.totalInformados,
            activo: paso.totalActivos,
            pasivo: paso.totalPasivos,
            resistente: totalNodos - paso.totalInformados
        });

        // Actualizar gráfica
        actualizarGrafica();
    } catch (error) {
        console.error('Error al actualizar métricas:', error);
    }
}

// ============================================
// GRÁFICA DE PROPAGACIÓN
// ============================================

function crearGrafica() {
    if (chartPropagacion) {
        chartPropagacion.destroy();
    }

    chartPropagacion = new Chart(chartCanvas, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                {
                    label: 'NO_INFORMADO',
                    data: [],
                    borderColor: '#666',
                    backgroundColor: 'rgba(102, 102, 102, 0.1)',
                    tension: 0.4
                },
                {
                    label: 'INFORMADO_ACTIVO',
                    data: [],
                    borderColor: '#ff6b6b',
                    backgroundColor: 'rgba(255, 107, 107, 0.1)',
                    tension: 0.4
                },
                {
                    label: 'INFORMADO_PASIVO',
                    data: [],
                    borderColor: '#ffd93d',
                    backgroundColor: 'rgba(255, 217, 61, 0.1)',
                    tension: 0.4
                },
                {
                    label: 'RESISTENTE',
                    data: [],
                    borderColor: '#6bcf7f',
                    backgroundColor: 'rgba(107, 207, 127, 0.1)',
                    tension: 0.4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: { color: '#e8e8e8' }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max: 250,
                    ticks: { color: '#a0a0a0' },
                    grid: { color: '#2a3f5f' }
                },
                x: {
                    ticks: { color: '#a0a0a0' },
                    grid: { color: '#2a3f5f' }
                }
            }
        }
    });
}

function actualizarGrafica() {
    if (!chartPropagacion || historicoEstados.length === 0) return;

    const labels = historicoEstados.map(h => `Paso ${h.paso}`);
    const noInformado = historicoEstados.map(h => h.noInformado);
    const activo = historicoEstados.map(h => h.activo);
    const pasivo = historicoEstados.map(h => h.pasivo);
    const resistente = historicoEstados.map(h => h.resistente);

    chartPropagacion.data.labels = labels;
    chartPropagacion.data.datasets[0].data = noInformado;
    chartPropagacion.data.datasets[1].data = activo;
    chartPropagacion.data.datasets[2].data = pasivo;
    chartPropagacion.data.datasets[3].data = resistente;
    chartPropagacion.update();
}

// ============================================
// CONTROLES DE SIMULACIÓN
// ============================================

function siguientePaso() {
    if (pasoActual < pasosSimulacion.length - 1) {
        pasoActual++;
        actualizarMetricas();
    }
}

function reiniciarSimulacion() {
    // SALVAVIDAS: Limpiar timeout anterior
    if (timeoutAutoAvanzar) {
        clearTimeout(timeoutAutoAvanzar);
        timeoutAutoAvanzar = null;
    }

    pasoActual = 0;
    enPausa = false;
    historicoEstados = [];
    actualizarMetricas();
    crearGrafica();
    btnPausarSim.textContent = '⏸ Pausar';
    autoAvanzar();
}

function autoAvanzar() {
    // SALVAVIDAS: Limpiar timeout anterior si existe
    if (timeoutAutoAvanzar) {
        clearTimeout(timeoutAutoAvanzar);
        timeoutAutoAvanzar = null;
    }

    // SALVAVIDAS: Validar que pasosSimulacion existe y tiene datos
    if (!Array.isArray(pasosSimulacion) || pasosSimulacion.length === 0) {
        console.warn('⚠️ autoAvanzar: No hay pasos de simulación');
        return;
    }

    // SALVAVIDAS: Si ya llegamos al final, parar
    if (pasoActual >= pasosSimulacion.length - 1) {
        btnPausarSim.textContent = '✅ Finalizado';
        enPausa = true;
        return;
    }

    // Si está en pausa, no continuar
    if (enPausa) {
        return;
    }

    // Programar siguiente paso
    timeoutAutoAvanzar = setTimeout(() => {
        siguientePaso();
        autoAvanzar(); // Recursión controlada
    }, 800); // Un paso cada 800ms
}

btnSiguientePaso.addEventListener('click', siguientePaso);
btnReiniciarSim.addEventListener('click', reiniciarSimulacion);
btnPausarSim.addEventListener('click', () => {
    enPausa = !enPausa;
    if (enPausa) {
        // SALVAVIDAS: Limpiar timeout cuando se pausa
        if (timeoutAutoAvanzar) {
            clearTimeout(timeoutAutoAvanzar);
            timeoutAutoAvanzar = null;
        }
        btnPausarSim.textContent = '▶ Reanudar';
    } else {
        btnPausarSim.textContent = '⏸ Pausar';
        autoAvanzar();
    }
});

// ============================================
// INICIALIZACIÓN
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    const tabBtn = document.querySelector('[data-tab="propagacion"]');
    if (tabBtn) {
        tabBtn.addEventListener('click', cargarPropagacion);
    }
});
