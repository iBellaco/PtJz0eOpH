# -*- coding: utf-8 -*-
"""
Champion builds and runes database for Part 1 (72 Champions).
Coach Soberano High-Performance Meta.
Strict Rules:
- 1 build per line (role)
- 3 Core Items
- 3-4 Situational Items (no overlap with core)
- 1 Tier 2 Boot evolved to Tier 3 Boot
- Situational Boots (Tier 2 alternatives)
- Runes: 1 Keystone + 4 Secondaries (3 of same category + 1 of different category, no duplicates, keystone never in secondaries)
- Only official catalog items and runes.
"""

PART1_BUILDS = {
    "aatrox": {
        "TOP": {
            "core": ["Eclipse", "Fuerza de trinidad", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Rencor de Serylda", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Eclipse", "Cuchilla negra", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Rencor de Serylda", "Ángel de la guarda", "Colmillo de serpiente"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "ahri": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón", "Impulso Cósmico"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "akali": {
        "MID": {
            "core": ["Creagrietas", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Perdición del liche", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas de mercurio"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "TOP": {
            "core": ["Creagrietas", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Perdición del liche", "Tormento de Liandry", "Bastón del Vacío", "Corona de la Reina Fragmentada"],
            "boot": "Botas de mercurio", "upgrade": "Trituradoras encadenadas", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Linaje", "Último Esfuerzo", "Fuerzas Renovadas"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "akshan": {
        "MID": {
            "core": ["Hoja del rey arruinado", "Filo Infinito", "Recaudadora"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Grebas codiciosas", "upgrade": "Botas inmortales", "sit_boots": ["Botas blindadas"],
            "runes": ["Fortalecimiento", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "TOP": {
            "core": ["Hoja del rey arruinado", "Fuerza de trinidad", "Recuerdos de Lord Dominik"],
            "sit": ["Filo Infinito", "Sanguinaria", "Fauces de Malmortius", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Fortalecimiento", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "ADC": {
            "core": ["Hoja del rey arruinado", "Filo Infinito", "Recuerdos de Lord Dominik"],
            "sit": ["Recaudadora", "Sanguinaria", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Fortalecimiento", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "alistar": {
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Irrupción de Fase", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Celeridad"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "ambessa": {
        "TOP": {
            "core": ["Eclipse", "Cuchilla negra", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Rencor de Serylda", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Eclipse", "Fuerza de trinidad", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Rencor de Serylda", "Ángel de la guarda", "Colmillo de serpiente"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "amumu": {
        "JUNGLE": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Coraza dual purpúrea", "Corona abrasadora", "Máscara abisal"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Celeridad"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "annie": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Tormento de Liandry", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Cetro de cristal de Rylai", "Mandato imperial"],
            "sit": ["Morellonomicón", "Sombrero mortal de Rabadon", "Corona de la Reina Fragmentada", "Medallón de los Solari de Hierro"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "ashe": {
        "ADC": {
            "core": ["Hoja del rey arruinado", "Huracán de Runaan", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Mandato imperial", "Cuchilla negra"],
            "sit": ["Recordatorio letal", "Colmillo de serpiente", "Ángel de la guarda", "Cimitarra mercurial"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas blindadas"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Fuente de Vida"],
            "spells": ["Destello", "Curación"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "aurelion_sol": {
        "MID": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas blindadas"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Sobrecrecimiento"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "aurora": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "TOP": {
            "core": ["Creagrietas", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Tormento de Liandry", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de mercurio", "upgrade": "Trituradoras encadenadas", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Fuerzas Renovadas"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "bard": {
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Canción de batalla de Shurelya", "Corazón de hielo"],
            "sit": ["Fuerza de la naturaleza", "Malla de espinas", "Medallón de los Solari de Hierro", "Mandato imperial"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Celeridad"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "blitzcrank": {
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "brand": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Tormento de Liandry", "Cetro de cristal de Rylai"],
            "sit": ["Morellonomicón", "Orbe infinito", "Sombrero mortal de Rabadon", "Bastón del Vacío"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "MID": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Morellonomicón", "Corona de la Reina Fragmentada"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Morellonomicón", "Impulso Cósmico"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cosecha Oscura", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "braum": {
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Guardián", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Extenuación"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "caitlyn": {
        "ADC": {
            "core": ["Filo Infinito", "Recaudadora", "Recuerdos de Lord Dominik"],
            "sit": ["Cañón de Fuego Rápido", "Sanguinaria", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Brutal"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "camille": {
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Rompecascos", "Ángel de la guarda", "Malla de espinas", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Revestimiento de Huesos", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Ángel de la guarda", "Rencor de Serylda", "Fauces de Malmortius", "Malla de espinas"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "cho_gath": {
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Presagio de Randuin", "Armadura de Warmog", "Coraza dual purpúrea"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Tormento de Liandry", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas de mercurio"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "corki": {
        "MID": {
            "core": ["Manamune", "Fuerza de trinidad", "Filo Infinito"],
            "sit": ["Recaudadora", "Recuerdos de Lord Dominik", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "ADC": {
            "core": ["Manamune", "Fuerza de trinidad", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Cañón de Fuego Rápido"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "darius": {
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Coraza del muerto", "Fuerza de la naturaleza", "Malla de espinas", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "diana": {
        "JUNGLE": {
            "core": ["Diente de Nashor", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Tormento de Liandry", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Diente de Nashor", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "dr_mundo": {
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Armadura de Warmog", "Presagio de Randuin", "Hidra titánica"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Coraza del muerto", "Armadura de Warmog", "Hidra titánica"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "draven": {
        "ADC": {
            "core": ["Sanguinaria", "Filo Infinito", "Recaudadora"],
            "sit": ["Recuerdos de Lord Dominik", "Cañón de Fuego Rápido", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas codiciosas", "upgrade": "Botas inmortales", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "ekko": {
        "JUNGLE": {
            "core": ["Diente de Nashor", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Perdición del liche", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Perdición del liche", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "evelynn": {
        "JUNGLE": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Perdición del liche", "Corona de la Reina Fragmentada", "Impulso Cósmico"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "ezreal": {
        "ADC": {
            "core": ["Manamune", "Fuerza de trinidad", "Rencor de Serylda"],
            "sit": ["Hoja del rey arruinado", "Ángel de la guarda", "Fauces de Malmortius", "Corona de la Reina Fragmentada"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Conquistador", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Banda de Maná"],
            "spells": ["Destello", "Curación"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "MID": {
            "core": ["Manamune", "Fuerza de trinidad", "Rencor de Serylda"],
            "sit": ["Corona de la Reina Fragmentada", "Hoja del rey arruinado", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "fiddlesticks": {
        "JUNGLE": {
            "core": ["Tormento de Liandry", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Cetro de cristal de Rylai", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Cetro de cristal de Rylai", "Tormento de Liandry"],
            "sit": ["Orbe infinito", "Sombrero mortal de Rabadon", "Morellonomicón", "Corona de la Reina Fragmentada"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "fiora": {
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Rompecascos"],
            "sit": ["Calibrador de Sterak", "Ángel de la guarda", "Malla de espinas", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Linaje", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "fizz": {
        "MID": {
            "core": ["Eco de Luden", "Perdición del liche", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "galio": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Corona de la Reina Fragmentada", "Bastón del Vacío", "Morellonomicón", "Tormento de Liandry"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas de mercurio"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas de mercurio", "upgrade": "Trituradoras encadenadas", "sit_boots": ["Botas blindadas"],
            "runes": ["Irrupción de Fase", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Celeridad"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "garen": {
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Coraza del muerto", "Fuerza de la naturaleza", "Malla de espinas", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "gnar": {
        "TOP": {
            "core": ["Fuerza de trinidad", "Calibrador de Sterak", "Baile de la muerte"],
            "sit": ["Cuchilla negra", "Fuerza de la naturaleza", "Malla de espinas", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Revestimiento de Huesos", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "gragas": {
        "JUNGLE": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Perdición del liche", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "TOP": {
            "core": ["Tormento de Liandry", "Creagrietas", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Malla de espinas", "Fuerza de la naturaleza", "Morellonomicón"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Irrupción de Fase", "Banda de Maná", "Trascendencia", "Piroláser", "Fuerzas Renovadas"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Perdición del liche", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "graves": {
        "JUNGLE": {
            "core": ["Filo fantasmal de Youmuu", "Recaudadora", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Baile de la muerte"],
            "boot": "Grebas codiciosas", "upgrade": "Botas inmortales", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Sanguinaria"],
            "sit": ["Cuchilla negra", "Calibrador de Sterak", "Ángel de la guarda", "Malla de espinas"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Pies Veloces", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "gwen": {
        "TOP": {
            "core": ["Diente de Nashor", "Creagrietas", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Diente de Nashor", "Creagrietas", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Orbe infinito", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Presteza", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "hecarim": {
        "JUNGLE": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Cuchilla negra", "Fuerza de la naturaleza", "Malla de espinas", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Celeridad"],
            "spells": ["Fantasmal", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "heimerdinger": {
        "MID": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón", "Impulso Cósmico"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "TOP": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón", "Impulso Cósmico"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de maná"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Fuerzas Renovadas"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Cetro de cristal de Rylai", "Tormento de Liandry"],
            "sit": ["Mandato imperial", "Morellonomicón", "Sombrero mortal de Rabadon", "Corona de la Reina Fragmentada"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Extenuación"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "hwei": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Tormento de Liandry", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Cetro de cristal de Rylai", "Mandato imperial"],
            "sit": ["Tormento de Liandry", "Morellonomicón", "Sombrero mortal de Rabadon", "Corona de la Reina Fragmentada"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "irelia": {
        "TOP": {
            "core": ["Hoja del rey arruinado", "Fuerza de trinidad", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Final del ingenio", "Ángel de la guarda", "Malla de espinas"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Brutal", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "MID": {
            "core": ["Hoja del rey arruinado", "Fuerza de trinidad", "Baile de la muerte"],
            "sit": ["Final del ingenio", "Calibrador de Sterak", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas de mercurio", "upgrade": "Trituradoras encadenadas", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "janna": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Incensario ardiente"],
            "sit": ["Bastón de aguas fluidas", "Mandato imperial", "Redención", "Bendición de Mikael"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas blindadas"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Curación"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "jarvan_iv": {
        "JUNGLE": {
            "core": ["Cuchilla negra", "Calibrador de Sterak", "Baile de la muerte"],
            "sit": ["Ángel de la guarda", "Malla de espinas", "Fuerza de la naturaleza", "Colmillo de serpiente"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "TOP": {
            "core": ["Cuchilla negra", "Fuerza de trinidad", "Calibrador de Sterak"],
            "sit": ["Baile de la muerte", "Ángel de la guarda", "Malla de espinas", "Fuerza de la naturaleza"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "jax": {
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Hoja del rey arruinado", "Rompecascos", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Compás Letal", "Triunfo", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Hoja del rey arruinado", "Ángel de la guarda", "Fauces de Malmortius", "Malla de espinas"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Compás Letal", "Triunfo", "Leyenda: Presteza", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "jayce": {
        "TOP": {
            "core": ["Manamune", "Eclipse", "Rencor de Serylda"],
            "sit": ["Filo de la noche", "Ángel de la guarda", "Baile de la muerte", "Fauces de Malmortius"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "MID": {
            "core": ["Manamune", "Filo fantasmal de Youmuu", "Rencor de Serylda"],
            "sit": ["Eclipse", "Filo de la noche", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "jhin": {
        "ADC": {
            "core": ["Filo Infinito", "Recaudadora", "Recuerdos de Lord Dominik"],
            "sit": ["Cañón de Fuego Rápido", "Sanguinaria", "Ángel de la guarda", "Filo fantasmal de Youmuu"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Pies Veloces", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Celeridad"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "jinx": {
        "ADC": {
            "core": ["Filo Infinito", "Huracán de Runaan", "Recuerdos de Lord Dominik"],
            "sit": ["Sanguinaria", "Cañón de Fuego Rápido", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "k_sante": {
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Guantelete de hielo", "Fuerza de la naturaleza", "Presagio de Randuin", "Coraza dual purpúrea"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "kai_sa": {
        "ADC": {
            "core": ["Manamune", "Diente de Nashor", "Sombrero mortal de Rabadon"],
            "sit": ["Corona de la Reina Fragmentada", "Huracán de Runaan", "Ángel de la guarda", "Bastón del Vacío"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "kalista": {
        "ADC": {
            "core": ["Hoja del rey arruinado", "Huracán de Runaan", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "karma": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Mandato imperial"],
            "sit": ["Incensario ardiente", "Bastón de aguas fluidas", "Corona de la Reina Fragmentada", "Redención"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón", "Impulso Cósmico"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "kassadin": {
        "MID": {
            "core": ["Vara de las edades", "Abrazo del serafín", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas de mercurio"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "katarina": {
        "MID": {
            "core": ["Orbe infinito", "Diente de Nashor", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Creagrietas", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "kayle": {
        "TOP": {
            "core": ["Diente de Nashor", "Creagrietas", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Orbe infinito", "Morellonomicón"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas de maná"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Sobrecrecimiento"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "MID": {
            "core": ["Diente de Nashor", "Creagrietas", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Orbe infinito", "Morellonomicón"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas de maná"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Banda de Maná"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "kayn": {
        "JUNGLE": {
            "core": ["Filo fantasmal de Youmuu", "Filoscuro de Draktharr", "Rencor de Serylda"],
            "sit": ["Eclipse", "Filo de la noche", "Ángel de la guarda", "Baile de la muerte"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "kennen": {
        "TOP": {
            "core": ["Creagrietas", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Tormento de Liandry", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Perdición del liche", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "kha_zix": {
        "JUNGLE": {
            "core": ["Filo fantasmal de Youmuu", "Filoscuro de Draktharr", "Rencor de Serylda"],
            "sit": ["Filo de la noche", "Eclipse", "Ángel de la guarda", "Colmillo de serpiente"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "kindred": {
        "JUNGLE": {
            "core": ["Fuerza de trinidad", "Filo Infinito", "Recuerdos de Lord Dominik"],
            "sit": ["Hoja del rey arruinado", "Sanguinaria", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Conquistador", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "ADC": {
            "core": ["Filo Infinito", "Huracán de Runaan", "Recuerdos de Lord Dominik"],
            "sit": ["Hoja del rey arruinado", "Sanguinaria", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "kog_maw": {
        "ADC": {
            "core": ["Hoja del rey arruinado", "Huracán de Runaan", "Final del ingenio"],
            "sit": ["Filo Infinito", "Recuerdos de Lord Dominik", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "MID": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón", "Impulso Cósmico"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "lee_sin": {
        "JUNGLE": {
            "core": ["Eclipse", "Cuchilla negra", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Ángel de la guarda", "Fauces de Malmortius", "Colmillo de serpiente"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "leona": {
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Celeridad"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "lillia": {
        "JUNGLE": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Creagrietas", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Coraza dual purpúrea"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Celeridad"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "TOP": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Creagrietas"],
            "sit": ["Sombrero mortal de Rabadon", "Bastón del Vacío", "Coraza dual purpúrea", "Fuerza de la naturaleza"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Fuerzas Renovadas"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "lissandra": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón", "Tormento de Liandry"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "lucian": {
        "ADC": {
            "core": ["Filo Infinito", "Recaudadora", "Recuerdos de Lord Dominik"],
            "sit": ["Sanguinaria", "Cañón de Fuego Rápido", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Brutal"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "MID": {
            "core": ["Filo Infinito", "Recaudadora", "Recuerdos de Lord Dominik"],
            "sit": ["Sanguinaria", "Fauces de Malmortius", "Ángel de la guarda", "Filo de la noche"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Brutal"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "lulu": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Incensario ardiente"],
            "sit": ["Bastón de aguas fluidas", "Mandato imperial", "Redención", "Bendición de Mikael"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas blindadas"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Extenuación"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "lux": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Precisión infalible", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Golpe Bajo"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco de Luden", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Mandato imperial", "Morellonomicón", "Corona de la Reina Fragmentada"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Fuente de Vida"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "malphite": {
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Guantelete de hielo", "Fuerza de la naturaleza", "Presagio de Randuin", "Coraza dual purpúrea"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Revestimiento de Huesos", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Perdición del liche", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Malla de espinas"],
            "sit": ["Promesa de caballero", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Celeridad"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "maokai": {
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Celeridad"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Guantelete de hielo", "Presagio de Randuin", "Armadura de Warmog"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Égida de fuego solar", "Malla de espinas", "Fuerza de la naturaleza"],
            "sit": ["Corazón de acero", "Coraza dual purpúrea", "Guantelete de hielo", "Coraza del muerto"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "master_yi": {
        "JUNGLE": {
            "core": ["Hoja del rey arruinado", "Final del ingenio", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Ángel de la guarda", "Filo Infinito", "Recuerdos de Lord Dominik"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "mel": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Mandato imperial"],
            "sit": ["Morellonomicón", "Sombrero mortal de Rabadon", "Corona de la Reina Fragmentada", "Incensario ardiente"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "milio": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Incensario ardiente"],
            "sit": ["Bastón de aguas fluidas", "Mandato imperial", "Redención", "Bendición de Mikael"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas blindadas"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Curación"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "miss_fortune": {
        "ADC": {
            "core": ["Filo fantasmal de Youmuu", "Recaudadora", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Filo de la noche"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Brutal"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Tormento de Liandry", "Cetro de cristal de Rylai"],
            "sit": ["Morellonomicón", "Sombrero mortal de Rabadon", "Mandato imperial", "Corona de la Reina Fragmentada"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Golpe Bajo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "mordekaiser": {
        "TOP": {
            "core": ["Cetro de cristal de Rylai", "Creagrietas", "Tormento de Liandry"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Sombrero mortal de Rabadon", "Coraza dual purpúrea"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Cetro de cristal de Rylai", "Creagrietas", "Tormento de Liandry"],
            "sit": ["Coraza del muerto", "Fuerza de la naturaleza", "Malla de espinas", "Sombrero mortal de Rabadon"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Presteza", "Golpe de Gracia", "Celeridad"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "morgana": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Cetro de cristal de Rylai", "Tormento de Liandry"],
            "sit": ["Morellonomicón", "Corona de la Reina Fragmentada", "Sombrero mortal de Rabadon", "Medallón de los Solari de Hierro"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "MID": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón", "Orbe infinito"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón", "Impulso Cósmico"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cosecha Oscura", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    }
}
