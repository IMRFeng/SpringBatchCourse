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

    private int executionCount = 0;

    @Override
    public void write(List<? extends List<Customer>> aggregateList) throws Exception {
        executionCount++;
        if (executionCount >= 3 && executionCount <= 5) {
            System.out.println("处理数据失败");
            throw InvalidDataException.newInstance();
        }

        for (List<Customer> customerList : aggregateList) {
            customerList.forEach(System.out::println);
        }
    }

    @Autowired
    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
}