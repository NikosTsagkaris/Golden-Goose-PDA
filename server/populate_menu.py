from app import database, models, crud, schemas
from sqlalchemy.orm import Session

MENU_DATA = {
    "ΚΑΦΕΣ": [
        ("Espresso μονός", 2.0),
        ("Espresso διπλός", 3.5),
        ("Espresso διπλός macchiato", 3.5),
        ("Freddo Espresso", 3.5),
        ("Freddo Espresso XL", 5.0),
        ("Cappuccino μονός", 3.0),
        ("Cappuccino διπλός", 3.8),
        ("Freddo Cappuccino", 3.5),
        ("Freddo Cappuccino XL", 5.0),
        ("Americano μονός", 3.0),
        ("Americano διπλός", 3.5),
        ("Iced Latte", 3.5),
        ("Latte", 3.5),
        ("Ελληνικός διπλός", 3.2),
        ("Frappé", 3.2),
        ("Ζεστός Nescafe", 3.2),
    ],
    "ΡΟΦΗΜΑΤΑ": [
        ("Σοκολάτα Κλασσική", 3.8),
        ("Σοκολάτα Λευκή", 4.5),
        ("Σοκολάτα Red Velvet", 4.5),
        ("Σοκολάτα Choco n’ Oreo", 4.5),
        ("Lipton Ice Tea 350ml", 3.0),
        ("Φυσικός χυμός πορτοκάλι", 3.8),
    ],
    "ΑΝΑΨΥΚΤΙΚΑ": [
        ("Coca Cola", 3.0),
        ("Πορτοκαλάδα", 3.0),
        ("Λεμονίτα", 3.0),
        ("Sprite", 3.0),
        ("Σόδα", 3.0),
        ("Τόνικ", 3.0),
        ("Red Bull", 4.5),
        ("Νερό", 0.5),
        ("Ανθρακούχο νερό", 3.5),
    ],
    "SMOOTHIES": [
        ("Red Rum", 5.0),
        ("Pinky Blenders", 5.0),
        ("Ginger Purple", 5.0),
    ],
    "ΑΛΚΟΟΛ": [
        ("Μπύρα", 4.0),
        ("Κρασί ποτήρι", 4.0),
        ("Απλό ποτό", 6.0),
        ("Special ποτό", 7.5),
        ("Ούζο ποτήρι", 3.5),
        ("Σφηνάκι", 3.0),
        ("Cocktail Μπρίκι", 8.0),
    ],
    "YAMAS": [
        ("Blueberry", 4.0),
        ("Mint", 4.0),
        ("Mango", 4.0),
        ("Strawberry", 4.0),
        ("Peach", 4.0),
    ],
    "HOTLY": [
        ("Mango with passion fruit", 4.0),
        ("Red peach with passion fruit", 4.0),
        ("Pineapple with yuzu", 4.0),
        ("Morello cherry with spices", 4.0),
        ("Raspberry with spearmint", 4.0),
        ("Blackcurrant with basil", 4.0),
        ("Strawberry with bergamot", 4.0),
        ("Hippophae with ginger", 4.0),
    ],
    "CICADA": [
        ("Λάιμ", 4.0),
        ("Φράουλα με λεμόνι", 4.0),
        ("Μανταρίνι", 4.0),
        ("Ροδάκινο", 4.0),
        ("Ανανάς με μάνγκο", 4.0),
        ("Κόκκινα φρούτα", 4.0),
    ],
    "Teabox": [
        ("Το Δάκρυ του Ολύμπου 01", 2.8),
        ("Η Πράσινη Κοιλάδα 02", 2.8),
        ("Ο Ήρεμος Ήλιος 03", 2.8),
        ("Πρωινό στο Λονδίνο 04", 2.8),
        ("Καθημερινή Αποτοξίνωση 05", 2.8),
        ("Η Απαγορευμένη Ουσία 06", 2.8),
        ("Παραμυθένια Γαία 07", 2.8),
        ("Ο Φρέσκος Αέρας 08", 2.8),
        ("Το Κίτρινο Δέντρο 09", 2.8),
        ("Δρόμος της Ανατολής 10", 2.8),
        ("Ο Θησαυρός της Υγείας 11", 2.8),
        ("Οι Καρποί των Ίνκας 12", 2.8),
    ],
    "ΦΑΓΗΤΟ": [
        ("Τοστ απλό", 3.0),
        ("Τοστ Special", 4.0),
        ("Τορτίγια κοτόπουλο", 5.0),
        ("Club sandwich ζαμπόν/γαλοπούλα", 5.5),
        ("Club sandwich κοτόπουλο", 6.0),
        ("Αραβική πίτα κοτόπουλο φιλέτο", 4.5),
        ("Αραβική πίτα γαλοπούλα καπνιστή", 4.5),
        ("Μπαγκέτα λευκή με γαλοπούλα καπνιστή", 4.5),
        ("Μπαγκέτα βιαννέζικη", 4.5),
        ("Τσιαπάτα με κοτομπουκιά πανέ", 5.0),
        ("Hamburger", 5.0),
        ("Chicken Stripes με Πατάτες", 6.8),
        ("Caesar’s", 6.0),
        ("Cheesecake", 4.5),
        ("Προφιτερόλ", 4.5),
        ("Εκμέκ Κανταϊφι", 4.5),
        ("Bites Κορμού", 4.5),
    ]
}

def create_category(db: Session, name: str, display_order: int):
    db_cat = models.Category(name=name, display_order=display_order)
    db.add(db_cat)
    db.commit()
    db.refresh(db_cat)
    return db_cat

def create_product(db: Session, name: str, price: float, category_id: int):
    db_prod = models.Product(name=name, price=price, category_id=category_id)
    db.add(db_prod)
    db.commit()
    db.refresh(db_prod)
    return db_prod

def populate_menu():
    db = database.SessionLocal()
    try:
        # Clear existing to avoid dups if re-running
        db.query(models.Product).delete()
        db.query(models.Category).delete()
        db.commit()

        for idx, (cat_name, products) in enumerate(MENU_DATA.items()):
            db_cat = create_category(db, cat_name, idx)
            for p_name, p_price in products:
                create_product(db, p_name, p_price, db_cat.id)
        print("Menu populated successfully!")
    except Exception as e:
        print(f"Error: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    populate_menu()
