ALTER TABLE products ADD COLUMN slug VARCHAR(220);

UPDATE products
SET slug = lower(regexp_replace(regexp_replace(name, '[^a-zA-Z0-9]+', '-', 'g'), '(^-+|-+$)', '', 'g')) || '-' || id;

ALTER TABLE products ALTER COLUMN slug SET NOT NULL;
ALTER TABLE products ADD CONSTRAINT uq_products_slug UNIQUE (slug);
