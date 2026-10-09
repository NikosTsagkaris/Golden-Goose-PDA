import sys
import os

target_file = '/home/ntvelop/GoldenGooseServer/print_worker.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Improve CUT and FEED constants
content = content.replace("CUT = b'\\x1d\\x56\\x01'", "CUT_S = b'\\x1d\\x56\\x42\\x00' # Partial cut with feed (robust)")
content = content.replace("FEED = b'\\n\\n\\n\\n'", "FEED = b'\\n' * 6 # 6 lines feed")

# 2. Update the end of format_escpos
if 'result += FEED + CUT_S' not in content:
    # Remove old manual return if we added it previously
    content = content.replace('result += FEED + CUT\n    return result', 'return result')
    # Add robust feed + cut sequence
    content = content.replace('return result', 'result += FEED + CUT_S\n    return result')

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Print worker autocut refined.")
