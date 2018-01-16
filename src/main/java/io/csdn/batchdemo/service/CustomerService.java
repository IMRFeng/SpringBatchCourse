package io.csdn.batchdemo.service;

import io.csdn.batchdemo.model.Customer;

import java.util.List;

/**
 * @author Zhantao Feng.
 */
public interface CustomerService {
    List<Customer> findCustomersWithPagination(int page, int size);
}
