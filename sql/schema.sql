CREATE DATABASE IF NOT EXISTS pensionat;

USE pensionat;

CREATE TABLE IF NOT EXISTS rooms (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number    VARCHAR(10)    NOT NULL UNIQUE,
    type           VARCHAR(20)    NOT NULL,
    extra_beds     INT            NOT NULL DEFAULT 0,
    price_per_night DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_extra_beds CHECK (extra_beds >= 0),
    CONSTRAINT chk_price      CHECK (price_per_night > 0)
);

CREATE TABLE IF NOT EXISTS customers (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    email      VARCHAR(255),
    phone      VARCHAR(50),
    address    VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS bookings (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id      BIGINT,
    room_id          BIGINT,
    check_in         DATE,
    check_out        DATE,
    number_of_guests INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_booking_room     FOREIGN KEY (room_id)     REFERENCES rooms (id),
    CONSTRAINT chk_dates           CHECK (check_out > check_in),
    CONSTRAINT chk_guests          CHECK (number_of_guests >= 1)
);
