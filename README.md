database/         # Esquema SQL y datos iniciales
# ViralSim

Propósito
---------

ViralSim es una aplicación para investigar y experimentar con la propagación de información, influencia o comportamientos en redes. Permite ejecutar simulaciones reproducibles sobre grafos, comparar modelos de propagación y calcular métricas relevantes para análisis académicos o de prototipado.

Estructura del proyecto
-----------------------

Raíz del repositorio:

```
docker-compose.yml
backend/                # Aplicación Java (Spring Boot)
database/               # Scripts SQL de inicialización
README.md
```

Estructura principal del backend (`backend/src/main/java/com/viralsim`):

```
api/                    # Controladores de errores y excepciones globales
controllers/            # Endpoints REST (ver lista abajo)
dto/                    # Objetos de transferencia (responses/requests)
engine/                 # Motor de simulación y orquestación de modelos
metrics/                # Cálculo de métricas sobre simulaciones
models/                 # Entidades: Grafo, Nodo, Arista, Simulación, Paso, etc.
repositories/           # Repositorios JPA para persistencia
services/               # Lógica de negocio y servicios
utils/                  # Generadores y utilidades (por ejemplo Watts-Strogatz)
resources/              # `application.properties`, `data.sql` de ejemplo
```

Controladores relevantes (ubicados en `backend/src/main/java/com/viralsim/controllers`):

- `SimulacionController` — endpoints para crear/ejecutar/comparar simulaciones
- `NodoController` — CRUD y operaciones sobre nodos
- `GrafoController` — CRUD y operaciones sobre grafos
- `AristaController` — CRUD de aristas
- `PasoSimulacionController` — consultar pasos de una simulación
- `NodoSimulacionController` — estado de nodos dentro de una simulación
- `ConfiguracionSimulacionController` — gestión de configuraciones

Dónde están los modelos de propagación
-------------------------------------

Las implementaciones de los modelos (Viral, Cascada Independiente, Threshold) se encuentran en el paquete `engine` y son ejecutadas por `MotorSimulacion`. No existe un controlador REST específico para `ModeloPropagacion`; en su lugar los modelos se seleccionan por `modeloId` desde `SimulacionController`.

Base de datos
------------

Los scripts de inicialización están en `database/init/schema.sql`. Revisar `backend/src/main/resources/application.properties` para la configuración de conexión.

Cómo ejecutar (rápido)
---------------------

1. Ajusta la conexión a la base de datos en `backend/src/main/resources/application.properties`.
2. Crea la base de datos y ejecuta `database/init/schema.sql`.
3. Desde `backend/` construye la aplicación: `mvn clean package`.
4. Ejecuta la app con: `java -jar target/backend-0.0.1-SNAPSHOT.jar` (o `mvn spring-boot:run`).

Dónde mirar primero
-------------------

- `backend/src/main/java/com/viralsim/engine/MotorSimulacion.java` — orquesta la ejecución de modelos.
- `backend/src/main/java/com/viralsim/controllers/SimulacionController.java` — endpoints para lanzar y comparar simulaciones.
- `database/init/schema.sql` — esquema inicial de la base de datos.

Contacto
--------

Para dudas sobre diseño o ejecución deja una issue o contacta al equipo en `EQUIPO.md`.

