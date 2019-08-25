package com.demo.api.tenant.service.notification;

import org.springframework.http.ResponseEntity;

public interface UserNotificationService {

    public ResponseEntity<?> getNotifications(String empCode, Integer pageSize, Integer currentPage);

    public ResponseEntity<?> markNotificationAsRead(Long notificationId);

    public ResponseEntity<?> getUnreadNotificationCount(String empCode);
}
