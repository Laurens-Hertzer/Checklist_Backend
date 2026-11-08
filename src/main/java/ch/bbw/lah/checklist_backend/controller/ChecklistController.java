package ch.bbw.lah.checklist_backend.controller;

import ch.bbw.lah.checklist_backend.model.Checklist;
import ch.bbw.lah.checklist_backend.service.ChecklistService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/checklist")
@Tag(name = "Checklists", description = "Create, retrieve, update, and delete checklist tasks.")
public class ChecklistController {
    private final ChecklistService checklistService;

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping("{id}")
    @Operation(summary = "Get a checklist by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checklist found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Checklist.class))),
            @ApiResponse(responseCode = "400", description = "Checklist ID is not a number",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Checklist not found",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Checklist> getChecklistById(
            @Parameter(description = "Checklist ID", example = "1")
            @PathVariable("id") Long checklistId) {
        Checklist checklist = checklistService.getChecklistById(checklistId);
        return new ResponseEntity<>(checklist, HttpStatus.OK);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping
    @Operation(summary = "Get all checklists")
    @ApiResponse(responseCode = "200", description = "List of checklists; an empty list is returned when none exist",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Checklist.class))))
    public ResponseEntity<List<Checklist>> getAllChecklists() {
        List<Checklist> checklists = checklistService.getAllChecklists();
        return ResponseEntity.ok(checklists);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping("date")
    @Operation(summary = "Get checklists for a date")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching checklists",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Checklist.class)))),
            @ApiResponse(responseCode = "400", description = "Date is missing or has an invalid format",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "No checklists found for the date", content = @Content)
    })
    public ResponseEntity<List<Checklist>> getChecklistsByDate(
            @Parameter(description = "Date in ISO format (yyyy-MM-dd)", example = "2026-11-07", required = true)
            @RequestParam(required = true) LocalDate date) {
        List<Checklist> checklists = checklistService.getChecklistsForDate(date);

        if (checklists.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(checklists);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PutMapping("{id}/completion")
    @Operation(summary = "Set checklist completion status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Completion status updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Checklist.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ID or completion parameter",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Checklist not found",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Checklist> setChecklistCompletion(
            @Parameter(description = "Checklist ID", example = "1")
            @PathVariable("id") Long checklistId,
            @Parameter(description = "Whether the task is completed", example = "true", required = true)
            @RequestParam boolean completed) {
        return ResponseEntity.ok(checklistService.setChecklistCompletion(checklistId, completed));
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping
    @Operation(summary = "Create a checklist")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Checklist created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Checklist.class))),
            @ApiResponse(responseCode = "400", description = "Invalid checklist data",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Checklist> createChecklist(@Valid @RequestBody Checklist checklist) {

        Checklist savedChecklist = checklistService.createChecklist(checklist);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedChecklist);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PutMapping("{id}")
    @Operation(summary = "Update a checklist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checklist updated",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Checklist erfolgreich updated"))),
            @ApiResponse(responseCode = "400", description = "Invalid ID or checklist data",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Checklist not found",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<String> updateChecklist(@PathVariable("id") Long checklistId, @RequestBody Checklist checklist) {
        checklist.setChecklistId(checklistId);
        checklistService.updateChecklist(checklist);
        return ResponseEntity.ok("Checklist erfolgreich updated");
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @DeleteMapping("{id}")
    @Operation(summary = "Delete a checklist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checklist deleted",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Checklist erfolgreich gelöscht."))),
            @ApiResponse(responseCode = "400", description = "Checklist ID is not a number",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Checklist not found",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<String> deleteChecklist(@PathVariable("id") Long checklistId) {
        checklistService.deleteChecklist(checklistId);
        return ResponseEntity.ok("Checklist erfolgreich gelöscht.");
    }
}
