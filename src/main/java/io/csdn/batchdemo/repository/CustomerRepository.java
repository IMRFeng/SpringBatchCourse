package io.csdn.batchdemo.repository;

import io.csdn.batchdemo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Zhantao Feng.
 */
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
