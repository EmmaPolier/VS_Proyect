# 📅 Cronograma de Desarrollo ViralSim - 5 Semanas
## Equipo de 3 Personas: Backend | Frontend | Database/DevOps

---

## 👥 Roles del Equipo

| Persona | Rol | Responsabilidades Principales |
|---------|-----|------|
| **Persona 1** | 🔧 Backend Lead | Motor de simulación, lógica de propagación, APIs REST |
| **Persona 2** | 🎨 Frontend Lead | Interfaz web, visualización, UX/UI |
| **Persona 3** | 🗄️ Database/DevOps | BD, DAOs, configuración, integración |

---

# 📊 SEMANA 1: Preparación y Arquitectura Base
**Objetivo**: Entorno listo, estructura creada, primeros modelos en código

## ✅ PERSONA 1 - Backend
- [ ] **Lunes**: Setup Java project
  - Crear estructura Maven en `backend/`
  - Configurar `pom.xml` con dependencias básicas (JUnit, MySQL driver, Spring Boot si lo usará)
  - Crear paquetes: `models`, `propagation`, `engine`, `metrics`, `api`
  
- [ ] **Martes-Miércoles**: Implementar Modelos (models/)
  - `Estado.java` (ENUM: NO_INFORMADO, INFORMADO_ACTIVO, INFORMADO_PASIVO, RESISTENTE)
  - `Nodo.java` (id, estado, pasoInfeccion, probabilidad, umbral, centralidad)
  - `Arista.java` (origen, destino, peso, probabilidad, activa)
  - `Grafo.java` (List nodos, List aristas, HashMap para O(1) lookup)
  
- [ ] **Jueves-Viernes**: Crear interfaces base
  - `ModeloPropagacion.java` (interfaz)
  - `SimulacionResultado.java` (POJO para resultados)
  - Estructura básica de `MotorSimulacion.java`
  
**Entregable**: Compilación sin errores, tests unitarios básicos

---

## ✅ PERSONA 2 - Frontend
- [ ] **Lunes**: Estructura HTML/CSS base
  - Crear `frontend/index.html` (layout principal)
  - Setup `css/style.css` y `css/responsive.css`
  - Crear secciones: panel control, visualización, métricas
  
- [ ] **Martes-Miércoles**: Diseño UI
  - Implementar formularios (carga grafo, selección modelo, parámetros)
  - Crear barra de control (play/pause/reset/step)
  - Diseñar panel de métricas (tabla/gráficos básicos)
  - Estilos responsive mobile-first
  
- [ ] **Jueves-Viernes**: Setup JavaScript base
  - `js/main.js` (inicialización)
  - `js/api-client.js` (plantilla para llamadas HTTP)
  - `js/ui-controller.js` (manejo de eventos básicos)
  - Validación de formularios
  
**Entregable**: Sitio funcional sin backend (datos mock)

---

## ✅ PERSONA 3 - Database/DevOps
- [ ] **Lunes-Martes**: Setup MySQL
  - Crear base de datos `viralsim`
  - Ejecutar `database/schema.sql` (tablas: nodos, aristas, simulaciones, métricas)
  - Script de seed data (grafo de prueba con 20 nodos)
  
- [ ] **Miércoles**: Documentar esquema
  - Crear `docs/DATABASE.md` con relaciones
  - Diagramas ER (textual o visual)
  
- [ ] **Jueves-Viernes**: Configuración de conexión
  - Crear `ConexionBD.java` (pooling de conexiones)
  - Crear `aplicacion.properties` con credenciales
  - Crear DAOs stub (NodoDAO, AristaDAO)
  
**Entregable**: BD lista, conexión funcional desde Java

---

## 🤝 Coordinación Semana 1
- **Reunión Inicio Lunes** (30 min): Revisión arquitectura, distribución tareas
- **Stand-up Miércoles** (15 min): Check progress
- **Reunión Viernes** (30 min): Review semana, planificación siguiente

---

# 📊 SEMANA 2: Lógica de Propagación y APIs
**Objetivo**: Modelos funcionando, APIs básicas, BD con operaciones CRUD

## ✅ PERSONA 1 - Backend
- [ ] **Lunes-Martes**: Implementar Modelos de Propagación
  - `ModeloViral.java`: Cada nodo propaga con su probabilidad
  - `ModeloCascadaIndependiente.java`: Un intento por arista
  - `ModeloUmbralLineal.java`: Presión social (porcentaje de vecinos activos)
  - Tests unitarios para cada modelo (casos: 100% adopción, 0% adopción, random)
  
- [ ] **Miércoles**: Implementar MotorSimulacion
  - `iniciarSimulacion(nodo, modelo)`: Inicia desde un nodo semilla
  - `ejecutarPaso()`: Propaga un paso, retorna boolean
  - `obtenerResultado()`: Devuelve SimulacionResultado
  - Manejo de estados y eventos
  
