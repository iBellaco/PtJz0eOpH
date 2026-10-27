"""Check resource completeness before spending time on Android builds/emulators."""
import argparse
from pathlib import Path
import sys
import xml.etree.ElementTree as ET


def validate_resources(root: Path) -> list[str]:
    issues = []

    def keys(directory):
        path = root / directory / 'strings.xml'
        try:
            nodes = ET.parse(path).getroot().findall('string')
        except (OSError, ET.ParseError) as error:
            issues.append(f'{directory}/strings.xml: XML inaccesible o inválido: {error}')
            return set()
        seen = set()
        for node in nodes:
            name = node.get('name')
            if not name:
                issues.append(f'{directory}/strings.xml: cadena sin nombre')
            elif name in seen:
                issues.append(f'{directory}/strings.xml: clave duplicada: {name}')
            else:
                seen.add(name)
        return seen

    default = keys('values')
    for locale in ('values-pt', 'values-pt-rBR'):
        missing = default - keys(locale)
        if missing:
            issues.append(f'{locale}/strings.xml: faltan traducciones: {", ".join(sorted(missing))}')
    return issues


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--resources', type=Path,
                        default=Path(__file__).resolve().parents[2] / 'app/src/main/res')
    args = parser.parse_args(argv)
    issues = validate_resources(args.resources)
    for issue in issues:
        print('LOCALIZED_RESOURCES_ERROR: ' + issue, file=sys.stderr)
    if issues:
        return 1
    print('LOCALIZED_RESOURCES: OK (values-pt y values-pt-rBR completos)')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
