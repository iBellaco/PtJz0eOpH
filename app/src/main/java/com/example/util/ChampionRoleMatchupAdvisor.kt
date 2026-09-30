package com.example.util

import com.example.model.Champion
import com.example.model.LaneRole

data class MatchupRoleResult(
    val advantages: List<String>,
    val counters: List<String>,
    val synergies: List<String>
)

object ChampionRoleMatchupAdvisor {

    private data class MatchupKey(val champId: String, val role: LaneRole)

    private val matchupDatabase: Map<MatchupKey, MatchupRoleResult> = mapOf(
        MatchupKey("aatrox", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana", "Dr. Mundo"),
            synergies = listOf("Ahri", "Yasuo", "Galio", "Lee Sin", "Jarvan IV", "Orianna", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("aatrox", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Dr. Mundo", "Shen", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Irelia", "Camille", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora", "Blitzcrank"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("ahri", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Ziggs", "Aurelion Sol", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Yasuo", "Zed", "Kassadin", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Lee Sin", "Vi", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("akali", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Galio", "Pantheon", "Vex", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Diana", "Nautilus", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("akali", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sion", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Renekton", "Sett", "Shen", "Aatrox", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora", "Blitzcrank"),
            synergies = listOf("Lee Sin", "Vi", "Amumu", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("akshan", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Lucian", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa", "Kalista"),
            synergies = listOf("Nautilus", "Leona", "Thresh", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Amumu")
        ),
        MatchupKey("akshan", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Aurelion Sol", "Kassadin", "Veigar", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Yasuo", "Akali", "Ahri", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand", "Caitlyn"),
            synergies = listOf("Nautilus", "Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("akshan", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sett", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Irelia", "Camille", "Jax", "Aatrox", "Akali", "Alistar", "Ambessa", "Aurelion Sol", "Aurora", "Blitzcrank"),
            synergies = listOf("Amumu", "Maokai", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("alistar", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Morgana", "Janna", "Braum", "Ahri", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank", "Brand"),
            synergies = listOf("Samira", "Kai'Sa", "Yasuo", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("ambessa", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Amumu", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Kha'Zix", "Warwick", "Aatrox", "Amumu", "Brand", "Camille", "Darius", "Diana", "Dr. Mundo"),
            synergies = listOf("Galio", "Ahri", "Nautilus", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("ambessa", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Aatrox", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Jax", "Renekton", "Aatrox", "Akali", "Akshan", "Alistar", "Aurelion Sol", "Aurora", "Blitzcrank"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("amumu", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Rammus", "Xin Zhao", "Zyra", "Zed", "Wukong", "Warwick", "Volibear", "Viego", "Vi"),
            counters = listOf("Olaf", "Lee Sin", "Kha'Zix", "Aatrox", "Ambessa", "Brand", "Camille", "Darius", "Diana", "Dr. Mundo"),
            synergies = listOf("Miss Fortune", "Samira", "Katarina", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("amumu", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Yuumi", "Soraka", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Morgana", "Janna", "Braum", "Ahri", "Alistar", "Annie", "Ashe", "Bardo", "Blitzcrank", "Brand"),
            synergies = listOf("Miss Fortune", "Samira", "Kai'Sa", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("annie", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Yasuo", "Katarina", "Akali", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Syndra", "Orianna", "Lux", "Ahri", "Akali", "Akshan", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("annie", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Yuumi", "Sona", "Soraka", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Nautilus", "Leona", "Blitzcrank", "Ahri", "Alistar", "Amumu", "Ashe", "Bardo", "Brand", "Braum"),
            synergies = listOf("Jhin", "Miss Fortune", "Samira", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("ashe", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Sivir", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Tristana", "Akshan", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa", "Kalista"),
            synergies = listOf("Braum", "Seraphine", "Lulu", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("ashe", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Bardo", "Brand", "Braum"),
            synergies = listOf("Jhin", "Miss Fortune", "Varus", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("aurelion_sol", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Malphite", "Annie", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Fizz", "Zed", "Katarina", "Ahri", "Akali", "Akshan", "Annie", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Amumu", "Galio", "Lee Sin", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("aurora", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Ziggs", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Zed", "Akali", "Kassadin", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Bardo", "Brand", "Caitlyn"),
            synergies = listOf("Jarvan IV", "Vi", "Nautilus", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("aurora", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Darius", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Irelia", "Camille", "Renekton", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Blitzcrank"),
            synergies = listOf("Lee Sin", "Amumu", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("bard", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Brand", "Braum"),
            synergies = listOf("Jhin", "Ezreal", "Caitlyn", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("blitzcrank", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Morgana", "Leona", "Braum", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jinx", "Samira", "Draven", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("brand", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Kha'Zix", "Lee Sin", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Camille", "Darius", "Diana", "Dr. Mundo"),
            synergies = listOf("Galio", "Nautilus", "Malphite", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("brand", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Kassadin", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("brand", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Nautilus", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Thresh"),
            counters = listOf("Blitzcrank", "Pyke", "Leona", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Braum"),
            synergies = listOf("Jhin", "Ashe", "Miss Fortune", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("braum", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Leona", "Nautilus", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Morgana", "Senna", "Zyra", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Lucian", "Ashe", "Kai'Sa", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("caitlyn", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Samira", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Tristana", "Varus", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Morgana", "Lux", "Thresh", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("camille", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Xin Zhao", "Olaf", "Aatrox", "Ambessa", "Amumu", "Brand", "Darius", "Diana", "Dr. Mundo"),
            synergies = listOf("Galio", "Orianna", "Ahri", "Lee Sin", "Jarvan IV", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("camille", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Gnar", "Sion", "Garen", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Jax", "Renekton", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Galio", "Shen", "Jarvan IV", "Lee Sin", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("cho_gath", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Fizz", "Zed", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Yuumi", "Yunara"),
            counters = listOf("Aurelion Sol", "Syndra", "Orianna", "Ahri", "Akali", "Akshan", "Annie", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Yasuo", "Diana", "Vi", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("cho_gath", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Malphite", "Shen", "Dr. Mundo", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Gwen", "Vayne", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Yasuo", "Orianna", "Jarvan IV", "Lee Sin", "Galio", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("corki", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Ezreal", "Jhin", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Lucian", "Akshan", "Ashe", "Ezreal", "Jhin", "Jinx", "Kai'Sa", "Kalista"),
            synergies = listOf("Leona", "Nautilus", "Thresh", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Amumu")
        ),
        MatchupKey("corki", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Yasuo", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("darius", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Sett", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Vayne", "Teemo", "Fiora", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("diana", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Dr. Mundo"),
            synergies = listOf("Yasuo", "Orianna", "Kennen", "Lee Sin", "Jarvan IV", "Galio", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("diana", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Kassadin", "Talon", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Galio", "Kassadin", "Vex", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Yasuo", "Jarvan IV", "Amumu", "Lee Sin", "Orianna", "Galio", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("dr_mundo", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Warwick", "Lee Sin", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Ahri", "Galio", "Orianna", "Lee Sin", "Jarvan IV", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("dr_mundo", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Malphite", "Sion", "Shen", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Gwen", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora", "Blitzcrank"),
            synergies = listOf("Lee Sin", "Vi", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("draven", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Caitlyn", "Varus", "Ashe", "Akshan", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa", "Kalista"),
            synergies = listOf("Thresh", "Nautilus", "Leona", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Amumu")
        ),
        MatchupKey("ekko", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Malphite", "Nautilus", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("ekko", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Ziggs", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Kassadin", "Pantheon", "Galio", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Vi", "Jarvan IV", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("evelynn", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Amumu", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Rengar", "Warwick", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Shen", "Galio", "Yuumi", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("ezreal", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Jinx", "Vayne", "Kai'Sa", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Tristana", "Akshan", "Ashe", "Corki", "Jhin", "Jinx", "Kai'Sa", "Kalista"),
            synergies = listOf("Karma", "Yuumi", "Lux", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("ezreal", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Ziggs", "Lux", "Veigar", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Zed", "Yasuo", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Nautilus", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("fiddlesticks", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Xin Zhao", "Olaf", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Kennen", "Miss Fortune", "Galio", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("fiddlesticks", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Leona", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Miss Fortune", "Samira", "Jhin", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("fiora", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Aatrox", "Sion", "Cho'Gath", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Malphite", "Jax", "Renekton", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("fizz", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Twisted Fate", "Lux", "Veigar", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Galio", "Kassadin", "Pantheon", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("galio", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Akali", "Ahri", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Yasuo", "Lucian", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Camille", "Jarvan IV", "Pantheon", "Lee Sin", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("galio", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Morgana", "Janna", "Thresh", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Samira", "Kai'Sa", "Miss Fortune", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("garen", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Riven", "Jax", "Irelia", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Darius", "Fiora", "Vayne", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("gnar", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sett", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Irelia", "Yasuo", "Malphite", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Yasuo", "Orianna", "Jarvan IV", "Lee Sin", "Galio", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("gragas", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Kha'Zix", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Xin Zhao", "Lee Sin", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Yasuo", "Orianna", "Ahri", "Lee Sin", "Jarvan IV", "Galio", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("gragas", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Zed", "Talon", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Yuumi", "Yunara"),
            counters = listOf("Kassadin", "Galio", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Yasuo", "Diana", "Vi", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("gragas", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Jax", "Irelia", "Riven", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Darius", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora", "Blitzcrank"),
            synergies = listOf("Yasuo", "Lee Sin", "Vi", "Jarvan IV", "Orianna", "Galio", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("graves", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Kha'Zix", "Evelynn", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Rammus", "Amumu", "Xin Zhao", "Aatrox", "Ambessa", "Brand", "Camille", "Darius", "Diana", "Dr. Mundo"),
            synergies = listOf("Galio", "Nautilus", "Shen", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("graves", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sett", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Malphite", "Teemo", "Irelia", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Amumu", "Vi", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("gwen", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Kha'Zix", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Nautilus", "Ahri", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("gwen", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Jax", "Riven", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Orianna", "Lee Sin", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("hecarim", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Amumu", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Warwick", "Lee Sin", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Yuumi", "Orianna", "Lulu", "Lee Sin", "Jarvan IV", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("heimerdinger", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Fizz", "Talon", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Syndra", "Lux", "Ziggs", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("heimerdinger", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Alistar", "Braum", "Leona", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jhin", "Caitlyn", "Ashe", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("heimerdinger", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Nasus", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Irelia", "Camille", "Jayce", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Lee Sin", "Shen", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("hwei", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("hwei", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Pyke", "Nautilus", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jhin", "Varus", "Caitlyn", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("irelia", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Ziggs", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Akali", "Zed", "Yasuo", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("irelia", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Aatrox", "Gnar", "Jayce", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Jax", "Fiora", "Sett", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("janna", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Leona", "Alistar", "Rell", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Sona", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jinx", "Vayne", "Zeri", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("jarvan_iv", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Xin Zhao", "Lee Sin", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Orianna", "Miss Fortune", "Lee Sin", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("jarvan_iv", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Jayce", "Kennen", "Teemo", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Darius", "Fiora", "Jax", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Galio", "Yasuo", "Ahri", "Lee Sin", "Orianna", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("jax", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Xin Zhao", "Zyra", "Zed", "Wukong", "Warwick", "Volibear", "Viego", "Vi"),
            counters = listOf("Lee Sin", "Olaf", "Warwick", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Orianna", "Ahri", "Lee Sin", "Jarvan IV", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("jax", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Camille", "Fiora", "Irelia", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Malphite", "Garen", "Gragas", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("jayce", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Yasuo", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Nautilus", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("jayce", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sett", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Irelia", "Malphite", "Wukong", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("jhin", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Tristana", "Lucian", "Akshan", "Ashe", "Caitlyn", "Corki", "Ezreal", "Jinx", "Kai'Sa"),
            synergies = listOf("Morgana", "Leona", "Nautilus", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Thresh")
        ),
        MatchupKey("jinx", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Varus", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Twitch"),
            counters = listOf("Draven", "Lucian", "Tristana", "Akshan", "Ashe", "Caitlyn", "Corki", "Ezreal", "Jhin", "Kai'Sa"),
            synergies = listOf("Thresh", "Lulu", "Nautilus", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Leona")
        ),
        MatchupKey("k_sante", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Gwen", "Darius", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("kai_sa", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Ezreal", "Twitch", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Lucian", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kalista"),
            synergies = listOf("Nautilus", "Leona", "Alistar", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Thresh")
        ),
        MatchupKey("kalista", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Ezreal", "Jhin", "Sivir", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Vayne"),
            counters = listOf("Ashe", "Draven", "Caitlyn", "Akshan", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa", "Kindred"),
            synergies = listOf("Thresh", "Nautilus", "Blitzcrank", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Leona")
        ),
        MatchupKey("karma", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Yasuo", "Katarina", "Akali", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Syndra", "Orianna", "Lux", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Hecarim", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("karma", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Leona", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Ezreal", "Lucian", "Caitlyn", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("kassadin", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Ahri", "Katarina", "Ekko", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Talon", "Lucian", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Amumu", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("katarina", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Ziggs", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Galio", "Kassadin", "Pantheon", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Amumu", "Malphite", "Diana", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Nautilus", "Leona")
        ),
        MatchupKey("kayle", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Galio", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("kayle", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Singed", "Garen", "Sion", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Irelia", "Jax", "Renekton", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Shen", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("kayn", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Shyvana", "Amumu", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Xin Zhao", "Graves", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Nautilus", "Shen", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("kennen", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Yasuo", "Katarina", "Zed", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Yuumi", "Yunara"),
            counters = listOf("Syndra", "Orianna", "Lux", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Amumu", "Jarvan IV", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("kennen", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sett", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Irelia", "Malphite", "Jayce", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Amumu", "Jarvan IV", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("kha_zix", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Evelynn", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Rammus", "Warwick", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Nautilus", "Shen", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("kindred", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Lucian", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Bardo", "Nautilus", "Leona", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Thresh")
        ),
        MatchupKey("kindred", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Rammus", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Kha'Zix", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Bardo", "Shen", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("kog_maw", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Ezreal", "Sivir", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Lucian", "Tristana", "Akshan", "Ashe", "Caitlyn", "Corki", "Ezreal", "Jhin", "Jinx"),
            synergies = listOf("Lulu", "Milio", "Janna", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("kog_maw", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Talon", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Amumu", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("lee_sin", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Kha'Zix", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Warwick", "Rammus", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Yasuo", "Ahri", "Galio", "Jarvan IV", "Orianna", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("leona", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Morgana", "Janna", "Thresh", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Samira", "Kai'Sa", "Miss Fortune", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("lillia", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Rammus", "Amumu", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Kha'Zix", "Lee Sin", "Rengar", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Yone", "Yasuo", "Galio", "Lee Sin", "Jarvan IV", "Orianna", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("lillia", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sion", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Irelia", "Fiora", "Camille", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Yone", "Yasuo", "Jarvan IV", "Lee Sin", "Orianna", "Galio", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("lissandra", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Zed", "Yasuo", "Katarina", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Yuumi", "Yunara"),
            counters = listOf("Syndra", "Orianna", "Lux", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("lucian", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Varus", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Nami", "Braum", "Leona", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("lucian", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Kassadin", "Aurelion Sol", "Veigar", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Yasuo", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("lulu", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Leona", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jinx", "Kog'Maw", "Vayne", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("lux", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Ziggs", "Annie", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Zed", "Fizz", "Yasuo", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Nautilus", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("lux", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Caitlyn", "Jhin", "Ezreal", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("malphite", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Zed", "Talon", "Yasuo", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Yuumi", "Yunara"),
            counters = listOf("Kassadin", "Galio", "Vladimir", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Yasuo", "Diana", "Miss Fortune", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("malphite", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Morgana", "Janna", "Braum", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Yasuo", "Miss Fortune", "Samira", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("malphite", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Tryndamere", "Jax", "Fiora", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Gwen", "Mordekaiser", "Dr. Mundo", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Yasuo", "Orianna", "Miss Fortune", "Lee Sin", "Jarvan IV", "Galio", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("maokai", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Kha'Zix", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Lee Sin", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Ahri", "Yasuo", "Lee Sin", "Jarvan IV", "Orianna", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("maokai", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Morgana", "Janna", "Thresh", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Samira", "Kai'Sa", "Jinx", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("maokai", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Gwen", "Darius", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("master_yi", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Shyvana", "Amumu", "Dr. Mundo", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Rammus", "Lee Sin", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Lulu", "Yuumi", "Morgana", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("mel", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Ziggs", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("mel", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jhin", "Caitlyn", "Varus", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("milio", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Leona", "Nautilus", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Pyke", "Zyra", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jinx", "Kog'Maw", "Caitlyn", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("miss_fortune", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Sivir", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Tristana", "Lucian", "Akshan", "Ashe", "Caitlyn", "Corki", "Ezreal", "Jhin", "Jinx"),
            synergies = listOf("Amumu", "Leona", "Nautilus", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Thresh")
        ),
        MatchupKey("miss_fortune", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Zyra", "Brand", "Sona", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Leona", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jhin", "Ashe", "Varus", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("mordekaiser", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Olaf", "Warwick", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Nautilus", "Ahri", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("mordekaiser", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Garen", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Vayne", "Olaf", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("morgana", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Kha'Zix", "Lee Sin", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Nautilus", "Ahri", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("morgana", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Yasuo", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("morgana", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Blitzcrank", "Nautilus", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Sona", "Soraka", "Zyra", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Caitlyn", "Jhin", "Samira", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("nami", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Lucian", "Samira", "Jhin", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("nasus", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Kayle", "Teemo", "Singed", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Darius", "Fiora", "Gwen", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("nautilus", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Lee Sin", "Warwick", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Yasuo", "Ahri", "Galio", "Lee Sin", "Jarvan IV", "Orianna", "Malphite", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("nautilus", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Morgana", "Janna", "Braum", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Samira", "Kai'Sa", "Yasuo", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("nautilus", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Cho'Gath", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Gwen", "Darius", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Yasuo", "Orianna", "Lee Sin", "Jarvan IV", "Galio", "Malphite", "Leona", "Thresh", "Amumu", "Diana")
        ),
        MatchupKey("nidalee", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Amumu", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Kha'Zix", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Renekton", "Nautilus", "Galio", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("nilah", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Miss Fortune", "Jhin", "Ezreal", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Vayne"),
            counters = listOf("Caitlyn", "Draven", "Ashe", "Akshan", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa", "Kalista"),
            synergies = listOf("Sona", "Yuumi", "Nami", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("nilah", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Orianna", "Lee Sin", "Jarvan IV", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("nocturne", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Warwick", "Lee Sin", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Twisted Fate", "Shen", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("norra", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Ziggs", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("norra", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jhin", "Caitlyn", "Ezreal", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("nunu_willump", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Lee Sin", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Katarina", "Kennen", "Orianna", "Lee Sin", "Jarvan IV", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("olaf", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Kha'Zix", "Graves", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Yuumi", "Lulu", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("olaf", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Aatrox", "Mordekaiser", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Vayne", "Camille", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("orianna", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Annie", "Veigar", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Yasuo", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Malphite", "Diana", "Lee Sin", "Galio", "Yasuo", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("ornn", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Gwen", "Vayne", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Yasuo", "Miss Fortune", "Orianna", "Lee Sin", "Jarvan IV", "Galio", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("pantheon", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Lee Sin", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Twisted Fate", "Ahri", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("pantheon", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Kassadin", "Fizz", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Galio", "Ahri", "Syndra", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Twisted Fate", "Vi", "Taliyah", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("pantheon", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Nautilus", "Leona", "Braum", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Draven", "Samira", "Kai'Sa", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("pantheon", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Teemo", "Jayce", "Kennen", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Malphite", "Shen", "Poppy", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Twisted Fate", "Lee Sin", "Taliyah", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("poppy", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Kha'Zix", "Lee Sin", "Rengar", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Warwick", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Ahri", "Nautilus", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("poppy", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Rakan", "Leona", "Alistar", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Thresh"),
            counters = listOf("Morgana", "Janna", "Zyra", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Vayne", "Jhin", "Samira", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("poppy", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Riven", "Irelia", "Camille", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Darius", "Sett", "Mordekaiser", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("pyke", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Galio", "Pantheon", "Kassadin", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Vi", "Jarvan IV", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("pyke", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Nautilus", "Leona", "Morgana", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Draven", "Samira", "Lucian", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("rakan", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Braum", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Thresh"),
            counters = listOf("Morgana", "Leona", "Thresh", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Xayah", "Samira", "Yasuo", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("rammus", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Xin Zhao", "Graves", "Zyra", "Zed", "Wukong", "Warwick", "Volibear", "Viego", "Vi"),
            counters = listOf("Olaf", "Gwen", "Lillia", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Ahri", "Orianna", "Lee Sin", "Jarvan IV", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("rell", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Amumu", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Lee Sin", "Kha'Zix", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Miss Fortune", "Katarina", "Galio", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("rell", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Janna", "Morgana", "Thresh", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Samira", "Miss Fortune", "Kai'Sa", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("renekton", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Riven", "Irelia", "Yasuo", "Yone", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir", "Viktor"),
            counters = listOf("Fiora", "Garen", "Darius", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Nidalee", "Taliyah", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("rengar", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Evelynn", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Rammus", "Warwick", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Shen", "Galio", "Orianna", "Lee Sin", "Jarvan IV", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("rengar", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Teemo", "Kayle", "Jayce", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Darius", "Sett", "Shen", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Lee Sin", "Vi", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("riven", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Aatrox", "Yasuo", "Irelia", "Yone", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir", "Viktor"),
            counters = listOf("Renekton", "Garen", "Poppy", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("rumble", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Kha'Zix", "Lee Sin", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Nautilus", "Orianna", "Lee Sin", "Jarvan IV", "Yasuo", "Malphite", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("rumble", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Yasuo", "Katarina", "Zed", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Yuumi", "Yunara"),
            counters = listOf("Kassadin", "Galio", "Syndra", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("rumble", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Darius", "Fiora", "Irelia", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("ryze", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Twisted Fate", "Annie", "Veigar", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("ryze", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sion", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Irelia", "Camille", "Jax", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Lee Sin", "Shen", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("samira", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Miss Fortune", "Jhin", "Ashe", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Vayne"),
            counters = listOf("Caitlyn", "Draven", "Tristana", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Nautilus", "Leona", "Rakan", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Thresh")
        ),
        MatchupKey("senna", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Tristana", "Lucian", "Akshan", "Ashe", "Caitlyn", "Corki", "Ezreal", "Jhin", "Jinx"),
            synergies = listOf("Nautilus", "Thresh", "Leona", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Amumu")
        ),
        MatchupKey("senna", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Lucian", "Jhin", "Ashe", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("seraphine", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Lucian", "Tristana", "Akshan", "Ashe", "Caitlyn", "Corki", "Ezreal", "Jhin", "Jinx"),
            synergies = listOf("Sona", "Karma", "Nautilus", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Leona")
        ),
        MatchupKey("seraphine", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Ziggs", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Amumu", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("seraphine", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Sona", "Miss Fortune", "Ashe", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("sett", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Morgana", "Janna", "Thresh", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Samira", "Kai'Sa", "Draven", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("sett", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Irelia", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Vayne", "Renekton", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("shen", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Morgana", "Thresh", "Zyra", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Samira", "Kai'Sa", "Jinx", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("shen", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Jax", "Irelia", "Fiora", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Darius", "Mordekaiser", "Gwen", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Twitch", "Evelynn", "Nocturne", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("shyvana", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Amumu", "Rammus", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Xin Zhao", "Olaf", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Orianna", "Galio", "Yuumi", "Lee Sin", "Jarvan IV", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("singed", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Garen", "Jax", "Shen", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Teemo", "Vayne", "Fiora", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Yuumi", "Hecarim", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("sion", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Malphite", "Cho'Gath", "Shen", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Gwen", "Darius", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Yasuo", "Orianna", "Jarvan IV", "Lee Sin", "Galio", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("sivir", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Caitlyn", "Jhin", "Ashe", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Vayne"),
            counters = listOf("Draven", "Lucian", "Tristana", "Akshan", "Ashe", "Caitlyn", "Corki", "Ezreal", "Jhin", "Jinx"),
            synergies = listOf("Yuumi", "Lulu", "Leona", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("skarner", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Lee Sin", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Ahri", "Orianna", "Lee Sin", "Jarvan IV", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("skarner", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Gwen", "Darius", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("smolder", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Ezreal", "Sivir", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Tristana", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Thresh", "Nautilus", "Leona", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Amumu")
        ),
        MatchupKey("smolder", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("sona", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Leona", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Seraphine", "Ezreal", "Jhin", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("soraka", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jinx", "Vayne", "Caitlyn", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("swain", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Fizz", "Akali", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Syndra", "Orianna", "Lux", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("swain", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jhin", "Ashe", "Miss Fortune", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("swain", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Darius", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Irelia", "Gwen", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Lee Sin", "Vi", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("syndra", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Annie", "Veigar", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Fizz", "Zed", "Katarina", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("taliyah", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Amumu", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Kha'Zix", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Pantheon", "Renekton", "Nautilus", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Leona")
        ),
        MatchupKey("taliyah", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Twisted Fate", "Aurelion Sol", "Veigar", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Pantheon", "Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("talon", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Xin Zhao", "Warwick", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Nautilus", "Shen", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("talon", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Galio", "Pantheon", "Kassadin", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("teemo", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Twisted Fate", "Veigar", "Aurelion Sol", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Syndra", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("teemo", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Nasus", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Irelia", "Malphite", "Jayce", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Amumu", "Vi", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("thresh", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Leona", "Braum", "Rakan", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra"),
            counters = listOf("Morgana", "Zyra", "Brand", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Blitzcrank"),
            synergies = listOf("Jinx", "Samira", "Draven", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("tristana", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Lucian", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Leona", "Nautilus", "Lulu", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Thresh")
        ),
        MatchupKey("tristana", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Kassadin", "Aurelion Sol", "Veigar", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Yasuo", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("tryndamere", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Rammus", "Warwick", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Lulu", "Yuumi", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("tryndamere", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Dr. Mundo", "Shen", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Malphite", "Jax", "Teemo", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("twisted_fate", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Aurelion Sol", "Veigar", "Kassadin", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Yasuo", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Pantheon", "Shen", "Camille", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("twitch", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Ezreal", "Sivir", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Lucian", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Yuumi", "Lulu", "Shen", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("twitch", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Lee Sin", "Kha'Zix", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Shen", "Galio", "Yuumi", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("urgot", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Darius", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Fiora", "Vayne", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora", "Blitzcrank"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("varus", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Samira", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Twitch"),
            counters = listOf("Draven", "Tristana", "Lucian", "Akshan", "Ashe", "Caitlyn", "Corki", "Ezreal", "Jhin", "Jinx"),
            synergies = listOf("Thresh", "Nautilus", "Leona", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Amumu")
        ),
        MatchupKey("varus", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Yasuo", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("vayne", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Ezreal", "Sivir", "Kai'Sa", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Tristana", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Lulu", "Janna", "Milio", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("vayne", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sion", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir"),
            counters = listOf("Malphite", "Irelia", "Teemo", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Shen", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("veigar", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal", "Ziggs", "Zeri", "Yunara", "Yasuo", "Xayah", "Varus", "Twitch"),
            counters = listOf("Draven", "Lucian", "Tristana", "Akshan", "Ashe", "Caitlyn", "Corki", "Ezreal", "Jhin", "Jinx"),
            synergies = listOf("Nautilus", "Leona", "Thresh", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Amumu")
        ),
        MatchupKey("veigar", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Twisted Fate", "Annie", "Aurelion Sol", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Katarina", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("vel_koz", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Annie", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Yasuo", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("vel_koz", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Zilean", "Yuumi", "Vex", "Syndra", "Swain"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jhin", "Varus", "Caitlyn", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("vex", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Yasuo", "Katarina", "Akali", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Syndra", "Orianna", "Lux", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("vi", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Kha'Zix", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego"),
            counters = listOf("Olaf", "Warwick", "Lee Sin", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Yasuo", "Ahri", "Galio", "Lee Sin", "Jarvan IV", "Orianna", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("viego", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Shyvana", "Amumu", "Maestro Yi", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vi"),
            counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Nautilus", "Ahri", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("viego", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Kassadin", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Yasuo", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("viktor", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Annie", "Veigar", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("vladimir", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Lux", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Kassadin", "Galio", "Orianna", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("vladimir", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Shen", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viktor"),
            counters = listOf("Riven", "Irelia", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora", "Blitzcrank"),
            synergies = listOf("Lee Sin", "Vi", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("volibear", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Amumu", "Zyra", "Zed", "Xin Zhao", "Wukong", "Warwick", "Viego", "Vi"),
            counters = listOf("Olaf", "Warwick", "Lee Sin", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Ahri", "Nautilus", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("volibear", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Riven", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Vladimir", "Viktor"),
            counters = listOf("Fiora", "Vayne", "Jax", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("warwick", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Kha'Zix", "Zyra", "Zed", "Xin Zhao", "Wukong", "Volibear", "Viego", "Vi"),
            counters = listOf("Olaf", "Rammus", "Lee Sin", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Ahri", "Shen", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("warwick", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Darius", "Yone", "Yasuo", "Xin Zhao", "Wukong", "Volibear", "Vladimir", "Viktor"),
            counters = listOf("Fiora", "Vayne", "Teemo", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("wukong", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn", "Zyra", "Zed", "Xin Zhao", "Warwick", "Volibear", "Viego", "Vi"),
            counters = listOf("Olaf", "Lee Sin", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Yasuo", "Ahri", "Galio", "Lee Sin", "Jarvan IV", "Orianna", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("wukong", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Jayce", "Kennen", "Teemo", "Yone", "Yasuo", "Xin Zhao", "Warwick", "Volibear", "Vladimir", "Viktor"),
            counters = listOf("Darius", "Garen", "Sett", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Yasuo", "Orianna", "Lee Sin", "Jarvan IV", "Galio", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("xayah", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Samira", "Kai'Sa", "Vayne", "Ziggs", "Zeri", "Yunara", "Yasuo", "Veigar", "Varus", "Twitch"),
            counters = listOf("Caitlyn", "Varus", "Draven", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Rakan", "Nautilus", "Leona", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Thresh")
        ),
        MatchupKey("xin_zhao", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Kha'Zix", "Zyra", "Zed", "Wukong", "Warwick", "Volibear", "Viego", "Vi"),
            counters = listOf("Olaf", "Warwick", "Rammus", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Yasuo", "Ahri", "Galio", "Lee Sin", "Jarvan IV", "Orianna", "Malphite", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("xin_zhao", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Irelia", "Yasuo", "Riven", "Yone", "Wukong", "Warwick", "Volibear", "Vladimir", "Viktor", "Vi"),
            counters = listOf("Jax", "Darius", "Malphite", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("yasuo", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Miss Fortune", "Jhin", "Ashe", "Ziggs", "Zeri", "Yunara", "Xayah", "Veigar", "Vayne", "Varus"),
            counters = listOf("Draven", "Caitlyn", "Vayne", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Gragas", "Nautilus", "Alistar", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("yasuo", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Ziggs", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Vex", "Annie", "Pantheon", "Ahri", "Akali", "Akshan", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Malphite", "Diana", "Gragas", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("yasuo", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Gnar", "Dr. Mundo", "Yone", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir", "Viktor"),
            counters = listOf("Renekton", "Darius", "Sett", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Malphite", "Gragas", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("yone", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Aurelion Sol", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Akali", "Vex", "Pantheon", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Malphite", "Lillia", "Diana", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Nautilus", "Leona")
        ),
        MatchupKey("yone", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Cho'Gath", "Dr. Mundo", "Yasuo", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Vladimir", "Viktor"),
            counters = listOf("Renekton", "Jax", "Fiora", "Aatrox", "Akali", "Akshan", "Alistar", "Ambessa", "Aurelion Sol", "Aurora"),
            synergies = listOf("Jarvan IV", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona", "Thresh", "Amumu")
        ),
        MatchupKey("yunara", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Samira", "Ziggs", "Zeri", "Yasuo", "Xayah", "Veigar", "Varus", "Twitch"),
            counters = listOf("Caitlyn", "Draven", "Varus", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Thresh", "Lulu", "Braum", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("yunara", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi"),
            counters = listOf("Zed", "Yasuo", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("yuumi", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Janna", "Zyra", "Zoe", "Zilean", "Vex", "Vel'Koz", "Thresh", "Syndra"),
            counters = listOf("Blitzcrank", "Nautilus", "Leona", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Zeri", "Twitch", "Jinx", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("zed", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn", "Zyra", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego", "Vi"),
            counters = listOf("Lee Sin", "Rammus", "Warwick", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Nautilus", "Shen", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("zed", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Ziggs", "Zyra", "Zoe", "Zilean", "Zeri", "Yuumi", "Yunara", "Yone"),
            counters = listOf("Malphite", "Lissandra", "Galio", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Vi", "Jarvan IV", "Diana", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("zeri", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ashe", "Ziggs", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus", "Twitch"),
            counters = listOf("Draven", "Tristana", "Caitlyn", "Akshan", "Ashe", "Corki", "Ezreal", "Jhin", "Jinx", "Kai'Sa"),
            synergies = listOf("Yuumi", "Lulu", "Janna", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("ziggs", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Jinx", "Zeri", "Yunara", "Yasuo", "Xayah", "Veigar", "Varus", "Twitch"),
            counters = listOf("Draven", "Lucian", "Tristana", "Akshan", "Ashe", "Caitlyn", "Corki", "Ezreal", "Jhin", "Jinx"),
            synergies = listOf("Nautilus", "Leona", "Thresh", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Amumu")
        ),
        MatchupKey("ziggs", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Annie", "Twisted Fate", "Zyra", "Zoe", "Zilean", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Zed", "Fizz", "Yasuo", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("zilean", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol", "Zyra", "Zoe", "Ziggs", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Hecarim", "Jarvan IV", "Vi", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("zilean", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zyra", "Zoe", "Yuumi", "Vex", "Vel'Koz", "Syndra", "Swain"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Hecarim", "Darius", "Jinx", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
        MatchupKey("zoe", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Twisted Fate", "Zyra", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Zed", "Fizz", "Yasuo", "Ahri", "Akali", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo"),
            synergies = listOf("Jarvan IV", "Vi", "Nautilus", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("zyra", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana", "Zed", "Xin Zhao", "Wukong", "Warwick", "Volibear", "Viego", "Vi"),
            counters = listOf("Kha'Zix", "Lee Sin", "Xin Zhao", "Aatrox", "Ambessa", "Amumu", "Brand", "Camille", "Darius", "Diana"),
            synergies = listOf("Galio", "Nautilus", "Ahri", "Lee Sin", "Jarvan IV", "Orianna", "Yasuo", "Malphite", "Leona", "Thresh")
        ),
        MatchupKey("zyra", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol", "Zoe", "Zilean", "Ziggs", "Zeri", "Zed", "Yuumi", "Yunara"),
            counters = listOf("Zed", "Fizz", "Akali", "Ahri", "Akshan", "Annie", "Aurelion Sol", "Aurora", "Bardo", "Brand"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu", "Lee Sin", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus", "Leona")
        ),
        MatchupKey("zyra", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh", "Zoe", "Zilean", "Yuumi", "Vex", "Vel'Koz", "Syndra", "Swain"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Ahri", "Alistar", "Amumu", "Annie", "Ashe", "Bardo", "Brand"),
            synergies = listOf("Jhin", "Ashe", "Miss Fortune", "Lee Sin", "Jarvan IV", "Orianna", "Galio", "Yasuo", "Malphite", "Nautilus")
        ),
    )

    fun getMatchups(champion: Champion, role: LaneRole): MatchupRoleResult {
        val cleanId = champion.id.lowercase().replace("-", "_").replace(" ", "_").replace("'", "")
        val nameId = champion.name.lowercase().replace("-", "_").replace(" ", "_").replace("'", "")
        val ddragon = champion.ddragonId.lowercase().replace("-", "_").replace(" ", "_").replace("'", "")

        val found = matchupDatabase[MatchupKey(cleanId, role)]
            ?: matchupDatabase[MatchupKey(champion.id.lowercase().trim(), role)]
            ?: matchupDatabase[MatchupKey(nameId, role)]
            ?: matchupDatabase[MatchupKey(ddragon, role)]
        if (found != null) {
            return found
        }

        // Try primary role if role is same as primary
        if (role == champion.primaryRole) {
            val primaryMatch = matchupDatabase[MatchupKey(cleanId, champion.primaryRole)]
                ?: matchupDatabase[MatchupKey(champion.id.lowercase().trim(), champion.primaryRole)]
                ?: matchupDatabase[MatchupKey(nameId, champion.primaryRole)]
            if (primaryMatch != null) {
                return primaryMatch
            }
        }

                // Dynamic Role-Strict Fallback (10 curated champions for Pro/Premium tier)
        return when (role) {
            LaneRole.TOP -> MatchupRoleResult(
                advantages = listOf("Sion", "Dr. Mundo", "Shen", "Cho'Gath", "Malphite", "Nasus", "Garen", "Teemo", "Kayle", "Urgot"),
                counters = listOf("Fiora", "Irelia", "Camille", "Jax", "Riven", "Renekton", "Kled", "Vayne", "Olaf", "Warwick"),
                synergies = listOf("Lee Sin", "Jarvan IV", "Orianna", "Galio", "Amumu", "Malphite", "Yasuo", "Gragas", "Diana", "Nautilus")
            )
            LaneRole.JUNGLE -> MatchupRoleResult(
                advantages = listOf("Amumu", "Shyvana", "Maestro Yi", "Evelynn", "Fiddlesticks", "Lillia", "Kha'Zix", "Kindred", "Rammus", "Ekko"),
                counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix", "Warwick", "Olaf", "Volibear", "Jax", "Rengar", "Pantheon", "Vi"),
                synergies = listOf("Galio", "Ahri", "Yasuo", "Orianna", "Malphite", "Renekton", "Diana", "Leona", "Nautilus", "Jarvan IV")
            )
            LaneRole.MID -> MatchupRoleResult(
                advantages = listOf("Lux", "Veigar", "Ziggs", "Aurelion Sol", "Twisted Fate", "Brand", "Corki", "Seraphine", "Annie", "Syndra"),
                counters = listOf("Zed", "Yasuo", "Kassadin", "Fizz", "Akali", "Yone", "Vex", "Galio", "Talon", "Katarina"),
                synergies = listOf("Jarvan IV", "Vi", "Diana", "Lee Sin", "Xin Zhao", "Wukong", "Pantheon", "Amumu", "Nautilus", "Gragas")
            )
            LaneRole.ADC -> MatchupRoleResult(
                advantages = listOf("Vayne", "Kai'Sa", "Samira", "Senna", "Varus", "Sivir", "Corki", "Miss Fortune", "Ashe", "Zeri"),
                counters = listOf("Draven", "Caitlyn", "Tristana", "Lucian", "Twitch", "Kalista", "Ezreal", "Jhin", "Akshan", "Blitzcrank"),
                synergies = listOf("Thresh", "Nautilus", "Leona", "Lulu", "Braum", "Janna", "Milio", "Yuumi", "Nami", "Blitzcrank")
            )
            LaneRole.SUPPORT -> MatchupRoleResult(
                advantages = listOf("Sona", "Soraka", "Yuumi", "Nami", "Janna", "Senna", "Milio", "Seraphine", "Lux", "Lulu"),
                counters = listOf("Blitzcrank", "Nautilus", "Pyke", "Morgana", "Braum", "Alistar", "Leona", "Thresh", "Zyra", "Brand"),
                synergies = listOf("Samira", "Jinx", "Kai'Sa", "Draven", "Lucian", "Vayne", "Varus", "Tristana", "Miss Fortune", "Kalista")
            )
        }
    }
}
