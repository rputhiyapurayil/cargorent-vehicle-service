package com.cargorent.vehicle.dto;

import java.util.List;

/** Values for the search filter dropdowns. */
public record VehicleMeta(List<String> types, List<String> brands, List<String> cities,
                          List<String> fuelTypes, List<String> transmissions) {
}
