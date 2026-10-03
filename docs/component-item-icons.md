# Iconos locales del catálogo de Coach

Los 52 objetos básicos y de nivel medio usan un único archivo WebP por objeto. Las 98 pertenencias a secciones comparten los mismos archivos; los iconos se incluyen en el APK y no necesitan descargar imágenes durante el uso.

`tools/component-item-icons.json` conserva las URL indicadas, ruta local, dimensiones, tamaño y SHA-256 de cada archivo. Los iconos mantienen la proporción y transparencia originales, con un máximo de 128 píxeles por lado y calidad WebP 85. Ocupan 112.480 bytes en conjunto, frente a 358.516 bytes de las fuentes descargadas.

Para importar de nuevo las fuentes, se necesita Pillow y acceso a las URL:

```sh
python3 tools/import-component-icons.py --download
python3 tools/generate-component-items.py
```

La validación se realiza sin red y sin Pillow:

```sh
python3 tools/import-component-icons.py --check
python3 tools/generate-component-items.py --check
```

Cada sección del catálogo muestra una sola tarjeta. Los grupos se ordenan como completos, nivel medio y básicos/iniciales. Apoyo conserva sus dos objetos iniciales después del nivel medio. Las clasificaciones y pertenencias canónicas del JSON no se cambian.
