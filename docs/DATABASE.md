# Esquema de Base de Datos - ViralSim

## Descripción General
Base de datos MySQL con 5 tablas principales para persistir la red, simulaciones, historial e métricas.

---

## Tabla: `nodos`

**Propósito**: Almacena información de los 250 nodos (usuarios)

**Columnas**:
```
id (INT, PK, AI)              - Identificador único
nombre (VARCHAR(100))          - Nombre del usuario (opcional)
estado (ENUM)                  - NO_INFORMADO, INFORMADO_ACTIVO, INFORMADO_PASIVO, RESISTENTE
paso_infeccion (INT, NULL)     - Paso en que se infectó (-1 o NULL si no)
probabilidad_propagacion (FLOAT) - Probabilidad individual (0.0-1.0)
umbral (FLOAT)                 - Umbral para modelo Threshold (0.1-0.9)
centralidad_grado (FLOAT)      - Grado / 249
betweenness_centrality (FLOAT) - Medida de intermediación
padre_id (INT, NULL, FK)       - ID del nodo padre (para trazabilidad)
fecha_creacion (TIMESTAMP)     - Cuándo se creó el nodo
fecha_actualizacion (TIMESTAMP) - Última actualización
```

**Índices**:
- PRIMARY KEY (id)
- INDEX (estado)
- INDEX (paso_infeccion)
- FOREIGN KEY (padre_id) → nodos(id)

---

## Tabla: `aristas`

**Propósito**: Almacena las conexiones (amistades) entre nodos

**Columnas**:
```
id (INT, PK, AI)              - Identificador único
nodo_origen_id (INT, FK)      - ID nodo origen
nodo_destino_id (INT, FK)     - ID nodo destino
probabilidad_arista (FLOAT)   - Probabilidad de transmisión en esta arista
activa (BOOLEAN, DEFAULT 1)   - Flag para modelo Cascada (1=activa, 0=bloqueada)
peso (FLOAT, DEFAULT 1.0)     - Peso de la conexión
fecha_creacion (TIMESTAMP)
```

**Índices**:
- PRIMARY KEY (id)
- FOREIGN KEY (nodo_origen_id) → nodos(id)
- FOREIGN KEY (nodo_destino_id) → nodos(id)
- INDEX (nodo_origen_id, nodo_destino_id)
- INDEX (activa)

---

## Tabla: `simulaciones`

**Propósito**: Metadata de cada ejecución de simulación

**Columnas**:
```
id (INT, PK, AI)              - Identificador único
nombre (VARCHAR(100))         - Nombre descriptivo
modelo (VARCHAR(50))          - "Viral", "Cascada", "Threshold"
nodo_origen_id (INT, FK)      - Nodo que inició el rumor
paso_final (INT)              - Total de pasos ejecutados
alcance_total (FLOAT)         - % de nodos informados
paso_50_porciento (INT)       - Paso en que alcanzó 50% (velocidad)
estado_simulacion (ENUM)      - "COMPLETADA", "PAUSADA", "ERROR"
fecha_inicio (TIMESTAMP)
fecha_fin (TIMESTAMP)
fecha_creacion (TIMESTAMP)
notas (TEXT)                  - Observaciones
```

**Índices**:
- PRIMARY KEY (id)
- FOREIGN KEY (nodo_origen_id) → nodos(id)
- INDEX (modelo)
- INDEX (fecha_creacion)

---

## Tabla: `historial_infeccion`

**Propósito**: Registro detallado de cada cambio de estado (animación)
**Esta es la tabla más importante para reproducir la simulación**

**Columnas**:
```
id (INT, PK, AI)              - Identificador único
simulacion_id (INT, FK)       - A qué simulación pertenece
paso (INT)                    - Número de paso
nodo_id (INT, FK)             - Qué nodo cambió
estado_anterior (ENUM)        - Estado antes
estado_nuevo (ENUM)           - Estado después
padre_id (INT, FK, NULL)      - Quién lo contagió
timestamp (TIMESTAMP)         - Cuándo ocurrió
```

**Índices**:
- PRIMARY KEY (id)
- FOREIGN KEY (simulacion_id) → simulaciones(id)
- FOREIGN KEY (nodo_id) → nodos(id)
- FOREIGN KEY (padre_id) → nodos(id)
- INDEX (simulacion_id, paso) - Para recuperar un paso completo
- INDEX (nodo_id)

