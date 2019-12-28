package org.mozi.varann.data.records;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Entity("effect")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariantEffectRecord extends BaseRecord {
    /**
     * Mongo Id
     */
    @Id
    @JsonIgnore
    private ObjectId _id;

    // Fields up to the INFO column

    /**
     dbSNP rs id
     */
    @JsonIgnore
    public String rsId;

    /**
     * Annotation for the variant
     */
    private HashMap<String, Collection<AnnotationRecord>> annotation;

    private HashMap<String, Collection<String>> hgvsNomination;

}
