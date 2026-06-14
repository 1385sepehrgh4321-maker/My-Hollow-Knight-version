package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class SettingMenu extends BaseMenu{
    private Preferences prefs;

    public SettingMenu(HollowKnightEngine engine) {
        super(engine);
    }

    @Override
    public void show() {
        super.show();
        prefs = Gdx.app.getPreferences("HollowKnightSettings");
        float currentVolume = prefs.getFloat("volume", 0.7f);
        float currentBrightness = prefs.getFloat("brightness", 1.0f);
        boolean isMuted = prefs.getBoolean("muted", false);
        String currentLanguage = prefs.getString("language", "English");

        Table table = new Table();
        table.setFillParent(true);
        table.defaults().pad(12).center();

        TextButton editControllers = new TextButton("Edit Controllers" , skin);
        editControllers.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                engine.setScreen(new ControllersMenu(engine));
            }
        });

        final CheckBox muteCheckBox = new CheckBox(" Mute Audio", skin);
        muteCheckBox.setChecked(isMuted);
        muteCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                boolean checked = muteCheckBox.isChecked();
                prefs.putBoolean("muted", checked);
                prefs.flush(); // Save permanently


                if (engine.getMenuMusic() != null) {
                    if (checked) {
                        engine.getMenuMusic().setVolume(0f); // Silence it
                    } else {
                        float activeVolume = prefs.getFloat("volume", 0.7f);
                        engine.getMenuMusic().setVolume(activeVolume);
                    }
                }
            }
        });

        Label volumeLabel = new Label("Music Volume", skin);
        final Slider volumeSlider = new Slider(0f, 1f, 0.05f, false, skin);
        volumeSlider.setValue(currentVolume);
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                prefs.putFloat("volume", volumeSlider.getValue());
                prefs.flush();

                if (engine.getMenuMusic() != null && !prefs.getBoolean("muted", false)) {
                    engine.getMenuMusic().setVolume(volumeSlider.getValue());
                }
            }
        });

        Label brightnessLabel = new Label("Brightness", skin);
        final Slider brightnessSlider = new Slider(0.2f, 1f, 0.05f, false, skin);
        brightnessSlider.setValue(currentBrightness);
        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("brightness", brightnessSlider.getValue());
                prefs.flush();
            }
        });

        Label languageLabel = new Label("Language", skin);
        final SelectBox<String> languageSelect = new SelectBox<>(skin);
        languageSelect.setItems("English", "Español", "Deutsch", "Français", "Persian");
        languageSelect.setSelected(currentLanguage);
        languageSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedLang = languageSelect.getSelected();
                prefs.putString("language", selectedLang);
                prefs.flush();
                System.out.println("Language swapped to: " + selectedLang);
            }
        });

        TextButton backBtn = new TextButton("Back", skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                engine.setScreen(new MainMenu(engine));
            }
        });



        table.add(volumeLabel).row();
        table.add(volumeSlider).width(300).row();
        table.add(muteCheckBox).row();

        table.add(brightnessLabel).row();
        table.add(brightnessSlider).width(300).row();

        table.add(languageLabel).row();
        table.add(languageSelect).width(200).row();

        table.add(editControllers).width(250).padTop(10).row();

        table.add(backBtn).padTop(25).row();

        stage.addActor(table);
    }
}
