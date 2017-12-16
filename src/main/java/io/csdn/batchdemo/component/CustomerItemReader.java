package io.csdn.batchdemo.component;

import io.csdn.batchdemo.dto.ReaderResponse;
import io.csdn.batchdemo.exception.InvalidDataException;
import io.csdn.batchdemo.model.Customer;
import io.csdn.batchdemo.service.CustomerService;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Zhantao Feng.
 */
public class CustomerItemReader implements ItemStreamReader<List<Customer>> {

    private CustomerService customerService;

    private int page;

    private ReaderResponse response = null;

    @Value("${spring.batch.read.size:100}")
    private int pageSize;

    @Override
    public List<Customer> read() throws Exception {
        if (this.page == -1) return null;

        if (response == null || response.hasNext()) {
            response = fetchCustomerData();

            this.page++;

            if (page == 12 || page == 13 || page == 14) {
                System.out.println("读取数据出错喽");
                throw new TimeoutException("出错啦， 超市异常");
            }
            return response.getCustomers();
        }

        this.page = -1;
        response = null;
        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        System.out.println("open..." + this.page);
        if (executionContext.containsKey("curPage")) {
            this.page = executionContext.getInt("curPage");
        } else {
            this.page = 0;
            executionContext.put("curPage", this.page);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        System.out.println("update..." + this.page);
        executionContext.put("curPage", this.page);
    }

    @Override
    public void close() throws ItemStreamException {
    }

    private ReaderResponse fetchCustomerData() {
        return this.customerService.findCustomersWithPagination(this.page, this.pageSize);
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }
}
