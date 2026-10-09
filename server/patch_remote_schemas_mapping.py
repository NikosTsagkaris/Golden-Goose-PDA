import os

schemas_path = "/home/ntvelop/brikipos/server/app/schemas.py"

with open(schemas_path, "r") as f:
    content = f.read()

# 1. Update Product schema with Field alias
if "from pydantic import BaseModel" in content and "Field" not in content:
    content = content.replace("from pydantic import BaseModel", "from pydantic import BaseModel, Field")

# Update Product class to use alias
old_product = """class Product(ProductBase):
    id: int
    option_group: int = -1"""

new_product = """class Product(ProductBase):
    id: int
    option_group: int = Field(-1, alias="option_group_code")

    class Config:
        from_attributes = True
        populate_by_name = True"""

if old_product in content:
    content = content.replace(old_product, new_product)

with open(schemas_path, "w") as f:
    f.write(content)

print("Updated schemas.py with mapping successfully.")
