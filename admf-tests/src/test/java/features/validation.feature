Feature: Test validation

  @regression
  Scenario: validate a submitted event

    * def accountId = 'acct-' + java.util.UUID.randomUUID()
    * def eventId = 'evt-' + java.util.UUID.randomUUID()

    Given url gatewayUrl + '/events'
    And request
      """
      {
        "eventId": "#(eventId)",
        "accountId": "#(accountId)",
        "type": "CREDIT",
        "amount": -50.00,
        "currency": "USD",
        "eventTimestamp": "2026-05-15T14:02:11Z",
        "metadata": { "source": "karate-test" }
      }
      """
    When method post
    Then status 400
