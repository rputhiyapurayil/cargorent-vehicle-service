INSERT INTO vehicles (registration_number, type, brand, model, manufacture_year, seats, fuel_type, transmission, price_per_day, status, image_url, city, description, created_at) VALUES
('KL07CA1234', 'HATCHBACK', 'Maruti Suzuki', 'Swift',          2023, 5, 'Petrol',   'Manual',    1499.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=800', 'Kochi',              'Economical city hatchback, great mileage.', NOW()),
('KL11BD4521', 'SEDAN',     'Honda',         'City',           2022, 5, 'Petrol',   'Automatic', 2299.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1590362891991-f776e747a588?w=800', 'Kozhikode',          'Comfortable sedan with CVT automatic.', NOW()),
('KL01CF7788', 'SUV',       'Hyundai',       'Creta',          2024, 5, 'Diesel',   'Automatic', 2999.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800', 'Thiruvananthapuram', 'Popular compact SUV with sunroof.', NOW()),
('KL08BX3310', 'MUV',       'Toyota',        'Innova Crysta',  2023, 7, 'Diesel',   'Manual',    3499.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1619767886558-efdc259cde1a?w=800', 'Thrissur',           'Spacious 7-seater, ideal for family trips.', NOW()),
('KL07CB9090', 'SUV',       'Mahindra',      'Thar',           2024, 4, 'Diesel',   'Manual',    3299.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=800', 'Kochi',              '4x4 convertible, perfect for Munnar and Wayanad.', NOW()),
('KL11BF2002', 'SUV',       'Tata',          'Nexon EV',       2024, 5, 'Electric', 'Automatic', 2499.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1593941707882-a5bba14938c7?w=800', 'Kozhikode',          'Electric SUV, ~300 km real-world range.', NOW()),
('KL01CC5566', 'SUV',       'Kia',           'Seltos',         2023, 5, 'Petrol',   'Automatic', 2799.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=800', 'Thiruvananthapuram', 'Feature-rich SUV with ventilated seats.', NOW()),
('KL07CD1212', 'SUV',       'Mahindra',      'XUV700',         2024, 7, 'Diesel',   'Automatic', 3999.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=800', 'Kochi',              'Premium 7-seater with ADAS.', NOW()),
('KL08BY6060', 'SUV',       'Toyota',        'Fortuner',       2023, 7, 'Diesel',   'Automatic', 5499.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=800', 'Thrissur',           'Full-size luxury SUV.', NOW()),
('KL13AZ4400', 'VAN',       'Force',         'Traveller',      2022, 12, 'Diesel',  'Manual',    4999.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800', 'Kannur',             '12-seater van for group tours.', NOW()),
('KL07BZ7007', 'BIKE',      'Royal Enfield', 'Classic 350',    2023, 2, 'Petrol',   'Manual',     899.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=800', 'Kochi',              'Iconic cruiser, helmet included.', NOW()),
('KL11CE3131', 'SCOOTER',   'Honda',         'Activa 6G',      2024, 2, 'Petrol',   'Automatic',  449.00, 'AVAILABLE', NULL, 'Kozhikode',          'Easy city scooter, helmet included.', NOW()),
('KL01CG8181', 'HATCHBACK', 'Tata',          'Tiago',          2023, 5, 'CNG',      'Manual',    1299.00, 'AVAILABLE', 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800', 'Thiruvananthapuram', 'Budget hatchback running on CNG.', NOW()),
('KL08CA9999', 'SEDAN',     'Hyundai',       'Verna',          2024, 5, 'Petrol',   'Automatic', 2499.00, 'MAINTENANCE', 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800', 'Thrissur',         'Turbo sedan. Currently in service.', NOW());

-- Starting GPS fix for every vehicle at its city centre (small offsets so markers do not overlap).
INSERT INTO vehicle_locations (vehicle_id, latitude, longitude, speed_kmph, recorded_at)
SELECT v.id,
       CASE v.city WHEN 'Kochi' THEN 9.9816 WHEN 'Kozhikode' THEN 11.2588 WHEN 'Thiruvananthapuram' THEN 8.5241
                   WHEN 'Thrissur' THEN 10.5276 ELSE 11.8745 END + (v.id % 5) * 0.004,
       CASE v.city WHEN 'Kochi' THEN 76.2999 WHEN 'Kozhikode' THEN 75.7804 WHEN 'Thiruvananthapuram' THEN 76.9366
                   WHEN 'Thrissur' THEN 76.2144 ELSE 75.3704 END + (v.id % 3) * 0.004,
       0, NOW()
FROM vehicles v;
