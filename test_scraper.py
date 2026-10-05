import urllib.request
import re

url = "https://wildrift.leagueoflegends.com/es-es/champions/zed/"
req = urllib.request.Request(url, headers={
    "User-Agent": "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Mobile Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
    "Accept-Language": "es-ES,es;q=0.9,en;q=0.8"
})
try:
    with urllib.request.urlopen(req) as resp:
        html = resp.read().decode("utf-8")
        print("HTML length:", len(html))
        matches = re.findall(r'https?://[^\s"\'<>]+\.(?:jpg|jpeg|png|webp|avif)', html, re.I)
        print("Total images found in HTML:", len(matches))
        for m in matches[:30]:
            print(" ->", m)
except Exception as e:
    print("ERROR:", e)
