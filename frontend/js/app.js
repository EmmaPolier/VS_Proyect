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
            ? `${grafos[0].nombre} (${grafos[0].id})`
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
        btnGenerarRed.textContent = '⏳ Generando red...';

        // Crear grafo
        const grafo = await crearGrafo('Red Watts-Strogatz 250 nodos');
        
        // En un proyecto real, aquí se llamaría al endpoint de generación
        // Por ahora simularemos que se generó correctamente
        
        setTimeout(async () => {
            await actualizarEstadisticas();
            btnGenerarRed.disabled = false;
            btnGenerarRed.textContent = '⚡ Generar Red Watts-Strogatz (250 nodos)';
            alert('✅ Red generada exitosamente. Ahora puedes editar nodos o lanzar simulaciones.');
        }, 2000);
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
        alert(`✅ Red cargada: ${grafos[0].nombre}`);
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

// Actualizar estadísticas cada 5 segundos
setInterval(actualizarEstadisticas, 5000);
