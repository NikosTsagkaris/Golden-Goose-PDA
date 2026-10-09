INSERT INTO option_groups (id, name, group_code, is_active) VALUES (5, 'Cold Tea Options', 3, true) ON CONFLICT DO NOTHING;
UPDATE options SET group_id = 5 WHERE group_id = 4 AND name IN ('Λεμόνι', 'Ροδάκινο');
INSERT INTO products (name, price, category_id, is_available, option_group_code) VALUES ('Monster', 3.8, 22, true, -1) ON CONFLICT DO NOTHING;
