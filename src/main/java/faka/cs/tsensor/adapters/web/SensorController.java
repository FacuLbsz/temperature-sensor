package faka.cs.tsensor.adapters.web;

import faka.cs.tsensor.adapters.web.request.AddTemperatureDataRequest;
import faka.cs.tsensor.adapters.web.request.TemperatureDataRequest;
import faka.cs.tsensor.application.domain.AggregatedDailySensorData;
import faka.cs.tsensor.application.domain.AggregatedHourlySensorData;
import faka.cs.tsensor.application.domain.TemperatureData;
import faka.cs.tsensor.application.port.incoming.SensorService;
import faka.cs.tsensor.application.port.incoming.command.AddTemperatureDataCommand;
import faka.cs.tsensor.application.port.incoming.query.GetAggregatedDailyTemperatureQuery;
import faka.cs.tsensor.application.port.incoming.query.GetAggregatedHourlyTemperatureQuery;
import java.time.LocalDate;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    /**
     * Persists a set of temperature data for a given sensor id.
     *
     * @param sensorId                  sensorId
     * @param addTemperatureDataRequest set of temperature data to add to a sensor in a given time
     */
    @PutMapping(value = "/sensor/{sensorId}/temperature", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void add(
            @PathVariable String sensorId,
            @Valid @RequestBody AddTemperatureDataRequest addTemperatureDataRequest
    ) {
        sensorService.add(
                new AddTemperatureDataCommand(
                        sensorId,
                        addTemperatureDataRequest
                                .getData()
                                .stream()
                                .map(SensorController::requestToModel)
                                .collect(Collectors.toList())
                )
        );
    }

    /**
     * Aggregates a list of temperature data by day in a given period.
     *
     * @param sensorId sensor id
     * @param from from date
     * @param to to date
     * @return a list of aggregated temperature data per day
     */
    @GetMapping(value = "/sensor/{sensorId}/temperature/daily", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public AggregatedDailySensorData getAggregatedDailyTemperature(
            @PathVariable String sensorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate to
    ) {
        return sensorService.getAggregatedDailyTemperature(
                new GetAggregatedDailyTemperatureQuery(
                        sensorId,
                        from,
                        to
                )
        );
    }


    /**
     * Aggregates a list of temperature data by hour in a given period.
     *
     * @param sensorId sensor id
     * @param from from date
     * @param fromHourOfTheDay hour for the given from date
     * @param to to date
     * @param toHourOfTheDay hour for the given to date
     * @return a list of aggregated temperature data per hour
     */
    @GetMapping(value = "/sensor/{sensorId}/temperature/hourly", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public AggregatedHourlySensorData getAggregatedHourlyTemperature(
            @PathVariable String sensorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate from,
            @RequestParam(defaultValue = "0") @Min(0) @Max(23) Integer fromHourOfTheDay,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @NotNull LocalDate to,
            @RequestParam(defaultValue = "23") @Min(0) @Max(23) Integer toHourOfTheDay
    ) {
        return sensorService.getAggregatedHourlyTemperature(
                new GetAggregatedHourlyTemperatureQuery(
                        sensorId,
                        from.atTime(fromHourOfTheDay, 0),
                        to.atTime(toHourOfTheDay, 0)
                )
        );
    }

    private static TemperatureData requestToModel(TemperatureDataRequest temperatureDataRequest) {
        return new TemperatureData(temperatureDataRequest.getPointInTime(), temperatureDataRequest.getTemperature());
    }

}
