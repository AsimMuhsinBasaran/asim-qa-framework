describe('Runtime bridge smoke', () => {
  it('hydrates the UI from the runtime artifact produced by Java', () => {
    const appUrl = Cypress.env('UI_BASE_URL') || 'http://127.0.0.1:5173';

    cy.visit(appUrl);

    cy.get('[data-testid="runtime-state"]').should('have.text', 'Runtime context loaded.');
    cy.get('[data-testid="order-id"]').should('have.text', '501');
    cy.get('[data-testid="user-id"]').should('have.text', '1001');
    cy.get('[data-testid="order-status"]').should('have.text', 'CREATED');
  });
});
