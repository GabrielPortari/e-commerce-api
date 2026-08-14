ALTER TABLE reviews ADD COLUMN author_ip VARCHAR(45);

-- Um IP só pode avaliar o mesmo produto uma vez. Índice parcial (não
-- constraint de coluna toda) porque author_ip é nulo pra linhas antigas,
-- criadas antes desta migration.
CREATE UNIQUE INDEX ux_reviews_product_author_ip ON reviews (product_id, author_ip)
    WHERE author_ip IS NOT NULL;
