package com.Sepehr.HallowKnight.view.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ZoteDialogueOverlay {
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private String fullText = "";
    private String displayedText = "";
    private float typingTimer = 0f;
    private final float charactersPerSecond = 35f;
    private boolean isFinishedTyping = false;

    public ZoteDialogueOverlay() {
        this.shapeRenderer = new ShapeRenderer();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("ui/TrajanPro-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1f;
        parameter.borderColor = Color.BLACK;

        this.font = generator.generateFont(parameter);
        generator.dispose();
    }

    public void startMessage(String message) {
        this.fullText = message;
        this.displayedText = "";
        this.typingTimer = 0f;
        this.isFinishedTyping = false;
    }

    public void update(float delta) {
        if (isFinishedTyping) return;

        typingTimer += delta;
        int visibleCharacters = (int) (typingTimer * charactersPerSecond);

        if (visibleCharacters >= fullText.length()) {
            displayedText = fullText;
            isFinishedTyping = true;
        } else {
            displayedText = fullText.substring(0, visibleCharacters);
        }
    }

    public void render(SpriteBatch batch, OrthographicCamera camera) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.85f);

        float boxWidth = 750f;
        float boxHeight = 120f;
        float boxX = camera.position.x - (boxWidth / 2f);
        float boxY = camera.position.y - (camera.viewportHeight / 2f) + 40f;

        shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
        shapeRenderer.end();

        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        font.draw(batch, "ZOTE THE MIGHTY:", boxX + 25f, boxY + boxHeight - 20f);
        font.draw(batch, displayedText, boxX + 25f, boxY + boxHeight - 50f, boxWidth - 50f, -1, true);

        if (isFinishedTyping) {
            font.draw(batch, "[W / UP] Continue", boxX + boxWidth - 180f, boxY + 25f);
        }
        batch.end();
    }

    public boolean isFinishedTyping() { return isFinishedTyping; }
    public void skipTyping() {
        this.displayedText = fullText;
        this.isFinishedTyping = true;
    }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}
