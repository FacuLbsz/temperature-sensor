package faka.cs.tsensor.application.domain;

import java.time.LocalDate;
import lombok.Value;

@Value
public class AggregatedDailyData {
    Double average;
    LocalDate date;
}


