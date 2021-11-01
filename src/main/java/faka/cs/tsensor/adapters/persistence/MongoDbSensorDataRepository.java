package faka.cs.tsensor.adapters.persistence;

import faka.cs.tsensor.adapters.persistence.entity.SensorActivityEntity;
import faka.cs.tsensor.adapters.persistence.entity.SensorDataEntity;
import faka.cs.tsensor.application.domain.AggregatedDailyData;
import faka.cs.tsensor.application.domain.AggregatedHourlyData;
import faka.cs.tsensor.application.domain.TemperatureData;
import faka.cs.tsensor.application.port.outgoing.SensorDataRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.data.mongodb.core.aggregation.Aggregation.addFields;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

public class MongoDbSensorDataRepository implements SensorDataRepository {

    public final MongoTemplate mongoTemplate;

    public MongoDbSensorDataRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    @Transactional
    public void upsert(String sensorId, List<TemperatureData> sensorData) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, SensorDataEntity.class);

        sensorData.forEach(it -> {
            LocalDateTime dateTimeHourOfTheDay = it.getPointInTime().truncatedTo(ChronoUnit.HOURS);

            Criteria sensorIdCriteria = Criteria.where("sensorId").is(sensorId);
            Criteria dateCriteria = Criteria.where("date").is(dateTimeHourOfTheDay);

            int temperatureMinute = it.getPointInTime().getMinute();

            Update update = new Update().addToSet(
                    "data",
                    new SensorActivityEntity(temperatureMinute, it.getTemperature())
            );

            bulkOps.upsert(Query.query(sensorIdCriteria.andOperator(dateCriteria)), update);
        });

        bulkOps.execute();
    }


    @Override
    public List<AggregatedDailyData> getAggregatedAverageDailyTemperature(
            String sensorId,
            LocalDate from,
            LocalDate to
    ) {

        Criteria sensorIdCriteria = Criteria.where("sensorId").is(sensorId);
        Criteria fromCriteria = Criteria.where("date").gte(from.atStartOfDay());
        Criteria toCriteria = Criteria.where("date").lte(to.atTime(LocalTime.MAX));

        Aggregation aggregation = newAggregation(Arrays.asList(
                match(sensorIdCriteria.andOperator(fromCriteria, toCriteria)),
                addFields()
                        .addField("yearMonthDay")
                        .withValueOf(DateOperators.dateValue("$date")
                                .withTimezone(DateOperators.Timezone.valueOf(TimeZone.getDefault().getID()))
                                .toString("%Y-%m-%d"))
                        .build(),
                unwind("$data"),
                group("$yearMonthDay")
                        .avg("$data.temperature").as("avg"),
                sort(Sort.by("_id").ascending()),
                addFields()
                        .addField("date")
                        .withValue(DateOperators.dateFromString("$_id"))
                        .addField("average")
                        .withValue(ArithmeticOperators.Round.roundValueOf("$avg").place(2))
                        .build(),
                project("date", "average").andExclude("_id")
        ));

        AggregationResults<AggregatedDailyData> aggregate = mongoTemplate.aggregate(
                aggregation,
                SensorDataEntity.class,
                AggregatedDailyData.class
        );

        return aggregate.getMappedResults();
    }

    @Override
    public List<AggregatedHourlyData> getAggregatedAverageHourlyTemperature(
            String sensorId,
            LocalDateTime from,
            LocalDateTime to
    ) {

        Criteria sensorIdCriteria = Criteria.where("sensorId").is(sensorId);
        Criteria fromCriteria = Criteria.where("date").gte(from.truncatedTo(ChronoUnit.HOURS));
        Criteria toCriteria = Criteria.where("date").lte(to.truncatedTo(ChronoUnit.HOURS));

        Aggregation aggregation = newAggregation(Arrays.asList(
                match(sensorIdCriteria.andOperator(fromCriteria, toCriteria)),
                addFields()
                        .addField("$yearMonthDayHour")
                        .withValueOf(DateOperators.dateValue("$date")
                                .withTimezone(DateOperators.Timezone.valueOf(TimeZone.getDefault().getID()))
                                .toString("%Y-%m-%dT%H:00:00"))
                        .build(),
                unwind("$data"),
                group("$yearMonthDayHour")
                        .avg("$data.temperature").as("avg"),
                sort(Sort.by("_id").ascending()),
                addFields()
                        .addField("date")
                        .withValue(DateOperators.dateFromString("$_id")
                                .withTimezone(DateOperators.Timezone.valueOf(TimeZone.getDefault().getID())))
                        .addField("average")
                        .withValue(ArithmeticOperators.Round.roundValueOf("$avg").place(2))
                        .build(),
                project("date", "average").andExclude("_id")
        ));
        AggregationResults<AggregatedHourlyData> aggregate = mongoTemplate.aggregate(
                aggregation,
                SensorDataEntity.class,
                AggregatedHourlyData.class
        );

        return aggregate.getMappedResults();
    }
}
