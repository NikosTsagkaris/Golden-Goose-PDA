-- Search on Pi 1
SELECT 'Pi 1 Payments Today' as src, sum(amount_cents)/100.0 as total FROM payments WHERE created_at > CURRENT_DATE;
SELECT 'Pi 1 Waiter Totals' as src, u.username, sum(p.amount_cents)/100.0 FROM app_users u JOIN payments p ON u.id = p.created_by WHERE p.created_at > CURRENT_DATE GROUP BY u.username;
