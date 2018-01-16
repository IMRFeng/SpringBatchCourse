package io.csdn.batchdemo.service;

import io.csdn.batchdemo.model.Customer;
import io.csdn.batchdemo.repository.CustomerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Zhantao Feng.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> findCustomersWithPagination(int minValue, int maxValue) {
        return this.customerRepository.findByIdGreaterThanEqualAndIdLessThanOrderById(minValue, maxValue);
    }
}
