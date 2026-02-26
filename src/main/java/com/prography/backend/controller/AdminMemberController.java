package com.prography.backend.controller;

import com.prography.backend.api.ApiResponse;
import com.prography.backend.api.PageResponse;
import com.prography.backend.domain.MemberStatus;
import com.prography.backend.dto.MemberDto;
import com.prography.backend.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/members")
public class AdminMemberController {

    private final MemberService memberService;

    public AdminMemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MemberDto.MemberDetailResponse>> create(@Valid @RequestBody MemberDto.CreateMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(memberService.createMember(request)));
    }

    @GetMapping
    public ApiResponse<PageResponse<MemberDto.MemberDetailResponse>> dashboard(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String searchType,
        @RequestParam(required = false) String searchValue,
        @RequestParam(required = false) Integer generation,
        @RequestParam(required = false) String partName,
        @RequestParam(required = false) String teamName,
        @RequestParam(required = false) MemberStatus status
    ) {
        return ApiResponse.success(memberService.getAdminMembers(page, size, searchType, searchValue, generation, partName, teamName, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<MemberDto.MemberDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(memberService.getAdminMemberDetail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<MemberDto.MemberDetailResponse> update(@PathVariable Long id,
                                                               @Valid @RequestBody MemberDto.UpdateMemberRequest request) {
        return ApiResponse.success(memberService.updateMember(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<MemberDto.MemberWithdrawResponse> withdraw(@PathVariable Long id) {
        return ApiResponse.success(memberService.withdrawMember(id));
    }
}
