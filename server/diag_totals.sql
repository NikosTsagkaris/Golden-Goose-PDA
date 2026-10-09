SELECT 'General' as type, payment_method, COALESCE(SUM(unit_price_cents * qty), 0) / 100.0 as total 
FROM order_lines 
WHERE status = 'PAID' AND is_voided = FALSE 
GROUP BY payment_method;

SELECT 'Waiter' as type, u.username, ol.payment_method, COALESCE(SUM(ol.unit_price_cents * ol.qty), 0) / 100.0 as total 
FROM app_users u 
LEFT JOIN orders o ON o.created_by = u.id 
LEFT JOIN order_lines ol ON ol.order_id = o.id AND ol.is_voided = FALSE 
WHERE ol.status = 'PAID' 
GROUP BY u.username, ol.payment_method;
