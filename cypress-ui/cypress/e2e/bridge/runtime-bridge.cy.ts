describe('Runtime bridge smoke', () => {
  it('hydrates the UI from the runtime artifact produced by Java', () => {
    const appUrl = Cypress.env('UI_BASE_URL') || 'http://127.0.0.1:5173';
    const contextFile = Cypress.env('CYPRESS_CONTEXT_FILE');

    expect(contextFile, 'runtime context file path').to.be.a('string').and.not.be.empty;

    cy.task('readRuntimeContext', contextFile).then((result: any) => {
      expect(result.available, 'runtime context availability').to.eq(true);
      expect(result.context.exports, 'runtime exports').to.exist;
      expect(result.context.exports.orderId, 'exported orderId').to.be.a('string').and.not.be.empty;
      expect(result.context.exports.userId, 'exported userId').to.be.a('string').and.not.be.empty;
      expect(result.context.exports.status, 'exported status').to.be.a('string').and.not.be.empty;

      cy.visit(appUrl);

      cy.get('[data-testid="runtime-state"]').should('have.text', 'Runtime context loaded.');
      cy.get('[data-testid="order-id"]').should('have.text', result.context.exports.orderId);
      cy.get('[data-testid="user-id"]').should('have.text', result.context.exports.userId);
      cy.get('[data-testid="order-status"]').should('have.text', result.context.exports.status);
    });
  });
});
