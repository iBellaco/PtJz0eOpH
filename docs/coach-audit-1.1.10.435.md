# Coach 1.1.10.435 — build 1151

Corrige el falso negativo de validate-patch-content.py al añadir referencias legítimas a GamePatch.VERSION para invalidar la caché de categorías. La comprobación valida ahora las dos declaraciones GlobalScrapingSource, sus identidades y su argumento de parche, en vez de contar referencias ajenas por todo el archivo.

Se añade regresión para referencias de caché, una fuente con parche literal, fuentes ausentes y fuentes duplicadas. Se ejecutan las comprobaciones previas completas de fix. Todos los cambios funcionales de 1.1.10.434 permanecen incluidos y pendientes de su validación Android.

La versión previamente validada 1.1.10.433 tiene la solicitud #179 para integración manual. No se ha fusionado main ni se han incluido aquí las correcciones pendientes como si ya estuvieran validadas.
