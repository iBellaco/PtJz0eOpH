# Coach 1.1.10.463 — build 1179

## Versiones de prueba y producción

Se incorpora un formato controlado para compilaciones internas. La versión
base permanece numérica para producción, por ejemplo `1.1.10.463`. Al compilar
con `-PcoachTestIteration=1`, la aplicación muestra
`1.1.10.463-prueba.1`; el número debe ser positivo y permite generar nuevas
iteraciones sin modificar la versión oficial.

Las compilaciones con `-prueba.N` no consultan ni muestran actualizaciones
oficiales. La política reconoce únicamente el formato numérico como producción
y únicamente el formato numérico con `-prueba.N` como prueba.

La comprobación de publicación rechaza un `versionName` que conserve el sufijo
de prueba. El `versionCode` continúa siendo numérico y ascendente para todas
las compilaciones.

## Verificación

- Se añadió `AppVersionPolicyTest` para formatos válidos, inválidos e
  iteraciones positivas.
- Se amplió la validación de versión de producción para bloquear sufijos de
  prueba.
- La compilación local no se ejecutó porque el wrapper necesita descargar una
  distribución de Gradle y la conexión del entorno la rechazó; se dejaron
  preparadas las pruebas para el flujo de validación.
