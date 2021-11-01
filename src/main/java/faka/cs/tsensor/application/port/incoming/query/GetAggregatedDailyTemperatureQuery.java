package faka.cs.tsensor.application.port.incoming.query;

import java.time.LocalDate;
import lombok.Value;

@Value
public class GetAggregatedDailyTemperatureQuery {
    String sensorId;
    LocalDate from;
    LocalDate to;
}
