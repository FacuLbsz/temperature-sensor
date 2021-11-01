package faka.cs.tsensor.adapters.persistence;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import faka.cs.tsensor.application.domain.AggregatedDailyData;
import faka.cs.tsensor.application.domain.AggregatedHourlyData;
import faka.cs.tsensor.application.domain.TemperatureData;
import faka.cs.tsensor.application.port.outgoing.SensorDataRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemorySensorDataRepository implements SensorDataRepository {

    public Table<String, LocalDateTime, List<TemperatureData>> temperatureDataTable;

    public InMemorySensorDataRepository(Table<String, LocalDateTime, List<TemperatureData>> temperatureDataTable) {
        this.temperatureDataTable = temperatureDataTable == null ? HashBasedTable.create() : temperatureDataTable;
    }

    public InMemorySensorDataRepository() {
        this(null);
    }

    @Override
    public void upsert(String sensorId, List<TemperatureData> sensorData) {
        sensorData.forEach(temperatureData -> {
            LocalDateTime pointInTimeTruncatedHours = temperatureData.getPointInTime().truncatedTo(ChronoUnit.HOURS);
            List<TemperatureData> temperatureDataList = temperatureDataTable.get(sensorId, pointInTimeTruncatedHours);
            if (temperatureDataList == null) {
                temperatureDataTable.put(sensorId, pointInTimeTruncatedHours, Lists.newArrayList(temperatureData));
            } else {
                temperatureDataList.add(temperatureData);
            }
        });
    }

    @Override
    public List<AggregatedDailyData> getAggregatedAverageDailyTemperature(
            String sensorId,
            LocalDate from,
            LocalDate to
    ) {
        LocalDateTime fromAtStartOfDay = from.atStartOfDay();
        LocalDateTime toAtStartOfDay = to.atStartOfDay();
        return temperatureDataTable.row(sensorId)
                .entrySet()
                .stream()
                .filter(
                        it -> it.getKey().isEqual(fromAtStartOfDay) || it.getKey().isEqual(toAtStartOfDay)
                                || (it.getKey().isAfter(fromAtStartOfDay) && it.getKey().isBefore(toAtStartOfDay))
                )
                .sorted(Map.Entry.comparingByKey())
                .map(
                        it -> new AggregatedDailyData(
                                it.getValue()
                                        .stream()
                                        .mapToDouble(TemperatureData::getTemperature)
                                        .average()
                                        .orElse(Double.NaN),
                                it.getKey().toLocalDate()
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregatedHourlyData> getAggregatedAverageHourlyTemperature(
            String sensorId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        LocalDateTime fromAtStartOfDay = from.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime toAtStartOfDay = to.truncatedTo(ChronoUnit.HOURS);

        List<TemperatureData> temperatureData = temperatureDataTable.row(sensorId)
                .entrySet()
                .stream()
                .filter(
                        it -> it.getKey().isEqual(fromAtStartOfDay) || it.getKey().isEqual(toAtStartOfDay)
                                || (it.getKey().isAfter(fromAtStartOfDay) && it.getKey().isBefore(toAtStartOfDay))
                )
                .flatMap(it -> it.getValue().stream())
                .collect(Collectors.toList());

        final Map<LocalDateTime, List<TemperatureData>> collect = temperatureData
                .stream()
                .collect(Collectors.groupingBy(it -> it.getPointInTime().truncatedTo(ChronoUnit.HOURS)));

        return collect
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(it -> new AggregatedHourlyData(
                        it.getValue()
                                .stream()
                                .mapToDouble(TemperatureData::getTemperature)
                                .average()
                                .orElse(Double.NaN),
                        it.getKey()
                ))
                .collect(Collectors.toList());
    }

    public List<TemperatureData> get(String sensorId, LocalDateTime dateHourOfTheDay) {
        return temperatureDataTable.get(sensorId, dateHourOfTheDay);
    }
}
