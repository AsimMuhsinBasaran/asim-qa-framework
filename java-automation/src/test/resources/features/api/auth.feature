@api
Feature: Authentication API

  @mock @e2e
  Scenario: User registers successfully
    Given base url is configured
    When user loads json file "auth/register-user.json"
    And user sends "POST" request to "/auth/register" with loaded body
    Then response status code should be 201
    And response field "id" should not be null
    And response field "email" should not be null
    And response field "token" should not be null

  @mock @e2e
  Scenario: User logs in successfully
    Given base url is configured
    When user loads json file "auth/login-user.json"
    And user sends "POST" request to "/auth/login" with loaded body
    Then response status code should be 200
    And response field "token" should not be null
    And response field "userId" should not be null
    And response field "email" should not be null

  @mock @e2e
  Scenario: Login fails with wrong password
    Given base url is configured
    When user loads json file "auth/login-wrong-password.json"
    And user sends "POST" request to "/auth/login" with loaded body
    Then response status code should be 401
    And response field "error" should be "Invalid credentials"
    And response field "code" should be "UNAUTHORIZED"

  @mock @e2e
  Scenario: Login fails with non-existent email
    Given base url is configured
    When user loads json file "auth/login-wrong-password.json"
    And user updates request field "email" as "notfound@example.com"
    And user sends "POST" request to "/auth/login" with loaded body
    Then response status code should be 401
    And response field "error" should be "Invalid credentials"

  @mock @e2e
  Scenario: Register fails when email already exists
    Given base url is configured
    When user loads json file "auth/register-user.json"
    And user updates request field "email" as "existing@example.com"
    And user sends "POST" request to "/auth/register" with loaded body
    Then response status code should be 409
    And response field "error" should be "Email already in use"
    And response field "code" should be "CONFLICT"

  @mock @e2e
  Scenario: Register fails when required fields are missing
    Given base url is configured
    When user loads json file "auth/register-missing-fields.json"
    And user sends "POST" request to "/auth/register" with loaded body
    Then response status code should be 400
    And response field "code" should be "VALIDATION_ERROR"
    And response field "error" should not be null

  @mock @e2e
  Scenario: Get current user profile with valid token
    Given base url is configured
    And user uses valid mock bearer auth
    And user sends "GET" request to "/auth/me"
    Then response status code should be 200
    And response field "id" should not be null
    And response field "email" should not be null
    And response field "role" should not be null

  @mock @e2e
  Scenario: Get profile fails with missing token
    Given base url is configured
    And user sends "GET" request to "/auth/me"
    Then response status code should be 401
    And response field "error" should be "Authorization token is required"
    And response field "code" should be "UNAUTHORIZED"
