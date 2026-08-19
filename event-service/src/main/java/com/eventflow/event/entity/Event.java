package com.eventflow.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 300)
    private String venue;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    public Event(
            String name,
            String description,
            String venue,
            Instant startsAt,
            Instant endsAt,
            Instant now
    ) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = EventStatus.DRAFT;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void updateDetails(
            String name,
            String description,
            String venue,
            Instant startsAt,
            Instant endsAt,
            Instant now
    ) {
        this.name = name;
        this.description = description;
        this.venue = venue;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.updatedAt = now;
    }

    public void publish(Instant now) {
        this.status = EventStatus.PUBLISHED;
        this.updatedAt = now;
    }

    public void cancel(Instant now) {
        this.status = EventStatus.CANCELLED;
        this.updatedAt = now;
    }

    public void complete(Instant now) {
        this.status = EventStatus.COMPLETED;
        this.updatedAt = now;
    }
}