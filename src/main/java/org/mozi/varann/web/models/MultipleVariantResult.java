package org.mozi.varann.web.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 2/8/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleVariantResult {

    private List<VariantInfo> result;
    private List<String> notFound;
    private Map<String, List<String>> multiVals;
}
