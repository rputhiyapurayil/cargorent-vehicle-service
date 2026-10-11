package com.cargorent.vehicle.dto;

import com.cargorent.vehicle.entity.VehicleStatus;
import com.cargorent.vehicle.entity.VehicleType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record VehicleRequest(
        @NotBlank @Size(max = 20) String registrationNumber,
        @NotNull VehicleType type,
        @NotBlank @Size(max = 50) String brand,
        @NotBlank @Size(max = 50) String model,
        @Min(1990) @Max(2100) Integer year,
        @Min(1) @Max(60) Integer seats,
        @Size(max = 20) String fuelType,
        @Size(max = 20) String transmission,
        @NotNull @DecimalMin(value = "1.0") @Digits(integer = 8, fraction = 2) BigDecimal pricePerDay,
        VehicleStatus status,
        @Size(max = 500) String imageUrl,
        @Size(max = 60) String city,
        @Size(max = 1000) String description) {
}
