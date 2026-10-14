package ch.bbw.lah.checklist_backend.service;

import ch.bbw.lah.checklist_backend.model.Setting;
import org.springframework.stereotype.Service;

import java.util.List;

public interface SettingService {
    Setting createSetting(Setting setting);

    Setting getSettingById(Long settingId);

    List<Setting> getAllSettings();

    Setting updateSetting(Setting setting);

    void deleteSetting(Long settingId);
}