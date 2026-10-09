import os

crud_path = "/home/ntvelop/brikipos/server/app/crud.py"

with open(crud_path, "r") as f:
    content = f.read()

# 1. Update get_category_products and get_product to include option_group_code
# Since it uses SQLAlchemy models, usually it just works if the schema is updated,
# but we need to map the DB field 'option_group_code' to the schema field 'option_group'
# if the model uses Column(Integer, default=-1). 

# Let's check how many return statements we have and if they need modification
# for explicit mapping. Wait, if Product model has 'option_group_code' and 
# Product schema has 'option_group', we need to map them.

if "option_group=p.option_group_code" not in content:
    # We might need to manually map in the router or here. 
    # Usually pydantic's from_orm handles it if names match. 
    # Since I named them differently (code vs none), I should match them.
    # Actually, I'll just rename the Column in models.py to match the schema if possible,
    # or just add the mapping in the schema Config.
    pass

# 2. Add get_options_by_code
if "def get_options_by_code" not in content:
    content += """

def get_options_by_code(db: Session, group_code: int):
    group = db.query(models.OptionGroup).filter(models.OptionGroup.group_code == group_code).first()
    if not group:
        return []
    
    options = db.query(models.Option).filter(
        models.Option.group_id == group.id,
        models.Option.is_active == True
    ).order_by(models.Option.sort_order).all()
    
    return [
        {
            "id": o.id,
            "name": o.name,
            "price": o.price_delta_cents / 100.0
        } for o in options
    ]
"""

with open(crud_path, "w") as f:
    f.write(content)

print("Updated crud.py successfully.")
