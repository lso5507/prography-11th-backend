package com.prography.backend.integration.session;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SessionControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("Session 도메인 통합 - 생성/목록/삭제")
    void create_list_delete_session() throws Exception {
        String title = "it-session-" + System.nanoTime();
        MvcResult created = mockMvc.perform(post("/api/v1/admin/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", title,
                    "date", LocalDate.now().toString(),
                    "time", "10:00",
                    "location", "강남"
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode root = objectMapper.readTree(created.getResponse().getContentAsString());
        long sessionId = root.path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/admin/sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(delete("/api/v1/admin/sessions/{id}", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
