import { spawn } from 'node:child_process';
import fs from 'node:fs';
import http from 'node:http';
import net from 'node:net';
import path from 'node:path';
import process from 'node:process';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(scriptDir, '..');
const javaAutomationDir = path.join(repoRoot, 'java-automation');
const uiDemoAppDir = path.join(repoRoot, 'ui-demo-app');
const cypressUiDir = path.join(repoRoot, 'cypress-ui');
const runtimeArtifactPath = path.join(
  repoRoot,
  'test-data',
  'runtime',
  'poc-runtime-bridge',
  'cypress-runtime-context-poc-REQ-POC-001.json'
);
const runtimeRoute = '/test-data/runtime/poc-runtime-bridge/cypress-runtime-context-poc-REQ-POC-001.json';

function runCommand(command, args, options = {}) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, {
      cwd: options.cwd ?? repoRoot,
      env: options.env ?? process.env,
      stdio: options.stdio ?? 'inherit',
      shell: false
    });

    child.on('error', reject);
    child.on('close', (code, signal) => {
      resolve({ code, signal });
    });
  });
}

function findFreePort() {
  return new Promise((resolve, reject) => {
    const server = net.createServer();
    server.unref();
    server.on('error', reject);
    server.listen(0, '127.0.0.1', () => {
      const address = server.address();
      const port = typeof address === 'object' && address ? address.port : null;
      server.close((closeError) => {
        if (closeError) {
          reject(closeError);
          return;
        }

        if (!port) {
          reject(new Error('Could not determine a free port for the Vite server.'));
          return;
        }

        resolve(port);
      });
    });
  });
}

function waitForHttp(url, timeoutMs = 30000, intervalMs = 250) {
  return new Promise((resolve, reject) => {
    const deadline = Date.now() + timeoutMs;

    const poll = () => {
      http
        .get(url, (res) => {
          res.resume();
          if (res.statusCode && res.statusCode >= 200 && res.statusCode < 500) {
            resolve();
            return;
          }

          retryOrFail(new Error(`Unexpected HTTP status ${res.statusCode} from ${url}`));
        })
        .on('error', retryOrFail);
    };

    const retryOrFail = (error) => {
      if (Date.now() >= deadline) {
        reject(new Error(`Timed out waiting for ${url}: ${error.message}`));
        return;
      }

      setTimeout(poll, intervalMs);
    };

    poll();
  });
}

function forwardStream(stream, prefix, target) {
  if (!stream) {
    return;
  }

  stream.on('data', (chunk) => {
    target.write(`[${prefix}] ${chunk}`);
  });
}

async function startViteServer(port) {
  const viteBinary = process.platform === 'win32'
    ? path.join(uiDemoAppDir, 'node_modules', '.bin', 'vite.cmd')
    : path.join(uiDemoAppDir, 'node_modules', '.bin', 'vite');

  if (!fs.existsSync(viteBinary)) {
    throw new Error(`Vite binary not found: ${viteBinary}`);
  }

  const child = spawn(viteBinary, ['--host', '127.0.0.1', '--port', String(port), '--strictPort'], {
    cwd: uiDemoAppDir,
    env: {
      ...process.env,
      PORT: String(port)
    },
    stdio: ['ignore', 'pipe', 'pipe'],
    shell: false
  });

  forwardStream(child.stdout, 'vite', process.stdout);
  forwardStream(child.stderr, 'vite', process.stderr);

  child.on('exit', (code, signal) => {
    if (code !== null || signal !== null) {
      process.stdout.write(`[vite] exited with code=${code} signal=${signal}\n`);
    }
  });

  await waitForHttp(`http://127.0.0.1:${port}/`);
  await waitForHttp(`http://127.0.0.1:${port}${runtimeRoute}`);

  return child;
}

async function stopProcess(child) {
  if (!child || child.killed) {
    return;
  }

  child.kill('SIGTERM');

  await new Promise((resolve) => {
    const timeout = setTimeout(() => {
      if (!child.killed) {
        child.kill('SIGKILL');
      }
      resolve();
    }, 5000);

    child.once('exit', () => {
      clearTimeout(timeout);
      resolve();
    });
  });
}

async function main() {
  const javaResult = await runCommand('mvn', ['test', '-Dtest=RuntimeContextWriterTest'], {
    cwd: repoRoot,
    stdio: 'inherit'
  });

  if (javaResult.code !== 0) {
    process.exit(javaResult.code ?? 1);
  }

  if (!fs.existsSync(runtimeArtifactPath)) {
    throw new Error(`Runtime artifact was not generated: ${runtimeArtifactPath}`);
  }

  const port = await findFreePort();
  const viteProcess = await startViteServer(port);
  const uiBaseUrl = `http://127.0.0.1:${port}`;

  try {
    const cypressResult = await runCommand(
      process.platform === 'win32' ? 'npm.cmd' : 'npm',
      [
        'test',
        '--',
        '--browser',
        'electron',
        '--spec',
        'cypress/e2e/runtime-bridge.cy.ts'
      ],
      {
        cwd: cypressUiDir,
        env: {
          ...process.env,
          UI_BASE_URL: uiBaseUrl,
          CYPRESS_CONTEXT_FILE: '../test-data/runtime/poc-runtime-bridge/cypress-runtime-context-poc-REQ-POC-001.json'
        },
        stdio: 'inherit'
      }
    );

    process.exit(cypressResult.code ?? 1);
  } finally {
    await stopProcess(viteProcess);
  }
}

main().catch((error) => {
  process.stderr.write(`${error instanceof Error ? error.stack ?? error.message : String(error)}\n`);
  process.exit(1);
});
