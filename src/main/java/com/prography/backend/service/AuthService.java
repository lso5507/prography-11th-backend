package com.prography.backend.service;

import com.prography.backend.domain.MemberStatus;
import com.prography.backend.dto.AuthDto;
import com.prography.backend.entity.Member;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        Member member = memberRepository.findByLoginId(request.loginId())
            .orElseThrow(() -> new AppException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new AppException(ErrorCode.MEMBER_WITHDRAWN);
        }
        return new AuthDto.LoginResponse(
            member.getId(),
            member.getLoginId(),
            member.getName(),
            member.getPhone(),
            member.getStatus(),
            member.getRole(),
            member.getCreatedAt(),
            member.getUpdatedAt()
        );
    }

    public BCryptPasswordEncoder passwordEncoder() {
        return passwordEncoder;
    }
}
