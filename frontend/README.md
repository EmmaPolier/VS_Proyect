# Frontend ViralSim

Frontend temporal pero funcional para el simulador de propagación de rumores en redes sociales.

## 🎯 Características

### 3 Vistas Principales

#### 1. **Editar Nodos** (`vista-nodos.js`)
- Tabla paginada de todos los nodos (8 por página)
- Búsqueda por nombre/ID
- Filtro por estado
- Editor modal para ajustar:
  - Nombre
  - Estado (NO_INFORMADO, INFORMADO_ACTIVO, INFORMADO_PASIVO, RESISTENTE)
  - Probabilidad de propagación (slider 0-1)
  - Umbral (slider 0-1)

#### 2. **Lanzar Chisme** (`vista-chisme.js`)
- Visualización interactiva del grafo con vis.js
- Selección de nodo semilla (clic en el nodo)
- Configuración de simulación:
  - Seleccionar modelo de propagación (Viral, Cascada Independiente, Umbral)
  - Escribir mensaje/chisme
  - Ajustar alcance máximo de pasos
- Lanzar simulación con un clic

#### 3. **Propagación** (`vista-propagacion.js`)
- Animación en tiempo real de la propagación
- Métricas en vivo:
  - Paso actual
  - Barras de estado para cada tipo de nodo
  - Gráfica de propagación con Chart.js
- Controles:
  - ⏸ Pausar/Reanudar
  - ⏭ Siguiente paso (manual)
  - 🔄 Reiniciar simulación

### Pestaña de Inicio
- Vista general de estadísticas
- Botones para generar o cargar red
- Guía rápida de pasos

## 📋 Requisitos

1. **Backend ejecutándose** en `http://localhost:8080`
2. **Librerías CDN**:
   - vis.js (visualización de grafos)
   - Chart.js (gráficas)

## 🚀 Cómo Usar

### 1. Generar una Red
```
Inicio → "Generar Red Watts-Strogatz" → Esperar
```

### 2. Editar Nodos (Opcional)
```
Editar Nodos → Buscar nodo → Clic en "Editar" → Cambiar parámetros → Guardar
```

### 3. Lanzar Simulación
```
Lanzar Chisme → Clic en nodo del grafo → Seleccionar modelo → Escribir mensaje → "Lanzar Simulación"
```

### 4. Ver Propagación
```
Propagación → Observar animación automática (o usar controles manuales)
```

## 🔧 Estructura

```
frontend/
├── index.html          # Página principal con estructura HTML
├── css/
│   └── style.css       # Estilos tema oscuro (variables CSS)
├── js/
│   ├── api.js          # Funciones para llamar al backend
│   ├── app.js          # Lógica principal y navegación
│   ├── vista-nodos.js  # Gestión de nodos
│   ├── vista-chisme.js # Lanzar simulaciones
│   └── vista-propagacion.js # Ver animación y métricas
└── README.md           # Este archivo
```

## 🎨 Diseño

- **Tema oscuro** con variables CSS
- **Responsivo** para escritorio y tablet
- **Colores**:
  - 🔴 INFORMADO_ACTIVO: #ff6b6b
  - 🟡 INFORMADO_PASIVO: #ffd93d
  - 🟢 RESISTENTE: #6bcf7f
  - ⚪ NO_INFORMADO: #666666

## 📡 Endpoints API Necesarios

El frontend espera estos endpoints en el backend:

### Grafos
- `POST /api/grafos/crear` - Crear grafo
- `GET /api/grafos` - Listar grafos
- `GET /api/grafos/{id}` - Obtener grafo

### Nodos
- `GET /api/nodos/grafo/{grafoId}` - Listar nodos
- `GET /api/nodos/{id}` - Obtener nodo
- `PUT /api/nodos/{id}` - Actualizar nodo
- `GET /api/nodos/grafo/{grafoId}/top-grado` - Top por grado
- `GET /api/nodos/grafo/{grafoId}/top-betweenness` - Top por betweenness

### Aristas
- `GET /api/aristas/grafo/{grafoId}` - Listar aristas

### Simulaciones
- `POST /api/simulaciones` - Crear simulación
- `POST /api/simulaciones/{id}/ejecutar` - Ejecutar simulación
- `GET /api/simulaciones` - Listar simulaciones
- `GET /api/simulaciones/{id}` - Obtener simulación
- `GET /api/simulaciones/{id}/metricas` - Obtener métricas
- `GET /api/simulaciones/{id}/pasos` - Obtener pasos
- `GET /api/simulaciones/{id}/nodo-simulacion?paso=N` - Obtener estado de nodos en paso

### Configuraciones
- `POST /api/configuraciones` - Crear configuración

## 🛠️ Cómo Servir el Frontend

### Opción 1: Live Server (VS Code Extension)
```
Click derecho en index.html → "Open with Live Server"
```

### Opción 2: Python
```bash
cd frontend
python -m http.server 3000
# Abrir http://localhost:3000
```

### Opción 3: Node.js (http-server)
```bash
npm install -g http-server
cd frontend
http-server -p 3000
```

### Opción 4: Docker
```bash
docker run -p 3000:80 -v $(pwd)/frontend:/usr/share/nginx/html nginx
```

## ⚠️ CORS

El backend debe permitir CORS para `http://localhost:3000` (o donde se sirva el frontend).

En `ViralSimApplication.java`:
```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173")
                .allowedMethods("*")
                .allowedHeaders("*");
        }
    };
}
```

## 📊 Estados de Nodo

| ID | Nombre | Color | Emoji |
|----|--------|-------|-------|
| 0  | NO_INFORMADO | #666 | ⚪ |
| 1  | INFORMADO_ACTIVO | #ff6b6b | 🔴 |
| 2  | INFORMADO_PASIVO | #ffd93d | 🟡 |
| 3  | RESISTENTE | #6bcf7f | 🟢 |

## 🔄 Modelos de Propagación

| ID | Modelo | Descripción |
|----|--------|-------------|
| 1  | Viral | Cada nodo se propaga con su probabilidad |
| 2  | Cascada Independiente | Un único intento de activación por arista |
| 3  | Umbral | Adopción cuando % de vecinos activos > umbral |

## 🐛 Debugging

Abre la consola del navegador (F12) para ver logs de:
- Llamadas a la API
- Errores de carga
- Eventos del grafo

## 📝 Notas

- El frontend es **temporal** - diseñado para pruebas funcionales
- No hay persistencia de sesión (sin localStorage)
- Los datos se pierden al recargar
- No hay validación robusta de formularios
- Los estilos son básicos pero funcionales

## 🎓 Próximas Mejoras

- [ ] Guardar preferencias en localStorage
- [ ] Exportar resultados a JSON/CSV
- [ ] Más opciones de visualización
- [ ] Comparación de múltiples simulaciones
- [ ] Zoom e interacción mejorada en grafos
- [ ] Temas claros/oscuros
- [ ] Internacionalización (i18n)

---

**Versión**: 1.0 (Temporal)  
**Última actualización**: Mayo 2026
