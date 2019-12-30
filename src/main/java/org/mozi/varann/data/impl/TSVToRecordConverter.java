package org.mozi.varann.data.impl;

import de.charite.compbio.jannovar.data.ReferenceDictionary;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/30/19
 */
public interface TSVToRecordConverter<RecordType, TSVRecord> {
    RecordType convert(TSVRecord record, final ReferenceDictionary refDict);
}
