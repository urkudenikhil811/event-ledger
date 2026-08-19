Feature: Health checks

  @smoke
  Scenario: Account Service is up
    Given url accountUrl + '/health'
    When method get
    Then status 200
    And match response.status == 'UP'

  @smoke
  Scenario: Event Gateway is up
    Given url gatewayUrl + '/health'
    When method get
    Then status 200
    And match response.status == 'UP'