-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Vessels table
CREATE TABLE IF NOT EXISTS vessels (
    mmsi VARCHAR(9) PRIMARY KEY,
    vessel_name VARCHAR(255),
    vessel_type INTEGER,
    length INTEGER,
    width INTEGER,
    draft DOUBLE PRECISION,
    last_seen TIMESTAMP WITH TIME ZONE
);

-- Ports table with geometry
CREATE TABLE IF NOT EXISTS ports (
    id BIGSERIAL PRIMARY KEY,
    port_name VARCHAR(255) NOT NULL,
    un_locode VARCHAR(5),
    state VARCHAR(2),
    boundary GEOMETRY(Polygon, 4326),
    berth_count INTEGER
);
CREATE INDEX IF NOT EXISTS idx_ports_boundary ON ports USING GIST (boundary);

-- AIS records table
CREATE TABLE IF NOT EXISTS ais_records (
    id BIGSERIAL PRIMARY KEY,
    mmsi VARCHAR(9) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    sog DOUBLE PRECISION,
    cog DOUBLE PRECISION,
    heading INTEGER,
    status INTEGER,
    port_id BIGINT REFERENCES ports(id),
    geom GEOMETRY(Point, 4326)
);
CREATE INDEX IF NOT EXISTS idx_ais_mmsi_timestamp ON ais_records (mmsi, timestamp);
CREATE INDEX IF NOT EXISTS idx_ais_port_timestamp ON ais_records (port_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_ais_geom ON ais_records USING GIST (geom);

-- Congestion events table
CREATE TABLE IF NOT EXISTS congestion_events (
    id BIGSERIAL PRIMARY KEY,
    port_id BIGINT NOT NULL REFERENCES ports(id),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    vessels_in_queue INTEGER,
    avg_dwell_time_hours DOUBLE PRECISION,
    severity VARCHAR(10)
);
CREATE INDEX IF NOT EXISTS idx_congestion_port_start ON congestion_events (port_id, start_time);