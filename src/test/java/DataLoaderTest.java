package org.mozi.varann;

import htsjdk.variant.variantcontext.VariantContext;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mozi.varann.data.DataLoader;
import org.mozi.varann.data.GenomeDbRepository;
import org.mozi.varann.data.ReferenceRepository;
import org.mozi.varann.data.TranscriptDbRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DataLoaderTest {

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
        dataLoader = new DataLoader(transcriptRepo, refRepo, ignite);
        ReflectionTestUtils.setField(dataLoader, "basePath", "src/test/resources");
    }

    @Test
    public void testDbMapInit() {
        dataLoader.loadDbPath();
        assertThat(dataLoader.getDbPathMap()).size().isGreaterThan(0);
    }

    @Test
    public void testDataCacheLoad(){
        dataLoader.loadDbPath();
        dataLoader.loadGenomeCache();
        List<VariantContext> vcs = (List< VariantContext>) ignite.getOrCreateCache("genomeCache").get("test");
        assertThat(vcs).size().isGreaterThan(1);
        logger.info("VC: " +  vcs.get(0).getID());

    }

}
