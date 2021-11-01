package faka.cs.tsensor.application.port.incoming;

import faka.cs.tsensor.application.domain.AggregatedDailySensorData;
import faka.cs.tsensor.application.domain.AggregatedHourlySensorData;
import faka.cs.tsensor.application.port.incoming.command.AddTemperatureDataCommand;
import faka.cs.tsensor.application.port.incoming.query.GetAggregatedDailyTemperatureQuery;
import faka.cs.tsensor.application.port.incoming.query.GetAggregatedHourlyTemperatureQuery;

/**
    It provides a service layer to handle the use cases that interact with a sensor.
 */
public interface SensorService {
    void add(AddTemperatureDataCommand addTemperatureDataCommand);

    AggregatedDailySensorData getAggregatedDailyTemperature(
            GetAggregatedDailyTemperatureQuery getAggregatedDailyTemperatureQuery
    );

    AggregatedHourlySensorData getAggregatedHourlyTemperature(
            GetAggregatedHourlyTemperatureQuery getAggregatedHourlyTemperatureQuery
    );
}