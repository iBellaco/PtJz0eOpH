# Auditoría de Coach 1.1.10.620

- `versionCode`: 1336
- `versionName`: 1.1.10.620

## Correcciones incluidas

- El draft deja de mostrar recomendaciones genéricas cuando el usuario ya eligió su campeón; solo conserva una recomendación que coincida exactamente con esa selección.
- El selector de campeón abre con la línea de la casilla tocada ya filtrada y marcada.
- Los controles para quitar campeones pasan a tener un objetivo táctil de 48 dp y una señal visual más clara.
- El acceso entra tras validar la cuenta sin esperar tareas de mantenimiento ni el cierre remoto de una sesión anterior. La nueva sesión inicia su reemplazo antes de abrir el perfil y conserva el cierre de la sesión desplazada.
- El diagnóstico del Auto-Scan diferencia baneos confirmados de campeones únicos para reflejar correctamente un baneo repetido entre ambos equipos.
- En drafts tempranos se posterga el análisis auxiliar costoso y se limita la recuperación pesada de títulos, preservando las lecturas rápidas de línea y selecciones.

## Verificación

- `:app:testDebugUnitTest --tests com.example.DraftChampionSelectionTest --tests com.example.CoachRoutineRegressionTest`: correcto.
- El ensamblado local de depuración y de entrega alcanzó la compilación del código, pero no pudo concluir porque el entorno no tenía acceso de red para recuperar dependencias de Android faltantes. El flujo remoto conserva la validación completa con su red y firma de entrega.
