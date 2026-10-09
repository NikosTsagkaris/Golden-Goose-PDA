from app import database, crud, auth_utils, models
db = next(database.get_db())
users = db.query(models.User).all()
print(f"Found {len(users)} users")
for u in users:
    try:
        is_valid = auth_utils.verify_password("1234", u.pin)
        print(f"User: {u.username}, PinHash: {u.pin[:10]}..., Valid: {is_valid}")
    except Exception as e:
        print(f"User: {u.username}, Error: {e}")
