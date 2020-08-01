package com.example.C57_EventBus.event_bus;


import java.util.List;
import java.util.concurrent.Executor;

public class EventBus {
    private Executor executor;
    private ObserverRegistry registry = new ObserverRegistry();

//    public EventBus() {
//        this(MoreExecutors.directExecutor());
//    }

    protected EventBus(Executor executor) {
        this.executor = executor;
    }

    public void register(Object object) {
        registry.register(object);
    }

    public void post(Object event) {
        List<ObserverAction> observerActions = registry.getMatchedObserverActions(event);
        for (ObserverAction observerAction : observerActions) {
            System.out.println("observerAction:" + observerAction);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("execute event:" + event);
                    observerAction.execute(event);
                }
            });
        }
    }
}
