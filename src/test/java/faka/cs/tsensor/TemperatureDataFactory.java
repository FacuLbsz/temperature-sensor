package faka.cs.tsensor;

import faka.cs.tsensor.application.domain.TemperatureData;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class TemperatureDataFactory {

    private final LocalDateTime now;
    private final LocalDateTime todayZeroHours;
    private final LocalDateTime todayOneHours;
    private final LocalDateTime yesterdayZeroHours;

    public TemperatureDataFactory(LocalDateTime now) {
        this.now = now;
        this.todayZeroHours = now.withHour(0);
        this.todayOneHours = todayZeroHours.plusHours(1);
        this.yesterdayZeroHours = todayZeroHours.minusDays(1);
    }

    public TemperatureData temperatureDataAtTodayZeroHoursOneMinute() {
        LocalDateTime todayZeroHoursOneMinute = todayZeroHours.withMinute(1);
        return new TemperatureData(todayZeroHoursOneMinute, 5.5D);
    }

    public TemperatureData temperatureDataAtTodayZeroHoursTwoMinutes() {
        LocalDateTime todayZeroHoursOneMinute = todayZeroHours.withMinute(2);
        return new TemperatureData(todayZeroHoursOneMinute, 5D);
    }

    public TemperatureData temperatureDataAtTodayOneHoursOneMinute() {
        LocalDateTime todayZeroHoursOneMinute = todayOneHours.withMinute(1);
        return new TemperatureData(todayZeroHoursOneMinute, 2D);
    }

    public TemperatureData temperatureDataAtTodayOneHoursTwoMinutes() {
        LocalDateTime todayOneHoursTwoMinutes = todayOneHours.withMinute(2);
        return new TemperatureData(todayOneHoursTwoMinutes, 5D);
    }

    public TemperatureData temperatureDataAtYesterdayZeroHoursOneMinute() {
        LocalDateTime yesterdayZeroHoursOneMinute = yesterdayZeroHours.withMinute(1);
        return new TemperatureData(yesterdayZeroHoursOneMinute, 3D);
    }

    public TemperatureData temperatureDataAtYesterdayOneHoursTwoMinutes() {
        LocalDateTime yesterdayOneHoursTwoMinutes = yesterdayZeroHours.plusHours(1).withMinute(2);
        return new TemperatureData(yesterdayOneHoursTwoMinutes, 4D);
    }
}
