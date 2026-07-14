package com.LocalServe.LocalServe.repository;

import com.LocalServe.LocalServe.entity.Notification;
import com.LocalServe.LocalServe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
