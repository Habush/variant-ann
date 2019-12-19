/**
 * @author <a href="mailto:hsamireh@gmail.com">Abdulrahman Semrie</a>
 * 12/18/19
 * Loads the genome data to mongodb and elasticsearch
 */
package org.mozi.varann;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozi.varann.data.DataLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Startup implements ApplicationListener<ContextRefreshedEvent> {
    private Logger logger = LogManager.getLogger(Startup.class);
    private final DataLoader loader;

    public Startup(DataLoader loader) {
        this.loader = loader;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            logger.info("Initializing the app....");
            loader.initData();
            logger.info("Records loaded.");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
