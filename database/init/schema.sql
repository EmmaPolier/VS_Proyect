CREATE DATABASE IF NOT EXISTS viralSim_graph;
USE viralSim_graph;
SET FOREIGN_KEY_CHECKS = 0;

-- Borrar tablas (de mayor a menor dependencia)
DROP TABLE IF EXISTS NodoSimulacion;
DROP TABLE IF EXISTS PasoSimulacion;
DROP TABLE IF EXISTS ConfiguracionSimulacion;
DROP TABLE IF EXISTS Simulacion;
DROP TABLE IF EXISTS Arista;
DROP TABLE IF EXISTS Nodo;
DROP TABLE IF EXISTS Grafo;
DROP TABLE IF EXISTS ModeloPropagacion;
DROP TABLE IF EXISTS Estado;

SET FOREIGN_KEY_CHECKS = 1;

-- ─────────────────────────────────────────
-- TABLAS BASE
-- ─────────────────────────────────────────

CREATE TABLE Estado (
  id     INT         NOT NULL,
  nombre VARCHAR(50) NOT NULL,

  CONSTRAINT pk_estado        PRIMARY KEY (id),
  CONSTRAINT uq_estado_nombre UNIQUE      (nombre),
  CONSTRAINT ck_estado_nombre CHECK       (nombre IN ('NO_INFORMADO','INFORMADO_ACTIVO','INFORMADO_PASIVO','RESISTENTE'))
);

CREATE TABLE ModeloPropagacion (
  id          INT          NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(100) NOT NULL,
  descripcion TEXT,

  CONSTRAINT pk_modelo        PRIMARY KEY (id),
  CONSTRAINT uq_modelo_nombre UNIQUE      (nombre),
  CONSTRAINT ck_modelo_nombre CHECK       (CHAR_LENGTH(nombre) >= 2)
);

CREATE TABLE Grafo (
  id         INT       NOT NULL AUTO_INCREMENT,
  totalNodos INT       NOT NULL,
  creadoEn   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT pk_grafo            PRIMARY KEY (id),
  CONSTRAINT ck_grafo_totalNodos CHECK       (totalNodos >= 0)
);

-- ─────────────────────────────────────────
-- NODO
-- ─────────────────────────────────────────

