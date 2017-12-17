package io.csdn.batchdemo.component;

import io.csdn.batchdemo.model.Customer;
import io.csdn.batchdemo.repository.CustomerRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Zhantao Feng.
 */
@Component
public class CustomerItemWriter implements ItemWriter<List<Customer>> {

    private CustomerRepository customerRepository;

    @Override
    public void write(List<? extends List<Customer>> aggregateList) {
        for (List<Customer> customerList : aggregateList) {
            for (Customer customer : customerList) {
                System.out.println(customer);
            }
        }
    }

    @Autowired
    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
}
