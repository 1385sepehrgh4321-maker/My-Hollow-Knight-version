package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.Sepehr.HallowKnight.Menu.Listener.ExitListener;
import com.Sepehr.HallowKnight.Menu.Listener.LoadGameListener;
import com.Sepehr.HallowKnight.Menu.Listener.NewGameListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class MainMenu extends BaseMenu{
    private Table mainMenuTable;
    private Table startGameMenuTable;

    public MainMenu(HollowKnightEngine engine) {
        super(engine);
    }

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
        mainMenuTable.defaults().pad(10).center().spaceBottom(10).width(300).height(50);
        mainMenuTable.bottom();
        mainMenuTable.padBottom(100);
        TextButton startGameBtn = new TextButton("Start Game" , skin);
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
        TextButton settingBtn = new TextButton("Setting" , skin);
        settingBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                engine.setScreen(new SettingMenu(engine));
            }
        });
        mainMenuTable.add(settingBtn).row();
        TextButton guideBtn = new TextButton("Guide" , skin);
        mainMenuTable.add(guideBtn).row();
        TextButton achievementBtn = new TextButton("Achievements" , skin);
        mainMenuTable.add(achievementBtn).row();
        TextButton exitBtn = new TextButton("EXIT" , skin);
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

        TextButton newGameBtn = new TextButton("New Game", skin);
        newGameBtn.addListener(new NewGameListener(engine));
        startGameMenuTable.add(newGameBtn).row();
        for (int i = 1; i < 5; i++) {
            TextButton loadSlot = new TextButton("Load Save " + i, skin);
            loadSlot.addListener(new LoadGameListener(engine));
            startGameMenuTable.add(loadSlot).row();
        }

        TextButton backBtn = new TextButton("Back", skin);
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
