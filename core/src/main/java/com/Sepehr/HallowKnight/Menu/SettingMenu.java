package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;

public class SettingMenu extends BaseMenu{
    private Preferences prefs;
    I18NBundle bundle;

    public SettingMenu(HollowKnightEngine engine) {
        super(engine);
        bundle = engine.getBundle();
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

        TextButton editControllers = new TextButton(bundle.get("btn_edit_controllers") , skin);
        editControllers.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                engine.setScreen(new ControllersMenu(engine));
            }
        });

        final CheckBox muteCheckBox = new CheckBox(bundle.get("chk_mute"), skin);
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

        Label volumeLabel = new Label(bundle.get("lbl_music_volume"), skin);
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

        TextButton resetSfx = new TextButton(bundle.get("btn_reset_audio") , skin);
        resetSfx.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("volume" , 0.7f);
                prefs.putBoolean("muted" , false);
                prefs.flush();

                volumeSlider.setValue(0.7f);
                muteCheckBox.setChecked(false);

                if(engine.getMenuMusic() != null)
                    engine.getMenuMusic().setVolume(0.7f);
            }
        });

        Label brightnessLabel = new Label(bundle.get("lbl_brightness"), skin);
        final Slider brightnessSlider = new Slider(0.2f, 1f, 0.05f, false, skin);
        brightnessSlider.setValue(currentBrightness);
        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("brightness", brightnessSlider.getValue());
                prefs.flush();
            }
        });

        Label languageLabel = new Label(bundle.get("lbl_language"), skin);
        final SelectBox<String> languageSelect = new SelectBox<>(skin);
        languageSelect.setItems("English", "Español");
        languageSelect.setSelected(currentLanguage);
        languageSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedLang = languageSelect.getSelected();
                prefs.putString("language", selectedLang);
                prefs.flush();
                engine.loadLocalization();
                engine.setScreen(new SettingMenu(engine));
            }
        });

        TextButton backBtn = new TextButton(bundle.get("btn_back"), skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                engine.setScreen(new MainMenu(engine));
            }
        });



        table.add(volumeLabel).colspan(2).row();
        table.add(volumeSlider).colspan(2).width(300).row();
        table.add(muteCheckBox).left().padRight(10);
        table.add(resetSfx).width(100).right().row();

        table.add(brightnessLabel).colspan(2).row();
        table.add(brightnessSlider).colspan(2).width(300).row();

        table.add(languageLabel).colspan(2).row();
        table.add(languageSelect).colspan(2).width(200).row();

        table.add(editControllers).colspan(2).width(250).padTop(10).row();

        table.add(backBtn).colspan(2).padTop(25).row();

        stage.addActor(table);
    }
}
