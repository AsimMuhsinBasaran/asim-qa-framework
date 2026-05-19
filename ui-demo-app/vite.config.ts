import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { defineConfig, type Plugin } from 'vite';
import react from '@vitejs/plugin-react';

// https://vite.dev/config/
const runtimeContextRoute = '/test-data/runtime/poc-runtime-bridge/cypress-runtime-context-poc-REQ-POC-001.json';
const configDir = path.dirname(fileURLToPath(import.meta.url));
const runtimeContextPath = path.resolve(configDir, '..', 'test-data', 'runtime', 'poc-runtime-bridge', 'cypress-runtime-context-poc-REQ-POC-001.json');

function runtimeContextMiddleware(): Plugin {
  return {
    name: 'runtime-context-middleware',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        if (req.url !== runtimeContextRoute) {
          next();
          return;
        }

        if (!fs.existsSync(runtimeContextPath)) {
          res.statusCode = 404;
          res.setHeader('Content-Type', 'application/json');
          res.end(JSON.stringify({ available: false, path: runtimeContextPath }));
          return;
        }

        try {
          const body = fs.readFileSync(runtimeContextPath, 'utf8');
          res.statusCode = 200;
          res.setHeader('Content-Type', 'application/json');
          res.setHeader('Cache-Control', 'no-store');
          res.end(body);
        } catch {
          res.statusCode = 500;
          res.setHeader('Content-Type', 'application/json');
          res.end(JSON.stringify({ available: false, path: runtimeContextPath }));
        }
      });
    }
  };
}

export default defineConfig({
  plugins: [react(), runtimeContextMiddleware()],
})
