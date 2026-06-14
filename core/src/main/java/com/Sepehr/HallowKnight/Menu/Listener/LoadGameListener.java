package com.Sepehr.HallowKnight.Menu.Listener;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class LoadGameListener extends ChangeListener {
    private final HollowKnightEngine engine;

    public LoadGameListener(HollowKnightEngine engine) {
        this.engine = engine;
    }

    @Override
    public void changed(ChangeEvent event, Actor actor) {

    }
}
