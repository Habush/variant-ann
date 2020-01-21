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
import java.util.Collection;

/**
 * Represents on entry in the Clinvar VCF database file
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity("clinvar")
@AllArgsConstructor
@NoArgsConstructor
public class ClinVarRecord extends BaseRecord {

	// Fields up to the INFO column
	@Id
	@JsonIgnore
	private ObjectId _id;

	// Entries of the INFO column

	/**
	 * Annotations, by index of reference
	 */
	private ArrayList<ClinVarAnnotation> annotations = new ArrayList<>();

	/**
	 * Publication ids for this variant
	 */
	private Collection<String> pubmeds;

}
