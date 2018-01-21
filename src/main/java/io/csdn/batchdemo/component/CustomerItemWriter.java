package io.csdn.batchdemo.component;

import io.csdn.batchdemo.model.Customer;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * @author Zhantao Feng.
 */
public class CustomerItemWriter implements ItemWriter<List<Customer>> {

    @Override
    public void write(List<? extends List<Customer>> aggregateList) throws Exception {
        for (List<Customer> customerList : aggregateList) {
            customerList.forEach(System.out::println);
        }
    }
}
