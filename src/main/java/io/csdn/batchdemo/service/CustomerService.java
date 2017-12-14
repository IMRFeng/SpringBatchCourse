package io.csdn.batchdemo.service;

import io.csdn.batchdemo.dto.ReaderResponse;

/**
 * @author Zhantao Feng.
 */
public interface CustomerService {
    ReaderResponse findCustomersWithPagination(int page, int size);
}
