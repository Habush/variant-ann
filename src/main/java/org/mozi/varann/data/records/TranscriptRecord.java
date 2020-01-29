package org.mozi.varann.data.records;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/29/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity("transcript")
public class TranscriptRecord {

    @Id
    private ObjectId _id;

    private String id;
    private String name;
    private String geneId;
    private String chrom;

    private long txStart;
    private long txEnd;
}
