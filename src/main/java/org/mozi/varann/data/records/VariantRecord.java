package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/13/20
 */
@EqualsAndHashCode(callSuper = true)
@Entity("variant")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantRecord extends BaseRecord {
    @Id
    @JsonIgnore
    private ObjectId _id;

    private String rsId;

    private String bioType;

    private List<String> ensGene = new ArrayList<>();

    private String exonicFunction;

    private String gene;

    private List<AminoAcidChange> ensAAChange = new ArrayList<>();

    private List<AminoAcidChange> refAAChange = new ArrayList<>();

    private IntervarRecord intervar;
}
