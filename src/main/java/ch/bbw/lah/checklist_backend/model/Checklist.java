package ch.bbw.lah.checklist_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "checklist")
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    @Schema(description = "Database-generated checklist ID", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long checklistId;

    @Column(nullable = false, name = "task")
    @Schema(description = "Checklist task text", example = "Pack lunch",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String task;

    @Column(nullable = false, name = "completed")
    @Schema(description = "Whether the task is completed", example = "false")
    private boolean completed;

    @Column(name = "due_date")
    @Schema(description = "Optional due date in ISO format", example = "2026-11-07")
    private LocalDate dueDate;

    @JsonIgnore
    @Column(name = "created_date", nullable = false, updatable = false)
    @Schema(hidden = true)
    private LocalDate createdDate;

    @Column(name = "recurring_parent_id")
    @Schema(description = "ID of the recurring template for generated daily copies")
    private Long recurringParentId;

    @Min(1)
    @Column(name = "delete_after_days")
    @Schema(description = "Optional number of days after which the task is deleted; minimum 1", minimum = "1")
    private Integer deleteAfterDays;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "checklist_repeat_days", joinColumns = @JoinColumn(name = "checklist_id"))
    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Days of the week on which this task repeats")
    private Set<DayOfWeek> repeatDays = new HashSet<>();

    @PrePersist
    private void setCreatedDate() {
        if (createdDate == null) {
            createdDate = LocalDate.now();
        }
    }
}
