package com.eventledger.event_gateway.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private Instant eventTimestamp;

    @Convert(converter = MetadataConverter.class)
    @Column(length = 4000)
    private Map<String, Object> metadata;

    protected Event() {
        // for JPA
    }

    public Event(String eventId, String accountId, TransactionType type, BigDecimal amount,
                 String currency, Instant eventTimestamp, Map<String, Object> metadata) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.eventTimestamp = eventTimestamp;
        this.metadata = metadata;
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getAccountId() { return accountId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getEventTimestamp() { return eventTimestamp; }
    public Map<String, Object> getMetadata() { return metadata; }
}