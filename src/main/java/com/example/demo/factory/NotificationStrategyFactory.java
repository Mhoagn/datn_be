package com.example.demo.factory;

import com.example.demo.entity.Notification;
import com.example.demo.strategy.notification.NotificationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationStrategyFactory {

    // Spring inject tất cả bean implement NotificationStrategy
    private final Map<Notification.Type, NotificationStrategy> strategyMap;

    public NotificationStrategyFactory(List<NotificationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        NotificationStrategy::getSupportedType,
                        Function.identity()
                ));
    }

    public NotificationStrategy getStrategy(Notification.Type type) {
        NotificationStrategy strategy = strategyMap.get(type);
//        if (strategy == null) {
//            throw new AppException(ErrorCode.NOTIFICATION_STRATEGY_NOT_FOUND);
//        }
        return strategy;
    }
}