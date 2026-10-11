package com.cargorent.vehicle.dto;

public record RecommendationResponse(VehicleResponse vehicle, int score, String reason) {
}
