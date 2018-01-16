package io.csdn.batchdemo.repository;

import io.csdn.batchdemo.model.Customer;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Zhantao Feng.
 */
@Repository
public interface CustomerRepository extends CrudRepository<Customer, Integer> {
    List<Customer> findByIdGreaterThanEqualAndIdLessThanOrderById(int minValue, int maxValue);
}
