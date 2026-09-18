package ch.bbw.lah.checklist_backend.service;

import ch.bbw.lah.checklist_backend.model.Checklist;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ChecklistService {
    Checklist createChecklist(Checklist checklist);

    Checklist getChecklistById(Long checklistId);

    List<Checklist> getAllChecklists();

    Checklist updateChecklist(Checklist checklist);

    boolean deleteChecklist(Long checklistId);
}