package com.demo.api.tenant.service.notification.response;

import lombok.Data;

import java.util.List;

@Data
public class UserNotificationListResVO {

    List<UserNotificationResVO> userNotificationResVOList;

    Integer currentPage;

    Integer pageSize;

    public UserNotificationListResVO(List<UserNotificationResVO> userNotificationResVOList, Integer currentPage, Integer
            pageSize) {
        this.userNotificationResVOList = userNotificationResVOList;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }
}
