package com.example.portmonitor.repository;

import com.example.portmonitor.entity.Port;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortRepository extends JpaRepository<Port, Long> {
    @Query(value = "SELECT * FROM ports WHERE ST_Contains(boundary, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))", nativeQuery = true)
    Optional<Port> findByLocation(@Param("lon") Double lon, @Param("lat") Double lat);
    List<Port> findAllByOrderByPortNameAsc();
}