Cypress.on('uncaught:exception', (err) => {
  return false
})

import './commands'