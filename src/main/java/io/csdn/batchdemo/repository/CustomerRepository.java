package io.csdn.batchdemo.repository;

import io.csdn.batchdemo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Zhantao Feng.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>{
}
