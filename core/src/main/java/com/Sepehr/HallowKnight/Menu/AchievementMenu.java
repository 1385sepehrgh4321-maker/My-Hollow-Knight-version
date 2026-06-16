package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;

public class AchievementMenu extends BaseMenu{
    private Preferences savePrefs;
    private I18NBundle bundle;

    public AchievementMenu(HollowKnightEngine engine) {
        super(engine);
        bundle = engine.getBundle();
    }

    @Override
    public void show() {
        super.show();
        savePrefs = Gdx.app.getPreferences("HollowKnightSaveData");

        Table table = new Table();
        table.defaults().pad(12).center();

        Label titleLabel = new Label(bundle.get("lbl_achievements_title"), skin);
        Label descLabel = new Label(bundle.get("lbl_achievements_desc"), skin);
        descLabel.setWrap(true);

        table.add(titleLabel).colspan(2).padBottom(10).row();
        table.add(descLabel).width(550).colspan(2).center().padBottom(25).row();
        table.add(new Label("=======================================================", skin)).colspan(2).padBottom(15).row();

        addAchievementRow(table, "ach_completion", "lbl_ach_completion_title", "lbl_ach_completion_desc");
        addAchievementRow(table, "ach_speedrun", "lbl_ach_speedrun_title", "lbl_ach_speedrun_desc");
        addAchievementRow(table, "ach_true_hunter", "lbl_ach_hunter_title", "lbl_ach_hunter_desc");
        addAchievementRow(table, "ach_false_knight", "lbl_ach_false_knight_title", "lbl_ach_false_knight_desc");
        addAchievementRow(table, "ach_custom_slot", "lbl_ach_custom_title", "lbl_ach_custom_desc");

        ScrollPane scrollPane = new ScrollPane(table, skin);
        scrollPane.setFillParent(true);
        scrollPane.setFadeScrollBars(false);
        stage.addActor(scrollPane);

        TextButton backBtn = new TextButton(bundle.get("btn_back"), skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                engine.setScreen(new MainMenu(engine));
            }
        });

        Table backTable = new Table();
        backTable.setFillParent(true);
        backTable.top().left().pad(15);
        backTable.add(backBtn).left().top().width(100);
        stage.addActor(backTable);

        stage.setScrollFocus(scrollPane);
    }

    private void addAchievementRow(Table table, String saveKey, String titleKey, String descKey) {
        boolean isUnlocked = savePrefs.getBoolean(saveKey, false);

        Table textBlock = new Table();
        Label achTitle = new Label(bundle.get(titleKey), skin);
        Label achDesc = new Label(bundle.get(descKey), skin);
        achDesc.setWrap(true);

        textBlock.add(achTitle).left().row();
        textBlock.add(achDesc).width(400).left();

        Label statusLabel;

        if (isUnlocked) {
            statusLabel = new Label(bundle.get("lbl_status_unlocked"), skin);
            statusLabel.setColor(Color.GREEN);

            achTitle.setColor(Color.GOLD);
            achDesc.setColor(Color.WHITE);
        } else {
            statusLabel = new Label(bundle.get("lbl_status_locked"), skin);
            statusLabel.setColor(Color.GRAY);

            achTitle.setColor(Color.LIGHT_GRAY);
            achDesc.setColor(Color.DARK_GRAY);
        }

        table.add(textBlock).left().padRight(40);
        table.add(statusLabel).right().row();

        table.add(new Label("-----------------------------------------------------------------------", skin)).colspan(2).pad(5).row();
    }
}
