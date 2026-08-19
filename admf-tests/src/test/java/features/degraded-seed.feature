@degraded-seed
Feature: Seed data for the degraded-mode check

  Scenario: Create one event while the system is healthy

    Given url gatewayUrl + '/events'
    And request
      """
      {
        "eventId": "evt-degraded-probe",
        "accountId": "acct-degraded-probe",
        "type": "CREDIT",
        "amount": 75.00,
        "currency": "USD",
        "eventTimestamp": "2026-05-15T14:02:11Z",
        "metadata": { "source": "karate-degraded" }
      }
      """
    When method post
    Then status 201