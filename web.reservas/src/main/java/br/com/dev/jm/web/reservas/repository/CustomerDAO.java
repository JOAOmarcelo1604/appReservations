package br.com.dev.jm.web.reservas.repository;

import br.com.dev.jm.web.reservas.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerDAO extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    Optional<Customer> findByFullName(String fullName);
}
