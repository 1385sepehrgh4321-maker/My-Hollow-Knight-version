package com.Sepehr.HallowKnight;

import com.Sepehr.HallowKnight.Menu.MainMenu;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HollowKnightEngine extends Game {
    private Music menuMusic;
    private SpriteBatch batch;
    @Override
    public void create() {
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("sprites/Hollow Knight/Audio/stille.mp3"));
        menuMusic.setLooping(true);
        Preferences prefs = Gdx.app.getPreferences("HollowKnightSettings");
        if (prefs.getBoolean("muted", false)) {
            menuMusic.setVolume(0f);
        } else {
            menuMusic.setVolume(prefs.getFloat("volume", 0.7f));
        }
        menuMusic.play();

        batch = new SpriteBatch();
        this.setScreen(new MainMenu(this));

    }

    public Music getMenuMusic() {
        return menuMusic;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        if(batch != null)
            batch.dispose();
        if(menuMusic != null)
            menuMusic.dispose();
    }
}
