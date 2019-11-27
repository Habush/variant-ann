package org.mozi.varann.data.config;


import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.vardbs.base.AlleleMatcher;
import htsjdk.variant.vcf.VCFFileReader;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;

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

    /*@Bean
    public IgniteCache<String, JannovarData> transcriptCache(){
        IgniteConfiguration cfg = new IgniteConfiguration();
//        TcpDiscoverySpi spi = new TcpDiscoverySpi();
//        TcpDiscoveryVmIpFinder tcpVmFinder = new TcpDiscoveryVmIpFinder();
//        tcpVmFinder.setAddresses(Arrays.asList("88.198.22.185"));
//        spi.setIpFinder(tcpVmFinder);
//        spi.setJoinTimeout(3000);
//        cfg.setDiscoverySpi(spi);
        CacheConfiguration <String, JannovarData> cacheConfig = new CacheConfiguration<>();
        cacheConfig.setName("transcriptCache");
        Ignite ignite = Ignition.getOrStart(cfg);
        return ignite.getOrCreateCache(cacheConfig);
    }

    @Bean
    public IgniteCache<String, AlleleMatcher> referenceCache(){
        IgniteConfiguration cfg = new IgniteConfiguration();
//        TcpDiscoverySpi spi = new TcpDiscoverySpi();
//        TcpDiscoveryVmIpFinder tcpVmFinder = new TcpDiscoveryVmIpFinder();
//        tcpVmFinder.setAddresses(Arrays.asList("88.198.22.185"));
//        spi.setIpFinder(tcpVmFinder);
//        spi.setJoinTimeout(3000);
//        cfg.setDiscoverySpi(spi);
        CacheConfiguration <String, AlleleMatcher> cacheConfig = new CacheConfiguration<>();
        cacheConfig.setName("refCache");
        Ignite ignite = Ignition.getOrStart(cfg);
        return ignite.getOrCreateCache(cacheConfig);
    }*/

    @Bean
    @Scope("singleton")
    public Ignite igniteInstance(){
        IgniteConfiguration cfg = new IgniteConfiguration();
//        TcpDiscoverySpi spi = new TcpDiscoverySpi();
//        TcpDiscoveryVmIpFinder tcpVmFinder = new TcpDiscoveryVmIpFinder();
//        tcpVmFinder.setAddresses(Arrays.asList("88.198.22.185"));
//        spi.setIpFinder(tcpVmFinder);
//        spi.setJoinTimeout(3000);
//        cfg.setDiscoverySpi(spi);
        return Ignition.getOrStart(cfg);
    }



}
