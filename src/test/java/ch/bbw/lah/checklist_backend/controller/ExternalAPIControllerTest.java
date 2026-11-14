package ch.bbw.lah.checklist_backend.controller;

import ch.bbw.lah.checklist_backend.model.Train;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExternalAPIControllerTest {
    private static final LocalDate FORECAST_DATE = LocalDate.of(2026, 11, 13);

    @Test
    void parsesForecastIntoFourDayPeriods() {
        var controller = new ExternalAPIController(
                "Zurich", "Winterthur", "Zurich, Switzerland", 47.3769, 8.5417);
        LocalDate date = FORECAST_DATE;
        String forecastJson = """
                {
                  "hourly": {
                    "time": ["%sT07:00", "%sT12:00", "%sT18:00", "%sT22:00"],
                    "temperature_2m": [8.5, 12.0, 9.0, 7.0],
                    "weather_code": [2, 3, 61, 2],
                    "precipitation_probability": [10, 20, 70, 30],
                    "wind_speed_10m": [5.0, 8.0, 12.0, 6.0]
                  }
                }
                """.formatted(date, date, date, date);
        var response = new ObjectMapper().readTree(forecastJson);

        var forecast = controller.parseForecast(response, date);

        assertEquals("Zurich, Switzerland", forecast.location());
        assertEquals(date, forecast.date());
        assertEquals(4, forecast.periods().size());
        assertEquals("morning", forecast.periods().get(0).period());
        assertEquals(8.5, forecast.periods().get(0).temperatureCelsius());
        assertEquals("Teilweise bewölkt", forecast.periods().get(0).weatherDescription());
        assertEquals("night", forecast.periods().get(3).period());
    }

    @Test
    void rejectsForecastWithMissingHourlyData() {
        var controller = new ExternalAPIController(
                "Zurich", "Winterthur", "Zurich, Switzerland", 47.3769, 8.5417);

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.parseForecast(
                        new ObjectMapper().createObjectNode(), FORECAST_DATE));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }

    @Test
    void parsesTrainConnection() {
        var controller = new ExternalAPIController(
                "Zurich", "Winterthur", "Zurich, Switzerland", 47.3769, 8.5417);
        var response = new ObjectMapper().readTree("""
                {
                  "connections": [{
                    "from": {
                      "station": {"name": "Zürich HB"},
                      "departure": "2026-11-13T15:00:00+0100",
                      "prognosis": {"departure": "2026-11-13T15:05:00+0100"},
                      "delay": 5
                    },
                    "to": {"station": {"name": "Winterthur"}}
                  }]
                }
                """);

        Train train = controller.parseTrainResponse(response);

        assertEquals("Zürich HB", train.departureStation());
        assertEquals("Winterthur", train.arrivalStation());
        assertEquals("2026-11-13T15:00:00+0100", train.departureTime());
        assertEquals(5, train.delay());
    }

    @Test
    void rejectsTrainResponseWithoutConnections() {
        var controller = new ExternalAPIController(
                "Zurich", "Winterthur", "Zurich, Switzerland", 47.3769, 8.5417);
        var response = new ObjectMapper().readTree("""
                {"connections":[]}
                """);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.parseTrainResponse(response));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
