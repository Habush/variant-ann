package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity("genes")
public class GeneRecord {
    @Id
    @JsonIgnore
    private ObjectId _id;

    private String id;
    private String symbol;
    private String entrezId;
    private String name;
    private String chrom;
    private long start;
    private long end;
    private String type;
    private int transcriptCount;
}
