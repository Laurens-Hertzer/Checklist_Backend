package ch.bbw.lah.checklist_backend.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "Checklist")
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "checklist_id")
    private Long checklistId;

    @Column(nullable = false, name="task")
    private String task;

    @Column(nullable = false, name = "completed")
    private boolean completed;

    @Column(name = "due_date")
    private LocalDate dueDate;
}
