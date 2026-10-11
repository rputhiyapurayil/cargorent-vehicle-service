package com.cargorent.vehicle.repository;

import com.cargorent.vehicle.entity.Vehicle;
import com.cargorent.vehicle.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);
    List<Vehicle> findByStatus(VehicleStatus status);

    @Query("select distinct v.brand from Vehicle v where v.status <> com.cargorent.vehicle.entity.VehicleStatus.RETIRED order by v.brand")
    List<String> findBrands();

    @Query("select distinct v.city from Vehicle v where v.city is not null and v.status <> com.cargorent.vehicle.entity.VehicleStatus.RETIRED order by v.city")
    List<String> findCities();
}
