package faka.cs.tsensor.application.services;

import faka.cs.tsensor.application.domain.AggregatedDailySensorData;
import faka.cs.tsensor.application.domain.AggregatedHourlySensorData;
import faka.cs.tsensor.application.port.incoming.SensorService;
import faka.cs.tsensor.application.port.incoming.command.AddTemperatureDataCommand;
import faka.cs.tsensor.application.port.incoming.query.GetAggregatedDailyTemperatureQuery;
import faka.cs.tsensor.application.port.incoming.query.GetAggregatedHourlyTemperatureQuery;
import faka.cs.tsensor.application.port.outgoing.SensorDataRepository;
import lombok.val;

public class SensorServiceImpl implements SensorService {

    private final SensorDataRepository sensorDataRepository;

    public SensorServiceImpl(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    @Override
    public void add(AddTemperatureDataCommand addTemperatureDataCommand) {
        val sensorId = addTemperatureDataCommand.getSensorId();
        sensorDataRepository.upsert(
                sensorId,
                addTemperatureDataCommand.getData()
        );

    }

    @Override
    public AggregatedDailySensorData getAggregatedDailyTemperature(
            GetAggregatedDailyTemperatureQuery getAggregatedDailyTemperatureQuery
    ) {
        val from = getAggregatedDailyTemperatureQuery.getFrom();
        val to = getAggregatedDailyTemperatureQuery.getTo();

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from date cannot be greater than to date");
        }

        val sensorId = getAggregatedDailyTemperatureQuery.getSensorId();
        val aggregatedAverageDailyTemperature = sensorDataRepository.getAggregatedAverageDailyTemperature(
                sensorId,
                from,
                to
        );

        return new AggregatedDailySensorData(
                sensorId,
                from.atStartOfDay(),
                to.atStartOfDay(),
                aggregatedAverageDailyTemperature
        );
    }

    @Override
    public AggregatedHourlySensorData getAggregatedHourlyTemperature(
            GetAggregatedHourlyTemperatureQuery getAggregatedHourlyTemperatureQuery
    ) {
        val from = getAggregatedHourlyTemperatureQuery.getFrom();
        val to = getAggregatedHourlyTemperatureQuery.getTo();

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from date cannot be greater than to date");
        }

        val sensorId = getAggregatedHourlyTemperatureQuery.getSensorId();
        val aggregatedAverageHourlyTemperature = sensorDataRepository.getAggregatedAverageHourlyTemperature(
                sensorId,
                from,
                to
        );

        return new AggregatedHourlySensorData(
                sensorId,
                from,
                to,
                aggregatedAverageHourlyTemperature
        );
    }
}
