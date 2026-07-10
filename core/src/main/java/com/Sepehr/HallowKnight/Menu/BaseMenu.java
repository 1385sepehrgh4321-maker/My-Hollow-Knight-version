package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;

public abstract class BaseMenu implements Screen {
    protected Stage stage;
    protected Skin skin;
    protected HollowKnightEngine engine;

    private Texture backgroundTexture;
    private Image backgroundImage;

    public BaseMenu(HollowKnightEngine engine) {
        this.engine = engine;
        this.stage = new Stage(new FitViewport(1280, 720));
        skin = new Skin();

        com.badlogic.gdx.graphics.g2d.TextureAtlas atlas =
            new com.badlogic.gdx.graphics.g2d.TextureAtlas(Gdx.files.internal("ui/HollowSkin.atlas"));
        skin.addRegions(atlas);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/TrajanPro-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 24;
        BitmapFont font = generator.generateFont(param);
        generator.dispose();

        skin.add("Hollowfont", font, BitmapFont.class);
        skin.load(Gdx.files.internal("ui/HollowSkin.json"));

        backgroundTexture = new Texture(Gdx.files.internal("sprites/Hollow Knight/Menu/controller_prompt_bg 2026.png"));
        backgroundImage = new Image(backgroundTexture);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this.stage);
    }

    @Override
    public void render(float delta) {
        Preferences prefs = Gdx.app.getPreferences("HollowKnightSettings");
        float b = prefs.getFloat("brightness", 1.0f);
        Gdx.gl.glClearColor(0.05f * b, 0.05f * b, 0.1f * b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        backgroundImage.setColor(b, b, b, 1f);

        stage.act(delta);
        stage.draw();
    }

    public void playButtonSound() {
        engine.playGlobalButtonSound();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}
