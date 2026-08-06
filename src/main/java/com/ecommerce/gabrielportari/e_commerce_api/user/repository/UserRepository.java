package com.ecommerce.gabrielportari.e_commerce_api.user.repository;

import com.ecommerce.gabrielportari.e_commerce_api.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
