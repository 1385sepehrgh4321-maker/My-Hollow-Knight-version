package com.Sepehr.HallowKnight;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MyGame extends ApplicationAdapter {
    private HollowKnightEngine engine;

    @Override
    public void create() {
        engine = new HollowKnightEngine();
        engine.create();
    }

    @Override
    public void resize(int width, int height) {
        engine.resize(width , height);
    }

    @Override
    public void render() {
        engine.render();
    }

    @Override
    public void dispose() {
        engine.dispose();
    }
}
