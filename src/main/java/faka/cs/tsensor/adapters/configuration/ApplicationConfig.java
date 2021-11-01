package faka.cs.tsensor.adapters.configuration;

import faka.cs.tsensor.adapters.persistence.MongoDbSensorDataRepository;
import faka.cs.tsensor.application.port.incoming.SensorService;
import faka.cs.tsensor.application.port.outgoing.SensorDataRepository;
import faka.cs.tsensor.application.services.SensorServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class ApplicationConfig {

    @Bean
    public SensorDataRepository sensorDataRepository(MongoTemplate mongoTemplate) {
        return new MongoDbSensorDataRepository(mongoTemplate);
    }

    @Bean
    public SensorService sensorService(SensorDataRepository sensorDataRepository) {
        return new SensorServiceImpl(sensorDataRepository);
    }

    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

}
