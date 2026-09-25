with open('app/src/main/java/com/example/data/WildRiftItemsData.kt', 'r', encoding='utf-8') as f:
    text = f.read()

out = []
in_quote = False
i = 0
backslash = chr(92)
quote = chr(34)
newline = chr(10)

while i < len(text):
    c = text[i]
    if c == quote and (i == 0 or text[i-1] != backslash):
        in_quote = not in_quote
        out.append(c)
    elif c == newline and in_quote:
        out.append(backslash + 'n')
    else:
        out.append(c)
    i += 1

fixed = ''.join(out)
with open('app/src/main/java/com/example/data/WildRiftItemsData.kt', 'w', encoding='utf-8') as f:
    f.write(fixed)
print("Fixed successfully!")
