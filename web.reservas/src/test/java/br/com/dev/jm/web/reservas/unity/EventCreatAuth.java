package br.com.dev.jm.web.reservas.unity;


import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.service.auth.IAuthService;
import br.com.dev.jm.web.reservas.service.customer.ICustomerService;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class EventCreatAuth {

    @Autowired
    private IAuthService authService;

    @Test
    public void shouldAddNewEvenAuth() {
        Customer customer = new Customer();
        customer.setFullName("John Doe");
        customer.setEmail("test@testtte.com");
        customer.setPassword("securePassword123");
        customer.setPhoneNumber("+1234567890");
        customer.setCpf("129.546.729-40");
        customer.setPassportNumber("A12345678");
        customer.setBirthDate(LocalDate.of(1990, 1, 1));
        customer.setPreferredLanguage("EN");
        customer.setCountryOrigin("USA");
        customer.setNotes("This is a test customer.");
        customer.setCreatedAt(LocalDateTime.now());


        assertNotNull(authService.criarUsuario(customer));

    }
}



