-- Delete items associated with sample categories
DELETE FROM catalog_items 
WHERE category_id IN (SELECT id FROM catalog_categories WHERE name LIKE 'ΣΑΜΠΛ%');

-- Delete the sample categories themselves
DELETE FROM catalog_categories 
WHERE name LIKE 'ΣΑΜΠΛ%';
