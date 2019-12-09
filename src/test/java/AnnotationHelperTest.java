import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import htsjdk.variant.variantcontext.VariantContext;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mozi.varann.AnnotationHelper;
import org.mozi.varann.data.DataLoader;
import org.mozi.varann.data.GenomeDbRepository;
import org.mozi.varann.data.ReferenceRepository;
import org.mozi.varann.data.TranscriptDbRepository;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import javax.cache.Cache;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AnnotationHelperTest {

    private static final Logger logger = LogManager.getLogger("test-logger");
    @Mock
    private TranscriptDbRepository transcriptRepo;
    @Mock
    private ReferenceRepository refRepo;
    @Mock
    private GenomeDbRepository genomeRepo;
    private static Ignite ignite;

    private DataLoader dataLoader;

    @BeforeAll
    public static void setUp() {
        ignite = Ignition.getOrStart(new IgniteConfiguration());
    }

    @AfterAll
    public static void tearDown() {
        ignite.close();
    }

    @BeforeEach
    public void initDataLoader() {
        dataLoader = new DataLoader(ignite, transcriptRepo, refRepo);
        ReflectionTestUtils.setField(dataLoader, "basePath", "src/test/resources");
    }

    @Test
    public void annotateByIdTest(){
        dataLoader.loadDbPath();
        dataLoader.loadGenomeCache();
        try (IgniteCache<String, List<VariantContext>> cache = ignite.getOrCreateCache("genomeCache");
             QueryCursor<Cache.Entry<String, List<VariantContext>>> cursor = cache.query(new ScanQuery<>((k, p) -> k.equals("test") && p.stream().anyMatch(v -> v.getID().equals("rs6040355"))))
        ) {
           List<Cache.Entry<String, List<VariantContext>>> result = cursor.getAll();
           assertThat(result.size()).isGreaterThan(0);
          /* System.out.println("Sample VCF");
           System.out.println(result.get(0).getValue().get(0).toString());*/
        }
    }
}
