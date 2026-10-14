# Auditoría Coach 1.1.10.276 — Wild Rift Parche 7.3b

Fuente oficial revisada: https://wildrift.leagueoflegends.com/en-us/news/game-updates/wild-rift-patch-notes-7-3b/

## Versión

| Área | Antes | Después |
|---|---:|---:|
| `versionCode` | 991 | 992 |
| `versionName` | 1.1.10.275 | 1.1.10.276 |
| Parche visible en la app | Parche 7.3a | Parche 7.3b |

## Objetos actualizados

| Objeto | Antes | Después | Impacto en recomendaciones |
|---|---|---|---|
| Filo fantasmal de Youmuu | 3000 de oro | 2900 de oro | Mejora el primer pico de asesinos AD y junglas físicos que rotan temprano. |
| Medallón de los Solari de Hierro | 2600 de oro, +200 vida | 2500 de oro, +300 vida | Más prioridad en soportes y tanques contra ráfaga de área. |
| Malla de espinas | 2700 de oro | 2850 de oro | Sigue como anti-curación para frontliners, pero se retrasa si la curación rival todavía no decide peleas. |
| Armadura de Warmog | Umbral interno: 950 vida adicional | 1250 vida adicional | Se limita a tanques que ya acumulan mucha vida; se evita recomendar temprano en builds que no alcanzan el umbral. |
| Puñal de Statikk | 3000 de oro, ataques +5 acumulaciones de vigor | 2900 de oro, ataques +7 acumulaciones de vigor | Se agregó como opción situacional para tiradores físicos que necesitan limpiar oleadas y rotar antes. |
| Flechas de los Yun Tal | 3100 de oro | 3000 de oro | Mantiene prioridad para tiradores de ataques continuos y crítico. |

## Runas actualizadas

| Runa | Antes | Después | Impacto en recomendaciones |
|---|---|---|---|
| Electrocutar | 40-210 de daño | 60-210 de daño | Sube el valor de intercambios cortos y asesinos de ráfaga en fase temprana. |
| Golpe Bajo | 7 s de enfriamiento | 5 s de enfriamiento | Más valor en campeones con ralentización o control frecuente. |
| Garras del Inmortal | 3,3% vida máxima y +10 vida permanente | 3% vida máxima y +8 vida permanente | Baja su peso en tanques de línea si no pueden activar intercambios seguros. |
| Aery | Escudo 25-120 | Escudo 20-100 | Se reduce el valor defensivo puro en encantadores; se conserva por hostigamiento y protección mixta. |

## Hechizos

Las notas oficiales 7.3b no listan cambios de hechizos de invocador. Se mantuvieron los valores actuales y solo se sincronizó el contexto de parche de las recomendaciones.

## Campeones con meta ajustado

| Campeón | Antes | Después | Motivo aplicado en Coach |
|---|---:|---:|---|
| Volibear | Tier B | Tier A | Buff a Mordisco Frenético: más daño y mejor curación potenciada. |
| Jhin | Tier B | Tier A | Buff a crítico estándar y ejecución por vida faltante. |
| Tryndamere | Tier S+ | Tier S+ | Buff de crítico por furia y daño de Giro Cuchilla; se actualizó consejo sin subir más el tier. |
| Yasuo | Tier S+ | Tier S+ | Buff de crítico y escudo; se actualizó consejo sin subir más el tier. |
| Hwei | Tier S | Tier A+ | Nerf a daño, escudos, alcance y hitbox de Fauces Trituradoras. |
| Nocturne | Tier C | Tier C | Nerf a vida por nivel, daño temprano de H1 y enfriamiento de Paranoia; se actualizó consejo. |
| Yunara | Tier S | Tier A | Nerf a daño de pasiva/activa y enfriamiento de definitiva. |
| Senna | Tier B | Tier C | Nerf a crítico, daño por alma y daño base de H1. |

## Archivos principales tocados

- `app/src/main/java/com/example/data/WildRiftItemsData.kt`
- `app/src/main/java/com/example/data/WildRiftSpellsAndRunes.kt`
- `app/src/main/assets/runes.json`
- `app/src/main/res/raw/champions_part1.json`
- `app/src/main/res/raw/champions_part2.json`
- `app/src/main/assets/champions_creator_builds.json`
- `app/src/main/assets/translations_pt.json`
- `tools/build_coaching.py`
- `tools/update-champion-builds.py`
- `app/src/main/java/com/example/data/WildRiftRepository.kt`
- `app/build.gradle.kts`
