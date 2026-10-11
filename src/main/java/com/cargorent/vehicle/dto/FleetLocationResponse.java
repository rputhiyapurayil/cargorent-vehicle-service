package com.cargorent.vehicle.dto;

/** Admin fleet map: a vehicle with its latest position and whether it is out on a rental now. */
public record FleetLocationResponse(Long vehicleId, String name, String registrationNumber, String type,
                                    String status, boolean onRent, LocationResponse location) {
}
