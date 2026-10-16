"""Refresh published relationships only; these publishers do not become statistical feeds."""
import concurrent.futures
import html
import json
from pathlib import Path
import re
import unicodedata
import urllib.request

ROOT=Path(__file__).resolve().parents[1]
HEADERS={'User-Agent':'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36'}

def fetch(url):
    with urllib.request.urlopen(urllib.request.Request(url,headers=HEADERS),timeout=25) as response:
        return response.read().decode('utf-8',errors='replace')

def canonical(value):
    return re.sub('[^a-z0-9]','',unicodedata.normalize('NFKD',html.unescape(value)).encode('ascii','ignore').decode().lower())

if __name__=='__main__':
    champions=[]
    for path in sorted((ROOT/'app/src/main/res/raw').glob('champions_part*.json')):
        champions.extend(json.loads(path.read_text()))
    roster={c['id']:c for c in champions}
    aliases={canonical(value):c['id'] for c in champions for value in [c['id'],c['name'],c.get('ddragonId','')] if value}
    aliases.update({'nunuandwillump':'nunu_willump','nunuwillump':'nunu_willump','masteryi':'master_yi','bard':'bard'})
    def resolve(value):return aliases.get(canonical(value))
    def clean(values,own):return list(dict.fromkeys(v for x in values if (v:=resolve(x)) and v!=own))
    best_index=json.loads(re.search(r'<script id="__NEXT_DATA__"[^>]*>(.*?)</script>',fetch('https://bestbuildwr.com/champions'),re.S)[1])['props']['pageProps']
    best_slugs={resolve(c['slug']):c['slug'] for c in best_index['champions'] if resolve(c['slug'])}
    fire_urls={c['id']:c['url'] for c in json.loads((ROOT/'docs/matchup-reference-audit.json').read_text())['references']}
    counter_urls={c['id']:c['url'] for c in json.loads((ROOT/'docs/lane-matchup-reference-audit.json').read_text())['references']}
    pick_home=fetch('https://thewildpick.com/counter/')
    pick_urls={resolve(slug):'https://thewildpick.com/counter/'+slug+'/' for slug in re.findall(r'href="/counter/([^/"?#]+)/"',pick_home) if resolve(slug)}
    core_home=fetch('https://wildriftcore.com/es/champions/')
    core_slugs={resolve(slug):slug for slug in re.findall(r'href="/es/champions/([^/"?#]+)/"',core_home) if resolve(slug)}
    tasks=[]
    for own in sorted(roster):
        slug=best_slugs.get(own,own.replace('_','-'))
        tasks.extend([(own,'bestbuildwr','https://bestbuildwr.com/champions/'+slug),
            (own,'wildriftfire',fire_urls.get(own,'https://www.wildriftfire.com/guide/'+slug)),
            (own,'wildriftcore','https://wildriftcore.com/es/champions/'+core_slugs.get(own,slug)+'/'),
            (own,'thewildpick',pick_urls.get(own,'https://thewildpick.com/counter/'+slug+'/'))])
        if own in counter_urls:tasks.append((own,'wildriftcounter',counter_urls[own]))
    def collect(task):
        own,source,url=task
        result={'champion':own,'source':source,'url':url,'advantages':[],'counters':[],'synergies':[]}
        try:
            raw=fetch(url);result['status']=200
            match=re.search(r'(?:Patch|patch|Parche)\s*(\d+\.\d+[a-z]?)',html.unescape(raw))
            if match:result['patch']=match[1]
            if source=='bestbuildwr':
                page=json.loads(re.search(r'<script id="__NEXT_DATA__"[^>]*>(.*?)</script>',raw,re.S)[1])['props']['pageProps']
                result['patch']=page.get('patchVersion')
                matches=page['champion'].get('counterMatches') or {}
                result['roles']={}
                for role in ['top','jungle','mid','adc','support']:
                    row={key:clean([c.get('slug',c.get('name','')) for c in (matches.get(field) or {}).get(role,[])],own)
                        for key,field in [('advantages','strongAgainst'),('counters','weakAgainst'),('synergies','synergy')]}
                    result['roles'][role.upper()]=row
                    for key in row:result[key]+=row[key]
            elif source=='wildriftfire':
                for heading,key in [('is Countered By','counters'),('Synergizes With','synergies')]:
                    block=re.search(r'<h2>[^<]*'+heading+r'</h2>(.*?)(?=<h2|$)',raw,re.S)
                    if block:result[key]=clean(re.findall(r'href="/guide/([^"/]+)',block[1]),own)
            elif source=='wildriftcore':
                for kind,slug in re.findall(r'<a class="wall__cell wall__cell--(hard|unfav|fav|syn|skill)" href="/es/champions/([^/]+)/',raw):
                    key={'hard':'counters','unfav':'counters','fav':'advantages','syn':'synergies'}.get(kind)
                    if key:result[key]+=clean([slug],own)
            elif source=='thewildpick':
                for heading,key in [('Best [^<]* counters','counters'),('Champions [^<]* beats','advantages')]:
                    block=re.search(r'<h2[^>]*>(?:<span[^>]*></span>)?'+heading+r'[^<]*</h2>(.*?)(?=<h2|$)',raw,re.S)
                    if block:result[key]=clean(re.findall(r'class="seo-row" href="/counter/([^/]+)/',block[1]),own)
            else:
                for block in re.finditer(r'<p[^>]*>[^<]*is (Weak|Strong) Against</p>(.*?)(?=<p[^>]*>[^<]*is (?:Weak|Strong) Against</p>|<h2|$)',raw,re.S):
                    key='counters' if block[1]=='Weak' else 'advantages'
                    result[key]+=clean(re.findall(r'alt="([^"]+)"',block[2]),own)
            for key in ['advantages','counters','synergies']:result[key]=list(dict.fromkeys(result[key]))
        except Exception as failure:result['error']=str(failure)
        return result
    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as pool:
        rows=[]
        for row in pool.map(collect,tasks):
            rows.append(row)
            if len(rows)%80==0:print('Fetched',len(rows),'of',len(tasks),flush=True)
    sources=['wildriftfire','wildriftcore','bestbuildwr','wildriftcounter','thewildpick']
    summaries={source:{'pages':sum(r['source']==source for r in rows),
        'ok':sum(r['source']==source and r.get('status')==200 for r in rows),
        'withRelations':sum(r['source']==source and any(r[k] for k in ['advantages','counters','synergies']) for r in rows),
        'patches':sorted({str(r['patch']) for r in rows if r['source']==source and r.get('patch')})} for source in sources}
    output={}
    for own in sorted(roster):
        records=[r for source in sources for r in rows if r['champion']==own and r['source']==source]
        merged={key:list(dict.fromkeys(v for r in records for v in r[key])) for key in ['advantages','counters','synergies']}
        merged['roles']=next((r['roles'] for r in records if r['source']=='bestbuildwr' and 'roles' in r),{})
        output[own]=merged
    payload={'checkedAt':'2026-10-16','sources':summaries,'champions':output}
    (ROOT/'app/src/main/assets/matchup_references.json').write_text(json.dumps(payload,ensure_ascii=False,separators=(',',':')))
    (ROOT/'docs/matchup-source-refresh-1.1.10.301.json').write_text(json.dumps({'checkedAt':payload['checkedAt'],'sources':summaries,'references':rows},ensure_ascii=False,indent=2))
    print(json.dumps(summaries,indent=2),flush=True)
