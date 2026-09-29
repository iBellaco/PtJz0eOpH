<p align="center">
  <img src="https://raw.githubusercontent.com/github/explore/main/topics/android/android.png" alt="Android" width="60"/>
  <h1 align="center">Wild Rift Drafting Coach & Overlay</h1>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Scraper-5%20Global%20Sources-C89B3C?style=for-the-badge&logo=leagueoflegends&logoColor=white" alt="Global Meta Scraper" />
  <img src="https://img.shields.io/badge/Android-14%2F15%20V2%2FV3%20Signed-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="V2/V3 Signature" />
</p>

---

## ⚡ Características Principales (100% Real)

### 1. 🔍 Escaneo y Lectura de Draft por OCR en Tiempo Real
- **Detección Automática de Campeones:** Utiliza **Google ML Kit Text Recognition** sobre la captura de pantalla (`MediaProjection API`) para escanear en vivo la sala de selección de campeones (selecciones del 1 al 9, aliados y enemigos).
- **Reconocimiento de Invocadores con Espacios:** Sistema OCR adaptado para procesar nombres con espacios (ej. *"D I E G O"*) y filtrar ruido de chat de forma efectiva.
- **Asignación Dinámica de Roles:** Seguimiento en tiempo real de carriles (Top, Jungle, Mid, ADC, Support) por si el usuario cambia de rol o realiza intercambios con un aliado.

### 2. Estadísticas del servidor chino
- Consulta de victorias, selecciones y bloqueos desde el servidor chino.
- Los fallos de conexión conservan los últimos datos guardados y muestran el estado real de la consulta.
- El décimo pick se reconoce por coincidencia espacial de su avatar con los retratos locales, sin API de IA ni lectura del nombre oculto.

### 3. 🪟 Superposición Flotante en Pantalla (Overlay HUD)
- **Burbuja Flotante Minimalista:** Permite ejecutar la aplicación directamente sobre el juego Wild Rift sin salir de la partida mediante el permiso `SYSTEM_ALERT_WINDOW`.
- **HUD Multifuncional:** Pestañas integradas de Draft en vivo, Tier List sincronizada e historial de partidas directamente en la ventana flotante.

### 4. 📚 Catálogo Oficial de Campeones, Objetos y Runas
- **141 Campeones:** Estadísticas detalladas, sinergias, counter-picks, habilidades (H1, H2, H3, H4/Ulti), rutas de build y consejos de Coach por línea.
- **Roles Flexibles:** Configuración de roles primarios y secundarios por campeón adaptados al meta de cada servidor (ej. Nilah en Jungla y Flex ADC).
- **Objetos y Runas:** Base de datos completa con costos, pasivas y árboles de runas actualizados.

### 5. 🧠 Recomendaciones Tácticas (Coach de Élite)
Análisis estructurado bajo estándares profesionales de esports (MOBA / Wild Rift):
1. **Lectura del Draft y Condición de Victoria (Win Condition).**
2. **Picks recomendados y Sinergias de equipo.**
3. **Configuración Óptima de Runas y Hechizos.**
4. **Ruta de Objetos e Itemización Dinámica Situacional.**
5. **Plan de Juego Macro y Objetivos (Fase de líneas, teamfights y micro-tips).**

### 6. 🔐 Seguridad, Firma V2/V3 y Autenticación
- **Firma APK Moderna:** Configuración explícita de esquemas de firma **V2 y V3** para garantizar compatibilidad total e instalaciones limpias sin errores de paquete en **Android 14 y Android 15**.
- **Autenticación Firebase & Perfiles:** Gestión de inicio de sesión, límite de dispositivos por cuenta estándar y perfiles multicuenta.
- **Respaldo Local/Nube:** Importación y exportación de historiales de draft en formato JSON con fusión inteligente.

---

## 🏗️ Estructura del Proyecto

```
├── app/src/main/java/com/example/
│   ├── data/                   # Repositorios, scrapper de 5 fuentes, ChineseMetaSyncService
│   │   ├── backup/             # BackupRestoreManager
│   │   └── local/              # Room Database (Drafts y Tier Lists)
│   ├── model/                  # Modelos de dominio (Champion, LaneRole, etc.)
│   ├── service/                # Foreground Services (FloatingAssistantService y DraftVisionScanner)
│   ├── ui/                     # Jetpack Compose + Material 3 (Screens, Dialogs, Admin Panel)
│   └── util/                   # Utilidades de OCR, SummonerSpellDetector y AuthManager
```

---

## 🛠️ Compilación y Desarrollo Local

```bash
# 1. Clonar el repositorio
git clone https://github.com/barbadiego695/wild-rift-coach.git
cd wild-rift-coach

# 2. Compilar el APK de depuración (firmado V2/V3)
./gradlew assembleDebug
```

---

## 📜 Licencia y Aviso Legal
Herramienta de asistencia comunitaria para jugadores de **League of Legends: Wild Rift**. *League of Legends: Wild Rift y Riot Games* son marcas comerciales de Riot Games, Inc. Este software no está afiliado oficialmente con Riot Games.
