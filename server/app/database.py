from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# Default to a local postgres URL, can be overridden by env vars in real deployment
# User will need to ensure this DB exists or configure it.
SQLALCHEMY_DATABASE_URL = "postgresql://pi:raspberry@localhost/goldengoose_pos"

engine = create_engine(SQLALCHEMY_DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
