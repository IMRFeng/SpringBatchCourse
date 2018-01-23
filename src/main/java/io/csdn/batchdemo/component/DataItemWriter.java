package io.csdn.batchdemo.component;

import io.csdn.batchdemo.model.BatchDataDestination;
import io.csdn.batchdemo.repository.BatchDataDestinationRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 把处理过的数据写入到目标数据库表
 *
 * @author Zhantao Feng.
 */
public class DataItemWriter implements ItemWriter<BatchDataDestination> {

    private BatchDataDestinationRepository batchDataDestinationRepository;

    @Override
    public void write(List<? extends BatchDataDestination> items) throws Exception {
        this.batchDataDestinationRepository.save(items);
    }

    @Autowired
    public void setBatchDataDestinationRepository(BatchDataDestinationRepository batchDataDestinationRepository) {
        this.batchDataDestinationRepository = batchDataDestinationRepository;
    }
}
