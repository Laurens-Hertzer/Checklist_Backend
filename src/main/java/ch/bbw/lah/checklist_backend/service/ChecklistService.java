package ch.bbw.lah.checklist_backend.service;

import ch.bbw.lah.checklist_backend.model.Checklist;

import java.time.LocalDate;
import java.util.List;

public interface ChecklistService {
    Checklist createChecklist(Checklist checklist);

    Checklist getChecklistById(Long checklistId);

    List<Checklist> getAllChecklists();

    List<Checklist> getChecklistsForDate(LocalDate date);

    Checklist setChecklistCompletion(Long checklistId, boolean completed);

    Checklist updateChecklist(Checklist checklist);

    void deleteChecklist(Long checklistId);
}