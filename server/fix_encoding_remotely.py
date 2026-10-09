import sqlalchemy
engine = sqlalchemy.create_engine('postgresql://pi:raspberry@localhost/goldengoose_db')
with engine.connect() as conn:
    conn.execute(sqlalchemy.text("UPDATE products SET name = 'Καφές Φίλτρου' WHERE id = 249"))
    conn.commit()
print("Successfully updated record 249 with Greek name.")
