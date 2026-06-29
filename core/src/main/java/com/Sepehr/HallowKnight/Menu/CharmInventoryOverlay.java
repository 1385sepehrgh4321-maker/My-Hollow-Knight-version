package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.HollowKnightEngine;
import com.Sepehr.HallowKnight.model.entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CharmInventoryOverlay implements Disposable {
    private final HollowKnightEngine game;
    private final Viewport viewport;
    private final Player player;

    private final SpriteBatch uiBatch;
    private final ShapeRenderer shapeRenderer;
    private final OrthographicCamera uiCamera;
    private final BitmapFont menuFont;

    private float lastMouseX = -1;
    private float lastMouseY = -1;

    // Grid layout parameters inside native screen coordinates
    private final float BUTTON_SIZE = 75f;
    private final float SPACING_X = 20f;
    private final float SPACING_Y = 20f;
    private final float START_X = 115f;
    private final float START_Y = 110f;

    private final CharmGridSlot[] slots;

    // Keyboard Navigation States
    private int selectedIndex = 0;
    private final int COLUMNS = 4;
    private final int ROWS = 2;
    private final int MAX_ACTIVE_CHARMS = 3;

    private static class CharmGridSlot {
        public final CharmType type;
        public final Rectangle bounds;
        public final String cleanName;
        public Texture texture;

        public CharmGridSlot(CharmType type, float x, float y, float size, String cleanName) {
            this.type = type;
            this.bounds = new Rectangle(x, y, size, size);
            this.cleanName = cleanName;
            this.texture = new Texture(Gdx.files.internal(type.getPath()));
        }
    }

    public CharmInventoryOverlay(HollowKnightEngine game, Viewport viewport, Player player) {
        this.game = game;
        this.viewport = viewport;
        this.player = player;

        this.uiBatch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();

        this.uiCamera = new OrthographicCamera();
        this.uiCamera.setToOrtho(false, viewport.getWorldWidth(), viewport.getWorldHeight());
        this.uiCamera.update();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/TrajanPro-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 24;
        this.menuFont = generator.generateFont(param);
        generator.dispose();

        float totalGridWidth = (COLUMNS * BUTTON_SIZE) + ((COLUMNS - 1) * SPACING_X);
        float totalGridHeight = (ROWS * BUTTON_SIZE) + ((ROWS - 1) * SPACING_Y);

        float startX = (viewport.getWorldWidth() - totalGridWidth) / 2f;
        float startY = ((viewport.getWorldHeight() - totalGridHeight) / 2f) + BUTTON_SIZE + SPACING_Y;


        slots = new CharmGridSlot[]{
            new CharmGridSlot(CharmType.QUICK_SLASH, startX, startY, BUTTON_SIZE, "Quick Slash"),
            new CharmGridSlot(CharmType.UNBREAKABLE_STRENGTH, startX + (BUTTON_SIZE + SPACING_X), startY, BUTTON_SIZE, "Unbreakable Strength"),
            new CharmGridSlot(CharmType.DASHMASTER, startX + 2 * (BUTTON_SIZE + SPACING_X), startY, BUTTON_SIZE, "Dashmaster"),
            new CharmGridSlot(CharmType.SOUL_CATCHER, startX + 3 * (BUTTON_SIZE + SPACING_X), startY, BUTTON_SIZE, "Soul Catcher"),

            new CharmGridSlot(CharmType.VOID_HEART, startX, startY - (BUTTON_SIZE + SPACING_Y), BUTTON_SIZE, "Void Heart"),
            new CharmGridSlot(CharmType.SHARP_SHADOW, startX + (BUTTON_SIZE + SPACING_X), startY - (BUTTON_SIZE + SPACING_Y), BUTTON_SIZE, "Sharp Shadow"),
            new CharmGridSlot(CharmType.HEAVY_BLOW, startX + 2 * (BUTTON_SIZE + SPACING_X), startY - (BUTTON_SIZE + SPACING_Y), BUTTON_SIZE, "Heavy Blow"),
            new CharmGridSlot(CharmType.QUICK_FOCUS, startX + 3 * (BUTTON_SIZE + SPACING_X), startY - (BUTTON_SIZE + SPACING_Y), BUTTON_SIZE, "Quick Focus")
        };
    }

    public void render(float delta) {
        Preferences prefs = Gdx.app.getPreferences("HollowKnightSettings");
        float b = prefs.getFloat("brightness", 1.0f);

        Vector3 unprojectedMouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiCamera.unproject(unprojectedMouse, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
        Vector2 mousePos = new Vector2(unprojectedMouse.x, unprojectedMouse.y);

        if (mousePos.x != lastMouseX || mousePos.y != lastMouseY) {
            for (int i = 0; i < slots.length; i++) {
                if (slots[i].bounds.contains(mousePos.x, mousePos.y)) {
                    selectedIndex = i;
                }
            }
            lastMouseX = mousePos.x;
            lastMouseY = mousePos.y;
        }

        int equippedCount = getEquippedCharmsCount();
        handleInputNavigation(equippedCount, mousePos);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(uiCamera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.0f, 0.0f, 0.0f, 0.70f));
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.setColor(new Color(0.15f * b, 0.15f * b, 0.15f * b, 0.85f));
        for (CharmGridSlot slot : slots) {
            shapeRenderer.rect(slot.bounds.x, slot.bounds.y, slot.bounds.width, slot.bounds.height);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(b, b, b, 1f));
        CharmGridSlot activeSlot = slots[selectedIndex];
        shapeRenderer.rect(activeSlot.bounds.x - 3, activeSlot.bounds.y - 3, activeSlot.bounds.width + 6, activeSlot.bounds.height + 6);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        uiBatch.setProjectionMatrix(uiCamera.combined);
        uiBatch.begin();

        menuFont.setColor(b, b, b, 1f);

        String activeHoveredName = slots[selectedIndex].cleanName;
        menuFont.draw(uiBatch, activeHoveredName, 40f, viewport.getWorldHeight() - 40f);

        String capacityText = "Equipped: " + equippedCount + " / " + MAX_ACTIVE_CHARMS;
        menuFont.draw(uiBatch, capacityText, viewport.getWorldWidth() - 260f, viewport.getWorldHeight() - 40f);

        // Draw the item icons over the backgrounds
        for (CharmGridSlot slot : slots) {
            if (slot.texture != null) {
                if (player.isCharmEquipped(slot.type)) {
                    uiBatch.setColor(b, b, b, 1.0f);
                } else {
                    uiBatch.setColor(0.35f * b, 0.35f * b, 0.35f * b, 0.75f);
                }
                uiBatch.draw(slot.texture, slot.bounds.x, slot.bounds.y, slot.bounds.width, slot.bounds.height);
            }
        }

        uiBatch.setColor(Color.WHITE);
        uiBatch.end();
    }

    private void handleInputNavigation(int equippedCount, Vector2 mousePos) {
        int row = selectedIndex / COLUMNS;
        int col = selectedIndex % COLUMNS;

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            col = (col - 1 + COLUMNS) % COLUMNS;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            col = (col + 1) % COLUMNS;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            row = (row - 1 + ROWS) % ROWS;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            row = (row + 1) % ROWS;
        }

        selectedIndex = (row * COLUMNS) + col;

        boolean keyTriggered = Gdx.input.isKeyJustPressed(Input.Keys.Z) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
        boolean mouseClicked = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        if (keyTriggered || (mouseClicked && slots[selectedIndex].bounds.contains(mousePos.x, mousePos.y))) {
            CharmGridSlot currentSlot = slots[selectedIndex];
            boolean isAlreadyActive = player.isCharmEquipped(currentSlot.type);

            if (isAlreadyActive) {
                player.toggleCharmState(currentSlot.type);
                playClickSound();
            } else if (equippedCount < MAX_ACTIVE_CHARMS) {
                player.toggleCharmState(currentSlot.type);
                playClickSound();
            }
        }
    }

    private int getEquippedCharmsCount() {
        int count = 0;
        for (CharmGridSlot slot : slots) {
            if (player.isCharmEquipped(slot.type)) {
                count++;
            }
        }
        return count;
    }

    private void playClickSound() {
        if (game.getMenuMusic() != null) {
            //todo : play the click sound once it added
        }
    }

    public void dispose() {
        uiBatch.dispose();
        shapeRenderer.dispose();
        if (menuFont != null) {
            menuFont.dispose();
        }
        for (CharmGridSlot slot : slots) {
            if (slot.texture != null) {
                slot.texture.dispose();
            }
        }
    }
}
