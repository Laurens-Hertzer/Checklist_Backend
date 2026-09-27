package ch.bbw.lah.checklist_backend.repository;

import ch.bbw.lah.checklist_backend.model.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
    Checklist findByChecklistId(Long checklistId);

//    Checklist createChecklist(Checklist checklist);
//
//    Checklist getAllChecklists();
//
//    Checklist updateChecklist(Checklist checklist);
//
//    Checklist deleteChecklist(Checklist checklist);
}
