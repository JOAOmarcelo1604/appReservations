CREATE TABLE tickets (
    -- Em Postgres, usamos BIGSERIAL para auto-incremento (não AUTO_INCREMENT)
    id BIGSERIAL PRIMARY KEY,

    title VARCHAR(255) NOT NULL,
    description TEXT,

    status VARCHAR(50) DEFAULT 'OPEN',
    priority VARCHAR(50) DEFAULT 'MEDIUM',

    -- Em Postgres, usamos TIMESTAMP em vez de DATETIME
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,

    admin_response TEXT,

    reservation_id BIGINT NOT NULL,

    CONSTRAINT fk_ticket_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);