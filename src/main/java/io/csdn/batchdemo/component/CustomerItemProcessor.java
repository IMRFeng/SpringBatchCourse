package io.csdn.batchdemo.component;

import io.csdn.batchdemo.model.Customer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhantao Feng.
 */
@Component
public class CustomerItemProcessor implements ItemProcessor<List<Customer>, List<Customer>> {
    @Override
    public List<Customer> process(List<Customer> item) throws Exception {
        List<Customer> batchCommits = new ArrayList<>();

        for (Customer customer : item) {
            customer.setLastName(customer.getLastName().toUpperCase());
            batchCommits.add(customer);
        }

        return batchCommits;
    }
}
