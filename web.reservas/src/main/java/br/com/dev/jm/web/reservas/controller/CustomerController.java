package br.com.dev.jm.web.reservas.controller;

import br.com.dev.jm.web.reservas.service.auth.IAuthService;
import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.service.customer.ICustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.dev.jm.web.reservas.security.UsuarioToken;


import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {


    private final ICustomerService service;

    private final IAuthService authService;

    @GetMapping
    public ResponseEntity<List<Customer>> getAll(){
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/search/{fullName}")
    public ResponseEntity<Customer> getByName(@PathVariable String fullName) {
        // Chama o método do service que criamos antes
        Customer customer = service.getName(fullName);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer novo){
        Customer res = authService.criarUsuario(novo);
        if(res != null){
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customerAtualizado){

        Customer res = service.updateCustomer(id, customerAtualizado);
        if (res != null) {
            return ResponseEntity.status(201).body(res);

        }
        return  ResponseEntity.badRequest().build();

    }



}

