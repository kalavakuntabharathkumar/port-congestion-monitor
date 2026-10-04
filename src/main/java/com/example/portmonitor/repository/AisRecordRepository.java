package com.example.portmonitor.repository;

import com.example.portmonitor.entity.AisRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface AisRecordRepository extends JpaRepository<AisRecord, Long> {
    @Query("SELECT a FROM AisRecord a WHERE a.mmsi = :mmsi AND a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp ASC")
    List<AisRecord> findByMmsiAndTimeRange(@Param("mmsi") String mmsi, @Param("start") Instant start, @Param("end") Instant end);
    @Query("SELECT a FROM AisRecord a WHERE a.port.id = :portId AND a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp ASC")
    List<AisRecord> findByPortAndTimeRange(@Param("portId") Long portId, @Param("start") Instant start, @Param("end") Instant end);
    int deleteByTimestampBefore(Instant cutoff);
}