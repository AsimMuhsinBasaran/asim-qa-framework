describe('Demo shop', () => {
  it('filters products, adds an item to cart, and updates the summary panel', () => {
    const appUrl = Cypress.env('DEMO_BASE_URL') || 'http://127.0.0.1:5173';

    cy.visit(appUrl);

    cy.get('[data-testid="page-title"]').should('have.text', 'Unified QA demo shop');
    cy.get('[data-testid="demo-shop"]').should('be.visible');
    cy.get('[data-testid="demo-search-input"]').clear().type('keyboard');
    cy.get('[data-testid="demo-search-input"]').should('have.value', 'keyboard');

    cy.get('[data-testid="product-grid"] [data-testid^="product-card-"]').should('have.length', 1);
    cy.get('[data-testid="product-card-keyboard-pro"]').should('be.visible');
    cy.get('[data-testid="product-card-keyboard-pro"]').should('contain.text', 'Keyboard Pro Mechanical');
    cy.get('[data-testid="product-card-keyboard-pro"]').should('contain.text', '$89.00');

    cy.get('[data-testid="add-to-cart-keyboard-pro"]').click();

    cy.get('[data-testid="cart-count"]').should('have.text', '1');
    cy.get('[data-testid="cart-item-total"]').should('have.text', '1 item');
    cy.get('[data-testid="cart-summary"]').within(() => {
      cy.get('[data-testid="cart-line-keyboard-pro"]').should('contain.text', 'Keyboard Pro Mechanical');
      cy.get('[data-testid="cart-line-keyboard-pro"]').should('contain.text', 'Qty 1');
      cy.get('[data-testid="cart-line-keyboard-pro"]').should('contain.text', '$89.00');
    });
  });
});
