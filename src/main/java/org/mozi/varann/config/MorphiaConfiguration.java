package org.mozi.varann.config;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *A class that configures the mongo and morphia dependencies
 */
@Configuration
public class MorphiaConfiguration {

    @Bean
    public Datastore datastore(@Value("${dbName}") String dbName) {
        Morphia morphia = new Morphia();

        //map entities
        morphia.mapPackage("org.mozi.varann.data.records");

        return morphia.createDatastore(new MongoClient(), dbName);
    }
}
