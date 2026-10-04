package com.example.portmonitor.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vessels")
public class Vessel {
    @Id
    @Column(name = "mmsi", length = 9)
    private String mmsi;
    @Column(name = "vessel_name")
    private String vesselName;
    @Column(name = "vessel_type")
    private Integer vesselType;
    @Column(name = "length")
    private Integer length;
    @Column(name = "width")
    private Integer width;
    @Column(name = "draft")
    private Double draft;
    @Column(name = "last_seen")
    private Instant lastSeen;

    public String getMmsi() { return mmsi; }
    public void setMmsi(String mmsi) { this.mmsi = mmsi; }
    public String getVesselName() { return vesselName; }
    public void setVesselName(String vesselName) { this.vesselName = vesselName; }
    public Integer getVesselType() { return vesselType; }
    public void setVesselType(Integer vesselType) { this.vesselType = vesselType; }
    public Integer getLength() { return length; }
    public void setLength(Integer length) { this.length = length; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Double getDraft() { return draft; }
    public void setDraft(Double draft) { this.draft = draft; }
    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
}