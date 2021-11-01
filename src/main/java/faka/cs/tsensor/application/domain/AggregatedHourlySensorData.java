package faka.cs.tsensor.application.domain;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Value;

@Value
public class AggregatedHourlySensorData {
    String sensorId;
    LocalDateTime from;
    LocalDateTime to;
    List<AggregatedHourlyData> data;
}
