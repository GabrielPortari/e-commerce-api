package com.ecommerce.gabrielportari.e_commerce_api.config;

import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Refuses to start under the "prod" profile if secrets were left at their
 * dev-only defaults from application.yml. Activate with
 * SPRING_PROFILES_ACTIVE=prod in production so this check actually runs;
 * without it, dev/local runs are unaffected.
 */
@Component
public class ProdSecretsValidator {

    private static final String DEFAULT_JWT_SECRET = "change-this-to-a-long-random-secret-in-production";
    private static final String DEFAULT_DB_PASSWORD = "ecommerce";
    private static final int MIN_JWT_SECRET_LENGTH = 32;

    private final Environment environment;
    private final String jwtSecret;
    private final String dbPassword;

    public ProdSecretsValidator(
            Environment environment,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${spring.datasource.password}") String dbPassword) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
        this.dbPassword = dbPassword;
    }

    @PostConstruct
    void validate() {
        if (!List.of(environment.getActiveProfiles()).contains("prod")) {
            return;
        }

        if (DEFAULT_JWT_SECRET.equals(jwtSecret) || jwtSecret.length() < MIN_JWT_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET não foi definido com um valor seguro para produção (mínimo "
                            + MIN_JWT_SECRET_LENGTH + " caracteres, diferente do padrão de desenvolvimento).");
        }

        if (DEFAULT_DB_PASSWORD.equals(dbPassword)) {
            throw new IllegalStateException(
                    "DB_PASSWORD não foi definido para produção — ainda está usando o valor padrão de "
                            + "desenvolvimento.");
        }
    }
}
