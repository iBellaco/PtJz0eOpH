#!/usr/bin/env python3
"""Refresh the 142 champions / 300 lane builds from the user-maintained item catalog."""
import argparse, json, re
from build_coaching import PROFILES, overview, element
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app/src/main/assets'
RAW = ROOT / 'app/src/main/res/raw'
LANES = {'TOP':'Línea de Barón','JUNGLE':'Jungla','MID':'Línea Central','ADC':'Línea de Dragón','SUPPORT':'Soporte'}
PT_LANES = {'TOP':'Rota do Barão (Topo)','JUNGLE':'Selva','MID':'Rota do Meio','ADC':'Rota do Dragão (Duo)','SUPPORT':'Suporte'}
# Champion-specific corrections prompted by the updated effects/statistics, not a universal template.
OVERRIDES = {
 'aatrox':['Eclipse','Cuchilla negra','Baile de la muerte'],
 'akali':['Orbe infinito','Creagrietas','Sombrero mortal de Rabadon'],
 'ekko':['Perdición del liche','Orbe infinito','Sombrero mortal de Rabadon'],
 'evelynn':['Orbe infinito','Perdición del liche','Sombrero mortal de Rabadon'],
 'fizz':['Perdición del liche','Orbe infinito','Sombrero mortal de Rabadon'],
 'jhin':['Filo Infinito','Recaudadora','Cañón de Fuego Rápido'],
 'caitlyn':['Filo Infinito','Cañón de Fuego Rápido','Recuerdos de Lord Dominik'],
 'jinx':['Flechas de los Yun Tal','Huracán de Runaan','Filo Infinito'],
 'lucian':['Segador de esencia','Filoveloz de Navori','Filo Infinito'],
 'xayah':['Flechas de los Yun Tal','Filoveloz de Navori','Filo Infinito'],
 'yunara':['Flechas de los Yun Tal','Huracán de Runaan','Filo Infinito'],
 'zeri':['Flechas de los Yun Tal','Huracán de Runaan','Filo Infinito'],
 'tristana':['Flechas de los Yun Tal','Filoveloz de Navori','Filo Infinito'],
 'sivir':['Flechas de los Yun Tal','Filoveloz de Navori','Filo Infinito'],
 'kalista':['Hoja del rey arruinado','Huracán de Runaan','Verdugo de Krakens'],
 'kindred':['Hoja del rey arruinado','Verdugo de Krakens','El final'],
 'amumu':['Tormento de Liandry','Égida de fuego solar','Coraza dual purpúrea'],
 'malphite':['Guantelete de hielo','Égida de fuego solar','Coraza dual purpúrea'],
}

def read(path): return json.loads(path.read_text())
def output(path, value, check):
    if path.name == 'translations_pt.json': value=dict(sorted(value.items()))
    text=json.dumps(value,ensure_ascii=False,indent=2)+'\n'
    if check:
        assert path.read_text()==text, f'Outdated generated data: {path.relative_to(ROOT)}'
    else: path.write_text(text)

