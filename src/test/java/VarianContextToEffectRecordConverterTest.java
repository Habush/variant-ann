import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mozi.varann.data.impl.annotation.VariantContextToEffectRecordConverter;
import org.mozi.varann.data.records.VariantEffectRecord;

import java.io.File;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/20/19
 */

public class VarianContextToEffectRecordConverterTest extends BaseTest {

    private static ReferenceDictionary refDict;

    @BeforeAll
    public static void setUp() {
        refDict = HG19RefDictBuilder.build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/var_effect_test.vcf"})
    public void testConvert(String path) {
        try(VCFFileReader fileReader = new VCFFileReader(new File(path), false)) {
            VariantContext vc = fileReader.iterator().toList().get(1);
            VariantContextToEffectRecordConverter converter = new VariantContextToEffectRecordConverter();
            VariantEffectRecord record = converter.convert(vc, refDict);
            Assert.assertTrue(record.getHgvsNomination().size() > 0);
            logger.info(record.getHgvsNomination());
        }

    }
}
