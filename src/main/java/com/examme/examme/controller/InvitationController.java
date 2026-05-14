package com.examme.examme.controller;

import com.examme.examme.dto.common.ApiResponse;
import com.examme.examme.service.StudentInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Invitations")
@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class InvitationController {

    private final StudentInvitationService studentInvitationService;

    @Operation(summary = "Accept group invitation")
    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<ApiResponse> accept(@PathVariable("invitationId") Long invitationId) {
        studentInvitationService.accept(invitationId);
        return ResponseEntity.ok(new ApiResponse("Invitation accepted", true));
    }

    @Operation(summary = "Reject group invitation")
    @PostMapping("/{invitationId}/reject")
    public ResponseEntity<ApiResponse> reject(@PathVariable("invitationId") Long invitationId) {
        studentInvitationService.reject(invitationId);
        return ResponseEntity.ok(new ApiResponse("Invitation rejected", true));
    }
}
