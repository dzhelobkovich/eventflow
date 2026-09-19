CREATE TABLE ticket_inventory
(
    ticket_type_id     UUID          NOT NULL,
    event_id           UUID          NOT NULL,
    price              NUMERIC(12,2) NOT NULL,
    currency           VARCHAR(3)    NOT NULL,
    total_quantity     INTEGER       NOT NULL,
    available_quantity INTEGER       NOT NULL,
    reserved_quantity  INTEGER       NOT NULL DEFAULT 0,
    sold_quantity      INTEGER       NOT NULL DEFAULT 0,
    version            BIGINT        NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ticket_inventory
        PRIMARY KEY (ticket_type_id),

    CONSTRAINT chk_ticket_inventory_price
        CHECK (price >= 0),

    CONSTRAINT chk_ticket_inventory_total_quantity
        CHECK (total_quantity > 0),

    CONSTRAINT chk_ticket_inventory_available_quantity
        CHECK (available_quantity >= 0),

    CONSTRAINT chk_ticket_inventory_reserved_quantity
        CHECK (reserved_quantity >= 0),

    CONSTRAINT chk_ticket_inventory_sold_quantity
        CHECK (sold_quantity >= 0),

    CONSTRAINT chk_ticket_inventory_quantity_balance
        CHECK (
            available_quantity
                + reserved_quantity
                + sold_quantity
                = total_quantity
            )
);

CREATE INDEX idx_ticket_inventory_event_id
    ON ticket_inventory (event_id);