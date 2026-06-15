package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;

public class ControllersMenu extends BaseMenu{
    private Preferences prefs;
    private boolean isListeningForInput = false;
    private String actionToRemap = "";
    private TextButton activeRemapButton = null;
    I18NBundle bundle;

    public ControllersMenu(HollowKnightEngine engine) {
        super(engine);
        bundle = engine.getBundle();
    }

    @Override
    public void show() {
        super.show();

        prefs = Gdx.app.getPreferences("HollowKnightControls");
        Preferences savePrefs = Gdx.app.getPreferences("HollowKnightSaveData");

        int jumpKey = prefs.getInteger("key_jump", 62);
        int attackKey = prefs.getInteger("key_attack", 54);
        int leftKey = prefs.getInteger("key_left", Input.Keys.LEFT);
        int rightKey = prefs.getInteger("key_right", Input.Keys.RIGHT);

        Table table = new Table();
        table.setFillParent(true);
        table.defaults().pad(10).center();
        Label titleLabel = new Label(bundle.get("lbl_rebind_title"), skin);
        table.add(titleLabel).colspan(2).padBottom(30).row();

        //left
        Label leftLabel = new Label(bundle.get("lbl_move_left"), skin);
        final TextButton leftBtn = new TextButton(Input.Keys.toString(leftKey), skin);
        leftBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startListening("key_left", leftBtn);
            }
        });
        table.add(leftLabel).left();
        table.add(leftBtn).width(150).row();

        //right
        Label rightLabel = new Label(bundle.get("lbl_move_right"), skin);
        final TextButton rightBtn = new TextButton(Input.Keys.toString(rightKey), skin);
        rightBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startListening("key_right", rightBtn);
            }
        });
        table.add(rightLabel).left();
        table.add(rightBtn).width(150).row();

        //jump
        Label jumpLabel = new Label(bundle.get("lbl_jump"), skin);
        final TextButton jumpBtn = new TextButton(Input.Keys.toString(jumpKey), skin);
        jumpBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startListening("key_jump", jumpBtn);
            }
        });
        table.add(jumpLabel).left();
        table.add(jumpBtn).width(150).row();

        //attack
        Label attackLabel = new Label(bundle.get("lbl_attack"), skin);
        final TextButton attackBtn = new TextButton(com.badlogic.gdx.Input.Keys.toString(attackKey), skin);
        attackBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startListening("key_attack", attackBtn);
            }
        });
        table.add(attackLabel).left();
        table.add(attackBtn).width(150).row();

        //dash
        if(savePrefs.getBoolean("unlocked_dash" , false)) {
            int dashKey = prefs.getInteger("key_dash", Input.Keys.SHIFT_LEFT);

            Label dashLabel = new Label(bundle.get("lbl_dash"), skin);
            final TextButton dashBtn = new TextButton(Input.Keys.toString(dashKey) , skin);
            dashBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    startListening("key_dash" , dashBtn);
                }
            });
            table.add(dashLabel).left();
            table.add(dashBtn).width(150).row();
        }

        Label resetLabel = new Label(bundle.get("lbl_reset_controllers"), skin);
        TextButton resetBtn = new TextButton(bundle.get("btn_reset"), skin);
        resetBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.remove("key_left");
                prefs.remove("key_right");
                prefs.remove("key_jump");
                prefs.remove("key_attack");
                prefs.flush();

                leftBtn.setText(Input.Keys.toString(Input.Keys.LEFT));
                rightBtn.setText(Input.Keys.toString(Input.Keys.RIGHT));
                jumpBtn.setText(Input.Keys.toString(62));
                attackBtn.setText(Input.Keys.toString(54));
            }
        });
        table.add(resetLabel).left();
        table.add(resetBtn).width(150).row();

        //back
        TextButton backBtn = new TextButton("Back" , skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                engine.setScreen(new SettingMenu(engine));
            }
        });
        table.add(backBtn).colspan(2).padTop(25).row();

        stage.addActor(table);

        setupInputProcessor();
    }

    private void startListening(String actionPreferenceName, TextButton associatedButton) {
        isListeningForInput = true;
        actionToRemap = actionPreferenceName;
        activeRemapButton = associatedButton;
        associatedButton.setText("[ Press Any Key ]");
    }

    private void setupInputProcessor() {
        InputMultiplexer multiplexer = new InputMultiplexer();

        multiplexer.addProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                if (isListeningForInput && activeRemapButton != null) {
                    if(keycode == Input.Keys.ESCAPE){
                        isListeningForInput = false;
                        activeRemapButton.setText(Input.Keys.toString(prefs.getInteger(actionToRemap)));
                        actionToRemap = "";
                        return true;
                    }
                    prefs.putInteger(actionToRemap, keycode);
                    prefs.flush();

                    activeRemapButton.setText(Input.Keys.toString(keycode));
                    isListeningForInput = false;
                    activeRemapButton = null;
                    actionToRemap = "";
                    return true;
                }
                return false;
            }
        });
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void hide() {
        super.hide();
        Gdx.input.setInputProcessor(null);
    }
}
