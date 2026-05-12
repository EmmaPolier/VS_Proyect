# ViralSim — División de trabajo (3 personas / 5 días)

---

## Persona 1 — Backend Motor
**Responsabilidad**: Generación de red + motor de simulación

| Día | Tarea |
|-----|-------|
| 1 | `WattsStrogatzGenerator.java` en `utils/` — genera 250 nodos + aristas con `n=250, k=6, p=0.1`, asigna probabilidades, persiste en BD. Endpoint `POST /api/grafos/generar` |
| 2 | Interfaz `EstrategiaPropagacion.java` en `engine/`. Implementar `ModeloViral.java` |
| 3 | Implementar `ModeloCascada.java` y `ModeloThreshold.java` |
| 4 | `MotorSimulacion.java` — orquesta los 3 modelos paso a paso, guarda `PasoSimulacion` + `NodoSimulacion`. Endpoint `POST /api/simulaciones/{id}/ejecutar` |
| 5 | Pruebas del motor, corrección de bugs, `POST /api/simulaciones/comparar?grafoId=X&nodoSemillaId=Y` |

---

## Persona 2 — Backend Métricas + Integración
**Responsabilidad**: Métricas, endpoints del frontend, CORS

| Día | Tarea |
|-----|-------|
| 1 | Agregar dependencia JGraphT al `pom.xml`. `CalculadorMetricas.java` en `metrics/` — centralidad de grado para los 250 nodos, actualizar `Nodo.centralidadGrado` en BD al generar red |
| 2 | Betweenness centrality con JGraphT — actualizar `Nodo.betweenness` en BD. `GET /api/simulaciones/{id}/metricas` |
| 3 | Endpoints adicionales para frontend: `GET /api/aristas/grafo/{id}`, `PUT /api/nodos/{id}`, `GET /api/nodos/grafo/{id}/top-grado`, `GET /api/nodos/grafo/{id}/top-betweenness` |
| 4 | Endpoints: `GET /api/simulaciones/{id}/pasos`, `GET /api/simulaciones/{id}/nodo-simulacion?paso=N`. Configurar CORS en `ViralSimApplication.java` |
| 5 | Integración completa: probar flujo generarRed → ejecutarSimulacion → métricas desde Postman/curl |

---

## Persona 3 — Frontend
**Responsabilidad**: Las 3 vistas HTML/CSS/JS

| Día | Tarea |
|-----|-------|
| 1 | `index.html` + `style.css` (tema oscuro, variables CSS, navbar con 3 tabs). `api.js` con todas las funciones fetch. `grafo-renderer.js` con config base de vis.js para 250 nodos |
| 2 | Vista **"Editar nodos"** (`vista-nodos.js`): tabla paginada 8 nodos/página, badges de estado, buscador, filtros, exportar JSON, paginador |
| 3 | Panel editor de la vista nodos: sliders (propagación, resistencia, umbral), dropdown estado/perfil, texto dinámico, botones Guardar/Descartar → `PUT /api/nodos/{id}` |
| 4 | Vista **"Lanzar chisme"** (`vista-chisme.js`): canvas vis.js con nodo seleccionable, panel derecho con datos del nodo, selector de modelo, textarea chisme, slider alcance, botón Lanzar |
| 5 | Vista **"Propagación"** (`vista-propagacion.js`): animación paso a paso con colores por estado, métricas en tiempo real, barras de estado, gráfica Chart.js, controles Pausar/Reiniciar/Paso a paso |

---

## Dependencias entre personas

```
Persona 1 día 1 ──► Persona 3 puede empezar con datos mock mientras tanto
Persona 1 día 4 ──► Persona 3 día 4 necesita /ejecutar funcionando
Persona 2 día 3 ──► Persona 3 día 2 necesita los endpoints GET adicionales
Persona 2 día 4 ──► Persona 3 día 5 necesita /pasos y /nodo-simulacion
```

> Ver `PLAN.md` para pseudocódigo, algoritmos y especificaciones técnicas detalladas de cada tarea.
