package ch.bbw.lah.checklist_backend.controller;

import ch.bbw.lah.checklist_backend.model.Setting;
import ch.bbw.lah.checklist_backend.repository.SettingRepository;
import ch.bbw.lah.checklist_backend.service.SettingService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/setting")
@Tag(name = "Settings", description = "Manage journey and weather location settings.")
public class SettingController {
    private SettingService settingService;
    private static final Logger logger = LoggerFactory.getLogger(SettingController.class);

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping("{id}")
    @Operation(summary = "Get a setting by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Setting found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Setting.class))),
            @ApiResponse(responseCode = "400", description = "Setting ID is not a number",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Setting not found",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Setting> getSettingById(
            @Parameter(description = "Setting ID", example = "1")
            @PathVariable("id") Long settingId) {
        Setting setting = settingService.getSettingById(settingId);
        return new ResponseEntity<>(setting, HttpStatus.OK);
    }
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping
    @Operation(summary = "Get all settings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Settings found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Setting.class)))),
            @ApiResponse(responseCode = "404", description = "No settings exist", content = @Content)
    })
    public ResponseEntity<List<Setting>> getAllSettings () {
        List<Setting> settings = settingService.getAllSettings();
        if (settings.isEmpty()) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping
    @Operation(summary = "Create a setting")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Setting created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Setting.class))),
            @ApiResponse(responseCode = "400", description = "Request body is missing or malformed",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity <Setting> createSetting (@Valid @RequestBody Setting setting) {

        Setting savedSetting = settingService.createSetting(setting);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedSetting);
    }
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PutMapping("{id}")
    @Operation(summary = "Update a setting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Setting updated",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Setting erfolgreich updated"))),
            @ApiResponse(responseCode = "400", description = "Setting ID or request body is invalid",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "An exception occurred while updating the setting",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(type = "string",
                                    example = "Setting mit ID 1konnte nicht geupdated werden.")))
    })
    public ResponseEntity<String> updateSetting(@PathVariable("id") Long settingId, @RequestBody Setting setting) {
        try {
            setting.setSettingId(settingId);
            settingService.updateSetting(setting);
            return ResponseEntity.ok("Setting erfolgreich updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Setting mit ID " + settingId + "konnte nicht geupdated werden.");
        }
    }
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @DeleteMapping("{id}")
    @Operation(summary = "Delete a setting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deletion request completed; a missing ID is ignored",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Setting erfolgreich gelöscht."))),
            @ApiResponse(responseCode = "400", description = "Setting ID is not a number",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<String> deleteSetting(@PathVariable("id") Long settingId) {

        try {
            settingService.deleteSetting(settingId);
            return ResponseEntity.ok("Setting erfolgreich gelöscht.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Setting mit ID " + settingId + " nicht gefunden.");
        }
    }
}
