# Coach 1.1.10.271 audit

## Purpose

This patch keeps the installed Spanish device audit deterministic while retaining the draft build detail changes from 1.1.10.270.

## Change

- The account deletion installed test waits for its Compose content and for the tagged action node before the first click. This prevents a cold-start race where the Spanish test could run before `account_delete_open` was published.

## Verification

- The preceding PR validation passed source integrity, data/security checks, Android compilation, rendered screens, and both installed device audits.
- A new main-branch run must pass the same installed Spanish audit before release publication.
