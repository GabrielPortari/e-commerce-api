CREATE TABLE product_images (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_product_images_product_id ON product_images (product_id);
