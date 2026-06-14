package com.Sepehr.HallowKnight.Menu.Listener;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import javax.swing.event.ChangeEvent;

public class ExitListener extends ChangeListener {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        Gdx.app.exit();
    }
}
