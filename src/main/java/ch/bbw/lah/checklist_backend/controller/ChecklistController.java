package ch.bbw.lah.checklist_backend.controller;

import ch.bbw.lah.checklist_backend.model.Checklist;
import ch.bbw.lah.checklist_backend.service.ChecklistService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/checklist")
public class ChecklistController {
    private final ChecklistService checklistService;

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping("{id}")
    public ResponseEntity<Checklist> getChecklistById(@PathVariable("id") Long checklistId) {
        Checklist checklist = checklistService.getChecklistById(checklistId);
        return new ResponseEntity<>(checklist, HttpStatus.OK);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping
    public ResponseEntity<List<Checklist>> getAllChecklists(@RequestParam(required = false) LocalDate date) {
        if (date != null) {
            return ResponseEntity.ok(checklistService.getChecklistsForDate(date));
        }
        List<Checklist> checklists = checklistService.getAllChecklists();
        return ResponseEntity.ok(checklists);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PutMapping("{id}/completion")
    public ResponseEntity<Checklist> setChecklistCompletion(
            @PathVariable("id") Long checklistId,
            @RequestParam boolean completed) {
        return ResponseEntity.ok(checklistService.setChecklistCompletion(checklistId, completed));
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping
    public ResponseEntity<Checklist> createChecklist(@Valid @RequestBody Checklist checklist) {

        Checklist savedChecklist = checklistService.createChecklist(checklist);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedChecklist);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PutMapping("{id}")
    public ResponseEntity<String> updateChecklist(@PathVariable("id") Long checklistId, @RequestBody Checklist checklist) {
        checklist.setChecklistId(checklistId);
        checklistService.updateChecklist(checklist);
        return ResponseEntity.ok("Checklist erfolgreich updated");
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteChecklist(@PathVariable("id") Long checklistId) {
        checklistService.deleteChecklist(checklistId);
        return ResponseEntity.ok("Checklist erfolgreich gelöscht.");
    }
}
