package faka.cs.tsensor.application.domain;

import java.time.LocalDateTime;
import lombok.Value;

@Value
public class AggregatedHourlyData {
    Double average;
    LocalDateTime date;
}