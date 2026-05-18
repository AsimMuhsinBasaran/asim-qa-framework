# Runtime Bridge Data

This directory is for generated data that is produced during test execution and consumed by other layers.

Typical contents may include:
- created entity IDs
- correlation IDs
- scenario metadata
- environment metadata

Generated files in this directory should not be committed.
Secrets, raw tokens, passwords, and authorization headers should not be stored here unless there is a strong and documented reason.

The first implementation phase expects scenario-based JSON artifacts such as:

```text
test-data/runtime/<runId>/<scenarioSlug>-<requestId>.json
```

This directory is a contract boundary, not a general context dump. Only explicit and approved export keys should be written.
