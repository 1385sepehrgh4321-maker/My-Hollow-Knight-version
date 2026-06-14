package com.Sepehr.HallowKnight.Menu.Listener;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class NewGameListener extends ChangeListener {
    private HollowKnightEngine engine;

    public NewGameListener(HollowKnightEngine engine) {
        this.engine = engine;
    }
    @Override
    public void changed(ChangeEvent event, Actor actor) {

    }
}
