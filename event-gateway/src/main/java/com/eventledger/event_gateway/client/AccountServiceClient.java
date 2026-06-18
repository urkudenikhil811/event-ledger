package com.eventledger.event_gateway.client;

import com.eventledger.event_gateway.exception.AccountServiceUnavailableException;
import com.eventledger.event_gateway.model.Event;
import com.eventledger.event_gateway.model.TransactionType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class AccountServiceClient {

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

        try {
            restClient.post()
                    .uri("/accounts/{accountId}/transactions", event.getAccountId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new AccountServiceUnavailableException(
                    "Could not reach Account Service to apply transaction", e);
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