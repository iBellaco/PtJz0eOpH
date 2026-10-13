# Coach draft build audit — 1.1.10.269

## Scope

This patch keeps Blind Pick recommendations disabled for a completely empty
draft, while enabling them as soon as a champion or draft slot exists. The
contextual build continues to use only the local item, rune, and spell
catalogs. Its title, reasons, and catalog names follow the active language.

## Validation

- Generated component and champion catalogs checked.
- Kotlin source formatting checked with `git diff --check`.
- Android validation is executed by the repository workflow.
- The prior workflow exposed an empty-draft visibility regression; this patch
  restores that empty state while retaining the Blind Pick path.
