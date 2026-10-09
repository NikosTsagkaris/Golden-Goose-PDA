import sys
import os
import re

target_file = '/home/ntvelop/GoldenGooseServer/app/pos.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update submit_order to split options into multiple lines
old_options_logic = """        try:
            opts = json.loads(l['options_json']) if isinstance(l['options_json'], str) else l['options_json']
            if opts and isinstance(opts, dict) and opts.get('text'):
                body += f"   {opts['text']}\\n"
        except: pass"""

new_options_logic = """        try:
            opts = json.loads(l['options_json']) if isinstance(l['options_json'], str) else l['options_json']
            if opts and isinstance(opts, dict) and opts.get('text'):
                # Split options by comma and put each on a new line
                opt_lines = [o.strip() for o in opts['text'].split(',') if o.strip()]
                for opt in opt_lines:
                    body += f"  - {opt}\\n"
        except: pass"""

if old_options_logic in content:
    content = content.replace(old_options_logic, new_options_logic)
    print("Updated options to multiline in submit_order.")
else:
    print("Could not find options logic in pos.py")

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Pos.py layout updated.")
