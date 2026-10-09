import os

menu_path = "/home/ntvelop/brikipos/server/app/routers/menu.py"

with open(menu_path, "r") as f:
    content = f.read()

# Add the options endpoint
if "/pos/options/{group_code}" not in content:
    content += """

@router.get("/pos/options/{group_code}", response_model=List[schemas.OptionResponse])
def get_options(group_code: int, db: Session = Depends(database.get_db)):
    return crud.get_options_by_code(db, group_code)
"""

# Also ensure it can be reached via /pos/options if needed by adding a duplicate with different prefix
# or just the one above if the router's prefix is matched. 
# The Android app calls BASE_URL + "pos/options/{group_code}".
# If this router has prefix "/menu", then it becomes "/menu/pos/options/...".
# I'll add a version without prefix to the main.py or just change this prefix.

with open(menu_path, "w") as f:
    f.write(content)

print("Updated menu.py successfully.")
