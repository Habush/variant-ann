package org.mozi.varann.data.impl.genes;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import org.apache.commons.csv.CSVRecord;
import org.mozi.varann.data.impl.TSVToRecordConverter;
import org.mozi.varann.data.records.GeneInfo;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/30/19
 */
public class GeneRecordConverter implements TSVToRecordConverter<GeneInfo, CSVRecord> {
    @Override
    public GeneInfo convert(CSVRecord record, ReferenceDictionary refDict) {
        GeneInfo geneInfo = new GeneInfo();

        geneInfo.setId(record.get("geneID"));
        geneInfo.setEntrezId(record.get("entrezID"));
        geneInfo.setSymbol(record.get("HGNC"));
        geneInfo.setName(record.get("name"));
        geneInfo.setChr(record.get("chr"));
        geneInfo.setStart(Long.parseLong(record.get("start")));
        geneInfo.setEnd(Long.parseLong(record.get("end")));
        geneInfo.setType(record.get("type"));
        geneInfo.setTranscriptCount(Integer.parseInt(record.get("transcriptCount")));

        return geneInfo;
    }
}
