package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;

public class GuideMenu extends BaseMenu{
    private Preferences controlPrefs;
    private Preferences savePrefs;
    private I18NBundle bundle;
    private Screen previousMenu;

    public GuideMenu(HollowKnightEngine engine , Screen previousMenu) {
        super(engine);
        bundle = engine.getBundle();
        this.previousMenu = previousMenu;
    }

    @Override
    public void show() {
        super.show();

        controlPrefs = Gdx.app.getPreferences("HollowKnightControls");
        savePrefs = Gdx.app.getPreferences("HollowKnightSaveData");

        int leftKey = controlPrefs.getInteger("key_left", Input.Keys.LEFT);
        int rightKey = controlPrefs.getInteger("key_right", Input.Keys.RIGHT);
        int jumpKey = controlPrefs.getInteger("key_jump", 62);
        int attackKey = controlPrefs.getInteger("key_attack", 54);

        Table table = new Table();
        table.defaults().pad(10).center();

        Label titleLabel = new Label(bundle.get("lbl_guide_title"), skin);
        Label descLabel = new Label(bundle.get("lbl_how_to_play"), skin);
        table.add(titleLabel).colspan(2).padBottom(10).row();
        table.add(descLabel).colspan(2).padBottom(30).row();

        Label actionHeader = new Label(bundle.get("lbl_action_header"), skin);
        Label keyHeader = new Label(bundle.get("lbl_key_header"), skin);

        table.add(actionHeader).left().padRight(50);
        table.add(keyHeader).right().row();

        table.add(new Label("----------------", skin)).colspan(2).row();

        addRow(table, bundle.get("lbl_move_left"), Input.Keys.toString(leftKey));
        addRow(table, bundle.get("lbl_move_right"), Input.Keys.toString(rightKey));
        addRow(table, bundle.get("lbl_jump"), Input.Keys.toString(jumpKey));
        addRow(table, bundle.get("lbl_attack"), Input.Keys.toString(attackKey));

        if (savePrefs.getBoolean("unlocked_dash", false)) {
            int dashKey = controlPrefs.getInteger("key_dash", Input.Keys.SHIFT_LEFT);
            addRow(table, bundle.get("lbl_dash"), Input.Keys.toString(dashKey));
        }

        table.add(new Label("\n=====================================\n", skin)).colspan(2).padTop(20).padBottom(20).row();

        Label healthTitle = new Label(bundle.get("lbl_health_title"), skin);
        Label healthDesc = new Label(bundle.get("lbl_health_desc"), skin);
        healthDesc.setWrap(true);
        table.add(healthTitle).colspan(2).left().padBottom(5).row();
        table.add(healthDesc).width(500).colspan(2).left().padBottom(30).row();

        Label soulTitle = new Label(bundle.get("lbl_soul_title"), skin);
        Label soulDesc = new Label(bundle.get("lbl_soul_desc"), skin);
        soulDesc.setWrap(true);
        table.add(soulTitle).colspan(2).left().padBottom(5).row();
        table.add(soulDesc).width(500).colspan(2).left().padBottom(30).row();

        TextButton backBtn = new TextButton(bundle.get("btn_back"), skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                playButtonSound();
                engine.setScreen(previousMenu);
            }
        });
        Table backTable = new Table();
        backTable.setFillParent(true);
        backTable.left().top().pad(15);
        backTable.add(backBtn).left().top().width(100);

        table.add(new Label("\n=====================================\n", skin)).colspan(2).padTop(20).padBottom(20).row();

        Label cheatsTitle = new Label(bundle.get("lbl_cheats_title"), skin);
        Label cheatsDesc = new Label(bundle.get("lbl_cheats_desc"), skin);
        cheatsDesc.setWrap(true);
        table.add(cheatsTitle).colspan(2).left().padBottom(5).row();
        table.add(cheatsDesc).width(500).colspan(2).left().padBottom(40).row();

        Label cheatSoulTitle = new Label(bundle.get("lbl_cheat_soul_title"), skin);
        Label cheatSoulDesc = new Label(bundle.get("lbl_cheat_soul_desc"), skin);
        cheatSoulDesc.setWrap(true);
        table.add(cheatSoulTitle).colspan(2).left().padBottom(5).row();
        table.add(cheatSoulDesc).width(500).colspan(2).left().padBottom(20).row();

        Label cheatGodTitle = new Label(bundle.get("lbl_cheat_heal_title"), skin);
        Label cheatGodDesc = new Label(bundle.get("lbl_cheat_heal_desc"), skin);
        cheatGodDesc.setWrap(true);
        table.add(cheatGodTitle).colspan(2).left().padBottom(5).row();
        table.add(cheatGodDesc).width(500).colspan(2).left().padBottom(20).row();




        ScrollPane scrollPane = new ScrollPane(table , skin);
        scrollPane.setFillParent(true);
        scrollPane.setFadeScrollBars(false);
        stage.addActor(scrollPane);
        stage.addActor(backTable);
        stage.setScrollFocus(scrollPane);
    }

    private void addRow(Table table, String actionName, String keyName) {
        Label actionLabel = new Label(actionName, skin);
        Label keyLabel = new Label("[" + keyName + "]", skin);

        table.add(actionLabel).left().padRight(50);
        table.add(keyLabel).right().row();
    }
}
