package faka.cs.tsensor.adapters.persistence.entity;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "sensorData")
@CompoundIndex(def = "{'sensorId': 1, 'date': 1}")
public class SensorDataEntity {

    @Id
    String id;
    String sensorId;
    // Hour of the day
    LocalDateTime date;
    // It will present 60 entries, 1 per minute.
    // I use an object to represent the sensor activity(temperature) as it is more
    // flexible to add more information in the future
    List<SensorActivityEntity> data;
}
