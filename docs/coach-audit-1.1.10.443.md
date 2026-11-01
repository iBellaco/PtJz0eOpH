# Coach 1.1.10.443 — build 1159

## Causa y corrección

La ejecución 41333689741 compiló el código: 220 pruebas ejecutadas, dos fallidas en
PersonalBackupRoundTripTest. Ambos mostraron `Illegal connection pointer 1`
desde ShadowLegacySQLiteConnection al ejecutar clearAllDrafts, antes de restaurar.
AppDatabase.INSTANCE sobrevivía entre métodos mientras Robolectric reiniciaba las
conexiones SQLite. La clase pasó de un método a tres en la versión anterior.

PersonalBackupRoundTripTest ahora crea una base Room en memoria por método,
la conecta al mismo singleton que utiliza BackupRestoreManager y la cierra y
desvincula en @After. SQLite nativo permite probar las transacciones, triggers
y el acceso desde las corrutinas con el motor real dentro de Robolectric.
No se omiten pruebas ni se cambian las aserciones de rechazo, rollback o roundtrip.
No se modifica la implementación de restauración ni se toca almacenamiento real.

Archivos: PersonalBackupRoundTripTest.kt, app/build.gradle.kts y este informe.
Las restantes recomendaciones siguen en docs/recommendations-tracker.md.

## Verificación

- Revisados los registros del fallo y la ubicación anterior a la restauración.
- Verificaciones locales: diff sin errores, recursos completos, 10 pruebas del
  plan de validación y 15 pruebas de reutilización del APK correctas.
- Ejecución Android remota pendiente; el entorno local no dispone de Gradle/SDK
  funcionales (descarga de Gradle rechazada en la intervención anterior).
