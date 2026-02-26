package com.prography.backend.service;

import com.prography.backend.api.PageResponse;
import com.prography.backend.domain.MemberRole;
import com.prography.backend.domain.MemberStatus;
import com.prography.backend.dto.MemberDto;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.Member;
import com.prography.backend.entity.Part;
import com.prography.backend.entity.Team;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.CohortMemberRepository;
import com.prography.backend.repository.CohortRepository;
import com.prography.backend.repository.MemberRepository;
import com.prography.backend.repository.PartRepository;
import com.prography.backend.repository.TeamRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private static final int INITIAL_DEPOSIT = 100_000;

    private final MemberRepository memberRepository;
    private final CohortRepository cohortRepository;
    private final PartRepository partRepository;
    private final TeamRepository teamRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositService depositService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public MemberService(MemberRepository memberRepository,
                         CohortRepository cohortRepository,
                         PartRepository partRepository,
                         TeamRepository teamRepository,
                         CohortMemberRepository cohortMemberRepository,
                         DepositService depositService) {
        this.memberRepository = memberRepository;
        this.cohortRepository = cohortRepository;
        this.partRepository = partRepository;
        this.teamRepository = teamRepository;
        this.cohortMemberRepository = cohortMemberRepository;
        this.depositService = depositService;
    }

    @Transactional(readOnly = true)
    public MemberDto.MemberSimpleResponse getMember(Long memberId) {
        Member member = findMember(memberId);
        return new MemberDto.MemberSimpleResponse(
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

    @Transactional
    public MemberDto.MemberDetailResponse createMember(MemberDto.CreateMemberRequest request) {
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new AppException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        Cohort cohort = cohortRepository.findById(request.cohortId())
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_NOT_FOUND));

        Part part = resolvePart(request.partId(), cohort);
        Team team = resolveTeam(request.teamId(), cohort);

        Member member = new Member(
            request.loginId(),
            passwordEncoder.encode(request.password()),
            request.name(),
            request.phone(),
            MemberRole.MEMBER,
            MemberStatus.ACTIVE
        );
        Member savedMember = memberRepository.save(member);

        CohortMember cohortMember = new CohortMember(cohort, savedMember, part, team, INITIAL_DEPOSIT);
        CohortMember saved = cohortMemberRepository.save(cohortMember);
        depositService.recordInitial(saved, INITIAL_DEPOSIT, null, "초기 보증금");
        return toDetail(savedMember, saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<MemberDto.MemberDetailResponse> getAdminMembers(int page, int size, String searchType,
                                                                         String searchValue, Integer generation,
                                                                         String partName, String teamName,
                                                                         MemberStatus status) {
        List<MemberDto.MemberDetailResponse> rows = new ArrayList<>();
        for (Member member : memberRepository.findAll()) {
            if (status != null && member.getStatus() != status) {
                continue;
            }
            if (!matchesSearch(member, searchType, searchValue)) {
                continue;
            }
            CohortMember cm = cohortMemberRepository.findByMember(member).stream().findFirst().orElse(null);
            MemberDto.MemberDetailResponse detail = toDetail(member, cm);
            if (generation != null && (detail.generation() == null || !generation.equals(detail.generation()))) {
                continue;
            }
            if (partName != null && detail.partName() != null
                && !detail.partName().toLowerCase(Locale.ROOT).contains(partName.toLowerCase(Locale.ROOT))) {
                continue;
            }
            if (teamName != null && detail.teamName() != null
                && !detail.teamName().toLowerCase(Locale.ROOT).contains(teamName.toLowerCase(Locale.ROOT))) {
                continue;
            }
            rows.add(detail);
        }

        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        List<MemberDto.MemberDetailResponse> content = rows.subList(from, to);
        int totalPages = size == 0 ? 1 : (int) Math.ceil((double) rows.size() / size);
        return new PageResponse<>(content, page, size, rows.size(), totalPages);
    }

    @Transactional(readOnly = true)
    public MemberDto.MemberDetailResponse getAdminMemberDetail(Long memberId) {
        Member member = findMember(memberId);
        CohortMember cm = cohortMemberRepository.findByMember(member).stream().findFirst().orElse(null);
        return toDetail(member, cm);
    }

    @Transactional
    public MemberDto.MemberDetailResponse updateMember(Long memberId, MemberDto.UpdateMemberRequest request) {
        Member member = findMember(memberId);
        member.update(request.name(), request.phone());

        CohortMember cm = cohortMemberRepository.findByMember(member).stream().findFirst().orElse(null);
        Cohort targetCohort = cm == null ? null : cm.getCohort();

        if (request.cohortId() != null) {
            targetCohort = cohortRepository.findById(request.cohortId())
                .orElseThrow(() -> new AppException(ErrorCode.COHORT_NOT_FOUND));
        }

        if (targetCohort != null && (request.partId() != null || request.teamId() != null || request.cohortId() != null)) {
            Part part = resolvePart(request.partId(), targetCohort);
            Team team = resolveTeam(request.teamId(), targetCohort);
            if (cm == null || request.cohortId() != null) {
                CohortMember newCm = new CohortMember(targetCohort, member, part, team, INITIAL_DEPOSIT);
                cm = cohortMemberRepository.save(newCm);
                depositService.recordInitial(cm, INITIAL_DEPOSIT, null, "초기 보증금");
            } else {
                cm.updateAssignment(part, team);
            }
        }
        return toDetail(member, cm);
    }

    @Transactional
    public MemberDto.MemberWithdrawResponse withdrawMember(Long memberId) {
        Member member = findMember(memberId);
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new AppException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        member.withdraw();
        return new MemberDto.MemberWithdrawResponse(member.getId(), member.getLoginId(), member.getName(), member.getStatus(),
            member.getUpdatedAt());
    }

    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Part resolvePart(Long partId, Cohort cohort) {
        if (partId == null) {
            return partRepository.findByCohortAndName(cohort, "SERVER")
                .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));
        }
        return partRepository.findByIdAndCohort(partId, cohort)
            .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));
    }

    private Team resolveTeam(Long teamId, Cohort cohort) {
        if (teamId == null) {
            return null;
        }
        return teamRepository.findByIdAndCohort(teamId, cohort)
            .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));
    }

    private boolean matchesSearch(Member member, String searchType, String searchValue) {
        if (searchType == null || searchValue == null || searchValue.isBlank()) {
            return true;
        }
        String needle = searchValue.toLowerCase(Locale.ROOT);
        return switch (searchType) {
            case "name" -> member.getName().toLowerCase(Locale.ROOT).contains(needle);
            case "loginId" -> member.getLoginId().toLowerCase(Locale.ROOT).contains(needle);
            case "phone" -> member.getPhone().toLowerCase(Locale.ROOT).contains(needle);
            default -> true;
        };
    }

    private MemberDto.MemberDetailResponse toDetail(Member member, CohortMember cm) {
        if (cm == null) {
            return new MemberDto.MemberDetailResponse(
                member.getId(),
                member.getLoginId(),
                member.getName(),
                member.getPhone(),
                member.getStatus(),
                member.getRole(),
                null,
                null,
                null,
                null,
                member.getCreatedAt(),
                member.getUpdatedAt()
            );
        }
        return new MemberDto.MemberDetailResponse(
            member.getId(),
            member.getLoginId(),
            member.getName(),
            member.getPhone(),
            member.getStatus(),
            member.getRole(),
            cm.getCohort().getGeneration(),
            cm.getPart() == null ? null : cm.getPart().getName(),
            cm.getTeam() == null ? null : cm.getTeam().getName(),
            cm.getDepositBalance(),
            member.getCreatedAt(),
            member.getUpdatedAt()
        );
    }
}
