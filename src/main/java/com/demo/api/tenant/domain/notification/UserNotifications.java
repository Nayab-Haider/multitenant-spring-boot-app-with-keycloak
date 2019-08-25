package com.demo.api.tenant.domain.notification;

import com.demo.api.configuration.Auditable;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
@Data
public class UserNotifications extends Auditable<String> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private String notificationRecipient;

    private String notificationTitle;

    private String notificationBody;

    private Boolean notificationReadStatus;

    private String actionUrl;

    public UserNotifications(String notificationRecipient, String notificationTitle, String notificationBody, Boolean
            notificationReadStatus, String actionUrl) {
        this.notificationRecipient = notificationRecipient;
        this.notificationTitle = notificationTitle;
        this.notificationBody = notificationBody;
        this.notificationReadStatus = notificationReadStatus;
        this.actionUrl = actionUrl;
    }

    public UserNotifications(String notificationRecipient, String notificationTitle, String notificationBody, String
            actionUrl) {
        this.notificationRecipient = notificationRecipient;
        this.notificationTitle = notificationTitle;
        this.notificationBody = notificationBody;
        this.notificationReadStatus = Boolean.FALSE;
        this.actionUrl = actionUrl;
    }

    public UserNotifications() {

    }
}
