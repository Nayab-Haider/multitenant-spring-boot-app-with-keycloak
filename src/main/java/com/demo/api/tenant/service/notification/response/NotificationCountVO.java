package com.demo.api.tenant.service.notification.response;

import lombok.Data;

@Data
public class NotificationCountVO {

    private Long unread;

    public NotificationCountVO(Long unread) {
        this.unread = unread;
    }

    public NotificationCountVO() {

    }
}
