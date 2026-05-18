describe("Cypress bootstrap smoke", () => {
  it("starts Cypress successfully", () => {
    expect(true).to.equal(true);
  });

  it("reads runtime context safely when file is missing", () => {
    cy.task("readRuntimeContext").then((context) => {
      expect(context).to.be.an("object");
    });
  });
});