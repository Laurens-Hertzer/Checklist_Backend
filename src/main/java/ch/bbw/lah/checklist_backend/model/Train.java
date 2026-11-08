package ch.bbw.lah.checklist_backend.model;

public record Train(
        String departureStation,
        String arrivalStation,
        String departureTime,
        Integer delay) {
}
