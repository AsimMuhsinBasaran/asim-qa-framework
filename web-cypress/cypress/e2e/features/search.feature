Feature: Demo Web Shop Search

  Scenario: Search laptop product
    Given I open the home page
    When I search product "laptop"
    Then I should see search results