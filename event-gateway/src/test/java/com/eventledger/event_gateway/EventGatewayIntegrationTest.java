package com.eventledger.event_gateway;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    @Test
    void traceId_isGeneratedAndReturnedInResponseHeader() throws Exception {
        String event = """
            {"eventId":"evt-trace-test","accountId":"acct-trace","type":"CREDIT","amount":10.00,"currency":"USD","eventTimestamp":"2026-05-15T09:00:00Z"}
            """;

        // no X-Trace-Id sent -> Gateway should generate one and return it
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Trace-Id"));
    }

    @Test
    void traceId_isEchoedBackWhenProvided() throws Exception {
        String event = """
            {"eventId":"evt-trace-test-2","accountId":"acct-trace","type":"CREDIT","amount":10.00,"currency":"USD","eventTimestamp":"2026-05-15T09:00:00Z"}
            """;

        // client sends a trace ID -> Gateway must echo it back
        mockMvc.perform(post("/events")
                        .header("X-Trace-Id", "my-trace-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(event))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Trace-Id", "my-trace-123"));
    }
}