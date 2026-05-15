package com.examme.examme.dto.response.group;

import com.examme.examme.entity.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponseDto {
    private Long id;
    private Long groupId;
    private String groupName;
    private String teacherEmail;
    private InvitationStatus status;
    private LocalDateTime invitedAt;

    public static InvitationResponseDtoBuilder builder() {
        return new InvitationResponseDtoBuilder();
    }

    public static class InvitationResponseDtoBuilder {
        private Long id;
        private Long groupId;
        private String groupName;
        private String teacherEmail;
        private InvitationStatus status;
        private LocalDateTime invitedAt;

        public InvitationResponseDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public InvitationResponseDtoBuilder groupId(Long groupId) {
            this.groupId = groupId;
            return this;
        }

        public InvitationResponseDtoBuilder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public InvitationResponseDtoBuilder teacherEmail(String teacherEmail) {
            this.teacherEmail = teacherEmail;
            return this;
        }

        public InvitationResponseDtoBuilder status(InvitationStatus status) {
            this.status = status;
            return this;
        }

        public InvitationResponseDtoBuilder invitedAt(LocalDateTime invitedAt) {
            this.invitedAt = invitedAt;
            return this;
        }

        public InvitationResponseDto build() {
            return new InvitationResponseDto(id, groupId, groupName, teacherEmail, status, invitedAt);
        }
    }
}
