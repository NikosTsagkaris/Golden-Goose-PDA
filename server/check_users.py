from app import database, models
from sqlalchemy.orm import Session

def check_users():
    db = database.SessionLocal()
    try:
        users = db.query(models.User).all()
        print(f"Total users found: {len(users)}")
        for u in users:
            print(f"ID: {u.id}, Username: {u.username}, PIN: {u.pin}, Role: {u.role}")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        db.close()

if __name__ == "__main__":
    check_users()
