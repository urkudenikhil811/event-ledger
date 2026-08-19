@degraded-verify
Feature: Gateway behavior when Account Service is unavailable

  Scenario: Writes fail with 503 but reads still succeed

    # writes are rejected
    Given url gatewayUrl + '/events'
    And request
      """
      {
        "eventId": "evt-during-outage",
        "accountId": "acct-degraded-probe",
        "type": "DEBIT",
        "amount": 25.00,
        "currency": "USD",
        "eventTimestamp": "2026-05-15T15:00:00Z",
        "metadata": {}
      }
      """
    When method post
    Then status 503

    # reads still work
    Given url gatewayUrl + '/events'
    And param account = 'acct-degraded-probe'
    When method get
    Then status 200
    And match response == '#[1]'