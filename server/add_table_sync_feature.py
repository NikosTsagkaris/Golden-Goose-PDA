
import os

CRUD_FILE = "/home/ntvelop/GoldenGooseServer/app/crud.py"
MAIN_FILE = "/home/ntvelop/GoldenGooseServer/app/main.py"

# --- Patch CRUD ---
try:
    with open(CRUD_FILE, "r") as f:
        crud_lines = f.readlines()
    
    if not any("def sync_tables" in line for line in crud_lines):
        with open(CRUD_FILE, "a") as f:
            f.write("\n\nimport uuid\n")
            f.write("def sync_tables(db, count: int):\n")
            f.write("    # Get current tables ordered by valid integer code if possible\n")
            f.write("    # We assume code is like 'T1', 'T2' or just '1', '2'\n")
            f.write("    # Let's fetch all active tables\n")
            f.write("    q = text(\"SELECT id, code, display_name FROM dining_tables WHERE is_active=true\")\n")
            f.write("    tables = db.execute(q).mappings().all()\n")
            f.write("    \n")
            f.write("    # Sort helper\n")
            f.write("    def extract_int(t):\n")
            f.write("        s = t['code']\n")
            f.write("        # Remove non-digits\n")
            f.write("        digits = ''.join(filter(str.isdigit, s))\n")
            f.write("        return int(digits) if digits else 999999\n")
            f.write("    \n")
            f.write("    # Convert to list to sort\n")
            f.write("    table_list = list(tables)\n")
            f.write("    table_list.sort(key=extract_int)\n")
            f.write("    \n")
            f.write("    current_count = len(table_list)\n")
            f.write("    \n")
            f.write("    if count > current_count:\n")
            f.write("        # Add tables\n")
            f.write("        # Determine next number. If we have 5 tables, next is 6.\n")
            f.write("        # But if we have T1, T5, count is 2. We want to add 3 tables?\n")
            f.write("        # The user wants 'count' tables TOTAL.\n")
            f.write("        # We should just append numbers starting from (max_existing_index + 1) or just current_count + 1?\n")
            f.write("        # Let's simple append: current_count + 1 to count\n")
            f.write("        start = current_count + 1\n")
            f.write("        for i in range(start, count + 1):\n")
            f.write("            new_id = str(uuid.uuid4())\n")
            f.write("            code = f'T{i}'\n")
            f.write("            name = f'Table {i}'\n")
            f.write("            # columns: id, code, display_name, area, capacity, is_active\n")
            f.write("            ins = text(\"INSERT INTO dining_tables (id, code, display_name, area, capacity, is_active) VALUES (:id, :code, :name, 'MAIN', 4, true)\")\n")
            f.write("            db.execute(ins, {'id': new_id, 'code': code, 'name': name})\n")
            f.write("            \n")
            f.write("    elif count < current_count:\n")
            f.write("        # Remove from end\n")
            f.write("        # We remove the last (current - count) tables from our sorted list\n")
            f.write("        num_to_remove = current_count - count\n")
            f.write("        to_remove = table_list[-num_to_remove:]\n")
            f.write("        for t in to_remove:\n")
            f.write("            # Hard delete? or Soft delete (is_active=false)?\n")
            f.write("            # User said 'create or remove'. Let's hard delete via is_active=false to be safe for foreign keys, or just delete.\n")
            f.write("            # Given previous migrations, maybe hard delete is fine if no foreign keys block it.\n")
            f.write("            # Let's try DELETE, if it fails due to FK, we'll swallow error? better soft delete.\n")
            f.write("            # But 'remove' implies gone. Let's try DELETE.\n")
            f.write("            try:\n")
            f.write("                db.execute(text(\"DELETE FROM dining_tables WHERE id = :id\"), {'id': str(t['id'])})\n")
            f.write("            except Exception as e:\n")
            f.write("                # Fallback to soft delete\n")
            f.write("                print(f'Count not delete {t[\"code\"]}: {e}')\n")
            f.write("                db.execute(text(\"UPDATE dining_tables SET is_active=false WHERE id = :id\"), {'id': str(t['id'])})\n")
            f.write("                \n")
            f.write("    db.commit()\n")
            f.write("    return True\n")
            
        print("Successfully added sync_tables to crud.py")
    else:
        print("sync_tables already exists in crud.py")

except Exception as e:
    print(f"Error patching crud.py: {e}")

# --- Patch Main ---
try:
    with open(MAIN_FILE, "r") as f:
        main_lines = f.readlines()
        
    if not any("/setup/tables/sync" in line for line in main_lines):
        # We need to insert the pydantic model and the endpoint
        injection = []
        injection.append("\nclass TableSyncIn(BaseModel):\n")
        injection.append("    count: int\n")
        injection.append("\n@app.post(\"/setup/tables/sync\")\n")
        injection.append("def sync_tables_endpoint(body: TableSyncIn, db: Session = Depends(get_db), user=Depends(require_user)):\n")
        injection.append("    crud.sync_tables(db, body.count)\n")
        injection.append("    return {\"status\": \"ok\", \"count\": body.count}\n")
        
        with open(MAIN_FILE, "a") as f:
            f.writelines(injection)
            
        print("Successfully added /setup/tables/sync to main.py")
    else:
        print("Endpoint already exists in main.py")

except Exception as e:
    print(f"Error patching main.py: {e}")
