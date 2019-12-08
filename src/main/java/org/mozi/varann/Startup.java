package org.mozi.varann;

import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.vardbs.base.JannovarVarDBException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mozi.varann.data.DataLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class Startup implements ApplicationListener<ContextRefreshedEvent> {
    private Logger logger = LogManager.getLogger(Startup.class);
    @Autowired
    private DataLoader loader;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            logger.info("Initializing the repos....");
            loader.init();
            logger.info("Repos loaded.");
        } catch (JannovarVarDBException | SerializationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
