package com.Sepehr.HallowKnight.Menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class PauseMenu{
    private final Stage stage;
    private final Table table;

    public interface PauseListener {
        void onContinue();
        void onShowCheats();
        void onOpenSettings();
        void onSaveAndExit();
    }

    public PauseMenu(Skin sharedSkin, final PauseListener listener) {
        this.stage = new Stage(new FitViewport(1280, 720));
        this.table = new Table();
        this.table.setFillParent(true);

        // Create a semi-transparent dark overlay background color
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0.65f));
        pixmap.fill();
        Texture bgTexture = new Texture(pixmap);
        pixmap.dispose();
        this.table.setBackground(new TextureRegionDrawable(bgTexture));

        Label.LabelStyle labelStyle = new Label.LabelStyle(sharedSkin.getFont("Hollowfont"), Color.WHITE);
        Label titleLabel = new Label("PAUSED", labelStyle);

        TextButton continueBtn = new TextButton("Continue", sharedSkin, "default");
        TextButton cheatsBtn = new TextButton("Cheat Codes", sharedSkin, "default");
        TextButton settingsBtn = new TextButton("Settings", sharedSkin, "default");
        TextButton saveExitBtn = new TextButton("Save & Exit", sharedSkin, "default");

        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onContinue();
            }
        });

        cheatsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onShowCheats();
            }
        });

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onOpenSettings();
            }
        });

        saveExitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onSaveAndExit();
            }
        });
        table.add(titleLabel).padBottom(50f).row();
        table.add(continueBtn).padBottom(20f).width(300).height(50).row();
        table.add(cheatsBtn).padBottom(20f).width(300).height(50).row();
        table.add(settingsBtn).padBottom(20f).width(300).height(50).row();
        table.add(saveExitBtn).width(300).height(50).row();

        stage.addActor(table);
    }

    public void setInputFocus() {
        Gdx.input.setInputProcessor(stage);
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void updateAndDraw(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
    }
}
