package org.mozi.varann;


import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.mozi.varann","org.mozi.varann.config", "org.mozi.varann.data"})
public class Application {

    public static void main(String[] args) {
        DOMConfigurator.configure("src/main/resources/log4j.xml");
        SpringApplication.run(Application.class);
    }
}
