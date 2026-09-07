USE pensionat;

INSERT INTO rooms (room_number, type, extra_beds, price_per_night) VALUES
    ('101', 'SINGLE', 0,  800.00),
    ('102', 'SINGLE', 0,  800.00),
    ('103', 'SINGLE', 0,  850.00),
    ('201', 'DOUBLE', 0, 1200.00),
    ('202', 'DOUBLE', 1, 1400.00),
    ('203', 'DOUBLE', 2, 1600.00),
    ('301', 'DOUBLE', 0, 1500.00),
    ('302', 'DOUBLE', 2, 1800.00);

INSERT INTO customers (first_name, last_name, email, phone, address) VALUES
    ('Anna',    'Lindqvist',  'anna.lindqvist@gmail.com',    '070-123 45 67', 'Storgatan 12, Stockholm'),
    ('Erik',    'Johansson',  'erik.johansson@hotmail.com',  '073-234 56 78', 'Kungsgatan 4, Göteborg'),
    ('Maria',   'Svensson',   'maria.svensson@outlook.com',  '076-345 67 89', 'Drottninggatan 8, Malmö'),
    ('Lars',    'Petersson',  'lars.petersson@gmail.com',    '070-456 78 90', 'Vasagatan 22, Uppsala'),
    ('Karin',   'Nilsson',    'karin.nilsson@yahoo.se',      '072-567 89 01', 'Järnvägsgatan 5, Linköping'),
    ('Mikael',  'Bergström',  'mikael.bergstrom@gmail.com',  '073-678 90 12', 'Parkvägen 3, Örebro');

INSERT INTO bookings (customer_id, room_id, check_in, check_out, number_of_guests) VALUES
    (1, 1, '2026-05-01', '2026-05-03', 1),
    (2, 4, '2026-05-05', '2026-05-08', 2),
    (3, 6, '2026-05-10', '2026-05-14', 4),
    (4, 3, '2026-05-15', '2026-05-16', 1),

    (5, 7, '2026-05-20', '2026-05-24', 2),
    (6, 8, '2026-05-22', '2026-05-25', 3),

    (1, 2, '2026-05-28', '2026-05-30', 1),
    (2, 5, '2026-06-01', '2026-06-05', 3),
    (3, 7, '2026-06-10', '2026-06-12', 2),
    (4, 4, '2026-06-15', '2026-06-20', 2);
