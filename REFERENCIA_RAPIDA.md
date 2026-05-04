# 🎯 Referencia Rápida - ViralSim

## 📍 Ubicación de Documentos

```
📄 PROYECTO_RESUMEN.md          ← Empieza aquí (índice visual)
📄 STRUCTURE.md                 ← Árbol de directorios completo
� ESTADO_DEL_PROYECTO.md       ← Estado del proyecto
📄 REFERENCIA_RAPIDA.md         ← Este archivo

📂 VS_Proyect/                  ← 🎯 Código del proyecto
  ├─ 📂 backend/
  ├─ 📂 frontend/
  ├─ 📂 database/
  └─ 📂 docs/
     ├─ BACKEND.md              ← Especificación de clases Java
     ├─ DATABASE.md             ← Esquema SQL y relaciones
     ├─ FRONTEND.md             ← Componentes HTML/CSS/JS
     └─ ARCHITECTURE.md         ← Diagramas de arquitectura
```

---

## 🏗️ Estructura Creada

✅ **Directorios backend**:
```
VS_Proyect/backend/src/main/java/com/viralsim/
├── models/          (Nodo, Arista, Grafo, Estado)
├── propagation/     (ModeloViral, Cascada, Threshold)
├── engine/          (MotorSimulacion, EventoSimulacion)
├── metrics/         (CalculadorMetricas, MetricaPaso)
├── database/        (DAOs)
└── utils/           (Generadores)
```

✅ **Directorios frontend**:
```
VS_Proyect/frontend/
├── index.html
├── css/             (style.css, responsive.css)
└── js/              (main.js, api-client.js, simulator.js, etc.)
```

✅ **Directorios base de datos**:
```
VS_Proyect/database/
├── schema.sql       (tablas: nodos, aristas, simulaciones, etc.)
└── seed-data.sql
```

✅ **Documentación**:
```
VS_Proyect/docs/
├── BACKEND.md
├── DATABASE.md
├── FRONTEND.md
└── ARCHITECTURE.md
```

---

## 🔑 Conceptos Clave

### Estados de un Nodo
```
NO_INFORMADO → INFORMADO_ACTIVO → INFORMADO_PASIVO → RESISTENTE
    (inicio)      (contagio)         (después 3-5      (final)
                                      pasos activo)
```

### Los 3 Modelos de Propagación

| Modelo | Lógica | Responde |
|--------|--------|----------|
| **Viral** | Cada nodo contagia a vecinos con su probabilidad | ¿Qué tan lejos llega en el peor caso? |
| **Cascada** | Probabilidad en cada arista, un intento | ¿Importa quién publica? |
| **Threshold** | Contagio por presión social (%) | ¿Necesita masa crítica? |

### Componentes Principales

1. **MotorSimulacion** - Orquesta la ejecución paso a paso
2. **Modelos** - 3 estrategias de propagación
3. **CalculadorMetricas** - Calcula en O(1) con HashMap
4. **DAOs** - Persistencia en MySQL
5. **Frontend** - Visualización con vis.js

---

## 📊 Base de Datos - Tablas

| Tabla | Registros | Clave | Propósito |
|-------|-----------|-------|-----------|
| `nodos` | 250 | id | Usuarios |
| `aristas` | ~750 | id | Conexiones |
| `simulaciones` | N | id | Metadata de ejecuciones |
| `historial_infeccion` | Miles | id | Paso a paso (animación) ← **IMPORTANTE** |
| `metricas` | 3×N | id | Snapshots por paso |

---

## 🎨 Frontend - Componentes

```
┌─ Panel Control ─────────────────┐
│ Modelo:     [dropdown]          │
│ Nodo Orig:  [dropdown]          │
│ Velocidad:  [1x▓ 2x 4x 8x]      │
│                                  │
│ [Iniciar] [Pausar] [Continuar]  │
│                                  │
│ Paso: 5 / 12                    │
└──────────────────────────────────┘

┌─ Visualización ─────────────────────────────┐
│                                              │
│        [250 nodos con vis.js]               │
│        🔵 → 🟠 (parpadea) → 🟡 → ⚫        │
│                                              │
│    (Zoom, Pan, Click para detalles)        │
└──────────────────────────────────────────────┘

┌─ Analíticas ────────────────┐
│ Alcance: [████████░░] 86%   │
│ Paso Actual: 5              │
│ Velocidad: 50% en paso 4    │
│                              │
│ [Gráfica de alcance]        │
│                              │
│ Top 5 nodos influyentes:    │
│ 1. Nodo #42 (0.193)         │
│ 2. Nodo #15 (0.178)         │
│ ...                          │
└─────────────────────────────┘
```

---

## 📡 API REST (Backend → Frontend)

### Iniciar Simulación
```
POST /api/simulacion/iniciar
Body: { modelo: "Viral", nodoOrigenId: 42, velocidad: 2 }
Response: Stream JSON por cada paso
```

### Estructura del Paso JSON
```json
{
  "paso": 1,
  "nodosActivados": [
    { "id": 15, "estado": "INFORMADO_ACTIVO", "padre": 42 },
    { "id": 28, "estado": "INFORMADO_ACTIVO", "padre": 42 },
    { "id": 63, "estado": "INFORMADO_ACTIVO", "padre": 42 }
  ],
  "metricas": {
    "alcance": 1.6,
    "nodosInformados": 4,
    "nodosActivos": 3,
    "nodosPasivos": 0,
    "nodosResistentes": 0
  }
}
```

