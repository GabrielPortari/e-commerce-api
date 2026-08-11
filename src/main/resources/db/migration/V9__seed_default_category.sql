ALTER TABLE categories
    ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT false;

INSERT INTO categories (name, is_default)
SELECT 'Geral', true
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE is_default = true);

-- Garante em nível de banco que nunca existe mais de uma categoria padrão,
-- mesmo que um bug futuro na aplicação tente criar outra.
CREATE UNIQUE INDEX idx_categories_single_default ON categories (is_default) WHERE is_default;
