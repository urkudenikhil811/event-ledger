package com.eventledger.event_gateway.controller;

import com.eventledger.event_gateway.exception.AccountServiceUnavailableException;
import com.eventledger.event_gateway.model.Event;
import com.eventledger.event_gateway.model.TransactionType;
import com.eventledger.event_gateway.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    private Event sampleEvent() {
        return new Event("evt-1", "acct-1", TransactionType.CREDIT,
                new BigDecimal("100.00"), "USD", Instant.parse("2026-05-15T09:00:00Z"), null);
    }

    private static final String VALID_BODY = """
            {"eventId":"evt-1","accountId":"acct-1","type":"CREDIT","amount":100.00,"currency":"USD","eventTimestamp":"2026-05-15T09:00:00Z"}
            """;

    @Test
    void postNewEvent_returns201() throws Exception {
        given(eventService.recordEvent(any()))
                .willReturn(new EventService.RecordResult(sampleEvent(), true));

        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value("evt-1"));
    }

    @Test
    void postDuplicateEvent_returns200() throws Exception {
        given(eventService.recordEvent(any()))
                .willReturn(new EventService.RecordResult(sampleEvent(), false));

        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void postInvalidEvent_returns400() throws Exception {
        String badCurrency = """
                {"eventId":"evt-1","accountId":"acct-1","type":"CREDIT","amount":100.00,"currency":"US","eventTimestamp":"2026-05-15T09:00:00Z"}
                """;
        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(badCurrency))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postWhenAccountUnavailable_returns503() throws Exception {
        given(eventService.recordEvent(any()))
                .willThrow(new AccountServiceUnavailableException("down", null));

        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void getById_found_returns200() throws Exception {
        given(eventService.getEvent("evt-1")).willReturn(Optional.of(sampleEvent()));

        mockMvc.perform(get("/events/evt-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("evt-1"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        given(eventService.getEvent("nope")).willReturn(Optional.empty());

        mockMvc.perform(get("/events/nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByAccount_returns200List() throws Exception {
        given(eventService.getEventsForAccount("acct-1")).willReturn(List.of(sampleEvent()));

        mockMvc.perform(get("/events?account=acct-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventId").value("evt-1"));
    }
}