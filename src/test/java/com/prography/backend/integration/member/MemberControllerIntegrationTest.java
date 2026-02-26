package com.prography.backend.integration.member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.Part;
import com.prography.backend.entity.Team;
import com.prography.backend.repository.CohortRepository;
import com.prography.backend.repository.PartRepository;
import com.prography.backend.repository.TeamRepository;
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
class MemberControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CohortRepository cohortRepository;
    @Autowired private PartRepository partRepository;
    @Autowired private TeamRepository teamRepository;

    @Test
    @DisplayName("Member 도메인 통합 - 등록/조회/탈퇴")
    void create_get_delete_member() throws Exception {
        Cohort cohort11 = cohortRepository.findByName("11기").orElseThrow();
        Part part = partRepository.findByCohortAndName(cohort11, "SERVER").orElseThrow();
        Team team = teamRepository.findByCohortAndName(cohort11, "Team A").orElseThrow();

        String loginId = "member-it-" + System.nanoTime();
        MvcResult created = mockMvc.perform(post("/api/v1/admin/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "loginId", loginId,
                    "password", "pass1234",
                    "name", "통합회원",
                    "phone", "010-1234-5678",
                    "cohortId", cohort11.getId(),
                    "partId", part.getId(),
                    "teamId", team.getId()
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode root = objectMapper.readTree(created.getResponse().getContentAsString());
        long memberId = root.path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/admin/members/{id}", memberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.loginId").value(loginId));

        mockMvc.perform(delete("/api/v1/admin/members/{id}", memberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
