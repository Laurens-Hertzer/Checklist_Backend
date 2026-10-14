package ch.bbw.lah.checklist_backend.service.impl;

import ch.bbw.lah.checklist_backend.model.Setting;
import ch.bbw.lah.checklist_backend.repository.SettingRepository;
import ch.bbw.lah.checklist_backend.service.SettingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SettingServiceImpl implements SettingService {
    private final SettingRepository settingRepository;

    @Override
    public Setting createSetting(Setting setting) {
        return settingRepository.save(setting);
    }

    @Override
    public Setting getSettingById(Long settingId) {
        return settingRepository.findById(settingId)
                .orElseThrow(() -> new RuntimeException("Setting mit ID " + settingId + " nicht gefunden!"));
    }

    @Override
    public List<Setting> getAllSettings() {
        return settingRepository.findAll();
    }

    @Override
    public Setting updateSetting(Setting setting) {
        return settingRepository.save(setting);
    }

    @Override
    public void deleteSetting(Long settingId) {
        settingRepository.deleteById(settingId);
    }
}