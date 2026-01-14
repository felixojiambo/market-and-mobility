-- V1__init.sql
-- Schema for: users, products, orders, order_items, rides
-- IDs: BIGSERIAL (Long)
-- Money: DECIMAL(19,2)

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'CUSTOMER')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(2000),
    price DECIMAL(19,2) NOT NULL CHECK (price >= 0),
    stock_quantity INT NOT NULL CHECK (stock_quantity >= 0),
    category VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes for product search
CREATE INDEX IF NOT EXISTS idx_products_name ON products (name);
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SHIPPED', 'DELIVERED')),
    total_cost DECIMAL(19,2) NOT NULL CHECK (total_cost >= 0),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users (id)
);

-- Indexes for order search
CREATE INDEX IF NOT EXISTS idx_orders_customer_status ON orders (customer_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price >= 0),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items (order_id);

CREATE TABLE IF NOT EXISTS rides (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    driver_id BIGINT NULL,
    pickup_location VARCHAR(255) NOT NULL,
    dropoff_location VARCHAR(255) NOT NULL,
    fare DECIMAL(19,2) NOT NULL CHECK (fare >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('REQUESTED', 'ACCEPTED', 'COMPLETED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_rides_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_rides_driver FOREIGN KEY (driver_id) REFERENCES users (id)
);

-- Indexes for ride search
CREATE INDEX IF NOT EXISTS idx_rides_user_status ON rides (user_id, status);
CREATE INDEX IF NOT EXISTS idx_rides_driver_status ON rides (driver_id, status);
CREATE INDEX IF NOT EXISTS idx_rides_status ON rides (status);
