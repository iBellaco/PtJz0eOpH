# Coach 1.1.10.402 — build 1118

## Captura y causa

El propietario muestra Senna seleccionada y los otros aliados con Apoyo,
Central, Barón y Jungla. La única línea restante es Dragón. También muestra
puestos vacíos desplazados hacia la izquierda antes de aparecer sus hechizos.
La región OCR anterior comenzaba en el borde del retrato seleccionado y podía
cortar las primeras letras de los títulos previos. Sin cuatro líneas leídas,
la regla existente de completar una única línea faltante no podía aplicarse.

## Cambios

- AdaptiveScreenLayoutEngine.calculateAllyLaneRect añade una banda aliada hacia
  la izquierda, escalada con la ventana del juego. Conserva el límite vertical
  anterior al invocador. No agrega perfiles ni coordenadas específicas por marca.
- DraftNameAtlas.createAllyLanes agrupa únicamente los puestos cuya línea falta,
  con texto intacto y alternativa sin insignia. Las máscaras se conservan.
- DraftVisionScanner acepta exclusivamente líneas desde esa banda; los primeros
  nueve campeones siguen procediendo de sus títulos habituales y requieren dos
  confirmaciones. La lectura de líneas sustituye al reintento rápido cuando hace
  falta, manteniendo el máximo de dos solicitudes OCR. Las recuperaciones
  completas periódicas siguen disponibles.
- ScannerDebugOverlay muestra la misma región adicional que consume OCR.
  El círculo del retrato sigue siendo el objetivo para el puesto seleccionado.
- El estado publicado del primer pick ahora puede ser desconocido. No se dibuja
  el décimo aliado por defecto. La regla existente 1 aliado/0 rivales determina
  primero aliado sin anuncio; la inferencia inversa permanece. La décima visión
  sigue exigiendo nueve títulos, aunque ya se conozca su lado.
- AGENTS.md y draft-recognition-contract.md conservan la aclaración del propietario.

## Pruebas

AllyDraftReconcilerTest añade Senna con cuatro líneas ajenas distintas: Dragón,
sin depender de su afinidad de Soporte. Menos de cuatro o duplicados mantienen
la incertidumbre. DraftNameAtlasTest comprueba límites, máscaras y banda solo
aliada a 1280x579, 1920x868 y 2400x1080.

DraftRivalNameInstalledTest añade una secuencia sintética con Senna ya elegida,
las cuatro líneas en posiciones distintas, insignias y nombres de invocador
JINX debajo. Exige Senna en Dragón en el Hub, cero rivales, primero aliado,
décimo rival todavía sin confirmar y máximo dos solicitudes por pasada.
Dimensiones: 1280x579 y 1920x868. Se incluye en el preflight de fix-error.

Estas pruebas de geometría/OCR requieren Actions con SDK/emulador y se informan
como pendientes hasta recibir el resultado. La imagen inline se inspeccionó
visualmente; no hay archivo local disponible de esa nueva captura. La secuencia
dibujada no es su reproducción píxel a píxel ni una prueba física en Red Magic.
Se ejecutan las regresiones Python disponibles y git diff --check antes del envío.
La revisión se publica primero en fix-error; main no se modifica.
