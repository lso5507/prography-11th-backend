package com.prography.backend.unit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.prography.backend.domain.MemberRole;
import com.prography.backend.domain.MemberStatus;
import com.prography.backend.dto.AuthDto;
import com.prography.backend.entity.Member;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.MemberRepository;
import com.prography.backend.service.AuthService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private AuthService authService;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        authService = new AuthService(memberRepository);
        encoder = new BCryptPasswordEncoder();
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        Member member = new Member("admin", encoder.encode("admin1234"), "관리자", "010-0000-0000", MemberRole.ADMIN,
            MemberStatus.ACTIVE);
        when(memberRepository.findByLoginId("admin")).thenReturn(Optional.of(member));

        AuthDto.LoginResponse response = authService.login(new AuthDto.LoginRequest("admin", "admin1234"));

        assertEquals("admin", response.loginId());
        assertEquals("관리자", response.name());
    }

    @Test
    @DisplayName("비밀번호 불일치 시 LOGIN_FAILED")
    void login_failed_when_wrong_password() {
        Member member = new Member("admin", encoder.encode("admin1234"), "관리자", "010-0000-0000", MemberRole.ADMIN,
            MemberStatus.ACTIVE);
        when(memberRepository.findByLoginId("admin")).thenReturn(Optional.of(member));

        AppException ex = assertThrows(AppException.class,
            () -> authService.login(new AuthDto.LoginRequest("admin", "wrong")));

        assertEquals(ErrorCode.LOGIN_FAILED, ex.getErrorCode());
    }
}
