package com.Sepehr.HallowKnight.model.world;

import com.Sepehr.HallowKnight.model.entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;

public class GameWorld {
    private BitmapFont font;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Player player;
    private Array<Rectangle> solidTiles = new Array<>();
    private Array<Rectangle> hazardTiles = new Array<>();

    //audio
    private MiniAudio miniAudio;
    private MASound backgroundMusic;

    //trigger and spawn
    private Rectangle nextMapPortal = null;
    private String nextMapTargetName = "";
    private boolean shouldTransition = false;


    public GameWorld(String mapPath) {
        this.map = new TmxMapLoader().load(mapPath);
        this.mapRenderer = new OrthogonalTiledMapRenderer(map);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/TrajanPro-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 18;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1.5f;
        parameter.borderColor = Color.BLACK;
        this.font = generator.generateFont(parameter);
        generator.dispose();

        this.miniAudio = new MiniAudio();
        float spawnX = 100;
        float spawnY = 100;
        int mapHeightInTiles = map.getProperties().get("height", Integer.class);
        int tileHeightInPixels = map.getProperties().get("tileheight", Integer.class);
        float totalMapHeightPixels = mapHeightInTiles * tileHeightInPixels;
        if (map.getLayers().get("player spawn") != null) {
            MapObjects spawnObjects = map.getLayers().get("player spawn").getObjects();
            if(spawnObjects.get("Spawn") != null) {
                PointMapObject spawn = (PointMapObject)spawnObjects.get("Spawn");
                spawnX = spawn.getProperties().get("x" , Float.class);
                spawnY = totalMapHeightPixels - spawn.getProperties().get("y" , Float.class);
            }
        }
        this.player = new Player(spawnX, spawnY , miniAudio);

        backgroundMusic = miniAudio.createSound("audio/Flower Wings.mp3");
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.3f);
        backgroundMusic.play();

        if (map.getLayers().get("trigger") != null) {
            MapObjects triggerObjects = map.getLayers().get("trigger").getObjects();
            for (MapObject object : triggerObjects) {
                if ("next map portal".equals(object.getName()) && object instanceof RectangleMapObject) {
                    this.nextMapPortal = ((RectangleMapObject) object).getRectangle();
                    float portalX = object.getProperties().get("x", Float.class);
                    float portalY = object.getProperties().get("y", Float.class);
                    if (object.getProperties().containsKey("nextMap")) {
                        this.nextMapTargetName = object.getProperties().get("nextMap", String.class);
                    } else {
                        this.nextMapTargetName = "maps/City of Tears.tmx";
                    }
                }
            }
        }

        MapObjects objects = map.getLayers().get("wall").getObjects();
        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                if (object.getProperties().containsKey("killing") &&
                    object.getProperties().get("killing", Boolean.class).equals(true)) {
                    hazardTiles.add(rect);
                } else {
                    solidTiles.add(rect);
                }
            }
        }
    }

    public void update(float delta) {
        player.update(delta);
        handleCollisions(delta);
        checkMapTransitions();
    }

    private void checkMapTransitions() {
        if (nextMapPortal != null && player.getHitbox().overlaps(nextMapPortal)) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.W) ||
                Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {
                this.shouldTransition = true;
            }
        }
    }

    private void handleCollisions(float delta) {
        boolean touchingWallLeft = false;
        boolean touchingWallRight = false;
        Vector2 pos = player.getPosition();
        Vector2 vel = player.getVelocity();
        Rectangle hitbox = player.getHitbox();

        pos.x += vel.x * delta;
        player.updateHitbox();

        for (Rectangle tile : solidTiles) {
            if (hitbox.overlaps(tile)) {
                if (vel.x > 0) {
                    pos.x = tile.x - hitbox.width;
                    touchingWallRight = true;
                } else if (vel.x < 0) {
                    pos.x = tile.x + tile.width;
                    touchingWallLeft = true;
                }
                vel.x = 0;
                player.updateHitbox();
            }
        }
        player.setWallStates(touchingWallLeft, touchingWallRight);

        pos.y += vel.y * delta;
        player.updateHitbox();
        player.setGrounded(false);

        for (Rectangle tile : solidTiles) {
            if (hitbox.overlaps(tile)) {
                if (vel.y < 0) {
                    pos.y = tile.y + tile.height;
                    vel.y = 0;
                    player.setGrounded(true);
                } else if (vel.y > 0) {
                    pos.y = tile.y - hitbox.height;
                    vel.y = 0;
                }
                player.updateHitbox();
            }
        }

        for (Rectangle hazard : hazardTiles) {
            if (player.getHitbox().overlaps(hazard)) {
                player.takeHazardDamage(1);
                break;
            }
        }
    }

    public void render(OrthographicCamera camera, SpriteBatch batch) {
        com.badlogic.gdx.utils.ScreenUtils.clear(0, 0, 0, 1);

        batch.setProjectionMatrix(camera.combined);

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.begin();
        player.draw(batch);
        if (isPlayerInPortal()) {
            float textX = player.getPosition().x - 40;
            float textY = player.getPosition().y + player.getHitbox().height + 40;
            font.draw(batch, "[W] Enter", textX, textY);
        }
        batch.end();
    }

    public void dispose() {
        if(player != null)
            player.dispose();
        if(map != null)
            map.dispose();
        if (backgroundMusic != null)
            backgroundMusic.dispose();
        if (miniAudio != null)
            miniAudio.dispose();
    }

    public Player getPlayer() { return player; }

    public TiledMap getMap() {
        return map;
    }

    public boolean isPlayerInPortal() {
        return nextMapPortal != null && player.getHitbox().overlaps(nextMapPortal);
    }
}
