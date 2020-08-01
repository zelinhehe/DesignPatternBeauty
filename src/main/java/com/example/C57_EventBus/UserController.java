package com.example.C57_EventBus;


import com.example.C57_EventBus.event_bus.AsyncEventBus;
import com.example.C57_EventBus.event_bus.EventBus;
import com.example.C57_EventBus.event_bus.Subscribe;
import com.example.C57_EventBus.service.NotificationService;
import com.example.C57_EventBus.service.PromotionService;
import com.example.C57_EventBus.service.UserService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class UserController {
    @Resource
    private UserService userService; // 依赖注入
    @Resource
    private EventBus eventBus;
    private static final int DEFAULT_EVENTBUS_THREAD_POOL_SIZE = 20;

    public UserController(UserService userService) {
        this.userService = userService;
        //eventBus = new EventBus(); // 同步阻塞模式
        eventBus = new AsyncEventBus(Executors.newFixedThreadPool(DEFAULT_EVENTBUS_THREAD_POOL_SIZE)); // 异步非阻塞模式
    }

    public void setRegObservers(List<Object> observers) {
        for (Object observer : observers) {
            eventBus.register(observer);
        }
    }

    public Long register(String telephone, String password) {
        //省略输入参数的校验代码
        //省略userService.register()异常的try-catch代码
        long userId = userService.register(telephone, password);

        System.out.println("eventBus.post start");
        eventBus.post(userId);
        System.out.println("eventBus.post end");
        return userId;
    }

    public static void main(String[] args) {
        RegPromotionObserver promotionObserver = new RegPromotionObserver();
        RegNotificationObserver notificationObserver = new RegNotificationObserver();
        List<Object> observers = new ArrayList<>();
        observers.add(promotionObserver);
        observers.add(notificationObserver);
        UserController userController = new UserController(new UserService());
        userController.setRegObservers(observers);

        System.out.println("start");
        userController.register("1", "1");
        System.out.println("end");
    }
}

class RegPromotionObserver {

    @Subscribe
    public void handleRegSuccess(Long userId) {
        new PromotionService().issueNewUserExperienceCash(userId);
    }
}

class RegNotificationObserver {

    @Subscribe
    public void handleRegSuccess(Long userId) {
        new NotificationService().sendInboxMessage(userId, "hello");
    }
}

