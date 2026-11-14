package ch.bbw.lah.checklist_backend.service;

import ch.bbw.lah.checklist_backend.model.Setting;
import ch.bbw.lah.checklist_backend.repository.SettingRepository;
import ch.bbw.lah.checklist_backend.service.impl.SettingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingServiceImplTest {
    @Mock
    private SettingRepository settingRepository;

    @Test
    void createSetting_savesSetting() {
        Setting setting = setting(4L, "Zurich");
        when(settingRepository.save(setting)).thenReturn(setting);

        assertSame(setting, new SettingServiceImpl(settingRepository).createSetting(setting));

        verify(settingRepository).save(setting);
    }

    @Test
    void getSettingById_returnsExistingSetting() {
        Setting setting = setting(4L, "Zurich");
        when(settingRepository.findById(4L)).thenReturn(Optional.of(setting));

        assertSame(setting, new SettingServiceImpl(settingRepository).getSettingById(4L));
    }

    @Test
    void getSettingById_returnsNotFoundForUnknownId() {
        when(settingRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new SettingServiceImpl(settingRepository).getSettingById(404L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getAllSettings_returnsRepositoryResults() {
        List<Setting> settings = List.of(setting(1L, "Zurich"), setting(2L, "Bern"));
        when(settingRepository.findAll()).thenReturn(settings);

        assertEquals(settings, new SettingServiceImpl(settingRepository).getAllSettings());
    }

    @Test
    void updateSetting_updatesExistingSetting() {
        Setting existing = setting(4L, "Oldtown");
        Setting update = setting(4L, "Zurich");
        when(settingRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(settingRepository.save(existing)).thenReturn(existing);

        Setting result = new SettingServiceImpl(settingRepository).updateSetting(update);

        assertSame(existing, result);
        assertEquals("Zurich", result.getTrainStarting());
        assertEquals("Winterthur", result.getTrainDestination());
        assertEquals("Zurich", result.getWeatherPlace());
        verify(settingRepository).save(existing);
    }

    @Test
    void updateSetting_returnsNotFoundForUnknownIdWithoutSaving() {
        Setting update = setting(404L, "Zurich");
        when(settingRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new SettingServiceImpl(settingRepository).updateSetting(update));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(settingRepository, never()).save(update);
    }

    @Test
    void deleteSetting_deletesExistingSetting() {
        when(settingRepository.existsById(4L)).thenReturn(true);

        new SettingServiceImpl(settingRepository).deleteSetting(4L);

        verify(settingRepository).deleteById(4L);
    }

    @Test
    void deleteSetting_returnsNotFoundForUnknownIdWithoutDeleting() {
        when(settingRepository.existsById(404L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new SettingServiceImpl(settingRepository).deleteSetting(404L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(settingRepository, never()).deleteById(404L);
    }

    private Setting setting(Long id, String departure) {
        Setting setting = new Setting();
        setting.setSettingId(id);
        setting.setTrainStarting(departure);
        setting.setTrainDestination("Winterthur");
        setting.setWeatherPlace("Zurich");
        return setting;
    }
}
