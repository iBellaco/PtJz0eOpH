# Auditoría Coach 1.1.10.258

- Build: 974.
- Baneos: vuelve a comprobar los retratos considerados vacíos antes de confirmar «Sin ban». La captura reportada contiene Zeri en el primer baneo rival; se añade prueba con esa cabecera. Mantiene el umbral y la separación entre candidatos.
- Calibración: añade Baneo aliado y Baneo rival como tercera y cuarta opciones, independientes de los diez retratos verticales. Los controles mueven la fila superior elegida en X/Y y ajustan un diámetro común. Guarda la geometría y respeta las posiciones personalizadas existentes. Los valores predeterminados de slots reflejan las coordenadas actualizadas del Red Magic.
- Selecciones: exige dos lecturas consecutivas de título por slot, sin mezclar evidencias entre jugadores. Reinicia la memoria únicamente al iniciar un draft vacío; pausar, reanudar o abrir el visor conserva las selecciones y los baneos existentes. Los campeones reconocidos no se descartan por carecer de línea: se completan las líneas libres respetando todas las observadas.
- Décima selección: conserva el reconocimiento visual, la sustitución exclusiva del último retrato y el cierre del escáner al desaparecer los slots. El lado de primera selección es el opuesto al de la décima selección confirmada.
- Pruebas: controles de baneos renderizados, persistencia, lectura estable en diez slots, cabecera Zeri y prueba instalada con bandas/retratos de la captura sin nombres de jugadores. Se verifica ausencia de selección al mostrar CALLE CENTRAL, conservación de Senna/Malphite y décimo rival por imagen sin nombre.
- Entrega: requiere comprobaciones de Actions y pruebas instaladas ES/PT aprobadas, APK release ofuscado con firma persistente y ZIP del proyecto sin archivos privados. No se afirma una prueba física en todos los teléfonos.
