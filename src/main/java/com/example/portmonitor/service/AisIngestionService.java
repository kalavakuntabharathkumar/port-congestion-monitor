package com.example.portmonitor.service;

import com.example.portmonitor.entity.AisRecord;
import com.example.portmonitor.entity.Port;
import com.example.portmonitor.entity.Vessel;
import com.example.portmonitor.repository.AisRecordRepository;
import com.example.portmonitor.repository.PortRepository;
import com.example.portmonitor.repository.VesselRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AisIngestionService {
    private static final Logger log = LoggerFactory.getLogger(AisIngestionService.class);
    private static final int BATCH_SIZE = 5000;
    private static final DateTimeFormatter AIS_TIMESTAMP_FORMAT = DateTimeFormatter.ISO_INSTANT;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final AisRecordRepository aisRecordRepository;
    private final PortRepository portRepository;
    private final VesselRepository vesselRepository;
    private final JdbcTemplate jdbcTemplate;

    public AisIngestionService(AisRecordRepository aisRecordRepository, PortRepository portRepository, VesselRepository vesselRepository, JdbcTemplate jdbcTemplate) {
        this.aisRecordRepository = aisRecordRepository;
        this.portRepository = portRepository;
        this.vesselRepository = vesselRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public IngestionResult ingestCsv(MultipartFile file) throws Exception {
        long startTime = System.currentTimeMillis();
        int totalRecords = 0, insertedRecords = 0, errorCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {
            List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
            for (CSVRecord record : csvParser) {
                totalRecords++;
                try {
                    String mmsi = record.get("MMSI");
                    Instant timestamp = Instant.from(AIS_TIMESTAMP_FORMAT.parse(record.get("BaseDateTime")));
                    Double lat = parseDouble(record.get("LAT"));
                    Double lon = parseDouble(record.get("LON"));
                    Double sog = parseDouble(record.get("SOG"));
                    Double cog = parseDouble(record.get("COG"));
                    Integer heading = parseInt(record.get("Heading"));
                    Integer status = parseInt(record.get("Status"));
                    if (mmsi == null || mmsi.isBlank() || lat == null || lon == null || timestamp == null) {
                        errorCount++; continue;
                    }
                    vesselRepository.findById(mmsi).orElseGet(() -> {
                        Vessel v = new Vessel(); v.setMmsi(mmsi);
                        v.setVesselName(record.get("VesselName"));
                        v.setVesselType(parseInt(record.get("VesselType")));
                        v.setLength(parseInt(record.get("Length")));
                        v.setWidth(parseInt(record.get("Width")));
                        v.setDraft(parseDouble(record.get("Draft")));
                        return vesselRepository.save(v);
                    }).setLastSeen(timestamp);
                    Port port = portRepository.findByLocation(lon, lat).orElse(null);
                    Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
                    batch.add(new Object[]{mmsi, timestamp, lat, lon, sog, cog, heading, status, port != null ? port.getId() : null, point.toString()});
                    if (batch.size() >= BATCH_SIZE) {
                        insertedRecords += flushBatch(batch);
                        batch.clear();
                    }
                } catch (Exception e) { log.warn("Failed to parse AIS record: {}", e.getMessage()); errorCount++; }
            }
            if (!batch.isEmpty()) insertedRecords += flushBatch(batch);
        }
        long durationMs = System.currentTimeMillis() - startTime;
        log.info("Ingested {} records in {} ms ({} errors)", insertedRecords, durationMs, errorCount);
        return new IngestionResult(totalRecords, insertedRecords, errorCount, durationMs);
    }

    private int flushBatch(List<Object[]> batch) {
        String sql = "INSERT INTO ais_records (mmsi, timestamp, latitude, longitude, sog, cog, heading, status, port_id, geom) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText(?, 4326)) " +
                     "ON CONFLICT (mmsi, timestamp) DO NOTHING";
        int[] results = jdbcTemplate.batchUpdate(sql, batch);
        int count = 0; for (int r : results) if (r > 0) count++;
        return count;
    }
    private Double parseDouble(String s) { try { return s != null && !s.isBlank() ? Double.parseDouble(s) : null; } catch (NumberFormatException e) { return null; } }
    private Integer parseInt(String s) { try { return s != null && !s.isBlank() ? Integer.parseInt(s) : null; } catch (NumberFormatException e) { return null; } }

    public record IngestionResult(int totalRecords, int insertedRecords, int errorCount, long durationMs) {}
}