package com.cargorent.vehicle.service;

import com.cargorent.vehicle.dto.RecommendationResponse;
import com.cargorent.vehicle.entity.Vehicle;
import com.cargorent.vehicle.entity.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationServiceTest {

    private static long ids = 1;

    private static Vehicle v(VehicleType type, String brand, String price, String city) {
        Vehicle v = new Vehicle();
        v.setId(ids++);
        v.setType(type);
        v.setBrand(brand);
        v.setModel("M" + v.getId());
        v.setPricePerDay(new BigDecimal(price));
        v.setCity(city);
        v.setRegistrationNumber("KL" + v.getId());
        return v;
    }

    @Test
    void ranksSameTypeBrandAndBudgetHighest() {
        Vehicle pastSuv = v(VehicleType.SUV, "Kia", "2800", "Kochi");
        Vehicle similarSuv = v(VehicleType.SUV, "Kia", "3000", "Kochi");
        Vehicle otherSuv = v(VehicleType.SUV, "Tata", "2500", "Thrissur");
        Vehicle bike = v(VehicleType.BIKE, "Royal Enfield", "900", "Kozhikode");

        List<RecommendationResponse> recs = RecommendationService.rank(List.of(bike, otherSuv, similarSuv),
                List.of(pastSuv), 6);

        assertThat(recs).extracting(r -> r.vehicle().id()).containsExactly(similarSuv.getId(), otherSuv.getId());
        assertThat(recs.get(0).score()).isEqualTo(8); // type 3 + brand 2 + budget 2 + city 1
        assertThat(recs.get(0).reason()).contains("SUVs").contains("Kia");
    }

    @Test
    void newCustomerGetsCheapestOfEachType() {
        Vehicle cheapCar = v(VehicleType.HATCHBACK, "Tata", "1200", "Kochi");
        Vehicle dearCar = v(VehicleType.HATCHBACK, "Maruti", "1500", "Kochi");
        Vehicle suv = v(VehicleType.SUV, "Kia", "2800", "Kochi");

        List<RecommendationResponse> recs = RecommendationService.forNewCustomer(List.of(dearCar, suv, cheapCar));

        assertThat(recs).extracting(r -> r.vehicle().id()).containsExactly(cheapCar.getId(), suv.getId());
    }
}
