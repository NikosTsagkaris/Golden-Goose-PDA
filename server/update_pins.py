from app.db import get_db
from app.auth import hash_pin
from sqlalchemy import text

db = next(get_db())

# Update PINs
db.execute(text("UPDATE app_users SET pin_hash = :pin WHERE username = 'waiter'"), {"pin": hash_pin("0000")})
db.execute(text("UPDATE app_users SET pin_hash = :pin WHERE username = 'waiter2'"), {"pin": hash_pin("5555")})
db.execute(text("UPDATE app_users SET pin_hash = :pin WHERE username = 'manager'"), {"pin": hash_pin("1234")})
db.commit()

print("✓ PINs updated successfully:")
print("  waiter: 0000")
print("  waiter2: 5555")
print("  manager: 1234")
