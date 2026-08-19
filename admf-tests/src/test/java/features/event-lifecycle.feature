Feature: Event lifecycle across both services

  @smoke
  Scenario: A submitted event updates the account balance

    * def accountId = 'acct-' + java.util.UUID.randomUUID()
    * def eventId = 'evt-' + java.util.UUID.randomUUID()

    Given url gatewayUrl + '/events'
    And request
      """
      {
        "eventId": "#(eventId)",
        "accountId": "#(accountId)",
        "type": "CREDIT",
        "amount": 150.00,
        "currency": "USD",
        "eventTimestamp": "2026-05-15T14:02:11Z",
        "metadata": { "source": "karate-test" }
      }
      """
    When method post
    Then status 201

    Given url accountUrl + '/accounts/' + accountId + '/balance'
    When method get
    Then status 200
    And match response.balance == 150.00
