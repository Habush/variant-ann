import org.junit.Assert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mozi.varann.util.SearchUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 2/17/20
 */
public class SearchUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"src/test/resources/test.xml"})
    public void testGetPubmedInfo(String path) throws IOException {
        FileInputStream inputStream = new FileInputStream(path);
        Map<String, String> infoMap = SearchUtils.getPubmedInfo(inputStream);

        Assert.assertEquals(infoMap.get("title"), "Sherloc: a comprehensive refinement of the ACMG-AMP variant classification criteria.");
        Assert.assertEquals(infoMap.get("publishedDate"), "11-05-2017");
    }
}
