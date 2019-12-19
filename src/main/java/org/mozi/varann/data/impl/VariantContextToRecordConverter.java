package org.mozi.varann.data.impl;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import htsjdk.variant.variantcontext.VariantContext;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 */
public interface VariantContextToRecordConverter<RecordType> {
    RecordType convert(VariantContext var1, final ReferenceDictionary refDic);
}

