Feature: Duplicate events are not double-counted

  @regression
  Scenario: The same eventId submitted twice applies only once

    * def accountId = 'acct-' + java.util.UUID.randomUUID()
    * def eventId = 'evt-' + java.util.UUID.randomUUID()
    * def payload =
      """
      {
        "eventId": "#(eventId)",
        "accountId": "#(accountId)",
        "type": "CREDIT",
        "amount": 100.00,
        "currency": "USD",
        "eventTimestamp": "2026-05-15T14:02:11Z",
        "metadata": { "source": "karate-test" }
      }
      """

    Given url gatewayUrl + '/events'
    And request payload
    When method post
    Then status 201

    Given url gatewayUrl + '/events'
    And request payload
    When method post
    Then status 200

    Given url accountUrl + '/accounts/' + accountId + '/balance'
    When method get
    Then status 200
    And match response.balance == 100.00