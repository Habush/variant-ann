package org.mozi.varann.config;


import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.mozi.varann.data.fs.FileSystemWrapper;
import org.mozi.varann.data.fs.NioFileSystemWrapper;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
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

    @Bean
    public FileSystemWrapper fileSystemWrapper() {
        return new NioFileSystemWrapper();
    }

    @Bean(destroyMethod = "close")
    @Scope("singleton")
    public Ignite igniteInstance(){
        IgniteConfiguration cfg = new IgniteConfiguration();

        //TCP Discovery config
        /*TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder tcpVmFinder = new TcpDiscoveryVmIpFinder();
        tcpVmFinder.setAddresses(Arrays.asList("46.4.115.181"));
        spi.setIpFinder(tcpVmFinder);
        spi.setJoinTimeout(30000);
        cfg.setDiscoverySpi(spi);*/

        //Cache Configuration
        DataStorageConfiguration storageCfg = new DataStorageConfiguration();
        storageCfg.setPageSize(16*1024);
        cfg.setDataStorageConfiguration(storageCfg);
        return Ignition.getOrStart(cfg);
    }


}
