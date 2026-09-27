package org.mvs.talons.talonchecker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscription {
    @Id
    private String chatId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_departments", joinColumns = @JoinColumn(name = "chat_id"))
    @Column(name = "department_id")
    @Builder.Default
    private Set<Integer> departmentIds = new HashSet<>();

    private String sessionCookie;

    private LocalDate startDate;
    private LocalDate endDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_department_target_dates", joinColumns = @JoinColumn(name = "chat_id"))
    @Builder.Default
    private Set<DepartmentTargetDate> departmentTargetDates = new HashSet<>();

    @Builder.Default
    private boolean isAccess = false;

    private LocalDateTime accessStatusChangedAt;
}
