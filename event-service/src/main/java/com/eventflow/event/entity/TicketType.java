package com.eventflow.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketType {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    public TicketType(
            Event event,
            String name,
            BigDecimal price,
            String currency,
            int capacity,
            Instant now
    ) {
        this.id = UUID.randomUUID();
        this.event = event;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.capacity = capacity;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(
            String name,
            BigDecimal price,
            String currency,
            int capacity,
            Instant now
    ) {
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.capacity = capacity;
        this.updatedAt = now;
    }
}
