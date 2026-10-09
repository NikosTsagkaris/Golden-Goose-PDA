import os

main_path = "/home/ntvelop/brikipos/server/app/main.py"

with open(main_path, "r") as f:
    content = f.read()

# Add the specific /pos/options endpoint that the Android app expects
if "/pos/options/{group_code}" not in content:
    content += """

@app.get("/pos/options/{group_code}", response_model=List[schemas.OptionResponse])
def get_options_pos(group_code: int, db: Session = database.SessionLocal()):
    # We open a manual session here or use Depends if we import Session and Depends
    return crud.get_options_by_code(db, group_code)
"""

# Ensure necessary imports are there
if "from sqlalchemy.orm import Session" not in content:
    content = "from sqlalchemy.orm import Session\n" + content
if "from typing import List" not in content:
    content = "from typing import List\n" + content

with open(main_path, "w") as f:
    f.write(content)

print("Updated main.py successfully.")
