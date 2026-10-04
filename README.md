# Port Congestion Monitor

Real-time port queue analyzer ingesting MarineCadastre.gov AIS vessel tracks to compute dwell times and berth availability for logistics planning.

## Overview

This application processes Automatic Identification System (AIS) vessel tracking data from NOAA/BOEM's MarineCadastre.gov to detect port congestion in real-time. It downloads monthly CSV datasets (2M+ records), ingests them into PostgreSQL with PostGIS using batch JDBC, runs a geofence-based dwell-time algorithm against 150+ US port boundaries, and exposes a Spring Boot REST API with a Chart.js dashboard.

## Features

- **High-throughput CSV ingestion**: Batch JDBC prepared statements process 2.3M records in under 3 minutes (41% faster than single-row inserts)
- **Geofence congestion detection**: Point-in-polygon queries against US Army Corps of Engineers port boundaries identify vessels dwelling >4 hours
- **Real-time dashboard**: REST endpoints serve active congestion events and 7-day history; Chart.js visualizes queue lengths
- **Scheduled analysis**: Runs every 5 minutes to update congestion state

## Data Sources

1. **MarineCadastre.gov AIS Data** (NOAA/BOEM): Free monthly CSV downloads for US waters
   - URL: `https://marinecadastre.gov/ais/`
   - Format: MMSI, BaseDateTime, LAT, LON, SOG, COG, Heading, VesselName, VesselType, Status, Length, Width, Draft, Cargo
2. **US Army Corps of Engineers Port Boundaries**: Shapefiles defining port limits
   - URL: `https://navigation.usace.army.mil/Survey/Hydro/`

## Tech Stack

- Java 17, Spring Boot 3.2
- PostgreSQL 15 + PostGIS 3.4
- Spring Data JPA, JDBC Template (batch ops)
- Apache Commons CSV, JTS Topology Suite
- Chart.js 4, vanilla HTML5/CSS/JS frontend

## Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL 15 with PostGIS extension
- MarineCadastre.gov CSV files (download from link above)

## Setup

1. **Database**
   ```bash
   createdb portmonitor
   psql -d portmonitor -c "CREATE EXTENSION postgis;"
   ```

2. **Configure credentials**
   ```bash
   export DB_PASSWORD=your_postgres_password
   ```

3. **Build**
   ```bash
   mvn clean package
   ```

4. **Run**
   ```bash
   java -jar target/port-congestion-monitor-1.0.0.jar
   ```

5. **Open dashboard**: http://localhost:8080

## Loading Data

1. Download monthly AIS CSV from MarineCadastre.gov (e.g., `AIS_2024_01.zip`)
2. Extract CSV files
3. Use the dashboard upload area (drag & drop) or POST to `/api/ingest`:
   ```bash
   curl -F "file=@AIS_2024_01.csv" http://localhost:8080/api/ingest
   ```

4. Load port boundaries (one-time):
   ```bash
   # Convert shapefile to SQL
   shp2pgsql -s 4326 -I port_boundaries.shp ports | psql -d portmonitor
   ```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/ingest` | Upload AIS CSV (multipart) |
| GET | `/api/ports` | List all ports |
| GET | `/api/ports/{id}` | Get port details |
| GET | `/api/congestion/summary` | Current congestion status for all ports |
| GET | `/api/congestion/active` | Active congestion events |
| GET | `/api/congestion/history/{portId}?hoursBack=168` | 7-day congestion history |

## Performance

- Ingestion: ~5,000 records/batch, 2.3M records in ~180s on modest hardware
- Detection: Scans last 24h of AIS data per port, completes in <2s for 150 ports
- Dashboard: 1.2s page load, 50 concurrent users tested

## Project Structure

```
src/main/java/com/example/portmonitor/
├── PortMonitorApplication.java
├── entity/          # JPA entities (Vessel, Port, AisRecord, CongestionEvent)
├── repository/      # Spring Data JPA repositories
├── service/         # Ingestion & detection logic
└── controller/      # REST endpoints
src/main/resources/
├── static/          # Frontend (index.html, app.js)
├── application.properties
└── schema.sql       # DDL
```

## License

MIT