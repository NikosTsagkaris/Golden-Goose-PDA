import time
import sys
import os
import socket

try:
    import cups
except ImportError:
    cups = None

# ESC/POS Setup
INIT = b'\x1b\x40'
FONT_A = b'\x1b\x4d\x00'
BOLD_ON = b'\x1b\x45\x01'
BOLD_OFF = b'\x1b\x45\x00'
SIZE_2X = b'\x1d\x21\x11'
SIZE_NORMAL = b'\x1d\x21\x00'
SELECT_GREEK = b'\x1b\x74\x5a' # CP90 / WPC1253
CUT = b'\x1d\x56\x01'
FEED = b'\n\n\n\n'

def format_escpos(content):
    result = INIT + FONT_A + SELECT_GREEK
    
    def encode_greek(txt):
        try:
            return txt.encode('cp1253')
        except:
            return txt.encode('cp1253', errors='replace')

    for line in content.split('\n'):
        if not line.strip() and not line.startswith('['):
            result += b'\n'
            continue
            
        if line.startswith('[LB]'):
            text = line[4:]
            result += SIZE_2X + BOLD_ON + encode_greek(text) + b'\n' + SIZE_NORMAL + BOLD_OFF
        elif line.startswith('[B]'):
            text = line[3:]
            result += BOLD_ON + encode_greek(text) + b'\n' + BOLD_OFF
        elif line.startswith('[CUT]'):
            result += FEED + CUT
        else:
            result += encode_greek(line) + b'\n'
            
    return result

def get_db_conn():
    import psycopg2
    try:
        # Using the confirmed credentials from previous steps
        return psycopg2.connect("dbname=ntvelop_db user=ntvelop_app password=2105135381 host=localhost")
    except Exception as e:
        print(f"DB Error: {e}")
        return None

def send_to_network_printer(ip, data):
    print(f"Sending to network printer at {ip}:9100")
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.settimeout(10)
        s.connect((ip, 9100))
        s.sendall(data)
        print("Data sent successfully via socket.")

def process_job(conn, job):
    job_id = job[0]
    payload = job[1]
    printer_ip = job[2]
    
    print(f"Processing Job {job_id} (Target IP: {printer_ip or 'CUPS default'})")
    try:
        # Binary Format
        binary = format_escpos(payload)
        
        if printer_ip:
            # Direct Network Printing
            send_to_network_printer(printer_ip, binary)
        else:
            # Fallback to CUPS for local printers
            temp_path = f"/tmp/print_{job_id}.bin"
            with open(temp_path, "wb") as f:
                f.write(binary)
                
            c = cups.Connection()
            printers = c.getPrinters()
            if printers:
                pname = list(printers.keys())[0]
                c.printFile(pname, temp_path, f"Job {job_id}", {"raw": "true"})
                print(f"Sent to local printer {pname} via CUPS")
            else:
                print("No local printers found in CUPS and no Printer IP provided.")
            
            if os.path.exists(temp_path):
                os.remove(temp_path)
            
        # Update Status
        with conn.cursor() as cur:
            cur.execute("UPDATE print_jobs SET status='SENT' WHERE id=%s", (job_id,))
        conn.commit()
            
    except Exception as e:
        print(f"Failed: {e}")
        with conn.cursor() as cur:
            cur.execute("UPDATE print_jobs SET status='FAILED', last_error=%s WHERE id=%s", (str(e), job_id))
        conn.commit()

def loop():
    print("Print Worker Started (Remote Socket + CUPS).")
    conn = None
    while True:
        try:
            if not conn or conn.closed:
                conn = get_db_conn()
            
            if conn:
                with conn.cursor() as cur:
                    cur.execute("SELECT id, payload_text, printer_ip FROM print_jobs WHERE status='PENDING' ORDER BY created_at ASC LIMIT 1")
                    job = cur.fetchone()
                    if job:
                        process_job(conn, job)
                    else:
                        time.sleep(1) 
            else:
                time.sleep(5)
        except Exception as e:
            print(f"Loop Error: {e}")
            time.sleep(5)
            conn = None

if __name__ == "__main__":
    loop()
