package com.prography.backend;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.Member;
import com.prography.backend.entity.Part;
import com.prography.backend.entity.QrCode;
import com.prography.backend.entity.Team;
import com.prography.backend.repository.CohortRepository;
import com.prography.backend.repository.CohortMemberRepository;
import com.prography.backend.repository.MemberRepository;
import com.prography.backend.repository.PartRepository;
import com.prography.backend.repository.QrCodeRepository;
import com.prography.backend.repository.SessionRepository;
import com.prography.backend.repository.TeamRepository;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CohortMemberRepository cohortMemberRepository;

    private Cohort cohort11;
    private Part serverPart;
    private Team teamA;
    private Long adminId;

    @BeforeEach
    void setUp() {
        cohort11 = cohortRepository.findByName("11기").orElseThrow();
        serverPart = partRepository.findByCohortAndName(cohort11, "SERVER").orElseThrow();
        teamA = teamRepository.findByCohortAndName(cohort11, "Team A").orElseThrow();
        Member admin = memberRepository.findByLoginId("admin").orElseThrow();
        adminId = admin.getId();
    }

    @Test
    @Order(1)
    @DisplayName("로그인")
    void api01_login() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("loginId", "admin", "password", "admin1234"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(adminId));
    }

    @Test
    @Order(2)
    @DisplayName("회원 조회")
    void api02_get_member() throws Exception {
        mockMvc.perform(get("/api/v1/members/{id}", adminId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.loginId").value("admin"));
    }

    @Test
    @Order(3)
    @DisplayName("회원 등록")
    void api03_create_member() throws Exception {
        String loginId = uniqueLoginId("user03");
        mockMvc.perform(post("/api/v1/admin/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "loginId", loginId,
                    "password", "pass1234",
                    "name", "테스트회원03",
                    "phone", "010-1234-0003",
                    "cohortId", cohort11.getId(),
                    "partId", serverPart.getId(),
                    "teamId", teamA.getId()
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.loginId").value(loginId));
    }

    @Test
    @Order(4)
    @DisplayName("회원 대시보드")
    void api04_get_members_dashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/members").param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content", notNullValue()));
    }

    @Test
    @Order(5)
    @DisplayName("회원 상세")
    void api05_get_member_detail() throws Exception {
        Long memberId = createMemberForScenario("user05");
        mockMvc.perform(get("/api/v1/admin/members/{id}", memberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(memberId));
    }

    @Test
    @Order(6)
    @DisplayName("회원 수정")
    void api06_update_member() throws Exception {
        Long memberId = createMemberForScenario("user06");
        mockMvc.perform(put("/api/v1/admin/members/{id}", memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "name", "수정회원06",
                    "phone", "010-5678-0006",
                    "partId", serverPart.getId(),
                    "teamId", teamA.getId()
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("수정회원06"));
    }

    @Test
    @Order(7)
    @DisplayName("회원 탈퇴")
    void api07_delete_member() throws Exception {
        Long memberId = createMemberForScenario("user07");
        mockMvc.perform(delete("/api/v1/admin/members/{id}", memberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(8)
    @DisplayName("기수 목록")
    void api08_get_cohorts() throws Exception {
        mockMvc.perform(get("/api/v1/admin/cohorts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(2)));
    }

    @Test
    @Order(9)
    @DisplayName("기수 상세")
    void api09_get_cohort_detail() throws Exception {
        mockMvc.perform(get("/api/v1/admin/cohorts/{id}", cohort11.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.parts.length()", greaterThanOrEqualTo(5)));
    }

    @Test
    @Order(10)
    @DisplayName("일정 목록 (회원)")
    void api10_get_sessions_member() throws Exception {
        mockMvc.perform(get("/api/v1/sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(11)
    @DisplayName("일정 목록 (관리자)")
    void api11_get_sessions_admin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(12)
    @DisplayName("일정 생성")
    void api12_create_session() throws Exception {
        String title = "세션12-" + System.nanoTime();
        mockMvc.perform(post("/api/v1/admin/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", title,
                    "date", LocalDate.now().toString(),
                    "time", "18:00",
                    "location", "강남"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value(title));
    }

    @Test
    @Order(13)
    @DisplayName("일정 수정")
    void api13_update_session() throws Exception {
        Long sessionId = createSessionForScenario("세션13");
        mockMvc.perform(put("/api/v1/admin/sessions/{id}", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", "세션13-수정",
                    "date", LocalDate.now().toString(),
                    "time", "19:00",
                    "location", "신촌",
                    "status", "IN_PROGRESS"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.title").value("세션13-수정"));
    }

    @Test
    @Order(14)
    @DisplayName("일정 삭제")
    void api14_delete_session() throws Exception {
        Long sessionId = createSessionForScenario("세션14");
        mockMvc.perform(delete("/api/v1/admin/sessions/{id}", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(15)
    @DisplayName("QR 생성")
    void api15_create_qrcode() throws Exception {
        Long sessionId = createSessionForScenario("세션15");
        com.prography.backend.entity.SessionEntity session = sessionRepository.findById(sessionId).orElseThrow();
        QrCode active = qrCodeRepository.findFirstBySessionAndActiveTrue(session).orElseThrow();
        active.expireNow();
        qrCodeRepository.save(active);

        mockMvc.perform(post("/api/v1/admin/sessions/{id}/qrcodes", sessionId))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sessionId").value(sessionId));
    }

    @Test
    @Order(16)
    @DisplayName("QR 갱신")
    void api16_renew_qrcode() throws Exception {
        Long sessionId = createSessionForScenario("세션16");
        Long qrId = getQrCodeIdFromSession(sessionId);
        mockMvc.perform(put("/api/v1/admin/qrcodes/{id}", qrId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @Order(17)
    @DisplayName("QR 출석 체크")
    void api17_check_in() throws Exception {
        Long memberId = createMemberForScenario("user17");
        Long sessionId = createSessionForScenario("세션17");
        String qrHash = getQrHashFromSession(sessionId);

        mockMvc.perform(post("/api/v1/attendances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "hashValue", qrHash,
                    "memberId", memberId
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.memberId").value(memberId));
    }

    @Test
    @Order(18)
    @DisplayName("내 출결 기록")
    void api18_get_my_attendances() throws Exception {
        Long memberId = createMemberForScenario("user18");
        Long sessionId = createSessionForScenario("세션18");
        String qrHash = getQrHashFromSession(sessionId);
        checkIn(memberId, qrHash);

        mockMvc.perform(get("/api/v1/attendances").param("memberId", String.valueOf(memberId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(19)
    @DisplayName("내 출결 요약")
    void api19_get_attendance_summary() throws Exception {
        Long memberId = createMemberForScenario("user19");
        Long sessionId = createSessionForScenario("세션19");
        String qrHash = getQrHashFromSession(sessionId);
        checkIn(memberId, qrHash);

        mockMvc.perform(get("/api/v1/members/{id}/attendance-summary", memberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deposit", notNullValue()));
    }

    @Test
    @Order(20)
    @DisplayName("출결 등록")
    void api20_register_attendance() throws Exception {
        Long memberId = createMemberForScenario("user20");
        Long sessionId = createSessionForScenario("세션20");

        mockMvc.perform(post("/api/v1/admin/attendances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "sessionId", sessionId,
                    "memberId", memberId,
                    "status", "ABSENT",
                    "lateMinutes", 0
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("ABSENT"));
    }

    @Test
    @Order(21)
    @DisplayName("출결 수정")
    void api21_update_attendance() throws Exception {
        Long memberId = createMemberForScenario("user21");
        Long sessionId = createSessionForScenario("세션21");
        Long attendanceId = registerAttendance(sessionId, memberId, "ABSENT", 0);

        mockMvc.perform(put("/api/v1/admin/attendances/{id}", attendanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "status", "EXCUSED",
                    "lateMinutes", 0
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("EXCUSED"));
    }

    @Test
    @Order(22)
    @DisplayName("일정별 출결 요약")
    void api22_get_session_attendance_summary() throws Exception {
        Long memberId = createMemberForScenario("user22");
        Long sessionId = createSessionForScenario("세션22");
        registerAttendance(sessionId, memberId, "PRESENT", 0);

        mockMvc.perform(get("/api/v1/admin/attendances/sessions/{id}/summary", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(23)
    @DisplayName("회원 출결 상세")
    void api23_get_member_attendance_detail() throws Exception {
        Long memberId = createMemberForScenario("user23");
        Long sessionId = createSessionForScenario("세션23");
        registerAttendance(sessionId, memberId, "PRESENT", 0);

        mockMvc.perform(get("/api/v1/admin/attendances/members/{id}", memberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.attendances.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(24)
    @DisplayName("일정별 출결 목록")
    void api24_get_session_attendances() throws Exception {
        Long memberId = createMemberForScenario("user24");
        Long sessionId = createSessionForScenario("세션24");
        registerAttendance(sessionId, memberId, "LATE", 10);

        mockMvc.perform(get("/api/v1/admin/attendances/sessions/{id}", sessionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.attendances.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(25)
    @DisplayName("보증금 이력")
    void api25_get_deposit_history() throws Exception {
        Long memberId = createMemberForScenario("user25");
        Long cohortMemberId = getCohortMemberId(memberId);

        mockMvc.perform(get("/api/v1/admin/cohort-members/{id}/deposits", cohortMemberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()", greaterThanOrEqualTo(1)));
    }

    private String uniqueLoginId(String prefix) {
        return prefix + System.nanoTime();
    }

    private Long createMemberForScenario(String prefix) throws Exception {
        String loginId = uniqueLoginId(prefix);
        MvcResult result = mockMvc.perform(post("/api/v1/admin/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "loginId", loginId,
                    "password", "pass1234",
                    "name", "시나리오회원-" + prefix,
                    "phone", "010-0000-" + String.format("%04d", Math.abs(prefix.hashCode() % 10000)),
                    "cohortId", cohort11.getId(),
                    "partId", serverPart.getId(),
                    "teamId", teamA.getId()
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private Long createSessionForScenario(String titlePrefix) throws Exception {
        String title = titlePrefix + "-" + System.nanoTime();
        MvcResult result = mockMvc.perform(post("/api/v1/admin/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", title,
                    "date", LocalDate.now().toString(),
                    "time", "09:00",
                    "location", "강남"
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        Long sessionId = root.path("data").path("id").asLong();

        mockMvc.perform(put("/api/v1/admin/sessions/{id}", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "status", "IN_PROGRESS"
                ))))
            .andExpect(status().isOk());

        return sessionId;
    }

    private Long getQrCodeIdFromSession(Long sessionId) throws Exception {
        com.prography.backend.entity.SessionEntity session = sessionRepository.findById(sessionId).orElseThrow();
        return qrCodeRepository.findFirstBySessionAndActiveTrue(session).orElseThrow().getId();
    }

    private String getQrHashFromSession(Long sessionId) throws Exception {
        com.prography.backend.entity.SessionEntity session = sessionRepository.findById(sessionId).orElseThrow();
        return qrCodeRepository.findFirstBySessionAndActiveTrue(session).orElseThrow().getHashValue();
    }

    private void checkIn(Long memberId, String qrHash) throws Exception {
        mockMvc.perform(post("/api/v1/attendances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "hashValue", qrHash,
                    "memberId", memberId
                ))))
            .andExpect(status().isCreated());
    }

    private Long registerAttendance(Long sessionId, Long memberId, String status, int lateMinutes) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/attendances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "sessionId", sessionId,
                    "memberId", memberId,
                    "status", status,
                    "lateMinutes", lateMinutes
                ))))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private Long getCohortMemberId(Long memberId) throws Exception {
        Member member = memberRepository.findById(memberId).orElseThrow();
        CohortMember cohortMember = cohortMemberRepository.findByMember(member).stream().findFirst().orElseThrow();
        return cohortMember.getId();
    }
}
