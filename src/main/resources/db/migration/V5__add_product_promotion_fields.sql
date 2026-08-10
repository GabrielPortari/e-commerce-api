ALTER TABLE products
    ADD COLUMN on_sale BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN discount_price NUMERIC(10, 2),
    ADD CONSTRAINT chk_products_discount_price CHECK (discount_price IS NULL OR discount_price < price);

CREATE INDEX idx_products_on_sale ON products (on_sale);
