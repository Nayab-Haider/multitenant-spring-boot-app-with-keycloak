package com.demo.api.tenant.service.notification;

import com.demo.api.tenant.domain.notification.UserNotifications;
import com.demo.api.tenant.service.notification.response.UserNotificationListResVO;
import com.demo.api.tenant.response.ResponseDomain;
import com.demo.api.tenant.repository.notification.UserNotificationRepository;
import com.demo.api.tenant.service.notification.response.NotificationCountVO;
import com.demo.api.tenant.service.notification.response.UserNotificationResVO;
import com.demo.api.utils.CommonUtility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserNotificationServiceImpl implements UserNotificationService {

    private static final Logger log = LogManager.getLogger(UserNotificationServiceImpl.class);


    @Autowired
    UserNotificationRepository userNotificationRepository;

    @Override
    public ResponseEntity<?> getNotifications(String empCode, Integer pageSize, Integer currentPage) {
        log.info("Entering Controller Class ::: UserNotificationServiceImpl ::: method ::: getNotifications");
        log.info("Method Arguments :::" + String.format("%s ::: %d ::: %d ", empCode, pageSize, currentPage));
        if (null == pageSize) {
            pageSize = 10;
        }
        if (null == currentPage) {
            currentPage = 1;
        }
        UserNotificationListResVO userNotificationListResVO = null;
        List<UserNotificationResVO> userNotificationResVOList = StreamSupport.stream
                (userNotificationRepository.findByNotificationRecipient(empCode).spliterator(), false)
                .map(userNotifications -> {
                    return new UserNotificationResVO(
                            userNotifications.getNotificationId(),
                            userNotifications.getNotificationRecipient(),
                            userNotifications.getNotificationTitle(),
                            userNotifications.getNotificationBody(),
                            userNotifications.getNotificationReadStatus(),
                            userNotifications.getActionUrl()
                    );
                })
                .sorted(Comparator.comparing(UserNotificationResVO::getNotificationId).reversed())
                .collect(Collectors.toList());
        if (!userNotificationResVOList.isEmpty()) {
            userNotificationListResVO = new UserNotificationListResVO(CommonUtility.getPage
                    (userNotificationResVOList, pageSize, currentPage), currentPage, pageSize);
            log.info(userNotificationListResVO.toString());
            log.info("Exit Controller Class ::: UserNotificationServiceImpl ::: method ::: getNotifications");
            return new ResponseEntity<>(userNotificationListResVO, HttpStatus.OK);
        }
        log.info("Exit Controller Class ::: UserNotificationServiceImpl ::: method ::: getNotifications");
        return ResponseDomain.responseNotFound();
    }

    @Override
    public ResponseEntity<?> markNotificationAsRead(Long notificationId) {
        UserNotifications userNotifications = userNotificationRepository.findById(notificationId).get();
        if (null != userNotifications) {
            userNotifications.setNotificationReadStatus(Boolean.TRUE);
            userNotificationRepository.save(userNotifications);
            return ResponseDomain.putResponse();
        }
        return ResponseDomain.responseNotFound();
    }

    @Override
    public ResponseEntity<?> getUnreadNotificationCount(String empCode) {

        Long unreadCount = userNotificationRepository.countByNotificationRecipientAndNotificationReadStatus(empCode,
                Boolean.FALSE);

        NotificationCountVO notificationCountVO = new NotificationCountVO(unreadCount);

        return new ResponseEntity<>(notificationCountVO, HttpStatus.OK);
    }
}
