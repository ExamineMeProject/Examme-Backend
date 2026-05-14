package com.examme.examme.controller;

import com.examme.examme.dto.common.ApiResponse;
import com.examme.examme.dto.projection.AdminQuizResultRowDto;
import com.examme.examme.dto.projection.StudyGroupAdminDto;
import com.examme.examme.dto.request.user.RoleUpdateDto;
import com.examme.examme.dto.request.user.UserUpdateDto;
import com.examme.examme.dto.response.exam.ExamPackageSummaryDto;
import com.examme.examme.dto.response.user.UserDto;
import com.examme.examme.service.AdminPanelService;
import com.examme.examme.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin")
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminPanelService adminPanelService;

    @Operation(summary = "List all users")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> listUsers() {
        return ResponseEntity.ok(adminPanelService.listUsers());
    }

    @Operation(summary = "Get user by id")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Update any user")
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("id") Long id, @RequestBody UserUpdateDto dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse("User deleted", true));
    }

    @Operation(summary = "Change user role")
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDto> updateRole(@PathVariable("id") Long id, @RequestBody RoleUpdateDto dto) {
        return ResponseEntity.ok(userService.updateUserRole(id, dto.getRole()));
    }

    @Operation(summary = "List all groups")
    @GetMapping("/groups")
    public ResponseEntity<List<StudyGroupAdminDto>> listGroups() {
        return ResponseEntity.ok(adminPanelService.listGroups());
    }

    @Operation(summary = "Delete any group")
    @DeleteMapping("/groups/{id}")
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable("id") Long id) {
        adminPanelService.deleteGroup(id);
        return ResponseEntity.ok(new ApiResponse("Group deleted", true));
    }

    @Operation(summary = "List all exam packages")
    @GetMapping("/exam-packages")
    public ResponseEntity<List<ExamPackageSummaryDto>> listExamPackages() {
        return ResponseEntity.ok(adminPanelService.listExamPackages());
    }

    @Operation(summary = "Delete any exam package")
    @DeleteMapping("/exam-packages/{id}")
    public ResponseEntity<ApiResponse> deleteExamPackage(@PathVariable("id") Long id) {
        adminPanelService.deleteExamPackage(id);
        return ResponseEntity.ok(new ApiResponse("Exam package deleted", true));
    }

    @Operation(summary = "View all completed quiz results")
    @GetMapping("/results")
    public ResponseEntity<List<AdminQuizResultRowDto>> allResults() {
        return ResponseEntity.ok(adminPanelService.listAllResults());
    }
}
