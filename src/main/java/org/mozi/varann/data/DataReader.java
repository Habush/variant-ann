package org.mozi.varann.data;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.vardbs.base.AlleleMatcher;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import htsjdk.variant.vcf.VCFFileReader;

import java.io.File;
import java.io.IOException;

/**
 * author: Abdulrahman Semrie
 * This class is used to load the variant annotation dbs from a vcf or gtf file
 */
public final  class DataReader {

    static VCFFileReader readVCF(String path) {
        return new VCFFileReader(new File(path), true);
    }

    static AlleleMatcher readFasta(String pathToFasta) throws JannovarVarDBException {
        return new AlleleMatcher(pathToFasta);
    }

    static JannovarData readSerializedObj(String path) throws SerializationException {
        return new JannovarDataSerializer(path).load();
    }

}
