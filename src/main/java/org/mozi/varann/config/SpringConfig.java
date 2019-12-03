package org.mozi.varann.config;


import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;

/**
 * The bean configuration to get Ignite instance
 */

@Configuration
@EnableIgniteRepositories(basePackages = "org.mozi.varann.data")
public class SpringConfig {

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
    public Ignite igniteInstance(){
        IgniteConfiguration cfg = new IgniteConfiguration();
//        TcpDiscoverySpi spi = new TcpDiscoverySpi();
//        TcpDiscoveryVmIpFinder tcpVmFinder = new TcpDiscoveryVmIpFinder();
//        tcpVmFinder.setAddresses(Arrays.asList("88.198.22.185"));
//        spi.setIpFinder(tcpVmFinder);
//        spi.setJoinTimeout(3000);
//        cfg.setDiscoverySpi(spi);
        return Ignition.start(cfg);
    }


}
