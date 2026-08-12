package com.ecommerce.gabrielportari.e_commerce_api.user;

import com.ecommerce.gabrielportari.e_commerce_api.user.entity.Role;
import com.ecommerce.gabrielportari.e_commerce_api.user.entity.User;
import com.ecommerce.gabrielportari.e_commerce_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Creates the first admin account from env vars when none exists yet. Keeps
 * admin credentials out of version control entirely (see V13 migration,
 * which removes the old hardcoded dev seed).
 */
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-bootstrap.email:}")
    private String bootstrapEmail;

    @Value("${app.admin-bootstrap.password:}")
    private String bootstrapPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        if (!StringUtils.hasText(bootstrapEmail) || !StringUtils.hasText(bootstrapPassword)) {
            log.warn(
                    "Nenhum usuário ADMIN existe e ADMIN_BOOTSTRAP_EMAIL/ADMIN_BOOTSTRAP_PASSWORD não foram "
                            + "definidos. Defina essas variáveis de ambiente e reinicie a aplicação para criar o "
                            + "primeiro admin.");
            return;
        }

        User admin = User.builder()
                .email(bootstrapEmail)
                .password(passwordEncoder.encode(bootstrapPassword))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Usuário ADMIN inicial criado para {}", bootstrapEmail);
    }
}
