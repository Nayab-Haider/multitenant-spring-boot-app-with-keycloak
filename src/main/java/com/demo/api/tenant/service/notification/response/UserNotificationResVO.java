package com.demo.api.tenant.service.notification.response;

import lombok.Data;

@Data
public class UserNotificationResVO {

    private Long notificationId;

    private String notificationRecipient;

    private String notificationTitle;

    private String notificationBody;

    private Boolean notificationReadStatus;

    private String actionUrl;

    public UserNotificationResVO() {
    }

    public UserNotificationResVO(Long notificationId, String notificationRecipient, String notificationTitle, String
            notificationBody, Boolean notificationReadStatus, String actionUrl) {
        this.notificationId = notificationId;
        this.notificationRecipient = notificationRecipient;
        this.notificationTitle = notificationTitle;
        this.notificationBody = notificationBody;
        this.notificationReadStatus = notificationReadStatus;
        this.actionUrl = actionUrl;
    }

//    @Override
//    public int compareTo(UserNotificationResVO o) {
//        return notificationId.compareTo(o.getNotificationId());
//    }
}
