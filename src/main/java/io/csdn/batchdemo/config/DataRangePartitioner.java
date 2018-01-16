package io.csdn.batchdemo.config;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhantao Feng.
 */
public class DataRangePartitioner implements Partitioner {

    private JdbcOperations jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int min = jdbcTemplate.queryForObject("SELECT MIN(id) from CUSTOMER_A", Integer.class);
        int max = jdbcTemplate.queryForObject("SELECT MAX(id) from CUSTOMER_A", Integer.class);
        int targetSize = (max - min) / gridSize + 1;

        Map<String, ExecutionContext> result = new HashMap<>();
        int number = 0;
        int start = min;
        int end = start + targetSize - 1;

        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }
            value.putInt("fromId", start);
            value.putInt("toId", end);
            start += targetSize;
            end += targetSize;
            number++;
            value.putString("threadName", "Thread" + number);
        }

        return result;
    }
}
