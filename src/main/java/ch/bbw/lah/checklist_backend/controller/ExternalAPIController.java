package ch.bbw.lah.checklist_backend.controller;

import ch.bbw.lah.checklist_backend.model.Train;
import ch.bbw.lah.checklist_backend.model.Weather;
import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
public class ExternalAPIController {

    private static final DateTimeFormatter API_DATE_TIME =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssXX");
    private static final ZoneId WEATHER_TIME_ZONE = ZoneId.of("Europe/Zurich");
    private static final String WEATHER_HOURLY_FIELDS =
            "temperature_2m,weather_code,precipitation_probability,wind_speed_10m";
    private static final List<ForecastSlot> FORECAST_SLOTS = List.of(
            new ForecastSlot("morning", 7),
            new ForecastSlot("midday", 12),
            new ForecastSlot("evening", 18),
            new ForecastSlot("night", 22));

    private final String defaultFrom;
    private final String defaultTo;
    private final String weatherLocation;
    private final double weatherLatitude;
    private final double weatherLongitude;

    public ExternalAPIController(
            @Value("${FROM_DESTINATION}") String defaultFrom,
            @Value("${TO_DESTINATION}") String defaultTo,
            @Value("${WEATHER_LOCATION}") String weatherLocation,
            @Value("${WEATHER_LATITUDE}") double weatherLatitude,
            @Value("${WEATHER_LONGITUDE}") double weatherLongitude) {
        this.defaultFrom = defaultFrom;
        this.defaultTo = defaultTo;
        this.weatherLocation = weatherLocation;
        this.weatherLatitude = weatherLatitude;
        this.weatherLongitude = weatherLongitude;
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping({"/api/train"})
    public ResponseEntity<Train> getTrainConnections(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        JsonNode response = WebClient.create()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("transport.opendata.ch")
                        .path("/v1/connections")
                        .queryParam("from", from == null || from.isBlank() ? defaultFrom : from)
                        .queryParam("to", to == null || to.isBlank() ? defaultTo : to)
                        .queryParam("limit", 1)
                        .queryParam("transportations", "train")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        JsonNode connection = response.path("connections").path(0);
        if (connection.isMissingNode()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No train connection found");
        }

        JsonNode departure = connection.path("from");
        JsonNode arrival = connection.path("to");
        JsonNode departureStation = departure.path("station");
        JsonNode arrivalStation = arrival.path("station");
        JsonNode delay = departure.path("delay");
        String departureTime = departure.path("departure").asString(null);
        String expectedDepartureTime = departure.path("prognosis").path("departure").asString(null);
        Integer delayMinutes;
        if (departureTime != null && expectedDepartureTime != null) {
            delayMinutes = Math.toIntExact(Duration.between(
                    OffsetDateTime.parse(departureTime, API_DATE_TIME),
                    OffsetDateTime.parse(expectedDepartureTime, API_DATE_TIME)).toMinutes());
        } else {
            delayMinutes = delay.isMissingNode() || delay.isNull() ? null : delay.asInt();
        }

        Train result = new Train(
                departureStation.path("name").asString(null),
                arrivalStation.path("name").asString(null),
                departureTime,
                delayMinutes);

        return ResponseEntity.ok(result);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping({"/api/weather"})
    public ResponseEntity<Weather> getTomorrowWeatherForecast() {
        JsonNode response = WebClient.create()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.open-meteo.com")
                        .path("/v1/forecast")
                        .queryParam("latitude", weatherLatitude)
                        .queryParam("longitude", weatherLongitude)
                        .queryParam("hourly", WEATHER_HOURLY_FIELDS)
                        .queryParam("forecast_days", 3)
                        .queryParam("timezone", WEATHER_TIME_ZONE.getId())
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        LocalDate tomorrow = LocalDate.now(WEATHER_TIME_ZONE).plusDays(1);
        return ResponseEntity.ok(parseForecast(response, tomorrow));
    }

    Weather parseForecast(JsonNode response, LocalDate forecastDate) {
        JsonNode hourly = response == null ? null : response.path("hourly");
        JsonNode times = hourly == null ? null : hourly.path("time");
        if (times == null || !times.isArray()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Weather forecast is missing hourly data");
        }

        List<Weather.ForecastPeriod> periods = FORECAST_SLOTS.stream()
                .map(slot -> createForecastPeriod(hourly, times, forecastDate, slot))
                .toList();
        return new Weather(weatherLocation, forecastDate, periods);
    }

    private Weather.ForecastPeriod createForecastPeriod(
            JsonNode hourly, JsonNode times, LocalDate forecastDate, ForecastSlot slot) {
        int index = findForecastHour(times, forecastDate, slot.hour());
        Integer weatherCode = optionalInteger(forecastValue(hourly, "weather_code", index), "weather code");

        return new Weather.ForecastPeriod(
                slot.period(),
                parseForecastTime(times.get(index).asString()),
                optionalDouble(forecastValue(hourly, "temperature_2m", index), "temperature"),
                weatherCode,
                weatherDescription(weatherCode),
                optionalInteger(forecastValue(hourly, "precipitation_probability", index),
                        "precipitation probability"),
                optionalDouble(forecastValue(hourly, "wind_speed_10m", index), "wind speed"));
    }

    private int findForecastHour(JsonNode times, LocalDate forecastDate, int hour) {
        String forecastTime = forecastDate + "T%02d:00".formatted(hour);
        for (int index = 0; index < times.size(); index++) {
            if (forecastTime.equals(times.get(index).asString(null))) {
                return index;
            }
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY, "Weather forecast is missing " + forecastTime);
    }

    private JsonNode forecastValue(JsonNode hourly, String field, int index) {
        JsonNode values = hourly.path(field);
        if (!values.isArray() || values.size() <= index) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Weather forecast is missing " + field);
        }
        return values.get(index);
    }

    private LocalDateTime parseForecastTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Weather forecast has an invalid time");
        }
    }

    private Double optionalDouble(JsonNode value, String field) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isNumber()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Weather forecast has invalid " + field);
        }
        return value.asDouble();
    }

    private Integer optionalInteger(JsonNode value, String field) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isIntegralNumber()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Weather forecast has invalid " + field);
        }
        return value.asInt();
    }

    private String weatherDescription(Integer weatherCode) {
        if (weatherCode == null) {
            return null;
        }
        return switch (weatherCode) {
            case 0 -> "Klar";
            case 1 -> "Überwiegend klar";
            case 2 -> "Teilweise bewölkt";
            case 3 -> "Bedeckt";
            case 45, 48 -> "Nebel";
            case 51, 53, 55 -> "Nieselregen";
            case 56, 57 -> "Gefrierender Nieselregen";
            case 61, 63, 65 -> "Regen";
            case 66, 67 -> "Gefrierender Regen";
            case 71, 73, 75 -> "Schneefall";
            case 77 -> "Schneegriesel";
            case 80, 81, 82 -> "Regenschauer";
            case 85, 86 -> "Schneeschauer";
            case 95 -> "Gewitter";
            case 96, 99 -> "Gewitter mit Hagel";
            default -> "Unbekannt";
        };
    }

    private record ForecastSlot(String period, int hour) {
    }
}
