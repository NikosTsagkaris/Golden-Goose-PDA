from app import database, models, crud, schemas
from sqlalchemy.orm import Session

def add_missing_tables():
    db = database.SessionLocal()
    try:
        # Check existing tables
        existing_numbers = [t.number for t in db.query(models.Table).all()]
        print(f"Existing tables: {existing_numbers}")
        
        for i in range(1, 21):
            if i not in existing_numbers:
                print(f"Adding table {i}...")
                t = schemas.TableCreate(
                    number=i,
                    area="Main" if i <= 10 else "Garden"
                )
                crud.create_table(db, t)
        
        db.commit()
        print("Done adding tables.")
    except Exception as e:
        print(f"Error: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    add_missing_tables()
