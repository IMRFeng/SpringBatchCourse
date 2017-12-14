package io.csdn.batchdemo.service;

import io.csdn.batchdemo.dto.ReaderResponse;
import io.csdn.batchdemo.model.Customer;
import io.csdn.batchdemo.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    public ReaderResponse findCustomersWithPagination(int page, int size) {
        ReaderResponse readerResponse = new ReaderResponse();

        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.ASC, "id"));
        Page<Customer> pages = this.customerRepository.findAll(pageable);

        readerResponse.setHasNext(pages.hasNext());
        readerResponse.setCustomers(pages.getContent());

        return readerResponse;
    }
}
