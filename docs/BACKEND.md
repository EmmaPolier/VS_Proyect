# Especificación del Backend - ViralSim

## Paquete: `com.viralsim.models` (Estructuras de Datos)

### Clase: `Estado.java`
**Tipo**: Enumeración
**Propósito**: Define los 4 estados posibles de un nodo
**Valores**:
- `NO_INFORMADO` (0)
- `INFORMADO_ACTIVO` (1)
- `INFORMADO_PASIVO` (2)
- `RESISTENTE` (3)

### Clase: `Nodo.java`
**Propósito**: Representa un usuario en la red
**Atributos**:
- `id: int` - Identificador único
- `estado: Estado` - Estado actual
- `pasoInfeccion: int` - Paso en que se informó (-1 si no)
- `vecinos: List<Nodo>` - Nodos conectados
- `probabilidadPropagacion: double` - Probabilidad individual (0.0-1.0)
- `umbral: double` - Umbral para modelo Threshold (0.1-0.9)
- `centralidadGrado: double` - Número vecinos / 249
- `betweenness: double` - Centralidad de intermediación
- `padre: Nodo` - Para trazabilidad BFS/DFS

### Clase: `Arista.java`
**Propósito**: Representa una amistad/conexión entre dos nodos
**Atributos**:
- `id: int` - Identificador único
- `nodoOrigen: Nodo` - Nodo origen
- `nodoDestino: Nodo` - Nodo destino
- `probabilidadArista: double` - Probabilidad de esta conexión (0.0-1.0)
- `activa: boolean` - Flag para modelo Cascada (un intento)
- `peso: double` - Peso/confianza de la amistad

### Clase: `Grafo.java`
**Propósito**: Representa la red social completa
**Atributos**:
- `nodos: List<Nodo>` - Los 250 nodos
- `aristas: List<Arista>` - Todas las conexiones
- `mapaNodos: HashMap<Integer, Nodo>` - Para acceso O(1) por ID
- `topologia: String` - "Watts-Strogatz"

**Métodos clave**:
- `getNodoById(id): Nodo`
- `obtenerVecinos(nodo): List<Nodo>`
- `obtenerAristasDeNodo(nodo): List<Arista>`
- `agregarNodo(nodo): void`
- `agregarArista(arista): void`

---

## Paquete: `com.viralsim.propagation` (Modelos)

### Interfaz: `ModeloPropagacion.java`
**Propósito**: Define el contrato para todos los modelos
**Métodos**:
- `propagar(grafo, nodo): List<Nodo>` - Ejecuta un paso de propagación
- `reiniciar(): void` - Reset del modelo
- `nombre(): String` - Nombre del modelo
- `descripcion(): String` - Descripción

### Clase: `ModeloViral.java` (implements ModeloPropagacion)
**Lógica**: Cada nodo contagia a vecinos no informados
- Recorre vecinos del nodo activo
- Genera número aleatorio para cada vecino
- Si random ≤ probabilidadPropagacion, el vecino se infecta

### Clase: `ModeloCascadaIndependiente.java` (implements ModeloPropagacion)
**Lógica**: Probabilidad en aristas, un intento por relación
- Recorre aristas del nodo activo (solo activas)
- Genera número aleatorio por arista
- Si random ≤ probabilidadArista, vecino se infecta
- Marca arista como inactiva (no más intentos)

### Clase: `ModeloUmbralLineal.java` (implements ModeloPropagacion)
**Lógica**: Contagio por presión social
- Recorre TODOS los nodos no informados
- Calcula fracción = vecinosInformados / totalVecinos
- Si fracción ≥ umbralNodo, nodo se infecta

---

## Paquete: `com.viralsim.engine` (Motor)

### Clase: `MotorSimulacion.java`
**Propósito**: Orquesta la ejecución completa de una simulación
**Atributos**:
- `grafo: Grafo` - Red a simular
- `modelo: ModeloPropagacion` - Modelo activo
- `pasoActual: int` - Paso de simulación actual
- `historial: List<EventoSimulacion>` - Registro de eventos
- `resultados: SimulacionResultado` - Resultados finales

**Métodos clave**:
- `iniciarSimulacion(nodoOrigen): void` - Inicia con un nodo
- `ejecutarPaso(): boolean` - Ejecuta un paso, retorna si continúa
- `detenerSimulacion(): void` - Termina la simulación
- `obtenerResultados(): SimulacionResultado` - Retorna resultados

### Clase: `EventoSimulacion.java`
**Propósito**: Registro de un cambio de estado
**Atributos**:
- `paso: int` - Número de paso
- `nodo: Nodo` - Nodo que cambió
- `estadoAnterior: Estado`
- `estadoNuevo: Estado`
- `timestamp: long` - Cuándo ocurrió

### Clase: `SimulacionResultado.java`
**Propósito**: Contiene todos los resultados de una ejecución
**Atributos**:
- `simulacionId: int` - ID en BD
- `modelo: String` - Nombre del modelo usado
- `nodoOrigen: int` - ID del nodo que inició
- `pasoFinal: int` - Total de pasos ejecutados
- `alcanceTotal: double` - % de alcance final
- `paso50Porciento: int` - Paso en que alcanzó 50%
- `estadosFinales: Map<Estado, Integer>` - Conteo de nodos por estado
- `historialCompleto: List<EventoSimulacion>`

---

## Paquete: `com.viralsim.metrics` (Métricas)

