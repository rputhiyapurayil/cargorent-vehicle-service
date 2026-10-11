package com.cargorent.vehicle.service;

import com.cargorent.vehicle.entity.VehicleLocation;
import com.cargorent.vehicle.repository.VehicleLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stands in for real GPS devices. Every few seconds, each vehicle that is currently
 * picked up "drives" a short distance in a slowly changing direction and a new fix is saved.
 * A real deployment would replace this with devices calling POST /vehicles/{id}/location.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cargorent.gps.simulator.enabled", havingValue = "true", matchIfMissing = true)
public class GpsSimulator {

    private static final double KM_PER_DEGREE = 111.0;

    private final VehicleLocationRepository locations;
    private final TrackingService trackingService;
    private final Map<Long, Double> headings = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${cargorent.gps.simulator.interval-ms:5000}", initialDelay = 10000)
    public void tick() {
        List<Long> onRent = trackingService.safeOnRent();
        for (Long vehicleId : onRent) {
            locations.findFirstByVehicleIdOrderByRecordedAtDescIdDesc(vehicleId)
                    .ifPresent(last -> locations.save(step(last)));
        }
    }

    VehicleLocation step(VehicleLocation last) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double heading = headings.getOrDefault(last.getVehicleId(), rnd.nextDouble(0, 2 * Math.PI))
                + rnd.nextDouble(-0.5, 0.5);
        headings.put(last.getVehicleId(), heading);
        double speed = rnd.nextDouble(20, 60);                       // km/h
        double km = speed * 5.0 / 3600.0 * 6;                        // exaggerated so movement is visible
        double lat = last.getLatitude() + Math.cos(heading) * km / KM_PER_DEGREE;
        double lng = last.getLongitude() + Math.sin(heading) * km / (KM_PER_DEGREE * Math.cos(Math.toRadians(lat)));
        return new VehicleLocation(last.getVehicleId(), round(lat), round(lng), Math.round(speed * 10) / 10.0);
    }

    private static double round(double v) {
        return Math.round(v * 1_000_000d) / 1_000_000d;
    }
}
