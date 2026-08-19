@degraded
Feature: Gateway behavior when Account Service is unavailable

  Scenario: Writes fail with 503 but reads still succeed

    * def accountId = 'acct-' + java.util.UUID.randomUUID()
    * def eventId = 'evt-' + java.util.UUID.randomUUID()

    # 1. seed one event while healthy
    Given url gatewayUrl + '/events'
    And request
      """
      {
        "eventId": "#(eventId)",
        "accountId": "#(accountId)",
        "type": "CREDIT",
        "amount": 75.00,
        "currency": "USD",
        "eventTimestamp": "2026-05-15T14:02:11Z",
        "metadata": { "source": "karate-degraded" }
      }
      """
    When method post
    Then status 201

    # 2. take the Account Service down
    * karate.exec('docker stop event-ledger-account-service-1')

    # 3. writes are rejected
    Given url gatewayUrl + '/events'
    And request
      """
      {
        "eventId": "#(eventId)-during-outage",
        "accountId": "#(accountId)",
        "type": "DEBIT",
        "amount": 25.00,
        "currency": "USD",
        "eventTimestamp": "2026-05-15T15:00:00Z",
        "metadata": {}
      }
      """
    When method post
    Then status 503

    # 4. reads still work
    Given url gatewayUrl + '/events'
    And param account = accountId
    When method get
    Then status 200
    And match response == '#[1]'

    # 5. bring it back
    * karate.exec('docker start event-ledger-account-service-1')

    # 6. wait for it to finish booting
    Given url accountUrl + '/health'
    And retry until responseStatus == 200
    When method get
    Then status 200