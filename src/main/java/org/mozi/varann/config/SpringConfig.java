package org.mozi.varann.config;


import de.charite.compbio.jannovar.JannovarException;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.impl.util.PathUtil;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;

/**
 * The bean configuration to get Ignite instance
 */

@Configuration
public class SpringConfig {

    private static final Logger logger = LogManager.getLogger(SpringConfig.class);

    @Bean
    public static PropertyPlaceholderConfigurer properties() {
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        ClassPathResource[] resources = new ClassPathResource[]{
                new ClassPathResource("application.properties")
        };
        ppc.setLocations(resources);
        ppc.setIgnoreUnresolvablePlaceholders(true);
        return ppc;
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient elasticClient() {
        RestClientBuilder clientBuilder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        return new RestHighLevelClient(clientBuilder);
    }

    @Bean
    @Scope("singleton")
    public ReferenceDictionary referenceDict(@Value("${basePath}") String basePath) throws JannovarException {
        logger.info("Loading reference dictionary...");
        JannovarData data = new JannovarDataSerializer(PathUtil.join(basePath, "refs", "hg19_ensembl.ser")).load();
        logger.info("Reference dictionary loaded.");
        return data.getRefDict();
    }


}
