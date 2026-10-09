import sys
import os

path = '/home/ntvelop/GoldenGooseServer/app/pos.py'
if not os.path.exists(path):
    print(f"File not found: {path}")
    sys.exit(1)

with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
in_class = False
for line in lines:
    # 1. Update Class Definition
    if 'class AddLineRequest(BaseModel):' in line:
        in_class = True
        new_lines.append(line)
        continue
    
    if in_class:
        if 'item_id: str' in line:
            new_lines.append(line.replace('item_id: str', 'item_id: Optional[str] = ""'))
        elif 'item_name: str' in line:
            new_lines.append(line.replace('item_name: str', 'product_name: str'))
        elif 'qty: int' in line:
            new_lines.append(line.replace('qty: int', 'quantity: int'))
        elif 'base_price: float' in line:
            new_lines.append(line.replace('base_price: float', 'unit_price: float'))
        elif 'options_text:' in line:
            new_lines.append(line)
            # Add options_json after options_text if not present
            if 'options_json' not in "".join(lines[lines.index(line):lines.index(line)+5]):
                new_lines.append('    options_json: Optional[dict] = {}\n')
        elif 'class PayLinesRequest' in line:
            in_class = False
            new_lines.append(line)
        elif any(field in line for field in ['options_price', 'note', 'is_plastic_cup', 'selected_options']):
            new_lines.append(line)
        continue

    # 2. Update Function Logic
    if 'req.item_name' in line:
        line = line.replace('req.item_name', 'req.product_name')
    if 'req.qty' in line:
        line = line.replace('req.qty', 'req.quantity')
    if 'req.base_price' in line:
        line = line.replace('req.base_price', 'req.unit_price')
    
    new_lines.append(line)

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
print("Patch applied successfully.")