- [ ] **Jueves**: Crear REST APIs básicas
  - POST `/api/simulacion/iniciar` (params: grafo_id, nodo_semilla, modelo)
  - GET `/api/simulacion/{id}/paso` (ejecuta un paso)
  - GET `/api/simulacion/{id}` (obtiene estado actual)
  - POST `/api/simulacion/{id}/reset`
  
- [ ] **Viernes**: Validación y tests
  - Tests de integración (BD + Lógica)
  - Documentar APIs en `docs/BACKEND.md`
  
**Entregable**: APIs funcionales, 80% cobertura de tests

---

## ✅ PERSONA 2 - Frontend
- [ ] **Lunes-Martes**: Visualización de Grafos
  - Integrar librería de grafos (Chart.js o D3.js simple)
  - `js/visualization.js`: Renderizar nodos y aristas
  - Colores por estado (NO_INFORMADO=gris, INFORMADO_ACTIVO=rojo, etc)
  - Zoom/pan básico
  
- [ ] **Miércoles**: Integración con API
  - `js/simulator.js`: Clase para controlar simulación
  - Conectar botones a endpoints del backend
  - Mostrar paso actual, iteración
  
- [ ] **Jueves**: Panel de Métricas
  - Mostrar: nodos informados, % adopción, pasos ejecutados
  - Actualizar en tiempo real conforme avanza simulación
  - Tabla de historial de pasos
  
- [ ] **Viernes**: Testing y UX
  - Pruebas manuales contra backend
  - Mejorar retroalimentación visual (loading, errores)
  - Responsive en mobile
  
**Entregable**: Frontend conectado, simulación funcional

---

## ✅ PERSONA 3 - Database/DevOps
- [ ] **Lunes-Martes**: Crear DAOs Completos
  - `NodoDAO.java`: CRUD nodos
  - `AristaDAO.java`: CRUD aristas
  - `GrafoDAO.java`: Cargar grafo completo
  - Métodos para obtener vecinos, grado, etc
  
- [ ] **Miércoles-Jueves**: Crear DAOs de Simulación
  - `SimulacionDAO.java`: Guardar/cargar simulaciones
  - `PasoDAO.java`: Guardar estado en cada paso (opcional, para historial)
  - Transacciones para integridad
  
- [ ] **Viernes**: Datos de Prueba
  - Seed script con grafo de 50 nodos (preparado)
  - Script para generar grafos Watts-Strogatz (si es posible)
  - Documentación de funciones BD
  
**Entregable**: DAOs 100% funcionales, seed data lista

---

## 🤝 Coordinación Semana 2
- **Stand-up Lunes** (15 min): Confirmación modelos
- **Sincronización Miércoles** (30 min): Backend-Frontend-BD
- **Testing Integration Viernes** (1 hora): Todos juntos

---

# 📊 SEMANA 3: Optimización, Métricas y Features Avanzadas
**Objetivo**: Sistema completo, métricas calculadas, interfaz pulida

## ✅ PERSONA 1 - Backend
- [ ] **Lunes-Martes**: Implementar Métricas
  - `CalculadorMetricas.java`: 
    - Grado de nodos
    - Betweenness centrality (importante para propagación)
    - Coeficiente de clustering
    - Componentes conectadas
  - APIs: GET `/api/grafo/{id}/metricas`
  
- [ ] **Miércoles**: Generación de Grafos
  - `GeneradorWattsStrogatz.java`: Crear redes realistas
  - POST `/api/grafo/generar` (params: nodos, vecinos, probabilidad rewiring)
  - Validar topología generada
  
- [ ] **Jueves**: APIs Avanzadas
  - GET `/api/grafo/{id}` (devuelve nodos + aristas completo)
  - POST `/api/grafo/cargar` (carga JSON)
  - GET `/api/simulacion/{id}/historial` (todos los pasos)
  - Paginación si es necesario
  
- [ ] **Viernes**: Performance y Docs
  - Optimizar queries a BD
  - Caché en memoria si es necesario
  - Documentación final APIs (Swagger o manual)
  
**Entregable**: Sistema de métricas funcionando, 50+ nodos sin lag

---

## ✅ PERSONA 2 - Frontend
- [ ] **Lunes-Martes**: Dashboard Avanzado
  - Gráficos de progresión (línea: nodos infectados vs pasos)
  - Tablas interactivas (filtrar nodos por estado)
  - Estadísticas en tiempo real
  
- [ ] **Miércoles**: Generador de Grafos UI
  - Formulario para generar Watts-Strogatz
  - Preview de grafo antes de simular
  - Exportar/importar grafo en JSON
  
