package com.example.portmonitor.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Polygon;

@Entity
@Table(name = "ports")
public class Port {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "port_name", nullable = false)
    private String portName;
    @Column(name = "un_locode", length = 5)
    private String unLocode;
    @Column(name = "state", length = 2)
    private String state;
    @Column(name = "boundary", columnDefinition = "geometry(Polygon, 4326)")
    private Polygon boundary;
    @Column(name = "berth_count")
    private Integer berthCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPortName() { return portName; }
    public void setPortName(String portName) { this.portName = portName; }
    public String getUnLocode() { return unLocode; }
    public void setUnLocode(String unLocode) { this.unLocode = unLocode; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Polygon getBoundary() { return boundary; }
    public void setBoundary(Polygon boundary) { this.boundary = boundary; }
    public Integer getBerthCount() { return berthCount; }
    public void setBerthCount(Integer berthCount) { this.berthCount = berthCount; }
}