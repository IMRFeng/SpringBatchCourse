package io.csdn.batchdemo.exception;

/**
 * @author Zhantao Feng.
 */
public class InvalidDataException extends RuntimeException {

    private static final long serialVersionUID = -1;

    public static InvalidDataException newInstance(String message) {
        return new InvalidDataException(message);
    }

    public static InvalidDataException newInstance() {
        return new InvalidDataException("数据出错啦！");
    }

    private InvalidDataException(String message) {
        super(message);
    }
}
