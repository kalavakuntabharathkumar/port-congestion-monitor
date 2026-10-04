package com.example.portmonitor.service;

import com.example.portmonitor.entity.AisRecord;
import com.example.portmonitor.entity.CongestionEvent;
import com.example.portmonitor.entity.Port;
import com.example.portmonitor.repository.AisRecordRepository;
import com.example.portmonitor.repository.CongestionEventRepository;
import com.example.portmonitor.repository.PortRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CongestionDetectionService {
    private static final Logger log = LoggerFactory.getLogger(CongestionDetectionService.class);
    private static final double DWELL_THRESHOLD_HOURS = 4.0;
    private static final int QUEUE_THRESHOLD = 3;
    private static final Duration LOOKBACK_WINDOW = Duration.ofHours(24);
    private final PortRepository portRepository;
    private final AisRecordRepository aisRecordRepository;
    private final CongestionEventRepository congestionEventRepository;

    public CongestionDetectionService(PortRepository portRepository, AisRecordRepository aisRecordRepository, CongestionEventRepository congestionEventRepository) {
        this.portRepository = portRepository;
        this.aisRecordRepository = aisRecordRepository;
        this.congestionEventRepository = congestionEventRepository;
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void detectCongestion() {
        Instant now = Instant.now();
        Instant since = now.minus(LOOKBACK_WINDOW);
        for (Port port : portRepository.findAll()) {
            try { analyzePort(port, since, now); }
            catch (Exception e) { log.error("Congestion detection failed for port {}: {}", port.getPortName(), e.getMessage()); }
        }
    }

    private void analyzePort(Port port, Instant since, Instant now) {
        List<AisRecord> records = aisRecordRepository.findByPortAndTimeRange(port.getId(), since, now);
        if (records.isEmpty()) return;
        Map<String, List<AisRecord>> byVessel = records.stream().collect(Collectors.groupingBy(AisRecord::getMmsi));
        List<VesselDwell> dwells = new ArrayList<>();
        for (List<AisRecord> vesselRecords : byVessel.values()) {
            vesselRecords.sort(Comparator.comparing(AisRecord::getTimestamp));
            Instant first = vesselRecords.get(0).getTimestamp();
            Instant last = vesselRecords.get(vesselRecords.size() - 1).getTimestamp();
            double hours = Duration.between(first, last).toMinutes() / 60.0;
            if (hours >= DWELL_THRESHOLD_HOURS) dwells.add(new VesselDwell(vesselRecords.get(0).getMmsi(), first, last, hours));
        }
        int queueSize = dwells.size();
        if (queueSize >= QUEUE_THRESHOLD) {
            double avgDwell = dwells.stream().mapToDouble(VesselDwell::hours).average().orElse(0);
            String severity = avgDwell > 24 ? "HIGH" : avgDwell > 12 ? "MEDIUM" : "LOW";
            List<CongestionEvent> active = congestionEventRepository.findActiveEvents().stream()
                    .filter(e -> e.getPort().getId().equals(port.getId())).toList();
            if (active.isEmpty()) {
                CongestionEvent event = new CongestionEvent();
                event.setPort(port); event.setStartTime(now);
                event.setVesselsInQueue(queueSize); event.setAvgDwellTimeHours(avgDwell); event.setSeverity(severity);
                congestionEventRepository.save(event);
                log.info("New congestion event at {}: {} vessels, avg dwell {:.1}h", port.getPortName(), queueSize, avgDwell);
            } else {
                CongestionEvent event = active.get(0);
                event.setVesselsInQueue(queueSize); event.setAvgDwellTimeHours(avgDwell); event.setSeverity(severity);
                congestionEventRepository.save(event);
            }
        } else {
            for (CongestionEvent event : congestionEventRepository.findActiveEvents().stream()
                    .filter(e -> e.getPort().getId().equals(port.getId())).toList()) {
                event.setEndTime(now); congestionEventRepository.save(event);
            }
        }
    }

    public List<CongestionEvent> getActiveEvents() { return congestionEventRepository.findActiveEvents(); }
    public List<CongestionEvent> getHistory(Long portId, Instant since) { return congestionEventRepository.findRecentByPort(portId, since); }
    private record VesselDwell(String mmsi, Instant entry, Instant exit, double hours) {}
}