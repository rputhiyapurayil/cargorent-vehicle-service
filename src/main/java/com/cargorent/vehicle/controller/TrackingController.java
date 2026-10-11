package com.cargorent.vehicle.controller;

import com.cargorent.vehicle.common.CurrentUser;
import com.cargorent.vehicle.dto.FleetLocationResponse;
import com.cargorent.vehicle.dto.LocationResponse;
import com.cargorent.vehicle.dto.LocationUpdateRequest;
import com.cargorent.vehicle.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping("/{id:\\d+}/location")
    public LocationResponse latest(CurrentUser user, @PathVariable Long id) {
        user.requireId();
        return trackingService.latest(user, id);
    }

    @GetMapping("/{id:\\d+}/locations")
    public List<LocationResponse> trail(CurrentUser user, @PathVariable Long id,
                                        @RequestParam(defaultValue = "100") int limit) {
        user.requireId();
        return trackingService.trail(user, id, limit);
    }

    /** Admin fleet map. */
    @GetMapping("/locations/latest")
    public List<FleetLocationResponse> fleet(CurrentUser user) {
        user.requireAdmin();
        return trackingService.fleet();
    }

    /** Endpoint a real GPS device would call. Admin only through the gateway. */
    @PostMapping("/{id:\\d+}/location")
    public LocationResponse record(CurrentUser user, @PathVariable Long id, @Valid @RequestBody LocationUpdateRequest req) {
        user.requireAdmin();
        return trackingService.record(id, req);
    }
}
