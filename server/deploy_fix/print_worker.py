import time
import sys
import os
import cups

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
        return psycopg2.connect("dbname=briki user=ntvelop password=ntvelop host=localhost")
    except Exception as e:
        print(f"DB Error: {e}")
        return None

def process_job(conn, job):
    job_id = job[0]
    payload = job[1]
    
    print(f"Processing Job {job_id}")
    try:
        # Binary Format
        binary = format_escpos(payload)
        temp_path = f"/tmp/print_{job_id}.bin"
        with open(temp_path, "wb") as f:
            f.write(binary)
            
        # Print Raw
        c = cups.Connection()
        printers = c.getPrinters()
        if printers:
            pname = list(printers.keys())[0]
            c.printFile(pname, temp_path, f"Job {job_id}", {"raw": "true"})
            print(f"Sent to {pname}")
        else:
            print("No printers found")
            
        # Update Status
        with conn.cursor() as cur:
            cur.execute("UPDATE print_jobs SET status='SENT' WHERE id=%s", (job_id,))
        conn.commit()
        if os.path.exists(temp_path):
            os.remove(temp_path)
            
    except Exception as e:
        print(f"Failed: {e}")
        with conn.cursor() as cur:
            cur.execute("UPDATE print_jobs SET status='FAILED', last_error=%s WHERE id=%s", (str(e), job_id))
        conn.commit()

def loop():
    print("Print Worker Started (CP90/Greek Fix).")
    conn = None
    while True:
        try:
            if not conn or conn.closed:
                conn = get_db_conn()
            
            if conn:
                with conn.cursor() as cur:
                    cur.execute("SELECT id, payload_text FROM print_jobs WHERE status='PENDING' ORDER BY created_at ASC LIMIT 1")
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
