from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

DATABASE_URL = "postgresql+psycopg2://ntvelop_app:2105135381@localhost:5432/ntvelop_db"
engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(bind=engine)
db = SessionLocal()

try:
    print("--- Listing Users in app_users ---")
    q = text("SELECT id, username, role, is_active FROM app_users")
    users = db.execute(q).mappings().all()
    for u in users:
        print(f"User: {u['username']}, Role: {u['role']}, Active: {u['is_active']}, ID: {u['id']}")
    if not users:
        print("No users found in app_users.")

except Exception as e:
    print(f"Error: {e}")
finally:
    db.close()
