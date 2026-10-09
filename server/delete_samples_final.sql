-- 1. Delete order_line_options for items in these categories
DELETE FROM order_line_options 
WHERE order_line_id IN (
    SELECT id FROM order_lines 
    WHERE item_id IN (
        SELECT id FROM catalog_items 
        WHERE category_id IN (
            'feb6f183-0cde-45e2-b3f7-07906ed44371',
            '8068e0d8-3683-4a2d-8dad-8553b720b1af'
        )
    )
);

-- 2. Delete order_lines linked to items in these categories
DELETE FROM order_lines 
WHERE item_id IN (
    SELECT id FROM catalog_items 
    WHERE category_id IN (
        'feb6f183-0cde-45e2-b3f7-07906ed44371',
        '8068e0d8-3683-4a2d-8dad-8553b720b1af'
    )
);

-- 3. Delete items in these categories
DELETE FROM catalog_items 
WHERE category_id IN (
    'feb6f183-0cde-45e2-b3f7-07906ed44371',
    '8068e0d8-3683-4a2d-8dad-8553b720b1af'
);

-- 4. Delete the categories themselves
DELETE FROM catalog_categories 
WHERE id IN (
    'feb6f183-0cde-45e2-b3f7-07906ed44371',
    '8068e0d8-3683-4a2d-8dad-8553b720b1af'
);
