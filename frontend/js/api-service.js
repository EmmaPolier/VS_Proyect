/**
 * API Service - Cliente REST para comunicación con backend
 * Base URL: http://localhost:8080/api
 */

const API_BASE_URL = 'http://localhost:8080/api';

class APIService {
  /**
   * Obtiene todos los grafos
   */
  static async obtenerGrafos() {
    try {
      const response = await fetch(`${API_BASE_URL}/grafos`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error('Error obteniendo grafos:', error);
      throw error;
    }
  }

  /**
   * Crea un nuevo grafo
   */
  static async crearGrafo() {
    try {
      const response = await fetch(`${API_BASE_URL}/grafos/crear`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error('Error creando grafo:', error);
      throw error;
    }
  }

  /**
   * Obtiene un grafo por ID
   */
  static async obtenerGrafoPorId(grafoId) {
    try {
      const response = await fetch(`${API_BASE_URL}/grafos/${grafoId}`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error obteniendo grafo ${grafoId}:`, error);
      throw error;
    }
  }

  /**
   * Calcula las métricas de un grafo
   */
  static async calcularMetricas(grafoId) {
    try {
      const response = await fetch(`${API_BASE_URL}/grafos/${grafoId}/calcular-metricas`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error calculando métricas para grafo ${grafoId}:`, error);
      throw error;
    }
  }

  /**
   * Obtiene todos los nodos de un grafo
   */
  static async obtenerNodosPorGrafo(grafoId) {
    try {
      const response = await fetch(`${API_BASE_URL}/nodos/grafo/${grafoId}`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error obteniendo nodos del grafo ${grafoId}:`, error);
      throw error;
    }
  }

  /**
   * Obtiene un nodo por ID
   */
  static async obtenerNodoPorId(nodoId) {
    try {
      const response = await fetch(`${API_BASE_URL}/nodos/${nodoId}`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error obteniendo nodo ${nodoId}:`, error);
      throw error;
    }
  }

  /**
   * Crea un nuevo nodo en un grafo
   */
  static async crearNodo(grafoId, nombre) {
    try {
      const response = await fetch(`${API_BASE_URL}/nodos?grafoId=${grafoId}&nombre=${nombre}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error creando nodo:`, error);
      throw error;
    }
  }

  /**
   * Actualiza un nodo existente
   */
  static async actualizarNodo(nodoId, datosActualizacion) {
    try {
      const response = await fetch(`${API_BASE_URL}/nodos/${nodoId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datosActualizacion)
      });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error actualizando nodo ${nodoId}:`, error);
      throw error;
    }
  }

  /**
   * Obtiene los top nodos por grado (centralidad)
   */
  static async obtenerTopNodosPorGrado(grafoId) {
    try {
      const response = await fetch(`${API_BASE_URL}/nodos/grafo/${grafoId}/top-grado`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error obteniendo top nodos por grado:`, error);
      throw error;
    }
  }

  /**
   * Obtiene los top nodos por betweenness centrality
   */
  static async obtenerTopNodosPorBetweenness(grafoId) {
    try {
      const response = await fetch(`${API_BASE_URL}/nodos/grafo/${grafoId}/top-betweenness`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error obteniendo top nodos por betweenness:`, error);
      throw error;
    }
  }

  /**
   * Obtiene todas las aristas de un grafo
   */
  static async obtenerAristasPorGrafo(grafoId) {
    try {
      const response = await fetch(`${API_BASE_URL}/aristas/grafo/${grafoId}`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error obteniendo aristas del grafo ${grafoId}:`, error);
      throw error;
    }
  }

  /**
   * Obtiene una arista por ID
   */
  static async obtenerAristaPorId(aristaId) {
    try {
      const response = await fetch(`${API_BASE_URL}/aristas/${aristaId}`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error obteniendo arista ${aristaId}:`, error);
      throw error;
    }
  }

  /**
   * Crea una nueva arista entre dos nodos
   */
  static async crearArista(nodoOrigenId, nodoDestinoId) {
    try {
      const response = await fetch(
        `${API_BASE_URL}/aristas?nodoOrigenId=${nodoOrigenId}&nodoDestinoId=${nodoDestinoId}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' }
        }
      );
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error creando arista:`, error);
      throw error;
    }
  }

  /**
   * Crea una nueva simulación
   * @param {number} grafoId - ID del grafo
   * @param {number} nodoSemillaId - ID del nodo semilla
   * @param {number} modeloId - ID del modelo (1=viral, 2=cascade, 3=threshold)
   */
  static async crearSimulacion(grafoId, nodoSemillaId, modeloId) {
    try {
      const response = await fetch(`${API_BASE_URL}/simulaciones?grafoId=${grafoId}&nodoSemillaId=${nodoSemillaId}&modeloId=${modeloId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error('Error creando simulación:', error);
      throw error;
    }
  }

  /**
   * Ejecuta una simulación
   */
  static async ejecutarSimulacion(simulacionId) {
    try {
      const response = await fetch(`${API_BASE_URL}/simulaciones/${simulacionId}/ejecutar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error ejecutando simulación ${simulacionId}:`, error);
      throw error;
    }
  }

  /**
   * Obtiene los pasos de una simulación
   */
  static async obtenerPasosSimulacion(simulacionId) {
    try {
      const response = await fetch(`${API_BASE_URL}/simulaciones/${simulacionId}/pasos`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error obteniendo pasos de simulación ${simulacionId}:`, error);
      throw error;
    }
  }

  /**
   * Obtiene las métricas de una simulación
   */
  static async obtenerMetricasSimulacion(simulacionId) {
    try {
      const response = await fetch(`${API_BASE_URL}/simulaciones/${simulacionId}/metricas`);
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error(`Error obteniendo métricas de simulación ${simulacionId}:`, error);
      throw error;
    }
  }

  /**
   * Compara múltiples modelos de propagación
   */
  static async compararModelos(grafoId, nodoSemillaId, configuracion) {
    try {
      const response = await fetch(`${API_BASE_URL}/simulaciones/comparar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          grafoId,
          nodoSemillaId,
          ...configuracion
        })
      });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error('Error comparando modelos:', error);
      throw error;
    }
  }

  /**
   * Verifica la conexión con el backend
   */
  static async verificarConexion() {
    try {
      const response = await fetch(`${API_BASE_URL}/grafos`);
      return response.ok;
    } catch (error) {
      console.error('Error conectando con backend:', error);
      return false;
    }
  }
}
