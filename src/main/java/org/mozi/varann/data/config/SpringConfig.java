package org.mozi.varann.data.config;


import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.springdata20.repository.config.EnableIgniteRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * The bean configuration to get Ignite instance
 */

@Configuration
@EnableIgniteRepositories(basePackages = "org.mozi.varann.data")
public class SpringConfig {

    @Bean
    public Ignite igniteInstance(){
        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder tcpVmFinder = new TcpDiscoveryVmIpFinder();
        IgniteConfiguration cfg = new IgniteConfiguration();
        tcpVmFinder.setAddresses(Arrays.asList("88.198.22.185"));
        spi.setIpFinder(tcpVmFinder);
        spi.setJoinTimeout(3000);
        cfg.setDiscoverySpi(spi);
        return Ignition.start(cfg);
    }

}
