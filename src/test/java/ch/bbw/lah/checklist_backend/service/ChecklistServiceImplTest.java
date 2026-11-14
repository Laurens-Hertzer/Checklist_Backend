package ch.bbw.lah.checklist_backend.service;

import ch.bbw.lah.checklist_backend.model.Checklist;
import ch.bbw.lah.checklist_backend.repository.ChecklistRepository;
import ch.bbw.lah.checklist_backend.service.impl.ChecklistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceImplTest {
    @Mock
    private ChecklistRepository checklistRepository;

    private ChecklistServiceImpl checklistService;

    @BeforeEach
    void setUp() {
        checklistService = new ChecklistServiceImpl(checklistRepository);
    }

    @Test
    void createChecklist_acceptsNullDeletionPeriod() {
        Checklist checklist = checklist("Ohne automatische Löschung");
        when(checklistRepository.save(checklist)).thenReturn(checklist);

        assertSame(checklist, checklistService.createChecklist(checklist));

        verify(checklistRepository).save(checklist);
    }

    @Test
    void createChecklist_acceptsDeletionPeriodOfOneDay() {
        Checklist checklist = checklist("Nach einem Tag löschen");
        checklist.setDeleteAfterDays(1);
        when(checklistRepository.save(checklist)).thenReturn(checklist);

        assertSame(checklist, checklistService.createChecklist(checklist));

        verify(checklistRepository).save(checklist);
    }

    @Test
    void createChecklist_rejectsZeroOrNegativeDeletionPeriodWithoutSaving() {
        for (int deletionPeriod : List.of(0, -1)) {
            Checklist checklist = checklist("Ungültige Löschfrist");
            checklist.setDeleteAfterDays(deletionPeriod);

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> checklistService.createChecklist(checklist));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }

        verify(checklistRepository, never()).save(any());
    }

    @Test
    void createChecklist_createsTodaysRecurringCopyForMatchingWeekday() {
        LocalDate today = LocalDate.now();
        Checklist template = recurringTemplate("Vorlage", today.getDayOfWeek());
        template.setChecklistId(12L);
        template.setDeleteAfterDays(3);
        when(checklistRepository.save(template)).thenReturn(template);
        when(checklistRepository.findByRecurringParentIdAndDueDate(12L, today))
                .thenReturn(Optional.empty());

        checklistService.createChecklist(template);

        ArgumentCaptor<Checklist> captor = ArgumentCaptor.forClass(Checklist.class);
        verify(checklistRepository, times(2)).save(captor.capture());
        Checklist copy = captor.getAllValues().get(1);
        assertEquals("Vorlage", copy.getTask());
        assertEquals(today, copy.getDueDate());
        assertEquals(today, copy.getCreatedDate());
        assertEquals(3, copy.getDeleteAfterDays());
        assertEquals(Boolean.FALSE, copy.getCompleted());
        assertEquals(12L, copy.getRecurringParentId());
    }

    @Test
    void getChecklistById_returnsChecklistWhenFound() {
        Checklist checklist = checklist("Gefunden");
        when(checklistRepository.findById(7L)).thenReturn(Optional.of(checklist));

        assertSame(checklist, checklistService.getChecklistById(7L));
    }

    @Test
    void getChecklistById_returnsNotFoundWhenMissing() {
        when(checklistRepository.findById(7L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> checklistService.getChecklistById(7L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getAllChecklists_deletesExpiredOneOffAndReturnsRemainingChecklists() {
        LocalDate today = LocalDate.now();
        Checklist expired = checklist("Abgelaufen");
        expired.setCreatedDate(today.minusDays(2));
        expired.setDeleteAfterDays(2);
        Checklist notYetExpired = checklist("Noch nicht abgelaufen");
        notYetExpired.setCreatedDate(today.minusDays(1));
        notYetExpired.setDeleteAfterDays(2);
        Checklist withoutDeletionPeriod = checklist("Bleibt erhalten");
        Checklist template = recurringTemplate("Vorlage", today.getDayOfWeek());
        List<Checklist> remaining = List.of(notYetExpired, withoutDeletionPeriod, template);
        when(checklistRepository.findAll()).thenReturn(
                List.of(expired, notYetExpired, withoutDeletionPeriod, template), remaining);

        List<Checklist> result = checklistService.getAllChecklists();

        assertEquals(remaining, result);
        verify(checklistRepository).deleteAll(List.of(expired));
    }

    @Test
    void getAllChecklists_usesCreatedDateAndKeepsChecklistUntilBoundaryDay() {
        LocalDate today = LocalDate.now();
        Checklist beforeBoundary = checklist("Noch nicht abgelaufen");
        beforeBoundary.setCreatedDate(today.minusDays(1));
        beforeBoundary.setDeleteAfterDays(2);
        Checklist onBoundary = checklist("Am Grenztag");
        onBoundary.setCreatedDate(today.minusDays(2));
        onBoundary.setDeleteAfterDays(2);
        when(checklistRepository.findAll()).thenReturn(
                List.of(beforeBoundary, onBoundary), List.of(beforeBoundary));

        List<Checklist> result = checklistService.getAllChecklists();

        assertEquals(List.of(beforeBoundary), result);
        verify(checklistRepository).deleteAll(List.of(onBoundary));
    }

    @Test
    void getAllChecklists_usesDueDateForGeneratedCopyAndNeverDeletesTemplate() {
        LocalDate today = LocalDate.now();
        Checklist expiredCopy = checklist("Abgelaufene Kopie");
        expiredCopy.setRecurringParentId(5L);
        expiredCopy.setDueDate(today.minusDays(2));
        expiredCopy.setDeleteAfterDays(2);
        Checklist template = recurringTemplate("Vorlage", today.getDayOfWeek());
        template.setChecklistId(5L);
        template.setCreatedDate(today.minusDays(50));
        template.setDeleteAfterDays(1);
        when(checklistRepository.findAll()).thenReturn(List.of(expiredCopy, template), List.of(template));

        List<Checklist> result = checklistService.getAllChecklists();

        assertEquals(List.of(template), result);
        verify(checklistRepository).deleteAll(List.of(expiredCopy));
    }

    @Test
    void getChecklistsForDate_returnsOneOffAndMatchingCopiesButNotTemplates() {
        LocalDate date = LocalDate.of(2026, 11, 14);
        Checklist matchingOneOff = checklist("Am Datum");
        matchingOneOff.setDueDate(date);
        Checklist otherOneOff = checklist("Anderes Datum");
        otherOneOff.setDueDate(date.minusDays(1));
        Checklist withoutDueDate = checklist("Ohne Datum");
        Checklist template = recurringTemplate("Wiederholungsvorlage", date.getDayOfWeek());
        Checklist matchingCopy = checklist("Kopie heute");
        matchingCopy.setRecurringParentId(8L);
        matchingCopy.setDueDate(date);
        Checklist otherCopy = checklist("Kopie anderes Datum");
        otherCopy.setRecurringParentId(8L);
        otherCopy.setDueDate(date.plusDays(1));
        when(checklistRepository.findAll()).thenReturn(List.of(
                matchingOneOff, otherOneOff, withoutDueDate, template, matchingCopy, otherCopy));

        assertEquals(List.of(matchingOneOff, withoutDueDate, matchingCopy),
                checklistService.getChecklistsForDate(date));
    }

    @Test
    void getChecklistsForDate_returnsEmptyListWhenNothingMatches() {
        when(checklistRepository.findAll()).thenReturn(List.of());

        assertTrue(checklistService.getChecklistsForDate(LocalDate.now()).isEmpty());
    }

    @Test
    void setChecklistCompletion_updatesAndSavesChecklist() {
        Checklist existing = checklist("Status ändern");
        existing.setChecklistId(42L);
        when(checklistRepository.findById(42L)).thenReturn(Optional.of(existing));
        when(checklistRepository.save(existing)).thenReturn(existing);

        Checklist result = checklistService.setChecklistCompletion(42L, true);

        assertSame(existing, result);
        assertEquals(Boolean.TRUE, result.getCompleted());
        verify(checklistRepository).save(existing);
    }

    @Test
    void setChecklistCompletion_throwsNotFoundWhenChecklistDoesNotExist() {
        when(checklistRepository.findById(42L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> checklistService.setChecklistCompletion(42L, true));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(checklistRepository, never()).save(any());
    }

    @Test
    void prepareTodaysChecklists_createsCopyForTodayAndCopiesTemplateFields() {
        LocalDate today = LocalDate.now();
        Checklist template = recurringTemplate("Heute wiederholen", today.getDayOfWeek());
        template.setChecklistId(17L);
        template.setDeleteAfterDays(4);
        when(checklistRepository.findAll()).thenReturn(List.of(template), List.of(template));
        when(checklistRepository.findByRecurringParentIdAndDueDate(17L, today))
                .thenReturn(Optional.empty());

        checklistService.prepareTodaysChecklists();

        ArgumentCaptor<Checklist> captor = ArgumentCaptor.forClass(Checklist.class);
        verify(checklistRepository).save(captor.capture());
        Checklist copy = captor.getValue();
        assertEquals(today, copy.getDueDate());
        assertEquals("Heute wiederholen", copy.getTask());
        assertEquals(4, copy.getDeleteAfterDays());
        assertEquals(Boolean.FALSE, copy.getCompleted());
        assertEquals(17L, copy.getRecurringParentId());
    }

    @Test
    void prepareTodaysChecklists_skipsTemplateForAnotherWeekday() {
        DayOfWeek anotherDay = LocalDate.now().getDayOfWeek().plus(1);
        Checklist template = recurringTemplate("Nicht heute", anotherDay);
        when(checklistRepository.findAll()).thenReturn(List.of(template), List.of(template));

        checklistService.prepareTodaysChecklists();

        verify(checklistRepository, never()).save(any());
        verify(checklistRepository, never()).findByRecurringParentIdAndDueDate(any(), any());
    }

    @Test
    void prepareTodaysChecklists_doesNotCreateDuplicateWhenCalledAgain() {
        LocalDate today = LocalDate.now();
        Checklist template = recurringTemplate("Nur eine Kopie", today.getDayOfWeek());
        template.setChecklistId(19L);
        Checklist existingCopy = checklist("Nur eine Kopie");
        existingCopy.setRecurringParentId(19L);
        existingCopy.setDueDate(today);
        when(checklistRepository.findAll()).thenReturn(
                List.of(template), List.of(template), List.of(template), List.of(template));
        when(checklistRepository.findByRecurringParentIdAndDueDate(19L, today))
                .thenReturn(Optional.empty(), Optional.of(existingCopy));

        checklistService.prepareTodaysChecklists();
        checklistService.prepareTodaysChecklists();

        verify(checklistRepository).save(any(Checklist.class));
    }

    @Test
    void prepareTodaysChecklists_deletesExpiredChecklistsBeforeCreatingCopies() {
        LocalDate today = LocalDate.now();
        Checklist expired = checklist("Abgelaufen");
        expired.setCreatedDate(today.minusDays(1));
        expired.setDeleteAfterDays(1);
        Checklist template = recurringTemplate("Heute wiederholen", today.getDayOfWeek());
        template.setChecklistId(21L);
        when(checklistRepository.findAll()).thenReturn(List.of(expired, template), List.of(expired, template));
        when(checklistRepository.findByRecurringParentIdAndDueDate(21L, today))
                .thenReturn(Optional.empty());

        checklistService.prepareTodaysChecklists();

        verify(checklistRepository).deleteAll(List.of(expired));
        verify(checklistRepository).save(any(Checklist.class));
    }

    @Test
    void updateChecklist_updatesFieldsAndCreatesTodaysRecurringCopy() {
        LocalDate today = LocalDate.now();
        Checklist existing = checklist("Alter Task");
        existing.setChecklistId(25L);
        Checklist update = recurringTemplate("Neuer Task", today.getDayOfWeek());
        update.setChecklistId(25L);
        update.setCompleted(true);
        update.setDueDate(today.plusDays(2));
        update.setDeleteAfterDays(5);
        when(checklistRepository.findById(25L)).thenReturn(Optional.of(existing));
        when(checklistRepository.save(existing)).thenReturn(existing);
        when(checklistRepository.findByRecurringParentIdAndDueDate(25L, today))
                .thenReturn(Optional.empty());

        Checklist result = checklistService.updateChecklist(update);

        assertSame(existing, result);
        assertEquals("Neuer Task", existing.getTask());
        assertEquals(Boolean.TRUE, existing.getCompleted());
        assertEquals(today.plusDays(2), existing.getDueDate());
        assertEquals(5, existing.getDeleteAfterDays());
        assertEquals(Set.of(today.getDayOfWeek()), existing.getRepeatDays());
        verify(checklistRepository, times(2)).save(any(Checklist.class));
    }

    @Test
    void updateChecklist_rejectsInvalidDeletionPeriodWithoutSaving() {
        Checklist update = checklist("Ungültige Änderung");
        update.setChecklistId(25L);
        update.setDeleteAfterDays(-1);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> checklistService.updateChecklist(update));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(checklistRepository, never()).save(any());
        verify(checklistRepository, never()).findById(any());
    }

    @Test
    void updateChecklist_returnsNotFoundForUnknownId() {
        Checklist update = checklist("Nicht gefunden");
        update.setChecklistId(25L);
        when(checklistRepository.findById(25L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> checklistService.updateChecklist(update));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(checklistRepository, never()).save(any());
    }

    @Test
    void deleteChecklist_deletesExistingChecklist() {
        when(checklistRepository.existsById(30L)).thenReturn(true);

        checklistService.deleteChecklist(30L);

        verify(checklistRepository).deleteById(30L);
    }

    @Test
    void deleteChecklist_returnsNotFoundForUnknownId() {
        when(checklistRepository.existsById(30L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> checklistService.deleteChecklist(30L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(checklistRepository, never()).deleteById(any());
    }

    private Checklist checklist(String task) {
        Checklist checklist = new Checklist();
        checklist.setTask(task);
        checklist.setCreatedDate(LocalDate.now());
        return checklist;
    }

    private Checklist recurringTemplate(String task, DayOfWeek day) {
        Checklist template = checklist(task);
        template.setRepeatDays(Set.of(day));
        return template;
    }
}
