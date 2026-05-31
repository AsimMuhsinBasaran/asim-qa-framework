Feature: API Tests

  @mock
  Scenario: Get post successfully
    Given base url is configured
    When user sends GET request to "/posts/1"
    Then response status code should be 200
    And response field "id" should be "1"
    And response field "userId" should not be null
    And response field "title" should not be null

  @mock
  Scenario: Create post successfully
    Given base url is configured
    When user sends POST request to "/posts" with body
    """
    {
      "title": "test title",
      "body": "test body",
      "userId": 99
    }
    """
    Then response status code should be 201
    And response field "id" should be "101"
    And response field "userId" should be "99"
    And response field "title" should not be null

  @mock
  Scenario: Create post should fail when title is missing
    Given base url is configured
    When user sends POST request to "/posts" with body
    """
    {
      "body": "test body",
      "userId": 99
    }
    """
    Then response status code should be 400
    And response field "error" should be "title is required"
    And response field "code" should be "VALIDATION_ERROR"

  @mock
  Scenario: Create post with json file
    Given base url is configured
    When user sends POST request to "/posts" with json file "create-post.json"
    Then response status code should be 201
    And response field "userId" should be "99"
    And response field "title" should not be null

  @mock
  Scenario: Create post with dynamic json body
    Given base url is configured
    When user loads json file "create-post.json"
    And user updates request field "title" as "Asim Dynamic Title"
    And user updates request field "userId" as "777"
    And user sends POST request to "/posts" with loaded body
    Then response status code should be 201
    And response field "userId" should be "777"
    And response field "title" should be "Asim Dynamic Title"

  @mock
  Scenario: Create post with nested json field
    Given base url is configured
    And user adds header "client-id" as "mobile"
    And user uses generic mock bearer auth
    When user loads json file "create-post-nested.json"
    And user updates request field "customer.profile.name" as "Asim Basaran"
    And user sends POST request to "/posts" with loaded body
    Then response status code should be 201
    And response field "customerName" should be "Asim Basaran"
    And response field "customerName" should contain "Asim"
    And response time should be less than 1000 ms
    And response array "items" size should be 3
    And response field "items[0].name" should be "first item"
    And response field "items[2].id" should be "3"
    And response field "error" should be null
    And response field "customerName" should exist
    And response field "randomField" should not exist
    And response array "items.name" should contain "second item"
    And response field "items.size()" should be greater than "2"
    And response field "items.size()" should be less than "5"
    And response array "items.id" should contain "2"
    And response should match json schema "schemas/create-post-schema.json"

  @mock
  Scenario: Generic POST request test
    Given base url is configured
    And user adds header "client-id" as "mobile"
    And user uses generic mock bearer auth
    When user loads json file "create-post-nested.json"
    And user updates request field "customer.profile.name" as "Asim Basaran"
    And user sends "POST" request to "/posts" with loaded body
    Then response status code should be 201
    And response field "customerName" should be "Asim Basaran"
    And response field "customerName" should contain "Asim"
    And response array "items.name" should contain "second item"
    And response field "items.size()" should be greater than "2"
    And response should match json schema "schemas/create-post-schema.json"

  @mock
  Scenario: Generic GET request test
    Given base url is configured
    And user sends "GET" request to "/posts/1"
    Then response status code should be 200
    And response field "id" should be "1"
    And response field "title" should not be null

  @mock @e2e
  Scenario: Poll last GET request successfully
    Given base url is configured
    And user sends "GET" request to "/posts/1"
    And user polls last request until response status code should be 200 within 1 seconds every 1 second
    And user polls last request until response field "id" should be "1" within 1 seconds every 1 second
    And user polls last request until response field "title" should not be null within 1 seconds every 1 second
