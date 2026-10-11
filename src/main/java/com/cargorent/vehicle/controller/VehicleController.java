package com.cargorent.vehicle.controller;

import com.cargorent.vehicle.common.CurrentUser;
import com.cargorent.vehicle.dto.RecommendationResponse;
import com.cargorent.vehicle.dto.VehicleMeta;
import com.cargorent.vehicle.dto.VehicleRequest;
import com.cargorent.vehicle.dto.VehicleResponse;
import com.cargorent.vehicle.entity.VehicleStatus;
import com.cargorent.vehicle.entity.VehicleType;
import com.cargorent.vehicle.service.RecommendationService;
import com.cargorent.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final RecommendationService recommendationService;

    /** Public search. Pass from/to to only see vehicles free on those dates. */
    @GetMapping
    public List<VehicleResponse> search(CurrentUser user,
                                        @RequestParam(required = false) String q,
                                        @RequestParam(required = false) VehicleType type,
                                        @RequestParam(required = false) String brand,
                                        @RequestParam(required = false) String city,
                                        @RequestParam(required = false) String fuelType,
                                        @RequestParam(required = false) String transmission,
                                        @RequestParam(required = false) Integer minSeats,
                                        @RequestParam(required = false) BigDecimal minPrice,
                                        @RequestParam(required = false) BigDecimal maxPrice,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                        @RequestParam(required = false) String sort,
                                        @RequestParam(defaultValue = "false") boolean includeAll) {
        boolean all = includeAll && user.isAdmin();
        return vehicleService.search(new VehicleService.SearchCriteria(q, type, brand, city, fuelType, transmission,
                minSeats, minPrice, maxPrice, from, to, sort, all));
    }

    @GetMapping("/meta")
    public VehicleMeta meta() {
        return vehicleService.meta();
    }

    @GetMapping("/{id:\\d+}")
    public VehicleResponse get(@PathVariable Long id) {
        return vehicleService.get(id);
    }

    @GetMapping("/{id:\\d+}/availability")
    public Map<String, Object> availability(@PathVariable Long id,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return Map.of("vehicleId", id, "from", from, "to", to, "available", vehicleService.isAvailable(id, from, to));
    }

    @GetMapping("/recommendations")
    public List<RecommendationResponse> recommendations(CurrentUser user) {
        return recommendationService.recommend(user.requireId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse create(CurrentUser user, @Valid @RequestBody VehicleRequest req) {
        user.requireAdmin();
        return vehicleService.create(req);
    }

    @PutMapping("/{id:\\d+}")
    public VehicleResponse update(CurrentUser user, @PathVariable Long id, @Valid @RequestBody VehicleRequest req) {
        user.requireAdmin();
        return vehicleService.update(id, req);
    }

    @PatchMapping("/{id:\\d+}/status")
    public VehicleResponse setStatus(CurrentUser user, @PathVariable Long id, @RequestParam VehicleStatus value) {
        user.requireAdmin();
        return vehicleService.setStatus(id, value);
    }

    @DeleteMapping("/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(CurrentUser user, @PathVariable Long id) {
        user.requireAdmin();
        vehicleService.retire(id);
    }
}
