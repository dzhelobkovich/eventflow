CREATE TABLE bookings
(
    id          UUID        NOT NULL,
    customer_id UUID        NOT NULL,
    event_id    UUID        NOT NULL,
    status      VARCHAR(30) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_bookings PRIMARY KEY (id),

    CONSTRAINT chk_bookings_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED'))
);

CREATE INDEX idx_bookings_customer_id
    ON bookings (customer_id);

CREATE INDEX idx_bookings_event_id
    ON bookings (event_id);

CREATE INDEX idx_bookings_status_expires_at
    ON bookings (status, expires_at);