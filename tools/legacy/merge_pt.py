import json, re

with open("pt_translations_bulk.json", "r", encoding="utf-8") as f:
    new_map = json.load(f)

with open("app/src/main/java/com/example/util/Translator.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Find "pt" to mapOf(
pt_start = "\"pt\" to mapOf("
pos = content.find(pt_start)
if pos != -1:
    insert_pos = pos + len(pt_start)
    
    entries_code = "\n"
    count = 0
    for k, v in new_map.items():
        if k == v and not re.search(r"[a-zA-ZáéíóúÁÉÍÓÚñÑ]", k):
            continue
        ek = k.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        ev = v.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        entries_code += f"        \"{ek}\" to \"{ev}\",\n"
        count += 1
        
    new_content = content[:insert_pos] + entries_code + content[insert_pos:]
    with open("app/src/main/java/com/example/util/Translator.kt", "w", encoding="utf-8") as f:
        f.write(new_content)
    print(f"Successfully merged {count} new translation entries into Translator.kt!")
else:
    print("Error: Could not find pt map in Translator.kt!")
