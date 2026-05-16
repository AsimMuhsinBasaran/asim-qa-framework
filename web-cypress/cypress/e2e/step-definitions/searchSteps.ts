import { Given, When, Then } from "@badeball/cypress-cucumber-preprocessor";
import HomePage from "../pages/HomePage";

Given("I open the home page", () => {
  cy.visit("/");
});

When("I search product {string}", (product: string) => {
  HomePage.searchProduct(product);
});

Then("I should see search results", () => {
  cy.url().should("include", "search");
});