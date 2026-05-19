# ui-demo-app

Minimal Vite + React app used as the UI target for the runtime bridge smoke flow.

## Purpose

- hydrates from the Java-generated runtime context artifact
- renders `orderId`, `userId`, and `status`
- preserves stable `data-testid` selectors for automated validation

## Local run

```bash
npm install
npm run dev
```

The app reads the runtime artifact through the Vite middleware route:

```text
/test-data/runtime/poc-runtime-bridge/cypress-runtime-context-poc-REQ-POC-001.json
```

If the file is missing, the UI shows a controlled missing-state message and still renders safely.
