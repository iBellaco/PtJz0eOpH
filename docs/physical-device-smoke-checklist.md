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
