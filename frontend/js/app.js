/**
 * APP.js - Lógica principal de la aplicación
 */

// ============================================
// NAVEGACIÓN DE TABS
// ============================================

const tabBtns = document.querySelectorAll('.tab-btn');
const tabContents = document.querySelectorAll('.tab-content');

tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
        const tabName = btn.getAttribute('data-tab');
        
        // SALVAVIDAS: Limpiar timers de simulación anterior antes de cambiar tab
        if (typeof timeoutAutoAvanzar !== 'undefined' && timeoutAutoAvanzar) {
            clearTimeout(timeoutAutoAvanzar);
            timeoutAutoAvanzar = null;
        }
        
        // Desactivar todas las tabs
        tabContents.forEach(tab => tab.classList.remove('active'));
        tabBtns.forEach(b => b.classList.remove('active'));
        
        // Activar tab seleccionada
        document.getElementById(tabName).classList.add('active');
        btn.classList.add('active');

        // Trigger event para cargar datos
        if (tabName === 'nodos') {
            cargarNodos();
        } else if (tabName === 'chisme') {
            cargarGrafoChisme();
        } else if (tabName === 'propagacion') {
            cargarPropagacion();
        }
    });
});

// ============================================
// TAB INICIO
// ============================================

async function actualizarEstadisticas() {
    try {
        // Grafos
        const grafos = await obtenerGrafos();
        document.getElementById('stat-grafo').textContent = grafos.length > 0 
            ? `ID: ${grafos[0].id} (${grafos[0].totalNodos} nodos)`
            : 'Ninguna';

        // Nodos
        if (grafos.length > 0) {
            const nodos = await obtenerNodosGrafo(grafos[0].id);
            document.getElementById('stat-nodos').textContent = nodos.length;
        } else {
            document.getElementById('stat-nodos').textContent = '0';
        }

        // Simulaciones
        const sims = await obtenerSimulaciones();
        document.getElementById('stat-simulaciones').textContent = sims.length;
    } catch (error) {
        console.error('Error al actualizar estadísticas:', error);
    }
}

// ============================================
// GENERACIÓN DE RED
// ============================================

const btnGenerarRed = document.getElementById('btn-generar-red');
const btnCargarExistente = document.getElementById('btn-cargar-existente');

btnGenerarRed.addEventListener('click', async () => {
    try {
        btnGenerarRed.disabled = true;
        btnGenerarRed.textContent = '⏳ Generando red (puede tomar ~10 segundos)...';

        // Crear grafo Watts-Strogatz
        const response = await fetch(`${API_BASE_URL}/grafos/crear`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        const grafo = await response.json();
        
        await actualizarEstadisticas();
        btnGenerarRed.disabled = false;
        btnGenerarRed.textContent = '⚡ Generar Red Watts-Strogatz (250 nodos)';
        alert('✅ Red generada exitosamente. Ahora puedes editar nodos o lanzar simulaciones.');
    } catch (error) {
        console.error('Error al generar red:', error);
        alert('❌ Error al generar la red: ' + error.message);
        btnGenerarRed.disabled = false;
        btnGenerarRed.textContent = '⚡ Generar Red Watts-Strogatz (250 nodos)';
    }
});

btnCargarExistente.addEventListener('click', async () => {
    try {
        const grafos = await obtenerGrafos();
        if (grafos.length === 0) {
            alert('No hay redes disponibles. Genera una primero.');
            return;
        }
        
        await actualizarEstadisticas();
        alert(`✅ Red cargada: ID ${grafos[0].id} (${grafos[0].totalNodos} nodos)`);
    } catch (error) {
        console.error('Error al cargar red:', error);
        alert('❌ Error al cargar la red');
    }
});

// ============================================
// INICIALIZACIÓN
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    // Cargar estadísticas iniciales
    actualizarEstadisticas();

    // Establecer la tab inicial
    document.querySelector('[data-tab="inicio"]').click();
});

// SALVAVIDAS: Intervalo con ID para poder cancelarlo después
let intervalEstadisticas = setInterval(actualizarEstadisticas, 5000);
