import sys
import os

target_file = '/home/ntvelop/GoldenGooseServer/app/crud.py'

if not os.path.exists(target_file):
    print(f"Error: {target_file} not found")
    sys.exit(1)

with open(target_file, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update get_menu_items_by_category
old_menu_sql = "        SELECT id::text as id, name, (base_price_cents / 100.0) as price, category_id::text as category_id \n        FROM catalog_items"
new_menu_sql = "        SELECT id::text as id, name, (base_price_cents / 100.0) as price, category_id::text as category_id, option_group_code as option_group \n        FROM catalog_items"

if old_menu_sql in content:
    content = content.replace(old_menu_sql, new_menu_sql)
    print("Updated get_menu_items_by_category SQL.")

# 2. Update get_item_by_id
old_item_sql = "SELECT id::text as id, name, (base_price_cents / 100.0) as price, category_id::text as category_id FROM catalog_items WHERE id = :iid"
new_item_sql = "SELECT id::text as id, name, (base_price_cents / 100.0) as price, category_id::text as category_id, option_group_code as option_group FROM catalog_items WHERE id = :iid"

if old_item_sql in content:
    content = content.replace(old_item_sql, new_item_sql)
    print("Updated get_item_by_id SQL.")

with open(target_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Crud.py updated for option groups.")
