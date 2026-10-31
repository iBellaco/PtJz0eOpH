"""Import the supplied artwork once; --check validates offline packaging without network access."""
import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
import io
import json
from pathlib import Path
import time
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / 'tools/component-item-icons.json'
ASSETS = ROOT / 'app/src/main/assets'


def check(entries):
    catalog = json.loads((ASSETS / 'wild_rift_component_items.json').read_text())
    identities = {item['id'] for section in catalog['secciones'].values()
                  for items in section.values() for item in items}
    assert set(entries) == identities, 'Every component must have exactly one local icon'
    assert len({entry['asset'] for entry in entries.values()}) == len(entries)
    total = 0
    for identity, entry in entries.items():
        assert entry['asset'] == f'component_icons/{identity}.webp'
        content = (ASSETS / entry['asset']).read_bytes()
        assert content[:4] == b'RIFF' and content[8:12] == b'WEBP', identity
        assert hashlib.sha256(content).hexdigest() == entry['sha256'], identity
        assert len(content) == entry['bytes'] and len(content) <= 16000, identity
        assert max(entry['width'], entry['height']) <= 128, identity
        total += len(content)
    assert total < 512000, 'Component icon budget exceeded'
    print(f'COMPONENT_ICONS: {len(entries)} local WebP icons, {total} bytes; shared across sections')


def download(pair):
    from PIL import Image, ImageOps
    identity, entry = pair
    request = urllib.request.Request(entry['source'], headers={'User-Agent': 'Coach-Asset-Importer/1.0'})
    for attempt in range(3):
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                content = response.read(4 * 1024 * 1024 + 1)
            assert len(content) <= 4 * 1024 * 1024, identity
            break
        except (OSError, TimeoutError):
            if attempt == 2:
                raise
            time.sleep(1 + attempt)
    image = ImageOps.exif_transpose(Image.open(io.BytesIO(content))).convert('RGBA')
    image.thumbnail((128, 128), Image.Resampling.LANCZOS)
    target = ASSETS / entry['asset']
    target.parent.mkdir(parents=True, exist_ok=True)
    image.save(target, format='WEBP', quality=85, method=6)
    encoded = target.read_bytes()
    return identity, {**entry, 'sha256': hashlib.sha256(encoded).hexdigest(),
                      'width': image.width, 'height': image.height,
                      'source_bytes': len(content), 'bytes': len(encoded)}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--download', action='store_true', help='Fetch supplied source URLs and optimize the artwork')
    parser.add_argument('--check', action='store_true', help='Validate local icons; no downloads')
    args = parser.parse_args()
    entries = json.loads(MANIFEST.read_text())
    if args.download:
        with ThreadPoolExecutor(max_workers=4) as pool:
            entries = dict(pool.map(download, entries.items()))
        MANIFEST.write_text(json.dumps(entries, ensure_ascii=False, indent=2) + '\n')
    check(entries)
