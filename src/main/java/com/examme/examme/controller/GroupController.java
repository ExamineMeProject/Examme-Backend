package com.examme.examme.controller;

import com.examme.examme.dto.common.ApiResponse;
import com.examme.examme.dto.request.group.GroupRequestDto;
import com.examme.examme.dto.response.group.GroupResponseDto;
import com.examme.examme.service.StudyGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Groups")
@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class GroupController {

    private final StudyGroupService studyGroupService;

    @Operation(summary = "Create group")
    @PostMapping
    public ResponseEntity<GroupResponseDto> create(@Valid @RequestBody GroupRequestDto dto) {
        return ResponseEntity.ok(studyGroupService.create(dto));
    }

    @Operation(summary = "List my groups")
    @GetMapping
    public ResponseEntity<List<GroupResponseDto>> list() {
        return ResponseEntity.ok(studyGroupService.listMine());
    }

    @Operation(summary = "Get group detail with students")
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponseDto> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(studyGroupService.getDetail(id));
    }

    @Operation(summary = "Update group")
    @PutMapping("/{id}")
    public ResponseEntity<GroupResponseDto> update(@PathVariable("id") Long id, @Valid @RequestBody GroupRequestDto dto) {
        return ResponseEntity.ok(studyGroupService.update(id, dto));
    }

    @Operation(summary = "Delete group")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable("id") Long id) {
        studyGroupService.delete(id);
        return ResponseEntity.ok(new ApiResponse("Group deleted", true));
    }

    @Operation(summary = "Invite student by email")
    @PostMapping("/{groupId}/invite")
    public ResponseEntity<ApiResponse> invite(@PathVariable("groupId") Long groupId, @RequestParam("email") String email) {
        studyGroupService.inviteStudent(groupId, email);
        return ResponseEntity.ok(new ApiResponse("Invitation sent", true));
    }
}
