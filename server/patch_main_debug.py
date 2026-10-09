
import os

MAIN_FILE = "/home/ntvelop/GoldenGooseServer/app/main.py"

try:
    with open(MAIN_FILE, "r") as f:
        lines = f.readlines()
    
    new_lines = []
    for line in lines:
        new_lines.append(line)
        if "def login_pin(body: PinLoginIn" in line:
            new_lines.append('    print(f"DEBUG LOGIN: Received PIN |{body.pin}|")\n')
            new_lines.append('    print(f"DEBUG LOGIN: Searching users...")\n')
        
        if "for u in users:" in line:
            new_lines.append('        print(f"DEBUG LOGIN: Checking user {u[\\"username\\"]}...")\n')
            
        if "if verify_pin(body.pin, u[\"pin_hash\"]):" in line:
            new_lines.append('            print(f"DEBUG LOGIN: MATCH FOUND for {u[\\"username\\"]}!")\n')

    with open(MAIN_FILE, "w") as f:
        f.writelines(new_lines)
        
    print("Successfully patched main.py with debug prints.")

except Exception as e:
    print(f"Error patching main.py: {e}")
