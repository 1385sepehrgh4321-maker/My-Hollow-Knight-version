package com.Sepehr.HallowKnight;

import com.Sepehr.HallowKnight.Menu.MainMenu;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.I18NBundle;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;

import java.util.Locale;

public class HollowKnightEngine extends Game {
    private Music menuMusic;
    private SpriteBatch batch;
    private I18NBundle bundle;
    private int activeSlot = 1;
    private MiniAudio miniAudio;
    private MASound buttonClickSfx;

    @Override
    public void create() {
        miniAudio = new MiniAudio();
        buttonClickSfx = miniAudio.createSound("audio/button.wav");

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

    public void playGlobalButtonSound() {
        if (buttonClickSfx != null) {
            buttonClickSfx.stop();
            buttonClickSfx.setVolume(0.5f);
            buttonClickSfx.play();
        }
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
        if (buttonClickSfx != null)
            buttonClickSfx.dispose();
        if (miniAudio != null)
            miniAudio.dispose();
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

    public MiniAudio getMiniAudio() {
        return this.miniAudio;
    }
}