### Clase: `CalculadorMetricas.java`
**Propósito**: Calcula métricas en tiempo real O(1)
**Atributos**:
- `grafo: Grafo` - Referencia a la red
- `mapaNodos: HashMap<Integer, Nodo>` - Acceso rápido

**Métodos clave**:
- `calcularAlcance(): double` - % de nodos informados
- `calcularAlcancePorEstado(): Map<Estado, Integer>` - Conteo por estado
- `obtenerNodosMasInfluyentes(cantidad): List<Nodo>` - Top N por grado
- `obtenerNodosMasCentrales(cantidad): List<Nodo>` - Top N por betweenness
- `calcularMetricasPaso(): Map<String, Object>` - Todas para este paso

### Clase: `MetricaPaso.java`
**Propósito**: Snapshot de métricas en un paso
**Atributos**:
- `paso: int`
- `alcance: double`
- `nodosInformados: int`
- `nodosActivos: int`
- `nodosPasivos: int`
- `nodosResistentes: int`
- `timestamp: long`

---

## Paquete: `com.viralsim.database` (Capa de Datos)

### Clase: `ConexionBD.java`
**Propósito**: Gestiona conexión a MySQL
**Atributos**:
- `url: String` - URL de conexión
- `usuario: String`
- `contraseña: String`
- `conexion: Connection` - Objeto de conexión activo

**Métodos clave**:
- `conectar(): void` - Abre conexión
- `desconectar(): void` - Cierra conexión
- `obtenerConexion(): Connection` - Retorna conexión actual

### Clase: `NodoDAO.java`
**Operaciones**:
- `crear(nodo): int` - INSERT, retorna ID generado
- `obtenerPorId(id): Nodo` - SELECT por ID
- `obtenerTodos(): List<Nodo>` - SELECT todos los 250
- `actualizar(nodo): void` - UPDATE
- `eliminar(id): void` - DELETE
- `obtenerPorEstado(estado): List<Nodo>` - SELECT filtrado

### Clase: `AristaDAO.java`
**Operaciones**:
- `crear(arista): int` - INSERT
- `obtenerPorId(id): Arista` - SELECT
- `obtenerAristasDeNodo(nodoId): List<Arista>` - SELECT relaciones
- `actualizar(arista): void` - UPDATE
- `eliminar(id): void` - DELETE

### Clase: `SimulacionDAO.java`
**Operaciones**:
- `crear(simulacion): int` - INSERT nueva simulación
- `obtenerPorId(id): SimulacionResultado` - SELECT
- `obtenerTodas(): List<SimulacionResultado>` - SELECT todas
- `actualizar(simulacion): void` - UPDATE

### Clase: `HistorialDAO.java`
**Operaciones**:
- `crear(evento): void` - INSERT evento
- `obtenerPorSimulacion(simId): List<EventoSimulacion>` - SELECT todos eventos
- `obtenerPorPaso(simId, paso): List<EventoSimulacion>` - SELECT por paso

### Clase: `MetricaDAO.java`
**Operaciones**:
- `guardar(metrica): void` - INSERT métrica
- `obtenerPorSimulacion(simId): List<MetricaPaso>` - SELECT todos pasos

---

## Paquete: `com.viralsim.utils` (Utilidades)

### Clase: `GeneradorAleatorios.java`
**Métodos clave**:
- `numeroAleatorio(min, max): double` - Número entre min y max
- `booleanoAleatorio(probabilidad): boolean` - true con probabilidad P
- `seleccionarAleatorio(lista): T` - Elemento aleatorio de lista

### Clase: `GeneradorWattsStrogatz.java`
**Propósito**: Genera la topología de mundo pequeño
**Parámetros**:
- n=250 nodos
- k=6 vecinos iniciales
- p=0.1 probabilidad de reconexión

**Método clave**:
- `generar(): Grafo` - Retorna grafo con topología Watts-Strogatz

### Clase: `GeneradorBetweenness.java`
**Propósito**: Calcula betweenness centrality
**Método clave**:
- `calcularBetweenness(grafo): Map<Integer, Double>` - Usa algoritmo de Brandes (JGraphT)

### Clase: `Exportador.java`
**Propósito**: Genera JSON para enviar al frontend
**Métodos clave**:
- `exportarGrafo(grafo): String` - JSON del grafo
- `exportarHistorial(historial): String` - JSON del historial
- `exportarMetricas(metricas): String` - JSON de métricas

---

## Flujo de Ejecución del Motor

```
MotorSimulacion.iniciarSimulacion(nodoOrigen)
  ├─ nodoOrigen.estado = INFORMADO_ACTIVO
  ├─ nodoOrigen.pasoInfeccion = 0
  ├─ guardarEnBD (NodoDAO.actualizar)
  └─ paso = 1
  
Bucle: while (hayNodosActivos)
  ├─ nodosActivos = Grafo.getNodos(estado==INFORMADO_ACTIVO)
  ├─ Para cada nodo en nodosActivos:
  │  ├─ nuevosInformados = Modelo.propagar(nodo, grafo)
  │  └─ Para cada nuevo en nuevosInformados:
  │     ├─ nuevo.estado = INFORMADO_ACTIVO
  │     └─ guardarEnBD (HistorialDAO.crear)
  ├─ CalculadorMetricas.calcularMetricasPaso()
  ├─ MetricaDAO.guardarPaso()
  └─ paso++
  
MotorSimulacion.terminar()
  ├─ CalculadorMetricas.calcularMetricasFinales()
  ├─ MetricaDAO.guardarFinales()
  └─ Exportador.generarJSON()
```

