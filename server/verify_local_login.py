import sys
import os
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
import bcrypt

# Setup path to import app modules
sys.path.append(os.getcwd())

# Database connection
DATABASE_URL = "postgresql+psycopg2://ntvelop_app:2105135381@localhost:5432/ntvelop_db"
engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(bind=engine)
db = SessionLocal()

def verify_pin(plain_pin, hashed_pin):
    try:
        return bcrypt.checkpw(plain_pin.encode('utf-8'), hashed_pin.encode('utf-8'))
    except Exception as e:
        print(f"Bcrypt error: {e}")
        return False

def test_login(username, pin):
    print(f"\nTesting login for user: {username} with PIN: {pin}")
    
    # 获取用户 from DB
    q = text("SELECT id, username, pin_hash FROM app_users WHERE username = :u")
    try:
        user = db.execute(q, {"u": username}).mappings().first()
        
        if not user:
            print(f"User {username} NOT FOUND in database.")
            return

        print(f"User found: {user['username']}")
        print(f"Stored Hash: {user['pin_hash']}")
        
        # Verify
        is_valid = verify_pin(pin, user['pin_hash'])
        
        if is_valid:
            print("✅ PIN VERIFIED SUCCESSFULLY!")
        else:
            print("❌ PIN VERIFICATION FAILED!")
            
            # Debug: Try generating a new hash and compare visually
            new_hash = bcrypt.hashpw(pin.encode('utf-8'), bcrypt.gensalt(rounds=6)).decode('utf-8')
            print(f"DEBUG: A fresh hash of '{pin}' would be: {new_hash}")
            
    except Exception as e:
        print(f"Database error: {e}")

if __name__ == "__main__":
    test_login("waiter", "0000")
    test_login("manager", "1234")
    db.close()
