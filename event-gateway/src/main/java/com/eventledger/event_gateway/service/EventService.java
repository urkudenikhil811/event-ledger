package com.eventledger.event_gateway.service;

import com.eventledger.event_gateway.client.AccountServiceClient;
import com.eventledger.event_gateway.model.Event;
import com.eventledger.event_gateway.repository.EventRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final AccountServiceClient accountServiceClient;
    private final Counter eventsReceived;

    public EventService(EventRepository eventRepository,
                        AccountServiceClient accountServiceClient,
                        MeterRegistry meterRegistry) {
        this.eventRepository = eventRepository;
        this.accountServiceClient = accountServiceClient;
        this.eventsReceived = meterRegistry.counter("events.received");
    }

    public RecordResult recordEvent(Event event) {
        eventsReceived.increment();
        Optional<Event> existing = eventRepository.findByEventId(event.getEventId());
        if (existing.isPresent()) {
            log.info("Duplicate event {} ignored", event.getEventId());
            return new RecordResult(existing.get(), false);
        }
        log.info("Recording new event {} for account {}", event.getEventId(), event.getAccountId());
        accountServiceClient.applyTransaction(event);
        Event saved = eventRepository.save(event);
        return new RecordResult(saved, true);
    }

    public Optional<Event> getEvent(String eventId) {
        return eventRepository.findByEventId(eventId);
    }

    public List<Event> getEventsForAccount(String accountId) {
        return eventRepository.findByAccountIdOrderByEventTimestampAsc(accountId);
    }

    public record RecordResult(Event event, boolean created) {}
}