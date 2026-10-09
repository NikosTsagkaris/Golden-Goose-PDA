import time
import sys
import os

# Adapt path to allow importing app
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app import database, models, crud
from sqlalchemy.orm import Session

def get_printer_connection():
    # Only import active layout if needed
    try:
        import cups
        return cups.Connection()
    except ImportError:
        print("CUPS not available. using Mock Printer.")
        return None

def format_escpos(content):
    # ESC/POS commands per User Request
    INIT = b'\x1b\x40'
    FONT_A = b'\x1b\x4d\x00' # Font A
    BOLD_ON = b'\x1b\x45\x01' # Bold ON
    BOLD_OFF = b'\x1b\x45\x00'
    
    # GS ! n (Select character size)
    # n=0x77 (8x height & width -> Absolute Maximum ESC/POS size)
    # n=0x33 (4x height & width -> Extra Large)
    # n=0x22 (3x height & width -> Large)
    SIZE_MAX = b'\x1d\x21\x77'
    SIZE_4X  = b'\x1d\x21\x33'
    SIZE_2X  = b'\x1d\x21\x22'
    SIZE_NORMAL = b'\x1d\x21\x00'
    
    # Select Character Code Table
    SELECT_GREEK = b'\x1b\x74\x5a' 
    
    # GS V m n (Feed n dots and cut)
    CUT = b'\x1d\x56\x01' 
    
    # Paper feed
    FEED = b'\n\n\n\n'
    
    result = INIT + FONT_A + SELECT_GREEK
    
    for line in content.split('\n'):
        if not line.strip() and not line.startswith('['):
            result += b'\n'
            continue
            
        def encode_greek(txt):
            try:
                return txt.encode('cp1253') 
            except UnicodeEncodeError:
                return txt.encode('cp1253', errors='replace')

        if line.startswith('[LB]'):
            text = line[4:]
            result += SIZE_MAX + BOLD_ON + encode_greek(text) + b'\n' + SIZE_NORMAL + BOLD_OFF
            
        elif line.startswith('[B]'):
            text = line[3:]
            result += SIZE_4X + BOLD_ON + encode_greek(text) + b'\n' + SIZE_NORMAL + BOLD_OFF
            
        elif line.startswith('[CUT]'):
            result += FEED + CUT
        else:
            result += SIZE_2X + encode_greek(line) + b'\n' + SIZE_NORMAL
            
    return result

def main():
    print("Starting Print Worker...")
    conn = get_printer_connection()
    
    # Simple polling loop
    while True:
        db = database.SessionLocal()
        try:
            # Query pending jobs
            jobs = db.query(models.PrintJob).filter(models.PrintJob.status == models.PrintJobStatus.PENDING).all()
            
            for job in jobs:
                print(f"Processing Job #{job.id} for Order #{job.order_id}")
                try:
                    # In real life, select printer
                    printer_name = "Thermal_Printer" # User confirmed printer exists
                    
                    if conn:
                        printers = conn.getPrinters()
                        if printer_name not in printers:
                            if printers:
                                printer_name = list(printers.keys())[0]
                                print(f"Falling back to printer: {printer_name}")
                        
                        # Use binary mode for ESC/POS
                        temp_path = f"/tmp/print_job_{job.id}.bin"
                        binary_content = format_escpos(job.content)
                        
                        with open(temp_path, "wb") as f:
                            f.write(binary_content)
                        
                        # Print as RAW to bypass CUPS internal text filtering
                        conn.printFile(printer_name, temp_path, f"Order #{job.order_id}", {"raw": "true"})
                        
                        # Cleanup
                        os.remove(temp_path)
                        print(f"Sent binary ESC/POS to {printer_name}")
                    else:
                        # Mock print
                        print(">>> MOCK ESC/POS PRINT <<<")
                        print(job.content)
                        print(">>> END MOCK <<<")
                    
                    job.status = models.PrintJobStatus.SENT
                    db.commit()
                    
                except Exception as e:
                    print(f"Failed to print job {job.id}: {e}")
                    job.status = models.PrintJobStatus.FAILED
                    db.commit()
                    
        except Exception as e:
            print(f"Worker Loop Error: {e}")
        finally:
            db.close()
            
        time.sleep(2) # Poll every 2s

if __name__ == "__main__":
    main()
