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
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi"),
            counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix"),
            synergies = listOf("Ahri", "Yasuo", "Galio")
        ),
        MatchupKey("aatrox", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Dr. Mundo", "Shen"),
            counters = listOf("Fiora", "Irelia", "Camille"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna")
        ),
        MatchupKey("ahri", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Ziggs", "Aurelion Sol"),
            counters = listOf("Yasuo", "Zed", "Kassadin"),
            synergies = listOf("Lee Sin", "Vi", "Jarvan IV")
        ),
        MatchupKey("akali", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Twisted Fate"),
            counters = listOf("Galio", "Pantheon", "Vex"),
            synergies = listOf("Jarvan IV", "Diana", "Nautilus")
        ),
        MatchupKey("akali", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sion"),
            counters = listOf("Renekton", "Sett", "Shen"),
            synergies = listOf("Lee Sin", "Vi", "Amumu")
        ),
        MatchupKey("akshan", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal"),
            counters = listOf("Draven", "Caitlyn", "Lucian"),
            synergies = listOf("Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("akshan", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Aurelion Sol", "Kassadin", "Veigar"),
            counters = listOf("Zed", "Yasuo", "Akali"),
            synergies = listOf("Nautilus", "Jarvan IV", "Vi")
        ),
        MatchupKey("akshan", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sett"),
            counters = listOf("Irelia", "Camille", "Jax"),
            synergies = listOf("Sejuani", "Amumu", "Maokai")
        ),
        MatchupKey("alistar", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Morgana", "Janna", "Braum"),
            synergies = listOf("Samira", "Kai'Sa", "Yasuo")
        ),
        MatchupKey("ambessa", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Amumu"),
            counters = listOf("Lee Sin", "Kha'Zix", "Warwick"),
            synergies = listOf("Galio", "Ahri", "Nautilus")
        ),
        MatchupKey("ambessa", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Aatrox"),
            counters = listOf("Fiora", "Jax", "Renekton"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna")
        ),
        MatchupKey("amumu", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Rammus", "Xin Zhao"),
            counters = listOf("Olaf", "Lee Sin", "Kha'Zix"),
            synergies = listOf("Miss Fortune", "Samira", "Katarina")
        ),
        MatchupKey("amumu", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Yuumi", "Soraka"),
            counters = listOf("Morgana", "Janna", "Braum"),
            synergies = listOf("Miss Fortune", "Samira", "Kai'Sa")
        ),
        MatchupKey("annie", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Yasuo", "Katarina", "Akali"),
            counters = listOf("Syndra", "Orianna", "Lux"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana")
        ),
        MatchupKey("annie", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Yuumi", "Sona", "Soraka"),
            counters = listOf("Nautilus", "Leona", "Blitzcrank"),
            synergies = listOf("Jhin", "Miss Fortune", "Samira")
        ),
        MatchupKey("ashe", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Sivir"),
            counters = listOf("Draven", "Caitlyn", "Tristana"),
            synergies = listOf("Braum", "Seraphine", "Lulu")
        ),
        MatchupKey("ashe", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Jhin", "Miss Fortune", "Varus")
        ),
        MatchupKey("aurelion_sol", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Malphite", "Annie"),
            counters = listOf("Fizz", "Zed", "Katarina"),
            synergies = listOf("Jarvan IV", "Amumu", "Galio")
        ),
        MatchupKey("aurora", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Ziggs"),
            counters = listOf("Zed", "Akali", "Kassadin"),
            synergies = listOf("Jarvan IV", "Vi", "Nautilus")
        ),
        MatchupKey("aurora", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Darius"),
            counters = listOf("Irelia", "Camille", "Renekton"),
            synergies = listOf("Lee Sin", "Amumu", "Sejuani")
        ),
        MatchupKey("bard", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Jhin", "Ezreal", "Caitlyn")
        ),
        MatchupKey("blitzcrank", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Morgana", "Leona", "Braum"),
            synergies = listOf("Jinx", "Samira", "Draven")
        ),
        MatchupKey("brand", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana"),
            counters = listOf("Kha'Zix", "Lee Sin", "Xin Zhao"),
            synergies = listOf("Galio", "Nautilus", "Malphite")
        ),
        MatchupKey("brand", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol"),
            counters = listOf("Zed", "Fizz", "Kassadin"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana")
        ),
        MatchupKey("brand", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Nautilus"),
            counters = listOf("Blitzcrank", "Pyke", "Leona"),
            synergies = listOf("Jhin", "Ashe", "Miss Fortune")
        ),
        MatchupKey("braum", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Leona", "Nautilus", "Thresh"),
            counters = listOf("Morgana", "Senna", "Zyra"),
            synergies = listOf("Lucian", "Ashe", "Kai'Sa")
        ),
        MatchupKey("caitlyn", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Samira"),
            counters = listOf("Draven", "Tristana", "Varus"),
            synergies = listOf("Morgana", "Lux", "Thresh")
        ),
        MatchupKey("camille", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn"),
            counters = listOf("Lee Sin", "Xin Zhao", "Olaf"),
            synergies = listOf("Galio", "Orianna", "Ahri")
        ),
        MatchupKey("camille", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Gnar", "Sion", "Garen"),
            counters = listOf("Fiora", "Jax", "Renekton"),
            synergies = listOf("Galio", "Shen", "Jarvan IV")
        ),
        MatchupKey("cho_gath", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Fizz", "Zed"),
            counters = listOf("Aurelion Sol", "Syndra", "Orianna"),
            synergies = listOf("Yasuo", "Diana", "Vi")
        ),
        MatchupKey("cho_gath", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Malphite", "Shen", "Dr. Mundo"),
            counters = listOf("Fiora", "Gwen", "Vayne"),
            synergies = listOf("Yasuo", "Orianna", "Jarvan IV")
        ),
        MatchupKey("corki", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Ezreal", "Jhin"),
            counters = listOf("Draven", "Caitlyn", "Lucian"),
            synergies = listOf("Leona", "Nautilus", "Thresh")
        ),
        MatchupKey("corki", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Twisted Fate"),
            counters = listOf("Zed", "Yasuo", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("darius", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Sett"),
            counters = listOf("Vayne", "Teemo", "Fiora"),
            synergies = listOf("Sejuani", "Jarvan IV", "Vi")
        ),
        MatchupKey("diana", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi"),
            counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix"),
            synergies = listOf("Yasuo", "Orianna", "Kennen")
        ),
        MatchupKey("diana", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Kassadin", "Talon"),
            counters = listOf("Galio", "Kassadin", "Vex"),
            synergies = listOf("Yasuo", "Jarvan IV", "Amumu")
        ),
        MatchupKey("dr_mundo", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana"),
            counters = listOf("Olaf", "Warwick", "Lee Sin"),
            synergies = listOf("Ahri", "Galio", "Orianna")
        ),
        MatchupKey("dr_mundo", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Malphite", "Sion", "Shen"),
            counters = listOf("Fiora", "Gwen", "Aatrox"),
            synergies = listOf("Lee Sin", "Vi", "Jarvan IV")
        ),
        MatchupKey("draven", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal"),
            counters = listOf("Caitlyn", "Varus", "Ashe"),
            synergies = listOf("Thresh", "Nautilus", "Leona")
        ),
        MatchupKey("ekko", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi"),
            counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix"),
            synergies = listOf("Galio", "Malphite", "Nautilus")
        ),
        MatchupKey("ekko", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Ziggs"),
            counters = listOf("Kassadin", "Pantheon", "Galio"),
            synergies = listOf("Vi", "Jarvan IV", "Diana")
        ),
        MatchupKey("evelynn", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Amumu", "Shyvana"),
            counters = listOf("Lee Sin", "Rengar", "Warwick"),
            synergies = listOf("Shen", "Galio", "Yuumi")
        ),
        MatchupKey("ezreal", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Jinx", "Vayne", "Kai'Sa"),
            counters = listOf("Draven", "Caitlyn", "Tristana"),
            synergies = listOf("Karma", "Yuumi", "Lux")
        ),
        MatchupKey("ezreal", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Ziggs", "Lux", "Veigar"),
            counters = listOf("Zed", "Yasuo", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Nautilus")
        ),
        MatchupKey("fiddlesticks", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Maestro Yi"),
            counters = listOf("Lee Sin", "Xin Zhao", "Olaf"),
            synergies = listOf("Kennen", "Miss Fortune", "Galio")
        ),
        MatchupKey("fiddlesticks", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Blitzcrank", "Nautilus", "Leona"),
            synergies = listOf("Miss Fortune", "Samira", "Jhin")
        ),
        MatchupKey("fiora", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Aatrox", "Sion", "Cho'Gath"),
            counters = listOf("Malphite", "Jax", "Renekton"),
            synergies = listOf("Sejuani", "Jarvan IV", "Vi")
        ),
        MatchupKey("fizz", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Twisted Fate", "Lux", "Veigar"),
            counters = listOf("Galio", "Kassadin", "Pantheon"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("galio", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Akali", "Ahri"),
            counters = listOf("Zed", "Yasuo", "Lucian"),
            synergies = listOf("Camille", "Jarvan IV", "Pantheon")
        ),
        MatchupKey("galio", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Morgana", "Janna", "Thresh"),
            synergies = listOf("Samira", "Kai'Sa", "Miss Fortune")
        ),
        MatchupKey("garen", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Riven", "Jax", "Irelia"),
            counters = listOf("Darius", "Fiora", "Vayne"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin")
        ),
        MatchupKey("gnar", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sett"),
            counters = listOf("Irelia", "Yasuo", "Malphite"),
            synergies = listOf("Yasuo", "Orianna", "Jarvan IV")
        ),
        MatchupKey("gragas", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Kha'Zix", "Shyvana"),
            counters = listOf("Olaf", "Xin Zhao", "Lee Sin"),
            synergies = listOf("Yasuo", "Orianna", "Ahri")
        ),
        MatchupKey("gragas", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Zed", "Talon"),
            counters = listOf("Kassadin", "Galio", "Ahri"),
            synergies = listOf("Yasuo", "Diana", "Vi")
        ),
        MatchupKey("gragas", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Jax", "Irelia", "Riven"),
            counters = listOf("Fiora", "Darius", "Aatrox"),
            synergies = listOf("Yasuo", "Lee Sin", "Vi")
        ),
        MatchupKey("graves", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Kha'Zix", "Evelynn", "Maestro Yi"),
            counters = listOf("Rammus", "Amumu", "Xin Zhao"),
            synergies = listOf("Galio", "Nautilus", "Shen")
        ),
        MatchupKey("graves", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sett"),
            counters = listOf("Malphite", "Teemo", "Irelia"),
            synergies = listOf("Sejuani", "Amumu", "Vi")
        ),
        MatchupKey("gwen", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana"),
            counters = listOf("Lee Sin", "Kha'Zix", "Xin Zhao"),
            synergies = listOf("Galio", "Nautilus", "Ahri")
        ),
        MatchupKey("gwen", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo"),
            counters = listOf("Fiora", "Jax", "Riven"),
            synergies = listOf("Jarvan IV", "Vi", "Orianna")
        ),
        MatchupKey("hecarim", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Amumu", "Shyvana"),
            counters = listOf("Olaf", "Warwick", "Lee Sin"),
            synergies = listOf("Yuumi", "Orianna", "Lulu")
        ),
        MatchupKey("heimerdinger", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Fizz", "Talon"),
            counters = listOf("Syndra", "Lux", "Ziggs"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("heimerdinger", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Alistar", "Braum", "Leona"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Jhin", "Caitlyn", "Ashe")
        ),
        MatchupKey("heimerdinger", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Nasus"),
            counters = listOf("Irelia", "Camille", "Jayce"),
            synergies = listOf("Sejuani", "Lee Sin", "Shen")
        ),
        MatchupKey("hwei", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("hwei", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Pyke", "Nautilus"),
            synergies = listOf("Jhin", "Varus", "Caitlyn")
        ),
        MatchupKey("irelia", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Ziggs"),
            counters = listOf("Akali", "Zed", "Yasuo"),
            synergies = listOf("Jarvan IV", "Vi", "Diana")
        ),
        MatchupKey("irelia", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Aatrox", "Gnar", "Jayce"),
            counters = listOf("Jax", "Fiora", "Sett"),
            synergies = listOf("Sejuani", "Lee Sin", "Jarvan IV")
        ),
        MatchupKey("janna", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Leona", "Alistar", "Rell"),
            counters = listOf("Blitzcrank", "Nautilus", "Sona"),
            synergies = listOf("Jinx", "Vayne", "Zeri")
        ),
        MatchupKey("jarvan_iv", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn"),
            counters = listOf("Olaf", "Xin Zhao", "Lee Sin"),
            synergies = listOf("Galio", "Orianna", "Miss Fortune")
        ),
        MatchupKey("jarvan_iv", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Jayce", "Kennen", "Teemo"),
            counters = listOf("Darius", "Fiora", "Jax"),
            synergies = listOf("Galio", "Yasuo", "Ahri")
        ),
        MatchupKey("jax", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Xin Zhao"),
            counters = listOf("Lee Sin", "Olaf", "Warwick"),
            synergies = listOf("Galio", "Orianna", "Ahri")
        ),
        MatchupKey("jax", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Camille", "Fiora", "Irelia"),
            counters = listOf("Malphite", "Garen", "Gragas"),
            synergies = listOf("Sejuani", "Jarvan IV", "Lee Sin")
        ),
        MatchupKey("jayce", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Twisted Fate"),
            counters = listOf("Zed", "Yasuo", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Nautilus")
        ),
        MatchupKey("jayce", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sett"),
            counters = listOf("Irelia", "Malphite", "Wukong"),
            synergies = listOf("Sejuani", "Jarvan IV", "Lee Sin")
        ),
        MatchupKey("jhin", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal"),
            counters = listOf("Draven", "Tristana", "Lucian"),
            synergies = listOf("Morgana", "Leona", "Nautilus")
        ),
        MatchupKey("jinx", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Varus"),
            counters = listOf("Draven", "Lucian", "Tristana"),
            synergies = listOf("Thresh", "Lulu", "Nautilus")
        ),
        MatchupKey("k_sante", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo"),
            counters = listOf("Fiora", "Gwen", "Darius"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna")
        ),
        MatchupKey("kai_sa", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Ezreal", "Twitch"),
            counters = listOf("Draven", "Caitlyn", "Lucian"),
            synergies = listOf("Nautilus", "Leona", "Alistar")
        ),
        MatchupKey("kalista", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Ezreal", "Jhin", "Sivir"),
            counters = listOf("Ashe", "Draven", "Caitlyn"),
            synergies = listOf("Thresh", "Nautilus", "Blitzcrank")
        ),
        MatchupKey("karma", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Yasuo", "Katarina", "Akali"),
            counters = listOf("Syndra", "Orianna", "Lux"),
            synergies = listOf("Jarvan IV", "Vi", "Hecarim")
        ),
        MatchupKey("karma", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Leona"),
            synergies = listOf("Ezreal", "Lucian", "Caitlyn")
        ),
        MatchupKey("kassadin", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Ahri", "Katarina", "Ekko"),
            counters = listOf("Zed", "Talon", "Lucian"),
            synergies = listOf("Jarvan IV", "Amumu", "Vi")
        ),
        MatchupKey("katarina", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Ziggs"),
            counters = listOf("Galio", "Kassadin", "Pantheon"),
            synergies = listOf("Amumu", "Malphite", "Diana")
        ),
        MatchupKey("kayle", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Galio"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("kayle", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Singed", "Garen", "Sion"),
            counters = listOf("Irelia", "Jax", "Renekton"),
            synergies = listOf("Sejuani", "Jarvan IV", "Shen")
        ),
        MatchupKey("kayn", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Shyvana", "Amumu", "Maestro Yi"),
            counters = listOf("Lee Sin", "Xin Zhao", "Graves"),
            synergies = listOf("Galio", "Nautilus", "Shen")
        ),
        MatchupKey("kennen", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Yasuo", "Katarina", "Zed"),
            counters = listOf("Syndra", "Orianna", "Lux"),
            synergies = listOf("Amumu", "Jarvan IV", "Diana")
        ),
        MatchupKey("kennen", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sett"),
            counters = listOf("Irelia", "Malphite", "Jayce"),
            synergies = listOf("Amumu", "Jarvan IV", "Diana")
        ),
        MatchupKey("kha_zix", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Evelynn", "Shyvana"),
            counters = listOf("Lee Sin", "Rammus", "Warwick"),
            synergies = listOf("Galio", "Nautilus", "Shen")
        ),
        MatchupKey("kindred", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal"),
            counters = listOf("Draven", "Caitlyn", "Lucian"),
            synergies = listOf("Bardo", "Nautilus", "Leona")
        ),
        MatchupKey("kindred", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Rammus"),
            counters = listOf("Lee Sin", "Kha'Zix", "Xin Zhao"),
            synergies = listOf("Galio", "Bardo", "Shen")
        ),
        MatchupKey("kog_maw", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Ezreal", "Sivir"),
            counters = listOf("Draven", "Lucian", "Tristana"),
            synergies = listOf("Lulu", "Milio", "Janna")
        ),
        MatchupKey("kog_maw", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol"),
            counters = listOf("Zed", "Fizz", "Talon"),
            synergies = listOf("Jarvan IV", "Amumu", "Vi")
        ),
        MatchupKey("lee_sin", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Kha'Zix"),
            counters = listOf("Olaf", "Warwick", "Rammus"),
            synergies = listOf("Yasuo", "Ahri", "Galio")
        ),
        MatchupKey("leona", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Morgana", "Janna", "Thresh"),
            synergies = listOf("Samira", "Kai'Sa", "Miss Fortune")
        ),
        MatchupKey("lillia", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Rammus", "Amumu", "Shyvana"),
            counters = listOf("Kha'Zix", "Lee Sin", "Rengar"),
            synergies = listOf("Yone", "Yasuo", "Galio")
        ),
        MatchupKey("lillia", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sion"),
            counters = listOf("Irelia", "Fiora", "Camille"),
            synergies = listOf("Yone", "Yasuo", "Jarvan IV")
        ),
        MatchupKey("lissandra", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Zed", "Yasuo", "Katarina"),
            counters = listOf("Syndra", "Orianna", "Lux"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("lucian", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal"),
            counters = listOf("Draven", "Caitlyn", "Varus"),
            synergies = listOf("Nami", "Braum", "Leona")
        ),
        MatchupKey("lucian", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Kassadin", "Aurelion Sol", "Veigar"),
            counters = listOf("Zed", "Yasuo", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin")
        ),
        MatchupKey("lulu", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Leona"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Jinx", "Kog'Maw", "Vayne")
        ),
        MatchupKey("lux", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Ziggs", "Annie"),
            counters = listOf("Zed", "Fizz", "Yasuo"),
            synergies = listOf("Jarvan IV", "Vi", "Nautilus")
        ),
        MatchupKey("lux", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Caitlyn", "Jhin", "Ezreal")
        ),
        MatchupKey("malphite", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Zed", "Talon", "Yasuo"),
            counters = listOf("Kassadin", "Galio", "Vladimir"),
            synergies = listOf("Yasuo", "Diana", "Miss Fortune")
        ),
        MatchupKey("malphite", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Morgana", "Janna", "Braum"),
            synergies = listOf("Yasuo", "Miss Fortune", "Samira")
        ),
        MatchupKey("malphite", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Tryndamere", "Jax", "Fiora"),
            counters = listOf("Gwen", "Mordekaiser", "Dr. Mundo"),
            synergies = listOf("Yasuo", "Orianna", "Miss Fortune")
        ),
        MatchupKey("maokai", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Kha'Zix"),
            counters = listOf("Olaf", "Lee Sin", "Xin Zhao"),
            synergies = listOf("Galio", "Ahri", "Yasuo")
        ),
        MatchupKey("maokai", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Morgana", "Janna", "Thresh"),
            synergies = listOf("Samira", "Kai'Sa", "Jinx")
        ),
        MatchupKey("maokai", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo"),
            counters = listOf("Fiora", "Gwen", "Darius"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna")
        ),
        MatchupKey("master_yi", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Shyvana", "Amumu", "Dr. Mundo"),
            counters = listOf("Rammus", "Lee Sin", "Xin Zhao"),
            synergies = listOf("Lulu", "Yuumi", "Morgana")
        ),
        MatchupKey("mel", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Ziggs"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("mel", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Jhin", "Caitlyn", "Varus")
        ),
        MatchupKey("milio", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Leona", "Nautilus", "Thresh"),
            counters = listOf("Blitzcrank", "Pyke", "Zyra"),
            synergies = listOf("Jinx", "Kog'Maw", "Caitlyn")
        ),
        MatchupKey("miss_fortune", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Sivir"),
            counters = listOf("Draven", "Tristana", "Lucian"),
            synergies = listOf("Amumu", "Leona", "Nautilus")
        ),
        MatchupKey("miss_fortune", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Zyra", "Brand", "Sona"),
            counters = listOf("Blitzcrank", "Nautilus", "Leona"),
            synergies = listOf("Jhin", "Ashe", "Varus")
        ),
        MatchupKey("mordekaiser", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi"),
            counters = listOf("Lee Sin", "Olaf", "Warwick"),
            synergies = listOf("Galio", "Nautilus", "Ahri")
        ),
        MatchupKey("mordekaiser", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Garen"),
            counters = listOf("Fiora", "Vayne", "Olaf"),
            synergies = listOf("Jarvan IV", "Sejuani", "Vi")
        ),
        MatchupKey("morgana", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana"),
            counters = listOf("Kha'Zix", "Lee Sin", "Xin Zhao"),
            synergies = listOf("Galio", "Nautilus", "Ahri")
        ),
        MatchupKey("morgana", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol"),
            counters = listOf("Zed", "Yasuo", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin")
        ),
        MatchupKey("morgana", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Blitzcrank", "Nautilus", "Thresh"),
            counters = listOf("Sona", "Soraka", "Zyra"),
            synergies = listOf("Caitlyn", "Jhin", "Samira")
        ),
        MatchupKey("nami", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Lucian", "Samira", "Jhin")
        ),
        MatchupKey("nasus", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Kayle", "Teemo", "Singed"),
            counters = listOf("Darius", "Fiora", "Gwen"),
            synergies = listOf("Sejuani", "Jarvan IV", "Vi")
        ),
        MatchupKey("nautilus", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn"),
            counters = listOf("Olaf", "Lee Sin", "Warwick"),
            synergies = listOf("Yasuo", "Ahri", "Galio")
        ),
        MatchupKey("nautilus", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Morgana", "Janna", "Braum"),
            synergies = listOf("Samira", "Kai'Sa", "Yasuo")
        ),
        MatchupKey("nautilus", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Cho'Gath"),
            counters = listOf("Fiora", "Gwen", "Darius"),
            synergies = listOf("Yasuo", "Orianna", "Lee Sin")
        ),
        MatchupKey("nidalee", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Amumu"),
            counters = listOf("Lee Sin", "Kha'Zix", "Xin Zhao"),
            synergies = listOf("Renekton", "Nautilus", "Galio")
        ),
        MatchupKey("nilah", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Miss Fortune", "Jhin", "Ezreal"),
            counters = listOf("Caitlyn", "Draven", "Ashe"),
            synergies = listOf("Sona", "Yuumi", "Nami")
        ),
        MatchupKey("nilah", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi"),
            counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix"),
            synergies = listOf("Galio", "Orianna", "Taric")
        ),
        MatchupKey("nocturne", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn"),
            counters = listOf("Olaf", "Warwick", "Lee Sin"),
            synergies = listOf("Galio", "Twisted Fate", "Shen")
        ),
        MatchupKey("norra", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Ziggs"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("norra", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Jhin", "Caitlyn", "Ezreal")
        ),
        MatchupKey("nunu_willump", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi"),
            counters = listOf("Olaf", "Lee Sin", "Xin Zhao"),
            synergies = listOf("Katarina", "Kennen", "Orianna")
        ),
        MatchupKey("olaf", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana"),
            counters = listOf("Lee Sin", "Kha'Zix", "Graves"),
            synergies = listOf("Galio", "Yuumi", "Lulu")
        ),
        MatchupKey("olaf", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Aatrox", "Mordekaiser"),
            counters = listOf("Fiora", "Vayne", "Camille"),
            synergies = listOf("Sejuani", "Jarvan IV", "Vi")
        ),
        MatchupKey("orianna", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Annie", "Veigar", "Twisted Fate"),
            counters = listOf("Zed", "Fizz", "Yasuo"),
            synergies = listOf("Jarvan IV", "Malphite", "Diana")
        ),
        MatchupKey("ornn", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo"),
            counters = listOf("Fiora", "Gwen", "Vayne"),
            synergies = listOf("Yasuo", "Miss Fortune", "Orianna")
        ),
        MatchupKey("pantheon", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn"),
            counters = listOf("Olaf", "Lee Sin", "Xin Zhao"),
            synergies = listOf("Galio", "Twisted Fate", "Ahri")
        ),
        MatchupKey("pantheon", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Kassadin", "Fizz"),
            counters = listOf("Galio", "Ahri", "Syndra"),
            synergies = listOf("Twisted Fate", "Vi", "Taliyah")
        ),
        MatchupKey("pantheon", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Nautilus", "Leona", "Braum"),
            synergies = listOf("Draven", "Samira", "Kai'Sa")
        ),
        MatchupKey("pantheon", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Teemo", "Jayce", "Kennen"),
            counters = listOf("Malphite", "Shen", "Poppy"),
            synergies = listOf("Twisted Fate", "Lee Sin", "Taliyah")
        ),
        MatchupKey("poppy", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Kha'Zix", "Lee Sin", "Rengar"),
            counters = listOf("Olaf", "Warwick", "Xin Zhao"),
            synergies = listOf("Galio", "Ahri", "Nautilus")
        ),
        MatchupKey("poppy", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Rakan", "Leona", "Alistar"),
            counters = listOf("Morgana", "Janna", "Zyra"),
            synergies = listOf("Vayne", "Jhin", "Samira")
        ),
        MatchupKey("poppy", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Riven", "Irelia", "Camille"),
            counters = listOf("Darius", "Sett", "Mordekaiser"),
            synergies = listOf("Sejuani", "Jarvan IV", "Vi")
        ),
        MatchupKey("pyke", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Twisted Fate"),
            counters = listOf("Galio", "Pantheon", "Kassadin"),
            synergies = listOf("Vi", "Jarvan IV", "Diana")
        ),
        MatchupKey("pyke", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Nautilus", "Leona", "Morgana"),
            synergies = listOf("Draven", "Samira", "Lucian")
        ),
        MatchupKey("rakan", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Braum"),
            counters = listOf("Morgana", "Leona", "Thresh"),
            synergies = listOf("Xayah", "Samira", "Yasuo")
        ),
        MatchupKey("rammus", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Xin Zhao", "Graves"),
            counters = listOf("Olaf", "Gwen", "Lillia"),
            synergies = listOf("Galio", "Ahri", "Orianna")
        ),
        MatchupKey("rell", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Amumu"),
            counters = listOf("Olaf", "Lee Sin", "Kha'Zix"),
            synergies = listOf("Miss Fortune", "Katarina", "Galio")
        ),
        MatchupKey("rell", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Janna", "Morgana", "Thresh"),
            synergies = listOf("Samira", "Miss Fortune", "Kai'Sa")
        ),
        MatchupKey("renekton", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Riven", "Irelia", "Yasuo"),
            counters = listOf("Fiora", "Garen", "Darius"),
            synergies = listOf("Nidalee", "Taliyah", "Lee Sin")
        ),
        MatchupKey("rengar", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Evelynn", "Shyvana"),
            counters = listOf("Lee Sin", "Rammus", "Warwick"),
            synergies = listOf("Shen", "Galio", "Orianna")
        ),
        MatchupKey("rengar", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Teemo", "Kayle", "Jayce"),
            counters = listOf("Darius", "Sett", "Shen"),
            synergies = listOf("Sejuani", "Lee Sin", "Vi")
        ),
        MatchupKey("riven", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Aatrox", "Yasuo", "Irelia"),
            counters = listOf("Renekton", "Garen", "Poppy"),
            synergies = listOf("Sejuani", "Jarvan IV", "Lee Sin")
        ),
        MatchupKey("rumble", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana"),
            counters = listOf("Kha'Zix", "Lee Sin", "Xin Zhao"),
            synergies = listOf("Galio", "Nautilus", "Orianna")
        ),
        MatchupKey("rumble", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Yasuo", "Katarina", "Zed"),
            counters = listOf("Kassadin", "Galio", "Syndra"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana")
        ),
        MatchupKey("rumble", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo"),
            counters = listOf("Darius", "Fiora", "Irelia"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana")
        ),
        MatchupKey("ryze", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Twisted Fate", "Annie", "Veigar"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("ryze", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sion"),
            counters = listOf("Irelia", "Camille", "Jax"),
            synergies = listOf("Sejuani", "Lee Sin", "Shen")
        ),
        MatchupKey("samira", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Miss Fortune", "Jhin", "Ashe"),
            counters = listOf("Caitlyn", "Draven", "Tristana"),
            synergies = listOf("Nautilus", "Leona", "Rakan")
        ),
        MatchupKey("senna", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal"),
            counters = listOf("Draven", "Tristana", "Lucian"),
            synergies = listOf("Nautilus", "Thresh", "Leona")
        ),
        MatchupKey("senna", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Lucian", "Jhin", "Ashe")
        ),
        MatchupKey("seraphine", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal"),
            counters = listOf("Draven", "Lucian", "Tristana"),
            synergies = listOf("Sona", "Karma", "Nautilus")
        ),
        MatchupKey("seraphine", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Ziggs"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Jarvan IV", "Amumu", "Vi")
        ),
        MatchupKey("seraphine", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Sona", "Miss Fortune", "Ashe")
        ),
        MatchupKey("sett", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Morgana", "Janna", "Thresh"),
            synergies = listOf("Samira", "Kai'Sa", "Draven")
        ),
        MatchupKey("sett", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Irelia"),
            counters = listOf("Fiora", "Vayne", "Renekton"),
            synergies = listOf("Sejuani", "Jarvan IV", "Vi")
        ),
        MatchupKey("shen", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Yuumi"),
            counters = listOf("Morgana", "Thresh", "Zyra"),
            synergies = listOf("Samira", "Kai'Sa", "Jinx")
        ),
        MatchupKey("shen", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Jax", "Irelia", "Fiora"),
            counters = listOf("Darius", "Mordekaiser", "Gwen"),
            synergies = listOf("Twitch", "Evelynn", "Nocturne")
        ),
        MatchupKey("shyvana", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Amumu", "Rammus"),
            counters = listOf("Lee Sin", "Xin Zhao", "Olaf"),
            synergies = listOf("Orianna", "Galio", "Yuumi")
        ),
        MatchupKey("singed", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Garen", "Jax", "Shen"),
            counters = listOf("Teemo", "Vayne", "Fiora"),
            synergies = listOf("Yuumi", "Hecarim", "Sejuani")
        ),
        MatchupKey("sion", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Malphite", "Cho'Gath", "Shen"),
            counters = listOf("Fiora", "Gwen", "Darius"),
            synergies = listOf("Yasuo", "Orianna", "Jarvan IV")
        ),
        MatchupKey("sivir", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Caitlyn", "Jhin", "Ashe"),
            counters = listOf("Draven", "Lucian", "Tristana"),
            synergies = listOf("Yuumi", "Lulu", "Leona")
        ),
        MatchupKey("skarner", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn"),
            counters = listOf("Olaf", "Lee Sin", "Xin Zhao"),
            synergies = listOf("Galio", "Ahri", "Orianna")
        ),
        MatchupKey("skarner", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Malphite", "Dr. Mundo"),
            counters = listOf("Fiora", "Gwen", "Darius"),
            synergies = listOf("Lee Sin", "Jarvan IV", "Orianna")
        ),
        MatchupKey("smolder", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Ezreal", "Sivir"),
            counters = listOf("Draven", "Caitlyn", "Tristana"),
            synergies = listOf("Thresh", "Nautilus", "Leona")
        ),
        MatchupKey("smolder", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("sona", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Leona"),
            synergies = listOf("Seraphine", "Ezreal", "Jhin")
        ),
        MatchupKey("soraka", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Jinx", "Vayne", "Caitlyn")
        ),
        MatchupKey("swain", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Katarina", "Fizz", "Akali"),
            counters = listOf("Syndra", "Orianna", "Lux"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana")
        ),
        MatchupKey("swain", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Jhin", "Ashe", "Miss Fortune")
        ),
        MatchupKey("swain", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Darius"),
            counters = listOf("Fiora", "Irelia", "Gwen"),
            synergies = listOf("Sejuani", "Lee Sin", "Vi")
        ),
        MatchupKey("syndra", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Annie", "Veigar", "Twisted Fate"),
            counters = listOf("Fizz", "Zed", "Katarina"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("taliyah", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Amumu"),
            counters = listOf("Lee Sin", "Kha'Zix", "Xin Zhao"),
            synergies = listOf("Pantheon", "Renekton", "Nautilus")
        ),
        MatchupKey("taliyah", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Twisted Fate", "Aurelion Sol", "Veigar"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Pantheon", "Jarvan IV", "Vi")
        ),
        MatchupKey("talon", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn"),
            counters = listOf("Lee Sin", "Xin Zhao", "Warwick"),
            synergies = listOf("Galio", "Nautilus", "Shen")
        ),
        MatchupKey("talon", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Twisted Fate"),
            counters = listOf("Galio", "Pantheon", "Kassadin"),
            synergies = listOf("Jarvan IV", "Vi", "Diana")
        ),
        MatchupKey("teemo", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Twisted Fate", "Veigar", "Aurelion Sol"),
            counters = listOf("Zed", "Fizz", "Syndra"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("teemo", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Nasus"),
            counters = listOf("Irelia", "Malphite", "Jayce"),
            synergies = listOf("Sejuani", "Amumu", "Vi")
        ),
        MatchupKey("thresh", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Leona", "Braum", "Rakan"),
            counters = listOf("Morgana", "Zyra", "Brand"),
            synergies = listOf("Jinx", "Samira", "Draven")
        ),
        MatchupKey("tristana", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal"),
            counters = listOf("Draven", "Caitlyn", "Lucian"),
            synergies = listOf("Leona", "Nautilus", "Lulu")
        ),
        MatchupKey("tristana", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Kassadin", "Aurelion Sol", "Veigar"),
            counters = listOf("Zed", "Yasuo", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Lee Sin")
        ),
        MatchupKey("tryndamere", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi"),
            counters = listOf("Lee Sin", "Rammus", "Warwick"),
            synergies = listOf("Galio", "Lulu", "Yuumi")
        ),
        MatchupKey("tryndamere", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Dr. Mundo", "Shen"),
            counters = listOf("Malphite", "Jax", "Teemo"),
            synergies = listOf("Sejuani", "Jarvan IV", "Vi")
        ),
        MatchupKey("twisted_fate", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Aurelion Sol", "Veigar", "Kassadin"),
            counters = listOf("Zed", "Fizz", "Yasuo"),
            synergies = listOf("Pantheon", "Shen", "Camille")
        ),
        MatchupKey("twitch", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Ezreal", "Sivir"),
            counters = listOf("Draven", "Caitlyn", "Lucian"),
            synergies = listOf("Yuumi", "Lulu", "Shen")
        ),
        MatchupKey("twitch", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Shyvana", "Maestro Yi"),
            counters = listOf("Lee Sin", "Kha'Zix", "Xin Zhao"),
            synergies = listOf("Shen", "Galio", "Yuumi")
        ),
        MatchupKey("urgot", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Darius"),
            counters = listOf("Fiora", "Vayne", "Aatrox"),
            synergies = listOf("Sejuani", "Jarvan IV", "Vi")
        ),
        MatchupKey("varus", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Samira"),
            counters = listOf("Draven", "Tristana", "Lucian"),
            synergies = listOf("Thresh", "Nautilus", "Leona")
        ),
        MatchupKey("varus", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Twisted Fate"),
            counters = listOf("Zed", "Yasuo", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("vayne", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Ezreal", "Sivir", "Kai'Sa"),
            counters = listOf("Draven", "Caitlyn", "Tristana"),
            synergies = listOf("Lulu", "Janna", "Milio")
        ),
        MatchupKey("vayne", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Darius", "Garen", "Sion"),
            counters = listOf("Malphite", "Irelia", "Teemo"),
            synergies = listOf("Sejuani", "Jarvan IV", "Shen")
        ),
        MatchupKey("veigar", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ezreal"),
            counters = listOf("Draven", "Lucian", "Tristana"),
            synergies = listOf("Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("veigar", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Twisted Fate", "Annie", "Aurelion Sol"),
            counters = listOf("Zed", "Fizz", "Katarina"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("vel_koz", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Annie", "Twisted Fate"),
            counters = listOf("Zed", "Fizz", "Yasuo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("vel_koz", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Jhin", "Varus", "Caitlyn")
        ),
        MatchupKey("vex", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Yasuo", "Katarina", "Akali"),
            counters = listOf("Syndra", "Orianna", "Lux"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("vi", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Kha'Zix"),
            counters = listOf("Olaf", "Warwick", "Lee Sin"),
            synergies = listOf("Yasuo", "Ahri", "Galio")
        ),
        MatchupKey("viego", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Shyvana", "Amumu", "Maestro Yi"),
            counters = listOf("Lee Sin", "Xin Zhao", "Kha'Zix"),
            synergies = listOf("Galio", "Nautilus", "Ahri")
        ),
        MatchupKey("viego", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Kassadin"),
            counters = listOf("Zed", "Yasuo", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Diana")
        ),
        MatchupKey("viktor", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Annie", "Veigar", "Twisted Fate"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("vladimir", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Lux"),
            counters = listOf("Kassadin", "Galio", "Orianna"),
            synergies = listOf("Jarvan IV", "Amumu", "Diana")
        ),
        MatchupKey("vladimir", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Shen"),
            counters = listOf("Riven", "Irelia", "Aatrox"),
            synergies = listOf("Sejuani", "Lee Sin", "Vi")
        ),
        MatchupKey("volibear", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Amumu"),
            counters = listOf("Olaf", "Warwick", "Lee Sin"),
            synergies = listOf("Galio", "Ahri", "Nautilus")
        ),
        MatchupKey("volibear", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Riven"),
            counters = listOf("Fiora", "Vayne", "Jax"),
            synergies = listOf("Sejuani", "Jarvan IV", "Vi")
        ),
        MatchupKey("warwick", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Kha'Zix"),
            counters = listOf("Olaf", "Rammus", "Lee Sin"),
            synergies = listOf("Galio", "Ahri", "Shen")
        ),
        MatchupKey("warwick", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Garen", "Darius"),
            counters = listOf("Fiora", "Vayne", "Teemo"),
            synergies = listOf("Sejuani", "Jarvan IV", "Vi")
        ),
        MatchupKey("wukong", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn"),
            counters = listOf("Olaf", "Lee Sin", "Xin Zhao"),
            synergies = listOf("Yasuo", "Ahri", "Galio")
        ),
        MatchupKey("wukong", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Jayce", "Kennen", "Teemo"),
            counters = listOf("Darius", "Garen", "Sett"),
            synergies = listOf("Yasuo", "Orianna", "Lee Sin")
        ),
        MatchupKey("xayah", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Samira", "Kai'Sa", "Vayne"),
            counters = listOf("Caitlyn", "Varus", "Draven"),
            synergies = listOf("Rakan", "Nautilus", "Leona")
        ),
        MatchupKey("xin_zhao", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Kha'Zix"),
            counters = listOf("Olaf", "Warwick", "Rammus"),
            synergies = listOf("Yasuo", "Ahri", "Galio")
        ),
        MatchupKey("xin_zhao", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Irelia", "Yasuo", "Riven"),
            counters = listOf("Jax", "Darius", "Malphite"),
            synergies = listOf("Sejuani", "Jarvan IV", "Lee Sin")
        ),
        MatchupKey("yasuo", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Miss Fortune", "Jhin", "Ashe"),
            counters = listOf("Draven", "Caitlyn", "Vayne"),
            synergies = listOf("Gragas", "Nautilus", "Alistar")
        ),
        MatchupKey("yasuo", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Ziggs", "Twisted Fate"),
            counters = listOf("Vex", "Annie", "Pantheon"),
            synergies = listOf("Malphite", "Diana", "Gragas")
        ),
        MatchupKey("yasuo", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Gnar", "Dr. Mundo"),
            counters = listOf("Renekton", "Darius", "Sett"),
            synergies = listOf("Malphite", "Gragas", "Lee Sin")
        ),
        MatchupKey("yone", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Aurelion Sol"),
            counters = listOf("Akali", "Vex", "Pantheon"),
            synergies = listOf("Malphite", "Lillia", "Diana")
        ),
        MatchupKey("yone", LaneRole.TOP) to MatchupRoleResult(
            advantages = listOf("Sion", "Cho'Gath", "Dr. Mundo"),
            counters = listOf("Renekton", "Jax", "Fiora"),
            synergies = listOf("Sejuani", "Jarvan IV", "Lee Sin")
        ),
        MatchupKey("yunara", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Samira"),
            counters = listOf("Caitlyn", "Draven", "Varus"),
            synergies = listOf("Thresh", "Lulu", "Braum")
        ),
        MatchupKey("yunara", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Lux", "Twisted Fate"),
            counters = listOf("Zed", "Yasuo", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("yuumi", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Sona", "Soraka", "Janna"),
            counters = listOf("Blitzcrank", "Nautilus", "Leona"),
            synergies = listOf("Zeri", "Twitch", "Jinx")
        ),
        MatchupKey("zed", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Maestro Yi", "Shyvana", "Evelynn"),
            counters = listOf("Lee Sin", "Rammus", "Warwick"),
            synergies = listOf("Galio", "Nautilus", "Shen")
        ),
        MatchupKey("zed", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Ziggs"),
            counters = listOf("Malphite", "Lissandra", "Galio"),
            synergies = listOf("Vi", "Jarvan IV", "Diana")
        ),
        MatchupKey("zeri", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Ashe"),
            counters = listOf("Draven", "Tristana", "Caitlyn"),
            synergies = listOf("Yuumi", "Lulu", "Janna")
        ),
        MatchupKey("ziggs", LaneRole.ADC) to MatchupRoleResult(
            advantages = listOf("Vayne", "Kai'Sa", "Jinx"),
            counters = listOf("Draven", "Lucian", "Tristana"),
            synergies = listOf("Nautilus", "Leona", "Thresh")
        ),
        MatchupKey("ziggs", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Annie", "Twisted Fate"),
            counters = listOf("Zed", "Fizz", "Yasuo"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("zilean", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Hecarim", "Jarvan IV", "Vi")
        ),
        MatchupKey("zilean", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Hecarim", "Darius", "Jinx")
        ),
        MatchupKey("zoe", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Lux", "Veigar", "Twisted Fate"),
            counters = listOf("Zed", "Fizz", "Yasuo"),
            synergies = listOf("Jarvan IV", "Vi", "Nautilus")
        ),
        MatchupKey("zyra", LaneRole.JUNGLE) to MatchupRoleResult(
            advantages = listOf("Amumu", "Rammus", "Shyvana"),
            counters = listOf("Kha'Zix", "Lee Sin", "Xin Zhao"),
            synergies = listOf("Galio", "Nautilus", "Ahri")
        ),
        MatchupKey("zyra", LaneRole.MID) to MatchupRoleResult(
            advantages = listOf("Veigar", "Twisted Fate", "Aurelion Sol"),
            counters = listOf("Zed", "Fizz", "Akali"),
            synergies = listOf("Jarvan IV", "Vi", "Amumu")
        ),
        MatchupKey("zyra", LaneRole.SUPPORT) to MatchupRoleResult(
            advantages = listOf("Braum", "Alistar", "Thresh"),
            counters = listOf("Blitzcrank", "Nautilus", "Pyke"),
            synergies = listOf("Jhin", "Ashe", "Miss Fortune")
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

        // Dynamic Role-Strict Fallback
        return when (role) {
            LaneRole.TOP -> {
                val adv = if (champion.isRanged) listOf("Darius", "Garen", "Sion")
                    else if (champion.damageType == com.example.model.DamageType.MAGIC) listOf("Malphite", "Dr. Mundo", "Shen")
                    else listOf("Sion", "Dr. Mundo", "Aatrox")
                val cnt = if (champion.isRanged) listOf("Irelia", "Camille", "Jax")
                    else listOf("Fiora", "Darius", "Renekton")
                val syn = listOf("Lee Sin", "Jarvan IV", "Vi")
                MatchupRoleResult(adv, cnt, syn)
            }
            LaneRole.JUNGLE -> {
                val adv = if (champion.isFrontline) listOf("Maestro Yi", "Kha'Zix", "Evelynn")
                    else listOf("Amumu", "Shyvana", "Maestro Yi")
                val cnt = listOf("Lee Sin", "Xin Zhao", "Kha'Zix")
                val syn = listOf("Galio", "Ahri", "Yasuo")
                MatchupRoleResult(adv, cnt, syn)
            }
            LaneRole.MID -> {
                val adv = if (champion.damageType == com.example.model.DamageType.MAGIC) listOf("Lux", "Veigar", "Ziggs")
                    else listOf("Veigar", "Twisted Fate", "Aurelion Sol")
                val cnt = listOf("Zed", "Yasuo", "Kassadin")
                val syn = listOf("Jarvan IV", "Vi", "Diana")
                MatchupRoleResult(adv, cnt, syn)
            }
            LaneRole.ADC -> {
                val adv = listOf("Vayne", "Kai'Sa", "Samira")
                val cnt = listOf("Draven", "Caitlyn", "Tristana")
                val syn = listOf("Thresh", "Nautilus", "Leona")
                MatchupRoleResult(adv, cnt, syn)
            }
            LaneRole.SUPPORT -> {
                val adv = if (champion.isFrontline) listOf("Sona", "Soraka", "Yuumi")
                    else listOf("Braum", "Alistar", "Thresh")
                val cnt = if (champion.isFrontline) listOf("Morgana", "Janna", "Braum")
                    else listOf("Blitzcrank", "Nautilus", "Pyke")
                val syn = listOf("Samira", "Jinx", "Kai'Sa")
                MatchupRoleResult(adv, cnt, syn)
            }
        }
    }
}
