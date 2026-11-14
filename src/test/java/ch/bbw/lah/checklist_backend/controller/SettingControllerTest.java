package ch.bbw.lah.checklist_backend.controller;

import ch.bbw.lah.checklist_backend.model.Setting;
import ch.bbw.lah.checklist_backend.service.SettingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettingController.class)
class SettingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SettingService settingService;

    @Test
    void getAllSettings_returnsNotFoundWhenListIsEmpty() throws Exception {
        when(settingService.getAllSettings()).thenReturn(List.of());

        mockMvc.perform(get("/api/setting"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSettingById_returnsSetting() throws Exception {
        when(settingService.getSettingById(4L)).thenReturn(setting(4L));

        mockMvc.perform(get("/api/setting/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settingId").value(4))
                .andExpect(jsonPath("$.trainStarting").value("Zurich"));
    }

    @Test
    void getSettingById_returnsNotFoundWhenMissing() throws Exception {
        when(settingService.getSettingById(404L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Setting not found"));

        mockMvc.perform(get("/api/setting/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSettingById_returnsBadRequestForNonNumericId() throws Exception {
        mockMvc.perform(get("/api/setting/not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSetting_returnsCreatedSetting() throws Exception {
        when(settingService.createSetting(any(Setting.class))).thenReturn(setting(8L));

        mockMvc.perform(post("/api/setting")
                        .contentType("application/json")
                        .content(validSettingJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.settingId").value(8))
                .andExpect(jsonPath("$.trainDestination").value("Winterthur"));
    }

    @Test
    void createSetting_returnsBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/setting")
                        .contentType("application/json")
                        .content("""
                                {"trainStarting":"Zurich"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSetting_returnsBadRequestWhenRequiredFieldIsBlank() throws Exception {
        mockMvc.perform(post("/api/setting")
                        .contentType("application/json")
                        .content("""
                                {
                                  "trainStarting": " ",
                                  "trainDestination": "Winterthur",
                                  "weatherPlace": "Zurich"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSetting_returnsBadRequestForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/setting")
                        .contentType("application/json")
                        .content("{\"trainStarting\":"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSetting_usesPathIdAndReturnsSuccessMessage() throws Exception {
        mockMvc.perform(put("/api/setting/12")
                        .contentType("application/json")
                        .content("""
                                {
                                  "settingId": 99,
                                  "trainStarting": "Zurich",
                                  "trainDestination": "Winterthur",
                                  "weatherPlace": "Zurich"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Setting erfolgreich updated"));

        verify(settingService).updateSetting(org.mockito.ArgumentMatchers.argThat(
                setting -> setting.getSettingId().equals(12L)));
    }

    @Test
    void updateSetting_returnsBadRequestWhenRequiredFieldIsMissing() throws Exception {
        mockMvc.perform(put("/api/setting/12")
                        .contentType("application/json")
                        .content("""
                                {"trainStarting":"Zurich","trainDestination":"Winterthur"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSetting_returnsNotFoundWhenSettingDoesNotExist() throws Exception {
        when(settingService.updateSetting(any(Setting.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Setting not found"));

        mockMvc.perform(put("/api/setting/12")
                        .contentType("application/json")
                        .content(validSettingJson()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSetting_returnsBadRequestForNonNumericId() throws Exception {
        mockMvc.perform(put("/api/setting/not-a-number")
                        .contentType("application/json")
                        .content(validSettingJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSetting_returnsSuccessMessage() throws Exception {
        mockMvc.perform(delete("/api/setting/12"))
                .andExpect(status().isOk())
                .andExpect(content().string("Setting erfolgreich gelöscht."));

        verify(settingService).deleteSetting(12L);
    }

    @Test
    void deleteSetting_returnsNotFoundWhenSettingDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Setting not found"))
                .when(settingService).deleteSetting(404L);

        mockMvc.perform(delete("/api/setting/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSetting_returnsBadRequestForNonNumericId() throws Exception {
        mockMvc.perform(delete("/api/setting/not-a-number"))
                .andExpect(status().isBadRequest());
    }

    private Setting setting(Long id) {
        Setting setting = new Setting();
        setting.setSettingId(id);
        setting.setTrainStarting("Zurich");
        setting.setTrainDestination("Winterthur");
        setting.setWeatherPlace("Zurich");
        return setting;
    }

    private String validSettingJson() {
        return """
                {
                  "trainStarting": "Zurich",
                  "trainDestination": "Winterthur",
                  "weatherPlace": "Zurich"
                }
                """;
    }
}
