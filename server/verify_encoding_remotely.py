import sqlalchemy
engine = sqlalchemy.create_engine('postgresql://pi:raspberry@localhost/goldengoose_db')
with engine.connect() as conn:
    res = conn.execute(sqlalchemy.text("SELECT id, name, price FROM products WHERE id = 249 OR name = 'Special Hot Dog'"))
    for row in res:
        print(f"ID: {row[0]}, Name: {row[1]}, Price: {row[2]}")
