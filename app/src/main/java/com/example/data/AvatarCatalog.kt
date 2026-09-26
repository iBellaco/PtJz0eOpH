package com.example.data

import com.example.model.AvatarItem

object AvatarCatalog {
    val DEFAULT_AVATAR = AvatarItem(
        id = "default_poro",
        name = "Poro Guardián",
        title = "Espíritu de la Grieta",
        region = "Poro",
        rarity = "Clásico",
        imageUrl = "file:///android_asset/offline_images/763bd901dd7903b0ef0081f2f1109dbd.png",
        borderHex = "#C8AA6E",
        description = "El leal compañero de todo Invocador de Wild Rift.",
        isDefault = true
    )

    val avatars: List<AvatarItem> = listOf(
        DEFAULT_AVATAR,
        AvatarItem(
            id = "poro_wukong",
            name = "Poro Wukong",
            title = "El Rey Mono",
            region = "Poro",
            rarity = "Común",
            imageUrl = "file:///android_asset/offline_images/e8a4313a1a6195419ea38509e182357f.jpg",
            borderHex = "#A0A0A0",
            description = "Un poro listo para la batalla con su bastón mágico."
        ),
        AvatarItem(
            id = "poro_volibear",
            name = "Poro Volibear",
            title = "El Rugido del Trueno",
            region = "Poro",
            rarity = "Raro",
            imageUrl = "file:///android_asset/offline_images/0e2476c9fb4f0a4f0bba0ca15a867c0f.jpg",
            borderHex = "#00BFFF",
            description = "Un poro imbuido con el poder de la tormenta."
        ),
        AvatarItem(
            id = "poro_nashor",
            name = "Poro Nashor",
            title = "El Rey de la Grieta",
            region = "Poro",
            rarity = "Épico",
            imageUrl = "file:///android_asset/offline_images/0ff726a7d53de0d252297216e58ee357.jpg",
            borderHex = "#8A2BE2",
            description = "El poro más temible del río."
        ),
        AvatarItem(
            id = "poro_ahri",
            name = "Poro Ahri",
            title = "La Mujer Zorro",
            region = "Poro",
            rarity = "Épico",
            imageUrl = "file:///android_asset/offline_images/03fa2d4274a2a4a79b70f0b8b40a26e2.jpg",
            borderHex = "#8A2BE2",
            description = "Un poro con un encanto irresistible."
        ),
        AvatarItem(
            id = "poro_kaisa",
            name = "Poro Kai'Sa",
            title = "Hija del Vacío",
            region = "Poro",
            rarity = "Épico",
            imageUrl = "file:///android_asset/offline_images/e461f7b0e02a95ba7f0413a4981e2aa9.jpg",
            borderHex = "#8A2BE2",
            description = "Un poro que sobrevivió al vacío."
        ),
        AvatarItem(
            id = "poro_irelia",
            name = "Poro Irelia",
            title = "La Danza de las Cuchillas",
            region = "Poro",
            rarity = "Común",
            imageUrl = "file:///android_asset/offline_images/930c937ac0438948c6eadfb2bea29a30.jpg",
            borderHex = "#A0A0A0",
            description = "Un poro que danza en el campo de batalla."
        ),
        AvatarItem(
            id = "poro_aatrox",
            name = "Poro Aatrox",
            title = "La Espada de los Oscuros",
            region = "Poro",
            rarity = "Épico",
            imageUrl = "file:///android_asset/poros/poro_aatrox.jpg",
            borderHex = "#8A2BE2",
            description = "Un poro consumido por la furia oscura."
        ),
        AvatarItem(
            id = "poro_akshan",
            name = "Poro Akshan",
            title = "El Centinela Rebelde",
            region = "Poro",
            rarity = "Raro",
            imageUrl = "file:///android_asset/poros/poro_akshan.jpg",
            borderHex = "#00BFFF",
            description = "Un poro carismático buscando justicia."
        ),
        AvatarItem(
            id = "poro_alistar",
            name = "Poro Alistar",
            title = "El Minotauro",
            region = "Poro",
            rarity = "Común",
            imageUrl = "file:///android_asset/poros/poro_alistar.jpg",
            borderHex = "#A0A0A0",
            description = "Un poro fuerte que protege a su rebaño."
        ),
        AvatarItem(
            id = "poro_akali",
            name = "Poro Akali",
            title = "La Asesina Sigilosa",
            region = "Poro",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/poros/poro_akali.jpg",
            borderHex = "#FFD700",
            description = "Un poro ninja que acecha en las sombras."
        ),
        AvatarItem(
            id = "poro_ambessa",
            name = "Poro Ambessa",
            title = "La Matriarca de la Guerra",
            region = "Poro",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/poros/poro_ambessa.jpg",
            borderHex = "#FF0000",
            description = "Un poro con una voluntad de hierro."
        ),
        AvatarItem(
            id = "poro_amumu",
            name = "Poro Amumu",
            title = "La Momia Triste",
            region = "Poro",
            rarity = "Épico",
            imageUrl = "file:///android_asset/poros/poro_amumu.jpg",
            borderHex = "#8A2BE2",
            description = "Un poro solitario buscando amigos."
        ),
        AvatarItem(
            id = "aatrox",
            name = "Aatrox",
            title = "la Espada de los Oscuros",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/aatrox.png",
            borderHex = "#C8AA6E",
            description = "Aatrox y sus hermanos, otrora respetados defensores de Shurima contra el Vacío, acabarían convi..."
        ),
        AvatarItem(
            id = "ahri",
            name = "Ahri",
            title = "La Mujer Zorro de nueve Colas",
            region = "Jonia",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/ahri.png",
            borderHex = "#C8AA6E",
            description = "Ahri es una raposa vastaya conectada de forma innata a la magia del reino de los espíritus. Es ..."
        ),
        AvatarItem(
            id = "akali",
            name = "Akali",
            title = "la Asesina Sigilosa",
            region = "Jonia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/akali.png",
            borderHex = "#C8AA6E",
            description = "Tras abandonar la Orden Kinkou y su título de Puño de la Sombra, Akali actúa ahora en solitario..."
        ),
        AvatarItem(
            id = "akshan",
            name = "Akshan",
            title = "el Centinela Rebelde",
            region = "Shurima",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/akshan.png",
            borderHex = "#C8AA6E",
            description = "Impávido ante el peligro, Akshan combate el mal con gran carisma, ganas de impartir justa venga..."
        ),
        AvatarItem(
            id = "alistar",
            name = "Alistar",
            title = "El Minotauro",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/alistar.png",
            borderHex = "#C8AA6E",
            description = "Alistar, un poderoso guerrero con una reputación temible, busca venganza por la muerte de su cl..."
        ),
        AvatarItem(
            id = "ambessa",
            name = "Ambessa",
            title = "la Matriarca de la Guerra",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/offline_images/0e02eb044e810d4aaad954fb52aabe0b.webp",
            borderHex = "#C8AA6E",
            description = "Ambessa Medarda comanda el campo de batalla con implacable disciplina militar. Sus cadenas y ho..."
        ),
        AvatarItem(
            id = "amumu",
            name = "Amumu",
            title = "La Momia Triste",
            region = "Shurima",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/amumu.png",
            borderHex = "#C8AA6E",
            description = "Cuenta la leyenda que Amumu es un alma solitaria y melancólica de la vieja Shurima que vaga por..."
        ),
        AvatarItem(
            id = "annie",
            name = "Annie",
            title = "La Hija de la Oscuridad",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/annie.png",
            borderHex = "#C8AA6E",
            description = "Peligrosa pero encantadoramente precoz, Annie es una pequeña maga con un inmenso poder pirománt..."
        ),
        AvatarItem(
            id = "ashe",
            name = "Ashe",
            title = "La Arquera de Hielo",
            region = "Freljord",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/ashe.png",
            borderHex = "#C8AA6E",
            description = "Ashe, comandante hija del hielo de la tribu de Avarosa, lidera las hordas más numerosas del nor..."
        ),
        AvatarItem(
            id = "aurelion_sol",
            name = "Aurelion Sol",
            title = "El Forjador de Estrellas",
            region = "Targon",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/aurelion_sol.png",
            borderHex = "#C8AA6E",
            description = "Aurelion Sol solía agraciar al vasto vacío del cosmos con las maravillas celestiales que él mis..."
        ),
        AvatarItem(
            id = "aurora",
            name = "Aurora",
            title = "la Bruja entre Mundos",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/aurora.png",
            borderHex = "#C8AA6E",
            description = "Desde que nació, Aurora ha tenido una visión única de la vida gracias a su capacidad para mover..."
        ),
        AvatarItem(
            id = "bard",
            name = "Bardo",
            title = "El Guardián Errante",
            region = "Runaterra",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/bard.png",
            borderHex = "#C8AA6E",
            description = "Bardo, un viajero de más allá de las estrellas, es un agente de la serendipia que lucha para ma..."
        ),
        AvatarItem(
            id = "blitzcrank",
            name = "Blitzcrank",
            title = "El Gran Gólem de Vapor",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/blitzcrank.png",
            borderHex = "#C8AA6E",
            description = "Blitzcrank es un autómata enorme, casi indestructible, creado originalmente para el tratamiento..."
        ),
        AvatarItem(
            id = "brand",
            name = "Brand",
            title = "La Venganza Ardiente",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/brand.png",
            borderHex = "#C8AA6E",
            description = "Brand, antiguo miembro de la tribu Kegan Rodhe del helado Freljord, es una lección sobre la ten..."
        ),
        AvatarItem(
            id = "braum",
            name = "Braum",
            title = "El Corazón de Freljord",
            region = "Freljord",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/braum.png",
            borderHex = "#C8AA6E",
            description = "Bendecido con bíceps enormes y un corazón aún más grande, Braum es un héroe muy apreciado en Fr..."
        ),
        AvatarItem(
            id = "caitlyn",
            name = "Caitlyn",
            title = "La Sheriff de Piltover",
            region = "Piltóver",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/caitlyn.png",
            borderHex = "#C8AA6E",
            description = "Reconocida como su mejor pacificadora, Caitlyn es también la mejor arma de Piltover para librar..."
        ),
        AvatarItem(
            id = "camille",
            name = "Camille",
            title = "la Sombra de Acero",
            region = "Piltóver",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/camille.png",
            borderHex = "#C8AA6E",
            description = "Convertida en un arma viviente diseñada para operar fuera de la ley, Camille es la jefa de espí..."
        ),
        AvatarItem(
            id = "cho_gath",
            name = "Cho'Gath",
            title = "El Terror del Vacío",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/cho_gath.png",
            borderHex = "#C8AA6E",
            description = "Desde el momento en que Cho'Gath emergió por primera vez a la dura luz solar de Runaterra, a la..."
        ),
        AvatarItem(
            id = "corki",
            name = "Corki",
            title = "El Bombardero Osado",
            region = "Ciudad de Bandle",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/corki.png",
            borderHex = "#C8AA6E",
            description = "El piloto yordle Corki adora dos cosas por encima de todas las demás: volar y su glamuroso bigo..."
        ),
        AvatarItem(
            id = "darius",
            name = "Darius",
            title = "La Mano de Noxus",
            region = "Noxus",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/darius.png",
            borderHex = "#C8AA6E",
            description = "No hay mayor símbolo del poder de Noxus que Darius, el comandante más temido y más curtido en b..."
        ),
        AvatarItem(
            id = "diana",
            name = "Diana",
            title = "El Desdén de la Luna",
            region = "Targon",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/diana.png",
            borderHex = "#C8AA6E",
            description = "Portadora de una espada en forma de media luna, Diana es una guerrera de los Lunari, una fe rec..."
        ),
        AvatarItem(
            id = "dr_mundo",
            name = "Dr. Mundo",
            title = "El Loco de Zaun",
            region = "Zaun",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/dr_mundo.png",
            borderHex = "#C8AA6E",
            description = "Loco de remate, trágicamente homicida, terriblemente morado: el Dr. Mundo es lo que mantiene en..."
        ),
        AvatarItem(
            id = "draven",
            name = "Draven",
            title = "El Ejecutor Glorioso",
            region = "Noxus",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/draven.png",
            borderHex = "#C8AA6E",
            description = "En Noxus, los guerreros conocidos como 'justicieros' se enfrentan en recintos donde corre la sa..."
        ),
        AvatarItem(
            id = "ekko",
            name = "Ekko",
            title = "El Chico que Quebró el Tiempo",
            region = "Zaun",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/ekko.png",
            borderHex = "#C8AA6E",
            description = "Ekko, un prodigio surgido de las implacables calles de Zaun, manipula el tiempo para sacar vent..."
        ),
        AvatarItem(
            id = "evelynn",
            name = "Evelynn",
            title = "El Abrazo Agónico",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/evelynn.png",
            borderHex = "#C8AA6E",
            description = "En los oscuros adentros de Runaterra, el súcubo Evelynn deambula en busca de su siguiente vícti..."
        ),
        AvatarItem(
            id = "ezreal",
            name = "Ezreal",
            title = "El Explorador Pródigo",
            region = "Piltóver",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/ezreal.png",
            borderHex = "#C8AA6E",
            description = "Ezreal, un aventurero aficionado a deslizarse y dotado de artes mágicas sin saberlo, saquea cat..."
        ),
        AvatarItem(
            id = "fiddlesticks",
            name = "Fiddlesticks",
            title = "el Terror Ancestral",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/fiddlesticks.png",
            borderHex = "#C8AA6E",
            description = "Algo ha despertado en Runaterra. Algo ancestral. Algo terrible. El horror conocido como Fiddles..."
        ),
        AvatarItem(
            id = "fiora",
            name = "Fiora",
            title = "La Estocada Excelsa",
            region = "Demacia",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/champions/fiora.png",
            borderHex = "#C8AA6E",
            description = "Fiora, la duelista más temida de Valoran, ha alcanzado renombre por su estilo brusco y su mente..."
        ),
        AvatarItem(
            id = "fizz",
            name = "Fizz",
            title = "El Gamberro de las Mareas",
            region = "Aguas Esturbias",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/fizz.png",
            borderHex = "#C8AA6E",
            description = "Fizz es un yordle anfibio que habita entre los arrecifes de alrededor de Aguas Estancadas. Suel..."
        ),
        AvatarItem(
            id = "galio",
            name = "Galio",
            title = "el Coloso",
            region = "Demacia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/galio.png",
            borderHex = "#C8AA6E",
            description = "Fuera de la reluciente ciudad de Demacia, el coloso de piedra Galio se mantiene vigilante. Cons..."
        ),
        AvatarItem(
            id = "garen",
            name = "Garen",
            title = "El Poder de Demacia",
            region = "Demacia",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/garen.png",
            borderHex = "#C8AA6E",
            description = "Garen, un orgulloso y noble guerrero, lucha en las filas de la Vanguardia Impertérrita. Es quer..."
        ),
        AvatarItem(
            id = "gnar",
            name = "Gnar",
            title = "El Eslabón Perdido",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/gnar.png",
            borderHex = "#C8AA6E",
            description = "Gnar es un yordle primitivo cuyas payasadas lúdicas pueden estallar en la ira de un niño pequeñ..."
        ),
        AvatarItem(
            id = "gragas",
            name = "Gragas",
            title = "El Camorrista",
            region = "Freljord",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/gragas.png",
            borderHex = "#C8AA6E",
            description = "Alegre e imponente por partes iguales, Gragas es un cervecero enorme y provocador en su propia ..."
        ),
        AvatarItem(
            id = "graves",
            name = "Graves",
            title = "El Forajido",
            region = "Aguas Esturbias",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/graves.png",
            borderHex = "#C8AA6E",
            description = "Malcolm Graves es un famoso mercenario, jugador y ladrón. Un hombre buscado en todas las ciudad..."
        ),
        AvatarItem(
            id = "gwen",
            name = "Gwen",
            title = "La Costurera Consagrada",
            region = "Islas de la Sombra",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/gwen.png",
            borderHex = "#C8AA6E",
            description = "Gwen, una muñeca a la que la magia confirió vida, va armada con los mismos utensilios que en su..."
        ),
        AvatarItem(
            id = "hecarim",
            name = "Hecarim",
            title = "La Sombra de la Guerra",
            region = "Islas de la Sombra",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/hecarim.png",
            borderHex = "#C8AA6E",
            description = "Hecarim es una fusión espectral de hombre y bestia, condenado a arrollar las almas de los vivos..."
        ),
        AvatarItem(
            id = "heimerdinger",
            name = "Heimerdinger",
            title = "El Inventor Venerado",
            region = "Piltóver",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/heimerdinger.png",
            borderHex = "#C8AA6E",
            description = "El profesor Cecil B. Heimerdinger, un científico yordle excéntrico pero brillante, es considera..."
        ),
        AvatarItem(
            id = "hwei",
            name = "Hwei",
            title = "el Visionario",
            region = "Jonia",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/hwei.png",
            borderHex = "#8A2BE2",
            description = "Hwei es un pintor melancólico de Jonia que crea arte deslumbrante para confrontar a los criminales y consolar a sus víctimas."
        ),
        AvatarItem(
            id = "irelia",
            name = "Irelia",
            title = "la Danza de las Cuchillas",
            region = "Jonia",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/irelia.png",
            borderHex = "#C8AA6E",
            description = "La ocupación noxiana de Jonia produjo mucho héroes, pero ninguno más improbable que la joven Ir..."
        ),
        AvatarItem(
            id = "janna",
            name = "Janna",
            title = "La Furia de la Tormenta",
            region = "Runaterra",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/janna.png",
            borderHex = "#C8AA6E",
            description = "Janna, armada con el poder de los vendavales de Runaterra, es un misterioso espíritu elemental ..."
        ),
        AvatarItem(
            id = "jarvan_iv",
            name = "Jarvan IV",
            title = "El Ejemplo de Demacia",
            region = "Demacia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/jarvan_iv.png",
            borderHex = "#C8AA6E",
            description = "El príncipe Jarvan, descendiente de la dinastía Escudo de Luz, es heredero al trono de Demacia...."
        ),
        AvatarItem(
            id = "jax",
            name = "Jax",
            title = "El Maestro de Armas",
            region = "Runaterra",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/jax.png",
            borderHex = "#C8AA6E",
            description = "Incomparable en su habilidad con armas especiales y su mordaz sarcasmo, Jax es el último maestr..."
        ),
        AvatarItem(
            id = "jayce",
            name = "Jayce",
            title = "El Defensor del Mañana",
            region = "Piltóver",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/jayce.png",
            borderHex = "#C8AA6E",
            description = "Jayce es un brillante inventor que ha jurado defender con su vida a Piltover y a su irreductibl..."
        ),
        AvatarItem(
            id = "jhin",
            name = "Jhin",
            title = "El Virtuoso",
            region = "Jonia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/jhin.png",
            borderHex = "#C8AA6E",
            description = "Jhin es un meticuloso criminal psicópata que ve el asesinato como arte. Otrora prisionero jonio..."
        ),
        AvatarItem(
            id = "jinx",
            name = "Jinx",
            title = "La Bala Perdida",
            region = "Zaun",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/jinx.png",
            borderHex = "#C8AA6E",
            description = "Jinx, una criminal perturbada e impulsiva de Zaun, vive para sembrar el caos sin importarle las..."
        ),
        AvatarItem(
            id = "k_sante",
            name = "K'Sante",
            title = "el Orgullo de Nazumah",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/k_sante.png",
            borderHex = "#C8AA6E",
            description = "K'Sante, un guerrero desafiante y valiente, lucha contra gigantescas bestias y despiadados Asce..."
        ),
        AvatarItem(
            id = "kai_sa",
            name = "Kai'Sa",
            title = "La Hija del Vacío",
            region = "Runaterra",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/kai_sa.png",
            borderHex = "#C8AA6E",
            description = "Reclamada por el Vacío cuando era solo una niña, Kai'Sa logró sobrevivir por pura tenacidad y f..."
        ),
        AvatarItem(
            id = "kalista",
            name = "Kalista",
            title = "El Espíritu de la Venganza",
            region = "Islas de la Sombra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/kalista.png",
            borderHex = "#C8AA6E",
            description = "Kalista, un espectro de cólera y castigo, es el inmortal espíritu de la venganza, una pesadilla..."
        ),
        AvatarItem(
            id = "karma",
            name = "Karma",
            title = "La Iluminada",
            region = "Jonia",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/karma.png",
            borderHex = "#C8AA6E",
            description = "No hay mortal que ejemplifique las tradiciones espirituales de Jonia mejor que Karma. Es la per..."
        ),
        AvatarItem(
            id = "kassadin",
            name = "Kassadin",
            title = "El Caminante del Vacío",
            region = "El Vacío",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/kassadin.png",
            borderHex = "#C8AA6E",
            description = "Dejando tras sí una huella ardiente por los lugares más oscuros del mundo, Kassadin sabe que su..."
        ),
        AvatarItem(
            id = "katarina",
            name = "Katarina",
            title = "La Cuchilla Siniestra",
            region = "Noxus",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/champions/katarina.png",
            borderHex = "#C8AA6E",
            description = "Con un juicio decisivo y letal en el combate, Katarina es una de las mejores asesinas noxianas...."
        ),
        AvatarItem(
            id = "kayle",
            name = "Kayle",
            title = "la Justa",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/kayle.png",
            borderHex = "#C8AA6E",
            description = "Kayle, nacida de un Aspecto de Targon en el punto álgido de las Guerras Rúnicas, honró el legad..."
        ),
        AvatarItem(
            id = "kayn",
            name = "Kayn",
            title = "la Guadaña de las Sombras",
            region = "Jonia",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/kayn.png",
            borderHex = "#C8AA6E",
            description = "Shieda Kayn, un practicante sin par de la mortífera magia sombría, lucha por alcanzar su verdad..."
        ),
        AvatarItem(
            id = "kennen",
            name = "Kennen",
            title = "El Corazón de la Tempestad",
            region = "Jonia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/kennen.png",
            borderHex = "#C8AA6E",
            description = "Más allá de ser el rápido encargado de mantener el equilibrio en Jonia, Kennen es también el ún..."
        ),
        AvatarItem(
            id = "kha_zix",
            name = "Kha'Zix",
            title = "El Saqueador del Vacío",
            region = "El Vacío",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/kha_zix.png",
            borderHex = "#C8AA6E",
            description = "El Vacío crece y el Vacío se adapta; verdades que son más evidentes en Kha'Zix que en ningún ot..."
        ),
        AvatarItem(
            id = "kindred",
            name = "Kindred",
            title = "Los Cazadores Eternos",
            region = "Runaterra",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/kindred.png",
            borderHex = "#C8AA6E",
            description = "Divididos, pero nunca separados, Kindred representan las dos esencias de la muerte. El arco de ..."
        ),
        AvatarItem(
            id = "kog_maw",
            name = "Kog'Maw",
            title = "La Boca del Abismo",
            region = "Runaterra",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/champions/kog_maw.png",
            borderHex = "#C8AA6E",
            description = "Escupido de una incursión deteriorada del Vacío en los yermos de Icathia, Kog'Maw es una criatu..."
        ),
        AvatarItem(
            id = "lee_sin",
            name = "Lee Sin",
            title = "El Monje Ciego",
            region = "Jonia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/lee_sin.png",
            borderHex = "#C8AA6E",
            description = "Lee Sin, maestro de las artes marciales ancestrales de Jonia, es un luchador con principios que..."
        ),
        AvatarItem(
            id = "leona",
            name = "Leona",
            title = "El Amanecer Radiante",
            region = "Targon",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/leona.png",
            borderHex = "#C8AA6E",
            description = "Imbuida del fuego del sol, Leona es una guerrera sagrada de los Solari que defiende el Monte Ta..."
        ),
        AvatarItem(
            id = "lillia",
            name = "Lillia",
            title = "el Tímido Florecer",
            region = "Jonia",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/lillia.png",
            borderHex = "#C8AA6E",
            description = "Lillia es una tímida cervatilla feérica que merodea a su antojo por los bosques jonios. Se ocul..."
        ),
        AvatarItem(
            id = "lissandra",
            name = "Lissandra",
            title = "La Bruja de Hielo",
            region = "Freljord",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/lissandra.png",
            borderHex = "#C8AA6E",
            description = "La magia de Lissandra convierte el poder del hielo en algo oscuro y terrible. Con la fuerza de ..."
        ),
        AvatarItem(
            id = "lucian",
            name = "Lucian",
            title = "El Destello Purificador",
            region = "Demacia",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/lucian.png",
            borderHex = "#C8AA6E",
            description = "Lucian, antiguo Centinela de la Luz, es un sombrío cazador de espíritus eternos a los que persi..."
        ),
        AvatarItem(
            id = "lulu",
            name = "Lulu",
            title = "El Hada Hechicera",
            region = "Ciudad de Bandle",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/lulu.png",
            borderHex = "#C8AA6E",
            description = "Lulu, la yordle maga, es conocida por invocar ilusiones oníricas y criaturas imaginarias en sus..."
        ),
        AvatarItem(
            id = "lux",
            name = "Lux",
            title = "La Dama Luminosa",
            region = "Demacia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/lux.png",
            borderHex = "#C8AA6E",
            description = "Luxanna Crownguard procede de Demacia, un reino insular en el que las habilidades mágicas se ob..."
        ),
        AvatarItem(
            id = "malphite",
            name = "Malphite",
            title = "El Fragmento del Monolito",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/malphite.png",
            borderHex = "#C8AA6E",
            description = "Como una criatura enorme de piedra viviente, a Malphite le cuesta imponer el orden en un mundo ..."
        ),
        AvatarItem(
            id = "maokai",
            name = "Maokai",
            title = "El Treant Retorcido",
            region = "Islas de la Sombra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/maokai.png",
            borderHex = "#C8AA6E",
            description = "Maokai es un imponente y feroz treant que lucha contra los horrores antinaturales de las Islas ..."
        ),
        AvatarItem(
            id = "master_yi",
            name = "Maestro Yi",
            title = "El Espadachín Wuju",
            region = "Jonia",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/master_yi.png",
            borderHex = "#C8AA6E",
            description = "Maestro Yi ha atemperado su cuerpo y agudizado su mente, de modo que el pensamiento y la acción..."
        ),
        AvatarItem(
            id = "mel",
            name = "Mel",
            title = "la Consejera Dorada",
            region = "Runaterra",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/offline_images/19acee68d94821c4b6fed292f63821cc.webp",
            borderHex = "#C8AA6E",
            description = "Mel domina las líneas con magia solar áurea, proveyendo daño a distancia, escudos reflectantes ..."
        ),
        AvatarItem(
            id = "milio",
            name = "Milio",
            title = "la Llama Serena",
            region = "Runaterra",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/champions/milio.png",
            borderHex = "#C8AA6E",
            description = "Milio es un jovencito amable de Ixtal que, a pesar de su corta edad, ha conseguido dominar el a..."
        ),
        AvatarItem(
            id = "miss_fortune",
            name = "Miss Fortune",
            title = "La Cazarrecompensas",
            region = "Aguas Esturbias",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/miss_fortune.png",
            borderHex = "#C8AA6E",
            description = "Sarah Fortune se cuenta sin duda entre los capitanes más conocidos de Aguas Estancadas y es tan..."
        ),
        AvatarItem(
            id = "mordekaiser",
            name = "Mordekaiser",
            title = "La Pesadilla de Hierro",
            region = "Runaterra",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/mordekaiser.png",
            borderHex = "#C8AA6E",
            description = "Mordekaiser es un sanguinario señor de la guerra proveniente de tiempos olvidados al que los si..."
        ),
        AvatarItem(
            id = "morgana",
            name = "Morgana",
            title = "la Caída",
            region = "Demacia",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/morgana.png",
            borderHex = "#C8AA6E",
            description = "En vistas del conflicto entre su naturaleza celestial y su naturaleza mortal, Morgana decidió a..."
        ),
        AvatarItem(
            id = "nami",
            name = "Nami",
            title = "La Invocadora de Mareas",
            region = "Aguas Esturbias",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/nami.png",
            borderHex = "#C8AA6E",
            description = "Nami, una joven y testaruda vastaya marina, fue la primera de la tribu marai en abandonar las o..."
        ),
        AvatarItem(
            id = "nasus",
            name = "Nasus",
            title = "El Guardián de las Arenas",
            region = "Shurima",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/nasus.png",
            borderHex = "#C8AA6E",
            description = "Nasus es un imponente ser Ascendido con cabeza de chacal procedente de la antigua Shurima; una ..."
        ),
        AvatarItem(
            id = "nautilus",
            name = "Nautilus",
            title = "El Titán Abisal",
            region = "Aguas Esturbias",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/nautilus.png",
            borderHex = "#C8AA6E",
            description = "El gigante acorazado Nautilus, una leyenda solitaria tan antigua como los pecios de Aguas Estan..."
        ),
        AvatarItem(
            id = "nidalee",
            name = "Nidalee",
            title = "La Cazadora Bestial",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/nidalee.png",
            borderHex = "#C8AA6E",
            description = "Criada en lo más profundo de la jungla, Nidalee es una rastreadora maestra que puede convertirs..."
        ),
        AvatarItem(
            id = "nilah",
            name = "Nilah",
            title = "la Alegría Desatada",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/nilah.png",
            borderHex = "#C8AA6E",
            description = "Nilah es una guerrera ascética proveniente de tierras lejanas que busca a los adversarios más m..."
        ),
        AvatarItem(
            id = "nocturne",
            name = "Nocturne",
            title = "La Pesadilla Eterna",
            region = "Runaterra",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/nocturne.png",
            borderHex = "#C8AA6E",
            description = "El ser conocido como Nocturne es una fusión demoníaca extraída de las pesadillas que acechan a ..."
        ),
        AvatarItem(
            id = "norra",
            name = "Norra",
            title = "la Maestra de los Portales",
            region = "Runaterra",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/offline_images/87bfb0c8cb1949e183b6262556f5f12d.jpg",
            borderHex = "#C8AA6E",
            description = "Norra abre fisuras hacia la Ciudad de Bandle para dotar de hipermovilidad a su equipo y desorie..."
        ),
        AvatarItem(
            id = "nunu_willump",
            name = "Nunu y Willump",
            title = "Un Niño y su Yeti",
            region = "Freljord",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/nunu_willump.png",
            borderHex = "#C8AA6E",
            description = "Había una vez un niño que quería acabar con un temible monstruo para demostrar que era un héroe..."
        ),
        AvatarItem(
            id = "olaf",
            name = "Olaf",
            title = "El Berserker",
            region = "Freljord",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/champions/olaf.png",
            borderHex = "#C8AA6E",
            description = "Olaf porta sus hachas de forma que es una fuerza imparable de destrucción que no quiere nada, s..."
        ),
        AvatarItem(
            id = "orianna",
            name = "Orianna",
            title = "La Dama Mecánica",
            region = "Piltóver",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/orianna.png",
            borderHex = "#C8AA6E",
            description = "Orianna, antaño una chica curiosa de carne y hueso, es ahora una maravilla tecnológica compuest..."
        ),
        AvatarItem(
            id = "ornn",
            name = "Ornn",
            title = "Las Llamas de la Forja",
            region = "Freljord",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/ornn.png",
            borderHex = "#C8AA6E",
            description = "Ornn es el espíritu de Freljord de la forja y la artesanía. Trabaja en la soledad de una enorme..."
        ),
        AvatarItem(
            id = "pantheon",
            name = "Pantheon",
            title = "la Lanza Inquebrantable",
            region = "Targon",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/pantheon.png",
            borderHex = "#C8AA6E",
            description = "Tiempo atrás, Atreus había albergado al Aspecto de la Guerra en su interior contra su voluntad,..."
        ),
        AvatarItem(
            id = "poppy",
            name = "Poppy",
            title = "La Guardiana del Martillo",
            region = "Runaterra",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/poppy.png",
            borderHex = "#C8AA6E",
            description = "En Runaterra hay un gran número de campeones valerosos, pero muy pocos son tan tenaces como Pop..."
        ),
        AvatarItem(
            id = "pyke",
            name = "Pyke",
            title = "el Destripador de los Puertos",
            region = "Aguas Esturbias",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/pyke.png",
            borderHex = "#C8AA6E",
            description = "A Pyke, un conocido arponero de los muelles del matadero de Aguas Estancadas, le esperaba la mu..."
        ),
        AvatarItem(
            id = "rakan",
            name = "Rakan",
            title = "El Encantador",
            region = "Jonia",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/rakan.png",
            borderHex = "#C8AA6E",
            description = "Con un carácter tan impulsivo como encantador, Rakan es un infame alborotador vastaya y el mejo..."
        ),
        AvatarItem(
            id = "rammus",
            name = "Rammus",
            title = "El Armadurillo",
            region = "Shurima",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/rammus.png",
            borderHex = "#C8AA6E",
            description = "Idealizado por muchos, ignorado por otros, inexplicable para todos, Rammus, el extraño ser, es ..."
        ),
        AvatarItem(
            id = "rell",
            name = "Rell",
            title = "la Dama de Hierro",
            region = "Runaterra",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/rell.png",
            borderHex = "#C8AA6E",
            description = "Rell, producto de crueles experimentos a manos de la Rosa Negra, es ahora una intrépida arma vi..."
        ),
        AvatarItem(
            id = "renekton",
            name = "Renekton",
            title = "El Carnicero de las Arenas",
            region = "Shurima",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/renekton.png",
            borderHex = "#C8AA6E",
            description = "Renekton es una terrorífica criatura Ascendida movida por la ira y procedente de los desiertos ..."
        ),
        AvatarItem(
            id = "rengar",
            name = "Rengar",
            title = "El Acechador Orgulloso",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/rengar.png",
            borderHex = "#C8AA6E",
            description = "Rengar es un feroz cazador de trofeos vastaya que vive por el placer de perseguir y asesinar cr..."
        ),
        AvatarItem(
            id = "riven",
            name = "Riven",
            title = "La Exiliada",
            region = "Noxus",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/riven.png",
            borderHex = "#C8AA6E",
            description = "Antaño maestra de la espada de las huestes de Noxus, Riven es una expatriada en la tierra que p..."
        ),
        AvatarItem(
            id = "rumble",
            name = "Rumble",
            title = "La Amenaza Mecánica",
            region = "Runaterra",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/rumble.png",
            borderHex = "#C8AA6E",
            description = "Rumble es un joven inventor con temperamento. Utilizando nada más que sus propias manos y un mo..."
        ),
        AvatarItem(
            id = "ryze",
            name = "Ryze",
            title = "El Hechicero Rúnico",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/ryze.png",
            borderHex = "#C8AA6E",
            description = "Ryze es considerado uno de los hechiceros con más experiencia de Runaterra. El archimago ancest..."
        ),
        AvatarItem(
            id = "samira",
            name = "Samira",
            title = "la Rosa del Desierto",
            region = "Noxus",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/samira.png",
            borderHex = "#C8AA6E",
            description = "Samira mira de cara a la muerte con una confianza férrea y busca emociones extremas allá donde ..."
        ),
        AvatarItem(
            id = "senna",
            name = "Senna",
            title = "la Redentora",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/senna.png",
            borderHex = "#C8AA6E",
            description = "Maldita desde que era apenas una niña y perseguida por la Niebla Negra, Senna se unió a una ord..."
        ),
        AvatarItem(
            id = "seraphine",
            name = "Seraphine",
            title = "la Cantante Soñadora",
            region = "Piltóver",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/seraphine.png",
            borderHex = "#C8AA6E",
            description = "Seraphine, de padres zaunitas, nació en Piltover y es capaz de escuchar las almas de los demás...."
        ),
        AvatarItem(
            id = "sett",
            name = "Sett",
            title = "el Jefe",
            region = "Jonia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/sett.png",
            borderHex = "#C8AA6E",
            description = "Sett es una prominente figura en los emergentes círculos criminales jonios, que aseguró su posi..."
        ),
        AvatarItem(
            id = "shen",
            name = "Shen",
            title = "El Ojo del Crepúsculo",
            region = "Jonia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/shen.png",
            borderHex = "#C8AA6E",
            description = "Shen, el Ojo del Crepúsculo, es el cabecilla de los sigilosos guerreros de Jonia conocidos como..."
        ),
        AvatarItem(
            id = "shyvana",
            name = "Shyvana",
            title = "La Medio Dragón",
            region = "Demacia",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/shyvana.png",
            borderHex = "#C8AA6E",
            description = "Shyvana es una criatura con la magia de un fragmento de runa incandescente alojada en lo más pr..."
        ),
        AvatarItem(
            id = "singed",
            name = "Singed",
            title = "El Químico Loco",
            region = "Zaun",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/singed.png",
            borderHex = "#C8AA6E",
            description = "Singed es un alquimista zaunita de intelecto sin igual que se ha entregado en cuerpo y alma a s..."
        ),
        AvatarItem(
            id = "sion",
            name = "Sion",
            title = "El Coloso no Muerto",
            region = "Noxus",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/sion.png",
            borderHex = "#C8AA6E",
            description = "Sion fue un héroe de guerra en el pasado y los noxianos lo veneraban porque había arrebatado la..."
        ),
        AvatarItem(
            id = "sivir",
            name = "Sivir",
            title = "La Señora de la Batalla",
            region = "Shurima",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/sivir.png",
            borderHex = "#C8AA6E",
            description = "Sivir es una afamada buscadora de tesoros y capitana mercenaria que se gana la vida en el desie..."
        ),
        AvatarItem(
            id = "skarner",
            name = "Skarner",
            title = "el Soberano Primigenio",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/skarner.png",
            borderHex = "#C8AA6E",
            description = "A Skarner, el colosal brackern ancestral, se le rinde culto en Ixtal como uno de los fundadores..."
        ),
        AvatarItem(
            id = "smolder",
            name = "Smolder",
            title = "el Heredero de las Llamas",
            region = "Runaterra",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/smolder.png",
            borderHex = "#C8AA6E",
            description = "Oculto entre los escarpados acantilados de la frontera noxiana y bajo la atenta mirada de su ma..."
        ),
        AvatarItem(
            id = "sona",
            name = "Sona",
            title = "La Virtuosa de las Cuerdas",
            region = "Demacia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/sona.png",
            borderHex = "#C8AA6E",
            description = "Sona es la artista más virtuosa de Demacia con el etwahl de cuerda y solo se comunica a través ..."
        ),
        AvatarItem(
            id = "soraka",
            name = "Soraka",
            title = "La Hija de las Estrellas",
            region = "Targon",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/champions/soraka.png",
            borderHex = "#C8AA6E",
            description = "Soraka, una nómada proveniente de las dimensiones celestiales más allá del monte Targon, dejó a..."
        ),
        AvatarItem(
            id = "swain",
            name = "Swain",
            title = "el Gran General de Noxus",
            region = "Noxus",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/swain.png",
            borderHex = "#C8AA6E",
            description = "Jericho Swain es el visionario líder de Noxus, una nación expansionista que solo venera la fuer..."
        ),
        AvatarItem(
            id = "syndra",
            name = "Syndra",
            title = "La Soberana Oscura",
            region = "Jonia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/syndra.png",
            borderHex = "#C8AA6E",
            description = "Syndra es una temible maga jonia con un poder increíble a su disposición. De niña, inquietó a l..."
        ),
        AvatarItem(
            id = "taliyah",
            name = "Taliyah",
            title = "La Tejedora de Piedra",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/taliyah.png",
            borderHex = "#C8AA6E",
            description = "Taliyah es una hechicera nómada de Shurima desgarrada entre la curiosidad de una adolescente y ..."
        ),
        AvatarItem(
            id = "talon",
            name = "Talon",
            title = "La Sombra de la Espada",
            region = "Noxus",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/talon.png",
            borderHex = "#C8AA6E",
            description = "Talon es el cuchillo de la oscuridad, un asesino despiadado capaz de atacar sin previo aviso y ..."
        ),
        AvatarItem(
            id = "teemo",
            name = "Teemo",
            title = "El Explorador Veloz",
            region = "Ciudad de Bandle",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/teemo.png",
            borderHex = "#C8AA6E",
            description = "Sin inmutarse siquiera por los obstáculos más peligrosos y amenazantes, Teemo explora el mundo ..."
        ),
        AvatarItem(
            id = "thresh",
            name = "Thresh",
            title = "El Carcelero Implacable",
            region = "Islas de la Sombra",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/thresh.png",
            borderHex = "#C8AA6E",
            description = "Thresh, un ser sádico y astuto, es un ambicioso y trastornado espíritu de las Islas de la Sombr..."
        ),
        AvatarItem(
            id = "tristana",
            name = "Tristana",
            title = "La Artillera Yordle",
            region = "Ciudad de Bandle",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/tristana.png",
            borderHex = "#C8AA6E",
            description = "Pese a que la mayoría de los yordles canalizan sus energías hacia el descubrimiento, la invenci..."
        ),
        AvatarItem(
            id = "tryndamere",
            name = "Tryndamere",
            title = "El Rey Bárbaro",
            region = "Freljord",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/tryndamere.png",
            borderHex = "#C8AA6E",
            description = "Impulsado por una furia y una rabia desenfrenadas, Tryndamere solía abrirse paso por Freljord d..."
        ),
        AvatarItem(
            id = "twisted_fate",
            name = "Twisted Fate",
            title = "El Maestro de las Cartas",
            region = "Aguas Esturbias",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/twisted_fate.png",
            borderHex = "#C8AA6E",
            description = "Twisted Fate es un tahúr y timador de mala reputación que ha viajado por buena parte del mundo ..."
        ),
        AvatarItem(
            id = "twitch",
            name = "Twitch",
            title = "La Rata Apestada",
            region = "Zaun",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/twitch.png",
            borderHex = "#C8AA6E",
            description = "Rata de la peste zaunita por nacimiento y apasionado experto de la suciedad por afición, a Twit..."
        ),
        AvatarItem(
            id = "urgot",
            name = "Urgot",
            title = "la Pesadilla Mecánica",
            region = "Zaun",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/urgot.png",
            borderHex = "#C8AA6E",
            description = "Urgot, antiguo verdugo noxiano, fue traicionado por el imperio por el que había matado a tantos..."
        ),
        AvatarItem(
            id = "varus",
            name = "Varus",
            title = "La Flecha del Castigo",
            region = "Jonia",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/varus.png",
            borderHex = "#C8AA6E",
            description = "Como uno de los antiguos oscuros Varus era un asesino despiadado al que le encantaba torturar a..."
        ),
        AvatarItem(
            id = "vayne",
            name = "Vayne",
            title = "La Cazadora Noctívaga",
            region = "Demacia",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/champions/vayne.png",
            borderHex = "#C8AA6E",
            description = "Shauna Vayne es una cazadora de monstruos demaciana letal y despiadada que ha dedicado su vida ..."
        ),
        AvatarItem(
            id = "veigar",
            name = "Veigar",
            title = "El Pequeño Maestro del Mal",
            region = "Ciudad de Bandle",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/champions/veigar.png",
            borderHex = "#C8AA6E",
            description = "Entusiasta maestro de la magia negra, Veigar ha hecho suyos poderes a los que pocos mortales se..."
        ),
        AvatarItem(
            id = "vel_koz",
            name = "Vel'Koz",
            title = "El Ojo del Vacío",
            region = "El Vacío",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/vel_koz.png",
            borderHex = "#C8AA6E",
            description = "No es seguro que Vel'Koz sea el primer ente del Vacío que ha aparecido en Runaterra, pero no ha..."
        ),
        AvatarItem(
            id = "vex",
            name = "Vex",
            title = "la Lúgubre",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/vex.png",
            borderHex = "#C8AA6E",
            description = "En el oscuro corazón de las Islas de la Sombra, una yordle solitaria atraviesa arduamente la br..."
        ),
        AvatarItem(
            id = "vi",
            name = "Vi",
            title = "La Agente de Piltover",
            region = "Piltóver",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/vi.png",
            borderHex = "#C8AA6E",
            description = "Otrora delincuente en las perversas calles de Zaun, Vi es una mujer impulsiva e imponente con m..."
        ),
        AvatarItem(
            id = "viego",
            name = "Viego",
            title = "el Rey Arruinado",
            region = "Islas de la Sombra",
            rarity = "Mítico",
            imageUrl = "file:///android_asset/champions/viego.png",
            borderHex = "#C8AA6E",
            description = "Viego, soberano de un reino ya olvidado, murió hace más de mil años cuando su intento por devol..."
        ),
        AvatarItem(
            id = "viktor",
            name = "Viktor",
            title = "El Heraldo de las Máquinas",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/viktor.png",
            borderHex = "#C8AA6E",
            description = "Viktor, el heraldo de una nueva era tecnológica, ha consagrado su vida al progreso de la humani..."
        ),
        AvatarItem(
            id = "vladimir",
            name = "Vladimir",
            title = "El Segador Carmesí",
            region = "Noxus",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/vladimir.png",
            borderHex = "#C8AA6E",
            description = "Un demonio con sed de sangre mortal, Vladimir ha influido en el destino de Noxus desde los prim..."
        ),
        AvatarItem(
            id = "volibear",
            name = "Volibear",
            title = "la Tormenta Incesante",
            region = "Freljord",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/volibear.png",
            borderHex = "#C8AA6E",
            description = "Para aquellos que aún lo veneran, Volibear es la encarnación de la tormenta. Destructivo, salva..."
        ),
        AvatarItem(
            id = "warwick",
            name = "Warwick",
            title = "la Ira Descontrolada de Zaun",
            region = "Zaun",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/warwick.png",
            borderHex = "#C8AA6E",
            description = "Warwick es un monstruo que acecha en los oscuros callejones de Zaun. Transformado por atroces e..."
        ),
        AvatarItem(
            id = "wukong",
            name = "Wukong",
            title = "El Rey Mono",
            region = "Jonia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/wukong.png",
            borderHex = "#C8AA6E",
            description = "Wukong es un embaucador vastaya que se vale de su fuerza, agilidad e inteligencia para confundi..."
        ),
        AvatarItem(
            id = "xayah",
            name = "Xayah",
            title = "La Rebelde",
            region = "Jonia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/xayah.png",
            borderHex = "#C8AA6E",
            description = "Mortal y precisa, Xayah es una vastaya revolucionaria que libra una guerra personal para salvar..."
        ),
        AvatarItem(
            id = "xin_zhao",
            name = "Xin Zhao",
            title = "El Senescal de Demacia",
            region = "Demacia",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/xin_zhao.png",
            borderHex = "#C8AA6E",
            description = "Xin Zhao es un guerreo decidido y valiente, leal a la dinastía Escudo de Luz. En su momento fue..."
        ),
        AvatarItem(
            id = "yasuo",
            name = "Yasuo",
            title = "La Espada sin Honor",
            region = "Jonia",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/yasuo.png",
            borderHex = "#C8AA6E",
            description = "Yasuo, un intrépido jonio con una fuerza de voluntad inquebrantable, es también un hábil espada..."
        ),
        AvatarItem(
            id = "yone",
            name = "Yone",
            title = "el Imperecedero",
            region = "Jonia",
            rarity = "Legendario",
            imageUrl = "file:///android_asset/champions/yone.png",
            borderHex = "#C8AA6E",
            description = "En vida, fue Yone, hermanastro de Yasuo y pupilo de renombre en la escuela de esgrima de su ald..."
        ),
        AvatarItem(
            id = "yunara",
            name = "Yunara",
            title = "la Tejedora de Penumbra",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/offline_images/bb9667bedb5e250145dc7830f2e846c1.webp",
            borderHex = "#C8AA6E",
            description = "Yunara es una tiradora letal que enlaza a sus víctimas con hilos invisibles de daño verdadero y..."
        ),
        AvatarItem(
            id = "yuumi",
            name = "Yuumi",
            title = "la Gata Mágica",
            region = "Ciudad de Bandle",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/yuumi.png",
            borderHex = "#C8AA6E",
            description = "Yuumi, una gata mágica de Ciudad de Bandle, fue antaño la compañera de una hechicera yordle, No..."
        ),
        AvatarItem(
            id = "zed",
            name = "Zed",
            title = "El Maestro de las Sombras",
            region = "Jonia",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/zed.png",
            borderHex = "#C8AA6E",
            description = "Zed, despiadado y nada compasivo, es el líder de la Orden de la Sombra, una organización que él..."
        ),
        AvatarItem(
            id = "zeri",
            name = "Zeri",
            title = "La Chispa de Zaun",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/zeri.png",
            borderHex = "#C8AA6E",
            description = "Una joven testaruda y llena de vida de la clase trabajadora de Zaun. Zeri canaliza su magia elé..."
        ),
        AvatarItem(
            id = "ziggs",
            name = "Ziggs",
            title = "El Experto en Hexplosivos",
            region = "Zaun",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/ziggs.png",
            borderHex = "#C8AA6E",
            description = "Gran amante de las bombas grandes y las mechas cortas, el yordle Ziggs es una explosiva fuerza ..."
        ),
        AvatarItem(
            id = "zilean",
            name = "Zilean",
            title = "El Guardián del Tiempo",
            region = "Runaterra",
            rarity = "Raro",
            imageUrl = "file:///android_asset/champions/zilean.png",
            borderHex = "#C8AA6E",
            description = "Zilean, el que un día fuera un poderoso mago de Icathia, se obsesionó con el paso del tiempo tr..."
        ),
        AvatarItem(
            id = "zoe",
            name = "Zoe",
            title = "el Aspecto del Crepúsculo",
            region = "Targon",
            rarity = "Épico",
            imageUrl = "file:///android_asset/champions/zoe.png",
            borderHex = "#C8AA6E",
            description = "Como personificación de la travesura, de la imaginación y del cambio, Zoe es la mensajera cósmi..."
        ),
        AvatarItem(
            id = "zyra",
            name = "Zyra",
            title = "La Dama de Espinas",
            region = "Runaterra",
            rarity = "Común",
            imageUrl = "file:///android_asset/champions/zyra.png",
            borderHex = "#C8AA6E",
            description = "Nacida gracias a un fenómeno sobrenatural hace mucho tiempo, Zyra encarna la ira de la naturale..."
        ),
    )

    fun getAvatarById(id: String): AvatarItem {
        return avatars.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT_AVATAR
    }
}
