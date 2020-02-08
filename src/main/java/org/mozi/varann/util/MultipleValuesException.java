package org.mozi.varann.util;

import com.google.common.base.Joiner;
import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 2/8/20
 */

@Data
public class MultipleValuesException extends Exception {
    private String req;
    private List<String> values;

    public MultipleValuesException(String req, List<String> values){
        super(String.format("Multiple values found for %s: %s", req, Joiner.on(",").join(values)));
    }
}
