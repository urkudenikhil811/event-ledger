package com.eventledger.event_gateway.client;

import com.eventledger.event_gateway.exception.AccountServiceUnavailableException;
import com.eventledger.event_gateway.model.Event;
import com.eventledger.event_gateway.model.TransactionType;
import com.eventledger.event_gateway.tracing.TraceIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class AccountServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceClient.class);

    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 200;

    private final RestClient restClient;

    public AccountServiceClient(RestClient accountServiceRestClient) {
        this.restClient = accountServiceRestClient;
    }

    public void applyTransaction(Event event) {
        ApplyTransactionPayload payload = new ApplyTransactionPayload(
                event.getType(),
                event.getAmount(),
                event.getCurrency(),
                event.getEventTimestamp(),
                event.getEventId()
        );

        String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);

        RestClientException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                restClient.post()
                        .uri("/accounts/{accountId}/transactions", event.getAccountId())
                        .header(TraceIdFilter.TRACE_ID_HEADER, traceId != null ? traceId : "unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
                return; // success — stop retrying
            } catch (RestClientException e) {
                lastError = e;
                log.warn("Account Service call failed for event {} (attempt {}/{})",
                        event.getEventId(), attempt, MAX_ATTEMPTS);
                if (attempt < MAX_ATTEMPTS) {
                    sleep(BASE_BACKOFF_MS * attempt);
                }
            }
        }
        throw new AccountServiceUnavailableException(
                "Account Service unavailable after " + MAX_ATTEMPTS + " attempts", lastError);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record ApplyTransactionPayload(
            TransactionType type,
            BigDecimal amount,
            String currency,
            Instant eventTimestamp,
            String sourceEventId
    ) {}
}