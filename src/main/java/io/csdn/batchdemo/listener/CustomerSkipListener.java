package io.csdn.batchdemo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;

/**
 * @author Zhantao Feng.
 */
@Slf4j
public class CustomerSkipListener {

    @OnSkipInRead
    public void onSkipInRead(Throwable t) {
        log.error("读取数据时出现异常，进行跳过处理，异常信息: {} - {}", t.getMessage(), t.getClass().getName());
    }

    @OnSkipInWrite
    public void onSkipInWrite(Object o, Throwable t) {
        log.error("输出数据{}时出现异常，进行跳过处理，异常信息: {} - {}, {}", o, t.getMessage(), t.getClass().getName());
    }

    @OnSkipInProcess
    public void onSkipInProcess(Object o, Throwable t) {
        log.error("处理数据{}时出现异常，进行跳过处理，异常信息: {} - {}", o, t.getMessage(), t.getClass().getName());
    }
}