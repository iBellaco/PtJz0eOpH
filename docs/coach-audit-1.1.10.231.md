# Coach 1.1.10.231 — código 947

## Cambios

- `.github/scripts/cleanup-completed-runs.cjs`, su prueba y los cuatro flujos: se conservan hasta 30 ejecuciones en total, reservando 15 entregas y 15 servicios, y 30 releases, contando el borrador privado que conserva la firma de actualización. Se retiran las publicaciones antiguas de revisión temprana.
- `.github/scripts/package-delivery.py` y `build-apk.yml`: cada entrega final publica solo el APK ofuscado y el ZIP del proyecto; se retiran el ZIP duplicado del APK y `SHA256SUMS.txt`.
- `UserDetailManagementDialog.kt`, `AuthScreen.kt`, `UserInboxDialog.kt`, `EconomyPendingStatus.kt`, `EconomyServiceClient.kt` y `translations_pt.json`: el aviso de solicitud pendiente aparece en la bandeja del titular, deja de ocupar el panel de perfiles y explica que el servicio la procesa automáticamente en español y portugués.
- `app/build.gradle.kts`, `AGENTS.md` y `README.md`: versión y preferencias de publicación actualizadas.

## Verificación

- Prueba de retención de ejecuciones y releases con Node.
- Revisión de diferencias y formato de Git.
- Compilación, firma, ofuscación e instalación: comprobaciones de Actions requeridas antes de la entrega.

## Resumen para testers

Coach 1.1.10.231: comprobar que una solicitud pendiente se ve en la bandeja de entrada, que no aparece un botón de solicitud al gestionar otro perfil y que la solicitud se procesa automáticamente. Verificar que la entrega tiene solo el APK ofuscado y el ZIP del proyecto.
