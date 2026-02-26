package com.prography.backend.integration.attendance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.QrCode;
import com.prography.backend.entity.SessionEntity;
import com.prography.backend.entity.Part;
import com.prography.backend.entity.Team;
import com.prography.backend.repository.QrCodeRepository;
import com.prography.backend.repository.CohortRepository;
import com.prography.backend.repository.PartRepository;
import com.prography.backend.repository.SessionRepository;
import com.prography.backend.repository.TeamRepository;
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
class AttendanceControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CohortRepository cohortRepository;
    @Autowired private PartRepository partRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private SessionRepository sessionRepository;
    @Autowired private QrCodeRepository qrCodeRepository;

    @Test
    @DisplayName("Attendance 도메인 통합 - QR 출석 체크")
    void check_in_by_qr() throws Exception {
        Cohort cohort11 = cohortRepository.findByName("11기").orElseThrow();
        Part part = partRepository.findByCohortAndName(cohort11, "SERVER").orElseThrow();
        Team team = teamRepository.findByCohortAndName(cohort11, "Team A").orElseThrow();

        String loginId = "attendance-it-" + System.nanoTime();
        MvcResult memberCreated = mockMvc.perform(post("/api/v1/admin/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "loginId", loginId,
                    "password", "pass1234",
                    "name", "출결회원",
                    "phone", "010-9999-0000",
                    "cohortId", cohort11.getId(),
                    "partId", part.getId(),
                    "teamId", team.getId()
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        long memberId = objectMapper.readTree(memberCreated.getResponse().getContentAsString()).path("data").path("id").asLong();

        MvcResult sessionCreated = mockMvc.perform(post("/api/v1/admin/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", "attendance-session-" + System.nanoTime(),
                    "date", LocalDate.now().toString(),
                    "time", "09:00",
                    "location", "강남"
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode sessionRoot = objectMapper.readTree(sessionCreated.getResponse().getContentAsString()).path("data");
        long sessionId = sessionRoot.path("id").asLong();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/admin/sessions/{id}", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "status", "IN_PROGRESS"
                ))))
            .andExpect(status().isOk());

        SessionEntity session = sessionRepository.findById(sessionId).orElseThrow();
        QrCode qrCode = qrCodeRepository.findFirstBySessionAndActiveTrue(session).orElseThrow();
        String qrHash = qrCode.getHashValue();

        mockMvc.perform(post("/api/v1/attendances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "hashValue", qrHash,
                    "memberId", memberId
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }
}
