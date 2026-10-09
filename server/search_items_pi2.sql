SELECT * FROM order_lines WHERE product_name ILIKE '%Hot Dog%';
SELECT * FROM payments WHERE created_at > CURRENT_DATE;
SELECT * FROM orders WHERE created_at > CURRENT_DATE;
