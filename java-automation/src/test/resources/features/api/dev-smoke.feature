Feature: Dev API Smoke Tests

  @dev
  Scenario: Get public post from dev API
    Given base url is configured
    When user sends "GET" request to "/posts/1"
    Then response status code should be 200
    And response field "id" should be "1"
    And response field "userId" should be "1"
    And response field "title" should not be null
