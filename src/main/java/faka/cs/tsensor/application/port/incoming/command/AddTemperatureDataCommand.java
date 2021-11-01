package faka.cs.tsensor.application.port.incoming.command;

import faka.cs.tsensor.application.domain.TemperatureData;
import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class AddTemperatureDataCommand {
    @NonNull
    String sensorId;
    @NonNull
    List<TemperatureData> data;
}
