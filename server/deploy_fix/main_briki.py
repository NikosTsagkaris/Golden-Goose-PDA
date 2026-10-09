from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from . import models, database
from .routers import auth, tables, orders, menu, shifts, admin

# Initialize DB (Simple auto-create for V1)
models.Base.metadata.create_all(bind=database.engine)

app = FastAPI(
    title="BrikiPOS Server",
    description="Central POS Server for Raspberry Pi",
    version="2.0.0"
)

# Allow all origins for local LAN access (Android to Pi)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router)
app.include_router(tables.router)
app.include_router(orders.router)
app.include_router(menu.router)
app.include_router(shifts.router)
app.include_router(admin.router)

@app.get("/health")
def health_check():
    return {"status": "healthy", "system": "BrikiPOS V2"}
