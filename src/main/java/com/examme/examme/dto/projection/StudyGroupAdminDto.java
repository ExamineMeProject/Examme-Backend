package com.examme.examme.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroupAdminDto {
    private Long id;
    private String name;
    private Long teacherId;
    private String teacherEmail;
    private LocalDateTime createdAt;
}
