import os

schemas_path = "/home/ntvelop/brikipos/server/app/schemas.py"

with open(schemas_path, "r") as f:
    content = f.read()

# 1. Add option_group to Product schema
if "option_group: int = -1" not in content:
    product_def = """class Product(ProductBase):
    id: int"""
    replacement = product_def + "\n    option_group: int = -1"
    content = content.replace(product_def, replacement)

# 2. Add OptionResponse schema at the end
if "class OptionResponse(BaseModel):" not in content:
    content += """

class OptionResponse(BaseModel):
    id: int
    name: str
    price: float
"""

with open(schemas_path, "w") as f:
    f.write(content)

print("Updated schemas.py successfully.")
