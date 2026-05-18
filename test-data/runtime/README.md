# Runtime Bridge Data

This directory is for generated data that is produced during test execution and consumed by other layers.

Typical contents may include:
- created entity IDs
- correlation IDs
- scenario metadata
- environment metadata

Generated files in this directory should not be committed.
Secrets, raw tokens, passwords, and authorization headers should not be stored here unless there is a strong and documented reason.
