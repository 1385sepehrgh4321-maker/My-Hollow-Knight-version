package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.Sepehr.HallowKnight.model.world.GameWorld;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameplayScreen implements Screen {
    private final HollowKnightEngine engine;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private GameWorld world;
    private float mapWidthPixels;
    private float mapHeightPixels;

    public GameplayScreen(HollowKnightEngine engine) {
        this.engine = engine;
    }

    public void show() {
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

        if (engine.getMenuMusic() != null && engine.getMenuMusic().isPlaying()) {
            engine.getMenuMusic().stop();
        }
    }

    public void render(float delta) {
        world.update(delta);

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
        }
    }
}
