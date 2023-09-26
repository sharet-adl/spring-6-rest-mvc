package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.model.CustomerDTO;
import guru.springframework.spring6restmvc.service.ICustomerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@AllArgsConstructor
public class CustomerController {
    private final ICustomerService customerService;

    @RequestMapping(value = "/api/v1/customer", method = RequestMethod.GET)
    public List<CustomerDTO> listAllCustomers() {
        log.debug("listAllCustomers - controller invoked");
        return customerService.listCustomers();
    }

    @RequestMapping(value = "/api/v1/customer/{id}", method = RequestMethod.GET)
    public CustomerDTO getCustomer(@PathVariable("id") UUID id) {
        log.debug("getCustomer - controller invoked");
        return customerService.getCustomerById(id).orElseThrow(NotFoundException::new);
    }

    @PostMapping(value = "/api/v1/customer")
    public ResponseEntity handlePost(@RequestBody CustomerDTO customer) {
        CustomerDTO savedCustomer = customerService.saveNewCustomer(customer);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/customer/" + savedCustomer.getId().toString());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    @PutMapping(value = "/api/v1/customer/{id}")
    public ResponseEntity updateById(@PathVariable("id") UUID customerId, @RequestBody CustomerDTO customer) {
        Optional<CustomerDTO> existing = customerService.updateCustomerById(customerId, customer);
        if(existing.isEmpty()) {
            throw new NotFoundException();
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/api/v1/customer/{id}")
    public ResponseEntity deleteById(@PathVariable UUID id) {
        if(!customerService.deleteById(id)) {
            throw new NotFoundException();
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PatchMapping(value = "/api/v1/customer/{id}")
    public ResponseEntity patchById(@PathVariable("id") UUID uuid, @RequestBody CustomerDTO customer) {
        customerService.patchById(uuid, customer);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
