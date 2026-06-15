# Asim QA Framework

[![Java Maven Tests](https://github.com/asimmuhsinbasaran/asim-qa-framework/actions/workflows/java-maven-tests.yml/badge.svg?branch=main)](https://github.com/asimmuhsinbasaran/asim-qa-framework/actions/workflows/java-maven-tests.yml) [![Live Allure Report](https://img.shields.io/badge/Live%20Allure%20Report-Open%20Report-2ea44f?logo=githubpages&logoColor=white)](https://asimmuhsinbasaran.github.io/asim-qa-framework/)

A Java-based QA automation framework focused on API testing with Cucumber BDD, RestAssured, TestNG, WireMock, and Allure reporting. The current test suite is built around mock-backed API scenarios, dynamic request construction, response validation, polling, retry handling, sensitive data masking, and scenario-aware console logs for parallel execution.

## Tech Stack

- Java 17
- Maven
- Cucumber BDD
- TestNG
- RestAssured
- WireMock
- Allure
- Jackson
- JSON Schema Validator

## Key Capabilities

- API test automation with reusable Cucumber step definitions
- Mock-backed execution through WireMock mappings
- Dynamic request data loaded from JSON files
- Request field updates before execution
- Path parameter and query parameter support
- Response extraction into scenario-scoped variables
- Request body placeholder resolution, for example `{authToken}` or `{userId}`
- Generic HTTP request steps for GET, POST, PUT, DELETE, PATCH
- GET polling for eventually consistent responses
- GET retry handling for transient failures
- JSON field, array, response time, and schema assertions
- Sensitive data masking for tokens, passwords, API keys, secrets, and authorization headers
- Scenario-aware console logging with thread and scenario prefix
- 5-thread parallel mock execution through TestNG DataProvider
- Allure attachments for requests and responses

## Project Structure

```text
.
|-- java-automation/
|   |-- pom.xml
|   `-- src/test/
|       |-- java/com/asim/qa/
|       |   |-- api/                 # RestAssured API client
|       |   |-- config/              # Test configuration reader
|       |   |-- context/             # Scenario-scoped test context
|       |   |-- hooks/               # Cucumber suite, mock server, and logging hooks
|       |   |-- runners/             # TestNG Cucumber runners
|       |   |-- stepdefinitions/     # API and E2E BDD steps
|       |   `-- utils/               # Logging, assertions, masking, polling, JSON helpers
|       `-- resources/
|           |-- features/api/        # Cucumber API feature files
|           |-- schemas/             # JSON schemas
|           `-- test-data/request/   # Request payload templates
|-- mock-server/
|   `-- mappings/                    # WireMock stubs
|-- cypress-ui/                      # Cypress + TypeScript UI workspace
|-- mobile-appium/                   # Mobile automation workspace
|-- performance-jmeter/              # Performance testing workspace
`-- pom.xml                          # Maven parent project
```

## Repository Structure

- `java-automation/`: current Java API automation framework
- `cypress-ui/`: bootstrapped Cypress + TypeScript UI workspace
- `mock-server/`: WireMock mappings and mock backend stubs
- `test-data/`: shared contracts and runtime bridge data
- `docker/`: future local infrastructure and container support files
- `docs/`: architecture notes, conventions, and framework documentation

The repository is intentionally organized as a unified QA ecosystem rather than a set of disconnected test projects.

The Java Selenium UI package currently contains only skeleton classes; the active Java automation layer is the API framework.

## How to Run Tests

Compile test sources without executing tests:

```bash
mvn -q -DskipTests test-compile
```

Run the Java API automation suite:

```bash
mvn test
```

The default Maven test run executes `ApiTestRunner`, which targets the mock suite. WireMock is started and stopped by Cucumber hooks during the run.
Execution is controlled by Maven Surefire and the Cucumber TestNG runner; there is no source `testng.xml` suite file.

### Run Matrix

- `env` selects the active environment configuration.
- `cucumber.filter.tags` selects the Cucumber scenario scope.
- `runner.class` selects which runner class Surefire includes.
- `dataproviderthreadcount` controls TestNG Cucumber scenario parallelism.
- The default behavior is the mock E2E API suite: `@mock and @e2e`.
- `@api` marks the mock-backed API feature set.
- `@dev` is reserved for the dev smoke scenario and should be run with `env=dev`.
- Runner classes do not own tag selection; pass tags with `cucumber.filter.tags`.
- Java UI runners are planned only; no `UiTestRunner` or `E2ETestRunner` source class exists yet.

```bash
mvn test
mvn test -Denv=mock
mvn test -Denv=mock -Dcucumber.filter.tags="@mock and @e2e"
mvn test -pl java-automation -Dcucumber.filter.tags="@api"
mvn test -pl java-automation -Denv=dev -Dcucumber.filter.tags="@dev"
mvn test -pl java-automation -Denv=dev -Drunner.class=DevApiTestRunner -Dcucumber.filter.tags="@dev"
mvn test -pl java-automation -Ddataproviderthreadcount=1
mvn test -Drunner.class=ApiTestRunner
```

Retry and polling defaults can be overridden with Maven system properties when needed:

```bash
mvn test -pl java-automation -Dapi.retry.maxAttempts=3 -Dapi.retry.delayMs=1000
mvn test -pl java-automation -Dapi.polling.maxAttempts=3 -Dapi.polling.delayMs=1000
```

## Example Gherkin Scenarios

```gherkin
@mock @e2e
Scenario: User logs in successfully
  Given base url is configured
  When user loads json file "auth/login-user.json"
  And user sends "POST" request to "/auth/login" with loaded body
  Then response status code should be 200
  And response field "token" should not be null
```

```gherkin
@mock @e2e
Scenario: Full E2E - Register, login and place order
  Given base url is configured
  When user loads json file "auth/register-user.json"
  And user sends "POST" request to "/auth/register" with loaded body
  Then response status code should be 201
  And user saves response field "token" as "authToken"
  And user saves response field "id" as "userId"
  When user loads json file "order/create-order.json"
  And user adds header "Authorization" as "Bearer {authToken}"
  And user updates request field "userId" as "{userId}"
  And user sends "POST" request to "/orders" with loaded body
  Then response status code should be 201
```

```gherkin
@mock @e2e
Scenario: Poll last GET request successfully
  Given base url is configured
  And user sends "GET" request to "/posts/1"
  And user polls last request until response status code should be 200 within 1 seconds every 1 second
  And user polls last request until response field "id" should be "1" within 1 seconds every 1 second
  And user polls last request until response field "title" should not be null within 1 seconds every 1 second
```

## Reporting with Allure

Allure Cucumber integration is enabled in the TestNG runner. Test execution writes result files under:

```text
java-automation/target/allure-results
```

Request and response payloads are attached through framework utilities, with sensitive values masked before they are written to the report.

If Allure CLI is installed locally, generate or serve a report with:

```bash
allure serve java-automation/target/allure-results
```

Cucumber HTML and JSON reports are also generated under:

```text
java-automation/target/cucumber-report.html
java-automation/target/cucumber.json
```

## Parallel Execution

`ApiTestRunner` uses a TestNG DataProvider with parallel execution enabled. Maven Surefire is configured with a data provider thread count of 5 by default, so mock scenarios can run concurrently in five TestNG worker threads. Override it with `-Ddataproviderthreadcount=1` when investigating shared-state or environment issues.

`DevApiTestRunner` is an optional non-parallel dev smoke runner. Use it together with `-Dcucumber.filter.tags="@dev"` because Maven Surefire passes tag selection through the `cucumber.filter.tags` system property.

Parallel mock execution depends on scenario-scoped Cucumber objects, per-scenario API client state, and a single WireMock server on the configured mock port. Runtime exports and any future shared external systems should be treated as parallel-sensitive.

Console logs include both thread and scenario context:

```text
[thread=TestNG-PoolService-1][scenario=Poll last GET request successfully] Status Code : 200
[thread=TestNG-PoolService-2][scenario=User registers successfully] Status Code : 201
```

This keeps interleaved parallel output readable without adding a larger logging framework.

## Security / Masking

Sensitive values are masked in console logs, assertion output, request/response logs, and Allure attachments. The masking utility handles common secret fields and authorization patterns, including:

- `password`, `passwd`, `pwd`
- `token`, `access_token`, `refresh_token`, `id_token`
- `apiKey`, `api_key`
- `clientSecret`, `client_secret`
- `secret`
- `Authorization: Bearer ...`

Example:

```text
Authorization=Bearer ***MASKED***
"token": "***MASKED***"
```

## Roadmap

- Add more focused unit tests for utility classes such as masking, placeholder resolution, and polling
- Expand schema validation coverage for mock API domains
- Add clearer documentation for custom step definitions
- Introduce optional environment profiles for local, mock, and remote API execution
- Add CI workflow examples for test execution and report artifact publishing
