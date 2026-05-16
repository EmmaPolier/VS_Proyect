/**
 * VISTA-NODOS.js - Gestión y edición de nodos
 */

let grafoActual = null;
let nodosActuales = [];
let paginaActual = 1;
const NODOS_POR_PAGINA = 8;

// Elementos DOM
const tbodyNodos = document.getElementById('tbody-nodos');
const searchInput = document.getElementById('search-nodos');
const filterSelect = document.getElementById('filter-estado');
const btnPrevPag = document.getElementById('btn-prev-pag');
const btnNextPag = document.getElementById('btn-next-pag');
const pagInfo = document.getElementById('pag-info');
const modalEditor = document.getElementById('modal-editor-nodo');
const btnGuardarNodo = document.getElementById('btn-guardar-nodo');
const btnCancelarNodo = document.getElementById('btn-cancelar-nodo');
const sliderProbabilidad = document.getElementById('edit-probabilidad');
const sliderUmbral = document.getElementById('edit-umbral');
const valProb = document.getElementById('val-prob');
const valUmbral = document.getElementById('val-umbral');

let nodoEnEdicion = null;

// ============================================
// CARGAR Y RENDERIZAR NODOS
// ============================================

async function cargarNodos() {
    try {
        // Obtener el grafo actual (el primero disponible)
        const grafos = await obtenerGrafos();
        if (grafos.length === 0) {
            tbodyNodos.innerHTML = '<tr><td colspan="7" class="text-center">No hay redes disponibles. Genera una primero.</td></tr>';
            return;
        }

        grafoActual = grafos[0];
        nodosActuales = await obtenerNodosGrafo(grafoActual.id);
        
        if (!Array.isArray(nodosActuales)) {
            nodosActuales = [];
        }

        paginaActual = 1;
        renderizarNodos();
    } catch (error) {
        console.error('Error al cargar nodos:', error);
        tbodyNodos.innerHTML = '<tr><td colspan="7" class="text-center">Error al cargar nodos</td></tr>';
    }
}

function renderizarNodos() {
    // Aplicar filtros
    let nodosFiltrados = nodosActuales.filter(nodo => {
        const matchSearch = nodo.nombre.toLowerCase().includes(searchInput.value.toLowerCase()) ||
                           nodo.id.toString().includes(searchInput.value);
        const matchEstado = filterSelect.value === '' || nodo.estadoId === parseInt(filterSelect.value);
        return matchSearch && matchEstado;
    });

    // Paginar
    const totalPaginas = Math.ceil(nodosFiltrados.length / NODOS_POR_PAGINA);
    const inicio = (paginaActual - 1) * NODOS_POR_PAGINA;
    const fin = inicio + NODOS_POR_PAGINA;
    const nodosPagina = nodosFiltrados.slice(inicio, fin);

    // Renderizar tabla
    if (nodosPagina.length === 0) {
        tbodyNodos.innerHTML = '<tr><td colspan="7" class="text-center">No hay nodos que coincidan con los filtros</td></tr>';
    } else {
        tbodyNodos.innerHTML = nodosPagina.map(nodo => `
            <tr>
                <td>${nodo.id}</td>
                <td>${nodo.nombre}</td>
                <td>
                    <span class="badge badge-${getEstadoBadge(nodo.estadoId)}">
                        ${getEstadoLabel(nodo.estadoId)} ${getEstadoNombre(nodo.estadoId)}
                    </span>
                </td>
                <td>${(nodo.probabilidadPropagacion || 0).toFixed(2)}</td>
                <td>${(nodo.umbral || 0).toFixed(2)}</td>
                <td>${nodo.grado || 0}</td>
                <td>
                    <button class="btn btn-secondary" onclick="editarNodo(${nodo.id})">✏️ Editar</button>
                </td>
            </tr>
        `).join('');
    }

    // Actualizar paginador
    pagInfo.textContent = `Página ${paginaActual} de ${Math.max(1, totalPaginas)} (${nodosFiltrados.length} nodos)`;
    btnPrevPag.disabled = paginaActual === 1;
    btnNextPag.disabled = paginaActual >= totalPaginas;
}

function getEstadoBadge(estadoId) {
    switch (estadoId) {
        case 0: return 'info';
        case 1: return 'danger';
        case 2: return 'warning';
        case 3: return 'success';
        default: return 'info';
    }
}

// ============================================
// EDITAR NODO
// ============================================

async function editarNodo(id) {
    try {
        nodoEnEdicion = await obtenerNodo(id);
        if (!nodoEnEdicion) {
            alert('Error al cargar el nodo');
            return;
        }

        // Rellenar el modal
        document.getElementById('edit-nodo-id').textContent = nodoEnEdicion.id;
        document.getElementById('edit-nombre').value = nodoEnEdicion.nombre;
        document.getElementById('edit-estado').value = nodoEnEdicion.estadoId;
        sliderProbabilidad.value = nodoEnEdicion.probabilidadPropagacion || 0.5;
        sliderUmbral.value = nodoEnEdicion.umbral || 0.5;
        
        actualizarValoresSliders();
        modalEditor.classList.remove('hidden');
    } catch (error) {
        console.error('Error al editar nodo:', error);
        alert('Error al cargar los datos del nodo');
    }
}

function actualizarValoresSliders() {
    valProb.textContent = parseFloat(sliderProbabilidad.value).toFixed(2);
    valUmbral.textContent = parseFloat(sliderUmbral.value).toFixed(2);
}

async function guardarNodo() {
    try {
        const datos = {
            nombre: document.getElementById('edit-nombre').value,
            estadoId: parseInt(document.getElementById('edit-estado').value),
            probabilidadPropagacion: parseFloat(sliderProbabilidad.value),
            umbral: parseFloat(sliderUmbral.value)
        };

        await actualizarNodo(nodoEnEdicion.id, datos);
        
        // Actualizar en local
        const nodoIdx = nodosActuales.findIndex(n => n.id === nodoEnEdicion.id);
        if (nodoIdx !== -1) {
            nodosActuales[nodoIdx] = { ...nodosActuales[nodoIdx], ...datos };
        }

        renderizarNodos();
        modalEditor.classList.add('hidden');
        alert('✅ Nodo actualizado correctamente');
    } catch (error) {
        console.error('Error al guardar nodo:', error);
        alert('❌ Error al guardar el nodo: ' + error.message);
    }
}

function cancelarEdicion() {
    modalEditor.classList.add('hidden');
}

// ============================================
// EVENT LISTENERS
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    // Búsqueda y filtros
    searchInput.addEventListener('input', () => {
        paginaActual = 1;
        renderizarNodos();
    });

    filterSelect.addEventListener('change', () => {
        paginaActual = 1;
        renderizarNodos();
    });

    // Paginación
    btnPrevPag.addEventListener('click', () => {
        if (paginaActual > 1) {
            paginaActual--;
            renderizarNodos();
        }
    });

    btnNextPag.addEventListener('click', () => {
        paginaActual++;
        renderizarNodos();
    });

    // Modal editor
    btnGuardarNodo.addEventListener('click', guardarNodo);
    btnCancelarNodo.addEventListener('click', cancelarEdicion);

    // Sliders
    sliderProbabilidad.addEventListener('input', actualizarValoresSliders);
    sliderUmbral.addEventListener('input', actualizarValoresSliders);

    // Cargar nodos cuando se abra la tab
    const tabBtn = document.querySelector('[data-tab="nodos"]');
    if (tabBtn) {
        tabBtn.addEventListener('click', cargarNodos);
    }
});

// Cargar nodos al inicio
document.addEventListener('DOMContentLoaded', cargarNodos);
