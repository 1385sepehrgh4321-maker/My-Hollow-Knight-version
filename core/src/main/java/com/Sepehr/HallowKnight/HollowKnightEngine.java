package com.Sepehr.HallowKnight;

import com.Sepehr.HallowKnight.Menu.MainMenu;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;

public class HollowKnightEngine extends Game {
    private Music menuMusic;
    private SpriteBatch batch;
    private I18NBundle bundle;
    private int activeSlot = 1;

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

    public void loadLocalization() {
        Preferences settingsPrefs = Gdx.app.getPreferences("HollowKnightSettings");
        String currentLanguage = settingsPrefs.getString("language", "English");
        String localeCode = currentLanguage.equals("Español") ? "es" : "en";
        Locale locale = Locale.forLanguageTag(localeCode);
        this.bundle = I18NBundle.createBundle(Gdx.files.internal("i18n/MyBundle"), locale);
    }

    public I18NBundle getBundle() {
        if (this.bundle == null) {
            loadLocalization();
        }
        return this.bundle;
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

    public SpriteBatch getBatch() {
        return batch;
    }

    public int getActiveSlot() {
        return activeSlot;
    }

    public void setActiveSlot(int activeSlot) {
        this.activeSlot = activeSlot;
    }
}
