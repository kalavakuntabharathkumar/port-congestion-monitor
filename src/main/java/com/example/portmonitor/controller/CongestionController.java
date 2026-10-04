package com.example.portmonitor.controller;

import com.example.portmonitor.entity.CongestionEvent;
import com.example.portmonitor.entity.Port;
import com.example.portmonitor.repository.PortRepository;
import com.example.portmonitor.service.CongestionDetectionService;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/congestion")
public class CongestionController {
    private final CongestionDetectionService detectionService;
    private final PortRepository portRepository;
    public CongestionController(CongestionDetectionService detectionService, PortRepository portRepository) {
        this.detectionService = detectionService; this.portRepository = portRepository;
    }
    @GetMapping("/active")
    public List<CongestionEvent> getActiveEvents() { return detectionService.getActiveEvents(); }
    @GetMapping("/history/{portId}")
    public List<CongestionEvent> getHistory(@PathVariable Long portId, @RequestParam(defaultValue = "168") int hoursBack) {
        return detectionService.getHistory(portId, Instant.now().minus(java.time.Duration.ofHours(hoursBack)));
    }
    @GetMapping("/summary")
    public List<Map<String, Object>> getSummary() {
        return portRepository.findAll().stream().map(port -> {
            List<CongestionEvent> active = detectionService.getActiveEvents().stream()
                    .filter(e -> e.getPort().getId().equals(port.getId())).toList();
            CongestionEvent current = active.isEmpty() ? null : active.get(0);
            return Map.of(
                "portId", port.getId(), "portName", port.getPortName(), "unLocode", port.getUnLocode(),
                "state", port.getState(), "berthCount", port.getBerthCount(),
                "isCongested", current != null, "vesselsInQueue", current != null ? current.getVesselsInQueue() : 0,
                "avgDwellHours", current != null ? current.getAvgDwellTimeHours() : 0,
                "severity", current != null ? current.getSeverity() : "NONE"
            );
        }).toList();
    }
}