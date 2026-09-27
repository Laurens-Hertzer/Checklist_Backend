package ch.bbw.lah.checklist_backend.service.impl;

import ch.bbw.lah.checklist_backend.model.Checklist;
import ch.bbw.lah.checklist_backend.repository.ChecklistRepository;
import ch.bbw.lah.checklist_backend.service.ChecklistService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ChecklistServiceImpl implements ChecklistService {
    private final ChecklistRepository checklistRepository;

    @Override
    public Checklist createChecklist(Checklist checklist) {
        return checklistRepository.save(checklist);
    }

    @Override
    public Checklist getChecklistById(Long checklistId) {
        return checklistRepository.findByChecklistId(checklistId);
    }

    @Override
    public List<Checklist> getAllChecklists() {
        return checklistRepository.findAll();
    }

    @Override
    public Checklist updateChecklist(Checklist checklist) {
        return checklistRepository.save(checklist);
    }

    @Override
    public void deleteChecklist(Long checklistId) {
        checklistRepository.deleteById(checklistId);
    }
}