package org.mozi.varann.data.records;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * Represents on entry in the Clinvar VCF database file
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
@Data
@Entity("clinvar")
@AllArgsConstructor
@NoArgsConstructor
public class ClinVarRecord {

	// Fields up to the INFO column
	@Id
	@JsonIgnore
	private ObjectId _id;

	/**
	 * Name of the chromosome
	 */
	@JsonIgnore
	private String chrom;
	/**
	 * Position of the variant, 0-based
	 */
	@JsonIgnore
	private int pos;
	/**
	 * ID of the variant
	 */
	@JsonIgnore
	private String alleleId;
	/**
	 * Reference sequence
	 */
	@JsonIgnore
	private String ref;
	/**
	 * Alternative alleles in cluster
	 */
	@JsonIgnore
	private List<String> alt = new ArrayList<>();

	/**
	 * Hgvs strings for each alternate allele
	 */
	@JsonIgnore
	private List<String> hgvs = new ArrayList<>();

	// TODO: enable the following two settings as well
	/** Whether or not there is an OMIM/OMIA annotation */
	// final private boolean hasOmim;
	/** Whether or not it is validated */
	// final private boolean isValidated;

	// Entries of the INFO column

	/**
	 * Annotations, by index of reference
	 */
	private HashMap<String, Collection<ClinVarAnnotation>> annotations;

	/**
	 * Publication ids for this variant
	 */
	private Collection<String> pubmeds;

}
