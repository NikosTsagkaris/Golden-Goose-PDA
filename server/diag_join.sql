-- Query 1: Direct count of paid lines
SELECT 'Direct Count' as src, count(*), sum(unit_price_cents * qty) / 100.0 as total 
FROM order_lines 
WHERE status = 'PAID' AND is_voided = FALSE;

-- Query 2: Joined count used in Waiter Totals
SELECT 'Joined Count' as src, count(*), sum(ol.unit_price_cents * ol.qty) / 100.0 as total 
FROM app_users u 
JOIN orders o ON o.created_by = u.id 
JOIN order_lines ol ON ol.order_id = o.id 
WHERE ol.status = 'PAID' AND ol.is_voided = FALSE;

-- Query 3: Check for multiple orders with same ID? (Impossible)
-- Query 4: Check if an Order can have multiple created_by? (Impossible)
-- Query 5: Check if there are lines with order_id that doesn't exist?
SELECT count(*) FROM order_lines ol LEFT JOIN orders o ON ol.order_id = o.id WHERE o.id IS NULL;