- [ ] **Jueves**: Comparación de Modelos
  - Opción de ejecutar 3 modelos simultáneamente
  - Gráfico comparativo
  - Métricas lado a lado
  
- [ ] **Viernes**: Polish y Accesibilidad
  - Colores accesibles (para daltónicos)
  - Help tooltips
  - Documentación de usuario (en-app)
  
**Entregable**: Dashboard profesional, UX mejorada

---

## ✅ PERSONA 3 - Database/DevOps
- [ ] **Lunes-Martes**: Optimización BD
  - Índices en tablas principales (id, estado, pasos)
  - Vistas materializadas si es necesario
  - Análisis de queries lentas
  
- [ ] **Miércoles**: Historiales y Auditoría
  - Tabla para historial de pasos (opcional pero recomendado)
  - `HistorialDAO.java`
  - Limpiar datos antiguos (script)
  
- [ ] **Jueves-Viernes**: Backup y Dockerización
  - Scripts de backup/restore
  - Crear `Dockerfile` para MySQL (volúmenes persistentes)
  - Docker Compose con backend+MySQL (opcional)
  - Documentar setup en `INSTALL.md`
  
**Entregable**: BD optimizada, sistema backup funcional

---

## 🤝 Coordinación Semana 3
- **Daily Standup** (15 min cada día): Progreso
- **Integration Testing Viernes** (1.5 horas): Todos

---

# 📊 SEMANA 4: Testing Completo y Documentación
**Objetivo**: Calidad, cobertura de tests, documentación exhaustiva

## ✅ PERSONA 1 - Backend
- [ ] **Lunes-Martes**: Testing Exhaustivo
  - Tests unitarios para cada modelo (100% cobertura lógica)
  - Tests de integración (simulación completa)
  - Casos edge: grafo vacío, nodo aislado, grafo completo
  - Pruebas de carga (1000 nodos)
  
- [ ] **Miércoles-Jueves**: Bug Fixes y Refactoring
  - Revisar códigos de Persona 2 y 3 (code review)
  - Refactorizar si es necesario
  - Mejorar manejo de errores
  - Logging estructurado
  
- [ ] **Viernes**: Documentación Técnica
  - Completar `docs/BACKEND.md`
  - Diagrama de clases (ASCII o visual)
  - Guía de deployment
  
**Entregable**: 80%+ test coverage, 0 bugs críticos

---

## ✅ PERSONA 2 - Frontend
- [ ] **Lunes-Martes**: Testing Funcional
  - Tests manuales exhaustivos (compatibilidad navegadores)
  - Chrome, Firefox, Edge, Safari (si es posible)
  - Mobile: iPhone, Android
  - Resoluciones: 320px, 768px, 1920px
  
- [ ] **Miércoles-Jueves**: Bug Fixes y Performance
  - Optimizar carga de página (lazy loading imágenes)
  - Reducir bundle JS/CSS
  - Mejorar tiempo de respuesta API (caché local)
  
- [ ] **Viernes**: Documentación UX
  - `docs/FRONTEND.md` (componentes, features)
  - Tutorial de usuario (screenshots anotados)
  - Troubleshooting común
  
**Entregable**: Aplicación sin bugs, documentada

---

## ✅ PERSONA 3 - Database/DevOps
- [ ] **Lunes-Martes**: Testing de Integridad
  - Verificar ACID en transacciones
  - Tests de concurrencia (múltiples simulaciones simultáneas)
  - Validar constraints
  
- [ ] **Miércoles-Jueves**: Documentación y Automatización
  - `docs/DATABASE.md` actualizado
  - Scripts SQL documentados
  - Automatizar deploys (si es aplicable)
  
- [ ] **Viernes**: Backup y Recovery
  - Probar restore de backups
  - Documentar procedimiento de disaster recovery
  
**Entregable**: BD robusta, documentación completa

---

## 🤝 Coordinación Semana 4
- **Code Review Meetings** (1 hora cada uno): P1→P2, P1→P3, P2→P1
- **Testing Integration Full** (2 horas Jueves): Todos

---

# 📊 SEMANA 5: Polish, Presentación y Entrega
**Objetivo**: Sistema listo para demostración y entrega final

## ✅ PERSONA 1 - Backend
- [ ] **Lunes-Martes**: Performance Final
  - Profiling con JProfiler o similar
  - Optimizar hotspots
  - Caché de resultados si es necesario
  
- [ ] **Miércoles**: API Final Checklist
  - Todos los endpoints funcionando
  - Respuestas consistentes (formato JSON)
  - Códigos HTTP correctos (200, 400, 500, etc)
  
