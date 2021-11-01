package faka.cs.tsensor.application.port.outgoing;

import faka.cs.tsensor.application.domain.AggregatedDailyData;
import faka.cs.tsensor.application.domain.AggregatedHourlyData;
import faka.cs.tsensor.application.domain.TemperatureData;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
    It provides a repository layer to handle the use cases that require access to the persisted data of a sensor.
 */
public interface SensorDataRepository {

    void upsert(String sensorId, List<TemperatureData> sensorData);

    List<AggregatedDailyData> getAggregatedAverageDailyTemperature(
            String sensorId,
            LocalDate from,
            LocalDate to
    );

    List<AggregatedHourlyData> getAggregatedAverageHourlyTemperature(
            String sensorId,
            LocalDateTime from,
            LocalDateTime to
    );
}
