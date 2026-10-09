-- Search on Pi 2 (amount column)
SELECT 'Pi 2 Payments Today' as src, sum(amount) FROM payments WHERE created_at > CURRENT_DATE;
SELECT 'Pi 2 Waiter Totals' as src, u.username, sum(p.amount) FROM users u JOIN orders o ON u.id = o.waiter_id JOIN order_lines ol ON o.id = ol.order_id JOIN payments p ON ol.id = p.line_id WHERE created_at > CURRENT_DATE GROUP BY u.username;

-- Search on Pi 1 (amount_cents column)
-- I will run this separately since column names differ
