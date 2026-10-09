
import os

MAIN_FILE = "/home/ntvelop/GoldenGooseServer/app/main.py"

try:
    with open(MAIN_FILE, "r") as f:
        content = f.read()
    
    # Replace the bad lines with good ones
    # The bad sequence is likely u[\"username\"] inside the f-string
    # We will just replace the specific strings we know we added
    
    # Python's read() might have interpreted the backslashes already if they were written as literals
    # let's try a robust replacement
    
    new_content = content.replace('u[\\"username\\"]', "u['username']")
    
    # Also just in case I wrote it differently
    new_content = new_content.replace('u["username"]', "u['username']")
    
    # Safety check: if the syntax error was purely the backslash, removing it helps
    # But we want to ensure we use single quotes for the key inside double quoted f-string
    
    with open(MAIN_FILE, "w") as f:
        f.write(new_content)
        
    print("Successfully patched main.py to fix syntax.")

except Exception as e:
    print(f"Error patching main.py: {e}")