### Simulación Completada
```json
{
  "evento": "SIMULACION_COMPLETADA",
  "resultadoFinal": {
    "alcanceTotal": 86.4,
    "paso50Porciento": 4,
    "pasoFinal": 12,
    "estadosFinales": {
      "NO_INFORMADO": 34,
      "INFORMADO_ACTIVO": 0,
      "INFORMADO_PASIVO": 216,
      "RESISTENTE": 0
    }
  }
}
```

---

## 🚀 Orden de Implementación Recomendado

### Fase 1: Estructuras Base (Backend)
1. ✅ Crear paquetes (hecho)
2. Crear enumeración `Estado`
3. Crear clase `Nodo`
4. Crear clase `Arista`
5. Crear clase `Grafo`

### Fase 2: Lógica de Negocio (Backend)
6. Crear interfaz `ModeloPropagacion`
7. Implementar `ModeloViral`
8. Implementar `ModeloCascadaIndependiente`
9. Implementar `ModeloUmbralLineal`
10. Crear `MotorSimulacion`
11. Crear `CalculadorMetricas`

### Fase 3: Persistencia (Backend)
12. Crear `ConexionBD`
13. Crear `NodoDAO`
14. Crear `AristaDAO`
15. Crear `SimulacionDAO`
16. Crear `HistorialDAO`
17. Crear `MetricaDAO`
18. Crear `schema.sql`

### Fase 4: Generadores (Backend)
19. Crear `GeneradorAleatorios`
20. Crear `GeneradorWattsStrogatz`
21. Crear `GeneradorBetweenness` (con JGraphT)
22. Crear `Exportador` (JSON)

### Fase 5: Controlador (Backend)
23. Crear controlador REST

### Fase 6: Frontend
24. Crear `index.html`
25. Crear `style.css` + `responsive.css`
26. Crear `main.js` + módulos JS
27. Integrar vis.js
28. Integrar Chart.js

---

## 📚 Parámetros Importantes

### Red (Watts-Strogatz)
- **n** = 250 nodos
- **k** = 6 vecinos iniciales
- **p** = 0.1 probabilidad de reconexión

### Nodos
- **probabilidad_propagacion**: 0.4 - 0.9 (aleatorio)
- **umbral**: 0.1 - 0.9 (aleatorio, para Threshold)

### Aristas
- **probabilidad_arista**: 0.3 - 0.95 (aleatorio)

### Modelo Viral
- Cada nodo intenta contagiar a TODO sus vecinos
- Formula: `if (random() <= nodo.probabilidadPropagacion)`

### Modelo Cascada
- Cada arista solo se intenta UNA vez
- Formula: `if (random() <= arista.probabilidadArista)`
- Luego: `arista.activa = false`

### Modelo Threshold
- Se revisa TODOS los nodos no informados
- Formula: `if (vecinosInformados / totalVecinos >= nodo.umbral)`

---

## 🔍 Checklist Antes de Empezar a Codificar

- [ ] Revisar [PROYECTO_RESUMEN.md](./PROYECTO_RESUMEN.md)
- [ ] Revisar [VS_Proyect/docs/BACKEND.md](./VS_Proyect/docs/BACKEND.md)
- [ ] Revisar [VS_Proyect/docs/DATABASE.md](./VS_Proyect/docs/DATABASE.md)
- [ ] Revisar [VS_Proyect/docs/FRONTEND.md](./VS_Proyect/docs/FRONTEND.md)
- [ ] Revisar [VS_Proyect/docs/ARCHITECTURE.md](./VS_Proyect/docs/ARCHITECTURE.md)
- [ ] Entender los 3 modelos de propagación
- [ ] Entender los 4 estados de los nodos
- [ ] Entender el flujo de datos Cliente → Servidor
- [ ] Decidir herramientas (Maven, IDE, etc.)
- [ ] Crear BD MySQL "viralsim"

---

## 💡 Tips de Implementación

1. **HashMap es tu amigo**: Úsalo para mapear `id → Nodo` en O(1)
2. **Interfaz ModeloPropagacion**: Facilita agregar nuevos modelos luego
3. **DAO Pattern**: Separa lógica de negocio de acceso a datos
4. **JSON en Frontend**: No envíes el grafo completo cada paso, solo cambios
5. **Testing**: Prueba cada modelo con red pequeña primero
6. **Métricas**: Calcula en tiempo real, guarda para análisis

---

## 📞 Contacto / Referencias

- Especificaciones: `ViralSim_Entregable1.docx`
- Documentación técnica: Carpeta `VS_Proyect/docs/`
- Código existente (legado): Carpeta `code/`

---

## Resumen Final

✅ **Estructura de directorios**: Creada
✅ **Documentación de arquitectura**: Completa
✅ **Diseño de base de datos**: Definido
✅ **Especificación de clases**: Documentada
✅ **Componentes frontend**: Planeados

**Próximo paso**: Empezar a implementar código desde la Fase 1 ⬆️

