package com.cargorent.vehicle.service;

import com.cargorent.vehicle.client.BookingClient;
import com.cargorent.vehicle.dto.BookingSummary;
import com.cargorent.vehicle.dto.RecommendationResponse;
import com.cargorent.vehicle.dto.VehicleResponse;
import com.cargorent.vehicle.entity.Vehicle;
import com.cargorent.vehicle.entity.VehicleStatus;
import com.cargorent.vehicle.entity.VehicleType;
import com.cargorent.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rule-based recommendation. Looks at the customer's past bookings and scores every
 * available vehicle:
 * <ul>
 *   <li>+3 same type as the type they rent most</li>
 *   <li>+2 same brand as the brand they rent most</li>
 *   <li>+2 price within 25% of their average daily price (+1 if within 50%)</li>
 *   <li>+1 same city as their most recent rental</li>
 * </ul>
 * New customers with no history get a budget-friendly pick from each vehicle type.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    static final int LIMIT = 6;
    private static final Set<String> IGNORED_STATUSES = Set.of("CANCELLED", "PENDING_PAYMENT");

    private final VehicleRepository vehicles;
    private final BookingClient bookingClient;

    public List<RecommendationResponse> recommend(Long userId) {
        List<Vehicle> available = vehicles.findByStatus(VehicleStatus.AVAILABLE);
        List<Vehicle> history = pastVehicles(userId);
        return history.isEmpty() ? forNewCustomer(available) : rank(available, history, LIMIT);
    }

    /** Pure scoring step, kept static so it can be unit tested without a database. */
    static List<RecommendationResponse> rank(List<Vehicle> candidates, List<Vehicle> history, int limit) {
        VehicleType favType = mostCommon(history, Vehicle::getType);
        String favBrand = mostCommon(history, Vehicle::getBrand);
        String lastCity = history.get(history.size() - 1).getCity();
        BigDecimal avgPrice = history.stream().map(Vehicle::getPricePerDay)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(history.size()), 2, RoundingMode.HALF_UP);

        List<RecommendationResponse> scored = new ArrayList<>();
        for (Vehicle v : candidates) {
            int score = 0;
            List<String> reasons = new ArrayList<>();
            if (v.getType() == favType) {
                score += 3;
                reasons.add("you often rent " + plural(favType));
            }
            if (v.getBrand().equalsIgnoreCase(favBrand)) {
                score += 2;
                reasons.add("you like " + favBrand);
            }
            double diff = v.getPricePerDay().subtract(avgPrice).abs()
                    .divide(avgPrice, 4, RoundingMode.HALF_UP).doubleValue();
            if (diff <= 0.25) {
                score += 2;
                reasons.add("fits your usual budget");
            } else if (diff <= 0.5) {
                score += 1;
            }
            if (lastCity != null && lastCity.equalsIgnoreCase(v.getCity())) {
                score += 1;
                reasons.add("available in " + lastCity);
            }
            if (score > 0) {
                String reason = reasons.isEmpty() ? "Similar to your past rentals"
                        : capitalize(String.join(", ", reasons));
                scored.add(new RecommendationResponse(VehicleResponse.from(v), score, reason));
            }
        }
        scored.sort(Comparator.comparingInt(RecommendationResponse::score).reversed()
                .thenComparing(r -> r.vehicle().pricePerDay()));
        return scored.stream().limit(limit).toList();
    }

    static List<RecommendationResponse> forNewCustomer(List<Vehicle> available) {
        return available.stream()
                .collect(Collectors.groupingBy(Vehicle::getType,
                        Collectors.minBy(Comparator.comparing(Vehicle::getPricePerDay))))
                .values().stream()
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(Vehicle::getPricePerDay))
                .limit(LIMIT)
                .map(v -> new RecommendationResponse(VehicleResponse.from(v), 1,
                        "Best value " + label(v.getType())))
                .toList();
    }

    private List<Vehicle> pastVehicles(Long userId) {
        List<BookingSummary> bookings;
        try {
            bookings = bookingClient.userBookings(userId);
        } catch (Exception e) {
            log.warn("Could not load booking history for recommendations: {}", e.getMessage());
            return List.of();
        }
        if (bookings == null) return List.of();
        List<Long> ids = bookings.stream()
                .filter(b -> !IGNORED_STATUSES.contains(b.status()))
                .sorted(Comparator.comparing(BookingSummary::startDate))
                .map(BookingSummary::vehicleId)
                .toList();
        Map<Long, Vehicle> byId = vehicles.findAllById(new HashSet<>(ids)).stream()
                .collect(Collectors.toMap(Vehicle::getId, Function.identity()));
        return ids.stream().map(byId::get).filter(Objects::nonNull).toList();
    }

    private static <T> T mostCommon(List<Vehicle> list, Function<Vehicle, T> key) {
        return list.stream().collect(Collectors.groupingBy(key, LinkedHashMap::new, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    private static String label(VehicleType type) {
        return switch (type) {
            case SUV, MUV -> type.name();
            default -> type.name().toLowerCase();
        };
    }

    private static String plural(VehicleType type) {
        return label(type) + "s";
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
