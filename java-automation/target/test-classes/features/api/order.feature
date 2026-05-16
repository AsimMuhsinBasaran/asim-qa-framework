Feature: Order API - E2E Flows

  @mock @e2e
  Scenario: User places an order successfully
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    When user loads json file "order/create-order.json"
    And user sends "POST" request to "/orders" with loaded body
    Then response status code should be 201
    And response field "orderId" should not be null
    And response field "status" should be "PENDING"
    And response field "total" should not be null
    And response array "items" size should be 2
    And response time should be less than 2000 ms
    And response should match json schema "schemas/create-order-schema.json"

  @mock @e2e
  Scenario: User views their order
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    And user sends "GET" request to "/orders/501"
    Then response status code should be 200
    And response field "orderId" should be "501"
    And response field "status" should not be null
    And response field "userId" should not be null
    And response array "items" size should be 2

  @mock @e2e
  Scenario: Order fails when product is out of stock
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    When user loads json file "order/create-order-out-of-stock.json"
    And user sends "POST" request to "/orders" with loaded body
    Then response status code should be 422
    And response field "error" should be "Product is out of stock"
    And response field "code" should be "UNPROCESSABLE_ENTITY"

  @mock @e2e
  Scenario: Order fails when cart is empty
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    When user loads json file "order/create-order-empty-cart.json"
    And user sends "POST" request to "/orders" with loaded body
    Then response status code should be 400
    And response field "error" should be "Order must contain at least one item"
    And response field "code" should be "VALIDATION_ERROR"

  @mock @e2e
  Scenario: Cancel an existing order
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    When user loads json file "order/cancel-order.json"
    And user sends "PUT" request to "/orders/501/cancel" with loaded body
    Then response status code should be 200
    And response field "orderId" should be "501"
    And response field "status" should be "CANCELLED"

  @mock @e2e
  Scenario: Cannot cancel an already delivered order
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    And user sends "PUT" request to "/orders/502/cancel" with loaded body
    Then response status code should be 409
    And response field "error" should be "Cannot cancel a delivered order"
    And response field "code" should be "CONFLICT"

  @mock @e2e
  Scenario: Get all orders for user
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    And user sends "GET" request to "/orders"
    Then response status code should be 200
    And response array "orders" size should be 2
    And response field "orders[0].orderId" should not be null
    And response field "orders[0].status" should not be null
    And response array "orders.status" should contain "PENDING"

  @mock @e2e
  Scenario: Full E2E - Register, login and place order
    Given base url is configured
    # Step 1: Register
    When user loads json file "auth/register-user.json"
    And user updates request field "email" as "e2e-test@example.com"
    And user sends "POST" request to "/auth/register" with loaded body
    Then response status code should be 201
    And user saves response field "token" as "authToken"
    And user saves response field "id" as "userId"
    # Step 2: Place order with saved token
    Given base url is configured
    And user adds header "Authorization" as "Bearer {authToken}"
    When user loads json file "order/create-order.json"
    And user updates request field "userId" as "{userId}"
    And user sends "POST" request to "/orders" with loaded body
    Then response status code should be 201
    And response field "status" should be "PENDING"
    And response field "orderId" should not be null
