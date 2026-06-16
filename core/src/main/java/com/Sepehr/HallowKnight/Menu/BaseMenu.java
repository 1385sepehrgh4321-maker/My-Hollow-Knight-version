package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;

public abstract class BaseMenu implements Screen {
    protected Stage stage;
    protected Skin skin;
    protected HollowKnightEngine engine;

    private Texture backgroundTexture;
    private Image backgroundImage;

    public BaseMenu(HollowKnightEngine engine) {
        this.engine = engine;
        this.stage = new Stage(new FitViewport(1280 , 720));
        skin = new Skin();

        // 2. Safely add the atlas regions manually first
        com.badlogic.gdx.graphics.g2d.TextureAtlas atlas =
            new com.badlogic.gdx.graphics.g2d.TextureAtlas(Gdx.files.internal("ui/HollowSkin.atlas"));
        skin.addRegions(atlas);

        // 3. Generate the TTF Font manually in Java
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/TrajanPro-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 24;
        BitmapFont font = generator.generateFont(param);
        generator.dispose(); // Free generator memory

        // 4. Inject the font into the skin pool BEFORE loading the JSON file
        skin.add("Hollowfont", font, BitmapFont.class);

        // 5. Now that "Hollowfont" is safely registered, load the JSON structure
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

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width , height , true);
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
