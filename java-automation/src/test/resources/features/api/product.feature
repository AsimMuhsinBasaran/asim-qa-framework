Feature: Product API

  @mock @e2e
  Scenario: Get all products successfully
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    And user sends "GET" request to "/products"
    Then response status code should be 200
    And response array "products" size should be 3
    And response field "products[0].id" should not be null
    And response field "products[0].name" should not be null
    And response field "products[0].price" should not be null

  @mock @e2e
  Scenario: Get single product by id
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    And user sends "GET" request to "/products/1"
    Then response status code should be 200
    And response field "id" should be "1"
    And response field "name" should not be null
    And response field "stock" should not be null
    And response field "price" should not be null

  @mock @e2e
  Scenario: Get product fails when not found
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    And user sends "GET" request to "/products/999"
    Then response status code should be 404
    And response field "error" should be "Product not found"
    And response field "code" should be "NOT_FOUND"

  @mock @e2e
  Scenario: Create product successfully
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    When user loads json file "product/create-product.json"
    And user sends "POST" request to "/products" with loaded body
    Then response status code should be 201
    And response field "id" should not be null
    And response field "name" should not be null
    And response field "price" should not be null
    And response field "stock" should not be null

  @mock @e2e
  Scenario: Create product fails when price is missing
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    When user loads json file "product/create-product.json"
    And user updates request field "price" as ""
    And user sends "POST" request to "/products" with loaded body
    Then response status code should be 400
    And response field "error" should not be null
    And response field "code" should be "VALIDATION_ERROR"

  @mock @e2e
  Scenario: Update product stock successfully
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    When user loads json file "product/update-stock.json"
    And user sends "PUT" request to "/products/1/stock" with loaded body
    Then response status code should be 200
    And response field "id" should be "1"
    And response field "stock" should not be null

  @mock @e2e
  Scenario: Delete product successfully
    Given base url is configured
    And user adds header "Authorization" as "Bearer valid-test-token"
    And user sends "DELETE" request to "/products/1"
    Then response status code should be 204

  @mock @e2e
  Scenario: Get products without token returns unauthorized
    Given base url is configured
    And user sends "GET" request to "/products"
    Then response status code should be 401
    And response field "error" should be "Authorization token is required"
    And response field "code" should be "UNAUTHORIZED"
