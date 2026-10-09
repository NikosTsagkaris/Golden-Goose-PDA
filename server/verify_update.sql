SELECT o.name, o.group_id, g.name as group_name FROM options o JOIN option_groups g ON o.group_id = g.id WHERE o.name IN ('Λεμόνι', 'Ροδάκινο');
SELECT name, price, category_id FROM products WHERE name = 'Monster';
