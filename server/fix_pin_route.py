
import os

MAIN_FILE = "/home/ntvelop/GoldenGooseServer/app/main.py"

try:
    with open(MAIN_FILE, "r") as f:
        content = f.read()
    
    # Simple replace
    new_content = content.replace('@app.post("/auth/pin",', '@app.post("/auth/login",')
    
    with open(MAIN_FILE, "w") as f:
        f.write(new_content)
        
    print("Successfully patched main.py to use /auth/login.")

except Exception as e:
    print(f"Error patching main.py: {e}")
