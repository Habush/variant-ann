import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mozi.varann.data.impl.annotation.VariantRecordConverter;
import org.mozi.varann.data.records.VariantRecord;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/20/19
 */

public class VariantRecordConverterTest extends BaseTest {

    private static ReferenceDictionary refDict;

    @BeforeAll
    public static void setUp() {
        refDict = HG19RefDictBuilder.build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/demo.tsv"})
    public void testConvert(String path) throws IOException {
        List<VariantRecord> variantRecords = new ArrayList<>();
        VariantRecordConverter converter = new VariantRecordConverter();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)))) {
            //read column
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null){
                variantRecords.add(converter.convert(line, refDict));
            }
            Assert.assertEquals(4, variantRecords.size());
            VariantRecord firstRec = variantRecords.get(0);
            Assert.assertEquals("p.C1270F", firstRec.getEnsAAChange().get(0).getProteinChange());
        }

    }
}
