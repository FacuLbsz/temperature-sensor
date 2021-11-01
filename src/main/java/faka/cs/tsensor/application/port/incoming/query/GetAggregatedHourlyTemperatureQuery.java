package faka.cs.tsensor.application.port.incoming.query;

import java.time.LocalDateTime;
import lombok.Value;

@Value
public class GetAggregatedHourlyTemperatureQuery {
    String sensorId;
    LocalDateTime from;
    LocalDateTime to;
}
