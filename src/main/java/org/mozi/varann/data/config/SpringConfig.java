package org.mozi.varann.data.config;


import de.charite.compbio.jannovar.data.JannovarData;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

/**
 * The bean configuration to get Ignite instance
 */

@Configuration
@EnableIgniteRepositories(basePackages = "org.mozi.varann.data")
public class IgniteSpringConfig {

    @Bean
    public Ignite igniteInstance(){
        return Ignition.start("example-cache.xml");
    }

    @Bean
    public IgniteCache<String, JannovarData> igniteCacheInstance(){
        Ignite ignite = igniteInstance();

       return ignite.getOrCreateCache("refCache");
    }

}
