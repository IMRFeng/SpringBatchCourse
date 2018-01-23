package io.csdn.batchdemo.component;

import io.csdn.batchdemo.model.BatchDataSource;
import io.csdn.batchdemo.model.BatchDataDestination;
import io.csdn.batchdemo.repository.BatchDataDestinationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 数据处理
 *
 * @author Zhantao Feng.
 */
@Slf4j
public class DataItemProcessor implements ItemProcessor<BatchDataSource, BatchDataDestination> {

    private BatchDataDestinationRepository batchDataDestinationRepository;

    @Override
    public BatchDataDestination process(BatchDataSource item) throws Exception {
        if (StringUtils.isBlank(item.getDataText()) || item.getColumnNumber() < 0) {
            log.error("ID为{}出现非法数据", item.getId());
            throw new IllegalArgumentException("非法的参数配置，ID为" + item.getId());
        }

        BatchDataDestination destination = new BatchDataDestination();
        destination.setId(item.getId());

        // 根据数据源中的columnNumber值来update目标数据库表中对应的dataText
        final BeanWrapper wrapper = new BeanWrapperImpl(destination);
        wrapper.setPropertyValue("dataText" + item.getColumnNumber(), item.getDataText());

        BatchDataDestination dbDestination = this.batchDataDestinationRepository.findOne(item.getId());
        if (dbDestination == null) { //如果不存在在数据库那么添加其他列的数据
            destination.setSubClass(item.getSubClass());
            destination.setParentClass(item.getParentClass());
        }

        return destination;
    }

    @Autowired
    public void setBatchDataDestinationRepository(BatchDataDestinationRepository batchDataDestinationRepository) {
        this.batchDataDestinationRepository = batchDataDestinationRepository;
    }
}
