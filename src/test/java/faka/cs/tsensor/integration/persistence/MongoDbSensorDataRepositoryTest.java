package faka.cs.tsensor.integration.persistence;

import faka.cs.tsensor.TemperatureDataFactory;
import faka.cs.tsensor.adapters.persistence.entity.SensorActivityEntity;
import faka.cs.tsensor.adapters.persistence.entity.SensorDataEntity;
import faka.cs.tsensor.application.domain.AggregatedDailyData;
import faka.cs.tsensor.application.domain.AggregatedHourlyData;
import faka.cs.tsensor.application.domain.TemperatureData;
import faka.cs.tsensor.application.port.outgoing.SensorDataRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
public class MongoDbSensorDataRepositoryTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    }

    @Autowired
    SensorDataRepository mongoDbSensorDataRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void upsertCreatesOneSensorActivitySuccessfully() {
        //given
        String sensorId = UUID.randomUUID().toString();
        LocalDateTime pointInTime = LocalDateTime.now();

        //when
        mongoDbSensorDataRepository.upsert(
                sensorId,
                singletonList(
                        new TemperatureData(pointInTime, 6D)
                )
        );

        //then
        Criteria sensorIdCriteria = Criteria.where("sensorId").is(sensorId);

        LocalDateTime dateTimeHourOfTheDay = pointInTime.truncatedTo(ChronoUnit.HOURS);
        Criteria dateCriteria = Criteria.where("date").is(dateTimeHourOfTheDay);


        List<SensorDataEntity> sensorDataEntities = mongoTemplate.find(
                Query.query(sensorIdCriteria.andOperator(dateCriteria)),
                SensorDataEntity.class
        );

        assertEquals(1, sensorDataEntities.size());

        SensorDataEntity sensorDataEntity = sensorDataEntities.get(0);
        assertEquals(sensorId, sensorDataEntity.getSensorId());
        assertEquals(pointInTime.truncatedTo(ChronoUnit.HOURS), sensorDataEntity.getDate());

        List<SensorActivityEntity> data = sensorDataEntity.getData();
        assertEquals(1, data.size());
        assertEquals(new SensorActivityEntity(pointInTime.getMinute(), 6.0D), data.get(0));
    }

    @Test
    void upsertUpdatesOneSensorActivitySuccessfully() {

        //given
        String sensorId = UUID.randomUUID().toString();
        LocalDateTime pointInTime = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        LocalDateTime pointInTimePlusOneMinutes = pointInTime.plusMinutes(1);

        //when
        mongoDbSensorDataRepository.upsert(
                sensorId,
                asList(
                        new TemperatureData(pointInTime, 6D),
                        new TemperatureData(pointInTimePlusOneMinutes, 7D)
                )
        );

        //then
        Criteria sensorIdCriteria = Criteria.where("sensorId").is(sensorId);

        LocalDateTime dateTimeHourOfTheDay = pointInTime.truncatedTo(ChronoUnit.HOURS);
        Criteria dateCriteria = Criteria.where("date").is(dateTimeHourOfTheDay);


        List<SensorDataEntity> sensorDataEntities = mongoTemplate.find(
                Query.query(sensorIdCriteria.andOperator(dateCriteria)),
                SensorDataEntity.class
        );

        assertEquals(1, sensorDataEntities.size());

        SensorDataEntity sensorDataEntity = sensorDataEntities.get(0);
        assertEquals(sensorId, sensorDataEntity.getSensorId());
        assertEquals(pointInTime.truncatedTo(ChronoUnit.HOURS), sensorDataEntity.getDate());

        List<SensorActivityEntity> data = sensorDataEntity.getData();
        assertEquals(2, data.size());
        assertEquals(new SensorActivityEntity(pointInTime.getMinute(), 6.0D), data.get(0));
        assertEquals(new SensorActivityEntity(pointInTimePlusOneMinutes.getMinute(), 7.0D), data.get(1));
    }

    @Test
    void upsertCreatesTwoSensorActivityEntitiesSuccessfullyWhenHoursDiffer() {

        //given
        String sensorId = UUID.randomUUID().toString();
        LocalDateTime pointInTime = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        LocalDateTime pointInTimePlusOneMinutes = pointInTime.plusHours(1).plusMinutes(1);

        //when
        mongoDbSensorDataRepository.upsert(
                sensorId,
                asList(
                        new TemperatureData(pointInTime, 6D),
                        new TemperatureData(pointInTimePlusOneMinutes, 7D)
                )
        );

        //then
        Criteria sensorIdCriteria = Criteria.where("sensorId").is(sensorId);

        List<SensorDataEntity> sensorDataEntities = mongoTemplate.find(
                Query.query(sensorIdCriteria),
                SensorDataEntity.class
        );

        assertEquals(2, sensorDataEntities.size());

        SensorDataEntity sensorDataEntity1 = sensorDataEntities.get(0);
        assertEquals(sensorId, sensorDataEntity1.getSensorId());
        assertEquals(pointInTime.truncatedTo(ChronoUnit.HOURS), sensorDataEntity1.getDate());

        List<SensorActivityEntity> sensorDataEntityData = sensorDataEntity1.getData();
        assertEquals(1, sensorDataEntityData.size());
        assertEquals(new SensorActivityEntity(pointInTime.getMinute(), 6.0D), sensorDataEntityData.get(0));

        SensorDataEntity sensorDataEntity2 = sensorDataEntities.get(1);
        assertEquals(sensorId, sensorDataEntity2.getSensorId());
        assertEquals(pointInTimePlusOneMinutes.truncatedTo(ChronoUnit.HOURS), sensorDataEntity2.getDate());

        List<SensorActivityEntity> sensorDataEntity2Data = sensorDataEntity2.getData();
        assertEquals(1, sensorDataEntity2Data.size());
        assertEquals(new SensorActivityEntity(pointInTimePlusOneMinutes.getMinute(), 7D), sensorDataEntity2Data.get(0));
    }


    @Test
    public void averageTemperatureDataIsSuccessfullyAggregatedDaily() {

        //given
        LocalDateTime now = LocalDateTime.now();
        TemperatureDataFactory temperatureDataFactory = new TemperatureDataFactory(now);

        TemperatureData temperatureDataAtTodayZeroHoursOneMinute =
                temperatureDataFactory.temperatureDataAtTodayZeroHoursOneMinute();
        TemperatureData temperatureDataAtTodayOneHoursTwoMinutes =
                temperatureDataFactory.temperatureDataAtTodayOneHoursTwoMinutes();
        TemperatureData temperatureDataAtYesterdayZeroHoursOneMinute =
                temperatureDataFactory.temperatureDataAtYesterdayZeroHoursOneMinute();
        TemperatureData temperatureDataAtYesterdayOneHoursTwoMinutes =
                temperatureDataFactory.temperatureDataAtYesterdayOneHoursTwoMinutes();

        String sensorId = UUID.randomUUID().toString();

        mongoDbSensorDataRepository.upsert(
                sensorId,
                asList(
                        temperatureDataAtTodayZeroHoursOneMinute,
                        temperatureDataAtTodayOneHoursTwoMinutes,
                        temperatureDataAtYesterdayZeroHoursOneMinute,
                        temperatureDataAtYesterdayOneHoursTwoMinutes
                ));

        LocalDate from = LocalDate.from(now).minusDays(1);
        LocalDate to = LocalDate.from(now);

        //when
        List<AggregatedDailyData> averageDailyTemperature =
                mongoDbSensorDataRepository.getAggregatedAverageDailyTemperature(
                sensorId,
                from,
                to
        );

        //then
        assertEquals(
                asList(
                        new AggregatedDailyData(3.5D, temperatureDataFactory.getYesterdayZeroHours().toLocalDate()),
                        new AggregatedDailyData(5.25D, temperatureDataFactory.getTodayZeroHours().toLocalDate())
                ),
                averageDailyTemperature
        );
    }

    @Test
    public void averageTemperatureDataIsSuccessfullyAggregatedHourly() {

        //given
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

        String sensorId = UUID.randomUUID().toString();
        mongoDbSensorDataRepository.upsert(
                sensorId,
                asList(
                        temperatureDataAtTodayZeroHoursOneMinute,
                        temperatureDataAtTodayZeroHoursTwoMinutes,
                        temperatureDataAtTodayOneHoursOneMinute,
                        temperatureDataAtTodayOneHoursTwoMinutes
                ));

        LocalDateTime todayZeroHours = temperatureDataFactory.getTodayZeroHours();
        LocalDateTime todayOneHours = temperatureDataFactory.getTodayOneHours();

        LocalDateTime from = todayZeroHours.minusDays(1);
        LocalDateTime to = todayOneHours.plusDays(1);

        //when
        List<AggregatedHourlyData> averageHourlyTemperature =
                mongoDbSensorDataRepository.getAggregatedAverageHourlyTemperature(
                sensorId,
                from,
                to
        );

        //then
        assertEquals(
                asList(
                        new AggregatedHourlyData(5.25D, todayZeroHours.truncatedTo(ChronoUnit.HOURS)),
                        new AggregatedHourlyData(3.5D, todayOneHours.truncatedTo(ChronoUnit.HOURS))
                ),
                averageHourlyTemperature
        );
    }

}
