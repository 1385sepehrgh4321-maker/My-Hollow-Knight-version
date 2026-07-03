package com.Sepehr.HallowKnight.model.world;

import com.Sepehr.HallowKnight.model.entities.Zote;
import com.Sepehr.HallowKnight.model.entities.enemies.*;
import com.Sepehr.HallowKnight.model.entities.Player;
import com.Sepehr.HallowKnight.model.entities.spells.HowlingWraiths;
import com.Sepehr.HallowKnight.model.entities.spells.Spell;
import com.Sepehr.HallowKnight.model.entities.spells.VengefulSpirit;
import com.Sepehr.HallowKnight.model.save.JsonSaver;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.MapLayer;
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

import java.util.ArrayList;

public class GameWorld {
    private final BitmapFont font;
    private TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Player player;
    private final Array<Rectangle> solidTiles = new Array<>();
    private final Array<Rectangle> hazardTiles = new Array<>();
    private final ArrayList<Enemy> enemiesList = new ArrayList<>();
    private final Array<Spell> activeSpells = new Array<>();
    private float totalMapHeightPixels;

    private Zote zote = null;

    //audio
    private final MiniAudio miniAudio;
    private MASound backgroundMusic;

    //trigger and spawn
    private Rectangle nextMapPortal = null;
    private String nextMapTargetName = "";
    private boolean shouldTransition = false;

    //transition
    private enum TransitionState { RUNNING, FADE_OUT, FADE_IN }
    private TransitionState transitionState = TransitionState.RUNNING;
    private float transitionTimer = 0f;
    private final float FADE_DURATION = 0.8f;
    private String pendingMapPath = "";
    private String currentMapPath;
    private Texture blackOverlay;

    private final Array<Rectangle> activeLasers = new Array<>();

    public GameWorld(String mapPath, com.Sepehr.HallowKnight.model.save.SaveData saveData) {
        this.map = new TmxMapLoader().load(mapPath);
        this.currentMapPath = mapPath;
        this.mapRenderer = new OrthogonalTiledMapRenderer(map);
        spawnEnemies(this.map);

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
        totalMapHeightPixels = mapHeightInTiles * tileHeightInPixels;

        if (map.getLayers().get("player spawn") != null) {
            MapObjects spawnObjects = map.getLayers().get("player spawn").getObjects();
            if(spawnObjects.get("Spawn") != null) {
                PointMapObject spawn = (PointMapObject)spawnObjects.get("Spawn");
                spawnX = spawn.getProperties().get("x" , Float.class);
                spawnY = totalMapHeightPixels - spawn.getProperties().get("y" , Float.class);
            }
        }

        // --- JSON SAVE STATE OVERRIDE CONTROL ---
        if (saveData != null) {
            spawnX = saveData.playerX;
            spawnY = saveData.playerY;
        }

        this.player = new Player(spawnX, spawnY , miniAudio);

        if (saveData != null) {
            this.player.setCurrentMasks(saveData.masks);
            this.player.setCurrentSoul(saveData.soul);
        }
        // ----------------------------------------

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

        Pixmap pixmap = new Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.blackOverlay = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta) {
        checkCheatInputs();
        activeLasers.clear();

        if (transitionState == TransitionState.FADE_OUT) {
            transitionTimer += delta;

            if (backgroundMusic != null) {
                backgroundMusic.setVolume(Math.max(0f, 0.3f * (1f - (transitionTimer / FADE_DURATION))));
            }

            if (transitionTimer >= FADE_DURATION) {
                performMapSwitch(pendingMapPath);
                transitionState = TransitionState.FADE_IN;
                transitionTimer = 0f;
            }
            return;
        } else if (transitionState == TransitionState.FADE_IN) {
            transitionTimer += delta;

            if (backgroundMusic != null) {
                backgroundMusic.setVolume(Math.min(0.3f, 0.3f * (transitionTimer / FADE_DURATION)));
            }

            if (transitionTimer >= FADE_DURATION) {
                transitionState = TransitionState.RUNNING;
            }
            return;
        }

        player.update(delta);

        if (zote != null && zote.isInConversation()) {
            player.getVelocity().set(0, 0);
        }
        if (zote != null) {
            zote.updateZote(delta, player, solidTiles);
        }

        for (int i = enemiesList.size() - 1; i >= 0; i--) {
            Enemy enemy = enemiesList.get(i);
            if(enemy instanceof HushHornhead)
                ((HushHornhead) enemy).updateAI(delta , player);
            else if(enemy instanceof WingedSentry)
                ((WingedSentry) enemy).updateAI(delta , player);
            else if (enemy instanceof Crystallized) {
                Crystallized crystalHusk = (Crystallized) enemy;
                crystalHusk.updateAI(delta , player);
                if (crystalHusk.isLaserActive()) {
                    int additionalX = (crystalHusk.isFacingRight() ? 12 : -12);
                    float startX = crystalHusk.getPosition().x + (crystalHusk.getHitbox().width / 2f) + additionalX;
                    float laserY = crystalHusk.getPosition().y + (crystalHusk.getHitbox().height / 2f) + 18f;
                    float laserThickness = 6f;
                    float maxRange = 1200f;

                    if (crystalHusk.isFacingRight()) {
                        float closestWallX = startX + maxRange;
                        for (Rectangle wall : solidTiles) {
                            if (wall.y <= laserY && (wall.y + wall.height) >= laserY) {
                                if (wall.x > startX && wall.x < closestWallX) {
                                    closestWallX = wall.x;
                                }
                            }
                        }
                        Rectangle beam = new Rectangle(startX, laserY - (laserThickness / 2f), closestWallX - startX, laserThickness);
                        activeLasers.add(beam);

                        if (player.getHitbox().overlaps(beam)) {
                            if (!player.isCheatGodModeActive() && !player.isCheatNoclipActive()) {
                                player.takeDamage(1, false);
                            }
                        }
                    } else {
                        float closestWallX = startX - maxRange;
                        for (Rectangle wall : solidTiles) {
                            if (wall.y <= laserY && (wall.y + wall.height) >= laserY) {
                                float wallRightEdge = wall.x + wall.width;
                                if (wallRightEdge < startX && wallRightEdge > closestWallX) {
                                    closestWallX = wallRightEdge;
                                }
                            }
                        }
                        Rectangle beam = new Rectangle(closestWallX, laserY - (laserThickness / 2f), startX - closestWallX, laserThickness);
                        activeLasers.add(beam);

                        if (player.getHitbox().overlaps(beam)) {
                            if (!player.isCheatGodModeActive() && !player.isCheatNoclipActive()) { // <-- ENFORCED PROTECTION
                                player.takeDamage(1, true);
                            }
                        }
                    }
                }
            }
            else if(enemy instanceof FalseKnight) {
                ((FalseKnight) enemy).updateAI(delta , player);
            }
            else
                enemy.update(delta);
            if (enemy.isDeadFinished()) {
                enemy.dispose();
                enemiesList.remove(i);
            }
        }
        handleCollisions(delta);
        checkPlayerAttacks();
        handlePlayerSpells(delta);
        checkMapTransitions();
    }

