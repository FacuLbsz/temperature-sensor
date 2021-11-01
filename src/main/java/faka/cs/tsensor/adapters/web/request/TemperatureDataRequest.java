package faka.cs.tsensor.adapters.web.request;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class TemperatureDataRequest {
    @NotNull
    LocalDateTime pointInTime;
    @NotNull
    Double temperature;
}
