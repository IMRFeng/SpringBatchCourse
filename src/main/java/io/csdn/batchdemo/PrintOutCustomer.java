package io.csdn.batchdemo;

import io.csdn.batchdemo.model.Customer;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

/**
 * @author Zhantao Feng.
 */
public class PrintOutCustomer implements ItemWriter<Customer> {

    @Override
    public void write(List<? extends Customer> items) throws Exception {
        System.out.println("此chunk共有" + items.size() + "条数据！");

        items.forEach(System.out::println);
    }
}