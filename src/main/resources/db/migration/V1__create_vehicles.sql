CREATE TABLE vehicles (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_number  VARCHAR(20)    NOT NULL UNIQUE,
    type                 VARCHAR(20)    NOT NULL,
    brand                VARCHAR(50)    NOT NULL,
    model                VARCHAR(50)    NOT NULL,
    manufacture_year     INT,
    seats                INT,
    fuel_type            VARCHAR(20),
    transmission         VARCHAR(20),
    price_per_day        DECIMAL(10, 2) NOT NULL,
    status               VARCHAR(20)    NOT NULL DEFAULT 'AVAILABLE',
    image_url            VARCHAR(500),
    city                 VARCHAR(60),
    description          VARCHAR(1000),
    created_at           DATETIME       NOT NULL,
    INDEX idx_vehicles_status_type (status, type)
);

CREATE TABLE vehicle_locations (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id   BIGINT   NOT NULL,
    latitude     DOUBLE   NOT NULL,
    longitude    DOUBLE   NOT NULL,
    speed_kmph   DOUBLE,
    recorded_at  DATETIME NOT NULL,
    INDEX idx_locations_vehicle_time (vehicle_id, recorded_at),
    CONSTRAINT fk_locations_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id)
);
