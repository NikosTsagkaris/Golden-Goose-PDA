from sqlalchemy import create_engine, text
import uuid

db_url = "postgresql+psycopg2://ntvelop_app:2105135381@localhost:5432/ntvelop_db"
engine = create_engine(db_url)

categories = [
    {"name": "ΣΑΜΠΛ - Καφέδες", "sort_order": -10, "items": [
        {"name": "Espresso", "price": 150},
        {"name": "Cappuccino", "price": 250},
        {"name": "Freddo Espresso", "price": 200},
    ]},
    {"name": "ΣΑΜΠΛ - Αναψυκτικά", "sort_order": -9, "items": [
        {"name": "Coca Cola", "price": 180},
        {"name": "Water", "price": 50},
    ]},
    {"name": "ΣΑΜΠΛ - Φαγητό", "sort_order": -8, "items": [
        {"name": "Club Sandwich", "price": 550},
        {"name": "Toast", "price": 200},
    ]}
]

try:
    with engine.connect() as conn:
        for cat in categories:
            cat_id = str(uuid.uuid4())
            conn.execute(text("INSERT INTO catalog_categories (id, name, sort_order, is_active) VALUES (:id, :name, :so, true)"),
                         {"id": cat_id, "name": cat["name"], "so": cat["sort_order"]})
            for item in cat["items"]:
                conn.execute(text("INSERT INTO catalog_items (id, category_id, name, base_price_cents, is_active, sort_order) VALUES (gen_random_uuid(), :cid, :name, :price, true, 0)"),
                             {"cid": cat_id, "name": item["name"], "price": item["price"]})
        conn.commit()
        print("Sample data seeded successfully!")
except Exception as e:
    print(f"Error seeding data: {e}")
