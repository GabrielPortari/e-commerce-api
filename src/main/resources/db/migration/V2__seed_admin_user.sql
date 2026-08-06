-- Dev seed user. Credentials: admin@ecommerce.com / admin123
INSERT INTO users (email, password, role, created_at)
VALUES (
    'admin@ecommerce.com',
    '$2b$10$hsRtp0rK.e98JWDOnVIm1eeG3AnCCTnzo10PBgLXrgt9.gnlG3V9W',
    'ADMIN',
    now()
);
