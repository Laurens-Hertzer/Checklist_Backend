package ch.bbw.lah.checklist_backend.controller;

import ch.bbw.lah.checklist_backend.model.Setting;
import ch.bbw.lah.checklist_backend.repository.SettingRepository;
import ch.bbw.lah.checklist_backend.service.SettingService;
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
@RequestMapping("api/setting")
public class SettingController {
    private SettingService settingService;
    private static final Logger logger = LoggerFactory.getLogger(SettingController.class);

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping("{id}")
    public ResponseEntity<Setting> getSettingById(@PathVariable("id") Long settingId) {
        Setting setting = settingService.getSettingById(settingId);
        return new ResponseEntity<>(setting, HttpStatus.OK);
    }
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @GetMapping
    public ResponseEntity<List<Setting>> getAllSettings () {
        List<Setting> settings = settingService.getAllSettings();
        if (settings.isEmpty()) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PostMapping
    public ResponseEntity <Setting> createSetting (@Valid @RequestBody Setting setting) {

        Setting savedSetting = settingService.createSetting(setting);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedSetting);
    }
    @CrossOrigin(origins = "${CROSS_ORIGIN}")
    @PutMapping("{id}")
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
