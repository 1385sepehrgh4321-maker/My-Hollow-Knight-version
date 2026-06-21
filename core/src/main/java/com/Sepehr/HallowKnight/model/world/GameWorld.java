package com.Sepehr.HallowKnight.model.world;

import com.Sepehr.HallowKnight.model.entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GameWorld {
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Player player;
    private Array<Rectangle> solidTiles = new Array<>();
    private Array<Rectangle> hazardTiles = new Array<>();

    public GameWorld(String mapPath) {
        this.map = new TmxMapLoader().load(mapPath);
        this.mapRenderer = new OrthogonalTiledMapRenderer(map);
        this.player = new Player(100, 500);

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
    }

    private void handleCollisions(float delta) {
        Vector2 pos = player.getPosition();
        Vector2 vel = player.getVelocity();
        Rectangle hitbox = player.getHitbox();

        pos.x += vel.x * delta;
        player.updateHitbox();

        for (Rectangle tile : solidTiles) {
            if (hitbox.overlaps(tile)) {
                if (vel.x > 0) {
                    pos.x = tile.x - hitbox.width;
                } else if (vel.x < 0) {
                    pos.x = tile.x + tile.width;
                }
                vel.x = 0;
                player.updateHitbox();
            }
        }

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
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        player.draw(batch);
        batch.end();
    }

    public void dispose() {
        if(player != null)
            player.dispose();
        if(map != null)
            map.dispose();
    }

    public Player getPlayer() { return player; }

    public TiledMap getMap() {
        return map;
    }
}
