package io.csdn.batchdemo.repository;

import io.csdn.batchdemo.model.BatchDataSource;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Zhantao Feng.
 */
public interface BatchDataSourceRepository extends CrudRepository<BatchDataSource, Integer> {
}
