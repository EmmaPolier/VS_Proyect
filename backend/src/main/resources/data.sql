-- Datos iniciales para Estado
INSERT IGNORE INTO Estado (id, nombre) VALUES
(0, 'NO_INFORMADO'),
(1, 'INFORMADO_ACTIVO'),
(2, 'INFORMADO_PASIVO'),
(3, 'RESISTENTE');

-- Datos iniciales para ModeloPropagacion
INSERT IGNORE INTO ModeloPropagacion (nombre, descripcion) VALUES
('Modelo Viral',           'Cada nodo se propaga con su propia probabilidad de propagacion individual.'),
('Cascada Independiente',  'Propagacion basada en aristas con un unico intento de activacion por vecino.'),
('Modelo de Umbral',       'Adopcion basada en presion social: un nodo se activa cuando el porcentaje de vecinos activos supera su umbral.');
