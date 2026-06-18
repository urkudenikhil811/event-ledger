package com.eventledger.event_gateway.controller;

import com.eventledger.event_gateway.dto.EventView;
import com.eventledger.event_gateway.dto.SubmitEventRequest;
import com.eventledger.event_gateway.model.Event;
import com.eventledger.event_gateway.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventView> submitEvent(@Valid @RequestBody SubmitEventRequest request) {
        Event event = new Event(
                request.eventId(),
                request.accountId(),
                request.type(),
                request.amount(),
                request.currency(),
                request.eventTimestamp(),
                request.metadata()
        );
        EventService.RecordResult result = eventService.recordEvent(event);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(EventView.from(result.event()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventView> getEvent(@PathVariable String id) {
        Optional<Event> event = eventService.getEvent(id);
        if (event.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(EventView.from(event.get()));
    }

    @GetMapping
    public ResponseEntity<List<EventView>> getEventsByAccount(@RequestParam String account) {
        List<EventView> events = eventService.getEventsForAccount(account).stream()
                .map(EventView::from)
                .toList();
        return ResponseEntity.ok(events);
    }
}