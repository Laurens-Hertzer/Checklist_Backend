package ch.bbw.lah.checklist_backend.model;

import jakarta.persistence.*;
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
    private Long settingId;

    @Column(nullable = false, name = "train_starting")
    private String trainStarting;

    @Column(nullable = false, name = "train_destination")
    private String trainDestination;

    @Column(nullable = false, name = "weather_place")
    private String weatherPlace;
}