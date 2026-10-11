package com.cargorent.vehicle.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The subset of booking-service's booking JSON that this service reads. */
public record BookingSummary(Long id, Long userId, Long vehicleId, LocalDate startDate, LocalDate endDate,
                             String status, BigDecimal totalAmount) {
}
