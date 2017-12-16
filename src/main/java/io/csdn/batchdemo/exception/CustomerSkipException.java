package io.csdn.batchdemo.exception;

/**
 * @author Zhantao Feng.
 */
public class CustomerSkipException extends RuntimeException {

    private static final long serialVersionUID = -1;

    public static CustomerSkipException newInstance(String message) {
        return new CustomerSkipException(message);
    }

    private CustomerSkipException(String message) {
        super(message);
    }
}