CREATE TABLE Nodo (
  id                      INT          NOT NULL AUTO_INCREMENT,
  grafoId                 INT          NOT NULL,
  estadoId                INT          NOT NULL,
  padreId                 INT          DEFAULT NULL,
  nombre                  VARCHAR(100) NOT NULL,
  probabilidadPropagacion DOUBLE       NOT NULL DEFAULT 0.0,
  umbral                  DOUBLE       NOT NULL DEFAULT 0.5,
  centralidadGrado        DOUBLE       NOT NULL DEFAULT 0.0,
  betweenness             DOUBLE       NOT NULL DEFAULT 0.0,

  CONSTRAINT pk_nodo                         PRIMARY KEY (id),
  CONSTRAINT fk_nodo_grafo                   FOREIGN KEY (grafoId)  REFERENCES Grafo(id)  ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT fk_nodo_estado                  FOREIGN KEY (estadoId) REFERENCES Estado(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_nodo_padre                   FOREIGN KEY (padreId)  REFERENCES Nodo(id)   ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT uq_nodo_nombre_grafo            UNIQUE      (grafoId, nombre),
  CONSTRAINT ck_nodo_probabilidadPropagacion CHECK       (probabilidadPropagacion BETWEEN 0.0 AND 1.0),
  CONSTRAINT ck_nodo_umbral                  CHECK       (umbral                  BETWEEN 0.0 AND 1.0),
  CONSTRAINT ck_nodo_centralidadGrado        CHECK       (centralidadGrado        >= 0.0),
  CONSTRAINT ck_nodo_betweenness             CHECK       (betweenness             >= 0.0)
);

-- ─────────────────────────────────────────
-- ARISTA
-- ─────────────────────────────────────────

CREATE TABLE Arista (
  id                 INT     NOT NULL AUTO_INCREMENT,
  nodoOrigenId       INT     NOT NULL,
  nodoDestinoId      INT     NOT NULL,
  probabilidadArista DOUBLE  NOT NULL DEFAULT 0.5,
  activa             BOOLEAN NOT NULL DEFAULT TRUE,
  peso               DOUBLE  NOT NULL DEFAULT 1.0,

  CONSTRAINT pk_arista                PRIMARY KEY (id),
  CONSTRAINT fk_arista_nodoOrigen     FOREIGN KEY (nodoOrigenId)  REFERENCES Nodo(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_arista_nodoDestino    FOREIGN KEY (nodoDestinoId) REFERENCES Nodo(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT uq_arista_origen_destino UNIQUE      (nodoOrigenId, nodoDestinoId),
  CONSTRAINT ck_arista_probabilidad   CHECK       (probabilidadArista BETWEEN 0.0 AND 1.0),
  CONSTRAINT ck_arista_peso           CHECK       (peso > 0.0)
  
);

-- ─────────────────────────────────────────
-- SIMULACION
-- ─────────────────────────────────────────

CREATE TABLE Simulacion (
  id              INT       NOT NULL AUTO_INCREMENT,
  grafoId         INT       NOT NULL,
  modeloId        INT       NOT NULL,
  nodoSemillaId   INT       NOT NULL,
  iniciadaEn      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  totalPasos      INT       NOT NULL DEFAULT 0,
  totalInformados INT       NOT NULL DEFAULT 0,
  paso50Porciento INT       DEFAULT NULL,
  resultado       TEXT,

  CONSTRAINT pk_simulacion                 PRIMARY KEY (id),
  CONSTRAINT fk_simulacion_grafo           FOREIGN KEY (grafoId)       REFERENCES Grafo(id)             ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT fk_simulacion_modelo          FOREIGN KEY (modeloId)      REFERENCES ModeloPropagacion(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_simulacion_nodoSemilla     FOREIGN KEY (nodoSemillaId) REFERENCES Nodo(id)              ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT ck_simulacion_totalPasos      CHECK       (totalPasos      >= 0),
  CONSTRAINT ck_simulacion_totalInformados CHECK       (totalInformados >= 0),
  CONSTRAINT ck_simulacion_paso50          CHECK       (paso50Porciento IS NULL OR paso50Porciento >= 0)
);

-- ─────────────────────────────────────────
-- CONFIGURACION SIMULACION
-- ─────────────────────────────────────────

CREATE TABLE ConfiguracionSimulacion (
  id                 INT    NOT NULL AUTO_INCREMENT,
  simulacionId       INT    NOT NULL,
  probabilidadGlobal DOUBLE NOT NULL DEFAULT 0.5,
  velocidadAnimacion INT    NOT NULL DEFAULT 500,

  CONSTRAINT pk_configSim                    PRIMARY KEY (id),
  CONSTRAINT fk_configSim_simulacion         FOREIGN KEY (simulacionId) REFERENCES Simulacion(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT uq_configSim_simulacionId       UNIQUE      (simulacionId),
  CONSTRAINT ck_configSim_probabilidadGlobal CHECK       (probabilidadGlobal BETWEEN 0.0 AND 1.0),
  CONSTRAINT ck_configSim_velocidadAnimacion CHECK       (velocidadAnimacion > 0)
);

-- ─────────────────────────────────────────
-- PASO SIMULACION
-- ─────────────────────────────────────────

CREATE TABLE PasoSimulacion (
  id               INT NOT NULL AUTO_INCREMENT,
  simulacionId     INT NOT NULL,
  numeroPaso       INT NOT NULL,
  nuevosInformados INT NOT NULL DEFAULT 0,
  totalActivos     INT NOT NULL DEFAULT 0,
  totalResistentes INT NOT NULL DEFAULT 0,
  totalPasivos     INT NOT NULL DEFAULT 0,
  totalInformados  INT NOT NULL DEFAULT 0,

  CONSTRAINT pk_pasoSim                   PRIMARY KEY (id),
  CONSTRAINT fk_pasoSim_simulacion        FOREIGN KEY (simulacionId) REFERENCES Simulacion(id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT uq_pasoSim_simulacion_numero UNIQUE      (simulacionId, numeroPaso),
  CONSTRAINT ck_pasoSim_numeroPaso        CHECK       (numeroPaso       >= 0),
  CONSTRAINT ck_pasoSim_nuevosInformados  CHECK       (nuevosInformados >= 0),
  CONSTRAINT ck_pasoSim_totalActivos      CHECK       (totalActivos     >= 0),
  CONSTRAINT ck_pasoSim_totalResistentes  CHECK       (totalResistentes >= 0),
  CONSTRAINT ck_pasoSim_totalPasivos      CHECK       (totalPasivos     >= 0),
  CONSTRAINT ck_pasoSim_totalInformados   CHECK       (totalInformados  >= 0)
);

-- ─────────────────────────────────────────
-- NODO SIMULACION
-- ─────────────────────────────────────────

CREATE TABLE NodoSimulacion (
  id            INT NOT NULL AUTO_INCREMENT,
  simulacionId  INT NOT NULL,
  pasoId        INT NOT NULL,
  nodoId        INT NOT NULL,
  estadoId      INT NOT NULL,
  pasoInfeccion INT DEFAULT NULL,

  CONSTRAINT pk_nodoSim               PRIMARY KEY (id),
  CONSTRAINT fk_nodoSim_simulacion    FOREIGN KEY (simulacionId) REFERENCES Simulacion(id)     ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT fk_nodoSim_paso          FOREIGN KEY (pasoId)       REFERENCES PasoSimulacion(id) ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT fk_nodoSim_nodo          FOREIGN KEY (nodoId)       REFERENCES Nodo(id)            ON DELETE CASCADE  ON UPDATE CASCADE,
  CONSTRAINT fk_nodoSim_estado        FOREIGN KEY (estadoId)     REFERENCES Estado(id)          ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT uq_nodoSim_paso_nodo     UNIQUE      (pasoId, nodoId),
  CONSTRAINT ck_nodoSim_pasoInfeccion CHECK       (pasoInfeccion IS NULL OR pasoInfeccion >= 0)
);