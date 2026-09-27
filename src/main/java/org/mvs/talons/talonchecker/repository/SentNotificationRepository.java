package org.mvs.talons.talonchecker.repository;

import org.mvs.talons.talonchecker.entity.SentNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SentNotificationRepository extends JpaRepository<SentNotification, Long> {
    Optional<SentNotification> findByChatIdAndDepartmentIdAndSlotDate(String chatId, Integer departmentId, String slotDate);
    void deleteBySentAtBefore(LocalDateTime expiryDate);
}
