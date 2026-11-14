package ch.bbw.lah.checklist_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "setting")
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    @Schema(description = "Database-generated setting ID", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long settingId;

    @Column(nullable = false, name = "train_starting")
    @NotBlank
    @Schema(description = "Default train departure location; must not be blank", example = "Zurich",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String trainStarting;

    @Column(nullable = false, name = "train_destination")
    @NotBlank
    @Schema(description = "Default train arrival location; must not be blank", example = "Winterthur",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String trainDestination;

    @Column(nullable = false, name = "weather_place")
    @NotBlank
    @Schema(description = "Location used for the weather forecast; must not be blank", example = "Zurich",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String weatherPlace;
}