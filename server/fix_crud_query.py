
import os

CRUD_FILE = "/home/ntvelop/GoldenGooseServer/app/crud.py"

try:
    with open(CRUD_FILE, "r") as f:
        content = f.read()
    
    # We want to replace:
    # q = text("SELECT id, pin_hash, role FROM app_users WHERE is_active = true")
    # with
    # q = text("SELECT id, username, pin_hash, role FROM app_users WHERE is_active = true")
    
    new_content = content.replace(
        'SELECT id, pin_hash, role FROM app_users',
        'SELECT id, username, pin_hash, role FROM app_users'
    )
    
    with open(CRUD_FILE, "w") as f:
        f.write(new_content)
        
    print("Successfully patched crud.py to include username.")

except Exception as e:
    print(f"Error patching crud.py: {e}")
