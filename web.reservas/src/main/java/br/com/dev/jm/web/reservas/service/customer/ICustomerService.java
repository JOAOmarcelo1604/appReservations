package br.com.dev.jm.web.reservas.service.customer;

import br.com.dev.jm.web.reservas.entity.Customer;

import java.util.List;

public interface ICustomerService {

    public Customer insertCustomer(Customer novo);
    public Customer updateCustomer(Long id, Customer customerAtualizado);
    public Customer findById(Long Id);
    public Customer getName(String name);
    public List<Customer> getAll();

}
