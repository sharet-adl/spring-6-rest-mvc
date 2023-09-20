package guru.springframework.spring6restmvc.service;

import guru.springframework.spring6restmvc.model.Customer;

import java.util.List;
import java.util.UUID;

public interface ICustomerService {
    Customer getCustomerById(UUID id);

    List<Customer> listAllCustomer();

    Customer saveNewCustomer(Customer customer);

    Customer updateCustomerById(UUID id, Customer customer);

    void deleteById(UUID id);

    void patchById(UUID customerId, Customer customer);
}
