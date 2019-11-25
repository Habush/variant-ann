package org.mozi.varann.data.config;


import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The bean configuration to get Ignite instance
 */

@Configuration
@EnableIgniteRepositories(basePackages = "org.mozi.varann.data")
public class SpringConfig {

    @Bean
    public Ignite igniteInstance(){
        return Ignition.start("example-cache.xml");
    }

}
