CREATE TABLE booking_items
(
    id             UUID          NOT NULL,
    booking_id     UUID          NOT NULL,
    ticket_type_id UUID          NOT NULL,
    quantity       INTEGER       NOT NULL,
    unit_price     NUMERIC(12,2) NOT NULL,
    currency       VARCHAR(3)    NOT NULL,

    CONSTRAINT pk_booking_items
        PRIMARY KEY (id),

    CONSTRAINT fk_booking_items_booking
        FOREIGN KEY (booking_id)
            REFERENCES bookings (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_booking_items_ticket_inventory
        FOREIGN KEY (ticket_type_id)
            REFERENCES ticket_inventory (ticket_type_id),

    CONSTRAINT uq_booking_items_booking_ticket_type
        UNIQUE (booking_id, ticket_type_id),

    CONSTRAINT chk_booking_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_booking_items_unit_price
        CHECK (unit_price >= 0)
);

CREATE INDEX idx_booking_items_booking_id
    ON booking_items (booking_id);

CREATE INDEX idx_booking_items_ticket_type_id
    ON booking_items (ticket_type_id);