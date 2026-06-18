package com.eventledger.event_gateway.service;

import com.eventledger.event_gateway.client.AccountServiceClient;
import com.eventledger.event_gateway.exception.AccountServiceUnavailableException;
import com.eventledger.event_gateway.model.Event;
import com.eventledger.event_gateway.model.TransactionType;
import com.eventledger.event_gateway.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, accountServiceClient);
    }

    private Event sampleEvent() {
        return new Event("evt-1", "acct-1", TransactionType.CREDIT,
                new BigDecimal("100.00"), "USD", Instant.parse("2026-05-15T09:00:00Z"), null);
    }

    @Test
    void newEvent_appliesToAccountThenSaves() {
        Event event = sampleEvent();
        given(eventRepository.findByEventId("evt-1")).willReturn(Optional.empty());
        given(eventRepository.save(event)).willReturn(event);

        EventService.RecordResult result = eventService.recordEvent(event);

        assertThat(result.created()).isTrue();
        assertThat(result.event()).isEqualTo(event);

        // apply must happen BEFORE save
        InOrder inOrder = inOrder(accountServiceClient, eventRepository);
        inOrder.verify(accountServiceClient).applyTransaction(event);
        inOrder.verify(eventRepository).save(event);
    }

    @Test
    void duplicateEvent_doesNotApplyOrSave() {
        Event existing = sampleEvent();
        given(eventRepository.findByEventId("evt-1")).willReturn(Optional.of(existing));

        EventService.RecordResult result = eventService.recordEvent(existing);

        assertThat(result.created()).isFalse();
        assertThat(result.event()).isEqualTo(existing);
        verify(accountServiceClient, never()).applyTransaction(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void accountCallFails_nothingIsSaved() {
        Event event = sampleEvent();
        given(eventRepository.findByEventId("evt-1")).willReturn(Optional.empty());
        willThrow(new AccountServiceUnavailableException("down", null))
                .given(accountServiceClient).applyTransaction(event);

        assertThatThrownBy(() -> eventService.recordEvent(event))
                .isInstanceOf(AccountServiceUnavailableException.class);

        verify(eventRepository, never()).save(any());
    }
}