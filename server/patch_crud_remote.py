import os

file_path = "/home/ntvelop/brikipos/server/app/crud.py"
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

target = "amount=line.unit_price + line.options_price,"
replacement = "amount=(line.unit_price + line.options_price) * line.quantity,"

if target in content:
    new_content = content.replace(target, replacement)
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("Successfully patched pay_order_line.")
else:
    print("Target string not found. Possibly already patched or content differs.")
