package faka.cs.tsensor.unit;

import com.google.common.collect.HashBasedTable;
import faka.cs.tsensor.TemperatureDataFactory;
import faka.cs.tsensor.adapters.persistence.InMemorySensorDataRepository;
import faka.cs.tsensor.application.domain.AggregatedDailyData;
import faka.cs.tsensor.application.domain.AggregatedDailySensorData;
import faka.cs.tsensor.application.domain.AggregatedHourlyData;
import faka.cs.tsensor.application.domain.AggregatedHourlySensorData;
import faka.cs.tsensor.application.domain.TemperatureData;
import faka.cs.tsensor.application.port.incoming.command.AddTemperatureDataCommand;
import faka.cs.tsensor.application.port.incoming.query.GetAggregatedDailyTemperatureQuery;
import faka.cs.tsensor.application.port.incoming.query.GetAggregatedHourlyTemperatureQuery;
import faka.cs.tsensor.application.services.SensorServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SensorServiceTest {

    @Test
    public void oneTemperatureDataIsSuccessfullySaved() {
        InMemorySensorDataRepository sensorDataRepository = new InMemorySensorDataRepository();
        SensorServiceImpl sensorService = new SensorServiceImpl(sensorDataRepository);

        LocalDateTime pointInTime = LocalDateTime.now();
        TemperatureData temperatureData = new TemperatureData(pointInTime, 5D);

        sensorService.add(
                new AddTemperatureDataCommand(
                        "sensorId",
                        singletonList(temperatureData)
                )
        );

        List<TemperatureData> temperatureDataList = sensorDataRepository.get(
                "sensorId",
                pointInTime.truncatedTo(ChronoUnit.HOURS)
        );

        assertEquals(temperatureData, temperatureDataList.get(0));
        assertEquals(1, temperatureDataList.size());
    }

    @Test
    public void manyTemperatureDataAreSuccessfullySaved() {
        InMemorySensorDataRepository sensorDataRepository = new InMemorySensorDataRepository();
        SensorServiceImpl sensorService = new SensorServiceImpl(sensorDataRepository);

        LocalDateTime now = LocalDateTime.now();
        TemperatureDataFactory temperatureDataFactory = new TemperatureDataFactory(now);

        TemperatureData temperatureDataAtTodayZeroHoursOneMinute =
                temperatureDataFactory.temperatureDataAtTodayZeroHoursOneMinute();
        TemperatureData temperatureDataAtTodayZeroHoursTwoMinutes =
                temperatureDataFactory.temperatureDataAtTodayZeroHoursTwoMinutes();

        sensorService.add(
                new AddTemperatureDataCommand(
                        "sensorId",
                        asList(
                                temperatureDataAtTodayZeroHoursOneMinute,
                                temperatureDataAtTodayZeroHoursTwoMinutes
                        )
                )
        );

        List<TemperatureData> temperatureDataList = sensorDataRepository.get(
                "sensorId",
                temperatureDataFactory.getTodayZeroHours().truncatedTo(ChronoUnit.HOURS)
        );

        assertEquals(
                asList(
                        temperatureDataAtTodayZeroHoursOneMinute,
                        temperatureDataAtTodayZeroHoursTwoMinutes
                ),
                temperatureDataList
        );
    }


    @Test
    public void averageDailyTemperatureFailsWhenFromDateIsGreaterThanTo() {
        InMemorySensorDataRepository sensorDataRepository = new InMemorySensorDataRepository();
        SensorServiceImpl sensorService = new SensorServiceImpl(sensorDataRepository);

        LocalDate now = LocalDate.now();
        LocalDate from = now.plusDays(1);
        LocalDate to = now.minusDays(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sensorService.getAggregatedDailyTemperature(
                        new GetAggregatedDailyTemperatureQuery(
                                "sensorId",
                                from,
                                to
                        )
                ));

        assertEquals("from date cannot be greater than to date", exception.getMessage());
    }

    @Test
    public void averageTemperatureDataIsSuccessfullyAggregatedDaily() {

        LocalDateTime now = LocalDateTime.now();
        TemperatureDataFactory temperatureDataFactory = new TemperatureDataFactory(now);

        String sensorId = "sensorId";

        HashBasedTable<String, LocalDateTime, List<TemperatureData>> temperatureDataTable = HashBasedTable.create();

        LocalDateTime todayZeroHours = temperatureDataFactory.getTodayZeroHours();
        temperatureDataTable.put(
                sensorId,
                todayZeroHours.truncatedTo(ChronoUnit.HOURS),
                asList(
                        temperatureDataFactory.temperatureDataAtTodayZeroHoursOneMinute(),
                        temperatureDataFactory.temperatureDataAtTodayOneHoursTwoMinutes()
                ));

        LocalDateTime yesterdayZeroHours = temperatureDataFactory.getYesterdayZeroHours();
        temperatureDataTable.put(
                sensorId,
                yesterdayZeroHours.truncatedTo(ChronoUnit.HOURS),
                asList(
                        temperatureDataFactory.temperatureDataAtYesterdayZeroHoursOneMinute(),
                        temperatureDataFactory.temperatureDataAtYesterdayOneHoursTwoMinutes()
                ));

        InMemorySensorDataRepository sensorDataRepository = new InMemorySensorDataRepository(
                temperatureDataTable
        );
        SensorServiceImpl sensorService = new SensorServiceImpl(sensorDataRepository);

        LocalDate from = LocalDate.from(now).minusDays(1);
        LocalDate to = LocalDate.from(now);
        AggregatedDailySensorData averageDailyTemperature = sensorService.getAggregatedDailyTemperature(
                new GetAggregatedDailyTemperatureQuery(
                        sensorId,
                        from,
                        to
                )
        );

        assertEquals("sensorId", averageDailyTemperature.getSensorId());
        assertEquals(from.atStartOfDay(), averageDailyTemperature.getFrom());
        assertEquals(to.atStartOfDay(), averageDailyTemperature.getTo());

        assertEquals(
                asList(
                        new AggregatedDailyData(3.5D, yesterdayZeroHours.toLocalDate()),
                        new AggregatedDailyData(5.25D, todayZeroHours.toLocalDate())
                ),
                averageDailyTemperature.getData()
        );
    }


    @Test
    public void averageHourlyTemperatureFailsWhenFromDateIsGreaterThanTo() {
        InMemorySensorDataRepository sensorDataRepository = new InMemorySensorDataRepository();
        SensorServiceImpl sensorService = new SensorServiceImpl(sensorDataRepository);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusDays(1);
        LocalDateTime to = now.minusDays(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sensorService.getAggregatedHourlyTemperature(
                        new GetAggregatedHourlyTemperatureQuery(
                                "sensorId",
                                from,
                                to
                        )
                ));

        assertEquals("from date cannot be greater than to date", exception.getMessage());
    }

    @Test
    public void averageTemperatureDataIsSuccessfullyAggregatedHourly() {

        LocalDateTime now = LocalDateTime.now();
        TemperatureDataFactory temperatureDataFactory = new TemperatureDataFactory(now);

        TemperatureData temperatureDataAtTodayZeroHoursOneMinute =
                temperatureDataFactory.temperatureDataAtTodayZeroHoursOneMinute();
        TemperatureData temperatureDataAtTodayZeroHoursTwoMinutes =
                temperatureDataFactory.temperatureDataAtTodayZeroHoursTwoMinutes();
        TemperatureData temperatureDataAtTodayOneHoursOneMinute =
                temperatureDataFactory.temperatureDataAtTodayOneHoursOneMinute();
        TemperatureData temperatureDataAtTodayOneHoursTwoMinutes =
                temperatureDataFactory.temperatureDataAtTodayOneHoursTwoMinutes();


        String sensorId = "sensorId";

        HashBasedTable<String, LocalDateTime, List<TemperatureData>> temperatureDataTable = HashBasedTable.create();

        LocalDateTime todayZeroHours = temperatureDataFactory.getTodayZeroHours();
        LocalDateTime todayOneHours = temperatureDataFactory.getTodayOneHours();

        temperatureDataTable.put(
                sensorId,
                todayZeroHours.truncatedTo(ChronoUnit.HOURS),
                asList(
                        temperatureDataAtTodayZeroHoursOneMinute,
                        temperatureDataAtTodayZeroHoursTwoMinutes,
                        temperatureDataAtTodayOneHoursOneMinute,
                        temperatureDataAtTodayOneHoursTwoMinutes
                ));

        InMemorySensorDataRepository sensorDataRepository = new InMemorySensorDataRepository(
                temperatureDataTable
        );
        SensorServiceImpl sensorService = new SensorServiceImpl(sensorDataRepository);

        LocalDateTime from = todayZeroHours.minusDays(1);
        LocalDateTime to = todayOneHours.plusDays(1);
        AggregatedHourlySensorData averageHourlyTemperature = sensorService.getAggregatedHourlyTemperature(
                new GetAggregatedHourlyTemperatureQuery(
                        sensorId,
                        from,
                        to
                )
        );

        assertEquals("sensorId", averageHourlyTemperature.getSensorId());
        assertEquals(from, averageHourlyTemperature.getFrom());
        assertEquals(to, averageHourlyTemperature.getTo());

        assertEquals(
                asList(
                        new AggregatedHourlyData(5.25D, todayZeroHours.truncatedTo(ChronoUnit.HOURS)),
                        new AggregatedHourlyData(3.5D, todayOneHours.truncatedTo(ChronoUnit.HOURS))
                ),
                averageHourlyTemperature.getData()
        );
    }
}