    private void checkCheatInputs() {
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.CONTROL_LEFT)) {

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_1)) {
                if (this.transitionState == TransitionState.RUNNING) {
                    this.transitionState = TransitionState.FADE_OUT;
                    this.transitionTimer = 0f;
                    this.pendingMapPath = "maps/False Knight.tmx";
                }
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_2)) {
                player.setCheatNoclip(!player.isCheatNoclipActive());
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_3)) {
                player.setCurrentMasks(player.getMaxMasks());
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_4)) {
                player.setCurrentSoul(100f);
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_5)) {
                player.setCheatGodMode(!player.isCheatGodModeActive());
            }

            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.NUM_6)) {
                for (Enemy enemy : enemiesList) {
                    if (enemy.getHealth() > 0) {
                        boolean hitFromLeft = player.getPosition().x < enemy.getPosition().x;
                        enemy.takeDamage(999, hitFromLeft);
                    }
                }
            }
        }
    }

    public void spawnEnemies(TiledMap map) {

        MapLayer enemyLayer = map.getLayers().get("enemy spawn");
        if (enemyLayer == null) {
            return;
        }
        for (MapObject object : enemyLayer.getObjects()) {
            if (object instanceof com.badlogic.gdx.maps.objects.RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                float leftBound = rect.x;
                float rightBound = rect.x + rect.width;
                float spawnX = rect.x + (rect.width / 2f);

                float spawnY = rect.y ;
                if (object.getName() != null && object.getName().equalsIgnoreCase("mosscreep")) {

                    Mosscreep mosscreep = new Mosscreep("mosscreep" , spawnX, spawnY, leftBound, rightBound , "New folder/Mosscreep.atlas");
                    enemiesList.add(mosscreep);
                }
                else if (object.getName() != null && object.getName().equalsIgnoreCase("hush hornhead")) {
                    HushHornhead hushHornhead = new HushHornhead("hushhornhead" , spawnX , spawnY , leftBound , rightBound , "New folder/Hash Hornhead.atlas");
                    enemiesList.add(hushHornhead);
                }
                else if (object.getName() != null && object.getName().equalsIgnoreCase("winged sentry")) {
                    WingedSentry wingedSentry = new WingedSentry("wingedsentry" , spawnX , spawnY , leftBound , rightBound , "New folder/Wingedsentry.atlas");
                    enemiesList.add(wingedSentry);
                }
                else if (object.getName() != null && object.getName().equalsIgnoreCase("crystallized")) {
                    Crystallized crystallized = new Crystallized("crystallized" , spawnX , spawnY , leftBound , rightBound , "New folder/Crystallized.atlas");
                    enemiesList.add(crystallized);
                }
                else if(object.getName() != null && object.getName().equalsIgnoreCase("false knight")) {
                    FalseKnight falseKnight = new FalseKnight("falseknight" , spawnX , spawnY , leftBound , rightBound , "New folder/Falseknight.atlas" , 50);
                    enemiesList.add(falseKnight);
                }
                else if(object.getName() != null && object.getName().equalsIgnoreCase("zote")) {
                    this.zote = new Zote(spawnX , spawnY , "New folder/Zote.atlas" , miniAudio);
                    System.out.println("hello there");
                }
            }
        }
    }

    private void checkMapTransitions() {
        if (nextMapPortal != null && player.getHitbox().overlaps(nextMapPortal)) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.W) ||
                Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.UP)) {

                if (transitionState == TransitionState.RUNNING) {
                    this.transitionState = TransitionState.FADE_OUT;
                    this.transitionTimer = 0f;
                    this.pendingMapPath = nextMapTargetName;
                }
            }
        }
    }

    private void handleCollisions(float delta) {
        if (player.isCheatNoclipActive()) {
            Vector2 pos = player.getPosition();
            Vector2 vel = player.getVelocity();

            pos.x += vel.x * delta;
            pos.y += vel.y * delta;

            player.updateHitbox();
            player.setGrounded(false);
            player.setWallStates(false, false);
            return;
        }
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

        for (Enemy enemy : enemiesList) {
            if (enemy.getHealth() > 0 && player.getHitbox().overlaps(enemy.getHitbox())) {

                boolean knockLeft = player.getPosition().x < enemy.getPosition().x;

                player.takeDamage(1, knockLeft);
                enemy.onPlayerHit();

                break;
            }
        }
    }

    private void checkPlayerAttacks() {
        Rectangle nailBox = player.getAttackHitbox();
        if (nailBox == null) return;
        for (Enemy enemy : enemiesList) {
            if (enemy.getHealth() > 0 && nailBox.overlaps(enemy.getHitbox())) {
                boolean hitFromLeft = player.getPosition().x < enemy.getPosition().x;
                enemy.takeDamage(player.getAttackDamage(), hitFromLeft);
                player.onNailConnect();
                player.gainSoul();
                break;
            }
        }
    }

    private void handlePlayerSpells(float delta) {
        Player.SpellType pendingSpell = player.pollPendingSpell();
        if (pendingSpell == Player.SpellType.VENGEFUL_SPIRIT) {
            float spawnX = player.isFacingRight() ? (player.getPosition().x + player.getHitbox().width) : (player.getPosition().x - 40f);
            float spawnY = player.getPosition().y + (player.getHitbox().height / 3f);

            activeSpells.add(new VengefulSpirit(spawnX, spawnY, player.isFacingRight(), new TextureAtlas(Gdx.files.internal("New folder/Shadowball.atlas")), miniAudio));
        }
        else if (pendingSpell == Player.SpellType.HOWLING_WRAITHS) {
            activeSpells.add(new HowlingWraiths(player.getPosition().x, player.getPosition().y, player.getHitbox().width, new TextureAtlas(Gdx.files.internal("New folder/Shadowscream.atlas")) , miniAudio));
        }
        for (int i = activeSpells.size - 1; i >= 0; i--) {
            Spell spell = activeSpells.get(i);
            spell.update(delta);

            boolean hitWall = false;
            for (Rectangle tile : solidTiles) {
                if (spell.getHitbox().overlaps(tile)) {
                    hitWall = true;
                    break;
                }
            }
            if (spell.shouldDestroyOnWalls() && hitWall) {
                spell.destroy();
                activeSpells.removeIndex(i);
                continue;
            }

            for (Enemy enemy : enemiesList) {
                if (enemy.getHealth() > 0 && spell.getHitbox().overlaps(enemy.getHitbox())) {
                    spell.handleEnemyCollision(enemy);
                }
            }
            if (!spell.isActive()) {
                activeSpells.removeIndex(i);
            }
        }
    }

    private void performMapSwitch(String newMapPath) {
        if (this.map != null) this.map.dispose();
        solidTiles.clear();
        hazardTiles.clear();
        activeSpells.clear();

        for (Enemy enemy : enemiesList) {
            enemy.dispose();
        }
        enemiesList.clear();

        this.map = new TmxMapLoader().load(newMapPath);
        this.currentMapPath = newMapPath;
        this.mapRenderer.setMap(this.map);

        int mapHeightInTiles = map.getProperties().get("height", Integer.class);
        int tileHeightInPixels = map.getProperties().get("tileheight", Integer.class);
        totalMapHeightPixels = mapHeightInTiles * tileHeightInPixels;

        float spawnX = 100;
        float spawnY = 100;
        if (map.getLayers().get("player spawn") != null) {
            MapObjects spawnObjects = map.getLayers().get("player spawn").getObjects();
            if (spawnObjects.get("Spawn") != null) {
                PointMapObject spawn = (PointMapObject) spawnObjects.get("Spawn");
                spawnX = spawn.getProperties().get("x", Float.class);
                spawnY = totalMapHeightPixels - spawn.getProperties().get("y", Float.class);
            }
        }
        player.getPosition().set(spawnX, spawnY);
        player.getVelocity().set(0, 0);
        player.updateHitbox();

        spawnEnemies(this.map);

        this.nextMapPortal = null;
        this.nextMapTargetName = "";
        if (map.getLayers().get("trigger") != null) {
            MapObjects triggerObjects = map.getLayers().get("trigger").getObjects();
            for (MapObject object : triggerObjects) {
                if ("next map portal".equals(object.getName()) && object instanceof RectangleMapObject) {
                    this.nextMapPortal = ((RectangleMapObject) object).getRectangle();
                    if (object.getProperties().containsKey("nextMap"))
                        this.nextMapTargetName = object.getProperties().get("nextMap", String.class);
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

        if (backgroundMusic != null) backgroundMusic.dispose();


        String musicTrack = "audio/Flower Wings.mp3";
        if (newMapPath.toLowerCase().contains("city of tears")) {
            musicTrack = "audio/City of Tears.mp3";
        }

        backgroundMusic = miniAudio.createSound(musicTrack);
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0f);
        backgroundMusic.play();
    }

    public void saveCurrentWorldState(int activeSlotNum) {
        com.Sepehr.HallowKnight.model.save.JsonSaver.saveSlot(
            activeSlotNum,
            player.getCurrentMasks(),
            player.getCurrentSoul(),
            this.currentMapPath,
            player.getPosition().x,
            player.getPosition().y
        );
    }

    public void render(OrthographicCamera camera, SpriteBatch batch) {
        com.badlogic.gdx.utils.ScreenUtils.clear(0, 0, 0, 1);

        batch.setProjectionMatrix(camera.combined);

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.begin();
        for (Enemy enemy : enemiesList) {
            enemy.draw(batch);
        }

        if (zote != null) {
            zote.draw(batch);
            zote.drawDialogue(batch, camera);
        }

        for (Spell spell : activeSpells) {
            spell.draw(batch);
        }

        batch.setColor(1f, 0.25f, 0.65f, 0.95f);
        for (Rectangle beam : activeLasers) {
            batch.draw(blackOverlay, beam.x, beam.y, beam.width, beam.height);
        }
        batch.setColor(Color.WHITE);

        player.draw(batch);
        if (zote != null && zote.isPlayerNearby(player) && !zote.isInConversation()) {
            float textX = player.getPosition().x - 40f;
            float textY = player.getPosition().y + player.getHitbox().height + 40f;
            font.draw(batch, "[E] Talk", textX, textY);
        }
        if (isPlayerInPortal()) {
            float textX = player.getPosition().x - 40;
            float textY = player.getPosition().y + player.getHitbox().height + 40;
            font.draw(batch, "[W] Enter", textX, textY);
        }

        if (transitionState != TransitionState.RUNNING) {
            float alpha = 0f;
            if (transitionState == TransitionState.FADE_OUT) {
                alpha = Math.min(1f, transitionTimer / FADE_DURATION);
            } else if (transitionState == TransitionState.FADE_IN) {
                alpha = Math.max(0f, 1f - (transitionTimer / FADE_DURATION));
            }

            batch.setColor(0f, 0f, 0f, alpha);
            batch.draw(
                blackOverlay,
                camera.position.x - camera.viewportWidth / 2f,
                camera.position.y - camera.viewportHeight / 2f,
                camera.viewportWidth,
                camera.viewportHeight
            );
            batch.setColor(Color.WHITE);
        }
        batch.end();
    }

    public void dispose() {
        if (mapRenderer != null)
            mapRenderer.dispose();
        if (blackOverlay != null)
            blackOverlay.dispose();
        if(player != null)
            player.dispose();
        if(map != null)
            map.dispose();
        if (backgroundMusic != null)
            backgroundMusic.dispose();
        if (miniAudio != null)
            miniAudio.dispose();
        for (Enemy enemy : enemiesList) {
            enemy.dispose();
        }
    }

    public Player getPlayer() { return player; }

    public TiledMap getMap() {
        return map;
    }

    public boolean isPlayerInPortal() {
        return nextMapPortal != null && player.getHitbox().overlaps(nextMapPortal);
    }
}
