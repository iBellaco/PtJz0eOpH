# Auditoría de Coach 1.1.10.431

## Cambio incluido

Se formalizó la regla de entrega posterior a cada validación: el resultado debe
ser un único texto copiable para testers con las correcciones, eliminaciones,
ajustes y demás acciones necesarias de la aplicación. La salida debe excluir
referencias internas y cualquier dato sensible.

## Verificaciones

- `git diff --check`
- Revisión de consistencia de `AGENTS.md` con las reglas de entrega existentes.
