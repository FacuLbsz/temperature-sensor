package faka.cs.tsensor.adapters.web.request;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class AddTemperatureDataRequest {
    @NotNull
    @Valid
    List<TemperatureDataRequest> data;
}

