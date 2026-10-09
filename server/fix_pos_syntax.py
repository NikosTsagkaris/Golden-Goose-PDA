import sys
import os

target_file = '/home/ntvelop/GoldenGooseServer/app/pos.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Clean up any bad previous patch attempts
new_lines = []
for line in lines:
    if 'from app.auth import require_user' in line:
        continue
    new_lines.append(line)

# Add it at the top after other imports
insert_at = 0
for i, line in enumerate(new_lines):
    if line.startswith('import ') or line.startswith('from '):
        insert_at = i + 1

new_lines.insert(insert_at, 'from app.auth import require_user\n')

with open(target_file, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("Fixed syntax error in pos.py")
