package ch.bbw.lah.checklist_backend.service.impl;

import ch.bbw.lah.checklist_backend.model.Checklist;
import ch.bbw.lah.checklist_backend.repository.ChecklistRepository;
import ch.bbw.lah.checklist_backend.service.ChecklistService;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.HttpStatus;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChecklistServiceImpl implements ChecklistService {
    private final ChecklistRepository checklistRepository;

    @Override
    public Checklist createChecklist(Checklist checklist) {
        validateDeleteAfterDays(checklist);
        Checklist savedChecklist = checklistRepository.save(checklist);
        createRecurringCopyIfScheduled(savedChecklist, LocalDate.now());
        return savedChecklist;
    }

    @Override
    public Checklist getChecklistById(Long checklistId) {
        return checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));
    }

    @Override
    @Transactional
    public List<Checklist> getAllChecklists() {
        deleteExpiredChecklists(LocalDate.now());
        return checklistRepository.findAll();
    }

    @Override
    @Transactional
    public List<Checklist> getChecklistsForDate(LocalDate date) {
        return checklistRepository.findAll().stream()
                .filter(checklist -> checklist.getRecurringParentId() != null
                        ? date.equals(checklist.getDueDate())
                        : (checklist.getRepeatDays() == null || checklist.getRepeatDays().isEmpty())
                        && (checklist.getDueDate() == null || date.equals(checklist.getDueDate())))
                .collect(Collectors.toList());
    }

    @Override
    public Checklist setChecklistCompletion(Long checklistId, boolean completed) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));
        checklist.setCompleted(completed);
        return checklistRepository.save(checklist);
    }

    private Checklist createRecurringCopy(Checklist template, LocalDate date) {
        Checklist copy = new Checklist();
        copy.setTask(template.getTask());
        copy.setDueDate(date);
        copy.setCreatedDate(date);
        copy.setCompleted(false);
        copy.setRecurringParentId(template.getChecklistId());
        copy.setDeleteAfterDays(template.getDeleteAfterDays());
        return checklistRepository.save(copy);
    }

    @Scheduled(cron = "0 0 0 * * *")
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void prepareTodaysChecklists() {
        LocalDate today = LocalDate.now();
        deleteExpiredChecklists(today);

        List<Checklist> recurringTemplates = checklistRepository.findAll().stream()
                .filter(checklist -> checklist.getRecurringParentId() == null
                        && checklist.getRepeatDays() != null
                        && !checklist.getRepeatDays().isEmpty()
                        && checklist.getRepeatDays().contains(today.getDayOfWeek()))
                .collect(Collectors.toList());

        for (Checklist template : recurringTemplates) {
            createRecurringCopyIfScheduled(template, today);
        }
    }

    private void createRecurringCopyIfScheduled(Checklist template, LocalDate date) {
        if (template.getRecurringParentId() != null
                || template.getRepeatDays() == null
                || !template.getRepeatDays().contains(date.getDayOfWeek())) {
            return;
        }

        checklistRepository.findByRecurringParentIdAndDueDate(template.getChecklistId(), date)
                .orElseGet(() -> createRecurringCopy(template, date));
    }

    private void deleteExpiredChecklists(LocalDate today) {
        List<Checklist> expiredChecklists = checklistRepository.findAll().stream()
                .filter(checklist -> checklist.getRecurringParentId() != null
                        || checklist.getRepeatDays() == null
                        || checklist.getRepeatDays().isEmpty())
                .filter(checklist -> deletionStartDate(checklist) != null
                        && isExpiredOn(deletionStartDate(checklist), checklist.getDeleteAfterDays(), today))
                .collect(Collectors.toList());

        checklistRepository.deleteAll(expiredChecklists);
    }

    private boolean isExpiredOn(LocalDate startDate, Integer deleteAfterDays, LocalDate today) {
        return deleteAfterDays != null
                && ChronoUnit.DAYS.between(startDate, today) >= deleteAfterDays;
    }

    private LocalDate deletionStartDate(Checklist checklist) {
        return checklist.getRecurringParentId() != null
                ? checklist.getDueDate()
                : checklist.getCreatedDate();
    }

    private void validateDeleteAfterDays(Checklist checklist) {
        if (checklist.getDeleteAfterDays() != null && checklist.getDeleteAfterDays() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "deleteAfterDays must be at least 1");
        }
    }

    @Override
    public Checklist updateChecklist(Checklist checklist) {
        validateDeleteAfterDays(checklist);
        Checklist existingChecklist = checklistRepository.findById(checklist.getChecklistId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));
        existingChecklist.setTask(checklist.getTask());
        existingChecklist.setCompleted(checklist.getCompleted());
        existingChecklist.setDueDate(checklist.getDueDate());
        existingChecklist.setDeleteAfterDays(checklist.getDeleteAfterDays());
        existingChecklist.setRepeatDays(checklist.getRepeatDays());
        Checklist savedChecklist = checklistRepository.save(existingChecklist);
        createRecurringCopyIfScheduled(savedChecklist, LocalDate.now());
        return savedChecklist;
    }

    @Override
    public void deleteChecklist(Long checklistId) {
        if (!checklistRepository.existsById(checklistId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found");
        }
        checklistRepository.deleteById(checklistId);
    }
}