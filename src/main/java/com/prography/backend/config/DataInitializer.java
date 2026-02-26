package com.prography.backend.config;

import com.prography.backend.domain.MemberRole;
import com.prography.backend.domain.MemberStatus;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.Member;
import com.prography.backend.entity.Part;
import com.prography.backend.entity.Team;
import com.prography.backend.repository.CohortMemberRepository;
import com.prography.backend.repository.CohortRepository;
import com.prography.backend.repository.MemberRepository;
import com.prography.backend.repository.PartRepository;
import com.prography.backend.repository.TeamRepository;
import com.prography.backend.service.DepositService;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final List<String> PART_NAMES = List.of("SERVER", "WEB", "iOS", "ANDROID", "DESIGN");
    private static final List<String> TEAM_NAMES = List.of("Team A", "Team B", "Team C");
    private static final int INITIAL_DEPOSIT = 100_000;

    private final CohortRepository cohortRepository;
    private final PartRepository partRepository;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositService depositService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(CohortRepository cohortRepository,
                           PartRepository partRepository,
                           TeamRepository teamRepository,
                           MemberRepository memberRepository,
                           CohortMemberRepository cohortMemberRepository,
                           DepositService depositService) {
        this.cohortRepository = cohortRepository;
        this.partRepository = partRepository;
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
        this.cohortMemberRepository = cohortMemberRepository;
        this.depositService = depositService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Cohort cohort10 = cohortRepository.findByName("10기").orElseGet(() -> cohortRepository.save(new Cohort("10기", 10, false)));
        Cohort cohort11 = cohortRepository.findByName("11기").orElseGet(() -> cohortRepository.save(new Cohort("11기", 11, true)));

        seedParts(cohort10);
        seedParts(cohort11);
        seedTeams(cohort11);

        Member admin = memberRepository.findByLoginId("admin").orElseGet(() ->
            memberRepository.save(new Member(
                "admin",
                passwordEncoder.encode("admin1234"),
                "관리자",
                "010-0000-0000",
                MemberRole.ADMIN,
                MemberStatus.ACTIVE
            ))
        );

        if (cohortMemberRepository.findByMemberAndCohort(admin, cohort11).isEmpty()) {
            Part server = partRepository.findByCohortAndName(cohort11, "SERVER").orElseThrow();
            Team teamA = teamRepository.findByCohortAndName(cohort11, "Team A").orElseThrow();
            CohortMember cm = cohortMemberRepository.save(new CohortMember(cohort11, admin, server, teamA, INITIAL_DEPOSIT));
            depositService.recordInitial(cm, INITIAL_DEPOSIT, null, "초기 보증금");
        }
    }

    private void seedParts(Cohort cohort) {
        for (String partName : PART_NAMES) {
            if (partRepository.findByCohortAndName(cohort, partName).isEmpty()) {
                partRepository.save(new Part(cohort, partName));
            }
        }
    }

    private void seedTeams(Cohort cohort) {
        for (String teamName : TEAM_NAMES) {
            if (teamRepository.findByCohortAndName(cohort, teamName).isEmpty()) {
                teamRepository.save(new Team(cohort, teamName));
            }
        }
    }
}
