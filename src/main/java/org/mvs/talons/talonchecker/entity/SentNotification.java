package org.mvs.talons.talonchecker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sent_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatId;
    private Integer departmentId;
    private String slotDate;
    private LocalDateTime sentAt;
}
