package com.cargorent.vehicle.repository;

import com.cargorent.vehicle.entity.VehicleLocation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VehicleLocationRepository extends JpaRepository<VehicleLocation, Long> {

    Optional<VehicleLocation> findFirstByVehicleIdOrderByRecordedAtDescIdDesc(Long vehicleId);

    List<VehicleLocation> findByVehicleIdOrderByRecordedAtDescIdDesc(Long vehicleId, Pageable pageable);

    @Query("select l from VehicleLocation l where l.id in (select max(l2.id) from VehicleLocation l2 group by l2.vehicleId)")
    List<VehicleLocation> findLatestForAllVehicles();
}
