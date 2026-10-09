import os
import bcrypt
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker

DATABASE_URL = "postgresql+psycopg2://ntvelop_app:2105135381@localhost:5432/ntvelop_db"

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(bind=engine)
db = SessionLocal()

def update_user(old_username, new_username, new_pin):
    print(f"Processing update for {old_username} -> {new_username} with PIN {new_pin}...")
    try:
        # Check if new_username already exists
        check_q = text("SELECT id FROM app_users WHERE username = :u")
        existing = db.execute(check_q, {"u": new_username}).mappings().first()
        
        target_username = new_username
        
        if not existing:
            # Check if old_username exists to rename
            old_existing = db.execute(check_q, {"u": old_username}).mappings().first()
            if old_existing:
                print(f"Renaming {old_username} to {new_username}...")
                rename_q = text("UPDATE app_users SET username = :new WHERE username = :old")
                db.execute(rename_q, {"new": new_username, "old": old_username})
                # Now target is new_username
            else:
                print(f"Neither {new_username} nor {old_username} found. Skipping.")
                return
        else:
            print(f"User {new_username} already exists. Updating PIN.")

        # Hash PIN
        password_bytes = new_pin.encode('utf-8')
        hashed = bcrypt.hashpw(password_bytes, bcrypt.gensalt(rounds=6))
        hashed_str = hashed.decode('utf-8')
        
        # Update PIN
        query = text("UPDATE app_users SET pin_hash = :h WHERE username = :u")
        db.execute(query, {"h": hashed_str, "u": target_username})
        db.commit()
        print(f"Successfully updated {target_username} with PIN {new_pin}.")
        
    except Exception as e:
        print(f"Error updating: {e}")
        db.rollback()

if __name__ == "__main__":
    update_user("waiter1", "waiter", "0000")
    update_user("manager1", "manager", "1234")
    db.close()
