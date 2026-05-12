package com.examme.examme.entity;

import com.examme.examme.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name is too long")
    @Column(nullable = false)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Legacy DB columns (pre–full_name schema). Kept in sync from {@link #fullName} on persist/update.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PostLoad
    protected void hydrateFullNameFromLegacy() {
        if ((fullName == null || fullName.isBlank()) && firstName != null && !firstName.isBlank()) {
            if (lastName == null || lastName.isBlank() || "-".equals(lastName)) {
                fullName = firstName;
            } else {
                fullName = firstName + " " + lastName;
            }
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        syncLegacyNameColumns();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        syncLegacyNameColumns();
    }

    private void syncLegacyNameColumns() {
        if (fullName == null || fullName.isBlank()) {
            firstName = "?";
            lastName = "?";
            return;
        }
        String trimmed = fullName.trim();
        int space = trimmed.indexOf(' ');
        if (space < 0) {
            firstName = trimmed;
            lastName = "-";
        } else {
            firstName = trimmed.substring(0, space).trim();
            String rest = trimmed.substring(space + 1).trim();
            lastName = rest.isEmpty() ? "-" : rest;
        }
    }
}
