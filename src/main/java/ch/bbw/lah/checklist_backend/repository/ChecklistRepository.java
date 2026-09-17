package ch.bbw.lah.checklist_backend.repository;

import ch.bbw.lah.checklist_backend.model.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
    List<Checklist> findByChecklistId(Long checklistId);
}
