package faka.cs.tsensor.application.domain;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Value;

@Value
public class AggregatedDailySensorData {
    String sensorId;
    LocalDateTime from;
    LocalDateTime to;
    List<AggregatedDailyData> data;
}
