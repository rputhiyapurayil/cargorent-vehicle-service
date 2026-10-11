package com.cargorent.vehicle.service;

import com.cargorent.vehicle.client.BookingClient;
import com.cargorent.vehicle.common.ApiException;
import com.cargorent.vehicle.dto.VehicleMeta;
import com.cargorent.vehicle.dto.VehicleRequest;
import com.cargorent.vehicle.dto.VehicleResponse;
import com.cargorent.vehicle.entity.Vehicle;
import com.cargorent.vehicle.entity.VehicleLocation;
import com.cargorent.vehicle.entity.VehicleStatus;
import com.cargorent.vehicle.entity.VehicleType;
import com.cargorent.vehicle.repository.VehicleLocationRepository;
import com.cargorent.vehicle.repository.VehicleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    /** Rough centre of each city, used to place a newly added vehicle on the map. */
    static final Map<String, double[]> CITY_COORDS = Map.of(
            "Kochi", new double[]{9.9816, 76.2999},
            "Kozhikode", new double[]{11.2588, 75.7804},
            "Thiruvananthapuram", new double[]{8.5241, 76.9366},
            "Thrissur", new double[]{10.5276, 76.2144},
            "Kannur", new double[]{11.8745, 75.3704});

    private final VehicleRepository vehicles;
    private final VehicleLocationRepository locations;
    private final BookingClient bookingClient;

    public record SearchCriteria(String q, VehicleType type, String brand, String city, String fuelType,
                                 String transmission, Integer minSeats, BigDecimal minPrice, BigDecimal maxPrice,
                                 LocalDate from, LocalDate to, String sort, boolean includeAll) {
    }

    public List<VehicleResponse> search(SearchCriteria c) {
        if ((c.from() == null) != (c.to() == null)) {
            throw ApiException.badRequest("Provide both pickup and return dates");
        }
        if (c.from() != null && !c.to().isAfter(c.from())) {
            throw ApiException.badRequest("Return date must be after pickup date");
        }

        Specification<Vehicle> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (!c.includeAll()) p.add(cb.equal(root.get("status"), VehicleStatus.AVAILABLE));
            if (c.type() != null) p.add(cb.equal(root.get("type"), c.type()));
            if (notBlank(c.brand())) p.add(cb.equal(cb.lower(root.get("brand")), c.brand().toLowerCase()));
            if (notBlank(c.city())) p.add(cb.equal(cb.lower(root.get("city")), c.city().toLowerCase()));
            if (notBlank(c.fuelType())) p.add(cb.equal(cb.lower(root.get("fuelType")), c.fuelType().toLowerCase()));
            if (notBlank(c.transmission())) p.add(cb.equal(cb.lower(root.get("transmission")), c.transmission().toLowerCase()));
            if (c.minSeats() != null) p.add(cb.greaterThanOrEqualTo(root.get("seats"), c.minSeats()));
            if (c.minPrice() != null) p.add(cb.greaterThanOrEqualTo(root.get("pricePerDay"), c.minPrice()));
            if (c.maxPrice() != null) p.add(cb.lessThanOrEqualTo(root.get("pricePerDay"), c.maxPrice()));
            if (notBlank(c.q())) {
                String like = "%" + c.q().trim().toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("brand")), like),
                        cb.like(cb.lower(root.get("model")), like),
                        cb.like(cb.lower(root.get("registrationNumber")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };

        List<Vehicle> result = vehicles.findAll(spec, sortOf(c.sort()));

        if (c.from() != null) {
            Set<Long> booked = new HashSet<>(fetchBooked(c.from(), c.to()));
            result = result.stream().filter(v -> !booked.contains(v.getId())).toList();
        }
        return result.stream().map(VehicleResponse::from).toList();
    }

    public VehicleResponse get(Long id) {
        return VehicleResponse.from(find(id));
    }

    /** True when the vehicle is in service and has no overlapping booking in [from, to). */
    public boolean isAvailable(Long id, LocalDate from, LocalDate to) {
        Vehicle v = find(id);
        return v.getStatus() == VehicleStatus.AVAILABLE && !fetchBooked(from, to).contains(id);
    }

    public VehicleMeta meta() {
        return new VehicleMeta(
                Arrays.stream(VehicleType.values()).map(Enum::name).toList(),
                vehicles.findBrands(), vehicles.findCities(),
                List.of("Petrol", "Diesel", "Electric", "CNG"),
                List.of("Manual", "Automatic"));
    }

    @Transactional
    public VehicleResponse create(VehicleRequest req) {
        if (vehicles.existsByRegistrationNumberIgnoreCase(req.registrationNumber().trim())) {
            throw ApiException.conflict("A vehicle with this registration number already exists");
        }
        Vehicle v = new Vehicle();
        apply(v, req);
        vehicles.save(v);
        // Give the new vehicle a starting position so it appears on the fleet map.
        double[] base = CITY_COORDS.getOrDefault(v.getCity(), CITY_COORDS.get("Kochi"));
        locations.save(new VehicleLocation(v.getId(), base[0], base[1], 0));
        return VehicleResponse.from(v);
    }

    @Transactional
    public VehicleResponse update(Long id, VehicleRequest req) {
        Vehicle v = find(id);
        String reg = req.registrationNumber().trim();
        if (!v.getRegistrationNumber().equalsIgnoreCase(reg) && vehicles.existsByRegistrationNumberIgnoreCase(reg)) {
            throw ApiException.conflict("A vehicle with this registration number already exists");
        }
        apply(v, req);
        return VehicleResponse.from(v);
    }

    @Transactional
    public VehicleResponse setStatus(Long id, VehicleStatus status) {
        Vehicle v = find(id);
        v.setStatus(status);
        return VehicleResponse.from(v);
    }

    /**
     * "Remove" retires the vehicle instead of deleting the row, so past bookings,
     * payments and reports that reference its id stay valid.
     */
    @Transactional
    public void retire(Long id) {
        find(id).setStatus(VehicleStatus.RETIRED);
    }

    Vehicle find(Long id) {
        return vehicles.findById(id).orElseThrow(() -> ApiException.notFound("Vehicle not found"));
    }

    private List<Long> fetchBooked(LocalDate from, LocalDate to) {
        try {
            List<Long> ids = bookingClient.bookedVehicleIds(from, to);
            return ids == null ? List.of() : ids;
        } catch (Exception e) {
            log.warn("Booking service unreachable for availability check: {}", e.getMessage());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Availability check is temporarily unavailable. Please try again.");
        }
    }

    private static void apply(Vehicle v, VehicleRequest r) {
        v.setRegistrationNumber(r.registrationNumber().trim().toUpperCase());
        v.setType(r.type());
        v.setBrand(r.brand().trim());
        v.setModel(r.model().trim());
        v.setYear(r.year());
        v.setSeats(r.seats());
        v.setFuelType(r.fuelType());
        v.setTransmission(r.transmission());
        v.setPricePerDay(r.pricePerDay());
        if (r.status() != null) v.setStatus(r.status());
        v.setImageUrl(blankToNull(r.imageUrl()));
        v.setCity(blankToNull(r.city()));
        v.setDescription(r.description());
    }

    private static Sort sortOf(String sort) {
        if (sort == null) return Sort.by("pricePerDay").ascending();
        return switch (sort) {
            case "priceDesc" -> Sort.by("pricePerDay").descending();
            case "newest" -> Sort.by("createdAt").descending();
            case "name" -> Sort.by("brand", "model");
            default -> Sort.by("pricePerDay").ascending();
        };
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String blankToNull(String s) {
        return notBlank(s) ? s.trim() : null;
    }
}
