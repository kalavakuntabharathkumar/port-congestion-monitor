package com.example.portmonitor.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "congestion_events")
public class CongestionEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "port_id", nullable = false)
    private Port port;
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    @Column(name = "end_time")
    private Instant endTime;
    @Column(name = "vessels_in_queue")
    private Integer vesselsInQueue;
    @Column(name = "avg_dwell_time_hours")
    private Double avgDwellTimeHours;
    @Column(name = "severity")
    private String severity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Port getPort() { return port; }
    public void setPort(Port port) { this.port = port; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public Integer getVesselsInQueue() { return vesselsInQueue; }
    public void setVesselsInQueue(Integer vesselsInQueue) { this.vesselsInQueue = vesselsInQueue; }
    public Double getAvgDwellTimeHours() { return avgDwellTimeHours; }
    public void setAvgDwellTimeHours(Double avgDwellTimeHours) { this.avgDwellTimeHours = avgDwellTimeHours; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}