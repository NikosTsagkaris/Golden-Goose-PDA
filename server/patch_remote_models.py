import os

models_path = "/home/ntvelop/brikipos/server/app/models.py"

with open(models_path, "r") as f:
    content = f.read()

# 1. Add option_group_code to Product
if "option_group_code = Column(Integer, default=-1)" not in content:
    product_def = """class Product(Base):
    __tablename__ = "products"
    id = Column(Integer, primary_key=True)
    category_id = Column(Integer, ForeignKey("categories.id"))
    name = Column(String)
    price = Column(Float)"""
    
    replacement = product_def + "\n    option_group_code = Column(Integer, default=-1)"
    content = content.replace(product_def, replacement)

# 2. Add OptionGroup and Option models at the end
if "class OptionGroup(Base):" not in content:
    content += """

class OptionGroup(Base):
    __tablename__ = "option_groups"
    id = Column(Integer, primary_key=True)
    name = Column(String)
    group_code = Column(Integer, unique=True)
    is_active = Column(Boolean, default=True)
    
    options = relationship("Option", back_populates="group")

class Option(Base):
    __tablename__ = "options"
    id = Column(Integer, primary_key=True)
    group_id = Column(Integer, ForeignKey("option_groups.id"))
    name = Column(String)
    price_delta_cents = Column(Integer, default=0)
    is_active = Column(Boolean, default=True)
    sort_order = Column(Integer, default=0)
    
    group = relationship("OptionGroup", back_populates="options")
"""

with open(models_path, "w") as f:
    f.write(content)

print("Updated models.py successfully.")
