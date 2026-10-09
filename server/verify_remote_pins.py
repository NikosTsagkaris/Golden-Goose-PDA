from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
import sys
import os

sys.path.append(os.getcwd())

try:
    from app import models
    engine = create_engine('postgresql://pi:raspberry@localhost/briki_pos')
    Session = sessionmaker(bind=engine)
    session = Session()

    print("--- User Info ---")
    users = session.query(models.User).all()
    for u in users:
        print(f"User: '{u.username}', Role: '{u.role}', PIN: '{u.pin}', ID: {u.id}")
    
    session.close()
except Exception as e:
    print(f"Error: {e}")
    sys.exit(1)
