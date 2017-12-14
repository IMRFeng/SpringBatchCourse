package io.csdn.batchdemo.component;

import io.csdn.batchdemo.exception.InvalidDataException;
import io.csdn.batchdemo.model.Customer;
import io.csdn.batchdemo.repository.CustomerRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Zhantao Feng.
 */
public class CustomerItemWriter implements ItemWriter<List<Customer>> {

    private CustomerRepository customerRepository;

    private int i = 0;

    @Override
    public void write(List<? extends List<Customer>> aggregateList) throws Exception {
//        if (this.i == 3) {
//            throw InvalidDataException.newInstance();
//        }

        for (List<Customer> customerList : aggregateList) {
            customerList.forEach(System.out::println);
        }

        i++;
    }

    @Autowired
    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
}
