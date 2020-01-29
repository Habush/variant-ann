package org.mozi.varann.data.impl.transcript;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.mozi.varann.data.impl.TSVToRecordConverter;
import org.mozi.varann.data.records.TranscriptRecord;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 1/29/20
 */
public class TranscriptRecordConverter implements TSVToRecordConverter<TranscriptRecord, String> {
    @Override
    public TranscriptRecord convert(String line, ReferenceDictionary refDict) {
        TranscriptRecord builder = new TranscriptRecord();
        String[] values = line.split("\\t");
        builder.setId(values[0]);
        builder.setName(values[1]);
        builder.setGeneId(values[2]);
        builder.setChrom(values[3]);
        builder.setTxStart(Long.parseLong(values[4]));
        builder.setTxEnd(Long.parseLong(values[5]));

        return builder;
    }
}
