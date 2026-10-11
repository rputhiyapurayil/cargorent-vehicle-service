package com.cargorent.vehicle.client;

import com.cargorent.vehicle.dto.BookingSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Component
public class BookingClient {

    private final RestClient rest;

    public BookingClient(RestClient.Builder builder, @Value("${services.booking.url}") String baseUrl) {
        this.rest = builder.baseUrl(baseUrl).build();
    }

    /** Vehicles that have a booking overlapping [from, to). Used to hide them in a date search. */
    public List<Long> bookedVehicleIds(LocalDate from, LocalDate to) {
        return rest.get()
                .uri(u -> u.path("/bookings/booked-vehicle-ids").queryParam("from", from).queryParam("to", to).build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }

    /** Vehicles currently picked up by a customer (driven by the GPS simulator). */
    public List<Long> vehiclesOnRent() {
        LocalDate today = LocalDate.now();
        return rest.get()
                .uri(u -> u.path("/bookings/booked-vehicle-ids")
                        .queryParam("from", today).queryParam("to", today.plusDays(1))
                        .queryParam("activeOnly", true).build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }

    public List<BookingSummary> userBookings(Long userId) {
        return rest.get()
                .uri("/bookings/user/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() { });
    }
}
