package com.example.util

import com.example.model.LaneRole

/** Editorial enemy and ally relationships across the map, retrieved 2026-10-03.
 * No matchup win rates are inferred. See docs/expanded-matchup-reference-audit.json.
 */
object ExpandedMatchupReferences {
    val byChampion: Map<String, Map<LaneRole, MatchupRoleResult>> = mapOf(
        "aatrox" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Sett", "Urgot", "Nasus", "Skarner"), listOf("Pantheon", "Irelia", "Fiora", "Riven", "Camille"), listOf("Kennen", "Renekton", "Darius", "Riven", "Ornn")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Shyvana", "Amumu", "Jarvan IV", "Vi", "Lee Sin"), listOf("Rammus", "Rengar", "Hecarim", "Evelynn", "Nunu y Willump"), listOf("Gragas", "Nunu y Willump", "Rammus", "Amumu", "Warwick")),
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Gragas", "Yasuo", "Galio", "Orianna"), listOf("Ekko", "Talon", "Zoe", "Ahri", "Twisted Fate"), listOf("Pantheon", "Vex", "Ekko", "Galio", "Annie")),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Ezreal", "Miss Fortune", "Jhin", "Caitlyn"), listOf("Tristana", "Lucian", "Xayah", "Varus", "Zeri"), listOf("Jhin", "Samira", "Kai'Sa", "Ashe", "Zeri")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nautilus", "Karma", "Soraka", "Swain", "Yuumi"), listOf("Janna", "Leona", "Braum", "Rakan", "Thresh"), listOf("Rakan", "Alistar", "Nami", "Thresh", "Janna"))
        ),
        "ahri" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kayle", "Kennen", "Renekton", "Yone", "Nasus"), listOf("Singed", "Camille", "Wukong", "Malphite", "Dr. Mundo"), listOf("Garen", "Jayce", "Shen", "Camille", "Malphite")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Shyvana", "Lee Sin", "Warwick", "Wukong"), listOf("Nunu y Willump", "Vi", "Talon", "Kha'Zix", "Xin Zhao"), listOf("Vi", "Lillia", "Volibear", "Hecarim", "Evelynn")),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Fizz", "Orianna", "Yone", "Zoe"), listOf("Pantheon", "Kassadin", "Annie", "Vladimir", "Zed"), listOf()),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Ezreal", "Lucian", "Varus", "Xayah"), listOf("Samira", "Draven", "Ashe", "Tristana", "Zeri"), listOf("Lucian", "Zeri", "Kai'Sa", "Tristana", "Ashe")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Yuumi", "Thresh", "Karma", "Lux"), listOf("Nautilus", "Braum", "Alistar", "Lulu", "Rell"), listOf("Rakan", "Nami", "Janna", "Lux", "Thresh"))
        ),
        "akali" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Gwen", "Tryndamere", "Malphite", "Kayle"), listOf("Singed", "Kennen", "Irelia", "Gragas", "Shen"), listOf("Kennen", "Pantheon", "Malphite", "Urgot", "Sett")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Shyvana", "Jarvan IV", "Rammus", "Fizz"), listOf("Kha'Zix", "Gragas", "Wukong", "Rengar", "Talon"), listOf("Nunu y Willump", "Vi", "Gragas", "Wukong", "Hecarim")),
            LaneRole.MID to MatchupRoleResult(listOf("Yone", "Ekko", "Lux", "Fizz", "Syndra"), listOf("Galio", "Kassadin", "Vex", "Twisted Fate", "Zed"), listOf("Galio", "Karma", "Sett", "Swain", "Zed")),
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Ezreal", "Tristana", "Varus", "Jhin"), listOf("Lucian", "Zeri", "Kai'Sa", "Samira", "Xayah"), listOf("Zeri", "Tristana", "Draven", "Ashe", "Varus")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Karma", "Lulu", "Nami", "Lux"), listOf("Rakan", "Janna", "Nautilus", "Alistar"), listOf("Rakan", "Nami", "Braum", "Nautilus", "Thresh"))
        ),
        "akshan" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Renekton", "Sett", "Garen", "Dr. Mundo", "Vladimir"), listOf("Wukong", "Sion", "Riven", "Jayce"), listOf("Jax", "Wukong", "Shen", "Camille", "Urgot")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Kayn", "Ekko", "Shyvana", "Rammus"), listOf("Nunu y Willump", "Vi", "Maestro Yi", "Volibear", "Wukong"), listOf("Xin Zhao", "Gragas", "Rammus", "Diana", "Kayn")),
            LaneRole.MID to MatchupRoleResult(listOf("Galio", "Ahri", "Diana", "Kassadin", "Orianna"), listOf("Yasuo", "Twisted Fate", "Vex", "Irelia", "Pantheon"), listOf("Galio", "Ziggs", "Veigar")),
            LaneRole.ADC to MatchupRoleResult(listOf("Zeri", "Caitlyn", "Kai'Sa", "Varus", "Samira"), listOf("Draven", "Jinx", "Miss Fortune", "Ashe", "Jhin"), listOf("Ashe", "Caitlyn", "Jinx", "Lucian", "Draven")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Soraka", "Lux", "Karma", "Pyke"), listOf("Rakan", "Thresh", "Janna", "Braum", "Sona"), listOf("Alistar", "Nami", "Thresh", "Karma", "Pyke"))
        ),
        "alistar" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Aatrox", "Darius", "Sett", "Nasus"), listOf("Singed", "Wukong", "Riven", "Urgot", "Fiora"), listOf("Camille", "Riven", "Sion", "Urgot", "Nasus")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Diana", "Jarvan IV", "Wukong", "Xin Zhao", "Rammus"), listOf("Olaf", "Vi", "Volibear", "Hecarim", "Lillia"), listOf("Ekko", "Xin Zhao", "Nunu y Willump", "Fiddlesticks", "Volibear")),
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Zoe", "Yasuo", "Veigar", "Syndra"), listOf("Vex", "Kassadin", "Jayce", "Vladimir", "Gragas"), listOf("Vex", "Orianna", "Ahri", "Yone", "Annie")),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Jhin", "Lucian", "Kai'Sa", "Varus"), listOf("Xayah", "Tristana", "Vayne", "Draven", "Ezreal"), listOf("Kai'Sa", "Samira", "Draven", "Jhin", "Tristana")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Blitzcrank", "Soraka", "Lux", "Yuumi"), listOf("Janna", "Lulu", "Rakan", "Braum", "Thresh"), listOf())
        ),
        "ambessa" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Yone", "Sion", "Ornn", "Sett"), listOf("Kennen", "Riven", "Camille", "Volibear", "Renekton"), listOf("Poppy", "Camille", "Darius", "Gwen", "Garen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Gwen", "Amumu", "Gragas", "Xin Zhao", "Wukong"), listOf("Fiddlesticks", "Evelynn", "Shyvana", "Hecarim", "Talon"), listOf("Rengar", "Xin Zhao", "Nunu y Willump", "Kha'Zix", "Vi")),
            LaneRole.MID to MatchupRoleResult(listOf("Jayce", "Lux", "Syndra", "Irelia", "Zed"), listOf("Ekko", "Vex", "Lissandra", "Annie", "Vladimir"), listOf("Kassadin", "Vladimir", "Zoe", "Galio", "Lissandra")),
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Jinx", "Zeri", "Caitlyn", "Kai'Sa"), listOf("Samira", "Draven", "Sivir", "Tristana", "Xayah"), listOf("Tristana", "Zeri", "Jhin", "Ashe", "Samira")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Pyke", "Yuumi", "Milio", "Maokai", "Seraphine"), listOf("Sona", "Braum", "Lulu", "Rakan", "Nami"), listOf("Soraka", "Thresh", "Maokai", "Leona", "Janna"))
        ),
        "amumu" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Malphite", "Dr. Mundo", "Kennen", "Nasus", "Sion"), listOf("Wukong", "Olaf", "Pantheon", "Sett", "Gwen"), listOf("Malphite", "Jax", "Fiora", "Urgot", "Volibear")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Kha'Zix", "Diana", "Graves", "Jax"), listOf("Xin Zhao", "Vi", "Kindred", "Nunu y Willump", "Lee Sin"), listOf("Wukong", "Ekko", "Rammus", "Fiddlesticks", "Nunu y Willump")),
            LaneRole.MID to MatchupRoleResult(listOf("Irelia", "Akali", "Katarina", "Swain", "Vex"), listOf("Galio", "Kassadin", "Yone", "Ekko", "Vladimir"), listOf("Vex", "Ekko", "Diana", "Akali", "Twisted Fate")),
            LaneRole.ADC to MatchupRoleResult(listOf("Jhin", "Kai'Sa", "Varus", "Nilah", "Caitlyn"), listOf("Draven", "Samira", "Xayah", "Zeri", "Sivir"), listOf("Xayah", "Samira", "Zeri", "Jinx", "Miss Fortune")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Karma", "Nautilus", "Lux", "Nami"), listOf("Rakan", "Alistar", "Janna", "Soraka", "Sona"), listOf("Nami", "Soraka", "Rakan", "Sona", "Pyke"))
        ),
        "annie" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Aatrox", "Kayle", "Riven", "Sion"), listOf("Garen", "Camille", "Darius", "Dr. Mundo", "Malphite"), listOf("Malphite", "Darius", "Sion", "Camille", "Sett")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Vi", "Diana", "Talon", "Lee Sin"), listOf("Olaf", "Gragas", "Xin Zhao", "Nunu y Willump", "Fiddlesticks"), listOf("Evelynn", "Warwick", "Jarvan IV", "Vi", "Rammus")),
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Vex", "Akshan", "Twisted Fate", "Katarina"), listOf("Fizz", "Ekko", "Kassadin", "Jayce", "Lux"), listOf("Galio", "Vex", "Pantheon", "Zed", "Fizz")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Zeri", "Samira", "Xayah", "Varus"), listOf("Lucian", "Kai'Sa", "Tristana", "Sivir", "Caitlyn"), listOf("Jhin", "Samira", "Zeri", "Sivir", "Ashe")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Thresh", "Nautilus", "Soraka", "Pyke", "Yuumi"), listOf("Braum", "Nami", "Leona", "Janna", "Maokai"), listOf("Alistar", "Leona", "Rakan", "Pyke", "Blitzcrank"))
        ),
        "ashe" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Akali", "Aatrox", "Dr. Mundo", "Renekton"), listOf("Shen", "Irelia", "Camille", "Gwen", "Garen"), listOf("Singed", "Gragas", "Irelia", "Sett", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Wukong", "Diana", "Shyvana", "Hecarim"), listOf("Maestro Yi", "Nunu y Willump", "Evelynn", "Olaf", "Rammus"), listOf("Nunu y Willump", "Evelynn", "Vi", "Volibear", "Talon")),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Jayce", "Zoe", "Syndra", "Orianna"), listOf("Kassadin", "Katarina", "Vex", "Aurelion Sol", "Twisted Fate"), listOf("Zoe", "Jayce", "Diana", "Kassadin", "Annie")),
            LaneRole.ADC to MatchupRoleResult(listOf("Caitlyn", "Zeri", "Xayah", "Sivir", "Kalista"), listOf("Samira", "Tristana", "Draven", "Twitch", "Nilah"), listOf("Zeri", "Jhin", "Draven", "Nilah", "Seraphine")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Nami", "Karma", "Lux", "Soraka"), listOf("Rakan", "Blitzcrank", "Nautilus", "Braum", "Pyke"), listOf("Soraka", "Nami", "Janna", "Braum", "Seraphine"))
        ),
        "aurelion_sol" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Dr. Mundo", "Ornn", "Sion", "Teemo"), listOf("Gwen", "Sett", "Camille", "Gragas", "Tryndamere"), listOf("Darius", "Urgot", "Wukong", "Sett", "Gragas")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Kha'Zix", "Amumu", "Lillia", "Shyvana"), listOf("Kindred", "Maestro Yi", "Ekko", "Fiddlesticks", "Evelynn"), listOf("Vi", "Evelynn", "Volibear", "Rammus", "Rengar")),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Vladimir", "Galio", "Syndra", "Veigar"), listOf("Fizz", "Yone", "Ahri", "Kassadin", "Zed"), listOf()),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Zeri", "Varus", "Sivir", "Ezreal"), listOf("Lucian", "Tristana", "Jinx", "Draven", "Ashe"), listOf("Jinx", "Ashe", "Samira", "Xayah", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Karma", "Lulu", "Nautilus", "Lux", "Yuumi"), listOf("Janna", "Soraka", "Rakan", "Nami", "Leona"), listOf("Leona", "Pyke", "Janna", "Braum", "Rakan"))
        ),
        "aurora" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Dr. Mundo", "Renekton", "Kennen", "Gnar"), listOf("Fiora", "Singed", "Poppy", "Nasus", "Gwen"), listOf("Poppy", "Riven", "Camille", "Sion", "Shen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Amumu", "Evelynn", "Vi", "Shyvana", "Nocturne"), listOf("Fiddlesticks", "Kindred", "Talon", "Rengar", "Jarvan IV"), listOf("Amumu", "Kha'Zix", "Lee Sin", "Volibear", "Hecarim")),
            LaneRole.MID to MatchupRoleResult(listOf("Ryze", "Ahri", "Irelia", "Veigar", "Viktor"), listOf("Aurelion Sol", "Zoe", "Katarina", "Orianna", "Twisted Fate"), listOf()),
            LaneRole.ADC to MatchupRoleResult(listOf("Kalista", "Varus", "Caitlyn", "Kai'Sa", "Ezreal"), listOf("Tristana", "Corki", "Sivir", "Draven", "Lucian"), listOf("Tristana", "Jinx", "Xayah", "Ashe", "Zeri")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Blitzcrank", "Lux", "Seraphine", "Karma", "Pyke"), listOf("Janna", "Leona", "Sona", "Thresh", "Rakan"), listOf("Sona", "Alistar", "Leona", "Braum", "Maokai"))
        ),
        "bard" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Renekton", "Sett", "Garen", "Tryndamere"), listOf("Riven", "Shen", "Poppy", "Fiora", "Ornn"), listOf("Singed", "Garen", "Camille", "Ambessa", "Malphite")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rengar", "Kayn", "Lillia", "Fiddlesticks", "Vi"), listOf("Evelynn", "Jax", "Viego", "Maestro Yi", "Gwen"), listOf("Jarvan IV", "Volibear", "Viego", "Xin Zhao", "Ekko")),
            LaneRole.MID to MatchupRoleResult(listOf("Zed", "Veigar", "Fizz", "Yasuo", "Vladimir"), listOf("Zoe", "Lissandra", "Vex", "Syndra", "Twisted Fate"), listOf("Ziggs", "Kassadin", "Vladimir", "Syndra", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Caitlyn", "Varus", "Xayah", "Miss Fortune"), listOf("Samira", "Vayne", "Tristana", "Lucian", "Sivir"), listOf("Ashe", "Sivir", "Corki", "Miss Fortune", "Caitlyn")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Rakan", "Blitzcrank", "Braum", "Lulu", "Seraphine"), listOf("Alistar", "Maokai", "Nami", "Sona", "Janna"), listOf())
        ),
        "blitzcrank" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Aatrox", "Renekton", "Yone", "Riven", "Ornn"), listOf("Jax", "Singed", "Malphite", "Sion", "Darius"), listOf("Jax", "Gragas", "Kennen", "Urgot", "Riven")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Kayn", "Lillia", "Talon", "Shyvana"), listOf("Wukong", "Amumu", "Olaf", "Rammus", "Xin Zhao"), listOf("Wukong", "Nunu y Willump", "Vi", "Volibear", "Warwick")),
            LaneRole.MID to MatchupRoleResult(listOf("Diana", "Akali", "Vex", "Syndra", "Fizz"), listOf("Galio", "Kassadin", "Twisted Fate", "Talon", "Zoe"), listOf("Zoe", "Orianna", "Lux", "Annie", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Zeri", "Jhin", "Varus", "Kai'Sa"), listOf("Samira", "Tristana", "Sivir", "Lucian", "Xayah"), listOf("Kai'Sa", "Draven", "Samira", "Jinx", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Nami", "Soraka", "Karma", "Lux"), listOf("Alistar", "Nautilus", "Leona", "Braum", "Janna"), listOf())
        ),
        "brand" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Nasus", "Darius", "Ornn", "Renekton"), listOf("Gwen", "Riven", "Jayce", "Gragas", "Camille"), listOf("Dr. Mundo", "Renekton", "Jayce", "Camille", "Nasus")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Kha'Zix", "Wukong", "Amumu", "Nunu y Willump"), listOf("Jarvan IV", "Evelynn", "Xin Zhao", "Olaf", "Talon"), listOf("Kha'Zix", "Xin Zhao", "Evelynn", "Lee Sin", "Rammus")),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Annie", "Vladimir", "Yone", "Ahri"), listOf("Ekko", "Talon", "Fizz", "Swain", "Akali"), listOf("Vex", "Orianna", "Zed", "Ahri", "Lucian")),
            LaneRole.ADC to MatchupRoleResult(listOf("Zeri", "Tristana", "Lucian", "Kai'Sa", "Kalista"), listOf("Samira", "Jhin", "Caitlyn", "Ashe"), listOf("Draven", "Kai'Sa", "Caitlyn", "Samira", "Sivir")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Janna", "Lulu", "Thresh", "Nautilus"), listOf("Nami", "Soraka", "Pyke", "Alistar", "Rakan"), listOf("Leona", "Thresh", "Janna", "Alistar", "Pyke"))
        ),
        "braum" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Irelia", "Garen", "Ornn", "Sion", "Jayce"), listOf("Wukong", "Gragas", "Fiora", "Camille", "Shen"), listOf("Camille", "Garen", "Kayle", "Tryndamere", "Fiora")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Xin Zhao", "Kha'Zix", "Evelynn", "Nunu y Willump", "Graves"), listOf("Amumu", "Vi", "Lillia", "Olaf", "Jarvan IV"), listOf("Jarvan IV", "Vi", "Lillia", "Fiddlesticks", "Talon")),
            LaneRole.MID to MatchupRoleResult(listOf("Vex", "Ahri", "Twisted Fate", "Orianna", "Syndra"), listOf("Ekko", "Galio", "Annie", "Swain", "Kassadin"), listOf("Fizz", "Zoe", "Kassadin", "Ahri", "Twisted Fate")),
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Tristana", "Jhin", "Draven", "Varus"), listOf("Vayne", "Nilah", "Xayah", "Sivir", "Zeri"), listOf("Tristana", "Zeri", "Lucian", "Varus", "Kalista")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Nautilus", "Thresh", "Alistar", "Blitzcrank"), listOf("Rakan", "Janna", "Soraka", "Nami", "Senna"), listOf())
        ),
        "caitlyn" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Wukong", "Sett", "Singed", "Garen"), listOf("Fiora", "Camille", "Malphite", "Irelia", "Riven"), listOf("Shen", "Riven", "Darius", "Camille", "Kennen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Lee Sin", "Ekko", "Amumu", "Nunu y Willump"), listOf("Gragas", "Evelynn", "Rammus", "Rengar", "Xin Zhao"), listOf("Evelynn", "Xin Zhao", "Wukong", "Vi", "Volibear")),
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Diana", "Orianna", "Syndra", "Yone"), listOf("Kassadin", "Zoe", "Vex", "Talon", "Twisted Fate"), listOf("Kassadin", "Galio", "Zoe", "Twisted Fate", "Annie")),
            LaneRole.ADC to MatchupRoleResult(listOf("Lucian", "Ezreal", "Xayah", "Kalista", "Sivir"), listOf("Samira", "Jhin", "Ashe", "Miss Fortune", "Jinx"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Rakan", "Yuumi", "Nami", "Lux"), listOf("Nautilus", "Soraka", "Karma", "Thresh", "Maokai"), listOf("Karma", "Nami", "Thresh", "Milio", "Lux"))
        ),
        "camille" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Dr. Mundo", "Aatrox", "Nasus", "Garen", "Jayce"), listOf("Shen", "Jax", "Gwen", "Gragas", "Urgot"), listOf("Sion", "Riven", "Wukong", "Shen", "Kennen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Kayn", "Jarvan IV", "Olaf", "Evelynn"), listOf("Amumu", "Maestro Yi", "Wukong", "Xin Zhao", "Fiddlesticks"), listOf("Gragas", "Nunu y Willump", "Evelynn", "Gwen", "Lillia")),
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Ahri", "Zed", "Syndra", "Vladimir"), listOf("Galio", "Orianna", "Ekko", "Twisted Fate", "Annie"), listOf("Diana", "Galio", "Vex", "Annie", "Ekko")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Jinx", "Kai'Sa", "Varus", "Zeri"), listOf("Xayah", "Draven", "Tristana", "Samira", "Lucian"), listOf("Draven", "Xayah", "Samira", "Jhin", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Soraka", "Nautilus", "Yuumi", "Karma", "Leona"), listOf("Rakan", "Braum", "Lulu", "Janna", "Alistar"), listOf("Rakan", "Soraka", "Janna", "Sona", "Braum"))
        ),
        "corki" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Malphite", "Sett", "Tryndamere", "Sion", "Ornn"), listOf("Gwen", "Singed", "Jax", "Riven", "Urgot"), listOf("Wukong", "Irelia", "Fiora", "Camille", "Riven")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Shyvana", "Rammus", "Wukong", "Lee Sin"), listOf("Kha'Zix", "Gragas", "Nunu y Willump", "Xin Zhao", "Fiddlesticks"), listOf("Evelynn", "Rengar", "Nunu y Willump", "Wukong", "Jarvan IV")),
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Akali", "Diana", "Syndra", "Annie"), listOf("Vex", "Zoe", "Ahri", "Galio", "Kassadin"), listOf("Zed", "Pantheon", "Yasuo", "Twisted Fate", "Veigar")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Vayne", "Ashe", "Xayah", "Miss Fortune"), listOf("Kai'Sa", "Samira", "Tristana", "Draven", "Zeri"), listOf("Xayah", "Varus", "Miss Fortune", "Tristana", "Ashe")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Morgana", "Soraka", "Rakan", "Zyra"), listOf("Nautilus", "Janna", "Pyke", "Nami", "Leona"), listOf("Rakan", "Braum", "Thresh", "Leona", "Janna"))
        ),
        "darius" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Malphite", "Sion", "Aatrox", "Akali", "Garen"), listOf("Wukong", "Olaf", "Pantheon", "Riven", "Urgot"), listOf("Malphite", "Camille", "Volibear", "Aatrox", "Vladimir")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Lillia", "Kayn", "Shyvana", "Rammus"), listOf("Evelynn", "Nunu y Willump", "Gragas", "Kindred", "Xin Zhao"), listOf("Vi", "Diana", "Gragas", "Fiddlesticks", "Volibear")),
            LaneRole.MID to MatchupRoleResult(listOf("Vex", "Ekko", "Veigar", "Fizz", "Yone"), listOf("Zoe", "Kassadin", "Galio", "Brand", "Annie"), listOf("Vex", "Orianna", "Diana", "Gragas", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Zeri", "Kai'Sa", "Jhin", "Kalista", "Samira"), listOf("Xayah", "Tristana", "Caitlyn", "Draven", "Ashe"), listOf("Xayah", "Samira", "Jhin", "Ashe", "Tristana")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Leona", "Thresh", "Yuumi", "Nautilus"), listOf("Alistar", "Nami", "Janna", "Rakan", "Sona"), listOf("Janna", "Rakan", "Soraka", "Leona", "Braum"))
        ),
        "diana" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Gwen", "Garen", "Fiora", "Sion", "Teemo"), listOf("Olaf", "Singed", "Gragas", "Nasus", "Shen"), listOf("Wukong", "Darius", "Shen", "Camille", "Urgot")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Warwick", "Rammus", "Jarvan IV", "Graves"), listOf("Xin Zhao", "Evelynn", "Amumu", "Kindred", "Volibear"), listOf("Lillia", "Graves", "Jarvan IV", "Shyvana", "Nunu y Willump")),
            LaneRole.MID to MatchupRoleResult(listOf("Zed", "Ziggs", "Ekko", "Fizz", "Orianna"), listOf("Galio", "Pantheon", "Renekton", "Twisted Fate", "Annie"), listOf("Jayce", "Fizz", "Ekko", "Pantheon", "Twisted Fate")),
            LaneRole.ADC to MatchupRoleResult(listOf("Tristana", "Jinx", "Miss Fortune", "Caitlyn", "Kai'Sa"), listOf("Draven", "Samira", "Varus", "Lucian", "Xayah"), listOf("Xayah", "Zeri", "Ashe", "Jinx", "Sivir")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Soraka", "Senna", "Yuumi", "Karma", "Nautilus"), listOf("Braum", "Alistar", "Rakan", "Janna", "Thresh"), listOf("Alistar", "Leona", "Rakan", "Sona", "Janna"))
        ),
        "dr_mundo" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Sion", "Akali", "Volibear", "Jax"), listOf("Gwen", "Aatrox", "Irelia", "Riven", "Tryndamere"), listOf("Wukong", "Garen", "Pantheon", "Shen", "Gragas")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Gragas", "Rammus", "Graves", "Kha'Zix"), listOf("Xin Zhao", "Jarvan IV", "Ekko", "Kindred", "Nunu y Willump"), listOf("Amumu", "Nunu y Willump", "Vi", "Gragas", "Evelynn")),
            LaneRole.MID to MatchupRoleResult(listOf("Zoe", "Ahri", "Vex", "Veigar", "Annie"), listOf("Orianna", "Galio", "Diana", "Kassadin", "Yasuo"), listOf("Zoe", "Vex", "Galio", "Fizz", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Lucian", "Jhin", "Caitlyn", "Sivir"), listOf("Kai'Sa", "Zeri", "Tristana", "Kalista", "Xayah"), listOf("Jhin", "Draven", "Zeri", "Tristana", "Sivir")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Thresh", "Lux", "Soraka", "Blitzcrank"), listOf("Rakan", "Janna", "Braum", "Sona", "Leona"), listOf("Nami", "Soraka", "Nautilus", "Pyke", "Janna"))
        ),
        "draven" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Jax", "Kennen", "Aatrox", "Sion"), listOf("Malphite", "Darius", "Shen", "Wukong", "Camille"), listOf("Wukong", "Shen", "Riven", "Nasus", "Kennen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Kayn", "Lee Sin", "Shyvana", "Vi"), listOf("Olaf", "Gragas", "Ekko", "Fiddlesticks", "Xin Zhao"), listOf("Evelynn", "Nunu y Willump", "Rammus", "Fiddlesticks", "Ekko")),
            LaneRole.MID to MatchupRoleResult(listOf("Diana", "Katarina", "Twisted Fate", "Corki", "Orianna"), listOf("Vex", "Galio", "Zoe", "Fizz", "Talon"), listOf("Orianna", "Kassadin", "Twisted Fate", "Gragas", "Annie")),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Tristana", "Zeri", "Kai'Sa", "Miss Fortune"), listOf("Samira", "Lucian", "Jhin", "Ashe", "Varus"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Morgana", "Yuumi", "Nautilus", "Lux"), listOf("Soraka", "Nami", "Pyke", "Braum", "Blitzcrank"), listOf("Blitzcrank", "Janna", "Pyke", "Thresh", "Alistar"))
        ),
        "ekko" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Aatrox", "Sett", "Garen", "Sion", "Ornn"), listOf("Riven", "Camille", "Gragas", "Irelia", "Fiora"), listOf("Wukong", "Irelia", "Riven", "Camille", "Gragas")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kayn", "Xin Zhao", "Lillia", "Wukong", "Rammus"), listOf("Vi", "Evelynn", "Kha'Zix", "Kindred", "Talon"), listOf("Warwick", "Wukong", "Xin Zhao", "Olaf", "Nunu y Willump")),
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Irelia", "Fizz", "Syndra", "Yone"), listOf("Galio", "Kassadin", "Zoe", "Swain", "Twisted Fate"), listOf("Orianna", "Zoe", "Kassadin", "Twisted Fate", "Vex")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Lucian", "Caitlyn", "Miss Fortune", "Varus"), listOf("Tristana", "Xayah", "Samira", "Jhin", "Nilah"), listOf("Draven", "Jhin", "Caitlyn", "Tristana", "Samira")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Karma", "Thresh", "Lux", "Morgana", "Yuumi"), listOf("Braum", "Nautilus", "Rakan", "Leona", "Sona"), listOf("Soraka", "Alistar", "Rakan", "Braum", "Sona"))
        ),
        "evelynn" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Jayce", "Sion", "Gwen", "Ornn"), listOf("Shen", "Camille", "Kennen", "Urgot", "Singed"), listOf("Shen", "Camille", "Fiora", "Gwen", "Dr. Mundo")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Kayn", "Rammus", "Volibear", "Amumu"), listOf("Vi", "Gragas", "Kindred", "Nunu y Willump", "Talon"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Yone", "Lux", "Fizz", "Syndra", "Vladimir"), listOf("Orianna", "Kassadin", "Pantheon", "Galio", "Twisted Fate"), listOf("Ekko", "Irelia", "Vex", "Zoe", "Zed")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Lucian", "Vayne", "Miss Fortune", "Kai'Sa"), listOf("Draven", "Samira", "Ashe", "Zeri", "Xayah"), listOf("Draven", "Xayah", "Caitlyn", "Sivir", "Ashe")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Leona", "Lulu", "Lux", "Karma"), listOf("Braum", "Rakan", "Soraka", "Sona", "Janna"), listOf("Rakan", "Nami", "Thresh", "Yuumi", "Soraka"))
        ),
        "ezreal" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Malphite", "Jayce", "Ornn", "Volibear"), listOf("Wukong", "Camille", "Pantheon", "Singed", "Urgot"), listOf("Jax", "Darius", "Gwen", "Tryndamere", "Urgot")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Jarvan IV", "Vi", "Jax", "Shyvana"), listOf("Maestro Yi", "Evelynn", "Gragas", "Olaf", "Lillia"), listOf("Gragas", "Nunu y Willump", "Vi", "Xin Zhao", "Fiddlesticks")),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Fizz", "Ahri", "Amumu", "Vi"), listOf("Kassadin", "Vex", "Zoe", "Twisted Fate", "Annie"), listOf("Galio", "Jayce", "Fizz", "Twisted Fate", "Zoe")),
            LaneRole.ADC to MatchupRoleResult(listOf("Xayah", "Ashe", "Miss Fortune", "Caitlyn", "Varus"), listOf("Samira", "Kai'Sa", "Draven", "Sivir", "Zeri"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Alistar", "Lulu", "Lux", "Swain"), listOf("Rakan", "Janna", "Nami", "Sona", "Pyke"), listOf("Janna", "Rakan", "Nami", "Leona", "Braum"))
        ),
        "fiddlesticks" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Volibear", "Teemo", "Akali", "Wukong"), listOf("Gwen", "Kennen", "Singed", "Camille", "Darius"), listOf("Camille", "Sett", "Riven", "Urgot", "Pantheon")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Graves", "Nunu y Willump", "Kha'Zix", "Vi", "Evelynn"), listOf("Olaf", "Rengar", "Talon", "Diana", "Hecarim"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Vladimir", "Swain", "Katarina", "Syndra", "Veigar"), listOf("Galio", "Twisted Fate", "Kassadin", "Jayce", "Diana"), listOf("Fizz", "Vladimir", "Irelia", "Zed", "Yasuo")),
            LaneRole.ADC to MatchupRoleResult(listOf("Kai'Sa", "Vayne", "Sivir", "Caitlyn", "Miss Fortune"), listOf("Ashe", "Jhin", "Zeri", "Draven", "Ezreal"), listOf("Samira", "Twitch", "Jinx", "Draven", "Ashe")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Karma", "Yuumi", "Lux", "Sona", "Nautilus"), listOf("Pyke", "Soraka", "Janna", "Rakan", "Leona"), listOf("Pyke", "Blitzcrank", "Thresh", "Janna", "Braum"))
        ),
        "fiora" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Dr. Mundo", "Sion", "Ornn", "Yone", "Aatrox"), listOf("Darius", "Sett", "Shen", "Urgot", "Wukong"), listOf()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Zed", "Lee Sin", "Lillia", "Jarvan IV", "Vi"), listOf("Nunu y Willump", "Xin Zhao", "Gragas", "Kindred", "Volibear"), listOf("Evelynn", "Vi", "Rammus", "Fiddlesticks", "Lillia")),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Ekko", "Veigar", "Lux", "Fizz"), listOf("Kassadin", "Galio", "Katarina", "Annie", "Vex"), listOf("Vex", "Ekko", "Galio", "Gragas", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Caitlyn", "Jhin", "Varus", "Miss Fortune"), listOf("Draven", "Jinx", "Xayah", "Tristana", "Zeri"), listOf("Tristana", "Lucian", "Varus", "Jhin", "Ezreal")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Leona", "Pyke", "Karma", "Alistar"), listOf("Janna", "Rakan", "Thresh", "Sona", "Lulu"), listOf("Rakan", "Nami", "Thresh", "Braum", "Blitzcrank"))
        ),
        "fizz" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Sion", "Aatrox", "Malphite", "Garen"), listOf("Renekton", "Shen", "Fiora", "Wukong", "Darius"), listOf("Gwen", "Sett", "Wukong", "Riven", "Urgot")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Kha'Zix", "Lee Sin", "Hecarim", "Rammus"), listOf("Vi", "Nunu y Willump", "Kindred", "Evelynn", "Talon"), listOf("Rammus", "Xin Zhao", "Diana", "Lillia", "Volibear")),
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Veigar", "Vex", "Brand", "Syndra"), listOf("Kassadin", "Galio", "Irelia", "Gragas", "Vladimir"), listOf("Zed", "Annie", "Riven", "Orianna", "Gragas")),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Miss Fortune", "Lucian", "Varus", "Draven"), listOf("Xayah", "Samira", "Zeri", "Sivir", "Kai'Sa"), listOf("Draven", "Tristana", "Ashe", "Ezreal", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Nautilus", "Nami", "Karma", "Lux"), listOf("Leona", "Braum", "Lulu", "Soraka", "Rakan"), listOf("Janna", "Rakan", "Thresh", "Braum", "Lulu"))
        ),
        "galio" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Wukong", "Garen", "Camille", "Akali", "Kennen"), listOf("Tryndamere", "Singed", "Gwen", "Riven", "Urgot"), listOf("Wukong", "Malphite", "Fiora", "Urgot", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Diana", "Lee Sin", "Kha'Zix", "Amumu", "Fiddlesticks"), listOf("Olaf", "Lillia", "Jarvan IV", "Kindred", "Xin Zhao"), listOf("Jarvan IV", "Vi", "Camille", "Kindred", "Diana")),
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Fizz", "Ekko", "Corki", "Vladimir"), listOf("Sett", "Orianna", "Ahri", "Twisted Fate"), listOf("Irelia", "Annie", "Yasuo", "Kassadin", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Ezreal", "Lucian", "Kalista", "Vayne"), listOf("Tristana", "Xayah", "Draven", "Varus", "Ashe"), listOf("Samira", "Xayah", "Tristana", "Jhin", "Ashe")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nautilus", "Thresh", "Lulu", "Karma", "Yuumi"), listOf("Alistar", "Rakan", "Soraka", "Braum", "Janna"), listOf("Rakan", "Morgana", "Soraka", "Braum", "Sona"))
        ),
        "garen" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Gwen", "Renekton", "Aatrox", "Akali", "Irelia"), listOf("Camille", "Shen", "Pantheon", "Tryndamere", "Urgot"), listOf()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Rammus", "Lillia", "Graves", "Olaf"), listOf("Xin Zhao", "Nunu y Willump", "Diana", "Volibear", "Gragas"), listOf("Vi", "Gragas", "Evelynn", "Amumu", "Fiddlesticks")),
            LaneRole.MID to MatchupRoleResult(listOf("Jayce", "Fizz", "Syndra", "Zed", "Talon"), listOf("Galio", "Kassadin", "Zoe", "Annie", "Twisted Fate"), listOf("Twisted Fate", "Lux", "Ahri", "Annie", "Syndra")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Zeri", "Caitlyn", "Miss Fortune", "Kai'Sa"), listOf("Xayah", "Vayne", "Draven", "Lucian", "Varus"), listOf("Jhin", "Lucian", "Draven", "Sivir", "Ashe")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Soraka", "Lux", "Pyke", "Seraphine"), listOf("Alistar", "Janna", "Rakan", "Lulu", "Sona"), listOf("Soraka", "Nami", "Morgana", "Alistar", "Thresh"))
        ),
        "gnar" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Volibear", "Singed", "Sett", "Rumble", "Mordekaiser"), listOf("Malphite", "Irelia", "Ornn", "Akali", "Kennen"), listOf()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Vi", "Shyvana", "Amumu", "Rengar", "Ekko"), listOf("Fiddlesticks", "Jarvan IV", "Lillia", "Kha'Zix", "Nunu y Willump"), listOf("Jarvan IV", "Amumu", "Wukong", "Nocturne", "Ekko")),
            LaneRole.MID to MatchupRoleResult(listOf("Lux", "Ryze", "Vladimir", "Fizz", "Kassadin"), listOf("Vex", "Diana", "Ekko", "Annie", "Twisted Fate"), listOf("Vex", "Lissandra", "Zoe", "Diana", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Xayah", "Miss Fortune", "Caitlyn", "Zeri", "Ezreal"), listOf("Lucian", "Sivir", "Kalista", "Tristana", "Ashe"), listOf("Kai'Sa", "Sivir", "Lucian", "Tristana", "Corki")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Swain", "Rakan", "Soraka", "Nautilus", "Lulu"), listOf("Pyke", "Maokai", "Janna", "Alistar", "Braum"), listOf("Soraka", "Nami", "Braum", "Maokai", "Sona"))
        ),
        "gragas" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Malphite", "Akali", "Garen", "Wukong", "Sett"), listOf("Sion", "Vladimir", "Singed", "Ornn", "Irelia"), listOf("Gwen", "Shen", "Fiora", "Riven", "Sett")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Ekko", "Lee Sin", "Xin Zhao", "Shyvana", "Kha'Zix"), listOf("Vi", "Jarvan IV", "Hecarim", "Volibear", "Talon"), listOf("Rengar", "Evelynn", "Jarvan IV", "Xin Zhao", "Kindred")),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Vex", "Orianna", "Annie", "Syndra"), listOf("Fizz", "Jayce", "Diana", "Galio", "Ahri"), listOf("Orianna", "Kassadin", "Diana", "Zed", "Twisted Fate")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Vayne", "Kai'Sa", "Miss Fortune", "Jhin"), listOf("Draven", "Samira", "Tristana", "Sivir", "Xayah"), listOf("Samira", "Kai'Sa", "Jhin", "Sivir", "Xayah")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Janna", "Soraka", "Lux", "Nautilus"), listOf("Leona", "Alistar", "Braum", "Thresh", "Nami"), listOf("Alistar", "Soraka", "Rakan", "Janna", "Pyke"))
        ),
        "graves" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kayle", "Gragas", "Aatrox", "Renekton", "Nasus"), listOf("Darius", "Jax", "Gwen", "Camille", "Shen"), listOf("Shen", "Pantheon", "Camille", "Irelia", "Urgot")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Lillia", "Kayn", "Shyvana", "Vi"), listOf("Nunu y Willump", "Evelynn", "Fiddlesticks", "Rammus", "Kindred"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Galio", "Akali", "Diana", "Syndra", "Orianna"), listOf("Jayce", "Kassadin", "Zoe", "Katarina", "Vex"), listOf("Twisted Fate", "Zoe", "Lux", "Annie", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Miss Fortune", "Ezreal", "Kalista", "Varus"), listOf("Draven", "Samira", "Jhin", "Nilah", "Tristana"), listOf("Ashe", "Xayah", "Jhin", "Draven", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nautilus", "Lulu", "Thresh", "Soraka", "Rakan"), listOf("Braum", "Nami", "Janna", "Blitzcrank", "Maokai"), listOf("Rakan", "Nautilus", "Pyke", "Thresh", "Leona"))
        ),
        "gwen" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Dr. Mundo", "Sion", "Malphite", "Volibear"), listOf("Olaf", "Garen", "Riven", "Urgot", "Sett"), listOf("Nasus", "Camille", "Volibear", "Shen", "Tryndamere")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Jarvan IV", "Rammus", "Olaf", "Amumu"), listOf("Vi", "Nunu y Willump", "Evelynn", "Gragas", "Hecarim"), listOf("Rammus", "Kha'Zix", "Gragas", "Evelynn", "Kindred")),
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Lux", "Veigar", "Zed", "Syndra"), listOf("Diana", "Ekko", "Yasuo", "Vex", "Katarina"), listOf("Zoe", "Twisted Fate", "Ekko", "Pantheon", "Syndra")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Jinx", "Varus", "Miss Fortune", "Sivir"), listOf("Tristana", "Draven", "Kai'Sa", "Xayah", "Lucian"), listOf("Draven", "Xayah", "Tristana", "Jinx", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Soraka", "Janna", "Lulu", "Nautilus", "Nami"), listOf("Pyke", "Alistar", "Karma", "Rakan", "Braum"), listOf("Blitzcrank", "Morgana", "Janna", "Rakan", "Braum"))
        ),
        "hecarim" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Ornn", "Aatrox", "Akali", "Shen"), listOf("Urgot", "Camille", "Nasus", "Sett", "Darius"), listOf("Kennen", "Tryndamere", "Shen", "Malphite", "Gragas")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kayn", "Vi", "Graves", "Jax", "Wukong"), listOf("Volibear", "Olaf", "Evelynn", "Xin Zhao", "Gragas"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Syndra", "Corki", "Diana", "Katarina", "Orianna"), listOf("Twisted Fate", "Galio", "Talon", "Annie", "Ahri"), listOf("Annie", "Kassadin", "Galio", "Fizz", "Zoe")),
            LaneRole.ADC to MatchupRoleResult(listOf("Varus", "Caitlyn", "Xayah", "Samira", "Ezreal"), listOf("Tristana", "Ashe", "Draven", "Zeri", "Vayne"), listOf("Ashe", "Draven", "Lucian", "Jhin", "Kalista")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nautilus", "Nami", "Alistar", "Lulu", "Thresh"), listOf("Rakan", "Janna", "Morgana", "Braum", "Sona"), listOf("Janna", "Lulu", "Nami", "Braum", "Soraka"))
        ),
        "heimerdinger" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Jax", "Akali", "Riven", "Nasus", "Darius"), listOf("Urgot", "Sett", "Garen", "Ornn", "Sion"), listOf("Wukong", "Akali", "Camille", "Volibear", "Malphite")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kindred", "Viego", "Shyvana", "Nunu y Willump", "Lillia"), listOf("Rengar", "Volibear", "Gragas", "Kayn", "Vi"), listOf("Nunu y Willump", "Xin Zhao", "Hecarim", "Wukong", "Vi")),
            LaneRole.MID to MatchupRoleResult(listOf("Aurelion Sol", "Irelia", "Vladimir", "Twisted Fate", "Katarina"), listOf("Syndra", "Annie", "Galio", "Ekko", "Ziggs"), listOf("Twisted Fate", "Aurelion Sol", "Galio", "Lux", "Syndra")),
            LaneRole.ADC to MatchupRoleResult(listOf("Xayah", "Samira", "Kalista", "Kai'Sa", "Lucian"), listOf("Ashe", "Draven", "Caitlyn", "Sivir", "Jhin"), listOf("Tristana", "Ashe", "Jhin", "Samira", "Kai'Sa")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Morgana", "Yuumi", "Lux", "Rakan", "Blitzcrank"), listOf("Sona", "Soraka", "Maokai", "Thresh", "Alistar"), listOf("Maokai", "Pyke", "Rakan", "Nautilus", "Braum"))
        ),
        "hwei" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Volibear", "Mordekaiser", "Vladimir", "Gnar", "Sion"), listOf("Riven", "Gwen", "Camille", "Irelia", "Fiora"), listOf("Poppy", "Malphite", "Darius", "Camille", "Nasus")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Xin Zhao", "Wukong", "Amumu", "Viego", "Vi"), listOf("Nidalee", "Ekko", "Fiddlesticks", "Kha'Zix", "Evelynn"), listOf("Rammus", "Nocturne", "Warwick", "Kayn", "Xin Zhao")),
            LaneRole.MID to MatchupRoleResult(listOf("Ryze", "Veigar", "Lissandra", "Galio", "Mel"), listOf("Katarina", "Vel'Koz", "Aurora", "Fizz", "Ahri"), listOf()),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Ezreal", "Varus", "Smolder", "Miss Fortune"), listOf("Xayah", "Tristana", "Zeri", "Ashe", "Kog'Maw"), listOf("Tristana", "Xayah", "Jinx", "Kog'Maw", "Kai'Sa")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nautilus", "Lulu", "Karma", "Sona", "Milio"), listOf("Pyke", "Leona", "Rell", "Thresh", "Alistar"), listOf("Alistar", "Blitzcrank", "Leona", "Rell", "Rakan"))
        ),
        "irelia" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Wukong", "Sion", "Dr. Mundo", "Jayce"), listOf("Olaf", "Jax", "Riven", "Gwen", "Kennen"), listOf("Shen", "Riven", "Nasus", "Kennen", "Gwen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Xin Zhao", "Jarvan IV", "Zed", "Viego"), listOf("Gragas", "Rammus", "Ekko", "Fiddlesticks", "Wukong"), listOf("Nunu y Willump", "Evelynn", "Amumu", "Rammus", "Gragas")),
            LaneRole.MID to MatchupRoleResult(listOf("Diana", "Akali", "Zed", "Orianna", "Vladimir"), listOf("Galio", "Sett", "Pantheon", "Vex", "Ahri"), listOf("Galio", "Zoe", "Vex", "Twisted Fate", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Lucian", "Caitlyn", "Jhin", "Kai'Sa"), listOf("Draven", "Tristana", "Samira", "Xayah", "Kalista"), listOf("Lucian", "Jhin", "Zeri", "Xayah", "Ashe")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Karma", "Thresh", "Lux", "Soraka"), listOf("Rakan", "Alistar", "Janna", "Pyke", "Leona"), listOf("Nami", "Thresh", "Janna", "Sona", "Braum"))
        ),
        "janna" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Jax", "Garen", "Sion", "Ornn"), listOf("Pantheon", "Riven", "Malphite", "Camille", "Gragas"), listOf("Darius", "Shen", "Riven", "Camille", "Urgot")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Xin Zhao", "Wukong", "Amumu", "Nunu y Willump"), listOf("Gragas", "Vi", "Rammus", "Kha'Zix", "Volibear"), listOf("Amumu", "Jarvan IV", "Wukong", "Kindred", "Volibear")),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Fizz", "Irelia", "Yone", "Yasuo"), listOf("Kassadin", "Vex", "Twisted Fate", "Annie", "Orianna"), listOf("Diana", "Irelia", "Orianna", "Katarina", "Twisted Fate")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Vayne", "Kai'Sa", "Xayah", "Samira"), listOf("Draven", "Zeri", "Lucian", "Ashe", "Jhin"), listOf("Xayah", "Draven", "Samira", "Sivir", "Zeri")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Alistar", "Rakan", "Karma", "Lux"), listOf("Nami", "Thresh", "Soraka", "Lulu", "Nautilus"), listOf())
        ),
        "jarvan_iv" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Malphite", "Dr. Mundo", "Garen", "Singed", "Nasus"), listOf("Darius", "Jayce", "Akali", "Gwen", "Shen"), listOf("Malphite", "Fiora", "Shen", "Irelia", "Darius")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Vi", "Lillia", "Olaf", "Nunu y Willump"), listOf("Wukong", "Xin Zhao", "Gragas", "Kindred", "Talon"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Ziggs", "Vex", "Yasuo", "Diana", "Syndra"), listOf("Zoe", "Irelia", "Kassadin", "Ahri", "Galio"), listOf("Diana", "Galio", "Orianna", "Twisted Fate", "Veigar")),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Miss Fortune", "Jhin", "Sivir", "Varus"), listOf("Tristana", "Lucian", "Kai'Sa", "Zeri", "Samira"), listOf("Draven", "Samira", "Vayne", "Sivir", "Miss Fortune")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Karma", "Morgana", "Yuumi", "Nautilus", "Zyra"), listOf("Rakan", "Braum", "Lulu", "Pyke", "Soraka"), listOf("Soraka", "Rakan", "Leona", "Sona", "Braum"))
        ),
        "jax" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Wukong", "Irelia", "Gwen", "Volibear", "Yone"), listOf("Shen", "Gragas", "Pantheon", "Vladimir", "Dr. Mundo"), listOf("Singed", "Urgot", "Camille", "Akali", "Ornn")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Diana", "Warwick", "Shyvana", "Maestro Yi"), listOf("Rammus", "Amumu", "Nunu y Willump", "Evelynn", "Fiddlesticks"), listOf("Nunu y Willump", "Gragas", "Vi", "Evelynn", "Volibear")),
            LaneRole.MID to MatchupRoleResult(listOf("Ekko", "Akali", "Twisted Fate", "Yasuo", "Zed"), listOf("Vex", "Kassadin", "Zoe", "Galio", "Brand"), listOf("Galio", "Orianna", "Fizz", "Vladimir", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Kai'Sa", "Jinx", "Sivir", "Kalista"), listOf("Xayah", "Draven", "Tristana", "Samira", "Nilah"), listOf("Zeri", "Samira", "Kai'Sa", "Sivir", "Xayah")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Braum", "Lulu", "Nautilus", "Thresh"), listOf("Janna", "Rakan", "Alistar", "Nami", "Pyke"), listOf("Soraka", "Rakan", "Nami", "Braum", "Janna"))
        ),
        "jayce" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Yone", "Akali", "Kennen", "Riven", "Darius"), listOf("Malphite", "Olaf", "Camille", "Dr. Mundo", "Wukong"), listOf("Riven", "Malphite", "Irelia", "Teemo", "Pantheon")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Lillia", "Lee Sin", "Kayn", "Volibear"), listOf("Gragas", "Evelynn", "Vi", "Hecarim", "Fiddlesticks"), listOf("Gragas", "Vi", "Rengar", "Talon", "Jarvan IV")),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Ahri", "Zoe", "Yone", "Katarina"), listOf("Fizz", "Galio", "Vex", "Annie", "Twisted Fate"), listOf("Galio", "Twisted Fate", "Ahri", "Annie", "Zoe")),
            LaneRole.ADC to MatchupRoleResult(listOf("Lucian", "Draven", "Miss Fortune", "Vayne", "Varus"), listOf("Tristana", "Zeri", "Kai'Sa", "Sivir", "Jhin"), listOf("Zeri", "Ashe", "Varus", "Jhin", "Kai'Sa")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Pyke", "Lulu", "Karma", "Lux"), listOf("Alistar", "Rakan", "Nami", "Braum", "Soraka"), listOf("Nautilus", "Rakan", "Nami", "Thresh", "Leona"))
        ),
        "jhin" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Nasus", "Renekton", "Aatrox", "Garen"), listOf("Wukong", "Shen", "Ornn", "Camille", "Urgot"), listOf("Pantheon", "Riven", "Darius", "Sett", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Lee Sin", "Diana", "Vi", "Kha'Zix"), listOf("Olaf", "Gragas", "Jarvan IV", "Talon", "Nunu y Willump"), listOf("Xin Zhao", "Wukong", "Kha'Zix", "Fiddlesticks", "Rammus")),
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Diana", "Veigar", "Syndra", "Orianna"), listOf("Kassadin", "Galio", "Katarina", "Annie", "Akali"), listOf("Zoe", "Vex", "Jayce", "Galio", "Kassadin")),
            LaneRole.ADC to MatchupRoleResult(listOf("Caitlyn", "Lucian", "Xayah", "Ezreal", "Varus"), listOf("Tristana", "Samira", "Kai'Sa", "Ashe", "Zeri"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lux", "Karma", "Lulu", "Yuumi", "Nami"), listOf("Nautilus", "Alistar", "Rakan", "Braum", "Janna"), listOf("Janna", "Nami", "Alistar", "Sona", "Pantheon"))
        ),
        "jinx" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Fiora", "Wukong", "Sion", "Akali"), listOf("Shen", "Singed", "Pantheon", "Camille", "Tryndamere"), listOf("Singed", "Shen", "Darius", "Urgot", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Shyvana", "Lee Sin", "Amumu", "Nunu y Willump"), listOf("Gragas", "Xin Zhao", "Jarvan IV", "Rammus", "Evelynn"), listOf("Nunu y Willump", "Rammus", "Xin Zhao", "Fiddlesticks", "Evelynn")),
            LaneRole.MID to MatchupRoleResult(listOf("Vex", "Lux", "Akali", "Syndra", "Orianna"), listOf("Zoe", "Fizz", "Kassadin", "Twisted Fate", "Talon"), listOf("Diana", "Ahri", "Vex", "Twisted Fate", "Aurelion Sol")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Caitlyn", "Vayne", "Miss Fortune", "Xayah"), listOf("Draven", "Zeri", "Varus", "Kalista", "Tristana"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Lulu", "Rakan", "Nautilus", "Alistar"), listOf("Nami", "Soraka", "Pyke", "Janna", "Blitzcrank"), listOf("Soraka", "Lulu", "Janna", "Braum", "Thresh"))
        ),
        "kalista" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Dr. Mundo", "Yone", "Aatrox", "Teemo", "Sett"), listOf("Nasus", "Shen", "Tryndamere", "Darius", "Jax"), listOf("Wukong", "Shen", "Kennen", "Darius", "Gragas")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Hecarim", "Lee Sin", "Kayn", "Rammus"), listOf("Fiddlesticks", "Talon", "Kindred", "Volibear", "Rengar"), listOf("Amumu", "Fiddlesticks", "Ekko", "Hecarim", "Talon")),
            LaneRole.MID to MatchupRoleResult(listOf("Syndra", "Yasuo", "Vladimir", "Ahri", "Ekko"), listOf("Kassadin", "Jayce", "Twisted Fate", "Veigar", "Galio"), listOf("Syndra", "Vex", "Twisted Fate", "Aurelion Sol", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Varus", "Kai'Sa", "Sivir", "Samira"), listOf("Xayah", "Draven", "Caitlyn", "Ashe", "Tristana"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Karma", "Nautilus", "Pyke", "Blitzcrank"), listOf("Janna", "Leona", "Braum", "Alistar", "Nami"), listOf("Blitzcrank", "Nautilus", "Sona", "Leona", "Thresh"))
        ),
        "karma" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Gwen", "Renekton", "Sion", "Nasus", "Jayce"), listOf("Riven", "Malphite", "Fiora", "Camille", "Urgot"), listOf("Singed", "Jax", "Darius", "Camille", "Renekton")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kha'Zix", "Diana", "Lee Sin", "Shyvana", "Wukong"), listOf("Gragas", "Nunu y Willump", "Olaf", "Rengar", "Evelynn"), listOf("Gragas", "Evelynn", "Amumu", "Kindred", "Fiddlesticks")),
            LaneRole.MID to MatchupRoleResult(listOf("Ekko", "Akali", "Orianna", "Syndra", "Vladimir"), listOf("Kassadin", "Zoe", "Jayce", "Twisted Fate", "Annie"), listOf("Galio", "Diana", "Vex", "Annie", "Zoe")),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Lucian", "Xayah", "Varus", "Miss Fortune"), listOf("Tristana", "Zeri", "Samira", "Jhin", "Ashe"), listOf("Tristana", "Draven", "Jhin", "Ashe", "Miss Fortune")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Leona", "Lux", "Alistar", "Braum"), listOf("Nautilus", "Soraka", "Rakan", "Sona", "Janna"), listOf("Blitzcrank", "Nami", "Lulu", "Pyke", "Rakan"))
        ),
        "kassadin" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Garen", "Kayle", "Kennen", "Vladimir"), listOf("Shen", "Riven", "Singed", "Camille"), listOf("Pantheon", "Gwen", "Wukong", "Sett", "Shen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Rammus", "Jarvan IV", "Olaf", "Vi"), listOf("Maestro Yi", "Amumu", "Kindred", "Rengar", "Warwick"), listOf("Xin Zhao", "Kha'Zix", "Evelynn", "Hecarim", "Volibear")),
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Ekko", "Twisted Fate", "Annie", "Syndra"), listOf("Pantheon", "Lucian", "Irelia", "Zed", "Galio"), listOf("Pantheon", "Zed", "Renekton", "Fizz", "Sett")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Zeri", "Jhin", "Kalista", "Sivir"), listOf("Draven", "Tristana", "Kai'Sa", "Varus", "Ashe"), listOf("Draven", "Samira", "Varus", "Jhin", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Karma", "Lulu", "Yuumi", "Nautilus", "Maokai"), listOf("Rakan", "Soraka", "Braum", "Sona", "Alistar"), listOf("Soraka", "Rakan", "Alistar", "Yuumi", "Nami"))
        ),
        "katarina" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Gwen", "Aatrox", "Sion", "Ornn", "Teemo"), listOf("Singed", "Camille", "Shen", "Wukong", "Sett"), listOf("Wukong", "Pantheon", "Shen", "Urgot", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Warwick", "Jarvan IV", "Nunu y Willump", "Vi"), listOf("Rammus", "Amumu", "Gragas", "Evelynn", "Fiddlesticks"), listOf("Vi", "Amumu", "Nunu y Willump", "Fiddlesticks", "Lillia")),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Ahri", "Akali", "Syndra", "Veigar"), listOf("Kassadin", "Jayce", "Galio", "Vex", "Gragas"), listOf()),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Caitlyn", "Lucian", "Sivir", "Kai'Sa"), listOf("Draven", "Samira", "Xayah", "Tristana", "Ashe"), listOf("Zeri", "Samira", "Varus", "Lucian", "Sivir")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Pyke", "Karma", "Nami", "Senna"), listOf("Rakan", "Blitzcrank", "Alistar", "Sona", "Janna"), listOf("Janna", "Thresh", "Nautilus", "Braum", "Nami"))
        ),
        "kayle" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Aatrox", "Shen", "Ornn", "Fiora"), listOf("Nasus", "Wukong", "Irelia", "Tryndamere", "Jax"), listOf("Pantheon", "Kennen", "Shen", "Malphite", "Yone")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kayn", "Shyvana", "Diana", "Lillia", "Jarvan IV"), listOf("Xin Zhao", "Vi", "Fiddlesticks", "Gragas", "Evelynn"), listOf("Nunu y Willump", "Rammus", "Gragas", "Volibear", "Lee Sin")),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Fizz", "Zed", "Vladimir", "Veigar"), listOf("Zoe", "Kassadin", "Vex", "Annie", "Twisted Fate"), listOf("Twisted Fate", "Pantheon", "Akshan", "Zed", "Annie")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Zeri", "Lucian", "Sivir", "Miss Fortune"), listOf("Jinx", "Samira", "Draven", "Jhin", "Twitch"), listOf("Jhin", "Xayah", "Tristana", "Draven", "Samira")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Karma", "Leona", "Nami", "Nautilus"), listOf("Rakan", "Soraka", "Blitzcrank", "Alistar", "Braum"), listOf("Braum", "Rakan", "Alistar", "Janna", "Thresh"))
        ),
        "kayn" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Akali", "Renekton", "Gwen", "Kennen", "Dr. Mundo"), listOf("Wukong", "Jayce", "Shen", "Camille", "Tryndamere"), listOf("Singed", "Shen", "Pantheon", "Gragas", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Diana", "Vi", "Amumu", "Xin Zhao"), listOf("Gragas", "Maestro Yi", "Fiddlesticks", "Talon", "Kindred"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Yasuo", "Veigar", "Orianna", "Zoe", "Ziggs"), listOf("Vex", "Kassadin", "Galio", "Annie", "Zed"), listOf("Ahri", "Vex", "Lux", "Annie", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Jinx", "Caitlyn", "Kalista", "Varus"), listOf("Draven", "Zeri", "Jhin", "Tristana", "Vayne"), listOf("Tristana", "Kai'Sa", "Xayah", "Lucian", "Draven")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Lux", "Karma", "Nami", "Soraka"), listOf("Soraka", "Rakan", "Lulu", "Janna", "Sona"), listOf("Thresh", "Sona", "Nami", "Soraka", "Blitzcrank"))
        ),
        "kennen" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Wukong", "Riven", "Aatrox", "Yone", "Akali"), listOf("Pantheon", "Dr. Mundo", "Olaf", "Camille", "Nasus"), listOf("Tryndamere", "Riven", "Camille", "Fiora", "Garen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Diana", "Nunu y Willump", "Talon", "Graves"), listOf("Xin Zhao", "Rammus", "Jarvan IV", "Kayn", "Ekko"), listOf("Vi", "Evelynn", "Wukong", "Nunu y Willump", "Hecarim")),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Zed", "Twisted Fate", "Yasuo", "Fizz"), listOf("Veigar", "Diana", "Kassadin", "Annie", "Zoe"), listOf("Ahri", "Jayce", "Akali", "Annie", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Lucian", "Kai'Sa", "Zeri", "Varus"), listOf("Draven", "Tristana", "Miss Fortune", "Ezreal", "Ashe"), listOf("Tristana", "Zeri", "Kai'Sa", "Samira", "Miss Fortune")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Nami", "Yuumi", "Karma", "Soraka"), listOf("Rakan", "Braum", "Janna", "Alistar", "Leona"), listOf("Nautilus", "Thresh", "Alistar", "Braum", "Leona"))
        ),
        "kindred" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Dr. Mundo", "Ornn", "Renekton", "Aatrox"), listOf("Gwen", "Tryndamere", "Kennen", "Jax", "Camille"), listOf("Urgot", "Shen", "Camille", "Teemo", "Gragas")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Amumu", "Vi", "Hecarim", "Kayn", "Graves"), listOf("Rengar", "Rammus", "Fiddlesticks", "Kha'Zix", "Gragas"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Syndra", "Karma", "Galio", "Vladimir", "Veigar"), listOf("Twisted Fate", "Akali", "Vex", "Annie", "Orianna"), listOf("Galio", "Lux", "Zoe", "Katarina", "Diana")),
            LaneRole.ADC to MatchupRoleResult(listOf("Caitlyn", "Sivir", "Ezreal", "Miss Fortune", "Zeri"), listOf("Tristana", "Vayne", "Samira", "Nilah", "Draven"), listOf("Jhin", "Miss Fortune", "Jinx", "Ashe", "Varus")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lux", "Yuumi", "Nautilus", "Nami", "Sona"), listOf("Blitzcrank", "Braum", "Rakan", "Soraka", "Janna"), listOf("Braum", "Leona", "Blitzcrank", "Pyke", "Janna"))
        ),
        "lee_sin" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Teemo", "Akali", "Garen", "Kayle"), listOf("Shen", "Urgot", "Dr. Mundo", "Sett", "Mordekaiser"), listOf("Shen", "Wukong", "Pantheon", "Kennen", "Urgot")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Lillia", "Kha'Zix", "Wukong", "Graves"), listOf("Gragas", "Nunu y Willump", "Evelynn", "Xin Zhao", "Volibear"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Katarina", "Zoe", "Fizz", "Yone"), listOf("Vex", "Galio", "Annie", "Twisted Fate", "Diana"), listOf("Kassadin", "Orianna", "Twisted Fate", "Annie", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Ezreal", "Miss Fortune", "Kai'Sa", "Caitlyn"), listOf("Xayah", "Draven", "Samira", "Jinx", "Lucian"), listOf("Zeri", "Draven", "Jhin", "Sivir", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Karma", "Lux", "Soraka", "Lulu"), listOf("Rakan", "Janna", "Nautilus", "Braum", "Sona"), listOf("Alistar", "Rakan", "Nami", "Sona", "Braum"))
        ),
        "leona" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Renekton", "Sett", "Riven", "Sion", "Volibear"), listOf("Gwen", "Shen", "Urgot", "Darius", "Kennen"), listOf("Jayce", "Renekton", "Darius", "Camille", "Riven")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Xin Zhao", "Lee Sin", "Kha'Zix", "Amumu", "Nunu y Willump"), listOf("Lillia", "Olaf", "Rammus", "Kindred", "Kayn"), listOf("Gragas", "Wukong", "Jarvan IV", "Diana", "Ekko")),
            LaneRole.MID to MatchupRoleResult(listOf("Diana", "Twisted Fate", "Katarina", "Irelia", "Yone"), listOf("Orianna", "Zoe", "Vex", "Galio", "Annie"), listOf("Galio", "Ekko", "Kassadin", "Orianna", "Lux")),
            LaneRole.ADC to MatchupRoleResult(listOf("Kai'Sa", "Ezreal", "Ashe", "Caitlyn", "Jhin"), listOf("Xayah", "Tristana", "Draven", "Zeri", "Vayne"), listOf("Tristana", "Corki", "Ezreal", "Sivir", "Samira")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Nautilus", "Thresh", "Karma", "Blitzcrank"), listOf("Rakan", "Morgana", "Janna", "Alistar", "Braum"), listOf())
        ),
        "lillia" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Akali", "Kayle", "Ornn", "Sion", "Sett"), listOf("Jayce", "Riven", "Kennen", "Camille", "Irelia"), listOf("Gwen", "Malphite", "Darius", "Camille", "Sett")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Shyvana", "Rammus", "Amumu", "Wukong", "Volibear"), listOf("Diana", "Warwick", "Vi", "Fiddlesticks", "Evelynn"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Vex", "Ekko", "Galio", "Swain", "Vladimir"), listOf("Fizz", "Ahri", "Twisted Fate", "Annie", "Syndra"), listOf("Diana", "Jayce", "Pantheon", "Twisted Fate", "Fizz")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Lucian", "Kai'Sa", "Xayah", "Sivir"), listOf("Samira", "Draven", "Tristana", "Caitlyn", "Ashe"), listOf("Tristana", "Xayah", "Zeri", "Sivir", "Ashe")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Braum", "Alistar", "Nautilus", "Soraka"), listOf("Rakan", "Janna", "Nami", "Sona", "Zyra"), listOf("Alistar", "Rakan", "Sona", "Janna", "Thresh"))
        ),
        "lissandra" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Akali", "Irelia", "Sion", "Jayce", "Darius"), listOf("Shen", "Gwen", "Renekton", "Volibear", "Nasus"), listOf("Akali", "Gwen", "Sett", "Camille", "Aatrox")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Vi", "Evelynn", "Kayn", "Shyvana"), listOf("Fiddlesticks", "Olaf", "Talon", "Jax", "Xin Zhao"), listOf("Volibear", "Xin Zhao", "Lillia", "Amumu", "Kha'Zix")),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Fizz", "Kassadin", "Vladimir", "Katarina"), listOf("Aurelion Sol", "Diana", "Ziggs", "Zoe", "Galio"), listOf()),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Xayah", "Kalista", "Draven", "Jhin"), listOf("Ashe", "Ezreal", "Jinx", "Zeri", "Tristana"), listOf("Kai'Sa", "Tristana", "Zeri", "Lucian", "Ezreal")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nautilus", "Lux", "Pyke", "Thresh", "Karma"), listOf("Janna", "Sona", "Alistar", "Nami", "Soraka"), listOf("Alistar", "Sona", "Nami", "Maokai", "Karma"))
        ),
        "lucian" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Aatrox", "Garen", "Renekton", "Volibear"), listOf("Wukong", "Singed", "Riven", "Camille", "Ornn"), listOf("Wukong", "Camille", "Darius", "Malphite", "Urgot")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Jarvan IV", "Lee Sin", "Shyvana", "Amumu"), listOf("Xin Zhao", "Gragas", "Talon", "Evelynn", "Pantheon"), listOf("Gragas", "Nunu y Willump", "Rammus", "Fiddlesticks", "Hecarim")),
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Orianna", "Zed", "Irelia", "Kassadin"), listOf("Zoe", "Vex", "Ahri", "Katarina", "Aurelion Sol"), listOf("Twisted Fate", "Ekko", "Annie", "Kassadin", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Tristana", "Vayne", "Sivir", "Kai'Sa"), listOf("Kog'Maw", "Draven", "Xayah", "Samira", "Caitlyn"), listOf("Ziggs", "Zeri", "Jhin", "Varus", "Brand")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Pyke", "Rakan", "Karma", "Lux"), listOf("Braum", "Leona", "Alistar", "Nami", "Sona"), listOf("Nami", "Braum", "Thresh", "Rakan", "Alistar"))
        ),
        "lulu" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Sion", "Volibear", "Ornn", "Garen"), listOf("Malphite", "Shen", "Jax", "Urgot", "Gwen"), listOf("Riven", "Shen", "Wukong", "Camille", "Sett")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Shyvana", "Kayn", "Lee Sin", "Talon"), listOf("Gragas", "Olaf", "Evelynn", "Fiddlesticks"), listOf("Olaf", "Maestro Yi", "Rengar", "Xin Zhao", "Kindred")),
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Veigar", "Yone", "Vladimir", "Ekko"), listOf("Vex", "Kassadin", "Orianna", "Twisted Fate", "Annie"), listOf("Kassadin", "Galio", "Vex", "Katarina", "Fizz")),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Kai'Sa", "Ezreal", "Sivir", "Kalista"), listOf("Zeri", "Draven", "Caitlyn", "Jinx", "Ashe"), listOf("Tristana", "Vayne", "Jinx", "Zeri", "Sivir")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Nautilus", "Soraka", "Nami", "Lux"), listOf("Thresh", "Blitzcrank", "Pyke", "Leona", "Braum"), listOf("Nautilus", "Rakan", "Karma", "Braum", "Janna"))
        ),
        "master_yi" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Aatrox", "Irelia", "Sion", "Gwen"), listOf("Riven", "Tryndamere", "Wukong", "Camille", "Kennen"), listOf("Shen", "Kennen", "Singed", "Gragas", "Wukong")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Kayn", "Xin Zhao", "Nunu y Willump", "Shyvana"), listOf("Gragas", "Evelynn", "Vi", "Wukong", "Fiddlesticks"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Zed", "Veigar", "Ahri", "Vladimir", "Kassadin"), listOf("Vex", "Galio", "Fizz", "Annie", "Akali"), listOf("Galio", "Gragas", "Seraphine", "Vex", "Swain")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Kai'Sa", "Ashe", "Varus", "Caitlyn"), listOf("Draven", "Zeri", "Vayne", "Xayah", "Lucian"), listOf("Jhin", "Zeri", "Lucian", "Nilah", "Sivir")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Braum", "Soraka", "Nautilus", "Nami"), listOf("Rakan", "Leona", "Janna", "Alistar", "Blitzcrank"), listOf("Alistar", "Lulu", "Karma", "Braum", "Yuumi"))
        ),
        "malphite" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Jayce", "Gwen", "Tryndamere", "Irelia"), listOf("Nasus", "Singed", "Dr. Mundo", "Shen", "Gragas"), listOf()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kha'Zix", "Shyvana", "Rengar", "Zed", "Graves"), listOf("Gragas", "Vi", "Amumu", "Lillia", "Jax"), listOf("Jarvan IV", "Nunu y Willump", "Wukong", "Xin Zhao", "Hecarim")),
            LaneRole.MID to MatchupRoleResult(listOf("Zed", "Fizz", "Twisted Fate", "Syndra", "Orianna"), listOf("Kassadin", "Vex", "Galio", "Vladimir", "Aurelion Sol"), listOf("Orianna", "Katarina", "Yasuo", "Galio", "Annie")),
            LaneRole.ADC to MatchupRoleResult(listOf("Zeri", "Miss Fortune", "Jinx", "Vayne", "Kalista"), listOf("Xayah", "Lucian", "Tristana", "Sivir", "Varus"), listOf("Kai'Sa", "Zeri", "Draven", "Jinx", "Samira")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Karma", "Janna", "Nautilus", "Nami"), listOf("Rakan", "Alistar", "Soraka", "Sona", "Braum"), listOf("Nami", "Thresh", "Nautilus", "Pyke", "Leona"))
        ),
        "maokai" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Jayce", "Akali", "Riven", "Dr. Mundo", "Aatrox"), listOf("Gwen", "Camille", "Pantheon", "Shen", "Urgot"), listOf("Shen", "Riven", "Sett", "Darius", "Gwen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Diana", "Kayn", "Graves", "Vi", "Lee Sin"), listOf("Nunu y Willump", "Amumu", "Fiddlesticks", "Viego", "Volibear"), listOf("Amumu", "Gragas", "Evelynn", "Lillia", "Ekko")),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Annie", "Syndra", "Katarina", "Ahri"), listOf("Galio", "Brand", "Kassadin", "Aurelion Sol", "Veigar"), listOf("Galio", "Aurelion Sol", "Vex", "Zoe", "Zed")),
            LaneRole.ADC to MatchupRoleResult(listOf("Caitlyn", "Lucian", "Draven", "Jhin", "Kalista"), listOf("Tristana", "Xayah", "Jinx", "Vayne", "Kai'Sa"), listOf("Kai'Sa", "Twitch", "Tristana", "Miss Fortune", "Ezreal")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Karma", "Lux", "Pyke", "Soraka", "Nami"), listOf("Leona", "Sona", "Janna", "Thresh", "Braum"), listOf("Soraka", "Janna", "Seraphine", "Alistar", "Yuumi"))
        ),
        "mel" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Volibear", "Gnar", "Ornn", "Jayce", "Malphite"), listOf("Riven", "Camille", "Fiora", "Sion", "Sett"), listOf("Urgot", "Ornn", "Garen", "Poppy", "Riven")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Viego", "Lee Sin", "Warwick", "Diana"), listOf("Rammus", "Hecarim", "Xin Zhao", "Pantheon", "Evelynn"), listOf("Rammus", "Fiddlesticks", "Rengar", "Jarvan IV", "Hecarim")),
            LaneRole.MID to MatchupRoleResult(listOf("Vex", "Ahri", "Lux", "Twisted Fate", "Ryze"), listOf("Aurelion Sol", "Kassadin", "Vladimir", "Syndra", "Viktor"), listOf()),
            LaneRole.ADC to MatchupRoleResult(listOf("Lucian", "Varus", "Smolder", "Ezreal", "Miss Fortune"), listOf("Kog'Maw", "Zeri", "Kai'Sa", "Tristana", "Samira"), listOf("Ashe", "Jinx", "Xayah", "Tristana", "Miss Fortune")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nami", "Nautilus", "Janna", "Lulu", "Rakan"), listOf("Leona", "Zilean", "Braum", "Pyke", "Rell"), listOf("Rakan", "Sona", "Thresh", "Soraka", "Braum"))
        ),
        "milio" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Sion", "Aatrox", "Teemo", "Nasus"), listOf("Urgot", "Shen", "Tryndamere", "Malphite", "Rumble"), listOf("Malphite", "Teemo", "Kennen", "Vladimir", "Darius")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Warwick", "Rammus", "Xin Zhao", "Amumu"), listOf("Shyvana", "Wukong", "Jarvan IV", "Kha'Zix", "Vi"), listOf("Amumu", "Rengar", "Talon", "Fiddlesticks", "Evelynn")),
            LaneRole.MID to MatchupRoleResult(listOf("Lissandra", "Veigar", "Twisted Fate", "Annie", "Vex"), listOf("Zoe", "Syndra", "Kassadin", "Aurelion Sol", "Corki"), listOf("Twisted Fate", "Vex", "Diana", "Irelia", "Katarina")),
            LaneRole.ADC to MatchupRoleResult(listOf("Varus", "Ashe", "Xayah", "Jhin", "Caitlyn"), listOf("Tristana", "Zeri", "Lucian", "Jinx", "Miss Fortune"), listOf("Ashe", "Lucian", "Caitlyn", "Varus", "Vayne")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Swain", "Lux", "Leona", "Seraphine", "Karma"), listOf("Janna", "Blitzcrank", "Soraka", "Lulu", "Yuumi"), listOf())
        ),
        "miss_fortune" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Akali", "Sion", "Jayce", "Warwick"), listOf("Gwen", "Shen", "Malphite", "Nasus", "Jax"), listOf("Sett", "Malphite", "Darius", "Shen", "Garen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Lee Sin", "Kayn", "Amumu", "Wukong"), listOf("Olaf", "Gragas", "Xin Zhao", "Talon", "Fiddlesticks"), listOf("Vi", "Jarvan IV", "Nunu y Willump", "Rengar", "Xin Zhao")),
            LaneRole.MID to MatchupRoleResult(listOf("Zoe", "Twisted Fate", "Diana", "Annie", "Fizz"), listOf("Vex", "Lux", "Ekko", "Syndra", "Gragas"), listOf("Orianna", "Veigar", "Galio", "Ekko", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Kai'Sa", "Ezreal", "Kalista", "Xayah"), listOf("Draven", "Jinx", "Tristana", "Jhin", "Zeri"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Lulu", "Nautilus", "Thresh", "Rakan"), listOf("Blitzcrank", "Nami", "Braum", "Pyke", "Soraka"), listOf("Thresh", "Leona", "Braum", "Pyke", "Maokai"))
        ),
        "mordekaiser" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Irelia", "Garen", "Ambessa", "Teemo", "Malphite"), listOf("Tryndamere", "Riven", "Kayle", "Gragas", "Fiora"), listOf()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Shyvana", "Amumu", "Rammus", "Vi", "Evelynn"), listOf("Gwen", "Ekko", "Kayn", "Lee Sin", "Hecarim"), listOf("Ekko", "Hecarim", "Nunu y Willump", "Volibear", "Wukong")),
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Yasuo", "Aurelion Sol", "Syndra", "Katarina"), listOf("Zoe", "Lissandra", "Kassadin", "Galio", "Zed"), listOf("Vex", "Aurelion Sol", "Lissandra", "Syndra", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Sivir", "Jhin", "Xayah", "Ezreal", "Jinx"), listOf("Tristana", "Vayne", "Caitlyn", "Ashe", "Varus"), listOf("Tristana", "Xayah", "Lucian", "Ashe", "Samira")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Nautilus", "Maokai", "Alistar", "Swain"), listOf("Janna", "Nami", "Braum", "Lulu", "Soraka"), listOf("Nami", "Maokai", "Soraka", "Pyke", "Rakan"))
        ),
        "morgana" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Yone", "Riven", "Sett", "Kennen", "Malphite"), listOf("Fiora", "Camille", "Teemo", "Gwen", "Irelia"), listOf("Wukong", "Shen", "Gwen", "Urgot", "Tryndamere")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Diana", "Lee Sin", "Hecarim", "Amumu", "Fiddlesticks"), listOf("Evelynn", "Nunu y Willump", "Kha'Zix", "Rengar", "Kindred"), listOf("Shyvana", "Vi", "Kha'Zix", "Volibear", "Talon")),
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Akali", "Gragas", "Veigar", "Syndra"), listOf("Annie", "Pantheon", "Diana", "Kassadin", "Katarina"), listOf("Ekko", "Kassadin", "Zoe", "Annie", "Vex")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ashe", "Xayah", "Kai'Sa", "Varus", "Jhin"), listOf("Draven", "Ezreal", "Lucian", "Sivir", "Tristana"), listOf("Draven", "Samira", "Jinx", "Sivir", "Tristana")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Alistar", "Nautilus", "Karma", "Ashe"), listOf("Soraka", "Nami", "Janna", "Sona", "Pyke"), listOf("Thresh", "Alistar", "Braum", "Rakan", "Sona"))
        ),
        "nasus" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Akali", "Malphite", "Kennen", "Vladimir", "Tryndamere"), listOf("Camille", "Olaf", "Garen", "Urgot", "Gragas"), listOf("Singed", "Renekton", "Irelia", "Camille", "Darius")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Shyvana", "Hecarim", "Ekko", "Jax"), listOf("Graves", "Gragas", "Maestro Yi", "Fiddlesticks", "Xin Zhao"), listOf("Gragas", "Evelynn", "Amumu", "Volibear", "Fiddlesticks")),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Yone", "Diana", "Zed", "Irelia"), listOf("Ziggs", "Galio", "Zoe", "Aurelion Sol", "Brand"), listOf("Orianna", "Ekko", "Fizz", "Zoe", "Veigar")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Tristana", "Ashe", "Kalista", "Varus"), listOf("Jhin", "Caitlyn", "Lucian", "Samira", "Xayah"), listOf("Miss Fortune", "Tristana", "Draven", "Xayah", "Jhin")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Senna", "Alistar", "Lux", "Nautilus"), listOf("Soraka", "Braum", "Janna", "Sona", "Rakan"), listOf("Soraka", "Pyke", "Nami", "Leona", "Thresh"))
        ),
        "nautilus" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kayle", "Sion", "Gragas", "Volibear", "Ornn"), listOf("Singed", "Sett", "Camille", "Gwen", "Darius"), listOf("Jayce", "Renekton", "Malphite", "Kennen", "Gwen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Kayn", "Diana", "Shyvana", "Evelynn"), listOf("Wukong", "Olaf", "Xin Zhao", "Lillia"), listOf("Ekko", "Graves", "Warwick", "Fiddlesticks", "Gragas")),
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Orianna", "Twisted Fate", "Syndra", "Zoe"), listOf("Diana", "Galio", "Lux", "Aurelion Sol", "Vladimir"), listOf("Veigar", "Karma", "Pantheon", "Galio", "Aurelion Sol")),
            LaneRole.ADC to MatchupRoleResult(listOf("Jhin", "Ezreal", "Jinx", "Kai'Sa", "Vayne"), listOf("Xayah", "Samira", "Tristana", "Sivir", "Zeri"), listOf("Tristana", "Samira", "Sivir", "Lucian", "Kai'Sa")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Pyke", "Karma", "Nami", "Lulu"), listOf("Rakan", "Morgana", "Braum", "Leona", "Alistar"), listOf("Yuumi", "Nami", "Alistar", "Thresh", "Pyke"))
        ),
        "nocturne" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Dr. Mundo", "Garen", "Gnar", "Jayce", "Darius"), listOf("Fiora", "Gwen", "Kayle", "Ornn", "Swain"), listOf("Fiora", "Olaf", "Shen", "Camille", "Wukong")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Diana", "Kha'Zix", "Vi", "Volibear", "Graves"), listOf("Gragas", "Hecarim", "Jarvan IV", "Nunu y Willump", "Olaf"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Brand", "Ryze", "Twisted Fate", "Veigar", "Yone"), listOf("Akali", "Ahri", "Aurelion Sol", "Heimerdinger", "Kassadin"), listOf("Kassadin", "Swain", "Fizz", "Diana", "Annie")),
            LaneRole.ADC to MatchupRoleResult(listOf("Caitlyn", "Jinx", "Jhin", "Ezreal", "Kai'Sa"), listOf("Lucian", "Varus", "Vayne", "Xayah", "Zeri"), listOf("Xayah", "Draven", "Lucian", "Tristana", "Ashe")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Blitzcrank", "Lux", "Morgana", "Galio", "Pyke"), listOf("Braum", "Janna", "Sona", "Soraka", "Singed"), listOf("Braum", "Lulu", "Yuumi", "Rakan", "Poppy"))
        ),
        "nunu_willump" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Gragas", "Renekton", "Dr. Mundo", "Teemo"), listOf("Gwen", "Sett", "Singed", "Tryndamere", "Urgot"), listOf("Olaf", "Shen", "Nasus", "Kennen", "Irelia")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Lee Sin", "Amumu", "Hecarim", "Rammus"), listOf("Jarvan IV", "Vi", "Maestro Yi", "Olaf", "Xin Zhao"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Veigar", "Ahri", "Orianna", "Zed"), listOf("Kassadin", "Irelia", "Yone", "Galio", "Aurelion Sol"), listOf("Irelia", "Zoe", "Orianna", "Akali", "Twisted Fate")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Kai'Sa", "Caitlyn", "Sivir", "Samira"), listOf("Tristana", "Vayne", "Zeri", "Jhin", "Lucian"), listOf("Tristana", "Draven", "Jinx", "Samira", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Leona", "Karma", "Rakan", "Nautilus"), listOf("Soraka", "Blitzcrank", "Thresh", "Braum", "Janna"), listOf("Janna", "Nami", "Thresh", "Zyra", "Pyke"))
        ),
        "olaf" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Gwen", "Pantheon", "Volibear", "Dr. Mundo"), listOf("Camille", "Shen", "Fiora", "Kayle", "Akali"), listOf("Pantheon", "Nasus", "Wukong", "Urgot", "Garen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Lillia", "Wukong", "Amumu", "Xin Zhao"), listOf("Jarvan IV", "Gragas", "Rengar", "Talon", "Ekko"), listOf("Nunu y Willump", "Xin Zhao", "Evelynn", "Ekko", "Fiddlesticks")),
            LaneRole.MID to MatchupRoleResult(listOf("Vex", "Diana", "Galio", "Syndra", "Lux"), listOf("Orianna", "Katarina", "Kassadin", "Zed", "Talon"), listOf("Galio", "Diana", "Karma", "Twisted Fate", "Vex")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Varus", "Ashe", "Miss Fortune", "Kalista"), listOf("Lucian", "Draven", "Tristana", "Xayah", "Sivir"), listOf("Draven", "Jhin", "Varus", "Xayah", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Nautilus", "Alistar", "Yuumi", "Braum"), listOf("Soraka", "Lulu", "Nami", "Pyke", "Janna"), listOf("Janna", "Yuumi", "Lulu", "Nami", "Thresh"))
        ),
        "pantheon" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Renekton", "Jax", "Yone", "Riven"), listOf("Sion", "Olaf", "Gragas", "Malphite", "Camille"), listOf("Wukong", "Riven", "Irelia", "Fiora", "Kennen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Nunu y Willump", "Jarvan IV", "Zed", "Rammus"), listOf("Xin Zhao", "Wukong", "Volibear", "Talon", "Rengar"), listOf("Amumu", "Rammus", "Graves", "Hecarim", "Volibear")),
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Ahri", "Orianna", "Syndra", "Diana"), listOf("Vex", "Ekko", "Galio", "Zoe", "Brand"), listOf("Fizz", "Orianna", "Galio", "Diana", "Gragas")),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Miss Fortune", "Samira", "Caitlyn", "Sivir"), listOf("Draven", "Xayah", "Ashe", "Tristana", "Zeri"), listOf("Xayah", "Jhin", "Samira", "Zeri", "Sivir")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Soraka", "Yuumi", "Lux", "Karma"), listOf("Blitzcrank", "Rakan", "Thresh", "Braum", "Pyke"), listOf("Janna", "Rakan", "Soraka", "Alistar", "Leona"))
        ),
        "rammus" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Jax", "Akali", "Irelia", "Yasuo"), listOf("Sett", "Shen", "Wukong", "Darius", "Fiora"), listOf("Riven", "Fiora", "Singed", "Wukong", "Gwen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Lee Sin", "Rengar", "Viego", "Graves"), listOf("Amumu", "Kha'Zix", "Nunu y Willump", "Lillia", "Olaf"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Yasuo", "Katarina", "Vex", "Zoe", "Yone"), listOf("Kassadin", "Galio", "Ekko", "Akali", "Ahri"), listOf("Vex", "Fizz", "Katarina", "Veigar", "Irelia")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ashe", "Ezreal", "Jinx", "Kalista", "Caitlyn"), listOf("Draven", "Xayah", "Zeri", "Sivir", "Tristana"), listOf("Kai'Sa", "Lucian", "Draven", "Samira", "Jinx")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Lux", "Seraphine", "Karma", "Nautilus"), listOf("Braum", "Soraka", "Rakan", "Sona", "Alistar"), listOf("Soraka", "Nami", "Karma", "Sona", "Pyke"))
        ),
        "renekton" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Gwen", "Riven", "Akali", "Jayce", "Irelia"), listOf("Singed", "Olaf", "Shen", "Ornn", "Darius"), listOf("Gragas", "Gwen", "Pantheon")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Lee Sin", "Diana", "Wukong", "Kha'Zix"), listOf("Xin Zhao", "Nunu y Willump", "Rengar", "Fiddlesticks", "Viego"), listOf("Ekko", "Evelynn", "Vi", "Fiddlesticks", "Volibear")),
            LaneRole.MID to MatchupRoleResult(listOf("Yasuo", "Yone", "Zed", "Fizz", "Kassadin"), listOf("Zoe", "Lux", "Ahri", "Twisted Fate", "Vladimir"), listOf("Kassadin", "Katarina", "Zoe", "Galio", "Twisted Fate")),
            LaneRole.ADC to MatchupRoleResult(listOf("Lucian", "Varus", "Kai'Sa", "Samira", "Kalista"), listOf("Tristana", "Zeri", "Xayah", "Jinx", "Ashe"), listOf("Zeri", "Draven", "Ashe", "Samira", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Yuumi", "Nautilus", "Zyra", "Thresh"), listOf("Braum", "Janna", "Soraka", "Pyke", "Nami"), listOf("Leona", "Karma", "Pyke", "Blitzcrank", "Rakan"))
        ),
        "rengar" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Akali", "Teemo", "Gwen", "Yone", "Kennen"), listOf("Wukong", "Tryndamere", "Shen", "Irelia", "Camille"), listOf("Dr. Mundo", "Pantheon", "Shen", "Urgot", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Jarvan IV", "Vi", "Talon", "Evelynn"), listOf("Nunu y Willump", "Rammus", "Gragas", "Xin Zhao", "Hecarim"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Ekko", "Fizz", "Veigar", "Zoe"), listOf("Vex", "Diana", "Pantheon", "Galio", "Brand"), listOf("Galio", "Karma", "Ahri", "Annie", "Aurelion Sol")),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Ezreal", "Zeri", "Kai'Sa", "Sivir"), listOf("Draven", "Samira", "Tristana", "Kalista", "Xayah"), listOf("Draven", "Samira", "Varus", "Sivir", "Jhin")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Morgana", "Pyke", "Nami", "Lux", "Nautilus"), listOf("Alistar", "Soraka", "Leona", "Braum", "Maokai"), listOf("Rakan", "Yuumi", "Lulu", "Pyke", "Soraka"))
        ),
        "ryze" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Volibear", "Nasus", "Tryndamere", "Dr. Mundo", "Renekton"), listOf("Kennen", "Ornn", "Singed", "Fiora", "Gnar"), listOf("Shen", "Sett", "Urgot", "Renekton", "Darius")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Pantheon", "Kha'Zix", "Hecarim", "Nocturne"), listOf("Evelynn", "Jarvan IV", "Wukong", "Talon", "Viego"), listOf("Hecarim", "Jarvan IV", "Rengar", "Kha'Zix", "Lee Sin")),
            LaneRole.MID to MatchupRoleResult(listOf("Ekko", "Vladimir", "Akali", "Fizz", "Ahri"), listOf("Syndra", "Galio", "Zoe", "Lissandra", "Katarina"), listOf()),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Kalista", "Draven", "Xayah", "Caitlyn"), listOf("Jinx", "Tristana", "Vayne", "Zeri", "Kai'Sa"), listOf("Xayah", "Miss Fortune", "Jinx", "Sivir", "Jhin")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lux", "Morgana", "Karma", "Nautilus", "Pyke"), listOf("Maokai", "Alistar", "Seraphine", "Soraka", "Zilean"), listOf("Janna", "Rakan", "Karma", "Leona", "Thresh"))
        ),
        "samira" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Malphite", "Kennen", "Akali", "Teemo"), listOf("Wukong", "Shen", "Gragas", "Camille", "Sett"), listOf("Shen", "Wukong", "Malphite", "Sett", "Ornn")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Diana", "Kha'Zix", "Rengar", "Talon"), listOf("Olaf", "Nunu y Willump", "Amumu", "Fiddlesticks", "Xin Zhao"), listOf("Vi", "Gragas", "Jarvan IV", "Fiddlesticks", "Nunu y Willump")),
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Zed", "Twisted Fate", "Katarina", "Yasuo"), listOf("Galio", "Vex", "Orianna", "Aurelion Sol", "Annie"), listOf("Diana", "Galio", "Orianna", "Twisted Fate", "Veigar")),
            LaneRole.ADC to MatchupRoleResult(listOf("Kai'Sa", "Ezreal", "Jhin", "Caitlyn", "Vayne"), listOf("Draven", "Xayah", "Tristana", "Zeri", "Miss Fortune"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Nautilus", "Pyke", "Nami", "Sona"), listOf("Janna", "Rakan", "Lulu", "Alistar", "Braum"), listOf("Alistar", "Nautilus", "Gragas", "Galio", "Rakan"))
        ),
        "senna" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Wukong", "Malphite", "Riven", "Nasus", "Volibear"), listOf("Renekton", "Pantheon", "Gwen", "Shen", "Camille"), listOf("Irelia", "Malphite", "Sett", "Urgot", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Xin Zhao", "Shyvana", "Vi", "Graves"), listOf("Evelynn", "Lillia", "Nunu y Willump", "Fiddlesticks", "Rengar"), listOf("Warwick", "Diana", "Graves", "Amumu", "Fiddlesticks")),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Yasuo", "Vladimir", "Jayce", "Kassadin"), listOf("Twisted Fate", "Orianna", "Diana", "Gragas", "Katarina"), listOf("Orianna", "Galio", "Vex", "Twisted Fate", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Xayah", "Miss Fortune", "Sivir", "Kai'Sa"), listOf("Ashe", "Zeri", "Tristana", "Lucian", "Draven"), listOf("Galio", "Nasus", "Gragas", "Ornn", "Sett")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Yuumi", "Alistar", "Rakan", "Leona"), listOf("Thresh", "Morgana", "Seraphine", "Pyke", "Sona"), listOf("Nami", "Thresh", "Leona", "Alistar", "Braum"))
        ),
        "seraphine" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Kayle", "Wukong", "Teemo", "Jayce"), listOf("Camille", "Kennen", "Jax", "Urgot", "Fiora"), listOf("Wukong", "Nasus", "Dr. Mundo", "Gwen", "Darius")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Lillia", "Shyvana", "Graves", "Vi"), listOf("Nunu y Willump", "Gragas", "Talon", "Fiddlesticks"), listOf("Vi", "Ekko", "Amumu", "Xin Zhao", "Nunu y Willump")),
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Orianna", "Akali", "Lux", "Veigar"), listOf("Vex", "Galio", "Yasuo", "Fizz", "Kassadin"), listOf("Fizz", "Kassadin", "Irelia", "Annie", "Zoe")),
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Vayne", "Ashe", "Xayah", "Kalista"), listOf("Samira", "Kai'Sa", "Tristana", "Jhin", "Lucian"), listOf("Lux", "Ashe", "Jhin", "Draven", "Zeri")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Yuumi", "Rakan", "Nautilus", "Lux"), listOf("Alistar", "Soraka", "Blitzcrank", "Braum", "Maokai"), listOf("Leona", "Sona", "Janna", "Braum", "Rakan"))
        ),
        "sion" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Gwen", "Malphite", "Jayce", "Nasus"), listOf("Olaf", "Darius", "Shen", "Fiora", "Sett"), listOf("Urgot", "Pantheon", "Camille", "Akali", "Dr. Mundo")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Shyvana", "Zed", "Rammus", "Fiddlesticks"), listOf("Gragas", "Amumu", "Warwick", "Rengar", "Hecarim"), listOf("Wukong", "Rengar", "Maestro Yi", "Evelynn", "Talon")),
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Yone", "Vex", "Yasuo", "Syndra"), listOf("Kassadin", "Galio", "Fizz", "Aurelion Sol", "Ekko"), listOf("Orianna", "Katarina", "Irelia", "Vladimir", "Kassadin")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Miss Fortune", "Caitlyn", "Ashe", "Sivir"), listOf("Jinx", "Kai'Sa", "Lucian", "Xayah", "Tristana"), listOf("Samira", "Kai'Sa", "Tristana", "Ashe", "Kalista")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Sona", "Blitzcrank", "Karma", "Nautilus"), listOf("Soraka", "Alistar", "Janna", "Leona", "Nami"), listOf("Soraka", "Rakan", "Janna", "Braum", "Pyke"))
        ),
        "smolder" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Ornn", "Nasus", "Sion", "Mordekaiser", "Darius"), listOf("Fiora", "Riven", "Camille", "Urgot", "Singed"), listOf("Malphite", "Riven", "Volibear", "Shen", "Sett")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Vi", "Nocturne", "Kayn", "Amumu", "Wukong"), listOf("Volibear", "Gwen", "Evelynn", "Rengar", "Lillia"), listOf("Ekko", "Rammus", "Nunu y Willump", "Lee Sin", "Wukong")),
            LaneRole.MID to MatchupRoleResult(listOf("Galio", "Veigar", "Ryze", "Orianna", "Vladimir"), listOf("Kassadin", "Annie", "Zed", "Twisted Fate", "Aurora"), listOf("Twisted Fate", "Lissandra", "Aurora", "Vex", "Syndra")),
            LaneRole.ADC to MatchupRoleResult(listOf("Xayah", "Lucian", "Varus", "Ezreal", "Vayne"), listOf("Jinx", "Sivir", "Ashe", "Zeri", "Corki"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Rakan", "Alistar", "Lulu", "Lux"), listOf("Blitzcrank", "Pyke", "Nami", "Zilean", "Maokai"), listOf("Nami", "Braum", "Alistar", "Leona", "Nautilus"))
        ),
        "sona" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Renekton", "Wukong", "Garen", "Volibear", "Teemo"), listOf("Gwen", "Fiora", "Irelia", "Gragas", "Camille"), listOf("Fiora", "Darius", "Garen", "Urgot", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Kayn", "Graves", "Talon", "Amumu"), listOf("Nunu y Willump", "Fiddlesticks", "Xin Zhao", "Viego", "Ekko"), listOf("Olaf", "Vi", "Rengar", "Volibear", "Rammus")),
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Yone", "Katarina", "Akali", "Zed"), listOf("Galio", "Vex", "Orianna", "Swain", "Veigar"), listOf("Vex", "Katarina", "Veigar", "Vladimir", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Kai'Sa", "Caitlyn", "Xayah", "Tristana"), listOf("Samira", "Varus", "Draven", "Ashe", "Sivir"), listOf("Tristana", "Lucian", "Caitlyn", "Ashe", "Jhin")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Yuumi", "Karma", "Nautilus", "Nami"), listOf("Soraka", "Leona", "Thresh", "Braum", "Blitzcrank"), listOf())
        ),
        "soraka" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Sett", "Malphite", "Volibear", "Akali"), listOf("Wukong", "Fiora", "Camille", "Shen", "Urgot"), listOf("Shen", "Gragas", "Renekton", "Riven", "Fiora")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kayn", "Lee Sin", "Rammus", "Vi", "Amumu"), listOf("Olaf", "Gragas", "Xin Zhao", "Talon", "Lillia"), listOf("Vi", "Rammus", "Nunu y Willump", "Evelynn", "Rengar")),
            LaneRole.MID to MatchupRoleResult(listOf("Ekko", "Vex", "Fizz", "Vladimir", "Orianna"), listOf("Diana", "Zoe", "Kassadin", "Galio", "Irelia"), listOf("Twisted Fate", "Diana", "Pantheon", "Syndra", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Miss Fortune", "Caitlyn", "Sivir", "Tristana"), listOf("Draven", "Varus", "Lucian", "Ashe", "Jinx"), listOf("Draven", "Zeri", "Tristana", "Jinx", "Sivir")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Nautilus", "Karma", "Braum", "Thresh"), listOf("Blitzcrank", "Nami", "Rakan", "Sona", "Yuumi"), listOf())
        ),
        "syndra" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Yone", "Aatrox", "Riven", "Kayle"), listOf("Camille", "Malphite", "Gwen", "Fiora", "Darius"), listOf("Gwen", "Camille", "Wukong", "Urgot", "Darius")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Amumu", "Gragas", "Wukong", "Diana", "Lillia"), listOf("Kindred", "Hecarim", "Ekko", "Nunu y Willump", "Xin Zhao"), listOf("Kha'Zix", "Volibear", "Lillia", "Fiddlesticks", "Talon")),
            LaneRole.MID to MatchupRoleResult(listOf("Yone", "Yasuo", "Vladimir", "Orianna", "Vex"), listOf("Katarina", "Twisted Fate", "Aurelion Sol", "Zoe", "Irelia"), listOf("Zed", "Talon", "Yone", "Irelia", "Lucian")),
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Varus", "Caitlyn", "Kai'Sa", "Lucian"), listOf("Ashe", "Jinx", "Jhin", "Sivir", "Xayah"), listOf("Kalista", "Jinx", "Samira", "Lucian", "Jhin")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Karma", "Yuumi", "Lux", "Lulu", "Nami"), listOf("Pyke", "Braum", "Sona", "Janna", "Soraka"), listOf("Camille", "Pyke", "Janna", "Braum", "Soraka"))
        ),
        "teemo" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Renekton", "Jax", "Camille", "Volibear"), listOf("Jayce", "Aatrox", "Riven", "Gwen", "Urgot"), listOf()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Graves", "Warwick", "Maestro Yi", "Shyvana", "Hecarim"), listOf("Gragas", "Vi", "Kha'Zix", "Fiddlesticks", "Nunu y Willump"), listOf("Xin Zhao", "Vi", "Lillia", "Jarvan IV", "Kha'Zix")),
            LaneRole.MID to MatchupRoleResult(listOf("Irelia", "Twisted Fate", "Ekko", "Yone", "Yasuo"), listOf("Galio", "Vex", "Ahri", "Katarina", "Lux"), listOf("Galio", "Kassadin", "Renekton", "Vladimir", "Talon")),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Kai'Sa", "Lucian", "Tristana", "Jhin"), listOf("Samira", "Jinx", "Varus", "Xayah", "Zeri"), listOf("Caitlyn", "Xayah", "Jhin", "Ashe", "Ezreal")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Soraka", "Nami", "Lulu", "Karma", "Lux"), listOf("Rakan", "Braum", "Alistar", "Sona", "Leona"), listOf("Leona", "Thresh", "Janna", "Pyke", "Rakan"))
        ),
        "tristana" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Kennen", "Renekton", "Wukong", "Garen", "Riven"), listOf("Nasus", "Shen", "Jax", "Teemo", "Gwen"), listOf("Shen", "Garen", "Darius", "Irelia", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Lillia", "Jarvan IV", "Amumu", "Vi"), listOf("Rammus", "Kha'Zix", "Evelynn", "Gragas", "Talon"), listOf("Nunu y Willump", "Vi", "Rengar", "Fiddlesticks", "Ekko")),
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Katarina", "Zoe", "Kassadin", "Irelia"), listOf("Vex", "Diana", "Vladimir", "Annie", "Yasuo"), listOf("Fizz", "Galio", "Vex", "Veigar", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Vayne", "Samira", "Varus", "Kalista"), listOf("Draven", "Lucian", "Jinx", "Corki", "Sivir"), listOf("Miss Fortune", "Ashe", "Sivir", "Jhin", "Lucian")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Rakan", "Nautilus", "Nami", "Pyke"), listOf("Janna", "Soraka", "Lulu", "Braum", "Alistar"), listOf("Lulu", "Nami", "Rakan", "Leona", "Braum"))
        ),
        "tryndamere" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Gwen", "Dr. Mundo", "Irelia", "Garen", "Kennen"), listOf("Jax", "Singed", "Jayce", "Camille", "Malphite"), listOf("Dr. Mundo", "Urgot", "Riven", "Darius", "Teemo")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Graves", "Maestro Yi", "Diana", "Vi"), listOf("Kayn", "Gragas", "Kha'Zix", "Rammus", "Nunu y Willump"), listOf("Nunu y Willump", "Wukong", "Amumu", "Evelynn", "Gragas")),
            LaneRole.MID to MatchupRoleResult(listOf("Diana", "Galio", "Ekko", "Fizz", "Aurelion Sol"), listOf("Kassadin", "Veigar", "Twisted Fate", "Zed"), listOf("Zoe", "Fizz", "Kassadin", "Twisted Fate", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Varus", "Miss Fortune", "Caitlyn", "Ezreal"), listOf("Lucian", "Tristana", "Kai'Sa", "Jhin", "Zeri"), listOf("Xayah", "Tristana", "Draven", "Ezreal", "Varus")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Lulu", "Lux", "Leona", "Soraka"), listOf("Janna", "Nami", "Rakan", "Pyke", "Braum"), listOf("Leona", "Nautilus", "Braum", "Pyke", "Janna"))
        ),
        "twitch" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Sion", "Aatrox", "Sett", "Darius"), listOf("Kennen", "Riven", "Gwen", "Camille", "Jax"), listOf("Vladimir", "Riven", "Sett", "Camille", "Kennen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Wukong", "Graves", "Vi", "Lee Sin", "Viego"), listOf("Jarvan IV", "Kha'Zix", "Kayn", "Volibear", "Xin Zhao"), listOf("Wukong", "Diana", "Jarvan IV", "Amumu", "Fiddlesticks")),
            LaneRole.MID to MatchupRoleResult(listOf("Vex", "Ahri", "Yone", "Orianna", "Annie"), listOf("Ekko", "Katarina", "Twisted Fate", "Galio", "Talon"), listOf("Annie", "Ahri", "Twisted Fate", "Kassadin", "Aurelion Sol")),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Ezreal", "Varus", "Sivir", "Miss Fortune"), listOf("Nilah", "Tristana", "Ashe", "Jhin", "Draven"), listOf("Xayah", "Ezreal", "Kai'Sa", "Ashe", "Jhin")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Karma", "Lulu", "Soraka", "Nami"), listOf("Rakan", "Pyke", "Nautilus", "Braum", "Leona"), listOf("Nami", "Rakan", "Lulu", "Pyke", "Janna"))
        ),
        "varus" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Aatrox", "Renekton", "Sett", "Sion", "Ornn"), listOf("Pantheon", "Irelia", "Jax", "Gwen", "Camille"), listOf("Wukong", "Darius", "Shen", "Camille", "Kennen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lillia", "Jarvan IV", "Lee Sin", "Volibear", "Amumu"), listOf("Kha'Zix", "Vi", "Kindred", "Ekko", "Olaf"), listOf("Xin Zhao", "Amumu", "Evelynn", "Viego", "Lillia")),
            LaneRole.MID to MatchupRoleResult(listOf("Ekko", "Ahri", "Veigar", "Vladimir", "Vex"), listOf("Zoe", "Twisted Fate", "Kassadin", "Annie", "Galio"), listOf("Twisted Fate", "Orianna", "Vex", "Katarina", "Galio")),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Jinx", "Ezreal", "Xayah", "Kalista"), listOf("Tristana", "Samira", "Sivir", "Jhin", "Kai'Sa"), listOf("Ashe", "Draven", "Jhin", "Samira", "Sivir")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Yuumi", "Seraphine", "Nautilus", "Thresh"), listOf("Janna", "Rakan", "Soraka", "Blitzcrank", "Braum"), listOf("Rakan", "Janna", "Braum", "Leona", "Nami"))
        ),
        "vayne" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Malphite", "Dr. Mundo", "Sion", "Volibear"), listOf("Fiora", "Riven", "Camille", "Kennen", "Teemo"), listOf("Wukong", "Singed", "Darius", "Sett", "Shen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Shyvana", "Kayn", "Rammus", "Maestro Yi", "Jax"), listOf("Gragas", "Evelynn", "Kha'Zix", "Fiddlesticks", "Xin Zhao"), listOf("Nunu y Willump", "Gragas", "Amumu", "Fiddlesticks", "Xin Zhao")),
            LaneRole.MID to MatchupRoleResult(listOf("Zed", "Yasuo", "Katarina", "Irelia", "Gragas"), listOf("Zoe", "Diana", "Kassadin", "Twisted Fate", "Vladimir"), listOf("Vex", "Twisted Fate", "Ekko", "Annie", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Caitlyn", "Samira", "Kalista", "Xayah"), listOf("Draven", "Ashe", "Tristana", "Zeri", "Jhin"), listOf("Xayah", "Lucian", "Draven", "Ashe", "Ezreal")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Soraka", "Alistar", "Nautilus", "Maokai"), listOf("Lulu", "Janna", "Thresh", "Braum", "Sona"), listOf("Nami", "Lulu", "Janna", "Braum", "Yuumi"))
        ),
        "veigar" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Kennen", "Aatrox", "Akali", "Volibear"), listOf("Darius", "Nasus", "Wukong", "Fiora", "Gwen"), listOf("Gwen", "Irelia", "Riven", "Camille", "Malphite")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kayn", "Lillia", "Lee Sin", "Amumu", "Jarvan IV"), listOf("Xin Zhao", "Gragas", "Maestro Yi", "Talon", "Rengar"), listOf("Gragas", "Xin Zhao", "Wukong", "Volibear", "Evelynn")),
            LaneRole.MID to MatchupRoleResult(listOf("Vex", "Twisted Fate", "Yone", "Orianna", "Aurelion Sol"), listOf("Galio", "Renekton", "Kassadin", "Katarina", "Annie"), listOf("Katarina", "Vex", "Galio", "Aurelion Sol", "Talon")),
            LaneRole.ADC to MatchupRoleResult(listOf("Lucian", "Samira", "Kai'Sa", "Vayne", "Xayah"), listOf("Varus", "Ashe", "Sivir", "Jhin", "Ezreal"), listOf("Kai'Sa", "Xayah", "Tristana", "Samira", "Draven")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Rakan", "Thresh", "Yuumi", "Alistar"), listOf("Seraphine", "Morgana", "Janna", "Pyke", "Nami"), listOf("Rakan", "Blitzcrank", "Janna", "Pyke", "Braum"))
        ),
        "viego" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Volibear", "Jayce", "Sion", "Yone", "Ornn"), listOf("Kennen", "Urgot", "Darius", "Fiora", "Camille"), listOf("Sett", "Malphite", "Kennen", "Wukong", "Shen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Graves", "Vi", "Wukong", "Shyvana", "Diana"), listOf("Rammus", "Evelynn", "Nunu y Willump", "Ekko", "Olaf"), listOf()),
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Zed", "Syndra", "Galio", "Orianna"), listOf("Twisted Fate", "Vex", "Kassadin", "Katarina", "Ahri"), listOf("Vex", "Twisted Fate", "Gragas", "Annie", "Aurelion Sol")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Varus", "Miss Fortune", "Caitlyn", "Sivir"), listOf("Nilah", "Tristana", "Ashe", "Lucian", "Vayne"), listOf("Lucian", "Xayah", "Jhin", "Sivir", "Varus")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lux", "Karma", "Yuumi", "Nautilus", "Soraka"), listOf("Braum", "Rakan", "Thresh", "Janna", "Leona"), listOf("Janna", "Braum", "Sona", "Leona", "Blitzcrank"))
        ),
        "vladimir" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Gragas", "Ornn", "Nasus", "Yone", "Teemo"), listOf("Fiora", "Jayce", "Darius", "Urgot", "Camille"), listOf("Singed", "Riven", "Pantheon", "Camille", "Sett")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Vi", "Rammus", "Wukong", "Amumu", "Zed"), listOf("Xin Zhao", "Kha'Zix", "Jarvan IV", "Lillia", "Gwen"), listOf("Wukong", "Kayn", "Nunu y Willump", "Fiddlesticks", "Rengar")),
            LaneRole.MID to MatchupRoleResult(listOf("Vex", "Fizz", "Akali", "Katarina", "Ahri"), listOf("Kassadin", "Orianna", "Twisted Fate", "Galio", "Aurelion Sol"), listOf("Galio", "Lux", "Orianna", "Talon", "Zed")),
            LaneRole.ADC to MatchupRoleResult(listOf("Xayah", "Jhin", "Lucian", "Kai'Sa", "Samira"), listOf("Tristana", "Nilah", "Jinx", "Ashe", "Zeri"), listOf("Nilah", "Ashe", "Jhin", "Zeri", "Xayah")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Nami", "Rakan", "Nautilus", "Pyke"), listOf("Soraka", "Lulu", "Thresh", "Janna", "Leona"), listOf("Thresh", "Alistar", "Soraka", "Pyke", "Leona"))
        ),
        "volibear" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Pantheon", "Garen", "Yasuo", "Jayce", "Sion"), listOf("Nasus", "Darius", "Gragas", "Akali", "Camille"), listOf("Darius", "Camille", "Shen", "Garen", "Irelia")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Wukong", "Hecarim", "Shyvana", "Vi", "Kha'Zix"), listOf("Gwen", "Talon", "Olaf", "Ekko", "Lillia"), listOf("Evelynn", "Amumu", "Wukong", "Fiddlesticks", "Kha'Zix")),
            LaneRole.MID to MatchupRoleResult(listOf("Syndra", "Veigar", "Karma", "Lux", "Fizz"), listOf("Sona", "Aurelion Sol", "Annie", "Galio", "Ahri"), listOf("Fizz", "Annie", "Gragas", "Diana", "Vex")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Zeri", "Miss Fortune", "Kalista", "Caitlyn"), listOf("Ashe", "Jinx", "Lucian", "Tristana", "Sivir"), listOf("Miss Fortune", "Sivir", "Jhin", "Draven", "Samira")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Nautilus", "Lulu", "Soraka", "Alistar"), listOf("Pyke", "Braum", "Janna", "Blitzcrank", "Morgana"), listOf("Janna", "Sona", "Braum", "Lulu", "Nami"))
        ),
        "wukong" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Renekton", "Aatrox", "Dr. Mundo", "Irelia", "Pantheon"), listOf("Fiora", "Jax", "Shen", "Urgot", "Camille"), listOf("Jax", "Malphite", "Garen", "Gwen", "Shen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Kayn", "Rammus", "Vi", "Maestro Yi"), listOf("Evelynn", "Gragas", "Nunu y Willump", "Lillia", "Fiddlesticks"), listOf("Lillia", "Amumu", "Nunu y Willump", "Lee Sin", "Kha'Zix")),
            LaneRole.MID to MatchupRoleResult(listOf("Diana", "Katarina", "Lux", "Zed", "Talon"), listOf("Zoe", "Vex", "Kassadin", "Vladimir", "Diana"), listOf("Galio", "Zoe", "Annie", "Aurelion Sol", "Yasuo")),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Caitlyn", "Sivir", "Draven", "Miss Fortune"), listOf("Xayah", "Tristana", "Nilah", "Ashe", "Kai'Sa"), listOf("Jhin", "Xayah", "Draven", "Sivir", "Caitlyn")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nautilus", "Pyke", "Braum", "Alistar", "Yuumi"), listOf("Soraka", "Janna", "Nami", "Sona", "Lulu"), listOf("Nami", "Nautilus", "Thresh", "Rakan", "Pyke"))
        ),
        "yone" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Wukong", "Sett", "Gwen", "Aatrox", "Ornn"), listOf("Nasus", "Riven", "Jax", "Camille", "Shen"), listOf("Wukong", "Camille", "Irelia", "Urgot", "Vladimir")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Kayn", "Rammus", "Nunu y Willump", "Vi"), listOf("Kha'Zix", "Evelynn", "Talon", "Fiddlesticks", "Jax"), listOf("Vi", "Nunu y Willump", "Evelynn", "Hecarim", "Fiddlesticks")),
            LaneRole.MID to MatchupRoleResult(listOf("Ekko", "Diana", "Orianna", "Aurelion Sol", "Zoe"), listOf("Vex", "Jayce", "Pantheon", "Gragas", "Twisted Fate"), listOf("Kassadin", "Diana", "Karma", "Galio", "Zoe")),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Varus", "Caitlyn", "Ezreal", "Sivir"), listOf("Tristana", "Lucian", "Xayah", "Draven", "Ashe"), listOf("Jhin", "Xayah", "Ashe", "Samira", "Nilah")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nautilus", "Soraka", "Thresh", "Yuumi", "Lux"), listOf("Rakan", "Janna", "Alistar", "Braum", "Blitzcrank"), listOf("Janna", "Rakan", "Nautilus", "Soraka", "Pyke"))
        ),
        "yunara" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Mordekaiser", "Renekton", "Gnar", "Rumble"), listOf("Urgot", "Singed", "Malphite", "Shen", "Dr. Mundo"), listOf("Shen", "Fiora", "Camille", "Garen", "Poppy")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Pantheon", "Viego", "Shyvana", "Nocturne", "Xin Zhao"), listOf("Rammus", "Nidalee", "Taliyah", "Kindred", "Warwick"), listOf("Jarvan IV", "Rammus", "Fiddlesticks", "Wukong", "Hecarim")),
            LaneRole.MID to MatchupRoleResult(listOf("Mel", "Yasuo", "Veigar", "Kassadin", "Ryze"), listOf("Twisted Fate", "Katarina", "Syndra", "Zoe", "Ekko"), listOf("Annie", "Zed", "Lissandra", "Aurelion Sol", "Vex")),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Sivir", "Kai'Sa", "Ezreal", "Varus"), listOf("Kog'Maw", "Tristana", "Jinx", "Ashe", "Xayah"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nami", "Karma", "Milio", "Maokai", "Lux"), listOf("Alistar", "Rell", "Leona", "Lulu", "Thresh"), listOf("Braum", "Thresh", "Leona", "Rell", "Nautilus"))
        ),
        "zed" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Aatrox", "Renekton", "Akali", "Teemo", "Jayce"), listOf("Wukong", "Shen", "Kennen", "Gwen", "Riven"), listOf("Shen", "Darius", "Nasus", "Teemo", "Kennen")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Jarvan IV", "Lillia", "Vi", "Talon"), listOf("Gragas", "Ekko", "Nunu y Willump", "Xin Zhao", "Kindred"), listOf("Evelynn", "Amumu", "Rammus", "Fiddlesticks", "Lillia")),
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Fizz", "Vex", "Kassadin", "Aurelion Sol"), listOf("Pantheon", "Malphite", "Renekton", "Diana", "Ekko"), listOf("Kassadin", "Orianna", "Zoe", "Annie", "Vex")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Zeri", "Jinx", "Miss Fortune", "Kai'Sa"), listOf("Xayah", "Tristana", "Draven", "Ashe"), listOf("Ziggs", "Draven", "Ashe", "Jhin", "Xayah")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lux", "Yuumi", "Seraphine", "Thresh", "Nami"), listOf("Rakan", "Blitzcrank", "Soraka", "Braum", "Janna"), listOf("Janna", "Rakan", "Thresh", "Alistar", "Nami"))
        ),
        "zeri" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Aatrox", "Sion", "Sett", "Yone", "Ornn"), listOf("Gragas", "Darius", "Irelia", "Urgot", "Kennen"), listOf("Shen", "Riven", "Jax", "Camille", "Darius")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Kayn", "Lee Sin", "Viego", "Wukong"), listOf("Nunu y Willump", "Vi", "Evelynn", "Rengar", "Hecarim"), listOf("Gragas", "Nunu y Willump", "Vi", "Volibear", "Rammus")),
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Ekko", "Yone", "Orianna", "Annie"), listOf("Pantheon", "Renekton", "Kassadin", "Twisted Fate", "Galio"), listOf("Galio", "Katarina", "Jayce", "Kassadin", "Annie")),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Kai'Sa", "Vayne", "Xayah", "Jhin"), listOf("Draven", "Caitlyn", "Tristana", "Ashe", "Kalista"), listOf()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Lulu", "Rakan", "Nautilus", "Morgana"), listOf("Alistar", "Soraka", "Blitzcrank", "Thresh", "Leona"), listOf("Rakan", "Nami", "Leona", "Lulu", "Braum"))
        ),
        "zyra" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Volibear", "Yone", "Urgot", "Aatrox", "Renekton"), listOf("Gwen", "Tryndamere", "Singed", "Camille", "Fiora"), listOf("Irelia", "Urgot", "Gragas", "Fiora", "Camille")),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Gragas", "Lee Sin", "Vi", "Wukong", "Jarvan IV"), listOf("Rengar", "Shyvana", "Talon", "Lillia", "Viego"), listOf("Nunu y Willump", "Lillia", "Graves", "Veigar", "Vi")),
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Kassadin", "Yasuo", "Syndra", "Orianna"), listOf("Twisted Fate", "Veigar", "Annie", "Galio", "Akali"), listOf("Vladimir", "Ekko", "Galio", "Zoe", "Ahri")),
            LaneRole.ADC to MatchupRoleResult(listOf("Sivir", "Kalista", "Caitlyn", "Miss Fortune", "Draven"), listOf("Tristana", "Zeri", "Jinx", "Xayah", "Ezreal"), listOf("Jhin", "Draven", "Ezreal", "Ashe", "Varus")),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Karma", "Senna", "Lulu", "Thresh", "Lux"), listOf("Janna", "Pyke", "Yuumi", "Nautilus", "Soraka"), listOf("Soraka", "Nautilus", "Blitzcrank", "Thresh", "Pyke"))
        )
    )
}
