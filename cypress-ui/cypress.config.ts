import { defineConfig } from 'cypress';
import { readRuntimeContext, type RuntimeContextResult } from './cypress/utils/runtimeContextReader';

export default defineConfig({
  e2e: {
    specPattern: 'cypress/e2e/**/*.cy.ts',
    supportFile: 'cypress/support/e2e.ts',
    video: false,
    screenshotOnRunFailure: false,
    setupNodeEvents(on, config) {
      on('task', {
        readRuntimeContext(
          overridePath?: string
        ): RuntimeContextResult {
          return readRuntimeContext({
            contextFilePath: overridePath ?? config.env.RUNTIME_CONTEXT_FILE,
            cwd: config.projectRoot
          });
        }
      });

      config.env.RUNTIME_CONTEXT_FILE = config.env.RUNTIME_CONTEXT_FILE || process.env.RUNTIME_CONTEXT_FILE || '';
      return config;
    }
  }
});
