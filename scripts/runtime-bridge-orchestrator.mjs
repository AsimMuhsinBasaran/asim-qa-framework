import { spawn } from 'node:child_process';
import fs from 'node:fs';
import http from 'node:http';
import net from 'node:net';
import path from 'node:path';
import process from 'node:process';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(scriptDir, '..');
const uiDemoAppDir = path.join(repoRoot, 'ui-demo-app');
const cypressUiDir = path.join(repoRoot, 'cypress-ui');
const runtimeRoutePath = '/test-data/runtime/poc-runtime-bridge/cypress-runtime-context-poc-REQ-POC-001.json';

const FLOW_DESCRIPTORS = {
  poc: {
    mode: 'poc',
    name: 'POC runtime bridge',
    needsVite: false,
    javaTestClass: 'RuntimeContextWriterTest',
    runtimeArtifactPath: path.join(
      repoRoot,
      'test-data',
      'runtime',
      'poc-runtime-bridge',
      'cypress-runtime-context-poc-REQ-POC-001.json'
    ),
    cypressSpec: 'cypress/e2e/bridge/runtime-context.cy.ts',
    uiRuntimeArtifactPath: null
  },
  api: {
    mode: 'api',
    name: 'API runtime bridge',
    needsVite: true,
    javaTestClass: 'ApiRuntimeBridgeExportTest',
    runtimeArtifactPath: path.join(
      repoRoot,
      'test-data',
      'runtime',
      'api-runtime-bridge',
      'api-runtime-bridge-export-from-order-creation-REQ-API-BRIDGE-001.json'
    ),
    cypressSpec: 'cypress/e2e/bridge/runtime-bridge.cy.ts',
    uiRuntimeArtifactPath: path.join(
      repoRoot,
      'test-data',
      'runtime',
      'poc-runtime-bridge',
      'cypress-runtime-context-poc-REQ-POC-001.json'
    )
  }
};

const activeChildren = new Set();
let shutdownInProgress = false;

function getMode() {
  const modeArg = process.argv.find((arg) => arg.startsWith('--mode='));
  const mode = modeArg ? modeArg.split('=')[1] : 'poc';

  if (!Object.prototype.hasOwnProperty.call(FLOW_DESCRIPTORS, mode)) {
    throw new Error(`Unknown runtime bridge mode: ${mode}`);
  }

  return mode;
}

function getFlowDescriptor(mode) {
  return FLOW_DESCRIPTORS[mode];
}

function registerChild(child) {
  activeChildren.add(child);
  child.once('close', () => {
    activeChildren.delete(child);
  });
  return child;
}

function spawnCommand(command, args, options = {}) {
  return registerChild(
    spawn(command, args, {
      cwd: options.cwd ?? repoRoot,
      env: options.env ?? process.env,
      stdio: options.stdio ?? 'inherit',
      shell: false
    })
  );
}

