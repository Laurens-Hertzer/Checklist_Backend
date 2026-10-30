package ch.bbw.lah.checklist_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Long checklistId;

    @Column(nullable = false, name = "task")
    private String task;

    @Column(nullable = false, name = "completed")
    private boolean completed;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @JsonIgnore
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDate createdDate;

    @Column(name = "recurring_parent_id")
    private Long recurringParentId;

    @Min(1)
    @Column(name = "delete_after_days")
    private Integer deleteAfterDays;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "checklist_repeat_days", joinColumns = @JoinColumn(name = "checklist_id"))
    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> repeatDays = new HashSet<>();

    @PrePersist
    private void setCreatedDate() {
        if (createdDate == null) {
            createdDate = LocalDate.now();
        }
    }
}
