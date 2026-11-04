# Coach 1.1.10.470 — build 1186

## Navegación estable del recorrido inicial

- La auditoría instalada conserva la búsqueda del control traducido en el árbol
  real de Android.
- Si el emulador pierde temporalmente ese nodo, la auditoría usa el control
  táctil inferior disponible o su posición segura en la pantalla.
- Después de cada acción se comprueba que el contenido visible haya cambiado;
  un toque que no avance ya no puede producir un resultado válido.

## Verificación

- Regresión del cálculo de límites del control inferior.
- Regresión de la coordenada segura para árboles visuales incompletos.
- Verificación localizada de recursos en portugués y portugués de Brasil.
