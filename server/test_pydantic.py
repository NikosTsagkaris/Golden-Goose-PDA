from pydantic import BaseModel, ValidationError
from typing import List, Optional
import json

class AddLineRequest(BaseModel):
    item_id: Optional[str] = ""
    product_name: str
    quantity: int
    unit_price: float
    options_text: Optional[str] = ""
    options_json: Optional[dict] = {}
    options_price: float = 0.0
    note: Optional[str] = ""
    is_plastic_cup: bool = False
    selected_options: List[str] = []

# Sample data mimicking the App's request
sample_data = {
    "item_id": "test-id",
    "product_name": "Test Item",
    "quantity": 1,
    "unit_price": 1.5,
    "options_text": "Option A, Option B",
    "options_price": 0.5,
    "note": "A test note"
    # options_json is implicitly emptyMap() -> omitted or {}
}

try:
    req = AddLineRequest(**sample_data)
    print("Validation successful!")
except ValidationError as e:
    print("Validation failed:")
    print(e.json())

# Test with null note
sample_data_null_note = sample_data.copy()
sample_data_null_note["note"] = None

try:
    req = AddLineRequest(**sample_data_null_note)
    print("Validation with null note successful!")
except ValidationError as e:
    print("Validation with null note failed:")
    print(e.json())