function runCommand(command, args, options = {}) {
  return new Promise((resolve, reject) => {
    const child = spawnCommand(command, args, options);

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

function waitForHttp(url, timeoutMs = 30000, intervalMs = 250, expectedStatusCode = 200) {
  return new Promise((resolve, reject) => {
    const deadline = Date.now() + timeoutMs;

    const poll = () => {
      http
        .get(url, (res) => {
          res.resume();
          if (res.statusCode === expectedStatusCode) {
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

function resolveViteBinary() {
  return process.platform === 'win32'
    ? path.join(uiDemoAppDir, 'node_modules', '.bin', 'vite.cmd')
    : path.join(uiDemoAppDir, 'node_modules', '.bin', 'vite');
}

function stageRuntimeArtifact(flow) {
  if (!flow.uiRuntimeArtifactPath) {
    return;
  }

  const sourcePath = flow.runtimeArtifactPath;
  const targetPath = flow.uiRuntimeArtifactPath;
  const targetDir = path.dirname(targetPath);

  if (!fs.existsSync(sourcePath)) {
    throw new Error(`Runtime artifact was not generated: ${sourcePath}`);
  }

  fs.mkdirSync(targetDir, { recursive: true });
  fs.copyFileSync(sourcePath, targetPath);
}

async function startViteServer(port) {
  const viteBinary = resolveViteBinary();

  if (!fs.existsSync(viteBinary)) {
    throw new Error(`Vite binary not found: ${viteBinary}`);
  }

  const child = spawnCommand(viteBinary, ['--host', '127.0.0.1', '--port', String(port), '--strictPort'], {
    cwd: uiDemoAppDir,
    env: {
      ...process.env,
      PORT: String(port)
    },
    stdio: ['ignore', 'pipe', 'pipe']
  });

  forwardStream(child.stdout, 'vite', process.stdout);
  forwardStream(child.stderr, 'vite', process.stderr);

  child.on('exit', (code, signal) => {
    if (code !== null || signal !== null) {
      process.stdout.write(`[vite] exited with code=${code} signal=${signal}\n`);
    }
  });

  await waitForHttp(`http://127.0.0.1:${port}/`);

  return child;
}

async function stopChild(child) {
  if (!child) {
    return;
  }

  if (!child.killed) {
    child.kill('SIGTERM');
  }

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

async function cleanupAllChildren() {
  const children = Array.from(activeChildren);

  for (const child of children) {
    await stopChild(child);
  }
}

async function shutdown(exitCode) {
  if (shutdownInProgress) {
    return;
  }

  shutdownInProgress = true;

  try {
    await cleanupAllChildren();
  } finally {
    process.exit(exitCode);
  }
}

function installSignalHandlers() {
  process.on('SIGINT', () => {
    void shutdown(130);
  });

  process.on('SIGTERM', () => {
    void shutdown(143);
  });
}

async function runFlow(flow) {
  const javaResult = await runCommand('mvn', ['test', `-Dtest=${flow.javaTestClass}`], {
    cwd: repoRoot,
    stdio: 'inherit'
  });

  if (javaResult.code !== 0) {
    return javaResult.code ?? 1;
  }

  if (!fs.existsSync(flow.runtimeArtifactPath)) {
    throw new Error(`Runtime artifact was not generated: ${flow.runtimeArtifactPath}`);
  }

  stageRuntimeArtifact(flow);

  let viteProcess = null;
  try {
    let uiBaseUrl = 'http://127.0.0.1:5173';

    if (flow.needsVite) {
      const port = await findFreePort();
      viteProcess = await startViteServer(port);
      uiBaseUrl = `http://127.0.0.1:${port}`;
      await waitForHttp(`http://127.0.0.1:${port}${runtimeRoutePath}`);
    }

    const cypressEnv = {
      ...process.env,
      CYPRESS_CONTEXT_FILE: path.relative(cypressUiDir, flow.runtimeArtifactPath)
    };

    if (flow.needsVite) {
      cypressEnv.UI_BASE_URL = uiBaseUrl;
    } else {
      delete cypressEnv.UI_BASE_URL;
    }

    const cypressResult = await runCommand(
      process.platform === 'win32' ? 'npm.cmd' : 'npm',
      [
        'test',
        '--',
        '--browser',
        'electron',
        '--spec',
        flow.cypressSpec
      ],
      {
        cwd: cypressUiDir,
        env: cypressEnv,
        stdio: 'inherit'
      }
    );

    return cypressResult.code ?? 1;
  } finally {
    await stopChild(viteProcess);
  }
}

async function main() {
  installSignalHandlers();
  const mode = getMode();
  const flow = getFlowDescriptor(mode);

  const exitCode = await runFlow(flow);
  await cleanupAllChildren();
  process.exit(exitCode);
}

main().catch((error) => {
  process.stderr.write(`${error instanceof Error ? error.stack ?? error.message : String(error)}\n`);
  void cleanupAllChildren().finally(() => process.exit(1));
});
