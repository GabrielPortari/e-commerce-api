ALTER TABLE products
    ADD COLUMN featured BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX idx_products_featured ON products (featured);
