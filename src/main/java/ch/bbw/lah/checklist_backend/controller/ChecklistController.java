package ch.bbw.lah.checklist_backend.controller;

import ch.bbw.lah.checklist_backend.model.Checklist;
import ch.bbw.lah.checklist_backend.service.ChecklistService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
}
