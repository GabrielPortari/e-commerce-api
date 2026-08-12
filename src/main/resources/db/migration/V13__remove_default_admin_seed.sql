-- Remove the dev-only admin seeded by V2 (admin@ecommerce.com / admin123).
-- That password is public (committed to version control) and must never
-- exist in any reachable environment. Only deletes the row if it still has
-- the original seeded hash, so a legitimately repurposed account with a
-- changed password is left untouched.
DELETE FROM users
WHERE email = 'admin@ecommerce.com'
  AND password = '$2b$10$hsRtp0rK.e98JWDOnVIm1eeG3AnCCTnzo10PBgLXrgt9.gnlG3V9W';
