package org.mozi.varann.web.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/31/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneInfo {
    private String id;
    private String symbol;
    private String entrezId;
    private String name;
    private String type;

    List<VariantInfo> variants;
}
