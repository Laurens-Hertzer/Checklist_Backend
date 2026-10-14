package ch.bbw.lah.checklist_backend.controller;

import ch.bbw.lah.checklist_backend.model.Checklist;
import ch.bbw.lah.checklist_backend.repository.ChecklistRepository;
import ch.bbw.lah.checklist_backend.service.ChecklistService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/checklist")
public class ChecklistController {
    private ChecklistService checklistService;
    private static final Logger logger = LoggerFactory.getLogger(ChecklistController.class);

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping("{id}")
    public ResponseEntity<Checklist> getChecklistById(@PathVariable("id") Long checklistId) {
        Checklist checklist = checklistService.getChecklistById(checklistId);
        return new ResponseEntity<>(checklist, HttpStatus.OK);
    }
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping
    public ResponseEntity<List<Checklist>> getAllChecklists () {
        List<Checklist> checklists = checklistService.getAllChecklists();
        if (checklists.isEmpty()) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(checklists, HttpStatus.OK);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping
    public ResponseEntity <Checklist> createChecklist (@Valid @RequestBody Checklist checklist) {

        Checklist savedChecklist = checklistService.createChecklist(checklist);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedChecklist);
    }
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PutMapping("{id}")
    public ResponseEntity<String> updateChecklist(@PathVariable("id") Long checklistId, @RequestBody Checklist checklist) {
        try {
            checklist.setChecklistId(checklistId);
            checklistService.updateChecklist(checklist);
            return ResponseEntity.ok("Checklist erfolgreich updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Checklist mit ID " + checklistId + "konnte nicht geupdated werden.");
        }
    }
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteChecklist(@PathVariable("id") Long checklistId) {

        try {
            checklistService.deleteChecklist(checklistId);
            return ResponseEntity.ok("Checklist erfolgreich gelöscht.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Checklist mit ID " + checklistId + " nicht gefunden.");
        }
    }
}
