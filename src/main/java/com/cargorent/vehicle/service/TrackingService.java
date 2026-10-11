package com.cargorent.vehicle.service;

import com.cargorent.vehicle.client.BookingClient;
import com.cargorent.vehicle.common.ApiException;
import com.cargorent.vehicle.common.CurrentUser;
import com.cargorent.vehicle.dto.BookingSummary;
import com.cargorent.vehicle.dto.FleetLocationResponse;
import com.cargorent.vehicle.dto.LocationResponse;
import com.cargorent.vehicle.dto.LocationUpdateRequest;
import com.cargorent.vehicle.entity.Vehicle;
import com.cargorent.vehicle.entity.VehicleLocation;
import com.cargorent.vehicle.repository.VehicleLocationRepository;
import com.cargorent.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final VehicleRepository vehicles;
    private final VehicleLocationRepository locations;
    private final BookingClient bookingClient;

    public LocationResponse latest(CurrentUser user, Long vehicleId) {
        checkCanTrack(user, vehicleId);
        return locations.findFirstByVehicleIdOrderByRecordedAtDescIdDesc(vehicleId)
                .map(LocationResponse::from)
                .orElseThrow(() -> ApiException.notFound("No GPS data for this vehicle yet"));
    }

    /** Recent trail, oldest first, for drawing the route on the map. */
    public List<LocationResponse> trail(CurrentUser user, Long vehicleId, int limit) {
        checkCanTrack(user, vehicleId);
        List<LocationResponse> recent = locations
                .findByVehicleIdOrderByRecordedAtDescIdDesc(vehicleId, PageRequest.of(0, Math.min(Math.max(limit, 1), 500)))
                .stream().map(LocationResponse::from).collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(recent);
        return recent;
    }

    public List<FleetLocationResponse> fleet() {
        Map<Long, Vehicle> byId = vehicles.findAll().stream()
                .collect(Collectors.toMap(Vehicle::getId, Function.identity()));
        Set<Long> onRent = new HashSet<>(safeOnRent());
        return locations.findLatestForAllVehicles().stream()
                .filter(l -> byId.containsKey(l.getVehicleId()))
                .map(l -> {
                    Vehicle v = byId.get(l.getVehicleId());
                    return new FleetLocationResponse(v.getId(), v.displayName(), v.getRegistrationNumber(),
                            v.getType().name(), v.getStatus().name(), onRent.contains(v.getId()),
                            LocationResponse.from(l));
                })
                .sorted(Comparator.comparing(FleetLocationResponse::onRent).reversed()
                        .thenComparing(FleetLocationResponse::name))
                .toList();
    }

    public LocationResponse record(Long vehicleId, LocationUpdateRequest req) {
        if (!vehicles.existsById(vehicleId)) throw ApiException.notFound("Vehicle not found");
        VehicleLocation saved = locations.save(new VehicleLocation(vehicleId, req.latitude(), req.longitude(),
                req.speedKmph() == null ? 0 : req.speedKmph()));
        return LocationResponse.from(saved);
    }

    List<Long> safeOnRent() {
        try {
            List<Long> ids = bookingClient.vehiclesOnRent();
            return ids == null ? List.of() : ids;
        } catch (Exception e) {
            log.debug("Booking service unreachable: {}", e.getMessage());
            return List.of();
        }
    }

    /** Admins can track any vehicle; customers only a vehicle they have currently picked up. */
    private void checkCanTrack(CurrentUser user, Long vehicleId) {
        if (user.isInternal() || user.isAdmin()) return;
        List<BookingSummary> mine;
        try {
            mine = bookingClient.userBookings(user.requireId());
        } catch (Exception e) {
            throw ApiException.forbidden("Unable to verify your rental right now");
        }
        boolean renting = mine != null && mine.stream()
                .anyMatch(b -> vehicleId.equals(b.vehicleId()) && "PICKED_UP".equals(b.status()));
        if (!renting) {
            throw ApiException.forbidden("You can track a vehicle only while you have it on rent");
        }
    }
}
