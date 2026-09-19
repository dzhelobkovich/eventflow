package com.eventflow.booking.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "bookings")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @OneToMany(
            mappedBy = "booking",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<BookingItem> items = new ArrayList<>();

    @Version
    @Column(nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addItem(UUID ticketTypeId, int quantity, BigDecimal unitPrice, String currency) {
        BookingItem item = BookingItem.builder()
                .booking(this)
                .ticketTypeId(ticketTypeId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .currency(currency)
                .build();
        items.add(item);
    }

    public void confirm(Instant now) {
        this.status = BookingStatus.CONFIRMED;
        this.updatedAt = now;
    }

    public void cancel(Instant now) {
        this.status = BookingStatus.CANCELLED;
        this.updatedAt = now;
    }

    public void expire(Instant now) {
        this.status = BookingStatus.EXPIRED;
        this.updatedAt = now;
    }
}
