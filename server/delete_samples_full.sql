-- 1. Delete order_line_options that are linked to items in these categories
DELETE FROM order_line_options 
WHERE order_line_id IN (
    SELECT id FROM order_lines 
    WHERE item_id IN (
        SELECT id FROM catalog_items 
        WHERE category_id IN (
            'a61cecac-9dd2-4d3b-82a5-8d9b332bf459',
            'feb6f183-0cde-4d40-85f0-575083f5e5da',
            '8068e0d8-3683-49ca-96cf-71d37452d0b3'
        )
    )
);

-- 2. Delete order_lines linked to items in these categories
DELETE FROM order_lines 
WHERE item_id IN (
    SELECT id FROM catalog_items 
    WHERE category_id IN (
        'a61cecac-9dd2-4d3b-82a5-8d9b332bf459',
        'feb6f183-0cde-4d40-85f0-575083f5e5da',
        '8068e0d8-3683-49ca-96cf-71d37452d0b3'
    )
);

-- 3. Delete the items themselves
DELETE FROM catalog_items 
WHERE category_id IN (
    'a61cecac-9dd2-4d3b-82a5-8d9b332bf459',
    'feb6f183-0cde-4d40-85f0-575083f5e5da',
    '8068e0d8-3683-49ca-96cf-71d37452d0b3'
);

-- 4. Finally delete the categories
DELETE FROM catalog_categories 
WHERE id IN (
    'a61cecac-9dd2-4d3b-82a5-8d9b332bf459',
    'feb6f183-0cde-4d40-85f0-575083f5e5da',
    '8068e0d8-3683-49ca-96cf-71d37452d0b3'
);
