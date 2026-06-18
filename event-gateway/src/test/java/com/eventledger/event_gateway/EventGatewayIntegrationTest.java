package com.eventledger.event_gateway;

import com.eventledger.event_gateway.client.AccountServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventGatewayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountServiceClient accountServiceClient;

    @Test
    void fullFlow_storesNewEvent_andIsIdempotentOnDuplicate() throws Exception {
        String event = """
                {"eventId":"evt-int-1","accountId":"acct-int","type":"CREDIT","amount":100.00,"currency":"USD","eventTimestamp":"2026-05-15T09:00:00Z"}
                """;

        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(event))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value("evt-int-1"));

        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(event))
                .andExpect(status().isOk());

        mockMvc.perform(get("/events/evt-int-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("evt-int-1"));

        verify(accountServiceClient, times(1)).applyTransaction(any());
    }
}