- [ ] **Jueves-Viernes**: Demo y Soporte
  - Preparar casos de demo
  - Estar disponible para bugs últimos minutos
  - Documentación final
  
**Entregable**: Backend production-ready

---

## ✅ PERSONA 2 - Frontend
- [ ] **Lunes-Martes**: UX Final Polish
  - Mejorar animaciones y transiciones
  - Mensaje de error/success amigables
  - Loading states claros
  
- [ ] **Miércoles**: Casos de Demo
  - Preparar 3-4 escenarios de uso:
    - Red pequeña (10 nodos)
    - Red mediana (100 nodos)
    - Red grande (250 nodos)
  - Precargar datos para demo rápida
  
- [ ] **Jueves-Viernes**: Demo Finales
  - Presentación visual funcional
  - Captura de pantallas para documentación
  - Estar listo para preguntas
  
**Entregable**: Interfaz pulida, lista para demostración

---

## ✅ PERSONA 3 - Database/DevOps
- [ ] **Lunes-Martes**: Datos de Demostración
  - Crear 3 grafos pre-cargados (pequeño, mediano, grande)
  - Asegurar seed data es realista
  - Testing final con volumen de datos
  
- [ ] **Miércoles**: Infraestructura Final
  - Verificar Docker Compose funciona (si lo usa)
  - Documentar setup único comando
  - Crear script de "one-click deploy"
  
- [ ] **Jueves-Viernes**: Soporte
  - Estar disponible para problemas BD
  - Backup de datos finales
  
**Entregable**: Infraestructura lista, datos de demo listos

---

## 🤝 Coordinación Semana 5
- **Daily Standup** (15 min): Estado crítico
- **Demo General Miércoles** (1 hora): Todos
- **Ensayo Demo Viernes** (1 hora): Presentación final

---

# 📋 MILESTONES CLAVE

| Semana | Hito | Criterio de Aceptación |
|--------|------|------|
| 1 | Ambiente Listo | Código compila, BD funciona, frontend corre |
| 2 | MVP Funcional | Simulación básica end-to-end funciona |
| 3 | Feature Complete | Todos los modelos, métricas, UI avanzada |
| 4 | Calidad | 80%+ tests, sin bugs críticos, documentado |
| 5 | Entrega | Demo funcional, código limpio, listo para entregar |

---

# 🛠️ Herramientas Recomendadas por Persona

## Persona 1 (Backend)
- IDE: IntelliJ IDEA o Eclipse
- Build: Maven
- Testing: JUnit5, Mockito
- Versionado: Git (commits diarios)
- CI/CD: GitHub Actions (opcional Semana 5)

## Persona 2 (Frontend)
- IDE: VS Code
- Testing: Selenium o Playwright (opcional)
- Dev Tools: Chrome DevTools
- CSS: CSS vanilla o Bootstrap
- Gráficos: Chart.js o D3.js

## Persona 3 (Database)
- IDE: IntelliJ o VS Code
- BD: MySQL Workbench
- Backup: mysqldump
- Docker: Docker Desktop
- Versionado: Git

---

# 📞 Reuniones Recomendadas

### Diarias (todas las semanas)
- **15 min standup** (10:00 AM): Qué hice ayer, qué hago hoy, blockers
- Formato: Chat o video rápido

### Semanales (todas las semanas)
- **Viernes 1 hora**: Review semana, planificación siguiente
- Personas 1, 2, 3 + Product Owner (si existe)

### Especiales
- **Sincronización Integración**: Miércoles cada semana (15-30 min)
- **Code Review**: Entre personas (peer review)

---

# 📊 Definición de "Listo"

Cada tarea se considera completa cuando:

1. ✅ **Código**: Compila/funciona sin errores
2. ✅ **Tests**: 80%+ cobertura (backend), funcional (frontend)
3. ✅ **Documentación**: Código comentado, README actualizado
4. ✅ **Review**: Peer review aprobado
5. ✅ **Git**: Commit con mensaje descriptivo
6. ✅ **Integración**: Funciona con código del equipo

---

# 🎯 Métricas de Éxito Semana a Semana

| Semana | Métrica | Meta |
|--------|---------|------|
| 1 | Compilación exitosa | 100% |
| 2 | APIs funcionales | 90%+ |
| 3 | Bugs críticos | 0 |
| 4 | Test coverage | 80%+ |
| 5 | Demo sin crasheos | 100% |

---

## 📌 Notas Importantes

1. **Flexibilidad**: Si algo toma más tiempo, mover al siguiente sprint
2. **Daily Sync**: Coordinar si hay dependencias críticas
3. **Backup Regular**: Hacer commits diarios
4. **Escalabilidad**: Diseñar pensando en 1000+ nodos desde inicio
5. **Mobile First**: Frontend debe verse bien en móvil desde inicio