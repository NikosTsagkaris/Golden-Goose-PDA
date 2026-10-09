import sys
import os

target_file = '/home/ntvelop/GoldenGooseServer/print_worker.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update Constants for larger sizes
content = content.replace("SIZE_2X = b'\\x1d\\x21\\x11'", "SIZE_2X = b'\\x1d\\x21\\x11' # Double Width + Double Height")
content = content.replace("SIZE_NORMAL = b'\\x1d\\x21\\x00'", "SIZE_NORMAL = b'\\x1d\\x21\\x00'\nSIZE_3X = b'\\x1d\\x21\\x22' # Triple Width + Triple Height\nSIZE_4X = b'\\x1d\\x21\\x33' # Quad Width + Quad Height")

# 2. Update format_escpos to use Larger Fonts by default
old_format = """    for line in content.split('\\n'):
        if not line.strip() and not line.startswith('['):
            result += b'\\n'
            continue
            
        if line.startswith('[LB]'):
            text = line[4:]
            result += SIZE_2X + BOLD_ON + encode_greek(text) + b'\\n' + SIZE_NORMAL + BOLD_OFF
        elif line.startswith('[B]'):
            text = line[3:]
            result += BOLD_ON + encode_greek(text) + b'\\n' + BOLD_OFF
        elif line.startswith('[CUT]'):
            result += FEED + CUT
        else:
            result += encode_greek(line) + b'\\n'"""

new_format = """    for line in content.split('\\n'):
        if not line.strip() and not line.startswith('['):
            result += b'\\n'
            continue
            
        if line.startswith('[LB]'):
            # Massive Headers (Triple Size)
            text = line[4:]
            result += SIZE_3X + BOLD_ON + encode_greek(text) + b'\\n' + SIZE_NORMAL + BOLD_OFF
        elif line.startswith('[B]'):
            # Large Section Headers (Double Size)
            text = line[3:]
            result += SIZE_2X + BOLD_ON + encode_greek(text) + b'\\n' + SIZE_NORMAL + BOLD_OFF
        elif line.startswith('[CUT]'):
            result += FEED + CUT
        else:
            # Body text slightly larger if possible (Double width or just 2x height)
            # For 80mm, we want to fill the horizontal space.
            result += SIZE_2X + encode_greek(line) + b'\\n' + SIZE_NORMAL"""

if old_format in content:
    content = content.replace(old_format, new_format)

# 3. Ensure Auto-Cut is always at the end
if 'result += FEED + CUT' not in content:
    content = content.replace('return result', 'result += FEED + CUT\n    return result')

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Printer worker optimized for large text and autocut.")