def main(check=False):
    source=(ROOT/'app/src/main/java/com/example/data/WildRiftItemsData.kt').read_text()
    items={}
    for block in source.split('WildRiftItem(')[1:]:
        fields={key:json.loads('"'+value+'"') for key,value in re.findall(r'(\w+)\s*=\s*"((?:\\.|[^"\\])*)"',block.split('\n        ),')[0])}
        if 'name' in fields: items[fields['name']]=fields
    builds=read(ASSETS/'champions_creator_builds.json')
    paths=[RAW/f'champions_part{i}.json' for i in (1,2)]
    parts=[read(p) for p in paths]; champions={c['id']:c for part in parts for c in part}
    assert len(champions)==142 and len(builds)==300
    assert set(PROFILES)==set(champions), 'Every champion needs an authored coaching profile'
    translations=read(ASSETS/'translations_pt.json')
    phrases=read(ASSETS/'build_catalog_translations_pt.json')
    # Remove the previous generated advice before replacing it; never leave stale bilingual variants.
    for key in list(translations):
        if any(key.startswith('Diagnóstico del error/situación: '+c['name']+' · ') for c in champions.values()):
            del translations[key]
    by_lane={}
    rune_templates={r['runeName']:dict(r) for b in builds for r in b['coreRunes']}
    for build in builds:
        champ=champions[build['championId']]
        lane=next(k for k,v in LANES.items() if build['role'].split(' (')[0]==v)
        # Support income and flex identity take precedence over damage builds.
        if champ['id'] in OVERRIDES and lane!='SUPPORT':
            build['coreItems']=OVERRIDES[champ['id']][:]
        core=build['coreItems']
        situational=[i for i in build['situationalItems'] if i not in core]
        # Move displaced core items to conditional alternatives, retaining the user's options.
        old=next((b for b in champ.get('builds',[]) if b['role'].upper()==lane),{})
        alternatives=old.get('coreItems',[])+champ.get('situationalItems',[])
        for name in alternatives:
            if len(situational)>=4: break
            if name not in core+ situational and name in items: situational.append(name)
        build['situationalItems']=situational
        build['creatorName']='Coach (Criterio Táctico)'
        def localized(name):
            item=items.get(name,{})
            return phrases.get(name,item.get('namePt') or translations.get(name,name))
        def advice(name,kind):
            es=element(champ,LANES[lane],name,kind)
            pt=element(champ,PT_LANES[lane],name,kind,True,localized(name))
            if es: translations[es]=pt
            return es
        def entry(name,situational=False):
            assert name in items,(champ['id'],name)
            return {'itemName':name,'description':advice(name,'item')}
        build['coachAdvice']=overview(champ,lane,LANES[lane],translations)
        translations[build['coachAdvice']]=overview(champ,lane,PT_LANES[lane],translations,True)
        role_pt=PT_LANES[lane]+(' (Flex)' if '(Flex)' in build['role'] else '')
        translations[f"Línea: {build['role']} • Análisis del coach"]=f"Rota: {role_pt} • Análise do coach"
        if champ['id']=='syndra':
            build['coreRunes']=[dict(rune_templates['Golpe Bajo']) if r['runeName']=='Impacto Repentino' else r for r in build['coreRunes']]
        for field,kind,key in [('coreRunes','rune','runeName'),('situationalRunes','rune','runeName'),('coreSpells','spell','spellName'),('situationalSpells','spell','spellName')]:
            for choice in build[field]: choice['description']=advice(choice[key],kind)
        build['runes']=', '.join(r['runeName'] for r in build['coreRunes'])
        build['coreItemsWithDesc']=[entry(n) for n in core]
        build['situationalItemsWithDesc']=[entry(n,True) for n in situational]
        for key in ['bootsT2Item','bootsT3Item']:
            if build.get(key): build[key]=entry(build[key]['itemName'])
        # Conditional boot reasons and valid rune/spell substitution groups remain authoritative.
        by_lane[(champ['id'],lane)]=build
    for champ in champions.values():
        for lane in [champ['primaryRole']]+champ['secondaryRoles']:
            build=by_lane[(champ['id'],lane)]
            raw=next((b for b in champ.get('builds',[]) if b['role'].upper()==lane),None)
            assert raw is not None,(champ['id'],lane)
            raw.update(coreItems=build['coreItems'],items=build['coreItems'],situationalItems=build['situationalItems'],runes=build['runes'])
        primary=by_lane[(champ['id'],champ['primaryRole'])]
        champ['coreItems']=primary['coreItems']; champ['situationalItems']=primary['situationalItems']
        champ['coreItemsIcons']=[items[n].get('iconUrl','') for n in champ['coreItems']]
        champ['situationalItemsIcons']=[items[n].get('iconUrl','') for n in champ['situationalItems']]
    translations.update({'CRITERIO COACH':'CRITÉRIO COACH','Análisis del coach':'Análise do coach','Criterio del coach':'Critério do coach','Coach (Criterio Táctico)':'Coach (Critério Tático)'})
    translations['Criterio del coach • Coach (Criterio Táctico)']='Critério do coach • Coach (Critério Tático)'
    for lane, label in LANES.items():
        translations[f'Línea: {label} • Análisis del coach']=f'Rota: {PT_LANES[lane]} • Análise do coach'
    output(ASSETS/'champions_creator_builds.json',builds,check)
    output(ASSETS/'translations_pt.json',translations,check)
    for path,part in zip(paths,parts): output(path,part,check)
    print('BUILD_REFRESH: 142 champions, 300 lane builds; catalog-backed effects and bilingual champion-specific advice')

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');main(parser.parse_args().check)
