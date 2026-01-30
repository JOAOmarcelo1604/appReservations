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
public class EventServiceTest {

    @Autowired
    private ICustomerService costomerService;

    @Autowired
    private IAuthService authService;

    @Test
    public void shouldAddNewEvent() {
        Customer customer = new Customer();
        customer.setFullName("John Doe");
        customer.setEmail("tst@tessst.cmmm");
        customer.setPassword("securePassword123");
        customer.setPhoneNumber("+1234567890");
        customer.setCpf("729.256.723-00");
        customer.setPassportNumber("A12345678");
        customer.setBirthDate(LocalDate.of(1990, 1, 1));
        customer.setPreferredLanguage("EN");
        customer.setCountryOrigin("USA");
        customer.setNotes("This is a test customer.");
        customer.setCreatedAt(LocalDateTime.now());

        assertNotNull(costomerService.insertCustomer(customer));

    }

    @Test
    public void shouldAddNewEvenAuth() {
        Customer customerAuth = new Customer();
        customerAuth.setFullName("John Doe");
        customerAuth.setEmail("st@testte.cooom");
        customerAuth.setPassword("securePassword123");
        customerAuth.setPhoneNumber("+1234567890");
        customerAuth.setCpf("138.541.929-40");
        customerAuth.setPassportNumber("A12345678");
        customerAuth.setBirthDate(LocalDate.of(1990, 1, 1));
        customerAuth.setPreferredLanguage("EN");
        customerAuth.setCountryOrigin("USA");
        customerAuth.setNotes("This is a test customer.");
        customerAuth.setCreatedAt(LocalDateTime.now());


        assertNotNull(authService.criarUsuario(customerAuth));

    }
}



