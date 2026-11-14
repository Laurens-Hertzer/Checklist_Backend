package ch.bbw.lah.checklist_backend.controller;

import ch.bbw.lah.checklist_backend.model.Checklist;
import ch.bbw.lah.checklist_backend.service.ChecklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChecklistController.class)
class ChecklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChecklistService checklistService;

    @Test
    void createChecklist_returnsCreatedResponseAndJson() throws Exception {
        Checklist saved = new Checklist();
        saved.setChecklistId(7L);
        saved.setTask("Milch kaufen");
        when(checklistService.createChecklist(any(Checklist.class))).thenReturn(saved);

        mockMvc.perform(post("/api/checklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"task":"Milch kaufen","completed":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checklistId").value(7))
                .andExpect(jsonPath("$.task").value("Milch kaufen"));
    }

    @Test
    void createChecklist_returnsBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/checklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"task":"Milch kaufen"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createChecklist_returnsBadRequestWhenTaskIsBlank() throws Exception {
        mockMvc.perform(post("/api/checklist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"task":"  ","completed":false}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateChecklist_returnsBadRequestWhenTaskIsBlank() throws Exception {
        mockMvc.perform(put("/api/checklist/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"task":" ","completed":false}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getChecklist_returnsNotFoundForUnknownId() throws Exception {
        when(checklistService.getChecklistById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));

        mockMvc.perform(get("/api/checklist/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getChecklist_returnsBadRequestForNonNumericId() throws Exception {
        mockMvc.perform(get("/api/checklist/not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getChecklistsByDate_returnsBadRequestWhenDateIsMissing() throws Exception {
        mockMvc.perform(get("/api/checklist/date"))
                .andExpect(status().isBadRequest());
    }
}
