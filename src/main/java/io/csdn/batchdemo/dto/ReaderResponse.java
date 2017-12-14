package io.csdn.batchdemo.dto;

import io.csdn.batchdemo.model.Customer;
import lombok.Data;

import java.util.List;

/**
 * @author Zhantao Feng.
 */
@Data
public class ReaderResponse {

    private Boolean hasNext;

    private List<Customer> customers;

    public boolean hasNext() {
        return this.hasNext;
    }
}
