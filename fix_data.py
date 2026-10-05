import re

with open('app/src/main/java/com/example/data/WildRiftItemsData.kt', 'r', encoding='utf-8') as f:
    text = f.read()

lines = text.split('\n')
new_lines = []
in_string = False
current_line = ''

for l in lines:
    if in_string:
        current_line += '\\n' + l
        quotes = re.findall(r'(?<!\\)"', l)
        if len(quotes) % 2 != 0:
            in_string = False
            new_lines.append(current_line)
            current_line = ''
    else:
        quotes = re.findall(r'(?<!\\)"', l)
        if len(quotes) % 2 != 0:
            in_string = True
            current_line = l
        else:
            new_lines.append(l)

fixed_text = '\n'.join(new_lines)

with open('app/src/main/java/com/example/data/WildRiftItemsData.kt', 'w', encoding='utf-8') as f:
    f.write(fixed_text)

print('Successfully fixed WildRiftItemsData.kt formatting')
