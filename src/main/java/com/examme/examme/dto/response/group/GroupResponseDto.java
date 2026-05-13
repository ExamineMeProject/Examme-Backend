package com.examme.examme.dto.response.group;

import com.examme.examme.dto.response.user.StudentBriefDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponseDto {
    private Long id;
    private String name;
    private String description;
    private Long teacherId;
    private int studentCount;
    private LocalDateTime createdAt;
    private List<StudentBriefDto> students;
}
