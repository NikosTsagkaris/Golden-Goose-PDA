from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
import sys
import os

# Append the current directory to path
sys.path.append(os.getcwd())

try:
    from app import models
    # The database URL from .env
    # postgresql+psycopg2://ntvelop_app:2105135381@localhost:5432/ntvelop_db
    engine = create_engine('postgresql+psycopg2://ntvelop_app:2105135381@localhost:5432/ntvelop_db')
    Session = sessionmaker(bind=engine)
    session = Session()

    print("--- Database Update (ntvelop_db) ---")
    users = session.query(models.User).all()
    print(f"Total users found: {len(users)}")
    for u in users:
        print(f"User: '{u.username}', Role: '{u.role}', PIN: '{u.pin}'")

    waiter = session.query(models.User).filter_by(username='waiter').first()
    if waiter:
        print(f"Updating waiter pin from '{waiter.pin}' to '0000'")
        waiter.pin = '0000'
    else:
        print("Waiter user not found!")

    manager_users = session.query(models.User).filter((models.User.username.in_(['manager', 'admin'])) | (models.User.role == 'MANAGER')).all()
    if manager_users:
        for m in manager_users:
            print(f"Updating {m.username} (Role: {m.role}) pin from '{m.pin}' to '1234'")
            m.pin = '1234'
    else:
        print("Manager/Admin user not found!")

    session.commit()
    print("PINs updated successfully")
except Exception as e:
    print(f"Error: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
finally:
    if 'session' in locals():
        session.close()
