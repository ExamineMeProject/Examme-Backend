package com.examme.examme.repository;

import com.examme.examme.entity.Notification;
import com.examme.examme.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.readFlag = true WHERE n.user = :user AND n.readFlag = false")
    void markAllAsReadByUser(@Param("user") User user);
}
