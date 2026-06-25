package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.Sepehr.HallowKnight.model.world.GameWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameplayScreen implements Screen , PauseMenu.PauseListener {
    private final HollowKnightEngine engine;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private GameWorld world;
    private float mapWidthPixels;
    private float mapHeightPixels;

    private boolean isPaused = false;
    private PauseMenu pauseMenu;
    private Skin pauseSkin;

    public GameplayScreen(HollowKnightEngine engine) {
        this.engine = engine;
    }

    public void show() {
        if (world == null) {
            camera = new OrthographicCamera();
            viewport = new FitViewport(1280, 720, camera);
            world = new GameWorld("maps/Greenpath Hollow Knight/Hollow GreenPath.tmx");

            com.badlogic.gdx.maps.tiled.TiledMap map = world.getMap();
            com.badlogic.gdx.maps.MapProperties prop = map.getProperties();
            int mapWidthInTiles = prop.get("width", Integer.class);
            int mapHeightInTiles = prop.get("height", Integer.class);
            int tileWidth = prop.get("tilewidth", Integer.class);
            int tileHeight = prop.get("tileheight", Integer.class);

            this.mapWidthPixels = mapWidthInTiles * tileWidth;
            this.mapHeightPixels = mapHeightInTiles * tileHeight;
            int currentSlot = engine.getActiveSlot();
            Preferences slotPrefs = Gdx.app.getPreferences("HollowKnightSaveData_Slot_" + currentSlot);

            if (slotPrefs.getBoolean("has_saved_data", false) && world.getPlayer() != null) {
                float savedX = slotPrefs.getFloat("player_spawn_x");
                float savedY = slotPrefs.getFloat("player_spawn_y");

                world.getPlayer().getPosition().set(savedX, savedY);
                world.getPlayer().getVelocity().set(0, 0);
                world.getPlayer().updateHitbox();
                world.getPlayer().resetSpawnProtection();

                float camX = savedX + world.getPlayer().getHitbox().width  / 2f;
                float camY = savedY + world.getPlayer().getHitbox().height / 2f;

                float minX = viewport.getWorldWidth()  / 2f;
                float maxX = mapWidthPixels - viewport.getWorldWidth()  / 2f;
                float minY = viewport.getWorldHeight() / 2f;
                float maxY = mapHeightPixels - viewport.getWorldHeight() / 2f;

                camera.position.set(
                    com.badlogic.gdx.math.MathUtils.clamp(camX, minX, maxX),
                    com.badlogic.gdx.math.MathUtils.clamp(camY, minY, maxY),
                    0
                );
                camera.update();
            }

            setupPauseSkin();
            pauseMenu = new PauseMenu(pauseSkin, this);
        }

        if (engine.getMenuMusic() != null && engine.getMenuMusic().isPlaying()) {
            engine.getMenuMusic().stop();
        }

        if (isPaused && pauseMenu != null) {
            pauseMenu.setInputFocus();
        }
    }
    private void setupPauseSkin() {
        pauseSkin = new Skin();
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/HollowSkin.atlas"));
        pauseSkin.addRegions(atlas);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/TrajanPro-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 24;
        BitmapFont font = generator.generateFont(param);
        generator.dispose();

        pauseSkin.add("Hollowfont", font, BitmapFont.class);
        pauseSkin.load(Gdx.files.internal("ui/HollowSkin.json"));
    }

    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!isPaused) {
                pauseGame();
            } else {
                onContinue();
            }
        }

        if (!isPaused) {
            world.update(delta);
        }

        if (world.getPlayer() != null) {
            float targetX = world.getPlayer().getPosition().x;
            float targetY = world.getPlayer().getPosition().y;

            float minX = viewport.getWorldWidth() / 2f;
            float maxX = mapWidthPixels - (viewport.getWorldWidth() / 2f);

            float minY = viewport.getWorldHeight() / 2f;
            float maxY = mapHeightPixels - (viewport.getWorldHeight() / 2f);

            float clampedX = com.badlogic.gdx.math.MathUtils.clamp(targetX, minX, maxX);
            float clampedY = com.badlogic.gdx.math.MathUtils.clamp(targetY, minY, maxY);

            camera.position.set(clampedX, clampedY, 0);

        }
        camera.update();

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        world.render(camera, engine.getBatch());

        if (isPaused) {
            pauseMenu.updateAndDraw(delta);
        }
    }

    private void pauseGame() {
        isPaused = true;
        pauseMenu.setInputFocus();
    }

    public void onContinue() {
        isPaused = false;
        if (world != null && world.getPlayer() != null) {
            world.getPlayer().loadKeyBindings();
        }
        Gdx.input.setInputProcessor(null);
    }

    public void onSaveAndExit() {
        if (world != null && world.getPlayer() != null) {
            int currentSlot = engine.getActiveSlot();
            Preferences slotPrefs = Gdx.app.getPreferences("HollowKnightSaveData_Slot_" + currentSlot);
            float xToSave = world.getPlayer().getPosition().x;
            float yToSave = world.getPlayer().getPosition().y + 50;
            slotPrefs.putFloat("player_spawn_x", xToSave);
            slotPrefs.putFloat("player_spawn_y", yToSave);
            slotPrefs.putBoolean("has_saved_data", true);
            slotPrefs.flush();
        }
        this.dispose();
        engine.setScreen(new MainMenu(engine));
    }

    public void onOpenSettings() {
        engine.setScreen(new SettingMenu(engine , this));
    }

    public void onShowCheats() {
        engine.setScreen(new GuideMenu(engine , this));
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        if (world != null) {
            world.dispose();
            world = null;
        }
        if (pauseMenu != null) {
            pauseMenu.dispose();
            pauseMenu = null;
        }
        if (pauseSkin != null) {
            pauseSkin.dispose();
            pauseSkin = null;
        }
    }
}
