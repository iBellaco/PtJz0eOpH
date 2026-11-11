# Coach 1.1.10.510 — Patrocinios, avisos por idioma y catálogo 7.3f con Sylas

## Correcciones y modificaciones

- Se corrigió la operación atómica de aceptar o rechazar patrocinios. La
  aplicación conserva el identificador exacto de la solicitud económica y ya
  no intenta consultar rutas estimadas que pueden no existir.
- Una decisión administrativa confirmada puede retirar únicamente la solicitud
  original de patrocinio. Las demás operaciones económicas continúan sin poder
  eliminarse desde el cliente.
- La lista de moderación espera a disponer tanto de los anuncios como de sus
  solicitudes pendientes, evitando que una aprobación deje una espera residual
  por una carrera entre listeners.
- Los comunicados globales permiten seleccionar español o portugués y cada
  dispositivo muestra solamente el comunicado correspondiente al idioma
  elegido dentro de Coach. El cambio de idioma en una sesión activa actualiza
  el comunicado sin reiniciar la aplicación.
- El título de los informes de coordenadas identifica si quien los envía es un
  usuario Premium o gratuito, sin añadir correo ni otros datos de cuenta.
- Se incorporó el perfil medido de OPPO CPH2791 para capturas exactas de
  2354 × 1080. Solo se aplica cuando coinciden fabricante, modelo y dimensiones;
  una calibración guardada manualmente conserva prioridad.
- El respaldo del perfil OPPO contiene únicamente geometría y datos técnicos
  del dispositivo, sin el nombre de la persona que aportó la medición.
- El parche de juego activo pasa a `7.3f` según las notas oficiales publicadas
  el 11 de noviembre de 2026. Se actualizaron Eco de Luden, Vara de las
  edades, Perdición del liche, Morellonomicón, Cintomisil hextech, Tormento de
  Liandry, Baluarte de la montaña, Guadaña de la Niebla Negra, Eclipse y
  Chupasangre. Aplastar y Conservación reflejan sus nuevos valores de jungla.
- Las notas 7.3f no modifican runas; se verificó y conservó su catálogo actual.
- Sylas se incorpora a los 143 campeones con imagen local, pasiva y H1–H4
  traducidas, y dos builds originales basadas exclusivamente en objetos,
  runas y hechizos disponibles en el catálogo de Coach. La fuente compartida
  de WR-Meta aún muestra «Coming soon», por lo que no se le atribuye una
  build externa ni estadísticas competitivas no publicadas. Su tier se muestra
  en la sección «Nuevo: sin datos competitivos».

## Archivos principales modificados

- `app/src/main/java/com/example/data/SponsorReviewRepository.kt`
- `firestore.rules`
- `tests/firestore/sponsor-review.test.mjs`
- `app/src/main/java/com/example/data/GlobalAnnouncementManager.kt`
- `app/src/main/java/com/example/ui/components/BroadcastAnnouncementDialog.kt`
- `app/src/main/assets/translations_pt.json`
- `app/src/main/java/com/example/data/CalibrationReport.kt`
- `app/src/main/java/com/example/ui/components/CalibrationControls.kt`
- `app/src/main/java/com/example/service/screen/MeasuredDeviceCalibrationDefaults.kt`
- `docs/scanner-calibration-backup.json`
- `docs/scanner-device-validation.md`
- `app/src/main/java/com/example/data/GamePatch.kt`
- `app/src/main/java/com/example/data/WildRiftItemsData.kt`
- `app/src/main/java/com/example/data/WildRiftSpellsAndRunes.kt`
- catálogos de campeones, habilidades y builds en `app/src/main/res/raw/`,
  `app/src/main/assets/` y `src/data/`.
- perfiles de asesoría bilingüe en `tools/`.
- pruebas unitarias asociadas a patrocinio, comunicados, coordenadas y perfiles
  medidos.

## Versión y verificaciones

- Versión `1.1.10.510`, build `1226`.
- Compilación Android y pruebas unitarias dirigidas superadas.
- Caso de reglas Firestore con anuncio y solicitud pendiente superado: la
  aprobación publica el anuncio y retira la espera en una sola transacción.
- Suite de reglas, economía, eliminación de cuenta y recuperación superada antes
  del caso dirigido de patrocinio; el caso dirigido corregido también quedó en
  verde.
- Respaldo de coordenadas y catálogo de traducciones validados como JSON.
- Recursos español/portugués completos y auditoría de interfaz portuguesa sin
  texto visible en español.
- Validación de versión de producción superada frente al último release.
- Verificación dirigida de 7.3f, Sylas, builds, runas y catálogo completada.
- El catálogo ampliado y los perfiles de asesoría generados pasaron sus pruebas
  dirigidas: 143 campeones, 302 builds y 302 perfiles de línea. Las pruebas
  de reconocimiento del nuevo campeón, visualización de tiers y traducción
  portuguesa también pasaron.
- La ejecución extensa de 848 pruebas completó 842 y seis no pudieron
  iniciarse por artefactos Android de Robolectric ausentes en este entorno.
  Se instalaron los dos artefactos faltantes y se repitieron esas seis clases
  con resultado correcto; no se repitieron las matrices largas ya superadas.
- `:app:assembleRelease` se intentó y quedó bloqueado antes de compilar por
  ausencia de la clave y las variables de firma de producción en este entorno.
  No se generó ni se presentó un APK de depuración como release. La firma y
  la ofuscación deben verificarse en la ejecución autorizada de producción.
