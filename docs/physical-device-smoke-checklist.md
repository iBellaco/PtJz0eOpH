# Auditoría física del escáner

Esta lista complementa los emuladores del workflow. Debe ejecutarse en cada
nueva versión del APK en al menos un dispositivo de cada perfil:

| Perfil | Ejemplo | Qué validar |
|---|---|---|
| Alta densidad / gaming | RedMagic 10 | Baneos, décimo campeón y latencia |
| 1.5K / densidad media | Poco F6 o Xiaomi equivalente | Coordenadas, escalado y calibración |
| 1080p / densidad estándar | Android estándar | Compatibilidad general y controles |

## Procedimiento

1. Instalar el APK de release y abrir el Draft.
2. Confirmar el modelo, resolución, densidad y orientación en el informe del dispositivo.
3. Ejecutar calibración de aliados, rivales y filas de baneos; guardar el perfil.
4. Validar tres capturas de baneos y diez selecciones sin lecturas duplicadas.
5. Cambiar de orientación y repetir una detección.
6. Cerrar y abrir la app; comprobar que el perfil persiste y que la build final se abre después del décimo campeón.
7. Adjuntar capturas y marcar cualquier falso positivo o demora superior a 500 ms.

Los resultados deben guardarse con el formato
`docs/device-audits/<modelo>-<version>-<fecha>.md`. Los emuladores del workflow
siguen siendo la barrera automática; esta auditoría requiere el dispositivo
físico y no se puede simular desde GitHub Actions.

## Registro reproducible

Ejecutar desde un equipo con ADB y el teléfono conectado:

```sh
python3 tools/scanner-device-report.py collect --serial SERIAL_ADB --version 1.1.10.336 --profile redmagic10 --output docs/device-audits/redmagic10-1.1.10.336.json
python3 tools/scanner-device-report.py evaluate docs/device-audits/redmagic10-1.1.10.336.json
```

`collect` solo consulta modelo, Android, tamaño y densidad; no guarda el número
de serie ni lee cuentas. El informe comienza PENDING. Para Poco/1.5K y 1080p
repetir con perfiles y rutas diferentes. Ninguno de los tres perfiles físicos
tiene todavía mediciones adjuntas a este repositorio.

Para cada observación registrar `kind` (`bans` o `pick`), `expected` y `detected`
(listas de IDs, mismo orden de slots), `latencyMs` medida desde el cambio visible
hasta su detección, `evidence` (ruta relativa a captura o log local) y `sha256`
del archivo. Comparar con la grabación y el resultado del visor; no inferir la
latencia de la velocidad del vídeo. Adjuntar al menos tres capturas de baneos
y diez selecciones. No reutilizar una captura para representar cambios distintos.
En cada `pick` indicar `slot` (del 1 al 10) y un único campeón. En cada `bans`
indicar los diez slots. Si varias observaciones usan la misma grabación o log,
añadir `atMs` con el instante exacto y diferente en cada una. El evaluador rechaza
evidencia repetida, slots duplicados y diez picks que no sean campeones distintos.

Confirmar además `checks.orientation`, `checks.profilePersistence` y
`checks.finalBuildRedirect` después de ejecutar el procedimiento. El evaluador
rechaza emuladores, evidencia ausente o alterada, errores de selección y una
demora p95 superior a 500 ms. El resultado PASS es una comprobación del informe;
la validez de las mediciones se revisa contra la evidencia adjunta.
