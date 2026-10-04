package com.example.portmonitor.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ais_records", indexes = {
    @Index(name = "idx_ais_mmsi_timestamp", columnList = "mmsi, timestamp"),
    @Index(name = "idx_ais_port_timestamp", columnList = "port_id, timestamp")
})
public class AisRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "mmsi", length = 9, nullable = false)
    private String mmsi;
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "sog")
    private Double sog;
    @Column(name = "cog")
    private Double cog;
    @Column(name = "heading")
    private Integer heading;
    @Column(name = "status")
    private Integer status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "port_id")
    private Port port;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMmsi() { return mmsi; }
    public void setMmsi(String mmsi) { this.mmsi = mmsi; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getSog() { return sog; }
    public void setSog(Double sog) { this.sog = sog; }
    public Double getCog() { return cog; }
    public void setCog(Double cog) { this.cog = cog; }
    public Integer getHeading() { return heading; }
    public void setHeading(Integer heading) { this.heading = heading; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Port getPort() { return port; }
    public void setPort(Port port) { this.port = port; }
}