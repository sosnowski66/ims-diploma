package pl.wiktrans.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.wiktrans.ims.dto.CustomerDto;
import pl.wiktrans.ims.util.FailableActionResult;
import pl.wiktrans.ims.model.Customer;
import pl.wiktrans.ims.model.CustomerObject;
import pl.wiktrans.ims.repository.CustomerRepository;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerObjectService customerObjectService;

    public List<Customer> getAll() {
        return customerRepository.findAllByIsDeleted(false);
    }

    public Customer getById(Long id) { return customerRepository.getOne(id); }

    public Page<Customer> getPage(Pageable pageable) { return customerRepository.findActivePage(false, pageable); }

    public FailableActionResult addNewCustomer(CustomerDto customerDto) {
        try {
            Customer customer = customerDto.toEntity();
            customerRepository.save(customer);
            return FailableActionResult.success();
        } catch (Exception e){
            return FailableActionResult.failure(e.getMessage());
        }
    }

    public void updateCustomer(CustomerDto dto) {
        Customer customer = getById(dto.getId());
        customer.setEmail(dto.getEmail());
        customer.setNip(dto.getNip());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customerRepository.save(customer);

        dto.getCustomerObjects().forEach(dtoObject -> {
            if (dtoObject.getId() != null) {
                CustomerObject oldObject = customerObjectService.getById(dtoObject.getId());
                oldObject.setContactTitle(dtoObject.getContactTitle());
                oldObject.setContactName(dtoObject.getContactName());
                oldObject.setContactSurname(dtoObject.getContactSurname());
                oldObject.setAddress(dtoObject.getAddress());
                customerObjectService.save(oldObject);
            } else {
                CustomerObject customerObject = new CustomerObject();
                customerObject.setCustomer(customer);
                customerObject.setContactTitle(dtoObject.getContactTitle());
                customerObject.setContactName(dtoObject.getContactName());
                customerObject.setContactSurname(dtoObject.getContactSurname());
                customerObject.setAddress(dtoObject.getAddress());
                customerObjectService.save(customerObject);
            }
        });
    }

    public void deleteById(Long id) {
        Customer customer = getById(id);
        customer.setIsDeleted(true);
        customerRepository.save(customer);
        customerObjectService.deleteCustomerObjects(customer.getCustomerObjects());
    }
}
