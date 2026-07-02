package com.Sepehr.HallowKnight.model.event;

public interface GameEventListener {
    void onEvent(GameEvent event, Object data);
}
