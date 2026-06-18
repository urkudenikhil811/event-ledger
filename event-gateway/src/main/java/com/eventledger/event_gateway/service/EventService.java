package com.eventledger.event_gateway.service;

import com.eventledger.event_gateway.client.AccountServiceClient;
import com.eventledger.event_gateway.model.Event;
import com.eventledger.event_gateway.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final AccountServiceClient accountServiceClient;

    public EventService(EventRepository eventRepository, AccountServiceClient accountServiceClient) {
        this.eventRepository = eventRepository;
        this.accountServiceClient = accountServiceClient;
    }

    public RecordResult recordEvent(Event event) {
        Optional<Event> existing = eventRepository.findByEventId(event.getEventId());
        if (existing.isPresent()) {
            return new RecordResult(existing.get(), false);  // duplicate: don't apply again
        }
        accountServiceClient.applyTransaction(event);  // apply to Account first
        Event saved = eventRepository.save(event);      // save only if the apply worked
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