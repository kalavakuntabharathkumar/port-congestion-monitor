package com.example.portmonitor.repository;

import com.example.portmonitor.entity.CongestionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface CongestionEventRepository extends JpaRepository<CongestionEvent, Long> {
    @Query("SELECT c FROM CongestionEvent c WHERE c.port.id = :portId AND c.startTime >= :since ORDER BY c.startTime DESC")
    List<CongestionEvent> findRecentByPort(@Param("portId") Long portId, @Param("since") Instant since);
    @Query("SELECT c FROM CongestionEvent c WHERE c.endTime IS NULL ORDER BY c.startTime DESC")
    List<CongestionEvent> findActiveEvents();
}