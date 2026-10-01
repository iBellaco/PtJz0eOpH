package com.example.util

import com.example.model.LaneRole

/** Additional editorial lane matchups; see docs/matchup-reference-audit.json. */
object ReviewedLaneMatchups {
    val byChampion: Map<String, Map<LaneRole, MatchupRoleResult>> = mapOf(
        "aatrox" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Dr. Mundo", "Yone", "Sion", "Olaf", "Jayce", "Shen"), listOf("Fiora", "Irelia", "Darius", "Jax", "Sett", "Riven"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Diana", "Twisted Fate", "Yone", "Fizz", "Yasuo"), listOf("Vex", "Akali", "Ahri", "Galio", "Irelia", "Zed"), emptyList())
        ),
        "ahri" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Gragas", "Katarina", "Brand", "Lux", "Akali"), listOf("Yasuo", "Ekko", "Diana", "Irelia", "Zed", "Kassadin"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Katarina", "Riven", "Akali", "Rengar", "Annie", "Nasus"), listOf("Olaf", "Teemo", "Darius", "Fiora", "Ornn", "Sett"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Ashe", "Caitlyn", "Draven", "Jinx", "Zeri", "Miss Fortune"), listOf("Samira", "Nilah", "Xayah", "Ezreal", "Tristana", "Sivir"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nautilus", "Zyra", "Janna", "Soraka", "Rakan", "Leona"), listOf("Blitzcrank", "Lux", "Morgana", "Sona", "Lulu", "Karma"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Gwen", "Nunu y Willump", "Xin Zhao", "Jarvan IV", "Maestro Yi"), listOf("Vi", "Warwick", "Rengar", "Hecarim", "Graves"), emptyList())
        ),
        "akali" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Yasuo", "Ziggs", "Twisted Fate", "Veigar", "Katarina", "Orianna"), listOf("Kassadin", "Galio", "Annie", "Veigar", "Akshan", "Vex"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Teemo", "Yone", "Jax", "Olaf", "Kayle", "Gragas"), listOf("Rengar", "Shen", "Riven", "Urgot", "Pantheon", "Singed"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Zeri", "Sivir", "Twitch", "Ashe", "Miss Fortune"), listOf("Samira", "Lucian", "Xayah", "Vayne", "Tristana"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rengar", "Ekko", "Nunu y Willump", "Maestro Yi", "Evelynn", "Xin Zhao"), listOf("Viego", "Kayn", "Hecarim", "Graves", "Lee Sin"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Zyra", "Lux", "Thresh", "Lulu", "Alistar", "Senna"), listOf("Morgana", "Nami", "Blitzcrank", "Rakan", "Braum", "Janna"), emptyList())
        ),
        "akshan" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Kai'Sa", "Corki", "Akali", "Lux", "Katarina", "Lucian"), listOf("Yasuo", "Irelia", "Lissandra", "Veigar", "Zed", "Fizz"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Caitlyn", "Lucian", "Ashe", "Ezreal", "Draven", "Nilah"), listOf("Jinx", "Jhin", "Vayne", "Xayah", "Miss Fortune", "Kalista"), emptyList())
        ),
        "alistar" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Blitzcrank", "Leona", "Braum", "Nami", "Senna", "Yuumi"), listOf("Janna", "Morgana", "Thresh", "Zyra", "Lulu", "Rakan"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Kai'Sa", "Jinx", "Zeri", "Twitch"), listOf("Vayne", "Tristana", "Varus", "Lucian", "Caitlyn", "Ashe"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Riven", "Irelia", "Garen", "Yasuo", "Tryndamere", "Sion"), listOf("Galio", "Dr. Mundo", "Renekton", "Gragas", "Urgot", "Vayne"), emptyList())
        ),
        "ambessa" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Gwen", "Yasuo", "Jax", "Irelia", "Vladimir", "Teemo"), listOf("Pantheon", "Riven", "Darius", "Garen", "Jayce", "Poppy"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Gragas", "Kindred", "Lillia", "Maestro Yi", "Jarvan IV"), listOf("Shyvana", "Kha'Zix", "Wukong", "Viego", "Fiddlesticks", "Warwick"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Zed", "Fizz", "Talon", "Akali", "Diana"), listOf("Ziggs", "Lux", "Syndra", "Ahri", "Veigar", "Zoe"), emptyList())
        ),
        "amumu" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Yasuo", "Olaf", "Diana", "Jax", "Gragas", "Rammus"), listOf("Shyvana", "Lee Sin", "Vi", "Warwick", "Kha'Zix", "Volibear"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Jax", "Riven", "Tryndamere", "Shen", "Akali", "Gwen"), listOf("Vi", "Garen", "Pantheon", "Malphite", "Renekton", "Jayce"), emptyList())
        ),
        "annie" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Ahri", "Katarina", "Kayle", "Zed", "Irelia"), listOf("Kassadin", "Brand", "Orianna", "Fizz", "Veigar", "Lux"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Darius", "Renekton", "Yasuo", "Shen", "Akali", "Zed"), listOf("Malphite", "Garen", "Katarina", "Wukong", "Swain", "Kassadin"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Thresh", "Braum", "Lucian", "Lulu", "Soraka", "Kai'Sa"), listOf("Karma", "Ashe", "Leona", "Caitlyn", "Morgana", "Tristana"), emptyList())
        ),
        "ashe" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Zeri", "Kai'Sa", "Varus", "Miss Fortune", "Xayah", "Vayne"), listOf("Caitlyn", "Draven", "Jhin", "Tristana", "Ezreal", "Lucian"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Nami", "Karma", "Alistar", "Janna", "Soraka"), listOf("Blitzcrank", "Nautilus", "Seraphine", "Thresh", "Leona", "Sona"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Darius", "Kayle", "Gwen", "Akali", "Renekton", "Volibear"), listOf("Jax", "Shen", "Ornn", "Warwick", "Vladimir", "Olaf"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Corki", "Irelia", "Akali", "Aurelion Sol", "Malphite", "Galio"), listOf("Ahri", "Kassadin", "Talon", "Lux", "Zed", "Fizz"), emptyList())
        ),
        "aurelion_sol" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Katarina", "Irelia", "Orianna", "Diana", "Brand"), listOf("Fizz", "Zed", "Yasuo", "Katarina", "Karma", "Galio"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Riven", "Nasus", "Fiora", "Garen", "Malphite", "Dr. Mundo"), listOf("Darius", "Kennen", "Pantheon", "Tryndamere", "Jayce", "Kassadin"), emptyList())
        ),
        "aurora" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Yone", "Ryze", "Corki", "Lissandra", "Viktor", "Kassadin"), listOf("Vex", "Talon", "Kayle", "Ekko", "Zoe", "Katarina"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Kalista", "Xayah", "Ezreal", "Nilah", "Ashe"), listOf("Tristana", "Lucian", "Sivir", "Ziggs", "Draven", "Vayne"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Brand", "Blitzcrank", "Senna", "Lissandra", "Leona", "Braum"), listOf("Sona", "Zyra", "Yuumi", "Seraphine", "Morgana", "Nautilus"), emptyList())
        ),
        "blitzcrank" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Sona", "Soraka", "Nami", "Lulu", "Yuumi", "Janna"), listOf("Leona", "Alistar", "Morgana", "Thresh", "Braum", "Rakan"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Draven", "Varus", "Ashe", "Jinx", "Nilah"), listOf("Ezreal", "Lucian", "Tristana", "Caitlyn", "Jhin"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Renekton", "Wukong", "Jax", "Nasus", "Fiora", "Teemo"), listOf("Garen", "Darius", "Yasuo", "Dr. Mundo"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Orianna", "Veigar", "Annie", "Akali", "Talon"), listOf("Lux", "Zed", "Gragas", "Ekko", "Jayce", "Yasuo"), emptyList())
        ),
        "brand" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Ziggs", "Akali", "Katarina", "Akshan", "Corki"), listOf("Fizz", "Galio", "Lux", "Zed", "Corki", "Ekko"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Gragas", "Graves", "Jax", "Amumu", "Jarvan IV"), listOf("Rengar", "Kha'Zix", "Viego", "Kayn", "Diana", "Evelynn"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Seraphine", "Lulu", "Soraka", "Senna", "Thresh"), listOf("Maokai", "Pyke", "Nautilus", "Blitzcrank", "Morgana", "Braum"), emptyList())
        ),
        "braum" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Blitzcrank", "Thresh", "Jinx", "Jayce", "Nami"), listOf("Morgana", "Leona", "Soraka", "Alistar", "Karma", "Vayne"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Miss Fortune", "Blitzcrank", "Thresh", "Jinx", "Jayce", "Nami"), listOf("Morgana", "Leona", "Soraka", "Alistar", "Karma", "Vayne"), emptyList())
        ),
        "caitlyn" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Zeri", "Xayah", "Kai'Sa", "Lucian", "Sivir"), listOf("Jhin", "Ezreal", "Miss Fortune", "Nilah", "Twitch", "Jinx"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Twisted Fate", "Akali", "Ziggs", "Orianna", "Ahri"), listOf("Diana", "Jayce", "Irelia", "Lux", "Fizz", "Yasuo"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Ashe", "Renekton", "Jax", "Nasus", "Draven", "Yasuo"), listOf("Darius", "Jinx", "Teemo", "Fiora", "Zed", "Vayne"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Morgana", "Leona", "Zyra", "Rakan", "Lulu", "Senna"), listOf("Blitzcrank", "Soraka", "Nautilus", "Nami", "Thresh", "Sona"), emptyList())
        ),
        "camille" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Yone", "Wukong", "Ornn", "Irelia", "Aatrox"), listOf("Renekton", "Darius", "Fiora", "Riven", "Nasus", "Shen"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Lucian", "Fizz", "Syndra", "Yasuo", "Orianna", "Akshan"), listOf("Ekko", "Kayle", "Ahri", "Malphite", "Gragas", "Vex"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Shyvana", "Kha'Zix", "Evelynn", "Viego", "Vi", "Jarvan IV"), listOf("Rammus", "Gragas", "Hecarim", "Warwick", "Xin Zhao"), emptyList())
        ),
        "corki" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Ziggs", "Katarina", "Akali", "Orianna", "Yasuo", "Ekko"), listOf("Zed", "Fizz", "Lux", "Ahri", "Brand", "Syndra"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Tristana", "Vayne", "Ezreal", "Jinx", "Miss Fortune", "Nilah"), listOf("Caitlyn", "Draven", "Ashe", "Varus", "Lucian", "Sivir"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Blitzcrank", "Seraphine", "Nami", "Senna", "Yuumi", "Soraka"), listOf("Rakan", "Leona", "Braum", "Thresh", "Lulu", "Alistar"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Riven", "Nunu y Willump", "Pantheon", "Garen", "Yasuo", "Rammus"), listOf("Nasus", "Jax", "Teemo", "Wukong", "Zed", "Maokai"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Nunu y Willump", "Amumu", "Evelynn", "Gragas", "Jarvan IV", "Xin Zhao"), listOf("Lee Sin", "Hecarim", "Warwick", "Viego", "Maestro Yi", "Kayn"), emptyList())
        ),
        "darius" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Garen", "Renekton", "Yasuo", "Akali", "Sion", "Fiora"), listOf("Vayne", "Ornn", "Kayle", "Dr. Mundo", "Jayce", "Urgot"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jax", "Warwick", "Rammus", "Gragas", "Shyvana", "Jarvan IV"), listOf("Wukong", "Nunu y Willump", "Maestro Yi", "Ekko", "Evelynn", "Viego"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Lux", "Veigar", "Nasus", "Vladimir", "Vex", "Aurelion Sol"), listOf("Katarina", "Twisted Fate", "Ahri", "Fizz", "Ekko", "Viktor"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Twitch", "Samira", "Draven", "Ezreal", "Zeri", "Xayah"), listOf("Vayne", "Tristana", "Lucian", "Jhin", "Nilah", "Ziggs"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Seraphine", "Thresh", "Sona", "Senna", "Nautilus"), listOf("Veigar", "Morgana", "Zyra", "Braum", "Karma", "Nami"), emptyList())
        ),
        "diana" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Ahri", "Twisted Fate", "Orianna", "Katarina", "Ezreal"), listOf("Kayle", "Fizz", "Pantheon", "Gragas", "Ekko", "Jayce"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Teemo", "Singed", "Olaf", "Darius", "Sion"), listOf("Irelia", "Jax", "Nasus", "Tryndamere", "Kayle"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Tryndamere", "Rammus", "Lee Sin", "Xin Zhao", "Rengar", "Maestro Yi"), listOf("Amumu", "Warwick", "Gragas", "Graves", "Ekko", "Hecarim"), emptyList())
        ),
        "dr_mundo" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Wukong", "Irelia", "Jax", "Singed", "Renekton", "Tryndamere"), listOf("Darius", "Vayne", "Gwen", "Fiora", "Camille"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Amumu", "Olaf", "Jax", "Shyvana", "Diana", "Wukong"), listOf("Gragas", "Xin Zhao", "Kayn", "Viego", "Lillia", "Ekko"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Akali", "Zed", "Yone", "Irelia", "Jayce"), listOf("Annie", "Katarina", "Karma", "Ahri", "Ekko", "Fizz"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Thresh", "Sona", "Nami", "Shen", "Yuumi", "Senna"), listOf("Lulu", "Leona", "Zyra", "Brand", "Alistar", "Rakan"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Caitlyn", "Jinx", "Ezreal", "Jhin", "Senna", "Sivir"), listOf("Vayne", "Lucian", "Ashe", "Xayah", "Kai'Sa", "Zeri"), emptyList())
        ),
        "draven" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Sivir", "Zeri", "Varus", "Kai'Sa", "Kalista", "Caitlyn"), listOf("Senna", "Jhin", "Ashe", "Nilah", "Twitch"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Renekton", "Fiora", "Dr. Mundo", "Darius", "Vladimir"), listOf("Urgot", "Kennen", "Malphite", "Sett", "Camille", "Shen"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Ekko", "Aatrox", "Vladimir", "Galio", "Swain", "Fizz"), listOf("Yasuo", "Irelia", "Diana", "Annie", "Syndra", "Katarina"), emptyList())
        ),
        "ekko" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Twisted Fate", "Veigar", "Katarina", "Corki", "Annie"), listOf("Galio", "Swain", "Irelia", "Kassadin", "Yasuo", "Akali"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Tryndamere", "Darius", "Teemo", "Shen", "Malphite", "Gwen"), listOf("Nasus", "Riven", "Wukong", "Garen", "Sett", "Jax"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Lee Sin", "Jarvan IV", "Xin Zhao", "Graves", "Maestro Yi"), listOf("Kha'Zix", "Rengar", "Amumu", "Vi", "Nunu y Willump", "Talon"), emptyList())
        ),
        "evelynn" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Graves", "Dr. Mundo", "Jarvan IV", "Amumu", "Wukong", "Gragas"), listOf("Kha'Zix", "Xin Zhao", "Lee Sin", "Maestro Yi", "Rengar", "Fiddlesticks"), emptyList())
        ),
        "ezreal" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Ashe", "Varus", "Miss Fortune", "Lucian", "Kai'Sa", "Zeri"), listOf("Nilah", "Vayne", "Draven", "Tristana", "Kalista"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Syndra", "Ziggs", "Brand", "Twisted Fate", "Vex"), listOf("Yasuo", "Lux", "Zed", "Ekko", "Jayce", "Zoe"), emptyList())
        ),
        "fiddlesticks" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kayn", "Jarvan IV", "Wukong", "Lee Sin", "Jax", "Rammus"), listOf("Shyvana", "Vi", "Evelynn", "Ekko", "Xin Zhao"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Shen", "Vayne", "Gragas", "Nasus", "Camille", "Teemo"), listOf("Kennen", "Ornn", "Dr. Mundo", "Yasuo", "Tryndamere"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Lux", "Vladimir", "Katarina", "Akali", "Zed", "Brand"), listOf("Diana", "Pantheon", "Veigar", "Galio", "Zoe", "Fizz"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Seraphine", "Leona", "Karma", "Lulu", "Braum", "Sona"), listOf("Thresh", "Blitzcrank", "Soraka", "Nami", "Rakan", "Zyra"), emptyList())
        ),
        "fiora" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Dr. Mundo", "Nasus", "Rengar", "Irelia", "Yone"), listOf("Warwick", "Vayne", "Pantheon", "Darius", "Renekton", "Malphite"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Ekko", "Xin Zhao", "Viego", "Shyvana", "Rammus", "Brand"), listOf("Lillia", "Kha'Zix", "Graves", "Shyvana", "Jarvan IV"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Irelia", "Galio", "Ekko", "Riven", "Yasuo"), listOf("Garen", "Tristana", "Vladimir", "Malphite", "Jayce", "Zoe"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Jhin", "Twitch", "Ziggs", "Ezreal", "Miss Fortune"), listOf("Jinx", "Caitlyn", "Sivir", "Draven", "Vayne", "Varus"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Senna", "Morgana", "Seraphine", "Thresh", "Lux", "Sona"), listOf("Zyra", "Rakan", "Braum", "Leona", "Lulu", "Soraka"), emptyList())
        ),
        "fizz" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Brand", "Syndra", "Aurelion Sol", "Katarina", "Zoe"), listOf("Diana", "Gragas", "Kassadin", "Akali", "Kayle", "Annie"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Karma", "Jayce", "Nasus", "Swain", "Kennen", "Ornn"), listOf("Dr. Mundo", "Darius", "Olaf", "Tryndamere", "Sett", "Wukong"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Jinx", "Kalista", "Zeri", "Samira", "Jhin"), listOf("Draven", "Ziggs", "Vayne", "Tristana", "Nilah"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Senna", "Leona", "Nautilus", "Maokai", "Nami"), listOf("Rakan", "Lulu", "Lux", "Thresh", "Janna", "Sona"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Ekko", "Fiddlesticks", "Shyvana", "Volibear", "Xin Zhao", "Lee Sin"), listOf("Kayn", "Evelynn", "Vi", "Warwick", "Kha'Zix", "Maestro Yi"), emptyList())
        ),
        "galio" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Brand", "Malphite", "Veigar", "Katarina", "Fizz", "Diana"), listOf("Yasuo", "Akali", "Orianna", "Ahri", "Vex", "Syndra"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Singed", "Renekton", "Teemo", "Kennen", "Galio", "Kayle"), listOf("Malphite", "Jax", "Riven", "Pantheon", "Sett", "Urgot"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Thresh", "Morgana", "Sona", "Soraka", "Seraphine", "Blitzcrank"), listOf("Alistar", "Lulu", "Janna", "Braum", "Zyra", "Lux"), emptyList())
        ),
        "garen" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Akali", "Irelia", "Jax", "Dr. Mundo", "Yasuo"), listOf("Darius", "Camille", "Kayle", "Fiora", "Shen", "Ornn"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Shyvana", "Wukong", "Hecarim", "Nunu y Willump", "Viego", "Amumu"), listOf("Rengar", "Fiddlesticks", "Maestro Yi", "Jarvan IV", "Kayn"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Galio", "Diana", "Ekko", "Zed", "Yasuo", "Aurelion Sol"), listOf("Swain", "Aatrox", "Annie", "Malphite", "Orianna", "Veigar"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Nilah", "Xayah", "Samira", "Ashe", "Jhin"), listOf("Draven", "Swain", "Vayne", "Lucian", "Jinx", "Zeri"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Rakan", "Pyke", "Blitzcrank", "Alistar", "Leona"), listOf("Morgana", "Soraka", "Nami", "Lulu", "Nautilus", "Thresh"), emptyList())
        ),
        "gnar" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Garen", "Olaf", "Sett", "Gwen", "Aatrox"), listOf("Irelia", "Darius", "Olaf", "Renekton", "Nasus", "Camille"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Lux", "Akali", "Malphite", "Fizz", "Orianna", "Twisted Fate"), listOf("Galio", "Zed", "Ekko", "Zoe", "Yasuo", "Vladimir"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Vi", "Diana", "Hecarim", "Shyvana", "Jarvan IV", "Dr. Mundo"), listOf("Lillia", "Viego", "Kayn", "Pantheon", "Maestro Yi"), emptyList())
        ),
        "gragas" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Malphite", "Riven", "Shen", "Ornn", "Wukong", "Sion"), listOf("Pantheon", "Aatrox", "Jax", "Darius", "Kennen", "Garen"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Xin Zhao", "Lillia", "Talon", "Jarvan IV", "Nunu y Willump", "Maestro Yi"), listOf("Lee Sin", "Rengar", "Kha'Zix", "Viego", "Evelynn", "Diana"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Katarina", "Kayle", "Ziggs", "Zed", "Yone"), listOf("Yasuo", "Ahri", "Fizz", "Akali", "Lux", "Kassadin"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Ashe", "Jinx", "Zeri", "Varus", "Ezreal"), listOf("Vayne", "Kalista", "Jhin", "Twitch", "Kai'Sa"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Thresh", "Sona", "Seraphine", "Soraka", "Zyra", "Blitzcrank"), listOf("Morgana", "Alistar", "Janna", "Nautilus", "Leona", "Lulu"), emptyList())
        ),
        "graves" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Maestro Yi", "Wukong", "Jarvan IV", "Lee Sin", "Hecarim", "Amumu"), listOf("Evelynn", "Vi", "Rengar", "Kha'Zix", "Rammus", "Fiddlesticks"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Galio", "Talon", "Akali", "Kassadin", "Katarina"), listOf("Zed", "Irelia", "Jayce", "Fizz", "Vex", "Aurelion Sol"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Darius", "Riven", "Vayne", "Garen", "Nasus", "Nunu y Willump"), listOf("Fiora", "Renekton", "Gwen", "Wukong", "Pantheon", "Camille"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Varus", "Ezreal", "Vayne", "Ashe", "Lucian", "Jinx"), listOf("Caitlyn", "Miss Fortune", "Draven", "Tristana", "Jhin", "Samira"), emptyList())
        ),
        "gwen" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Shyvana", "Dr. Mundo", "Shen", "Teemo", "Sion", "Ornn"), listOf("Fiora", "Jax", "Riven", "Darius", "Nasus", "Tryndamere"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Zoe", "Ekko", "Talon", "Orianna", "Veigar", "Aurelion Sol"), listOf("Yasuo", "Riven", "Kayle", "Jayce", "Zed", "Swain"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Olaf", "Fiddlesticks", "Rammus", "Amumu", "Lillia", "Jarvan IV"), listOf("Rengar", "Kha'Zix", "Evelynn", "Maestro Yi", "Lee Sin", "Warwick"), emptyList())
        ),
        "hecarim" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Diana", "Shyvana", "Rammus", "Wukong", "Lee Sin", "Nunu y Willump"), listOf("Maestro Yi", "Evelynn", "Warwick", "Vi", "Jarvan IV", "Graves"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Shen", "Riven", "Yone", "Kayle", "Fiora"), listOf("Renekton", "Camille", "Darius", "Akali", "Jayce", "Sett"), emptyList())
        ),
        "heimerdinger" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Teemo", "Yone", "Akali", "Jayce", "Yasuo", "Gragas"), listOf("Nasus", "Irelia", "Camille", "Garen", "Fiora", "Renekton"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Malphite", "Yone", "Vex", "Irelia", "Akali"), listOf("Zoe", "Veigar", "Ekko", "Syndra", "Lux", "Zed"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Pyke", "Senna", "Morgana", "Karma", "Blitzcrank"), listOf("Sona", "Seraphine", "Soraka", "Nami", "Brand", "Janna"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Samira", "Miss Fortune", "Varus", "Nilah", "Twitch"), listOf("Jhin", "Vayne", "Ashe", "Caitlyn", "Tristana", "Xayah"), emptyList())
        ),
        "hwei" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Mel", "Orianna", "Ryze", "Vladimir", "Aurelion Sol", "Swain"), listOf("Fizz", "Zoe", "Kassadin", "Ahri", "Vel'Koz", "Katarina"), emptyList())
        ),
        "irelia" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Corki", "Syndra", "Fizz", "Zed", "Twisted Fate", "Yone"), listOf("Ekko", "Zoe", "Vex", "Annie", "Ahri", "Yasuo"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Vayne", "Yone", "Aatrox", "Vladimir", "Jayce", "Dr. Mundo"), listOf("Garen", "Darius", "Renekton", "Malphite", "Nasus", "Sett"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Ziggs", "Miss Fortune", "Zeri", "Varus", "Vayne"), listOf("Tristana", "Samira", "Nilah", "Twitch", "Sivir", "Ashe"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nami", "Seraphine", "Soraka", "Janna", "Karma", "Lux"), listOf("Thresh", "Blitzcrank", "Morgana", "Alistar", "Braum", "Leona"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Shyvana", "Lee Sin", "Evelynn", "Diana", "Jarvan IV", "Wukong"), listOf("Rammus", "Amumu", "Lillia", "Ekko", "Viego", "Kayn"), emptyList())
        ),
        "janna" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Kennen", "Alistar", "Leona", "Morgana", "Thresh", "Nunu y Willump"), listOf("Nami", "Blitzcrank", "Lux", "Sona", "Soraka", "Senna"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Varus", "Ezreal", "Vayne", "Twitch", "Ashe", "Jinx"), listOf("Nilah", "Miss Fortune", "Zeri", "Lucian", "Caitlyn"), emptyList())
        ),
        "jarvan_iv" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Malphite", "Evelynn", "Olaf", "Kha'Zix", "Maokai", "Rengar"), listOf("Vi", "Lee Sin", "Xin Zhao", "Nunu y Willump", "Ekko", "Gwen"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Teemo", "Kayle", "Riven", "Jayce", "Nasus", "Kennen"), listOf("Shen", "Renekton", "Jax", "Pantheon", "Fiora", "Darius"), emptyList())
        ),
        "jax" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Yasuo", "Wukong", "Irelia", "Kayle", "Gwen", "Volibear"), listOf("Renekton", "Garen", "Pantheon", "Urgot", "Dr. Mundo", "Singed"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Maestro Yi", "Xin Zhao", "Olaf", "Amumu", "Wukong", "Diana"), listOf("Kha'Zix", "Kayn", "Rammus", "Fiddlesticks", "Evelynn", "Lillia"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Corki", "Galio", "Twisted Fate", "Kassadin", "Yasuo"), listOf("Vladimir", "Kennen", "Orianna", "Syndra", "Annie", "Camille"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Varus", "Vayne", "Sivir", "Jhin"), listOf("Caitlyn", "Nilah", "Xayah", "Samira", "Kalista", "Lucian"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Seraphine", "Lux", "Blitzcrank", "Janna", "Sona", "Nautilus"), listOf("Lulu", "Zyra", "Rakan", "Alistar", "Thresh", "Brand"), emptyList())
        ),
        "jayce" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Singed", "Akali", "Volibear", "Teemo", "Darius", "Kayle"), listOf("Olaf", "Rengar", "Renekton", "Fiora", "Wukong", "Pantheon"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Katarina", "Akali", "Yone", "Karma", "Talon"), listOf("Brand", "Annie", "Galio", "Zoe", "Diana", "Vex"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Kalista", "Miss Fortune", "Ezreal", "Lucian", "Zeri", "Varus"), listOf("Senna", "Sivir", "Nilah", "Draven", "Tristana", "Jhin"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Zyra", "Soraka", "Rakan", "Morgana", "Leona", "Nami"), listOf("Senna", "Alistar", "Pyke", "Thresh", "Blitzcrank", "Braum"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Maokai", "Jarvan IV", "Hecarim", "Fiddlesticks", "Evelynn", "Vi"), listOf("Rammus", "Viego", "Maestro Yi", "Gragas", "Lee Sin"), emptyList())
        ),
        "jhin" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Xayah", "Caitlyn", "Varus", "Kalista", "Ezreal"), listOf("Lucian", "Vayne", "Tristana", "Nilah", "Twitch", "Jinx"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Kassadin", "Annie", "Zoe", "Lux", "Jayce", "Aurelion Sol"), listOf("Syndra", "Tristana", "Yasuo", "Galio", "Zed", "Diana"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Maokai", "Morgana", "Braum", "Alistar", "Janna", "Nami"), listOf("Sona", "Seraphine", "Leona", "Rakan", "Soraka", "Nautilus"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Hecarim", "Shyvana", "Nunu y Willump", "Maestro Yi", "Viego", "Graves"), listOf("Vi", "Kindred", "Kha'Zix", "Rengar", "Gragas"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Gwen", "Olaf", "Pantheon", "Irelia", "Riven", "Sett"), listOf("Fiora", "Camille", "Wukong", "Yasuo", "Volibear", "Vayne"), emptyList())
        ),
        "jinx" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Kai'Sa", "Jinx", "Varus", "Caitlyn"), listOf("Draven", "Tristana", "Xayah", "Twitch", "Nilah", "Senna"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Zoe", "Veigar", "Karma", "Twisted Fate", "Ezreal"), listOf("Ekko", "Yasuo", "Corki", "Ziggs", "Vladimir", "Lucian"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Ornn", "Gwen", "Wukong", "Darius", "Sett", "Camille"), listOf("Tryndamere", "Yone", "Jayce", "Brand", "Jax", "Maestro Yi"), emptyList())
        ),
        "k_sante" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Vladimir", "Ambessa", "Camille", "Yone", "Nasus", "Poppy"), listOf("Riven", "Urgot", "Jayce", "Renekton", "Kennen", "Fiora"), emptyList())
        ),
        "kai_sa" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Tristana", "Zeri", "Varus", "Kalista", "Jinx"), listOf("Vayne", "Xayah", "Ezreal", "Sivir", "Lucian", "Caitlyn"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Lux", "Akshan", "Kassadin", "Veigar", "Katarina", "Zoe"), listOf("Fizz", "Zed", "Irelia", "Yone", "Ekko", "Akali"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Janna", "Lulu", "Soraka", "Thresh", "Braum", "Alistar"), listOf("Seraphine", "Zyra", "Blitzcrank", "Nautilus", "Morgana", "Rakan"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Xin Zhao", "Jarvan IV", "Shyvana", "Hecarim", "Lee Sin", "Evelynn"), listOf("Rammus", "Rengar", "Vi", "Kha'Zix", "Amumu", "Viego"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Singed", "Ornn", "Sion", "Nasus", "Garen", "Darius"), listOf("Jayce", "Riven", "Irelia", "Vladimir", "Jax", "Urgot"), emptyList())
        ),
        "kalista" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Senna", "Ezreal", "Varus", "Jhin", "Sivir"), listOf("Nilah", "Xayah", "Lucian", "Twitch", "Ashe", "Zeri"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Brand", "Thresh", "Leona", "Rakan", "Janna", "Nami"), listOf("Morgana", "Sona", "Seraphine", "Karma", "Lulu", "Blitzcrank"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Jayce", "Garen", "Kayle", "Irelia", "Sion", "Volibear"), listOf("Camille", "Sett", "Riven", "Olaf", "Pantheon"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Varus", "Lux", "Yasuo", "Galio", "Akali"), listOf("Lucian", "Talon", "Zoe", "Malphite", "Veigar", "Aurelion Sol"), emptyList())
        ),
        "karma" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Diana", "Katarina", "Zed", "Akali", "Ezreal"), listOf("Veigar", "Lux", "Orianna", "Ahri", "Yasuo", "Ekko"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Pantheon", "Jax", "Singed", "Aatrox", "Swain", "Sett"), listOf("Fiora", "Olaf", "Nasus", "Vladimir", "Irelia", "Yone"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Braum", "Annie", "Janna", "Thresh", "Alistar"), listOf("Sona", "Lulu", "Blitzcrank", "Zyra", "Rakan", "Morgana"), emptyList())
        ),
        "kassadin" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Brand", "Veigar", "Ahri", "Annie", "Zoe"), listOf("Zed", "Yasuo", "Riven", "Yone", "Ekko", "Jayce"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Fizz", "Diana", "Singed", "Jax", "Shen", "Garen"), listOf("Irelia", "Yasuo", "Fiora", "Vi", "Riven", "Wukong"), emptyList())
        ),
        "katarina" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Lux", "Akali", "Morgana", "Brand", "Orianna"), listOf("Fizz", "Kayle", "Yasuo", "Pantheon", "Annie", "Ahri"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Darius", "Amumu", "Teemo", "Nasus", "Renekton", "Nunu y Willump"), listOf("Annie", "Riven", "Ahri", "Vi", "Irelia", "Pantheon"), emptyList())
        ),
        "kayle" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Darius", "Olaf", "Singed", "Fiora", "Garen", "Gwen"), listOf("Pantheon", "Riven", "Jax", "Wukong", "Jayce", "Vladimir"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Zed", "Yasuo", "Katarina", "Talon", "Aurelion Sol"), listOf("Annie", "Orianna", "Ziggs", "Syndra", "Zoe", "Twisted Fate"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Kalista", "Varus", "Ezreal", "Corki", "Xayah"), listOf("Lucian", "Vayne", "Tristana", "Sivir", "Caitlyn", "Samira"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Soraka", "Rakan", "Janna", "Seraphine", "Lulu"), listOf("Zyra", "Karma", "Nautilus", "Nami", "Morgana", "Lux"), emptyList())
        ),
        "kayn" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Diana", "Jarvan IV", "Lee Sin", "Wukong", "Gwen"), listOf("Vi", "Rengar", "Shyvana", "Evelynn", "Maestro Yi", "Xin Zhao"), emptyList())
        ),
        "kennen" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Riven", "Renekton", "Darius", "Yone", "Jax", "Aatrox"), listOf("Nasus", "Irelia", "Dr. Mundo", "Olaf", "Vladimir", "Kayle"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Akali", "Zed", "Yasuo", "Aurelion Sol", "Orianna"), listOf("Diana", "Ahri", "Vladimir", "Annie", "Galio", "Brand"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Braum", "Seraphine", "Blitzcrank", "Rakan", "Senna"), listOf("Janna", "Lulu", "Morgana", "Soraka", "Nami", "Alistar"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Draven", "Xayah", "Kalista", "Kai'Sa", "Jinx"), listOf("Ashe", "Ezreal", "Samira", "Sivir", "Nilah", "Tristana"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Kha'Zix", "Rengar", "Evelynn", "Maestro Yi", "Kayn"), listOf("Jarvan IV", "Gragas", "Lee Sin", "Amumu", "Viego", "Pantheon"), emptyList())
        ),
        "kha_zix" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Evelynn", "Amumu", "Maestro Yi", "Jarvan IV", "Xin Zhao", "Viego"), listOf("Rengar", "Vi", "Lee Sin", "Volibear", "Warwick", "Rammus"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Twisted Fate", "Lux", "Veigar", "Ezreal", "Aurelion Sol"), listOf("Diana", "Kayle", "Orianna", "Galio", "Lucian", "Fizz"), emptyList())
        ),
        "kindred" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Nunu y Willump", "Shyvana", "Viego", "Rengar", "Jarvan IV", "Ekko"), listOf("Lee Sin", "Gragas", "Maestro Yi", "Evelynn", "Kha'Zix", "Vi"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Kai'Sa", "Lucian", "Senna", "Zeri", "Sivir"), listOf("Vayne", "Tristana", "Nilah", "Ashe", "Samira", "Caitlyn"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Irelia", "Yone", "Malphite", "Syndra", "Katarina"), listOf("Talon", "Zoe", "Fizz", "Orianna", "Corki", "Pantheon"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nami", "Sona", "Seraphine", "Yuumi", "Lulu", "Soraka"), listOf("Alistar", "Blitzcrank", "Nautilus", "Rakan", "Janna", "Malphite"), emptyList())
        ),
        "kog_maw" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Corki", "Xayah", "Varus", "Samira", "Tristana", "Ezreal"), listOf("Vayne", "Draven", "Miss Fortune", "Twitch", "Ashe", "Jinx"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Ekko", "Vladimir", "Orianna", "Annie", "Ahri", "Galio"), listOf("Evelynn", "Jayce", "Aurelion Sol", "Veigar", "Syndra"), emptyList())
        ),
        "lee_sin" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Rengar", "Xin Zhao", "Maestro Yi", "Kha'Zix", "Evelynn"), listOf("Rammus", "Vi", "Wukong", "Fiddlesticks", "Warwick"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Tryndamere", "Rengar", "Akali", "Irelia", "Kayle", "Teemo"), listOf("Garen", "Sett", "Dr. Mundo", "Renekton", "Darius", "Nasus"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Aurelion Sol", "Akali", "Katarina", "Orianna", "Zoe", "Yone"), listOf("Pantheon", "Vex", "Jayce", "Ahri", "Malphite", "Swain"), emptyList())
        ),
        "leona" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Blitzcrank", "Sona", "Lulu", "Nami", "Nautilus", "Soraka"), listOf("Morgana", "Alistar", "Janna", "Karma", "Braum", "Thresh"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Draven", "Varus", "Twitch", "Jinx", "Ezreal"), listOf("Samira", "Lucian", "Jhin", "Zeri", "Vayne", "Caitlyn"), emptyList())
        ),
        "lillia" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Wukong", "Shyvana", "Nunu y Willump", "Warwick", "Kha'Zix"), listOf("Rengar", "Ekko", "Diana", "Maestro Yi", "Evelynn", "Jarvan IV"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Gragas", "Akali", "Malphite", "Shen", "Nasus", "Renekton"), listOf("Irelia", "Kayle", "Jayce", "Fiora", "Rengar", "Teemo"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Shen", "Akali", "Lux", "Yasuo", "Corki"), listOf("Diana", "Orianna", "Aurelion Sol", "Ziggs", "Malphite", "Kassadin"), emptyList())
        ),
        "lissandra" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Yone", "Akshan", "Twisted Fate", "Yasuo", "Katarina", "Fizz"), listOf("Vex", "Lux", "Malphite", "Zed", "Kassadin", "Varus"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Singed", "Gwen", "Dr. Mundo", "Ornn", "Riven", "Aatrox"), listOf("Olaf", "Jayce", "Vladimir", "Shen", "Mordekaiser", "Aurelion Sol"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Pyke", "Leona", "Alistar", "Sona", "Zyra", "Braum"), listOf("Karma", "Lulu", "Senna", "Nami", "Janna", "Yuumi"), emptyList())
        ),
        "lucian" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Varus", "Miss Fortune", "Ezreal", "Kalista", "Vayne"), listOf("Vayne", "Ashe", "Nilah", "Caitlyn", "Twitch", "Tristana"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Ezreal", "Diana", "Akali", "Yone", "Veigar", "Kassadin"), listOf("Brand", "Galio", "Annie", "Aurelion Sol", "Fizz", "Yasuo"), emptyList())
        ),
        "lulu" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Thresh", "Nami", "Alistar", "Morgana", "Janna", "Braum"), listOf("Sona", "Soraka", "Blitzcrank", "Rakan", "Karma", "Leona"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Akali", "Zed", "Fizz", "Kassadin", "Syndra"), listOf("Diana", "Yasuo", "Ekko", "Annie", "Vex", "Brand"), emptyList())
        ),
        "lux" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Brand", "Annie", "Orianna", "Corki", "Irelia", "Akali"), listOf("Fizz", "Kassadin", "Ahri", "Yasuo", "Aurelion Sol", "Karma"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Senna", "Nautilus", "Rakan", "Braum", "Lulu"), listOf("Sona", "Blitzcrank", "Soraka", "Janna", "Zyra", "Leona"), emptyList())
        ),
        "malphite" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Tryndamere", "Teemo", "Jax", "Yasuo", "Jayce", "Irelia"), listOf("Fiora", "Olaf", "Darius", "Garen", "Shen", "Dr. Mundo"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Tryndamere", "Lee Sin", "Kha'Zix", "Shyvana", "Rammus"), listOf("Amumu", "Volibear", "Rengar", "Graves", "Vi"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Ashe", "Vayne", "Nami", "Jinx", "Varus", "Jhin"), listOf("Sivir", "Samira", "Kai'Sa", "Ezreal", "Tristana", "Lucian"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Zyra", "Janna", "Lulu", "Braum", "Leona", "Nami"), listOf("Morgana", "Sona", "Senna", "Rakan", "Thresh", "Nautilus"), emptyList())
        ),
        "maokai" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Pyke", "Rakan", "Lux", "Sona", "Senna", "Seraphine"), listOf("Braum", "Alistar", "Janna", "Thresh", "Lulu", "Zyra"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Varus", "Xayah", "Jhin", "Caitlyn", "Samira"), listOf("Vayne", "Tristana", "Draven", "Zeri", "Jinx", "Sivir"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Jayce", "Gragas", "Akali", "Darius", "Irelia", "Yone"), listOf("Garen", "Dr. Mundo", "Nasus", "Sett", "Camille", "Fiora"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Diana", "Gragas", "Graves", "Kha'Zix", "Talon", "Xin Zhao"), listOf("Rammus", "Warwick", "Jarvan IV", "Nunu y Willump", "Rengar", "Lillia"), emptyList())
        ),
        "master_yi" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Evelynn", "Jarvan IV", "Lee Sin", "Xin Zhao", "Rengar", "Olaf"), listOf("Rammus", "Vi", "Amumu", "Kha'Zix", "Warwick", "Kayn"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Fizz", "Riven", "Jayce", "Yone", "Irelia"), listOf("Jax", "Teemo", "Pantheon", "Tryndamere", "Renekton", "Fiora"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Lux", "Twisted Fate", "Katarina", "Akali", "Ahri", "Orianna"), listOf("Fizz", "Annie", "Diana", "Pantheon", "Karma", "Kayle"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Ashe", "Kai'Sa", "Jinx", "Twitch", "Annie", "Zeri"), listOf("Xayah", "Varus", "Vayne", "Jhin", "Kalista"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Thresh", "Senna", "Janna", "Lulu", "Soraka", "Nami"), listOf("Blitzcrank", "Sona", "Seraphine", "Alistar", "Nautilus", "Karma"), emptyList())
        ),
        "mel" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Lux", "Yone", "Orianna", "Zed", "Vladimir"), listOf("Aurelion Sol", "Kassadin", "Kayle", "Fizz", "Syndra", "Ziggs"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Draven", "Varus", "Twitch", "Ezreal", "Jhin"), listOf("Corki", "Samira", "Kai'Sa", "Ziggs", "Vayne", "Kog'Maw"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Blitzcrank", "Leona", "Lux", "Zyra", "Thresh", "Seraphine"), listOf("Sona", "Janna", "Karma", "Leona", "Soraka", "Zilean"), emptyList())
        ),
        "milio" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Seraphine", "Soraka", "Rakan", "Leona", "Janna", "Nami"), listOf("Blitzcrank", "Thresh", "Pyke", "Nautilus", "Senna", "Lux"), emptyList())
        ),
        "miss_fortune" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Varus", "Kai'Sa", "Vayne", "Jinx", "Samira"), listOf("Tristana", "Draven", "Caitlyn", "Lucian", "Senna", "Twitch"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Karma", "Vladimir", "Syndra", "Swain", "Zoe", "Akali"), listOf("Ekko", "Lucian", "Talon", "Galio", "Yasuo", "Zed"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Soraka", "Yuumi", "Pyke", "Morgana", "Zyra", "Sona"), listOf("Malphite", "Braum", "Nautilus", "Rakan", "Blitzcrank", "Alistar"), emptyList())
        ),
        "mordekaiser" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Sett", "Garen", "Akali", "Shen", "Yone"), listOf("Olaf", "Fiora", "Warwick", "Riven", "Jax", "Gwen"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Diana", "Graves", "Shyvana", "Lee Sin", "Evelynn"), listOf("Maestro Yi", "Warwick", "Lillia", "Ekko", "Pantheon", "Gragas"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Yasuo", "Varus", "Zed", "Lux", "Diana"), listOf("Ahri", "Irelia", "Vex", "Kassadin", "Jayce", "Veigar"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Morgana", "Sona", "Alistar", "Soraka", "Zyra", "Braum"), listOf("Soraka", "Karma", "Pyke", "Nami", "Seraphine", "Blitzcrank"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Nilah", "Samira", "Kai'Sa", "Ezreal", "Lucian", "Ashe"), listOf("Xayah", "Kalista", "Vayne", "Zeri", "Twitch", "Sivir"), emptyList())
        ),
        "morgana" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Leona", "Braum", "Blitzcrank", "Nami", "Alistar", "Rakan"), listOf("Karma", "Janna", "Sona", "Soraka", "Yuumi", "Lulu"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Akali", "Fizz", "Veigar", "Yasuo", "Irelia"), listOf("Fizz", "Katarina", "Zed", "Seraphine", "Tristana", "Lissandra"), emptyList())
        ),
        "nami" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Janna", "Thresh", "Karma", "Braum", "Soraka", "Rakan"), listOf("Lulu", "Blitzcrank", "Morgana", "Leona", "Alistar", "Sona"), emptyList())
        ),
        "nasus" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Irelia", "Zed", "Wukong", "Kayle", "Jax", "Shen"), listOf("Darius", "Teemo", "Pantheon", "Olaf", "Garen", "Camille"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Yasuo", "Corki", "Ekko", "Zoe", "Irelia", "Kassadin"), listOf("Akali", "Zed", "Fizz", "Aurelion Sol", "Ahri", "Orianna"), emptyList())
        ),
        "nautilus" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Thresh", "Karma", "Lux", "Brand", "Maokai"), listOf("Morgana", "Janna", "Alistar", "Rakan", "Seraphine", "Lulu"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Malphite", "Teemo", "Jayce", "Sett", "Yasuo", "Jax"), listOf("Fiora", "Shen", "Darius", "Kayle", "Gragas", "Camille"), emptyList())
        ),
        "nidalee" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Pantheon", "Nunu y Willump", "Wukong", "Graves", "Gragas"), listOf("Rammus", "Warwick", "Amumu", "Maestro Yi", "Ekko", "Nocturne"), emptyList())
        ),
        "nilah" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Kalista", "Samira", "Miss Fortune", "Varus"), listOf("Caitlyn", "Xayah", "Sivir", "Draven", "Jinx", "Ashe"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Aurelion Sol", "Veigar", "Zed", "Ahri", "Kassadin"), listOf("Annie", "Ekko", "Lux", "Fizz", "Yasuo", "Katarina"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Irelia", "Riven", "Teemo", "Ornn", "Aatrox", "Jax"), listOf("Shen", "Garen", "Jayce", "Renekton", "Nasus", "Sett"), emptyList())
        ),
        "norra" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Annie", "Aurelion Sol", "Morgana", "Syndra", "Teemo"), listOf("Ekko", "Zoe", "Rengar", "Malphite", "Nocturne", "Ahri"), emptyList())
        ),
        "nunu_willump" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Wukong", "Shyvana", "Jax", "Graves", "Rammus", "Amumu"), listOf("Lee Sin", "Vi", "Kha'Zix", "Diana", "Evelynn", "Gragas"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Jax", "Teemo", "Renekton", "Singed", "Fiora", "Rammus"), listOf("Riven", "Nasus", "Tryndamere", "Maestro Yi", "Shen", "Yasuo"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Zed", "Kennen", "Veigar", "Fizz", "Talon", "Katarina"), listOf("Twisted Fate", "Galio", "Irelia", "Zoe", "Ziggs", "Brand"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Draven", "Jinx", "Corki", "Varus", "Twitch"), listOf("Nilah", "Xayah", "Samira", "Lucian", "Jhin"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Sona", "Soraka", "Nautilus", "Braum", "Leona", "Nami"), listOf("Morgana", "Lux", "Karma", "Seraphine", "Zyra", "Lulu"), emptyList())
        ),
        "olaf" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Irelia", "Nasus", "Renekton", "Malphite", "Jayce", "Yone"), listOf("Kennen", "Volibear", "Jax", "Pantheon", "Kayle", "Sett"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Nunu y Willump", "Lillia", "Evelynn", "Amumu", "Xin Zhao"), listOf("Gragas", "Jarvan IV", "Warwick", "Rengar", "Fiddlesticks", "Maestro Yi"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Akali", "Zed", "Fizz", "Riven", "Morgana"), listOf("Brand", "Zoe", "Kayle", "Lucian", "Annie", "Akshan"), emptyList())
        ),
        "orianna" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Lux", "Kayle", "Fizz", "Akali", "Yasuo"), listOf("Diana", "Gragas", "Ziggs", "Ahri", "Zed", "Syndra"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Varus", "Aatrox", "Urgot", "Yone", "Kennen", "Nasus"), listOf("Ornn", "Wukong", "Jax", "Riven", "Sett", "Garen"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Ashe", "Jinx", "Twitch", "Varus"), listOf("Miss Fortune", "Tristana", "Lucian", "Jhin", "Draven", "Caitlyn"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Karma", "Lux", "Nautilus", "Seraphine", "Lulu"), listOf("Sona", "Rakan", "Alistar", "Thresh", "Zyra", "Pyke"), emptyList())
        ),
        "ornn" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Renekton", "Jayce", "Sett", "Jax", "Yone"), listOf("Fiora", "Shen", "Olaf", "Camille", "Riven", "Vayne"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Katarina", "Malphite", "Irelia", "Twisted Fate", "Akali"), listOf("Vex", "Lux", "Galio", "Kayle", "Swain", "Fizz"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Rengar", "Wukong", "Hecarim", "Talon", "Warwick"), listOf("Vi", "Volibear", "Shyvana", "Graves", "Maestro Yi"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Seraphine", "Blitzcrank", "Soraka", "Braum", "Nautilus"), listOf("Sona", "Maokai", "Nami", "Pyke", "Brand", "Rakan"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Caitlyn", "Ezreal", "Jhin", "Sivir", "Jinx", "Lucian"), listOf("Ashe", "Kalista", "Kai'Sa", "Tristana", "Vayne"), emptyList())
        ),
        "pantheon" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Riven", "Teemo", "Jax", "Yasuo", "Yone", "Gwen"), listOf("Fiora", "Malphite", "Camille", "Olaf", "Shen", "Jayce"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Nunu y Willump", "Maestro Yi", "Jarvan IV", "Jax", "Xin Zhao", "Lee Sin"), listOf("Rammus", "Vi", "Diana", "Rengar", "Shyvana"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Galio", "Yasuo", "Yone", "Zed", "Irelia"), listOf("Orianna", "Ahri", "Zoe", "Syndra", "Jayce", "Swain"), emptyList())
        ),
        "poppy" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Yone", "Irelia", "Jayce", "Akali", "Teemo", "Fiora"), listOf("Shen", "Darius", "Garen", "Camille", "Malphite", "Olaf"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rammus", "Diana", "Warwick", "Wukong", "Gwen", "Maestro Yi"), listOf("Xin Zhao", "Amumu", "Vi", "Graves", "Lillia", "Gragas"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Pantheon", "Ashe", "Alistar", "Leona", "Lux", "Seraphine"), listOf("Sona", "Janna", "Rakan", "Senna", "Yuumi", "Soraka"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Jayce", "Katarina", "Orianna", "Kennen", "Fizz"), listOf("Kassadin", "Zoe", "Lissandra", "Corki", "Diana", "Viktor"), emptyList())
        ),
        "pyke" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Seraphine", "Soraka", "Sona", "Karma", "Senna", "Lulu"), listOf("Rakan", "Blitzcrank", "Leona", "Maokai", "Braum", "Morgana"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Kai'Sa", "Twisted Fate", "Corki", "Irelia", "Lux"), listOf("Fizz", "Ahri", "Akali", "Katarina", "Vladimir", "Malphite"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Teemo", "Shen", "Renekton", "Yasuo", "Jax", "Jayce"), listOf("Garen", "Pantheon", "Riven", "Dr. Mundo", "Tryndamere", "Nasus"), emptyList())
        ),
        "rakan" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nami", "Blitzcrank", "Lucian", "Soraka", "Miss Fortune", "Thresh"), listOf("Lulu", "Leona", "Janna", "Alistar", "Karma", "Veigar"), emptyList())
        ),
        "rammus" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Tryndamere", "Maestro Yi", "Xin Zhao", "Graves", "Lee Sin", "Jarvan IV"), listOf("Fiddlesticks", "Evelynn", "Lillia", "Amumu", "Nunu y Willump"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Jax", "Pantheon", "Rengar", "Jayce", "Yasuo", "Tryndamere"), listOf("Darius", "Shen", "Vi", "Riven", "Renekton", "Volibear"), emptyList())
        ),
        "rell" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Rakan", "Pyke", "Yuumi", "Nautilus", "Leona", "Lulu"), listOf("Janna", "Sona", "Nami", "Morgana", "Seraphine", "Zyra"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Gwen", "Malphite", "Yasuo", "Jax", "Viego", "Riven"), listOf("Shen", "Xin Zhao", "Olaf", "Sett", "Fiora", "Sion"), emptyList())
        ),
        "renekton" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Riven", "Irelia", "Fiora", "Jax", "Akali", "Tryndamere"), listOf("Vayne", "Garen", "Olaf", "Kayle", "Teemo", "Pantheon"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Vladimir", "Annie", "Gragas", "Galio", "Karma"), listOf("Jayce", "Lux", "Ekko", "Tristana", "Diana", "Pantheon"), emptyList())
        ),
        "rengar" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kha'Zix", "Kennen", "Graves", "Lillia", "Kindred", "Lee Sin"), listOf("Rammus", "Amumu", "Jarvan IV", "Warwick", "Maestro Yi"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Vladimir", "Jayce", "Vayne", "Yone", "Yasuo", "Gwen"), listOf("Tryndamere", "Pantheon", "Darius", "Renekton", "Urgot", "Fiora"), emptyList())
        ),
        "riven" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Vi", "Dr. Mundo", "Nasus", "Lee Sin", "Rengar", "Irelia"), listOf("Fiora", "Garen", "Renekton", "Kennen", "Pantheon", "Darius"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Fizz", "Zed", "Akali", "Kassadin", "Diana"), listOf("Lux", "Ahri", "Galio", "Ekko", "Jayce", "Corki"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Hecarim", "Nunu y Willump", "Kayn", "Viego", "Diana", "Xin Zhao"), listOf("Amumu", "Vi", "Gragas", "Fiddlesticks", "Rammus"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Ashe", "Tristana", "Xayah", "Kalista", "Twitch", "Jinx"), listOf("Zeri", "Draven", "Vayne", "Caitlyn", "Varus"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Sona", "Karma", "Nami", "Seraphine", "Yuumi", "Blitzcrank"), listOf("Alistar", "Braum", "Nautilus", "Lulu", "Janna", "Leona"), emptyList())
        ),
        "rumble" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Yone", "Sett", "Teemo", "Irelia", "Wukong", "Malphite"), listOf("Garen", "Jayce", "Sion", "Shen", "Camille", "Tryndamere"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Orianna", "Zoe", "Yasuo", "Akali", "Talon"), listOf("Syndra", "Kennen", "Annie", "Lux", "Ahri", "Galio"), emptyList())
        ),
        "ryze" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Fizz", "Kassadin", "Vex", "Katarina"), listOf("Swain", "Annie", "Jayce", "Yasuo", "Irelia", "Orianna"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Viktor", "Singed", "Heimerdinger", "Wukong", "Rumble", "Kennen"), listOf("Jayce", "Olaf", "Vayne", "Urgot", "Pantheon", "Irelia"), emptyList())
        ),
        "samira" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Akshan", "Kai'Sa", "Varus", "Ezreal", "Kalista"), listOf("Draven", "Miss Fortune", "Senna", "Nilah", "Jinx", "Xayah"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Corki", "Brand", "Zoe", "Akshan", "Kassadin", "Katarina"), listOf("Lucian", "Vex", "Irelia", "Syndra", "Ekko", "Yasuo"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Jayce", "Vayne", "Volibear", "Shen", "Ornn", "Kayle"), listOf("Kennen", "Yasuo", "Urgot", "Sett", "Pantheon", "Dr. Mundo"), emptyList())
        ),
        "senna" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Seraphine", "Sona", "Janna", "Soraka", "Yuumi", "Nami"), listOf("Blitzcrank", "Rakan", "Leona", "Thresh", "Braum", "Nautilus"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Draven", "Kai'Sa", "Sivir", "Ashe", "Zeri"), listOf("Caitlyn", "Jhin", "Ezreal", "Kalista", "Lucian", "Vayne"), emptyList())
        ),
        "seraphine" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Karma", "Lux", "Annie", "Corki", "Twisted Fate"), listOf("Fizz", "Ziggs", "Irelia", "Katarina", "Kayle", "Zed"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Morgana", "Thresh", "Alistar", "Rakan", "Braum", "Leona"), listOf("Sona", "Soraka", "Janna", "Senna", "Pyke", "Milio"), emptyList())
        ),
        "sett" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Yasuo", "Fiora", "Rengar", "Irelia", "Jayce", "Teemo"), listOf("Malphite", "Renekton", "Garen", "Pantheon", "Singed", "Volibear"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rengar", "Shyvana", "Ekko", "Fiddlesticks", "Diana", "Wukong"), listOf("Rammus", "Kayn", "Maestro Yi", "Kha'Zix", "Lillia"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Diana", "Katarina", "Ekko", "Yone", "Akali", "Yasuo"), listOf("Swain", "Zoe", "Ahri", "Ziggs", "Veigar", "Vladimir"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Zeri", "Draven", "Lucian", "Miss Fortune", "Varus", "Caitlyn"), listOf("Sivir", "Twitch", "Jhin", "Vayne", "Tristana", "Kai'Sa"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nami", "Blitzcrank", "Yuumi", "Nautilus", "Sona", "Katarina"), listOf("Karma", "Seraphine", "Janna", "Sona", "Leona", "Senna"), emptyList())
        ),
        "shen" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Pantheon", "Malphite", "Fiora", "Renekton", "Riven", "Jarvan IV"), listOf("Darius", "Teemo", "Sett", "Kayle", "Olaf", "Gwen"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Blitzcrank", "Seraphine", "Nautilus", "Nami", "Rakan", "Soraka"), listOf("Leona", "Braum", "Alistar", "Morgana", "Lux", "Janna"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Zoe", "Vladimir", "Orianna", "Vex", "Fizz", "Syndra"), listOf("Brand", "Jayce", "Veigar", "Galio", "Lux", "Ryze"), emptyList())
        ),
        "shyvana" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Amumu", "Rammus", "Jarvan IV", "Rengar", "Maestro Yi", "Evelynn"), listOf("Vi", "Lee Sin", "Kha'Zix", "Gragas", "Volibear", "Gwen"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Renekton", "Dr. Mundo", "Riven", "Wukong", "Nasus", "Jax"), listOf("Olaf", "Darius", "Pantheon", "Teemo", "Tryndamere", "Shen"), emptyList())
        ),
        "singed" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Jax", "Lee Sin", "Tryndamere", "Wukong", "Irelia", "Sion"), listOf("Kennen", "Riven", "Fiora", "Urgot", "Warwick", "Darius"), emptyList())
        ),
        "sion" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Tryndamere", "Teemo", "Irelia", "Malphite", "Kennen", "Vladimir"), listOf("Pantheon", "Garen", "Nasus", "Darius", "Riven", "Jax"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Maokai", "Lux", "Leona", "Senna", "Karma", "Nami"), listOf("Morgana", "Janna", "Braum", "Thresh", "Alistar", "Lulu"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Akali", "Katarina", "Irelia", "Talon", "Zed"), listOf("Morgana", "Ahri", "Swain", "Aatrox", "Brand", "Aurelion Sol"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kindred", "Amumu", "Warwick", "Lee Sin", "Viego"), listOf("Ekko", "Hecarim", "Gragas", "Lillia", "Jarvan IV", "Rammus"), emptyList())
        ),
        "sivir" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Draven", "Tristana", "Lucian", "Xayah", "Varus"), listOf("Jhin", "Jinx", "Kai'Sa", "Miss Fortune", "Vayne", "Ashe"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Galio", "Vladimir", "Swain", "Zoe", "Ziggs", "Fizz"), listOf("Annie", "Akali", "Diana", "Lux", "Twisted Fate", "Katarina"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Shen", "Yone", "Teemo", "Aatrox", "Jayce"), listOf("Yasuo", "Kennen", "Pantheon", "Darius", "Kayle", "Jax"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Nami", "Nautilus", "Blitzcrank", "Pyke", "Brand", "Rakan"), listOf("Senna", "Zyra", "Leona", "Karma", "Thresh", "Lux"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Fiddlesticks", "Gragas"), listOf("Viego", "Kayn", "Kha'Zix", "Graves", "Jarvan IV", "Gragas"), emptyList())
        ),
        "skarner" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Nidalee", "Warwick", "Volibear", "Nocturne", "Jax", "Kindred"), listOf("Ambessa", "Vi", "Jarvan IV", "Evelynn", "Viego"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Olaf", "Teemo", "Tryndamere", "Camille", "Dr. Mundo", "K'Sante"), listOf("Maokai", "Riven", "Ryze", "Vayne", "Poppy", "Vladimir"), emptyList())
        ),
        "smolder" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Corki", "Kalista", "Varus", "Ezreal", "Xayah", "Zeri"), listOf("Kog'Maw", "Yasuo", "Ziggs", "Caitlyn", "Tristana", "Swain"), emptyList())
        ),
        "sona" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Morgana", "Lulu", "Nami", "Janna", "Yuumi", "Soraka"), listOf("Blitzcrank", "Leona", "Thresh", "Nautilus", "Zyra", "Alistar"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Jinx", "Kalista", "Draven", "Miss Fortune", "Kai'Sa"), listOf("Miss Fortune", "Caitlyn", "Ezreal", "Jhin", "Varus", "Ashe"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Lux", "Akali", "Morgana", "Zed", "Fizz"), listOf("Fizz", "Kayle", "Yasuo", "Pantheon", "Annie", "Ahri"), emptyList())
        ),
        "soraka" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Braum", "Lulu", "Nunu y Willump", "Rakan", "Milio", "Brand"), listOf("Blitzcrank", "Thresh", "Leona", "Pyke", "Nautilus", "Alistar"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Kalista", "Jinx", "Zeri", "Kai'Sa", "Ezreal", "Nilah"), listOf("Lucian", "Caitlyn", "Samira", "Jhin", "Ashe"), emptyList())
        ),
        "swain" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Vex", "Talon", "Kayle", "Kassadin", "Katarina"), listOf("Ahri", "Jayce", "Akshan", "Fizz", "Yone", "Galio"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Teemo", "Irelia", "Malphite", "Ornn", "Volibear"), listOf("Vladimir", "Olaf", "Singed", "Sett", "Wukong", "Dr. Mundo"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Rakan", "Pantheon", "Seraphine", "Alistar", "Senna", "Leona"), listOf("Brand", "Lux", "Lulu", "Morgana", "Janna", "Sona"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Lucian", "Jhin", "Ezreal", "Kai'Sa", "Sivir"), listOf("Caitlyn", "Xayah", "Jinx", "Varus", "Twitch", "Kalista"), emptyList())
        ),
        "syndra" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Zoe", "Orianna", "Akshan", "Yone", "Jayce", "Brand"), listOf("Fizz", "Kassadin", "Lux", "Yasuo", "Kayle", "Talon"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Gragas", "Sett", "Yone", "Jayce", "Pantheon", "Singed"), listOf("Shen", "Fiora", "Teemo", "Tryndamere", "Kayle", "Yasuo"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Lucian", "Kai'Sa", "Miss Fortune", "Xayah", "Varus"), listOf("Ashe", "Jhin", "Varus", "Caitlyn", "Tristana", "Ezreal"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Senna", "Yuumi", "Brand", "Rakan", "Sona"), listOf("Braum", "Nautilus", "Blitzcrank", "Lulu", "Janna", "Seraphine"), emptyList())
        ),
        "taliyah" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Malphite", "Pantheon", "Amumu", "Jax", "Volibear"), listOf("Evelynn", "Rammus", "Maestro Yi", "Fiddlesticks", "Kindred", "Dr. Mundo"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Smolder", "Vex", "Ryze", "Orianna", "Aurora"), listOf("Katarina", "Kassadin", "Zed", "Fizz", "Annie", "Veigar"), emptyList())
        ),
        "talon" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Lux", "Ziggs", "Annie", "Twisted Fate", "Ahri", "Zoe"), listOf("Ekko", "Vex", "Veigar", "Fizz", "Kassadin", "Katarina"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Maestro Yi", "Evelynn", "Jax", "Kayn", "Nunu y Willump"), listOf("Rammus", "Xin Zhao", "Amumu", "Diana", "Fiddlesticks", "Jarvan IV"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Shen", "Teemo", "Kayle", "Malphite", "Akali", "Olaf"), listOf("Tryndamere", "Yasuo", "Volibear", "Fiora", "Gwen", "Gragas"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Samira", "Ashe", "Caitlyn", "Ezreal"), listOf("Draven", "Kai'Sa", "Twitch", "Jhin", "Jinx", "Zeri"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Sona", "Blitzcrank", "Seraphine"), listOf("Nami", "Janna", "Morgana", "Leona", "Braum", "Alistar"), emptyList())
        ),
        "teemo" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Dr. Mundo", "Garen", "Darius", "Kayle", "Jax", "Tryndamere"), listOf("Pantheon", "Jayce", "Ornn", "Malphite", "Riven", "Irelia"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Gragas", "Amumu", "Kayn", "Wukong", "Rammus", "Shyvana"), listOf("Xin Zhao", "Kha'Zix", "Warwick", "Fiddlesticks", "Evelynn"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Diana", "Ahri", "Akali", "Zed", "Katarina", "Yasuo"), listOf("Fizz", "Lux", "Karma", "Galio", "Zoe", "Vex"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Varus", "Twitch", "Zeri", "Vayne", "Ashe", "Kai'Sa"), listOf("Caitlyn", "Jhin", "Lucian", "Jinx", "Tristana", "Miss Fortune"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Blitzcrank", "Janna", "Nami", "Lulu", "Rakan"), listOf("Senna", "Soraka", "Leona", "Thresh", "Sona", "Seraphine"), emptyList())
        ),
        "thresh" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Blitzcrank", "Leona", "Sona", "Soraka", "Nautilus", "Rakan"), listOf("Lulu", "Morgana", "Janna", "Nami", "Zyra", "Seraphine"), emptyList())
        ),
        "tristana" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Varus", "Ezreal", "Graves", "Xayah", "Kai'Sa"), listOf("Draven", "Sivir", "Lucian", "Caitlyn", "Nilah", "Vayne"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Karma", "Irelia", "Yone", "Katarina", "Kassadin", "Akali"), listOf("Yasuo", "Annie", "Malphite", "Vex", "Zoe", "Diana"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Soraka", "Sona", "Morgana", "Senna", "Janna", "Brand"), listOf("Alistar", "Lux", "Leona", "Thresh", "Braum", "Seraphine"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Xin Zhao", "Jarvan IV", "Warwick", "Maestro Yi", "Volibear"), listOf("Vi", "Kayn", "Kha'Zix", "Rengar", "Gragas", "Graves"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Fiora", "Ornn", "Jayce", "Olaf", "Tryndamere", "Irelia"), listOf("Yasuo", "Dr. Mundo", "Gwen", "Darius", "Kennen", "Camille"), emptyList())
        ),
        "tryndamere" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Sion", "Irelia", "Olaf", "Jayce", "Dr. Mundo", "Ornn"), listOf("Malphite", "Teemo", "Darius", "Renekton", "Camille", "Sett"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Katarina", "Irelia", "Yone", "Zed", "Syndra", "Yasuo"), listOf("Fizz", "Kayle", "Ekko", "Zoe", "Gragas", "Lux"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Rengar", "Shyvana", "Jax", "Kha'Zix", "Hecarim", "Wukong"), listOf("Rammus", "Diana", "Evelynn", "Kindred", "Warwick", "Pantheon"), emptyList())
        ),
        "twisted_fate" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Evelynn", "Ziggs", "Annie", "Karma", "Zoe", "Orianna"), listOf("Fizz", "Diana", "Ahri", "Yasuo", "Akali", "Veigar"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Lucian", "Ezreal", "Tristana", "Draven", "Twitch", "Ashe"), listOf("Nilah", "Tristana", "Miss Fortune", "Xayah", "Jinx", "Kai'Sa"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Dr. Mundo", "Jax", "Riven", "Gragas", "Shyvana"), listOf("Brand", "Kha'Zix", "Rengar", "Pantheon", "Warwick", "Jarvan IV"), emptyList())
        ),
        "twitch" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Vayne", "Jinx", "Varus", "Ashe", "Ezreal", "Miss Fortune"), listOf("Jhin", "Tristana", "Kai'Sa", "Draven", "Xayah", "Samira"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Nunu y Willump", "Graves", "Amumu", "Brand", "Fiddlesticks"), listOf("Vi", "Kha'Zix", "Rengar", "Evelynn", "Lee Sin", "Kayn"), emptyList())
        ),
        "urgot" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Riven", "Jayce", "Tryndamere", "Gragas", "Akali", "Yone"), listOf("Dr. Mundo", "Kayle", "Teemo", "Olaf", "Garen", "Ornn"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Ziggs", "Katarina", "Viktor", "Galio", "Akali", "Zed"), listOf("Lucian", "Ekko", "Lux", "Syndra", "Zoe", "Kennen"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Xayah", "Ashe", "Varus", "Zeri", "Kai'Sa"), listOf("Draven", "Miss Fortune", "Vayne", "Twisted Fate", "Jinx", "Kalista"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Blitzcrank", "Leona", "Senna", "Braum", "Nautilus"), listOf("Shen", "Sona", "Nami", "Katarina", "Janna", "Rakan"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Fiddlesticks", "Warwick", "Wukong", "Kha'Zix", "Talon", "Rengar"), listOf("Hecarim", "Amumu", "Kindred", "Nunu y Willump", "Vi", "Graves"), emptyList())
        ),
        "varus" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Kalista", "Zeri", "Nilah", "Xayah", "Ashe"), listOf("Twitch", "Jinx", "Miss Fortune", "Sivir", "Samira", "Jhin"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Sona", "Senna", "Nami", "Lulu", "Rakan", "Soraka"), listOf("Alistar", "Braum", "Thresh", "Brand", "Soraka", "Zyra"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Diana", "Corki", "Lux", "Kassadin", "Kayle", "Vladimir"), listOf("Annie", "Zoe", "Jayce", "Yasuo", "Katarina", "Yone"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Evelynn", "Dr. Mundo", "Wukong", "Ekko", "Fiddlesticks", "Amumu"), listOf("Maestro Yi", "Lee Sin", "Kayn", "Xin Zhao", "Jarvan IV", "Rammus"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Dr. Mundo", "Volibear", "Gwen", "Teemo", "Olaf", "Darius"), listOf("Yasuo", "Tryndamere", "Camille", "Jax", "Malphite"), emptyList())
        ),
        "vayne" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Renekton", "Darius", "Nasus", "Garen", "Shen", "Riven"), listOf("Pantheon", "Teemo", "Malphite", "Akshan", "Jayce", "Vladimir"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Ezreal", "Jhin", "Miss Fortune", "Xayah", "Kalista", "Kai'Sa"), listOf("Caitlyn", "Draven", "Varus", "Ashe", "Tristana", "Graves"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Alistar", "Braum", "Shen", "Leona", "Nautilus", "Blitzcrank"), listOf("Soraka", "Sona", "Senna", "Janna", "Morgana", "Thresh"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Xin Zhao", "Maokai", "Nunu y Willump", "Jarvan IV", "Volibear", "Vi"), listOf("Rammus", "Maestro Yi", "Kha'Zix", "Rengar", "Kayn", "Evelynn"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Kassadin", "Irelia", "Nasus", "Sett", "Galio", "Gragas"), listOf("Pantheon", "Malphite", "Veigar", "Tristana", "Zoe", "Ahri"), emptyList())
        ),
        "veigar" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Ziggs", "Ryze", "Vladimir", "Yone", "Akali"), listOf("Zed", "Katarina", "Zoe", "Ekko", "Ahri", "Talon"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Tristana", "Draven", "Ezreal", "Corki", "Rakan", "Orianna"), listOf("Braum", "Seraphine", "Xayah", "Miss Fortune", "Lucian"), emptyList())
        ),
        "vel_koz" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Twisted Fate", "Veigar", "Corki", "Lux", "Ryze", "Viktor"), listOf("Yone", "Irelia", "Zed", "Kassadin", "Fizz", "Ekko"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Brand", "Alistar", "Yuumi", "Soraka", "Rakan", "Braum"), listOf("Seraphine", "Sona", "Morgana", "Karma", "Lulu", "Zyra"), emptyList())
        ),
        "vex" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Annie", "Varus", "Fizz", "Orianna", "Lux", "Yone"), listOf("Pantheon", "Galio", "Veigar", "Kassadin", "Katarina", "Kayle"), emptyList())
        ),
        "vi" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Jarvan IV", "Shyvana", "Amumu", "Maestro Yi", "Wukong", "Evelynn"), listOf("Lee Sin", "Xin Zhao", "Kha'Zix", "Warwick", "Viego", "Diana"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Malphite", "Nasus", "Jayce", "Jarvan IV", "Renekton", "Vladimir"), listOf("Riven", "Pantheon", "Teemo", "Darius", "Jax", "Olaf"), emptyList())
        ),
        "viego" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kayn", "Graves", "Jarvan IV", "Diana", "Shyvana", "Xin Zhao"), listOf("Rammus", "Evelynn", "Warwick", "Vi", "Amumu"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Gragas", "Yasuo", "Teemo", "Sett", "Sion", "Rengar"), listOf("Olaf", "Nasus", "Tryndamere", "Shen", "Riven", "Camille"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Malphite", "Fizz", "Vex", "Veigar", "Kassadin", "Ekko"), listOf("Syndra", "Aurelion Sol", "Veigar", "Twisted Fate", "Lux", "Vladimir"), emptyList())
        ),
        "viktor" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Varus", "Vex", "Yasuo", "Katarina", "Twisted Fate", "Veigar"), listOf("Ekko", "Fizz", "Zed", "Kassadin", "Ambessa", "Akali"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Yasuo", "Teemo", "Kayle", "Yone", "Dr. Mundo", "Vayne"), listOf("Renekton", "Shen", "Riven", "Garen", "Vayne", "Jax"), emptyList())
        ),
        "vladimir" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Teemo", "Jayce", "Akali", "Jax", "Darius", "Kayle"), listOf("Kennen", "Camille", "Irelia", "Riven", "Darius", "Yone"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Varus", "Yasuo", "Vex", "Irelia", "Katarina", "Lux"), listOf("Fizz", "Kassadin", "Zed", "Ahri", "Orianna", "Ziggs"), emptyList())
        ),
        "volibear" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Warwick", "Nunu y Willump", "Graves", "Lee Sin", "Evelynn", "Diana"), listOf("Maestro Yi", "Amumu", "Rengar", "Lillia", "Fiddlesticks", "Graves"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Sett", "Yasuo", "Renekton", "Riven", "Irelia", "Tryndamere"), listOf("Jayce", "Vayne", "Teemo", "Darius", "Kayle", "Jax"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Akali", "Katarina", "Zed", "Yone", "Yasuo", "Talon"), listOf("Syndra", "Corki", "Swain", "Ahri", "Zoe", "Lux"), emptyList())
        ),
        "warwick" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Maestro Yi", "Lee Sin", "Shyvana", "Kha'Zix", "Gragas", "Xin Zhao"), listOf("Evelynn", "Nunu y Willump", "Rengar", "Maokai", "Rammus", "Vi"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Nasus", "Akali", "Gwen", "Fiora", "Sett", "Wukong"), listOf("Kayle", "Olaf", "Urgot", "Singed", "Darius", "Nasus"), emptyList())
        ),
        "wukong" to mapOf(
            LaneRole.TOP to MatchupRoleResult(listOf("Zed", "Jayce", "Teemo", "Pantheon", "Irelia", "Jax"), listOf("Lee Sin", "Darius", "Garen", "Nasus", "Olaf", "Pantheon"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Kha'Zix", "Fiddlesticks", "Maokai", "Pantheon", "Nunu y Willump", "Warwick"), listOf("Lee Sin", "Amumu", "Jarvan IV", "Evelynn", "Vi", "Shyvana"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Yasuo", "Veigar", "Akali", "Annie", "Vex"), listOf("Morgana", "Fizz", "Diana", "Jayce", "Brand", "Vladimir"), emptyList())
        ),
        "xayah" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Ashe", "Vayne", "Zeri", "Kai'Sa", "Samira"), listOf("Miss Fortune", "Tristana", "Varus", "Caitlyn", "Kalista", "Draven"), emptyList())
        ),
        "xin_zhao" to mapOf(
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Vi", "Jarvan IV", "Evelynn", "Nunu y Willump", "Fiddlesticks", "Diana"), listOf("Malphite", "Rammus", "Lee Sin", "Evelynn", "Ekko", "Maestro Yi"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Jayce", "Kayle", "Darius", "Riven", "Vi", "Zed"), listOf("Jax", "Pantheon", "Teemo", "Fiora", "Malphite", "Shen"), emptyList())
        ),
        "yasuo" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Ahri", "Twisted Fate", "Veigar", "Corki", "Katarina", "Akali"), listOf("Fizz", "Renekton", "Annie", "Vex", "Vladimir", "Swain"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Akali", "Vayne", "Teemo", "Jayce", "Irelia", "Kennen"), listOf("Renekton", "Nasus", "Tryndamere", "Garen", "Camille", "Darius"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Tristana", "Miss Fortune", "Caitlyn", "Kai'Sa", "Jhin"), listOf("Nilah", "Kalista", "Samira", "Jinx", "Zeri", "Lucian"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Blitzcrank", "Morgana", "Alistar", "Lulu", "Braum", "Seraphine"), listOf("Sona", "Leona", "Nautilus", "Nami", "Zyra", "Janna"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Ekko", "Shyvana", "Vi", "Gragas", "Kayn", "Lee Sin"), listOf("Rammus", "Amumu", "Kindred", "Fiddlesticks", "Pantheon", "Nunu y Willump"), emptyList())
        ),
        "yone" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Corki", "Twisted Fate", "Irelia", "Yasuo", "Diana"), listOf("Pantheon", "Fizz", "Viego", "Ahri", "Annie", "Zed"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Jayce", "Sion", "Gwen", "Dr. Mundo", "Singed", "Tryndamere"), listOf("Pantheon", "Gragas", "Riven", "Fiora", "Garen", "Sett"), emptyList())
        ),
        "yuumi" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Seraphine", "Brand", "Braum", "Lux", "Thresh", "Senna"), listOf("Leona", "Alistar", "Rakan", "Blitzcrank", "Soraka", "Sona"), emptyList())
        ),
        "zed" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Veigar", "Ziggs", "Orianna", "Corki", "Kassadin", "Aurelion Sol"), listOf("Fizz", "Ekko", "Vladimir", "Ahri", "Kayle", "Galio"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Darius", "Teemo", "Riven", "Jayce", "Yasuo", "Gwen"), listOf("Kayle", "Jax", "Irelia", "Wukong", "Nasus", "Tryndamere"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Caitlyn", "Miss Fortune", "Varus", "Draven", "Zeri", "Jinx"), listOf("Vayne", "Lucian", "Ashe", "Samira", "Kalista", "Tristana"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Lulu", "Zyra", "Rakan", "Blitzcrank", "Lux", "Senna"), listOf("Leona", "Alistar", "Maokai", "Janna", "Soraka", "Sona"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Graves", "Jarvan IV", "Xin Zhao", "Gragas", "Lee Sin", "Talon"), listOf("Rammus", "Amumu", "Kindred", "Rengar", "Kayn", "Maestro Yi"), emptyList())
        ),
        "zeri" to mapOf(
            LaneRole.ADC to MatchupRoleResult(listOf("Miss Fortune", "Ezreal", "Xayah", "Kai'Sa", "Jinx", "Ashe"), listOf("Draven", "Tristana", "Jhin", "Caitlyn", "Vayne", "Samira"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Malphite", "Irelia", "Zoe", "Vladimir", "Pantheon", "Karma"), listOf("Veigar", "Ziggs", "Brand", "Vex", "Talon", "Ahri"), emptyList())
        ),
        "ziggs" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Orianna", "Corki", "Veigar", "Lux", "Ezreal", "Diana"), listOf("Kayle", "Talon", "Brand", "Ahri", "Jayce"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Miss Fortune", "Xayah", "Tristana", "Varus"), listOf("Caitlyn", "Samira", "Zeri", "Kai'Sa", "Jhin", "Varus"), emptyList())
        ),
        "zilean" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Seraphine", "Brand", "Blitzcrank", "Nautilus", "Sona", "Alistar"), listOf("Yuumi", "Maokai", "Lulu", "Leona", "Karma", "Pyke"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Fizz", "Orianna", "Lux", "Twisted Fate", "Talon", "Ryze"), listOf("Vex", "Ekko", "Kassadin", "Kayle", "Akshan", "Tristana"), emptyList())
        ),
        "zoe" to mapOf(
            LaneRole.MID to MatchupRoleResult(listOf("Akshan", "Irelia", "Orianna", "Fizz", "Lux", "Yasuo"), listOf("Veigar", "Ahri", "Kassadin", "Twisted Fate", "Akali", "Zed"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Gwen", "Darius", "Kennen", "Renekton", "Teemo", "Vayne"), listOf("Urgot", "Sion", "Tryndamere", "Malphite", "Pantheon", "Gragas"), emptyList()),
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Veigar", "Alistar", "Lux", "Morgana", "Senna", "Yuumi"), listOf("Soraka", "Blitzcrank", "Pyke", "Nami", "Yuumi", "Sona"), emptyList())
        ),
        "zyra" to mapOf(
            LaneRole.SUPPORT to MatchupRoleResult(listOf("Yuumi", "Seraphine", "Senna", "Karma", "Lulu", "Braum"), listOf("Janna", "Sona", "Blitzcrank", "Morgana", "Alistar", "Soraka"), emptyList()),
            LaneRole.MID to MatchupRoleResult(listOf("Corki", "Veigar", "Zoe", "Aurelion Sol", "Orianna", "Diana"), listOf("Ziggs", "Heimerdinger", "Katarina", "Zed", "Fizz", "Brand"), emptyList()),
            LaneRole.ADC to MatchupRoleResult(listOf("Jinx", "Ezreal", "Zeri", "Kai'Sa", "Xayah", "Ashe"), listOf("Miss Fortune", "Caitlyn", "Jhin", "Tristana", "Twitch", "Vayne"), emptyList()),
            LaneRole.JUNGLE to MatchupRoleResult(listOf("Gragas", "Amumu", "Fiddlesticks", "Lillia", "Rammus"), listOf("Kha'Zix", "Rengar", "Ekko", "Wukong", "Talon", "Xin Zhao"), emptyList()),
            LaneRole.TOP to MatchupRoleResult(listOf("Darius", "Garen", "Aatrox", "Kayle", "Volibear", "Gwen"), listOf("Rengar", "Wukong", "Irelia", "Nasus", "Pantheon", "Jayce"), emptyList())
        )
    )
}

