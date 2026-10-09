from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from datetime import timedelta
from .. import crud, schemas, database, auth_utils # We will need a util for jwt
from fastapi.security import OAuth2PasswordRequestForm

router = APIRouter(prefix="/auth", tags=["auth"])

@router.post("/login", response_model=schemas.Token)
def login(request: schemas.LoginRequest, db: Session = Depends(database.get_db)):
    user = crud.get_user_by_pin(db, pin=request.pin)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect PIN",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    # Create JWT
    access_token = auth_utils.create_access_token(
        data={"sub": user.username, "role": user.role}
    )
    return {
        "access_token": access_token, 
        "token_type": "bearer",
        "role": user.role,
        "username": user.username,
        "user_id": user.id
    }

@router.get("/license-status/{device_id}", response_model=schemas.LicenseStatus)
def get_license_status(device_id: str, db: Session = Depends(database.get_db)):
    license_item = crud.get_license_status(db, device_id)
    if not license_item:
        return {
            "device_id": device_id,
            "is_active": False,
            "message": "Device not registered"
        }
    
    from datetime import datetime, timezone
    now = datetime.now(timezone.utc)
    is_active = license_item.is_active and (license_item.expiry_date > now)
    days_remaining = 0
    if license_item.expiry_date:
        delta = license_item.expiry_date - now
        days_remaining = max(0, delta.days)

    msg = "License active" if is_active else "License expired or inactive"
    
    return {
        "device_id": device_id,
        "is_active": is_active,
        "expiry_date": license_item.expiry_date,
        "days_remaining": days_remaining,
        "message": msg
    }

@router.post("/activate", response_model=schemas.LicenseStatus)
def activate_device(request: schemas.DeviceActivation, db: Session = Depends(database.get_db)):
    license_item = crud.activate_device(db, request.device_id, request.activation_code)
    if not license_item:
        raise HTTPException(status_code=400, detail="Invalid activation code")
    
    return {
        "device_id": license_item.device_id,
        "is_active": True,
        "expiry_date": license_item.expiry_date,
        "message": "Activation successful"
    }
