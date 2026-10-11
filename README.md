# cargorent-vehicle-service

Vehicle catalogue (add / edit / remove), search with filters and date availability, simulated GPS tracking and rule-based recommendations.

Part of **CarGoRent** – see [cargorent-infra](../cargorent-infra/README.md) for the architecture, how to run
the whole system, demo accounts and the Postman collection.

| | |
|---|---|
| Port | 8082 |
| Schema | `cargorent_vehicles` (tables `vehicles`, `vehicle_locations`) |
| Swagger | http://localhost:8082/swagger-ui.html |
| Calls | booking-service: booked vehicle ids, a user's bookings |

## Endpoints

| Method | Path | Who |
|---|---|---|
| GET | `/vehicles?type=&brand=&city=&minPrice=&maxPrice=&from=&to=&sort=` | public |
| GET | `/vehicles/{id}`, `/vehicles/meta`, `/vehicles/{id}/availability` | public |
| GET | `/vehicles/recommendations` | customer |
| GET | `/vehicles/{id}/location`, `/vehicles/{id}/locations` | admin, or customer with that vehicle on rent |
| GET | `/vehicles/locations/latest` | admin (fleet map) |
| POST/PUT/DELETE | `/vehicles`, `/vehicles/{id}`; PATCH `/vehicles/{id}/status`; POST `/vehicles/{id}/location` | admin |

**GPS simulator** (`GpsSimulator`): every 5 s each vehicle that is picked up moves a short distance and a new fix is saved.
**Recommendation** (`RecommendationService`): scores available vehicles by the customer's most-rented type (+3), brand (+2), price within 25% of their average (+2, or +1 within 50%) and city (+1). New customers get the cheapest vehicle of each type.

## Run

```bash
# needs MySQL: cd ../cargorent-infra && docker compose -f docker-compose.mysql.yml up -d
mvn spring-boot:run
```

## Package layout

`com.cargorent.<service>` → `controller` · `service` · `repository` · `entity` · `dto` · `client` (calls to other services) · `common` (errors, caller identity) · `config`
