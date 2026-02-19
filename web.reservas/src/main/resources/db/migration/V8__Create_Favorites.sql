CREATE TABLE customer_favorites (
    customer_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id, unit_id),
    CONSTRAINT fk_customer_favorites_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE,
    CONSTRAINT fk_customer_favorites_unit FOREIGN KEY (unit_id) REFERENCES units (id) ON DELETE CASCADE
);