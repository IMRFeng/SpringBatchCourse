package io.csdn.batchdemo.reader;

import org.springframework.batch.item.ItemReader;

import java.util.Iterator;
import java.util.List;

/**
 * @author Zhantao Feng.
 */
public class DemoInputReader implements ItemReader<String> {

    private Iterator<String> inputStrings;

    public DemoInputReader(List<String> strings) {
        this.inputStrings = strings.iterator();
    }

    @Override
    public String read() throws Exception {
        if (this.inputStrings.hasNext()) {
            return this.inputStrings.next();
        }
        return null;
    }
}
