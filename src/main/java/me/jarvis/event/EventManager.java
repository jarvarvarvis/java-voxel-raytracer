package me.jarvis.event;

import com.google.common.eventbus.EventBus;

public class EventManager {

    public static final EventBus BUS = new EventBus();

    public static void post(Event object) {
        BUS.post(object);
    }

    public static void register(Object object) {
        BUS.register(object);
    }

    public static void unregister(Object object) {
        BUS.unregister(object);
    }
}
