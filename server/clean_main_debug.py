
import os

MAIN_FILE = "/home/ntvelop/GoldenGooseServer/app/main.py"

try:
    with open(MAIN_FILE, "r") as f:
        lines = f.readlines()
    
    new_lines = []
    for line in lines:
        if "DEBUG LOGIN:" in line:
            continue
        new_lines.append(line)

    with open(MAIN_FILE, "w") as f:
        f.writelines(new_lines)
        
    print("Successfully removed debug prints from main.py.")

except Exception as e:
    print(f"Error cleaning main.py: {e}")
