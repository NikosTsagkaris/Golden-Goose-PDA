import os
import bcrypt
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

# Use database URL from env or hardcoded since we know it
# postgresql+psycopg2://ntvelop_app:2105135381@localhost:5432/ntvelop_db
DATABASE_URL = "postgresql+psycopg2://ntvelop_app:2105135381@localhost:5432/ntvelop_db"

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(bind=engine)
db = SessionLocal()

def reset_password(username, new_pin):
    print(f"Updating PIN for {username}...")
    try:
        # Check if user exists first
        check_q = text("SELECT id FROM app_users WHERE username = :u")
        res = db.execute(check_q, {"u": username}).mappings().first()
        if not res:
            print(f"User {username} not found.")
            return

        # Hash using bcrypt
        password_bytes = new_pin.encode('utf-8')
        # Generate salt and hash
        hashed = bcrypt.hashpw(password_bytes, bcrypt.gensalt(rounds=6))
        # Decode to string for storage
        hashed_str = hashed.decode('utf-8')
        
        query = text("UPDATE app_users SET pin_hash = :h WHERE username = :u")
        db.execute(query, {"h": hashed_str, "u": username})
        db.commit()
        print(f"PIN for {username} updated successfully to {new_pin}")
    except Exception as e:
        print(f"Error updating {username}: {e}")
        db.rollback()

if __name__ == "__main__":
    reset_password("waiter", "0000")
    reset_password("manager", "1234")
    # Also try admin just in case
    reset_password("admin", "1234")
    db.close()
