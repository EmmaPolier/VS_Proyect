/**
 * API.js - Funciones para comunicarse con el backend ViralSim
 */

const API_BASE_URL = 'http://localhost:8080/api';

// ============================================
// GRAFOS
// ============================================

async function crearGrafo(nombre) {
    try {
        const response = await fetch(`${API_BASE_URL}/grafos/crear`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombre })
        });
        return await response.json();
    } catch (error) {
        console.error('Error al crear grafo:', error);
        throw error;
    }
}

async function obtenerGrafos() {
    try {
        const response = await fetch(`${API_BASE_URL}/grafos`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener grafos:', error);
        return [];
    }
}

async function obtenerGrafo(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/grafos/${id}`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener grafo:', error);
        return null;
    }
}

// ============================================
// NODOS
// ============================================

async function obtenerNodosGrafo(grafoId) {
    try {
        const response = await fetch(`${API_BASE_URL}/nodos/grafo/${grafoId}`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener nodos:', error);
        return [];
    }
}

async function obtenerNodo(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/nodos/${id}`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener nodo:', error);
        return null;
    }
}

async function actualizarNodo(id, datos) {
    try {
        const response = await fetch(`${API_BASE_URL}/nodos/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(datos)
        });
        return await response.json();
    } catch (error) {
        console.error('Error al actualizar nodo:', error);
        throw error;
    }
}

async function obtenerTopGrado(grafoId) {
    try {
        const response = await fetch(`${API_BASE_URL}/nodos/grafo/${grafoId}/top-grado`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener top grado:', error);
        return [];
    }
}

async function obtenerTopBetweenness(grafoId) {
    try {
        const response = await fetch(`${API_BASE_URL}/nodos/grafo/${grafoId}/top-betweenness`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener top betweenness:', error);
        return [];
    }
}

// ============================================
// ARISTAS
// ============================================

async function obtenerAristasGrafo(grafoId) {
    try {
        const response = await fetch(`${API_BASE_URL}/aristas/grafo/${grafoId}`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener aristas:', error);
        return [];
    }
}

// ============================================
// SIMULACIONES
// ============================================

async function crearSimulacion(datos) {
    try {
        const response = await fetch(`${API_BASE_URL}/simulaciones`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(datos)
        });
        return await response.json();
    } catch (error) {
        console.error('Error al crear simulación:', error);
        throw error;
    }
}

async function obtenerSimulaciones() {
    try {
        const response = await fetch(`${API_BASE_URL}/simulaciones`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener simulaciones:', error);
        return [];
    }
}

async function obtenerSimulacion(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/simulaciones/${id}`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener simulación:', error);
        return null;
    }
}

async function ejecutarSimulacion(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/simulaciones/${id}/ejecutar`, {
            method: 'POST'
        });
        return await response.json();
    } catch (error) {
        console.error('Error al ejecutar simulación:', error);
        throw error;
    }
}

async function obtenerMetricasSimulacion(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/simulaciones/${id}/metricas`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener métricas:', error);
        return null;
    }
}

async function obtenerPasosSimulacion(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/simulaciones/${id}/pasos`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener pasos:', error);
        return [];
    }
}

async function obtenerNodoSimulacion(id, paso) {
    try {
        const response = await fetch(`${API_BASE_URL}/simulaciones/${id}/nodo-simulacion?paso=${paso}`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener nodo simulación:', error);
        return [];
    }
}

// ============================================
// CONFIGURACIONES
// ============================================

async function crearConfiguracion(datos) {
    try {
        const response = await fetch(`${API_BASE_URL}/configuraciones`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(datos)
        });
        return await response.json();
    } catch (error) {
        console.error('Error al crear configuración:', error);
        throw error;
    }
}

async function obtenerConfiguraciones() {
    try {
        const response = await fetch(`${API_BASE_URL}/configuraciones`);
        return await response.json();
    } catch (error) {
        console.error('Error al obtener configuraciones:', error);
        return [];
    }
}

// ============================================
// ESTADOS
// ============================================

const ESTADOS = {
    0: { nombre: 'NO_INFORMADO', color: '#666666', label: '⚪' },
    1: { nombre: 'INFORMADO_ACTIVO', color: '#ff6b6b', label: '🔴' },
    2: { nombre: 'INFORMADO_PASIVO', color: '#ffd93d', label: '🟡' },
    3: { nombre: 'RESISTENTE', color: '#6bcf7f', label: '🟢' }
};

const MODELOS = {
    1: {
        id: 1,
        nombre: 'Modelo Viral',
        descripcion: 'Cada nodo se propaga con su propia probabilidad de forma independiente'
    },
    2: {
        id: 2,
        nombre: 'Cascada Independiente',
        descripcion: 'La propagación ocurre a través de aristas con un único intento de activación'
    },
    3: {
        id: 3,
        nombre: 'Modelo de Umbral',
        descripcion: 'Adopción basada en presión social - un nodo se activa cuando el porcentaje de vecinos activos supera su umbral'
    }
};

// ============================================
// UTILIDADES
// ============================================

function getEstadoColor(estadoId) {
    return ESTADOS[estadoId]?.color || '#999999';
}

function getEstadoNombre(estadoId) {
    return ESTADOS[estadoId]?.nombre || 'DESCONOCIDO';
}

function getEstadoLabel(estadoId) {
    return ESTADOS[estadoId]?.label || '❓';
}

function getModeloInfo(modeloId) {
    return MODELOS[modeloId] || null;
}

// ============================================
// GENERACIÓN DE RED
// ============================================

async function generarRedWattsStrogatz() {
    try {
        // Primero creamos un grafo
        const grafo = await crearGrafo('Red Watts-Strogatz 250 nodos');
        
        // Después ejecutamos el generador (necesita un endpoint específico)
        const response = await fetch(`${API_BASE_URL}/grafos/${grafo.id}/generar-watts-strogatz`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                n: 250,
                k: 6,
                p: 0.1
            })
        });
        
        if (response.ok) {
            return await response.json();
        }
        return grafo;
    } catch (error) {
        console.error('Error al generar red:', error);
        throw error;
    }
}
