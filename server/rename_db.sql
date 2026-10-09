SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'briki_pos' AND pid != pg_backend_pid();
ALTER DATABASE briki_pos RENAME TO goldengoose_pos;
