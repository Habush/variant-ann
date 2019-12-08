package org.mozi.varann;


import org.mozi.varann.util.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"org.mozi.varann.config", "org.mozi.varann.data", "org.mozi.varann", "org.mozi.varann.web"})
@EnableConfigurationProperties(StorageProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
