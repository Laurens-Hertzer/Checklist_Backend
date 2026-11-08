package ch.bbw.lah.checklist_backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record Weather(
        String location,
        LocalDate date,
        List<ForecastPeriod> periods) {

    public record ForecastPeriod(
            String period,
            LocalDateTime forecastAt,
            Double temperatureCelsius,
            Integer weatherCode,
            String weatherDescription,
            Integer precipitationProbabilityPercent,
            Double windSpeedKmh) {
    }
}
