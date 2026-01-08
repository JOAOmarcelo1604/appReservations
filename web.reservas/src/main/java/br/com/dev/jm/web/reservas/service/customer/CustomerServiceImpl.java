package br.com.dev.jm.web.reservas.service.customer;

import br.com.dev.jm.web.reservas.entity.Customer;
import br.com.dev.jm.web.reservas.repository.CustomerDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements ICustomerService {


    private final CustomerDAO customerDAO;


    @Override
    @Transactional
    public Customer insertCustomer(Customer novo) {
        if(customerDAO.existsByEmail(novo.getEmail())){
            throw new IllegalArgumentException("Já existe um cliente com este e-mail.");
        }
        if(novo.getCpf() != null && customerDAO.existsByCpf(novo.getCpf())){
            throw new IllegalArgumentException("Já existe um cliente com este CPF");
        }
        return customerDAO.save(novo);
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long Id, Customer customerAtualizado) {
    Customer clienteExistente = findById(Id);

    //clienteExistente.setFullName(customerAtualizado.getFullName());
    clienteExistente.setPhoneNumber(customerAtualizado.getPhoneNumber());
    clienteExistente.setCountryOrigin(customerAtualizado.getCountryOrigin());
    clienteExistente.setPreferredLanguage(customerAtualizado.getPreferredLanguage());
    clienteExistente.setNotes(customerAtualizado.getNotes());

        return customerDAO.save(clienteExistente);
    }

    @Override
    @Transactional
    public Customer findById(Long Id) {
        return customerDAO.findById(Id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com ID: " + Id));
    }

    @Override
    @Transactional
    public Customer getName(String name) {
        return customerDAO.findByFullName(name)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com nome: " + name));
    }

    @Override
    @Transactional
    public List<Customer> getAll() {
        return (List<Customer>) customerDAO.findAll();
    }
}
