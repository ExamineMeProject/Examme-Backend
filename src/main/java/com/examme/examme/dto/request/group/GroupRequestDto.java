package com.examme.examme.dto.request.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRequestDto {
    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name is too long")
    private String name;

    @Size(max = 2000, message = "Description is too long")
    private String description;
}
