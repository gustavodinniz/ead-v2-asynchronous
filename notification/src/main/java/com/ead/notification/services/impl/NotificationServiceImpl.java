package com.ead.notification.services.impl;

import com.ead.notification.repositories.NotificationRepository;
import com.ead.notification.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    NotificationRepository notificationRepository;
}
