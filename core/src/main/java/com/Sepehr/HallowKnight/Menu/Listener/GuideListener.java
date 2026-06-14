package com.Sepehr.HallowKnight.Menu.Listener;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import javax.swing.event.ChangeEvent;

public class GuideListener extends ChangeListener {
    private final HollowKnightEngine engine;

    public GuideListener(HollowKnightEngine engine) {
        this.engine = engine;
    }

    @Override
    public void changed(ChangeEvent event, Actor actor) {

    }
}
