# ViralSim

Plataforma de simulación de propagación en redes que modela cómo la información, influencia o contenido viral se propaga a través de redes sociales.

## Descripción General

ViralSim es una aplicación web que simula la propagación de información a través de redes utilizando tres modelos de propagación diferentes:

- **Modelo Viral**: Cada nodo se propaga con su propia probabilidad
- **Cascada Independiente**: Propagación basada en aristas con un único intento de activación
- **Modelo de Umbral**: Adopción basada en presión social (basada en porcentaje)

## Características

- **Motor de Simulación**: Ejecución paso a paso de modelos de propagación en grafos de red
- **Generación de Redes**: Generación de topología Watts-Strogatz para estructuras de red realistas
- **Métricas en Tiempo Real**: Calcula centralidad de red, tasas de adopción y estadísticas de propagación
- **Visualización Interactiva**: Interfaz web para visualización y control de grafos
- **Almacenamiento Persistente**: Base de datos MySQL para guardar simulaciones y datos históricos

## Estructura del Proyecto

```
backend/          # Motor de simulación Java
├── models/       # Definiciones de Grafo, Nodo, Arista, Estado
├── propagation/  # Implementaciones de modelos de propagación
├── engine/       # Orquestación de simulación
├── metrics/      # Cálculo de métricas
├── database/     # Capa de acceso a datos (DAOs)
└── utils/        # Utilidades y generadores

frontend/         # Interfaz web (HTML/JS/CSS)
database/         # Esquema SQL y datos iniciales
```

## Stack Tecnológico

- **Backend**: Java con Maven
- **Frontend**: HTML, CSS, JavaScript
- **Base de Datos**: MySQL
- **Arquitectura**: Multicapa (Modelos → Propagación → Motor → Métricas → Base de Datos)

## Primeros Pasos

1. Configura la conexión a la base de datos en el backend
2. Ejecuta schema.sql para crear las tablas
3. Construye el backend: `mvn clean install`
4. Despliega el frontend en el servidor web
5. Ejecuta simulaciones a través de la interfaz web

## Documentación

- `docs/ARCHITECTURE.md` - Descripción general del sistema
- `docs/BACKEND.md` - Clases Java y APIs
- `docs/DATABASE.md` - Esquema y relaciones
- `docs/FRONTEND.md` - Componentes y características de UI
