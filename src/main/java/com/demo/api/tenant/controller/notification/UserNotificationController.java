package com.demo.api.tenant.controller.notification;

import com.demo.api.tenant.response.ResponseDomain;
import com.demo.api.tenant.service.notification.UserNotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notifications")
@CrossOrigin
public class UserNotificationController {

    private static final Logger log = LogManager.getLogger(UserNotificationController.class);

    @Autowired
    UserNotificationService userNotificationService;

    @GetMapping("/{empCode}")
    ResponseEntity<?> getNotifications(@PathVariable("empCode") String empCode, @RequestParam(value = "pageSize",
            required = false) Integer pageSize, @RequestParam(value = "currentPage", required = false) Integer
                                               currentPage) {
        log.info("Entering Controller Class ::: NotificationController ::: method ::: getNotifications");
        if (empCode != null)
            return userNotificationService.getNotifications(empCode, pageSize, currentPage);
        return ResponseDomain.badRequest("Employee Code can't be blank.");
    }

    @GetMapping("/{empCode}/unread")
    ResponseEntity<?> getUnreadNotificationCount(@PathVariable("empCode") String empCode) {
        log.trace("Entering Controller Class ::: NotificationController ::: method ::: getUnreadNotificationCount");
        if (empCode != null)
            return userNotificationService.getUnreadNotificationCount(empCode);
        return ResponseDomain.badRequest("Employee Code can't be blank.");
    }

    @PutMapping("/{notificationId}/read")
    ResponseEntity<?> markNotificationAsRead(@PathVariable("notificationId") Long notificationId) {
        log.info("Entering Controller Class ::: NotificationController ::: method ::: markNotificationAsRead");
        if (notificationId != null)
            return userNotificationService.markNotificationAsRead(notificationId);
        return ResponseDomain.badRequest("Notification Id can't be blank.");
    }
}
