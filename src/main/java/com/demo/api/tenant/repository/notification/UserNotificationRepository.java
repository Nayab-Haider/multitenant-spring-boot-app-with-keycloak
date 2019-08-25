package com.demo.api.tenant.repository.notification;

import com.demo.api.tenant.domain.notification.UserNotifications;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserNotificationRepository extends CrudRepository<UserNotifications, Long> {

    List<UserNotifications> findByNotificationRecipient(String notificationRecipient);

    Long countByNotificationRecipientAndNotificationReadStatus(String notificationRecipient, Boolean
            notificationReadStatus);
}
