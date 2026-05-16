import HomePage from './pages/HomePage'

describe('Demo Web Shop Search Test', () => {

    it('should search for laptop', () => {

        cy.visit('/')

        HomePage.searchProduct('laptop')

        cy.url()
            .should('include', 'search')
    })

})