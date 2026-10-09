-- Terminate connections to all target/unused databases
SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname IN ('ntvelop_db', 'pos_db', 'goldengoose_pos') AND pid != pg_backend_pid();

-- Rename active database
ALTER DATABASE ntvelop_db RENAME TO goldengoose_db;

-- Drop unused databases
DROP DATABASE pos_db;
DROP DATABASE goldengoose_pos;
