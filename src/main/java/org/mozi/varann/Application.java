/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 * Loads the genome data to mongodb and elasticsearch
 */
package org.mozi.varann;

import org.mozi.varann.util.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"org.mozi.varann.config", "org.mozi.varann.data", "org.mozi.varann.data.records","org.mozi.varann.data.impl" ,"org.mozi.varann.web", "org.mozi.varann"})
@EnableConfigurationProperties(StorageProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
