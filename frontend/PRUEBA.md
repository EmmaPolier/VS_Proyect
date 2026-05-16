# 🚀 GUÍA RÁPIDA DE PRUEBA

Este documento te ayuda a probar rápidamente el frontend con el backend ViralSim.

## ✅ Requisitos

- Backend ViralSim ejecutándose en `http://localhost:8080`
- Navegador moderno (Chrome, Firefox, Edge, Safari)
- CORS habilitado en el backend

## 🎯 Pasos para Probar

### Paso 1: Verificar que el Backend está Ejecutándose

```bash
# En el directorio backend/
mvn spring-boot:run

# O si ya está compilado:
java -jar target/viralsim-backend-1.0.0.jar
```

Verifica en tu navegador: `http://localhost:8080/api/grafos`
Debería retornar un JSON vacío `[]`

### Paso 2: Servir el Frontend

Elige **UNA** de estas opciones:

#### Opción A: Live Server (Recomendado para VS Code)
1. Instala la extensión "Live Server"
2. Click derecho en `frontend/index.html` → "Open with Live Server"
3. Se abre automáticamente en `http://localhost:5500`

#### Opción B: http-server (Node.js)
```bash
# Instalar (una sola vez)
npm install -g http-server

# En la carpeta frontend/
cd frontend
http-server -p 3000 --cors
```
Abre: `http://localhost:3000`

#### Opción C: Python
```bash
# En la carpeta frontend/
cd frontend
python -m http.server 3000
```
Abre: `http://localhost:3000`

#### Opción D: Docker
```bash
cd frontend
docker run -p 3000:80 -v $(pwd):/usr/share/nginx/html nginx
```
Abre: `http://localhost:3000`

### Paso 3: Probar el Frontend

1. **Abre el navegador** en `http://localhost:3000` (o el puerto que uses)
2. Deberías ver la interfaz de ViralSim con fondo oscuro
3. **Abre la consola** (F12) para ver logs

### Paso 4: Generar una Red

1. En la pestaña "Inicio", haz clic en "⚡ Generar Red Watts-Strogatz"
2. Espera 2-3 segundos
3. Deberías ver el mensaje "✅ Red generada exitosamente"
4. Las estadísticas debajo deberían actualizarse

### Paso 5: Editar Nodos

1. Ve a la pestaña "Editar Nodos"
2. Deberías ver una tabla con los primeros 8 nodos
3. Usa la búsqueda o filtros para encontrar un nodo
4. Haz clic en "✏️ Editar"
5. Cambia la probabilidad o umbral con los sliders
6. Haz clic en "Guardar"
7. Deberías ver "✅ Nodo actualizado correctamente"

### Paso 6: Lanzar una Simulación

1. Ve a la pestaña "Lanzar Chisme"
2. El grafo debería cargarse (con vis.js)
3. **Haz clic en un nodo** en el grafo (el nodo semilla)
4. Deberías ver "Nodo Seleccionado ✅" con los datos
5. Selecciona un modelo: "🦠 Modelo Viral" (el más simple)
6. Escribe un mensaje en "Mensaje/Chisme"
7. Haz clic en "🚀 Lanzar Simulación"
8. Espera a que se ejecute (unos 2-3 segundos)

### Paso 7: Ver Propagación

1. Automáticamente se abrirá la pestaña "Propagación"
2. Deberías ver:
   - El grafo con los nodos coloreados por estado
   - Barras de estado mostrando cantidades
   - Una gráfica de propagación
3. La simulación debería auto-reproducirse paso a paso
4. Prueba los botones:
   - ⏸ Pausar/Reanudar
   - ⏭ Siguiente Paso
   - 🔄 Reiniciar

## 🔍 Troubleshooting

### Error: "No hay redes disponibles"
**Solución**: Genera una red en la pestaña "Inicio"

### Error: "Error al cargar nodos" o "Error CORS"
**Solución**: 
- Verifica que el backend está ejecutándose en `http://localhost:8080`
- Abre la consola (F12) y mira el error exacto
- Revisa que el backend tiene CORS habilitado

### Error: "Error al ejecutar la simulación"
**Solución**:
- Verifica que seleccionaste un nodo haciendo clic
- Verifica que escribiste un mensaje
- Revisa la consola para más detalles
- Verifica que el backend está respondiendo

### El grafo no se visualiza
**Solución**:
- Comprueba que vis.js está cargado (verifica en la pestaña Red de F12)
- Intenta recargar la página (Ctrl+R)
- Usa otro navegador (Chrome suele ser más confiable)

### La gráfica no se actualiza
**Solución**:
- Verifica que Chart.js está cargado (verifica en F12)
- Intenta pausar y reanudar la simulación

## 📊 Datos Esperados

### Cuando generas una red, debería haber:
- **250 nodos** (nombres de usuario aleatorios)
- **Aristas entre nodos** (conexiones)
- **Estados iniciales** (10% resistentes, 30% pasivos, 60% no informados)

### Cuando ejecutas una simulación, debería haber:
- **Pasos de propagación** (generalmente 5-15 pasos según el modelo)
- **Cambios de estado** en los nodos
- **Gráfica de propagación** mostrando cómo crece la información

## 🐛 Debugging

### Abre F12 (Herramientas de Desarrollador) para ver:

**Pestaña Console**:
- Logs de cargas de datos
- Errores en las llamadas API
- Información de debugging

**Pestaña Network**:
- Verifica que las llamadas a `/api/*` devuelven 200
- Revisa el payload de peticiones/respuestas

**Pestaña Application**:
- Verifica que puedes ver el localStorage (si lo usas)

## 🎬 Flujo Completo de Prueba

```
1. ✅ Backend ejecutándose
2. ✅ Frontend servido
3. ✅ Generar red
4. ✅ (Opcional) Editar algunos nodos
5. ✅ Lanzar simulación (con nodo semilla y modelo)
6. ✅ Ver propagación con animación
```

## 💡 Consejos

- La **primera carga** puede ser lenta (descarga vis.js y Chart.js desde CDN)
- Si abres **muchas pestañas**, algunas pueden ralentizarse
- Para **mejor rendimiento**, usa Chrome o Edge
- **Recarga la página** (Ctrl+R) si algo se queda pegado

## 🚨 Si Algo Falla

1. **Abre la consola** (F12) y copia el error exacto
2. **Verifica el backend** está ejecutándose: `curl http://localhost:8080/api/grafos`
3. **Recarga ambas aplicaciones** (backend y frontend)
4. **Borra el cache** del navegador (Ctrl+Shift+Delete) y recarga

---

**¡Ahora estás listo para probar!** 🎉

Si tienes problemas, revisa los logs en la consola del navegador (F12).
