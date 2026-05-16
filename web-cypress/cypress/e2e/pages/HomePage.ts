class HomePage {

    searchBox = '#small-searchterms'

    searchProduct(product: string) {
        cy.get(this.searchBox)
            .should('be.visible')
            .type(`${product}{enter}`)
    }

}

export default new HomePage()