---

## Tabla: `metricas`

**Propósito**: Snapshot de métricas en cada paso + finales

**Columnas**:
```
id (INT, PK, AI)              - Identificador único
simulacion_id (INT, FK)       - A qué simulación pertenece
paso (INT)                    - Número de paso
alcance_porcentaje (FLOAT)    - % de nodos informados
nodos_informados (INT)        - Cantidad absoluta
nodos_activos (INT)           - Nodos INFORMADO_ACTIVO
nodos_pasivos (INT)           - Nodos INFORMADO_PASIVO
nodos_resistentes (INT)       - Nodos RESISTENTE
nodos_no_informados (INT)     - Nodos NO_INFORMADO
velocidad_propagacion (INT)   - Paso del 50% (NULL si no alcanza)
timestamp (TIMESTAMP)
```

**Índices**:
- PRIMARY KEY (id)
- FOREIGN KEY (simulacion_id) → simulaciones(id)
- INDEX (simulacion_id, paso) - Para gráficas de evolución

---

## Tabla: `nodos_influyentes` (Opcional)

**Propósito**: Guarda top N nodos por centralidad (para caché)

**Columnas**:
```
id (INT, PK, AI)
nodo_id (INT, FK)
grado_centralidad (FLOAT)
betweenness_centrality (FLOAT)
rango_grado (INT)             - 1, 2, 3... (top)
rango_betweenness (INT)
fecha_actualizacion (TIMESTAMP)
```

---

## Relaciones Entre Tablas

```
simulaciones
    ├─ FK(nodo_origen_id) → nodos(id)
    └─ 1 ─→ ∞ historial_infeccion
           └─ FK(simulacion_id)
    └─ 1 ─→ ∞ metricas
           └─ FK(simulacion_id)

nodos
    ├─ 1 ─→ ∞ aristas (como nodo_origen)
    ├─ 1 ─→ ∞ aristas (como nodo_destino)
    ├─ 0..1 ─→ 1 nodos (padre_id, self-reference)
    └─ 1 ─→ ∞ historial_infeccion

aristas
    ├─ FK(nodo_origen_id) → nodos(id)
    └─ FK(nodo_destino_id) → nodos(id)

historial_infeccion
    ├─ FK(simulacion_id) → simulaciones(id)
    ├─ FK(nodo_id) → nodos(id)
    └─ FK(padre_id) → nodos(id)

metricas
    └─ FK(simulacion_id) → simulaciones(id)
```

---

## Datos Iniciales

- **250 nodos** creados con topología Watts-Strogatz
- Cada nodo tiene:
  - `probabilidad_propagacion`: aleatorio entre 0.4 y 0.9
  - `umbral`: aleatorio entre 0.1 y 0.9
  - `centralidad_grado` y `betweenness_centrality` precalculados
- **Aristas** generadas por Watts-Strogatz (~750 aristas, k=6 vecinos)
- Cada arista tiene `probabilidad_arista`: aleatorio entre 0.3 y 0.95

---

## Consultas Frecuentes

### Obtener todos los nodos informados en un paso
```sql
SELECT DISTINCT n.* FROM nodos n
JOIN historial_infeccion h ON n.id = h.nodo_id
WHERE h.simulacion_id = ? AND h.paso <= ?
AND h.estado_nuevo IN ('INFORMADO_ACTIVO', 'INFORMADO_PASIVO')
```

### Obtener árbol de contagio de un nodo
```sql
SELECT n.id, n.nombre, h.paso, h.padre_id
FROM historial_infeccion h
JOIN nodos n ON h.nodo_id = n.id
WHERE h.simulacion_id = ? AND h.nodo_id = ?
ORDER BY h.paso
```

### Top 5 nodos más influyentes (por grado)
```sql
SELECT n.* FROM nodos n
ORDER BY n.centralidad_grado DESC
LIMIT 5
```

### Comparar 3 modelos en misma red y nodo origen
```sql
SELECT modelo, alcance_total, paso_50_porciento, paso_final
FROM simulaciones
WHERE nodo_origen_id = ? AND estado_simulacion = 'COMPLETADA'
ORDER BY modelo
```

