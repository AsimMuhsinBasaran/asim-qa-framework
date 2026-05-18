import fs from 'node:fs';
import path from 'node:path';

export interface RuntimeContext {
  runId: string;
  env: string;
  scenarioName: string;
  requestId: string;
  exportedAt: string;
  exports: Record<string, string>;
  meta: {
    layer: string;
    source: string;
  };
}

export interface RuntimeContextResult {
  available: boolean;
  path: string;
  warnings: string[];
  context: RuntimeContext;
}

export interface ReadRuntimeContextOptions {
  contextFilePath?: string;
  cwd?: string;
}

const EMPTY_CONTEXT: RuntimeContext = {
  runId: '',
  env: '',
  scenarioName: '',
  requestId: '',
  exportedAt: '',
  exports: {},
  meta: {
    layer: '',
    source: ''
  }
};

export function resolveRuntimeContextPath(options: ReadRuntimeContextOptions = {}): string {
  const rawPath = options.contextFilePath?.trim() || process.env.RUNTIME_CONTEXT_FILE?.trim() || '../test-data/runtime/context.json';
  const baseDir = options.cwd ?? process.cwd();

  return path.isAbsolute(rawPath) ? rawPath : path.resolve(baseDir, rawPath);
}

export function readRuntimeContext(options: ReadRuntimeContextOptions = {}): RuntimeContextResult {
  const resolvedPath = resolveRuntimeContextPath(options);
  const warnings: string[] = [];

  if (!fs.existsSync(resolvedPath)) {
    warnings.push(`Runtime context file not found: ${resolvedPath}`);
    return {
      available: false,
      path: resolvedPath,
      warnings,
      context: EMPTY_CONTEXT
    };
  }

  try {
    const raw = fs.readFileSync(resolvedPath, 'utf8');
    const parsed = JSON.parse(raw) as Partial<RuntimeContext>;

    return {
      available: true,
      path: resolvedPath,
      warnings,
      context: {
        runId: parsed.runId ?? '',
        env: parsed.env ?? '',
        scenarioName: parsed.scenarioName ?? '',
        requestId: parsed.requestId ?? '',
        exportedAt: parsed.exportedAt ?? '',
        exports: parsed.exports ?? {},
        meta: {
          layer: parsed.meta?.layer ?? '',
          source: parsed.meta?.source ?? ''
        }
      }
    };
  } catch (error) {
    warnings.push(`Runtime context file could not be read: ${resolvedPath}`);
    warnings.push(error instanceof Error ? error.message : String(error));

    return {
      available: false,
      path: resolvedPath,
      warnings,
      context: EMPTY_CONTEXT
    };
  }
}
