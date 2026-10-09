from app import database, models, crud, schemas
from app.database import engine

def init_data():
    db = database.SessionLocal()
    
    # Check if we have users
    if not db.query(models.User).first():
        print("Creating initial users...")
        # Manager
        mgr = schemas.UserCreate(
            username="manager",
            password="123", # In real life, hash this
            pin="1234",
            role="MANAGER",
            full_name="Admin Manager"
        )
        crud.create_user(db, mgr)
        
        # Waiter
        waiter = schemas.UserCreate(
            username="waiter",
            password="123",
            pin="5555",
            role="WAITER",
            full_name="John Doe"
        )
        crud.create_user(db, waiter)
    
    # Check if we have tables
    if not db.query(models.Table).first():
        print("Creating tables...")
        for i in range(1, 11):
            t = schemas.TableCreate(
                number=i,
                area="Main" if i < 6 else "Garden"
            )
            crud.create_table(db, t)
            
    db.close()
    print("Initialization Complete.")

if __name__ == "__main__":
    # Ensure tables exist
    models.Base.metadata.create_all(bind=engine)
    init_data()
