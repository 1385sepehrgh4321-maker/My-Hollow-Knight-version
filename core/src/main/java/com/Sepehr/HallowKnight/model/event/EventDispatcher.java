package com.Sepehr.HallowKnight.model.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventDispatcher {
    private static final HashMap<GameEvent, List<GameEventListener>> listeners = new HashMap<>();

    public static void register(GameEvent event, GameEventListener listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    public static void dispatch(GameEvent event, Object data) {
        if (listeners.containsKey(event)) {
            List<GameEventListener> list = listeners.get(event);
            for (GameEventListener gameEventListener : list) {
                gameEventListener.onEvent(event, data);
            }
        }
    }
}
