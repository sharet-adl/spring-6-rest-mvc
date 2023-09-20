package guru.springframework.spring6restmvc.service;

import guru.springframework.spring6restmvc.model.Customer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomerService implements ICustomerService {
    private Map<UUID, Customer> customerMap;

    public CustomerService() {
        customerMap = new HashMap<>();

        Customer cust1 = Customer.builder()
                .name("C1")
                .id(UUID.randomUUID())
                .version(1)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        Customer cust2 = Customer.builder()
                .name("C2")
                .id(UUID.randomUUID())
                .version(1)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        customerMap.put(cust1.getId(), cust1);
        customerMap.put(cust2.getId(), cust2);
    }

    @Override
    public Customer getCustomerById(UUID id) {
        return customerMap.get(id);
    }

    @Override
    public List<Customer> listAllCustomer() {
        return new ArrayList<>(customerMap.values());
    }

    @Override
    public Customer saveNewCustomer(Customer customer) {
        Customer newCustomer = Customer.builder()
                .id(customer.getId())
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .version(1)
                .name(customer.getName())
                .build();
        return newCustomer;
    }

    @Override
    public Customer updateCustomerById(UUID id, Customer customer) {
        Customer existing = customerMap.get(id);
        if(true) {
            existing.setName(customer.getName());
            existing.setVersion(customer.getVersion());
        }
        // not needed - but reminder to invoke SAVE when using real repositories
        //customerMap.put(id, existing);
        return existing;
    }

    public void deleteById(UUID id) {
        customerMap.remove(id);
    }

    @Override
    public void patchById(UUID customerId, Customer customer) {
        Customer existing = customerMap.get(customerId);

        if(StringUtils.hasText(customer.getName())) {
            existing.setName(customer.getName());
        }
    }
}
