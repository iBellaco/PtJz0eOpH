import json

with open('app/src/main/res/raw/champions_part1.json', 'r', encoding='utf-8') as f:
    c1 = json.load(f)
with open('app/src/main/res/raw/champions_part2.json', 'r', encoding='utf-8') as f:
    c2 = json.load(f)

all_champs = c1 + c2
by_id = {c['id']: c for c in all_champs}
by_name = {c['name'].lower(): c for c in all_champs}

role_pools = {'TOP': [], 'JUNGLE': [], 'MID': [], 'ADC': [], 'SUPPORT': []}
for c in all_champs:
    roles = [c['primaryRole']] + c.get('secondaryRoles', [])
    for r in roles:
        if r in role_pools and c['name'] not in role_pools[r]:
            role_pools[r].append(c['name'])

# Explicit curated matchups for all 142 champions per role
curated = {
    # aatrox
    ("aatrox", "TOP"): (["Sion", "Dr. Mundo", "Shen"], ["Fiora", "Irelia", "Camille"], ["Lee Sin", "Jarvan IV", "Orianna"]),
    ("aatrox", "JUNGLE"): (["Amumu", "Shyvana", "Maestro Yi"], ["Lee Sin", "Xin Zhao", "Kha'Zix"], ["Ahri", "Yasuo", "Galio"]),
    # ahri
    ("ahri", "MID"): (["Lux", "Ziggs", "Aurelion Sol"], ["Yasuo", "Zed", "Kassadin"], ["Lee Sin", "Vi", "Jarvan IV"]),
    # akali
    ("akali", "MID"): (["Lux", "Veigar", "Twisted Fate"], ["Galio", "Pantheon", "Vex"], ["Jarvan IV", "Diana", "Nautilus"]),
    ("akali", "TOP"): (["Darius", "Garen", "Sion"], ["Renekton", "Sett", "Shen"], ["Lee Sin", "Vi", "Amumu"]),
    # akshan
    ("akshan", "MID"): (["Aurelion Sol", "Kassadin", "Veigar"], ["Zed", "Yasuo", "Akali"], ["Nautilus", "Jarvan IV", "Vi"]),
    ("akshan", "TOP"): (["Darius", "Garen", "Sett"], ["Irelia", "Camille", "Jax"], ["Sejuani", "Amumu", "Maokai"]),
    ("akshan", "ADC"): (["Vayne", "Kai'Sa", "Ezreal"], ["Draven", "Caitlyn", "Lucian"], ["Nautilus", "Leona", "Thresh"]),
    # alistar
    ("alistar", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Morgana", "Janna", "Braum"], ["Samira", "Kai'Sa", "Yasuo"]),
    # ambessa
    ("ambessa", "TOP"): (["Sion", "Garen", "Aatrox"], ["Fiora", "Jax", "Renekton"], ["Lee Sin", "Jarvan IV", "Orianna"]),
    ("ambessa", "JUNGLE"): (["Maestro Yi", "Shyvana", "Amumu"], ["Lee Sin", "Kha'Zix", "Warwick"], ["Galio", "Ahri", "Nautilus"]),
    # amumu
    ("amumu", "JUNGLE"): (["Maestro Yi", "Rammus", "Xin Zhao"], ["Olaf", "Lee Sin", "Kha'Zix"], ["Miss Fortune", "Samira", "Katarina"]),
    ("amumu", "SUPPORT"): (["Sona", "Yuumi", "Soraka"], ["Morgana", "Janna", "Braum"], ["Miss Fortune", "Samira", "Kai'Sa"]),
    # annie
    ("annie", "MID"): (["Yasuo", "Katarina", "Akali"], ["Syndra", "Orianna", "Lux"], ["Jarvan IV", "Amumu", "Diana"]),
    ("annie", "SUPPORT"): (["Yuumi", "Sona", "Soraka"], ["Nautilus", "Leona", "Blitzcrank"], ["Jhin", "Miss Fortune", "Samira"]),
    # ashe
    ("ashe", "ADC"): (["Vayne", "Kai'Sa", "Sivir"], ["Draven", "Caitlyn", "Tristana"], ["Braum", "Seraphine", "Lulu"]),
    ("ashe", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Blitzcrank", "Nautilus", "Pyke"], ["Jhin", "Miss Fortune", "Varus"]),
    # aurelion_sol
    ("aurelion_sol", "MID"): (["Veigar", "Malphite", "Annie"], ["Fizz", "Zed", "Katarina"], ["Jarvan IV", "Amumu", "Galio"]),
    # aurora
    ("aurora", "MID"): (["Lux", "Veigar", "Ziggs"], ["Zed", "Akali", "Kassadin"], ["Jarvan IV", "Vi", "Nautilus"]),
    ("aurora", "TOP"): (["Sion", "Garen", "Darius"], ["Irelia", "Camille", "Renekton"], ["Lee Sin", "Amumu", "Sejuani"]),
    # bard
    ("bard", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Jhin", "Ezreal", "Caitlyn"]),
    # blitzcrank
    ("blitzcrank", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Morgana", "Leona", "Braum"], ["Jinx", "Samira", "Draven"]),
    # brand
    ("brand", "SUPPORT"): (["Braum", "Alistar", "Nautilus"], ["Blitzcrank", "Pyke", "Leona"], ["Jhin", "Ashe", "Miss Fortune"]),
    ("brand", "MID"): (["Veigar", "Twisted Fate", "Aurelion Sol"], ["Zed", "Fizz", "Kassadin"], ["Jarvan IV", "Amumu", "Diana"]),
    ("brand", "JUNGLE"): (["Amumu", "Rammus", "Shyvana"], ["Kha'Zix", "Lee Sin", "Xin Zhao"], ["Galio", "Nautilus", "Malphite"]),
    # braum
    ("braum", "SUPPORT"): (["Leona", "Nautilus", "Thresh"], ["Morgana", "Senna", "Zyra"], ["Lucian", "Ashe", "Kai'Sa"]),
    # caitlyn
    ("caitlyn", "ADC"): (["Vayne", "Kai'Sa", "Samira"], ["Draven", "Tristana", "Varus"], ["Morgana", "Lux", "Thresh"]),
    # camille
    ("camille", "TOP"): (["Gnar", "Sion", "Garen"], ["Fiora", "Jax", "Renekton"], ["Galio", "Shen", "Jarvan IV"]),
    ("camille", "JUNGLE"): (["Maestro Yi", "Shyvana", "Evelynn"], ["Lee Sin", "Xin Zhao", "Olaf"], ["Galio", "Orianna", "Ahri"]),
    # cho_gath
    ("cho_gath", "TOP"): (["Malphite", "Shen", "Dr. Mundo"], ["Fiora", "Gwen", "Vayne"], ["Yasuo", "Orianna", "Jarvan IV"]),
    ("cho_gath", "MID"): (["Katarina", "Fizz", "Zed"], ["Aurelion Sol", "Syndra", "Orianna"], ["Yasuo", "Diana", "Vi"]),
    # corki
    ("corki", "MID"): (["Veigar", "Lux", "Twisted Fate"], ["Zed", "Yasuo", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    ("corki", "ADC"): (["Vayne", "Ezreal", "Jhin"], ["Draven", "Caitlyn", "Lucian"], ["Leona", "Nautilus", "Thresh"]),
    # darius
    ("darius", "TOP"): (["Sion", "Garen", "Sett"], ["Vayne", "Teemo", "Fiora"], ["Sejuani", "Jarvan IV", "Vi"]),
    # diana
    ("diana", "JUNGLE"): (["Amumu", "Shyvana", "Maestro Yi"], ["Lee Sin", "Xin Zhao", "Kha'Zix"], ["Yasuo", "Orianna", "Kennen"]),
    ("diana", "MID"): (["Katarina", "Kassadin", "Talon"], ["Galio", "Sett", "Renekton"], ["Yasuo", "Jarvan IV", "Amumu"]),
    # dr_mundo
    ("dr_mundo", "TOP"): (["Malphite", "Sion", "Shen"], ["Fiora", "Gwen", "Aatrox"], ["Lee Sin", "Vi", "Jarvan IV"]),
    ("dr_mundo", "JUNGLE"): (["Amumu", "Rammus", "Shyvana"], ["Olaf", "Warwick", "Lee Sin"], ["Ahri", "Galio", "Orianna"]),
    # draven
    ("draven", "ADC"): (["Vayne", "Kai'Sa", "Ezreal"], ["Caitlyn", "Varus", "Ashe"], ["Thresh", "Nautilus", "Leona"]),
    # ekko
    ("ekko", "JUNGLE"): (["Amumu", "Shyvana", "Maestro Yi"], ["Lee Sin", "Xin Zhao", "Kha'Zix"], ["Galio", "Malphite", "Nautilus"]),
    ("ekko", "MID"): (["Lux", "Veigar", "Ziggs"], ["Kassadin", "Pantheon", "Galio"], ["Vi", "Jarvan IV", "Diana"]),
    # evelynn
    ("evelynn", "JUNGLE"): (["Maestro Yi", "Amumu", "Shyvana"], ["Lee Sin", "Rengar", "Warwick"], ["Shen", "Galio", "Yuumi"]),
    # ezreal
    ("ezreal", "ADC"): (["Jinx", "Vayne", "Aphelios"], ["Draven", "Caitlyn", "Tristana"], ["Karma", "Yuumi", "Lux"]),
    ("ezreal", "MID"): (["Ziggs", "Lux", "Veigar"], ["Zed", "Yasuo", "Akali"], ["Jarvan IV", "Vi", "Nautilus"]),
    # fiddlesticks
    ("fiddlesticks", "JUNGLE"): (["Amumu", "Rammus", "Maestro Yi"], ["Lee Sin", "Xin Zhao", "Olaf"], ["Kennen", "Miss Fortune", "Galio"]),
    ("fiddlesticks", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Blitzcrank", "Nautilus", "Leona"], ["Miss Fortune", "Samira", "Jhin"]),
    # fiora
    ("fiora", "TOP"): (["Aatrox", "Sion", "Cho'Gath"], ["Malphite", "Jax", "Renekton"], ["Sejuani", "Jarvan IV", "Vi"]),
    # fizz
    ("fizz", "MID"): (["Twisted Fate", "Lux", "Veigar"], ["Galio", "Kassadin", "Pantheon"], ["Jarvan IV", "Vi", "Amumu"]),
    # galio
    ("galio", "MID"): (["Katarina", "Akali", "Ahri"], ["Zed", "Yasuo", "Lucian"], ["Camille", "Jarvan IV", "Pantheon"]),
    ("galio", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Morgana", "Janna", "Thresh"], ["Samira", "Kai'Sa", "Miss Fortune"]),
    # garen
    ("garen", "TOP"): (["Riven", "Jax", "Irelia"], ["Darius", "Fiora", "Vayne"], ["Jarvan IV", "Vi", "Lee Sin"]),
    # gnar
    ("gnar", "TOP"): (["Darius", "Garen", "Sett"], ["Irelia", "Yasuo", "Malphite"], ["Yasuo", "Orianna", "Jarvan IV"]),
    # gragas
    ("gragas", "JUNGLE"): (["Maestro Yi", "Kha'Zix", "Shyvana"], ["Olaf", "Xin Zhao", "Lee Sin"], ["Yasuo", "Orianna", "Ahri"]),
    ("gragas", "TOP"): (["Jax", "Irelia", "Riven"], ["Fiora", "Darius", "Aatrox"], ["Yasuo", "Lee Sin", "Vi"]),
    ("gragas", "MID"): (["Katarina", "Zed", "Talon"], ["Kassadin", "Galio", "Ahri"], ["Yasuo", "Diana", "Vi"]),
    # graves
    ("graves", "JUNGLE"): (["Kha'Zix", "Evelynn", "Maestro Yi"], ["Rammus", "Amumu", "Xin Zhao"], ["Galio", "Nautilus", "Shen"]),
    ("graves", "TOP"): (["Darius", "Garen", "Sett"], ["Malphite", "Teemo", "Irelia"], ["Sejuani", "Amumu", "Vi"]),
    # gwen
    ("gwen", "TOP"): (["Sion", "Malphite", "Dr. Mundo"], ["Fiora", "Jax", "Riven"], ["Jarvan IV", "Vi", "Orianna"]),
    ("gwen", "JUNGLE"): (["Amumu", "Rammus", "Shyvana"], ["Lee Sin", "Kha'Zix", "Xin Zhao"], ["Galio", "Nautilus", "Ahri"]),
    # hecarim
    ("hecarim", "JUNGLE"): (["Maestro Yi", "Amumu", "Shyvana"], ["Olaf", "Warwick", "Lee Sin"], ["Yuumi", "Orianna", "Lulu"]),
    # heimerdinger
    ("heimerdinger", "MID"): (["Katarina", "Fizz", "Talon"], ["Syndra", "Lux", "Ziggs"], ["Jarvan IV", "Vi", "Amumu"]),
    ("heimerdinger", "TOP"): (["Darius", "Garen", "Nasus"], ["Irelia", "Camille", "Jayce"], ["Sejuani", "Lee Sin", "Shen"]),
    ("heimerdinger", "SUPPORT"): (["Alistar", "Braum", "Leona"], ["Blitzcrank", "Nautilus", "Pyke"], ["Jhin", "Caitlyn", "Ashe"]),
    # hwei
    ("hwei", "MID"): (["Veigar", "Twisted Fate", "Aurelion Sol"], ["Zed", "Fizz", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    ("hwei", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Pyke", "Nautilus"], ["Jhin", "Varus", "Caitlyn"]),
    # irelia
    ("irelia", "TOP"): (["Aatrox", "Gnar", "Jayce"], ["Jax", "Fiora", "Sett"], ["Sejuani", "Lee Sin", "Jarvan IV"]),
    ("irelia", "MID"): (["Lux", "Veigar", "Ziggs"], ["Akali", "Zed", "Yasuo"], ["Jarvan IV", "Vi", "Diana"]),
    # janna
    ("janna", "SUPPORT"): (["Leona", "Alistar", "Rell"], ["Blitzcrank", "Nautilus", "Sona"], ["Jinx", "Vayne", "Zeri"]),
    # jarvan_iv
    ("jarvan_iv", "JUNGLE"): (["Maestro Yi", "Shyvana", "Evelynn"], ["Olaf", "Xin Zhao", "Lee Sin"], ["Galio", "Orianna", "Miss Fortune"]),
    ("jarvan_iv", "TOP"): (["Jayce", "Kennen", "Teemo"], ["Darius", "Fiora", "Jax"], ["Galio", "Yasuo", "Ahri"]),
    # jax
    ("jax", "TOP"): (["Camille", "Fiora", "Irelia"], ["Malphite", "Garen", "Gragas"], ["Sejuani", "Jarvan IV", "Lee Sin"]),
    ("jax", "JUNGLE"): (["Maestro Yi", "Shyvana", "Xin Zhao"], ["Lee Sin", "Olaf", "Warwick"], ["Galio", "Orianna", "Ahri"]),
    # jayce
    ("jayce", "TOP"): (["Darius", "Garen", "Sett"], ["Irelia", "Malphite", "Wukong"], ["Sejuani", "Jarvan IV", "Lee Sin"]),
    ("jayce", "MID"): (["Veigar", "Lux", "Twisted Fate"], ["Zed", "Yasuo", "Akali"], ["Jarvan IV", "Vi", "Nautilus"]),
    # jhin
    ("jhin", "ADC"): (["Vayne", "Kai'Sa", "Ezreal"], ["Draven", "Tristana", "Lucian"], ["Morgana", "Leona", "Nautilus"]),
    # jinx
    ("jinx", "ADC"): (["Vayne", "Kai'Sa", "Aphelios"], ["Draven", "Lucian", "Tristana"], ["Thresh", "Lulu", "Nautilus"]),
    # k_sante
    ("k_sante", "TOP"): (["Sion", "Malphite", "Dr. Mundo"], ["Fiora", "Gwen", "Darius"], ["Lee Sin", "Jarvan IV", "Orianna"]),
    # kai_sa
    ("kai_sa", "ADC"): (["Vayne", "Ezreal", "Twitch"], ["Draven", "Caitlyn", "Lucian"], ["Nautilus", "Leona", "Alistar"]),
    # kalista
    ("kalista", "ADC"): (["Ezreal", "Jhin", "Sivir"], ["Ashe", "Draven", "Caitlyn"], ["Thresh", "Nautilus", "Blitzcrank"]),
    # karma
    ("karma", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Leona"], ["Ezreal", "Lucian", "Caitlyn"]),
    ("karma", "MID"): (["Yasuo", "Katarina", "Akali"], ["Syndra", "Orianna", "Lux"], ["Jarvan IV", "Vi", "Hecarim"]),
    # kassadin
    ("kassadin", "MID"): (["Ahri", "Katarina", "Ekko"], ["Zed", "Talon", "Lucian"], ["Jarvan IV", "Amumu", "Vi"]),
    # katarina
    ("katarina", "MID"): (["Veigar", "Lux", "Ziggs"], ["Galio", "Kassadin", "Pantheon"], ["Amumu", "Malphite", "Diana"]),
    # kayle
    ("kayle", "TOP"): (["Singed", "Garen", "Sion"], ["Irelia", "Jax", "Renekton"], ["Sejuani", "Jarvan IV", "Shen"]),
    ("kayle", "MID"): (["Veigar", "Twisted Fate", "Galio"], ["Zed", "Fizz", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    # kayn
    ("kayn", "JUNGLE"): (["Shyvana", "Amumu", "Maestro Yi"], ["Lee Sin", "Xin Zhao", "Graves"], ["Galio", "Nautilus", "Shen"]),
    # kennen
    ("kennen", "TOP"): (["Darius", "Garen", "Sett"], ["Irelia", "Malphite", "Jayce"], ["Amumu", "Jarvan IV", "Diana"]),
    ("kennen", "MID"): (["Yasuo", "Katarina", "Zed"], ["Syndra", "Orianna", "Lux"], ["Amumu", "Jarvan IV", "Diana"]),
    # kha_zix
    ("kha_zix", "JUNGLE"): (["Maestro Yi", "Evelynn", "Shyvana"], ["Lee Sin", "Rammus", "Warwick"], ["Galio", "Nautilus", "Shen"]),
    # kindred
    ("kindred", "JUNGLE"): (["Amumu", "Shyvana", "Rammus"], ["Lee Sin", "Kha'Zix", "Xin Zhao"], ["Galio", "Bardo", "Shen"]),
    ("kindred", "ADC"): (["Vayne", "Kai'Sa", "Ezreal"], ["Draven", "Caitlyn", "Lucian"], ["Bardo", "Nautilus", "Leona"]),
    # kog_maw
    ("kog_maw", "ADC"): (["Vayne", "Ezreal", "Sivir"], ["Draven", "Lucian", "Tristana"], ["Lulu", "Milio", "Janna"]),
    ("kog_maw", "MID"): (["Veigar", "Twisted Fate", "Aurelion Sol"], ["Zed", "Fizz", "Talon"], ["Jarvan IV", "Amumu", "Vi"]),
    # lee_sin
    ("lee_sin", "JUNGLE"): (["Maestro Yi", "Shyvana", "Kha'Zix"], ["Olaf", "Warwick", "Rammus"], ["Yasuo", "Ahri", "Galio"]),
    # leona
    ("leona", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Morgana", "Janna", "Thresh"], ["Samira", "Kai'Sa", "Miss Fortune"]),
    # lillia
    ("lillia", "JUNGLE"): (["Sion", "Cho'Gath", "Amumu"], ["Kha'Zix", "Lee Sin", "Rengar"], ["Yone", "Yasuo", "Galio"]),
    ("lillia", "TOP"): (["Darius", "Garen", "Sion"], ["Irelia", "Fiora", "Camille"], ["Yone", "Yasuo", "Jarvan IV"]),
    # lissandra
    ("lissandra", "MID"): (["Zed", "Yasuo", "Katarina"], ["Syndra", "Orianna", "Lux"], ["Jarvan IV", "Vi", "Amumu"]),
    # lucian
    ("lucian", "ADC"): (["Vayne", "Kai'Sa", "Ezreal"], ["Draven", "Caitlyn", "Varus"], ["Nami", "Braum", "Leona"]),
    ("lucian", "MID"): (["Kassadin", "Aurelion Sol", "Veigar"], ["Zed", "Yasuo", "Akali"], ["Jarvan IV", "Vi", "Lee Sin"]),
    # lulu
    ("lulu", "SUPPORT"): (["Braum", "Alistar", "Leona"], ["Blitzcrank", "Nautilus", "Pyke"], ["Jinx", "Kog'Maw", "Vayne"]),
    # lux
    ("lux", "MID"): (["Veigar", "Ziggs", "Annie"], ["Zed", "Fizz", "Yasuo"], ["Jarvan IV", "Vi", "Nautilus"]),
    ("lux", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Caitlyn", "Jhin", "Ezreal"]),
    # malphite
    ("malphite", "TOP"): (["Tryndamere", "Jax", "Fiora"], ["Gwen", "Mordekaiser", "Dr. Mundo"], ["Yasuo", "Orianna", "Miss Fortune"]),
    ("malphite", "MID"): (["Zed", "Talon", "Yasuo"], ["Kassadin", "Galio", "Vladimir"], ["Yasuo", "Diana", "Miss Fortune"]),
    ("malphite", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Morgana", "Janna", "Braum"], ["Yasuo", "Miss Fortune", "Samira"]),
    # maokai
    ("maokai", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Morgana", "Janna", "Thresh"], ["Samira", "Kai'Sa", "Jinx"]),
    ("maokai", "TOP"): (["Sion", "Malphite", "Dr. Mundo"], ["Fiora", "Gwen", "Darius"], ["Lee Sin", "Jarvan IV", "Orianna"]),
    ("maokai", "JUNGLE"): (["Maestro Yi", "Shyvana", "Kha'Zix"], ["Olaf", "Lee Sin", "Xin Zhao"], ["Galio", "Ahri", "Yasuo"]),
    # master_yi
    ("master_yi", "JUNGLE"): (["Shyvana", "Amumu", "Dr. Mundo"], ["Rammus", "Lee Sin", "Xin Zhao"], ["Lulu", "Yuumi", "Morgana"]),
    # mel
    ("mel", "MID"): (["Veigar", "Lux", "Ziggs"], ["Zed", "Fizz", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    ("mel", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Jhin", "Caitlyn", "Varus"]),
    # milio
    ("milio", "SUPPORT"): (["Leona", "Nautilus", "Thresh"], ["Blitzcrank", "Pyke", "Zyra"], ["Jinx", "Kog'Maw", "Caitlyn"]),
    # miss_fortune
    ("miss_fortune", "ADC"): (["Vayne", "Kai'Sa", "Sivir"], ["Draven", "Tristana", "Lucian"], ["Amumu", "Leona", "Nautilus"]),
    ("miss_fortune", "SUPPORT"): (["Zyra", "Brand", "Sona"], ["Blitzcrank", "Nautilus", "Leona"], ["Jhin", "Ashe", "Varus"]),
    # mordekaiser
    ("mordekaiser", "TOP"): (["Sion", "Malphite", "Garen"], ["Fiora", "Vayne", "Olaf"], ["Jarvan IV", "Sejuani", "Vi"]),
    ("mordekaiser", "JUNGLE"): (["Amumu", "Shyvana", "Maestro Yi"], ["Lee Sin", "Olaf", "Warwick"], ["Galio", "Nautilus", "Ahri"]),
    # morgana
    ("morgana", "SUPPORT"): (["Blitzcrank", "Nautilus", "Thresh"], ["Sona", "Soraka", "Zyra"], ["Caitlyn", "Jhin", "Samira"]),
    ("morgana", "MID"): (["Veigar", "Twisted Fate", "Aurelion Sol"], ["Zed", "Yasuo", "Akali"], ["Jarvan IV", "Vi", "Lee Sin"]),
    ("morgana", "JUNGLE"): (["Amumu", "Rammus", "Shyvana"], ["Kha'Zix", "Lee Sin", "Xin Zhao"], ["Galio", "Nautilus", "Ahri"]),
    # nami
    ("nami", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Lucian", "Samira", "Jhin"]),
    # nasus
    ("nasus", "TOP"): (["Kayle", "Teemo", "Singed"], ["Darius", "Fiora", "Gwen"], ["Sejuani", "Jarvan IV", "Vi"]),
    # nautilus
    ("nautilus", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Morgana", "Janna", "Braum"], ["Samira", "Kai'Sa", "Yasuo"]),
    ("nautilus", "TOP"): (["Sion", "Malphite", "Cho'Gath"], ["Fiora", "Gwen", "Darius"], ["Yasuo", "Orianna", "Lee Sin"]),
    ("nautilus", "JUNGLE"): (["Maestro Yi", "Shyvana", "Evelynn"], ["Olaf", "Lee Sin", "Warwick"], ["Yasuo", "Ahri", "Galio"]),
    # nidalee
    ("nidalee", "JUNGLE"): (["Maestro Yi", "Shyvana", "Amumu"], ["Lee Sin", "Kha'Zix", "Xin Zhao"], ["Renekton", "Nautilus", "Galio"]),
    # nilah
    ("nilah", "ADC"): (["Miss Fortune", "Jhin", "Ezreal"], ["Caitlyn", "Draven", "Ashe"], ["Sona", "Yuumi", "Nami"]),
    ("nilah", "JUNGLE"): (["Amumu", "Shyvana", "Maestro Yi"], ["Lee Sin", "Xin Zhao", "Kha'Zix"], ["Galio", "Orianna", "Taric"]),
    # nocturne
    ("nocturne", "JUNGLE"): (["Maestro Yi", "Shyvana", "Evelynn"], ["Olaf", "Warwick", "Lee Sin"], ["Galio", "Twisted Fate", "Shen"]),
    # norra
    ("norra", "MID"): (["Veigar", "Lux", "Ziggs"], ["Zed", "Fizz", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    ("norra", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Jhin", "Caitlyn", "Ezreal"]),
    # nunu_willump
    ("nunu_willump", "JUNGLE"): (["Amumu", "Shyvana", "Maestro Yi"], ["Olaf", "Lee Sin", "Xin Zhao"], ["Katarina", "Kennen", "Orianna"]),
    # olaf
    ("olaf", "TOP"): (["Darius", "Aatrox", "Mordekaiser"], ["Fiora", "Vayne", "Camille"], ["Sejuani", "Jarvan IV", "Vi"]),
    ("olaf", "JUNGLE"): (["Amumu", "Rammus", "Sejuani"], ["Lee Sin", "Kha'Zix", "Graves"], ["Galio", "Yuumi", "Lulu"]),
    # orianna
    ("orianna", "MID"): (["Annie", "Veigar", "Twisted Fate"], ["Zed", "Fizz", "Yasuo"], ["Jarvan IV", "Malphite", "Diana"]),
    # ornn
    ("ornn", "TOP"): (["Sion", "Malphite", "Dr. Mundo"], ["Fiora", "Gwen", "Vayne"], ["Yasuo", "Miss Fortune", "Orianna"]),
    # pantheon
    ("pantheon", "TOP"): (["Teemo", "Jayce", "Kennen"], ["Malphite", "Shen", "Poppy"], ["Twisted Fate", "Lee Sin", "Taliyah"]),
    ("pantheon", "MID"): (["Katarina", "Kassadin", "Fizz"], ["Galio", "Sett", "Swain"], ["Twisted Fate", "Vi", "Taliyah"]),
    ("pantheon", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Nautilus", "Leona", "Braum"], ["Draven", "Samira", "Kai'Sa"]),
    ("pantheon", "JUNGLE"): (["Maestro Yi", "Shyvana", "Evelynn"], ["Olaf", "Lee Sin", "Xin Zhao"], ["Galio", "Twisted Fate", "Ahri"]),
    # poppy
    ("poppy", "TOP"): (["Riven", "Irelia", "Camille"], ["Darius", "Sett", "Mordekaiser"], ["Sejuani", "Jarvan IV", "Vi"]),
    ("poppy", "JUNGLE"): (["Kha'Zix", "Lee Sin", "Rengar"], ["Olaf", "Warwick", "Xin Zhao"], ["Galio", "Ahri", "Nautilus"]),
    ("poppy", "SUPPORT"): (["Rakan", "Leona", "Alistar"], ["Morgana", "Janna", "Zyra"], ["Vayne", "Jhin", "Samira"]),
    # pyke
    ("pyke", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Nautilus", "Leona", "Morgana"], ["Draven", "Samira", "Lucian"]),
    ("pyke", "MID"): (["Lux", "Veigar", "Twisted Fate"], ["Galio", "Pantheon", "Kassadin"], ["Vi", "Jarvan IV", "Diana"]),
    # rakan
    ("rakan", "SUPPORT"): (["Sona", "Soraka", "Braum"], ["Morgana", "Leona", "Thresh"], ["Xayah", "Samira", "Yasuo"]),
    # rammus
    ("rammus", "JUNGLE"): (["Maestro Yi", "Xin Zhao", "Graves"], ["Olaf", "Gwen", "Lillia"], ["Galio", "Ahri", "Orianna"]),
    # rell
    ("rell", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Janna", "Morgana", "Thresh"], ["Samira", "Miss Fortune", "Kai'Sa"]),
    ("rell", "JUNGLE"): (["Maestro Yi", "Shyvana", "Amumu"], ["Olaf", "Lee Sin", "Kha'Zix"], ["Miss Fortune", "Katarina", "Galio"]),
    # renekton
    ("renekton", "TOP"): (["Riven", "Irelia", "Yasuo"], ["Fiora", "Garen", "Darius"], ["Nidalee", "Taliyah", "Lee Sin"]),
    # rengar
    ("rengar", "JUNGLE"): (["Maestro Yi", "Evelynn", "Shyvana"], ["Lee Sin", "Rammus", "Warwick"], ["Shen", "Galio", "Orianna"]),
    ("rengar", "TOP"): (["Teemo", "Kayle", "Jayce"], ["Darius", "Sett", "Shen"], ["Sejuani", "Lee Sin", "Vi"]),
    # riven
    ("riven", "TOP"): (["Aatrox", "Yasuo", "Irelia"], ["Renekton", "Garen", "Poppy"], ["Sejuani", "Jarvan IV", "Lee Sin"]),
    # rumble
    ("rumble", "TOP"): (["Sion", "Malphite", "Dr. Mundo"], ["Darius", "Fiora", "Irelia"], ["Jarvan IV", "Amumu", "Diana"]),
    ("rumble", "MID"): (["Yasuo", "Katarina", "Zed"], ["Kassadin", "Galio", "Syndra"], ["Jarvan IV", "Amumu", "Diana"]),
    ("rumble", "JUNGLE"): (["Amumu", "Rammus", "Shyvana"], ["Kha'Zix", "Lee Sin", "Xin Zhao"], ["Galio", "Nautilus", "Orianna"]),
    # ryze
    ("ryze", "MID"): (["Twisted Fate", "Annie", "Veigar"], ["Zed", "Fizz", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    ("ryze", "TOP"): (["Darius", "Garen", "Sion"], ["Irelia", "Camille", "Jax"], ["Sejuani", "Lee Sin", "Shen"]),
    # samira
    ("samira", "ADC"): (["Miss Fortune", "Jhin", "Ashe"], ["Caitlyn", "Draven", "Tristana"], ["Nautilus", "Leona", "Rakan"]),
    # senna
    ("senna", "ADC"): (["Vayne", "Kai'Sa", "Ezreal"], ["Draven", "Tristana", "Lucian"], ["Nautilus", "Thresh", "Leona"]),
    ("senna", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Lucian", "Jhin", "Ashe"]),
    # seraphine
    ("seraphine", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Sona", "Miss Fortune", "Ashe"]),
    ("seraphine", "MID"): (["Veigar", "Lux", "Ziggs"], ["Zed", "Fizz", "Akali"], ["Jarvan IV", "Amumu", "Vi"]),
    ("seraphine", "ADC"): (["Vayne", "Kai'Sa", "Ezreal"], ["Draven", "Lucian", "Tristana"], ["Sona", "Karma", "Nautilus"]),
    # sett
    ("sett", "TOP"): (["Sion", "Garen", "Irelia"], ["Fiora", "Vayne", "Renekton"], ["Sejuani", "Jarvan IV", "Vi"]),
    ("sett", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Morgana", "Janna", "Thresh"], ["Samira", "Kai'Sa", "Draven"]),
    # shen
    ("shen", "TOP"): (["Jax", "Irelia", "Fiora"], ["Darius", "Mordekaiser", "Gwen"], ["Twitch", "Evelynn", "Nocturne"]),
    ("shen", "SUPPORT"): (["Sona", "Soraka", "Yuumi"], ["Morgana", "Thresh", "Zyra"], ["Samira", "Kai'Sa", "Jinx"]),
    # shyvana
    ("shyvana", "JUNGLE"): (["Maestro Yi", "Amumu", "Rammus"], ["Lee Sin", "Xin Zhao", "Olaf"], ["Orianna", "Galio", "Yuumi"]),
    # singed
    ("singed", "TOP"): (["Garen", "Jax", "Shen"], ["Teemo", "Vayne", "Fiora"], ["Yuumi", "Hecarim", "Sejuani"]),
    # sion
    ("sion", "TOP"): (["Malphite", "Cho'Gath", "Shen"], ["Fiora", "Gwen", "Darius"], ["Yasuo", "Orianna", "Jarvan IV"]),
    # sivir
    ("sivir", "ADC"): (["Caitlyn", "Jhin", "Ashe"], ["Draven", "Lucian", "Tristana"], ["Yuumi", "Lulu", "Leona"]),
    # skarner
    ("skarner", "JUNGLE"): (["Maestro Yi", "Shyvana", "Evelynn"], ["Olaf", "Lee Sin", "Xin Zhao"], ["Galio", "Ahri", "Orianna"]),
    ("skarner", "TOP"): (["Sion", "Malphite", "Dr. Mundo"], ["Fiora", "Gwen", "Darius"], ["Lee Sin", "Jarvan IV", "Orianna"]),
    # smolder
    ("smolder", "ADC"): (["Vayne", "Ezreal", "Sivir"], ["Draven", "Caitlyn", "Tristana"], ["Thresh", "Nautilus", "Leona"]),
    ("smolder", "MID"): (["Veigar", "Twisted Fate", "Aurelion Sol"], ["Zed", "Fizz", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    # sona
    ("sona", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Leona"], ["Seraphine", "Ezreal", "Jhin"]),
    # soraka
    ("soraka", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Jinx", "Vayne", "Caitlyn"]),
    # swain
    ("swain", "MID"): (["Katarina", "Fizz", "Akali"], ["Syndra", "Orianna", "Lux"], ["Jarvan IV", "Amumu", "Diana"]),
    ("swain", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Jhin", "Ashe", "Miss Fortune"]),
    ("swain", "TOP"): (["Sion", "Garen", "Darius"], ["Fiora", "Irelia", "Gwen"], ["Sejuani", "Lee Sin", "Vi"]),
    # syndra
    ("syndra", "MID"): (["Annie", "Veigar", "Twisted Fate"], ["Fizz", "Zed", "Katarina"], ["Jarvan IV", "Vi", "Amumu"]),
    # taliyah
    ("taliyah", "JUNGLE"): (["Maestro Yi", "Shyvana", "Amumu"], ["Lee Sin", "Kha'Zix", "Xin Zhao"], ["Pantheon", "Renekton", "Nautilus"]),
    ("taliyah", "MID"): (["Twisted Fate", "Aurelion Sol", "Veigar"], ["Zed", "Fizz", "Akali"], ["Pantheon", "Jarvan IV", "Vi"]),
    # talon
    ("talon", "JUNGLE"): (["Maestro Yi", "Shyvana", "Evelynn"], ["Lee Sin", "Xin Zhao", "Warwick"], ["Galio", "Nautilus", "Shen"]),
    ("talon", "MID"): (["Lux", "Veigar", "Twisted Fate"], ["Galio", "Pantheon", "Kassadin"], ["Jarvan IV", "Vi", "Diana"]),
    # teemo
    ("teemo", "TOP"): (["Darius", "Garen", "Nasus"], ["Irelia", "Malphite", "Jayce"], ["Sejuani", "Amumu", "Vi"]),
    ("teemo", "MID"): (["Twisted Fate", "Veigar", "Aurelion Sol"], ["Zed", "Fizz", "Syndra"], ["Jarvan IV", "Vi", "Amumu"]),
    # thresh
    ("thresh", "SUPPORT"): (["Leona", "Braum", "Rakan"], ["Morgana", "Zyra", "Brand"], ["Jinx", "Samira", "Draven"]),
    # tristana
    ("tristana", "ADC"): (["Vayne", "Kai'Sa", "Ezreal"], ["Draven", "Caitlyn", "Lucian"], ["Leona", "Nautilus", "Lulu"]),
    ("tristana", "MID"): (["Kassadin", "Aurelion Sol", "Veigar"], ["Zed", "Yasuo", "Akali"], ["Jarvan IV", "Vi", "Lee Sin"]),
    # tryndamere
    ("tryndamere", "TOP"): (["Sion", "Dr. Mundo", "Shen"], ["Malphite", "Jax", "Teemo"], ["Sejuani", "Jarvan IV", "Vi"]),
    ("tryndamere", "JUNGLE"): (["Amumu", "Shyvana", "Maestro Yi"], ["Lee Sin", "Rammus", "Warwick"], ["Galio", "Lulu", "Yuumi"]),
    # twisted_fate
    ("twisted_fate", "MID"): (["Aurelion Sol", "Veigar", "Kassadin"], ["Zed", "Fizz", "Yasuo"], ["Pantheon", "Shen", "Camille"]),
    # twitch
    ("twitch", "ADC"): (["Vayne", "Ezreal", "Sivir"], ["Draven", "Caitlyn", "Lucian"], ["Yuumi", "Lulu", "Shen"]),
    ("twitch", "JUNGLE"): (["Amumu", "Shyvana", "Maestro Yi"], ["Lee Sin", "Kha'Zix", "Xin Zhao"], ["Shen", "Galio", "Yuumi"]),
    # urgot
    ("urgot", "TOP"): (["Sion", "Garen", "Darius"], ["Fiora", "Vayne", "Aatrox"], ["Sejuani", "Jarvan IV", "Vi"]),
    # varus
    ("varus", "ADC"): (["Vayne", "Kai'Sa", "Samira"], ["Draven", "Tristana", "Lucian"], ["Thresh", "Nautilus", "Leona"]),
    ("varus", "MID"): (["Veigar", "Lux", "Twisted Fate"], ["Zed", "Yasuo", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    # vayne
    ("vayne", "ADC"): (["Ezreal", "Sivir", "Aphelios"], ["Draven", "Caitlyn", "Tristana"], ["Lulu", "Janna", "Milio"]),
    ("vayne", "TOP"): (["Darius", "Garen", "Sion"], ["Malphite", "Irelia", "Teemo"], ["Sejuani", "Jarvan IV", "Shen"]),
    # veigar
    ("veigar", "MID"): (["Twisted Fate", "Annie", "Aurelion Sol"], ["Zed", "Fizz", "Katarina"], ["Jarvan IV", "Vi", "Amumu"]),
    ("veigar", "ADC"): (["Vayne", "Kai'Sa", "Ezreal"], ["Draven", "Lucian", "Tristana"], ["Nautilus", "Leona", "Thresh"]),
    # vel_koz
    ("vel_koz", "MID"): (["Veigar", "Annie", "Twisted Fate"], ["Zed", "Fizz", "Yasuo"], ["Jarvan IV", "Vi", "Amumu"]),
    ("vel_koz", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Jhin", "Varus", "Caitlyn"]),
    # vex
    ("vex", "MID"): (["Yasuo", "Katarina", "Akali"], ["Syndra", "Orianna", "Lux"], ["Jarvan IV", "Vi", "Amumu"]),
    # vi
    ("vi", "JUNGLE"): (["Maestro Yi", "Shyvana", "Kha'Zix"], ["Olaf", "Warwick", "Lee Sin"], ["Yasuo", "Ahri", "Galio"]),
    # viego
    ("viego", "JUNGLE"): (["Shyvana", "Amumu", "Maestro Yi"], ["Lee Sin", "Xin Zhao", "Kha'Zix"], ["Galio", "Nautilus", "Ahri"]),
    ("viego", "MID"): (["Veigar", "Twisted Fate", "Kassadin"], ["Zed", "Yasuo", "Akali"], ["Jarvan IV", "Vi", "Diana"]),
    # viktor
    ("viktor", "MID"): (["Annie", "Veigar", "Twisted Fate"], ["Zed", "Fizz", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    # vladimir
    ("vladimir", "MID"): (["Veigar", "Twisted Fate", "Lux"], ["Kassadin", "Galio", "Orianna"], ["Jarvan IV", "Amumu", "Diana"]),
    ("vladimir", "TOP"): (["Sion", "Garen", "Shen"], ["Riven", "Irelia", "Aatrox"], ["Sejuani", "Lee Sin", "Vi"]),
    # volibear
    ("volibear", "TOP"): (["Sion", "Garen", "Riven"], ["Fiora", "Vayne", "Jax"], ["Sejuani", "Jarvan IV", "Vi"]),
    ("volibear", "JUNGLE"): (["Maestro Yi", "Shyvana", "Amumu"], ["Olaf", "Warwick", "Lee Sin"], ["Galio", "Ahri", "Nautilus"]),
    # warwick
    ("warwick", "TOP"): (["Sion", "Garen", "Darius"], ["Fiora", "Vayne", "Teemo"], ["Sejuani", "Jarvan IV", "Vi"]),
    ("warwick", "JUNGLE"): (["Maestro Yi", "Shyvana", "Kha'Zix"], ["Olaf", "Rammus", "Lee Sin"], ["Galio", "Ahri", "Shen"]),
    # wukong
    ("wukong", "TOP"): (["Jayce", "Kennen", "Teemo"], ["Darius", "Garen", "Sett"], ["Yasuo", "Orianna", "Lee Sin"]),
    ("wukong", "JUNGLE"): (["Maestro Yi", "Shyvana", "Evelynn"], ["Olaf", "Lee Sin", "Xin Zhao"], ["Yasuo", "Ahri", "Galio"]),
    # xayah
    ("xayah", "ADC"): (["Samira", "Kai'Sa", "Vayne"], ["Caitlyn", "Varus", "Draven"], ["Rakan", "Nautilus", "Leona"]),
    # xin_zhao
    ("xin_zhao", "JUNGLE"): (["Maestro Yi", "Shyvana", "Kha'Zix"], ["Olaf", "Warwick", "Rammus"], ["Yasuo", "Ahri", "Galio"]),
    ("xin_zhao", "TOP"): (["Irelia", "Yasuo", "Riven"], ["Jax", "Darius", "Malphite"], ["Sejuani", "Jarvan IV", "Lee Sin"]),
    # yasuo
    ("yasuo", "MID"): (["Lux", "Ziggs", "Twisted Fate"], ["Renekton", "Sett", "Pantheon"], ["Malphite", "Diana", "Gragas"]),
    ("yasuo", "TOP"): (["Sion", "Gnar", "Dr. Mundo"], ["Renekton", "Darius", "Sett"], ["Malphite", "Gragas", "Lee Sin"]),
    ("yasuo", "ADC"): (["Miss Fortune", "Jhin", "Ashe"], ["Draven", "Caitlyn", "Vayne"], ["Gragas", "Nautilus", "Alistar"]),
    # yone
    ("yone", "MID"): (["Veigar", "Lux", "Aurelion Sol"], ["Renekton", "Sett", "Pantheon"], ["Malphite", "Lillia", "Diana"]),
    ("yone", "TOP"): (["Sion", "Cho'Gath", "Dr. Mundo"], ["Renekton", "Jax", "Fiora"], ["Sejuani", "Jarvan IV", "Lee Sin"]),
    # yunara
    ("yunara", "ADC"): (["Vayne", "Kai'Sa", "Samira"], ["Caitlyn", "Draven", "Varus"], ["Thresh", "Lulu", "Braum"]),
    ("yunara", "MID"): (["Veigar", "Lux", "Twisted Fate"], ["Zed", "Yasuo", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    # yuumi
    ("yuumi", "SUPPORT"): (["Sona", "Soraka", "Janna"], ["Blitzcrank", "Nautilus", "Leona"], ["Zeri", "Twitch", "Jinx"]),
    # zed
    ("zed", "MID"): (["Lux", "Veigar", "Ziggs"], ["Malphite", "Lissandra", "Galio"], ["Vi", "Jarvan IV", "Diana"]),
    ("zed", "JUNGLE"): (["Maestro Yi", "Shyvana", "Evelynn"], ["Lee Sin", "Rammus", "Warwick"], ["Galio", "Nautilus", "Shen"]),
    # zeri
    ("zeri", "ADC"): (["Vayne", "Kai'Sa", "Ashe"], ["Draven", "Tristana", "Caitlyn"], ["Yuumi", "Lulu", "Janna"]),
    # ziggs
    ("ziggs", "MID"): (["Veigar", "Annie", "Twisted Fate"], ["Zed", "Fizz", "Yasuo"], ["Jarvan IV", "Vi", "Amumu"]),
    ("ziggs", "ADC"): (["Vayne", "Kai'Sa", "Jinx"], ["Draven", "Lucian", "Tristana"], ["Nautilus", "Leona", "Thresh"]),
    # zilean
    ("zilean", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Hecarim", "Darius", "Jinx"]),
    ("zilean", "MID"): (["Veigar", "Twisted Fate", "Aurelion Sol"], ["Zed", "Fizz", "Akali"], ["Hecarim", "Jarvan IV", "Vi"]),
    # zoe
    ("zoe", "MID"): (["Lux", "Veigar", "Twisted Fate"], ["Zed", "Fizz", "Yasuo"], ["Jarvan IV", "Vi", "Nautilus"]),
    # zyra
    ("zyra", "SUPPORT"): (["Braum", "Alistar", "Thresh"], ["Blitzcrank", "Nautilus", "Pyke"], ["Jhin", "Ashe", "Miss Fortune"]),
    ("zyra", "MID"): (["Veigar", "Twisted Fate", "Aurelion Sol"], ["Zed", "Fizz", "Akali"], ["Jarvan IV", "Vi", "Amumu"]),
    ("zyra", "JUNGLE"): (["Amumu", "Rammus", "Shyvana"], ["Kha'Zix", "Lee Sin", "Xin Zhao"], ["Galio", "Nautilus", "Ahri"])
}

print(f"Total curated entries: {len(curated)}")

# Verify all champions in curated exist
all_names = set(c['name'] for c in all_champs)
for (cid, role), (advs, cnts, syns) in curated.items():
    if cid not in by_id:
        print(f"Error: Unknown champion id {cid}")
    for a in advs:
        if a not in all_names:
            print(f"Error: Unknown adv {a} for {cid}")
    for c in cnts:
        if c not in all_names:
            print(f"Error: Unknown cnt {c} for {cid}")
    for s in syns:
        if s not in all_names:
            print(f"Error: Unknown syn {s} for {cid}")

# Generate Kotlin file: ChampionRoleMatchupAdvisor.kt
kt_code = '''package com.example.util

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
'''

for (cid, r_str), (advs, cnts, syns) in sorted(curated.items()):
    r_enum = f"LaneRole.{r_str}"
    adv_str = ', '.join([f'"{x}"' for x in advs])
    cnt_str = ', '.join([f'"{x}"' for x in cnts])
    syn_str = ', '.join([f'"{x}"' for x in syns])
    kt_code += f'        MatchupKey("{cid}", {r_enum}) to MatchupRoleResult(\n'
    kt_code += f'            advantages = listOf({adv_str}),\n'
    kt_code += f'            counters = listOf({cnt_str}),\n'
    kt_code += f'            synergies = listOf({syn_str})\n'
    kt_code += f'        ),\n'

kt_code += '''    )

    fun getMatchups(champion: Champion, role: LaneRole): MatchupRoleResult {
        val key = MatchupKey(champion.id.lowercase().trim(), role)
        val found = matchupDatabase[key]
        if (found != null) {
            return found
        }

        // Try primary role if flex role specific is missing
        val primaryKey = MatchupKey(champion.id.lowercase().trim(), champion.primaryRole)
        val primaryMatch = matchupDatabase[primaryKey]
        if (primaryMatch != null && role == champion.primaryRole) {
            return primaryMatch
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
'''

with open('app/src/main/java/com/example/util/ChampionRoleMatchupAdvisor.kt', 'w', encoding='utf-8') as f:
    f.write(kt_code)

print("Generated ChampionRoleMatchupAdvisor.kt successfully!")

# Also update champions_part1.json and champions_part2.json for primary role
for c in c1:
    cid = c['id']
    prole = c['primaryRole']
    key = (cid, prole)
    if key in curated:
        advs, cnts, syns = curated[key]
        c['advantageAgainst'] = advs
        c['counteredBy'] = cnts
        c['synergies'] = syns

for c in c2:
    cid = c['id']
    prole = c['primaryRole']
    key = (cid, prole)
    if key in curated:
        advs, cnts, syns = curated[key]
        c['advantageAgainst'] = advs
        c['counteredBy'] = cnts
        c['synergies'] = syns

with open('app/src/main/res/raw/champions_part1.json', 'w', encoding='utf-8') as f:
    json.dump(c1, f, indent=2, ensure_ascii=False)

with open('app/src/main/res/raw/champions_part2.json', 'w', encoding='utf-8') as f:
    json.dump(c2, f, indent=2, ensure_ascii=False)

print("Updated champions_part1.json and champions_part2.json successfully!")
