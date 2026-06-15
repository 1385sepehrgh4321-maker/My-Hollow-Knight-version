package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.Sepehr.HallowKnight.Menu.Listener.ExitListener;
import com.Sepehr.HallowKnight.Menu.Listener.LoadGameListener;
import com.Sepehr.HallowKnight.Menu.Listener.NewGameListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;

public class MainMenu extends BaseMenu{
    private Table mainMenuTable;
    private Table startGameMenuTable;
    I18NBundle bundle;

    public MainMenu(HollowKnightEngine engine) {
        super(engine);
        bundle = engine.getBundle();
    }

    private Texture logoTexture;

    @Override
    public void show() {
        super.show();

        createMainMenuTable();
        createStartGameMenuTable();

        stage.addActor(mainMenuTable);
        stage.addActor(startGameMenuTable);
    }

    private void createMainMenuTable() {
        mainMenuTable = new Table();
        mainMenuTable.setFillParent(true);
        mainMenuTable.defaults().pad(10).center().spaceBottom(10).width(300).height(30);
        mainMenuTable.bottom();
        mainMenuTable.padBottom(10);

        logoTexture = new Texture(Gdx.files.internal("sprites/Hollow Knight/Menu/vheart_title_spanish.png"));
        Image logoImage = new Image(logoTexture);
        mainMenuTable.add(logoImage).width(600).height(150).padBottom(50).row();

        TextButton startGameBtn = new TextButton(bundle.get("btn_start_game"), skin);
        startGameBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                mainMenuTable.setTouchable(Touchable.disabled);
                mainMenuTable.addAction(Actions.sequence(Actions.alpha(0.3f , 0.3f)));

                startGameMenuTable.setTouchable(Touchable.enabled);
                startGameMenuTable.addAction(Actions.sequence(
                    Actions.show(),
                    Actions.fadeIn(0.3f)));
            }
        });
        mainMenuTable.add(startGameBtn).row();
        TextButton settingBtn = new TextButton(bundle.get("btn_setting"), skin);
        settingBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                engine.setScreen(new SettingMenu(engine));
            }
        });
        mainMenuTable.add(settingBtn).row();
        TextButton guideBtn = new TextButton(bundle.get("btn_guide"), skin);
        guideBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                engine.setScreen(new GuideMenu(engine));
            }
        });
        mainMenuTable.add(guideBtn).row();
        TextButton achievementBtn = new TextButton(bundle.get("btn_achievements"), skin);
        mainMenuTable.add(achievementBtn).row();
        TextButton exitBtn = new TextButton(bundle.get("btn_exit"), skin);
        exitBtn.addListener(new ExitListener());
        mainMenuTable.add(exitBtn).row();
    }

    private void createStartGameMenuTable() {
        startGameMenuTable = new Table();
        startGameMenuTable.setFillParent(true);
        startGameMenuTable.defaults().pad(10).center().width(300).height(50);

        startGameMenuTable.setVisible(false);
        startGameMenuTable.getColor().a = 0f;
        startGameMenuTable.setTouchable(Touchable.disabled);

        TextButton newGameBtn = new TextButton(bundle.get("btn_new_game"), skin);
        newGameBtn.addListener(new NewGameListener(engine));
        startGameMenuTable.add(newGameBtn).row();
        for (int i = 1; i < 5; i++) {
            TextButton loadSlot = new TextButton(bundle.get("lbl_load_save")+ i, skin);
            loadSlot.addListener(new LoadGameListener(engine));
            startGameMenuTable.add(loadSlot).row();
        }

        TextButton backBtn = new TextButton(bundle.get("btn_back"), skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                mainMenuTable.addAction(Actions.alpha(1.0f, 0.3f));
                mainMenuTable.setTouchable(Touchable.enabled);
                mainMenuTable.invalidateHierarchy();

                startGameMenuTable.setTouchable(Touchable.disabled);
                startGameMenuTable.addAction(Actions.sequence(
                    Actions.hide()));
            }
        });

        startGameMenuTable.add(backBtn).padTop(20).row();
    }
}
