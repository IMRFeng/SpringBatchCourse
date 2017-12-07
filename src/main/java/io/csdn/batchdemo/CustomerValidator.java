package io.csdn.batchdemo;

import io.csdn.batchdemo.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;
import org.springframework.util.StringUtils;

/**
 * @author Zhantao Feng.
 */
public class CustomerValidator implements Validator<Customer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerValidator.class);

    @Override
    public void validate(Customer customer) throws ValidationException {
        if (StringUtils.containsWhitespace(customer.getPhone1()) || StringUtils.containsWhitespace(customer.getPhone2())) {
            String errorMsg = String.format("The customer %s's phone number has invalid character(s)", customer.getLastName());
            LOGGER.error(errorMsg);
            throw new ValidationException(errorMsg);
        }
    }
}
