package io.csdn.batchdemo.repository;

import io.csdn.batchdemo.model.BatchDataDestination;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Zhantao Feng.
 */
public interface BatchDataDestinationRepository extends CrudRepository<BatchDataDestination, Integer> {
}
