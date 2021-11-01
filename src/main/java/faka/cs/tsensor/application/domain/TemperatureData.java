package faka.cs.tsensor.application.domain;

import java.time.LocalDateTime;
import lombok.NonNull;
import lombok.Value;


@Value
public class TemperatureData {
    @NonNull
    LocalDateTime pointInTime;
    @NonNull
    Double temperature;
}
