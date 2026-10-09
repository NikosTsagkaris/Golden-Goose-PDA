import sys
import os

path = '/home/ntvelop/GoldenGooseServer/app/main.py'
if not os.path.exists(path):
    print(f"File not found: {path}")
    sys.exit(1)

with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
comment_mode = False

# List of functions to comment out in main.py
conflicting_functions = [
    'def open_order',
    'def api_add_line',
    'def api_get_order'
]

for line in lines:
    # Check if we should start commenting
    if any(func in line for func in conflicting_functions) and line.strip().startswith('def '):
        # We also need to comment out the decorator above it
        # This is a bit tricky with line-by-line, let's just comment the function and its @decorator
        if new_lines and new_lines[-1].strip().startswith('@app.'):
            new_lines[-1] = '# ' + new_lines[-1]
        line = '# ' + line
        comment_mode = True
    elif comment_mode:
        # Continue commenting until we see a new top-level definition or empty line followed by something else
        # Simple heuristic: comment until next @app. or next def at column 0
        if (line.strip().startswith('@app.') or line.strip().startswith('def ')) and not line.startswith(' '):
            comment_mode = False
        else:
            line = '# ' + line
    
    new_lines.append(line)

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
print("Patch applied to main.py successfully.")
