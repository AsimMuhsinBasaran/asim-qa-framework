describe('Runtime context consumption POC', () => {
  it('reads the runtime context artifact exported by Java', () => {
    const contextFile = Cypress.env('CYPRESS_CONTEXT_FILE');

    expect(contextFile, 'runtime context file path').to.be.a('string').and.not.be.empty;

    cy.task('readRuntimeContext', contextFile).then((result: any) => {
      expect(result.available, 'runtime context availability').to.eq(true);
      expect(result.context.exports.userId).to.eq('1001');
      expect(result.context.exports.email).to.eq('testuser@example.com');
      expect(result.path).to.include('cypress-runtime-context-poc-REQ-POC-001.json');
    });
  });
});
