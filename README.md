# ViralSim

A network propagation simulation platform that models how information, influence, or viral content spreads through social networks.

## Overview

ViralSim is a web-based application that simulates the propagation of information across networks using three different propagation models:

- **Viral Model**: Each node propagates with its own probability
- **Independent Cascade**: Edge-based propagation with single activation attempt
- **Threshold Model**: Social pressure-based adoption (percentage-based)

## Features

- **Simulation Engine**: Step-by-step execution of propagation models on network graphs
- **Network Generation**: Watts-Strogatz topology generation for realistic network structures
- **Real-time Metrics**: Calculates network centrality, adoption rates, and propagation statistics
- **Interactive Visualization**: Web-based frontend for graph visualization and control
- **Persistent Storage**: MySQL database for storing simulations and historical data

## Project Structure

```
backend/          # Java simulation engine
├── models/       # Graph, Node, Edge, State definitions
├── propagation/  # Propagation model implementations
├── engine/       # Simulation orchestration
├── metrics/      # Metrics calculation
├── database/     # Data access layer (DAOs)
└── utils/        # Utilities and generators

frontend/         # Web interface (HTML/JS/CSS)
database/         # SQL schema and seed data
docs/             # Technical documentation
```

## Tech Stack

- **Backend**: Java with Maven
- **Frontend**: HTML, CSS, JavaScript
- **Database**: MySQL
- **Architecture**: Multi-layered (Models → Propagation → Engine → Metrics → Database)

## Getting Started

1. Configure database connection in the backend
2. Run schema.sql to create tables
3. Build the backend: `mvn clean install`
4. Deploy frontend to web server
5. Run simulations through the web interface

## Documentation

- `docs/ARCHITECTURE.md` - System overview
- `docs/BACKEND.md` - Java classes and APIs
- `docs/DATABASE.md` - Schema and relationships
- `docs/FRONTEND.md` - UI components and features
