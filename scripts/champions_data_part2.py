# -*- coding: utf-8 -*-
"""
Champion builds and runes database for Part 2 (70 Champions).
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

PART2_BUILDS = {
    "nami": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Mandato imperial"],
            "sit": ["Incensario ardiente", "Bastón de aguas fluidas", "Redención", "Bendición de Mikael"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas blindadas"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Curación"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "nasus": {
        "TOP": {
            "core": ["Fuerza de trinidad", "Corazón de hielo", "Fuerza de la naturaleza"],
            "sit": ["Calibrador de Sterak", "Malla de espinas", "Presagio de Randuin", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "nautilus": {
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Celeridad"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Guantelete de hielo", "Presagio de Randuin", "Armadura de Warmog"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Revestimiento de Huesos", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Égida de fuego solar", "Malla de espinas", "Fuerza de la naturaleza"],
            "sit": ["Corazón de acero", "Coraza del muerto", "Presagio de Randuin", "Guantelete de hielo"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "nidalee": {
        "JUNGLE": {
            "core": ["Eco de Luden", "Perdición del liche", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cosecha Oscura", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "nilah": {
        "JUNGLE": {
            "core": ["Filo Infinito", "Recuerdos de Lord Dominik", "Sanguinaria"],
            "sit": ["Baile de la muerte", "Ángel de la guarda", "Fauces de Malmortius", "Arcoescudo Inmortal"],
            "boot": "Grebas codiciosas", "upgrade": "Botas inmortales", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "ADC": {
            "core": ["Filo Infinito", "Recuerdos de Lord Dominik", "Sanguinaria"],
            "sit": ["Baile de la muerte", "Ángel de la guarda", "Arcoescudo Inmortal", "Fauces de Malmortius"],
            "boot": "Grebas codiciosas", "upgrade": "Botas inmortales", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "nocturne": {
        "JUNGLE": {
            "core": ["Filo fantasmal de Youmuu", "Filoscuro de Draktharr", "Cuchilla negra"],
            "sit": ["Filo de la noche", "Ángel de la guarda", "Baile de la muerte", "Fauces de Malmortius"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Compás Letal", "Triunfo", "Leyenda: Presteza", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "norra": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Mandato imperial"],
            "sit": ["Morellonomicón", "Corona de la Reina Fragmentada", "Incensario ardiente", "Sombrero mortal de Rabadon"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "nunu_willump": {
        "JUNGLE": {
            "core": ["Égida de fuego solar", "Malla de espinas", "Fuerza de la naturaleza"],
            "sit": ["Coraza del muerto", "Corazón de acero", "Presagio de Randuin", "Coraza dual purpúrea"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Irrupción de Fase", "Celeridad", "Capa del Nimbo", "Trascendencia", "Fuente de Vida"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "olaf": {
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Cuchilla negra", "Malla de espinas", "Fuerza de la naturaleza", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Cuchilla negra", "Malla de espinas", "Fuerza de la naturaleza", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Presteza", "Último Esfuerzo", "Celeridad"],
            "spells": ["Fantasmal", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "orianna": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "ornn": {
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Presagio de Randuin", "Máscara abisal", "Armadura de Warmog"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "pantheon": {
        "TOP": {
            "core": ["Eclipse", "Cuchilla negra", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Rencor de Serylda", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "MID": {
            "core": ["Filo fantasmal de Youmuu", "Eclipse", "Rencor de Serylda"],
            "sit": ["Filo de la noche", "Ángel de la guarda", "Baile de la muerte", "Fauces de Malmortius"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Cuchilla negra", "Colmillo de serpiente"],
            "sit": ["Ángel de la guarda", "Baile de la muerte", "Fauces de Malmortius", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Fuente de Vida"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Eclipse", "Cuchilla negra", "Rencor de Serylda"],
            "sit": ["Calibrador de Sterak", "Ángel de la guarda", "Baile de la muerte", "Colmillo de serpiente"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "poppy": {
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Guantelete de hielo", "Fuerza de la naturaleza", "Presagio de Randuin", "Coraza dual purpúrea"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Revestimiento de Huesos", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Cuchilla negra", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Coraza del muerto", "Guantelete de hielo", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Irrupción de Fase", "Celeridad", "Capa del Nimbo", "Trascendencia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Guardián", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "pyke": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Filo fantasmal de Youmuu", "Filoscuro de Draktharr"],
            "sit": ["Filo de la noche", "Ángel de la guarda", "Colmillo de serpiente", "Fauces de Malmortius"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "MID": {
            "core": ["Filo fantasmal de Youmuu", "Filoscuro de Draktharr", "Filo de la noche"],
            "sit": ["Rencor de Serylda", "Ángel de la guarda", "Colmillo de serpiente", "Fauces de Malmortius"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas de mercurio"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "rakan": {
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Eco armónico", "Fuerza de la naturaleza", "Medallón de los Solari de Hierro", "Corona de la Reina Fragmentada"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas blindadas"],
            "runes": ["Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Celeridad"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "rammus": {
        "JUNGLE": {
            "core": ["Malla de espinas", "Coraza del muerto", "Fuerza de la naturaleza"],
            "sit": ["Presagio de Randuin", "Corazón de hielo", "Coraza dual purpúrea", "Égida de fuego solar"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "rell": {
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Égida de fuego solar", "Malla de espinas", "Fuerza de la naturaleza"],
            "sit": ["Coraza del muerto", "Convergencia de Zeke", "Coraza dual purpúrea", "Presagio de Randuin"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "renekton": {
        "TOP": {
            "core": ["Eclipse", "Cuchilla negra", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Rencor de Serylda", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "rengar": {
        "JUNGLE": {
            "core": ["Filo fantasmal de Youmuu", "Filoscuro de Draktharr", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Filo de la noche"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Cuchilla negra", "Ángel de la guarda", "Malla de espinas", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Linaje", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "riven": {
        "TOP": {
            "core": ["Eclipse", "Cuchilla negra", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Rencor de Serylda", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "rumble": {
        "TOP": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Creagrietas", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas blindadas"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Fuerzas Renovadas"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "MID": {
            "core": ["Tormento de Liandry", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Cetro de cristal de Rylai", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Creagrietas", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas de mercurio"],
            "runes": ["Cosecha Oscura", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "ryze": {
        "MID": {
            "core": ["Vara de las edades", "Abrazo del serafín", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Tormento de Liandry", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas de mercurio"],
            "runes": ["Irrupción de Fase", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Sobrecrecimiento"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "TOP": {
            "core": ["Vara de las edades", "Abrazo del serafín", "Sombrero mortal de Rabadon"],
            "sit": ["Tormento de Liandry", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Fuerza de la naturaleza"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Irrupción de Fase", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Fuerzas Renovadas"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "samira": {
        "ADC": {
            "core": ["Sanguinaria", "Filo Infinito", "Recuerdos de Lord Dominik"],
            "sit": ["Recaudadora", "Baile de la muerte", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas codiciosas", "upgrade": "Botas inmortales", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Brutal", "Leyenda: Linaje", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "senna": {
        "ADC": {
            "core": ["Filo fantasmal de Youmuu", "Cuchilla negra", "Rencor de Serylda"],
            "sit": ["Cañón de Fuego Rápido", "Filo Infinito", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Pies Veloces", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Cuchilla negra", "Filo fantasmal de Youmuu"],
            "sit": ["Cañón de Fuego Rápido", "Colmillo de serpiente", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas blindadas"],
            "runes": ["Pies Veloces", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Fuente de Vida"],
            "spells": ["Destello", "Curación"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "seraphine": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Cetro de cristal de Rylai"],
            "sit": ["Incensario ardiente", "Bastón de aguas fluidas", "Mandato imperial", "Corona de la Reina Fragmentada"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Curación"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "ADC": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Revestimiento de Huesos"],
            "spells": ["Destello", "Curación"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "sett": {
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Hidra titánica", "Malla de espinas", "Fuerza de la naturaleza", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Calibrador de Sterak", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "shen": {
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Presagio de Randuin", "Guantelete de hielo", "Armadura de Warmog"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Guardián", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "shyvana": {
        "JUNGLE": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Ángel de la guarda", "Tormento de Liandry"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Presteza", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "singed": {
        "TOP": {
            "core": ["Cetro de cristal de Rylai", "Tormento de Liandry", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Coraza del muerto", "Sombrero mortal de Rabadon", "Coraza dual purpúrea"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Celeridad"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "sion": {
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Presagio de Randuin", "Hidra titánica", "Rompecascos"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "sivir": {
        "ADC": {
            "core": ["Filo Infinito", "Puñal de Statikk", "Recuerdos de Lord Dominik"],
            "sit": ["Sanguinaria", "Cañón de Fuego Rápido", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "skarner": {
        "JUNGLE": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Coraza del muerto", "Presagio de Randuin", "Guantelete de hielo"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Celeridad"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Guantelete de hielo", "Fuerza de la naturaleza", "Presagio de Randuin", "Armadura de Warmog"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Revestimiento de Huesos", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "smolder": {
        "ADC": {
            "core": ["Segador de esencia", "Filo Infinito", "Recuerdos de Lord Dominik"],
            "sit": ["Cañón de Fuego Rápido", "Sanguinaria", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "MID": {
            "core": ["Segador de esencia", "Manamune", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Cañón de Fuego Rápido", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "sona": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Incensario ardiente"],
            "sit": ["Bastón de aguas fluidas", "Mandato imperial", "Redención", "Bendición de Mikael"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas blindadas"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Curación"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "soraka": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Incensario ardiente"],
            "sit": ["Bastón de aguas fluidas", "Mandato imperial", "Redención", "Bendición de Mikael"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas blindadas"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Curación"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "swain": {
        "MID": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Creagrietas", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Fuerzas Renovadas"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Cetro de cristal de Rylai", "Tormento de Liandry"],
            "sit": ["Morellonomicón", "Sombrero mortal de Rabadon", "Mandato imperial", "Corona de la Reina Fragmentada"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Revitalizar"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "TOP": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Creagrietas"],
            "sit": ["Sombrero mortal de Rabadon", "Malla de espinas", "Fuerza de la naturaleza", "Morellonomicón"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Fuerzas Renovadas"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "syndra": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "taliyah": {
        "JUNGLE": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Cetro de cristal de Rylai", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Cetro de cristal de Rylai", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Golpe Bajo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "talon": {
        "JUNGLE": {
            "core": ["Filo fantasmal de Youmuu", "Filoscuro de Draktharr", "Rencor de Serylda"],
            "sit": ["Eclipse", "Filo de la noche", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "MID": {
            "core": ["Filo fantasmal de Youmuu", "Filoscuro de Draktharr", "Rencor de Serylda"],
            "sit": ["Eclipse", "Filo de la noche", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "teemo": {
        "TOP": {
            "core": ["Tormento de Liandry", "Diente de Nashor", "Cetro de cristal de Rylai"],
            "sit": ["Sombrero mortal de Rabadon", "Bastón del Vacío", "Morellonomicón", "Corona de la Reina Fragmentada"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas blindadas"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Fuerzas Renovadas"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Tormento de Liandry", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Morellonomicón", "Corona de la Reina Fragmentada"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "thresh": {
        "SUPPORT": {
            "core": ["Baluarte de la montaña", "Convergencia de Zeke", "Promesa de caballero"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Corazón de hielo", "Medallón de los Solari de Hierro"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Soberano Gélido", "Fuente de Vida", "Revestimiento de Huesos", "Sobrecrecimiento", "Hextello"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "tristana": {
        "ADC": {
            "core": ["Filo Infinito", "Puñal de Statikk", "Recuerdos de Lord Dominik"],
            "sit": ["Sanguinaria", "Cañón de Fuego Rápido", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "MID": {
            "core": ["Filo Infinito", "Puñal de Statikk", "Recuerdos de Lord Dominik"],
            "sit": ["Sanguinaria", "Ángel de la guarda", "Fauces de Malmortius", "Filo de la noche"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Brutal"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "tryndamere": {
        "TOP": {
            "core": ["Hoja del rey arruinado", "Filo Infinito", "Recuerdos de Lord Dominik"],
            "sit": ["Bailarín Espectral", "Sanguinaria", "Ángel de la guarda", "Rompecascos"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Hoja del rey arruinado", "Filo Infinito", "Recuerdos de Lord Dominik"],
            "sit": ["Bailarín Espectral", "Sanguinaria", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "twisted_fate": {
        "MID": {
            "core": ["Eco de Luden", "Perdición del liche", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "twitch": {
        "ADC": {
            "core": ["Hoja del rey arruinado", "Huracán de Runaan", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Hoja del rey arruinado", "Huracán de Runaan", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "urgot": {
        "TOP": {
            "core": ["Cuchilla negra", "Calibrador de Sterak", "Baile de la muerte"],
            "sit": ["Malla de espinas", "Fuerza de la naturaleza", "Presagio de Randuin", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "varus": {
        "ADC": {
            "core": ["Manamune", "Filo fantasmal de Youmuu", "Rencor de Serylda"],
            "sit": ["Filo de la noche", "Ángel de la guarda", "Colmillo de serpiente", "Fauces de Malmortius"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "MID": {
            "core": ["Manamune", "Filo fantasmal de Youmuu", "Rencor de Serylda"],
            "sit": ["Eclipse", "Filo de la noche", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "vayne": {
        "ADC": {
            "core": ["Hoja del rey arruinado", "Huracán de Runaan", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "TOP": {
            "core": ["Hoja del rey arruinado", "Fuerza de trinidad", "Final del ingenio"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Malla de espinas"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Fuerzas Renovadas"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "veigar": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "ADC": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón", "Impulso Cósmico"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "vel_koz": {
        "MID": {
            "core": ["Eco de Luden", "Tormento de Liandry", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Tormento de Liandry", "Cetro de cristal de Rylai"],
            "sit": ["Morellonomicón", "Sombrero mortal de Rabadon", "Orbe infinito", "Corona de la Reina Fragmentada"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Golpe Bajo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "vex": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "vi": {
        "JUNGLE": {
            "core": ["Fuerza de trinidad", "Cuchilla negra", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Ángel de la guarda", "Malla de espinas", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "viego": {
        "JUNGLE": {
            "core": ["Hoja del rey arruinado", "Fuerza de trinidad", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Ángel de la guarda", "Recuerdos de Lord Dominik", "Fauces de Malmortius"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Linaje", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "MID": {
            "core": ["Hoja del rey arruinado", "Fuerza de trinidad", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Ángel de la guarda", "Fauces de Malmortius", "Filo Infinito"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Linaje", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "viktor": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Tormento de Liandry", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "vladimir": {
        "MID": {
            "core": ["Creagrietas", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Irrupción de Fase", "Trascendencia", "Celeridad", "Se Avecina Tormenta", "Sobrecrecimiento"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "TOP": {
            "core": ["Creagrietas", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Tormento de Liandry", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas blindadas"],
            "runes": ["Irrupción de Fase", "Trascendencia", "Celeridad", "Se Avecina Tormenta", "Fuerzas Renovadas"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "volibear": {
        "TOP": {
            "core": ["Corazón de acero", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Guantelete de hielo", "Presagio de Randuin", "Hidra titánica"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Fuerza de trinidad", "Égida de fuego solar", "Malla de espinas"],
            "sit": ["Fuerza de la naturaleza", "Coraza del muerto", "Calibrador de Sterak", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Celeridad"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "warwick": {
        "TOP": {
            "core": ["Hoja del rey arruinado", "Guantelete de hielo", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Malla de espinas", "Fuerza de la naturaleza", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Garras del Inmortal", "Demoler", "Fuerzas Renovadas", "Sobrecrecimiento", "Triunfo"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Hoja del rey arruinado", "Fuerza de trinidad", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Malla de espinas", "Fuerza de la naturaleza", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Celeridad"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "wukong": {
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Cuchilla negra", "Malla de espinas", "Fuerza de la naturaleza", "Ángel de la guarda"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Cuchilla negra", "Rencor de Serylda", "Ángel de la guarda", "Colmillo de serpiente"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "xayah": {
        "ADC": {
            "core": ["Filo Infinito", "Puñal de Statikk", "Recuerdos de Lord Dominik"],
            "sit": ["Cañón de Fuego Rápido", "Sanguinaria", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "xin_zhao": {
        "JUNGLE": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Cuchilla negra", "Ángel de la guarda", "Malla de espinas", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Golpe de Gracia", "Impacto Repentino"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        },
        "TOP": {
            "core": ["Fuerza de trinidad", "Baile de la muerte", "Calibrador de Sterak"],
            "sit": ["Cuchilla negra", "Ángel de la guarda", "Malla de espinas", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Botas de mercurio"],
            "runes": ["Conquistador", "Triunfo", "Leyenda: Velocidad", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "yasuo": {
        "MID": {
            "core": ["Hoja del rey arruinado", "Filo Infinito", "Arcoescudo Inmortal"],
            "sit": ["Baile de la muerte", "Recuerdos de Lord Dominik", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "TOP": {
            "core": ["Hoja del rey arruinado", "Filo Infinito", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Recuerdos de Lord Dominik", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Grebas de berserker"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        },
        "ADC": {
            "core": ["Hoja del rey arruinado", "Filo Infinito", "Arcoescudo Inmortal"],
            "sit": ["Baile de la muerte", "Recuerdos de Lord Dominik", "Ángel de la guarda", "Sanguinaria"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Extenuación"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "yone": {
        "MID": {
            "core": ["Hoja del rey arruinado", "Filo Infinito", "Arcoescudo Inmortal"],
            "sit": ["Baile de la muerte", "Recuerdos de Lord Dominik", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "TOP": {
            "core": ["Hoja del rey arruinado", "Filo Infinito", "Baile de la muerte"],
            "sit": ["Calibrador de Sterak", "Recuerdos de Lord Dominik", "Ángel de la guarda", "Fauces de Malmortius"],
            "boot": "Botas blindadas", "upgrade": "Avance blindado", "sit_boots": ["Grebas de berserker"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Presteza", "Último Esfuerzo", "Revestimiento de Huesos"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea de Barón (Meta Soberano)"
        }
    },
    "yunara": {
        "ADC": {
            "core": ["Filo Infinito", "Puñal de Statikk", "Recuerdos de Lord Dominik"],
            "sit": ["Sanguinaria", "Cañón de Fuego Rápido", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        },
        "MID": {
            "core": ["Filo Infinito", "Puñal de Statikk", "Recuerdos de Lord Dominik"],
            "sit": ["Sanguinaria", "Ángel de la guarda", "Fauces de Malmortius", "Filo de la noche"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Brutal"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "yuumi": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Incensario ardiente"],
            "sit": ["Bastón de aguas fluidas", "Mandato imperial", "Redención", "Bendición de Mikael"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Revitalizar"],
            "spells": ["Extenuación", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        }
    },
    "zed": {
        "MID": {
            "core": ["Filo fantasmal de Youmuu", "Filoscuro de Draktharr", "Rencor de Serylda"],
            "sit": ["Eclipse", "Filo de la noche", "Ángel de la guarda", "Colmillo de serpiente"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "JUNGLE": {
            "core": ["Filo fantasmal de Youmuu", "Filoscuro de Draktharr", "Rencor de Serylda"],
            "sit": ["Eclipse", "Filo de la noche", "Ángel de la guarda", "Baile de la muerte"],
            "boot": "Botas dinámicas", "upgrade": "Botas quebrantarmaduras", "sit_boots": ["Botas blindadas"],
            "runes": ["Primer Golpe", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Triunfo"],
            "spells": ["Destello", "Castigo"],
            "title": "Build Jungla (Meta Soberano)"
        }
    },
    "zeri": {
        "ADC": {
            "core": ["Fuerza de trinidad", "Huracán de Runaan", "Filo Infinito"],
            "sit": ["Recuerdos de Lord Dominik", "Sanguinaria", "Ángel de la guarda", "Arcoescudo Inmortal"],
            "boot": "Grebas de berserker", "upgrade": "Grebas de metal", "sit_boots": ["Grebas codiciosas"],
            "runes": ["Compás Letal", "Brutal", "Leyenda: Linaje", "Golpe de Gracia", "Revestimiento de Huesos"],
            "spells": ["Destello", "Fantasmal"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "ziggs": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Tormento de Liandry", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        },
        "ADC": {
            "core": ["Tormento de Liandry", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón", "Impulso Cósmico"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Se Avecina Tormenta", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea de Dragón (Meta Soberano)"
        }
    },
    "zilean": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Eco armónico", "Mandato imperial"],
            "sit": ["Incensario ardiente", "Corona de la Reina Fragmentada", "Redención", "Bendición de Mikael"],
            "boot": "Botas jonias de la lucidez", "upgrade": "Lucidez carmesí", "sit_boots": ["Botas de maná"],
            "runes": ["Aery", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Extenuación"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Impulso Cósmico", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revestimiento de Huesos"],
            "spells": ["Destello", "Barrera"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "zoe": {
        "MID": {
            "core": ["Eco de Luden", "Orbe infinito", "Sombrero mortal de Rabadon"],
            "sit": ["Bastón del Vacío", "Corona de la Reina Fragmentada", "Perdición del liche", "Morellonomicón"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Electrocutar", "Impacto Repentino", "Colección de Globos Oculares", "Tirano", "Banda de Maná"],
            "spells": ["Destello", "Prender"],
            "title": "Build Línea Central (Meta Soberano)"
        }
    },
    "zyra": {
        "SUPPORT": {
            "core": ["Guadaña de la Niebla Negra", "Tormento de Liandry", "Cetro de cristal de Rylai"],
            "sit": ["Morellonomicón", "Sombrero mortal de Rabadon", "Mandato imperial", "Corona de la Reina Fragmentada"],
            "boot": "Botas de maná", "upgrade": "Botas del lanzahechizos", "sit_boots": ["Botas jonias de la lucidez"],
            "runes": ["Cometa Arcano", "Banda de Maná", "Trascendencia", "Piroláser", "Revitalizar"],
            "spells": ["Destello", "Prender"],
            "title": "Build Soporte (Meta Soberano)"
        },
        "MID": {
            "core": ["Tormento de Liandry", "Cetro de cristal de Rylai", "Sombrero mortal de Rabadon"],
            "sit": ["Orbe infinito", "Bastón del Vacío", "Corona de la Reina Fragmentada", "Morellonomicón"],
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
