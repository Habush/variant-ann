package org.mozi.varann.data.impl.genes;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.apache.commons.csv.CSVRecord;
import org.mozi.varann.data.impl.TSVToRecordConverter;
import org.mozi.varann.data.records.GeneRecord;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/30/19
 */
public class GeneRecordConverter implements TSVToRecordConverter<GeneRecord, CSVRecord> {
    @Override
    public GeneRecord convert(CSVRecord record, ReferenceDictionary refDict) {
        GeneRecord geneRecord = new GeneRecord();

        geneRecord.setId(record.get("geneID"));
        geneRecord.setEntrezId(record.get("entrezID"));
        geneRecord.setSymbol(record.get("HGNC"));
        geneRecord.setName(record.get("name"));
        geneRecord.setChrom(record.get("chr"));
        geneRecord.setStart(Long.parseLong(record.get("start")));
        geneRecord.setEnd(Long.parseLong(record.get("end")));
        geneRecord.setType(record.get("type"));
        geneRecord.setTranscriptCount(Integer.parseInt(record.get("transcriptCount")));

        return geneRecord;
    }
}
