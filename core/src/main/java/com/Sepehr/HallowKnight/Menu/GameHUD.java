package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.model.entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameHUD {
    private final OrthographicCamera hudCamera;
    private final Viewport hudViewport;

    // Disposables
    private final Texture texSoulEmpty;
    private final Texture texSoulFull;
    private final Texture texMaskEmpty;
    private final Texture texMaskFull;

    // Regions for rendering logic
    private final TextureRegion soulOrbEmpty;
    private final TextureRegion soulOrbFull;
    private final TextureRegion maskEmpty;
    private final TextureRegion maskFull;

    public GameHUD() {
        this.hudCamera = new OrthographicCamera();
        this.hudViewport = new FitViewport(1280, 720, hudCamera);
        this.hudCamera.position.set(1280f / 2f, 720f / 2f, 0f);
        this.hudCamera.update();

        this.texSoulEmpty = new Texture(Gdx.files.internal("ui/SoulOrb_Empty.png"));
        this.texSoulFull  = new Texture(Gdx.files.internal("ui/SoulOrb_Full.png"));
        this.texMaskEmpty = new Texture(Gdx.files.internal("ui/EmptyHealth.png"));
        this.texMaskFull  = new Texture(Gdx.files.internal("ui/FilledHealth.png"));

        this.soulOrbEmpty = new TextureRegion(texSoulEmpty);
        this.soulOrbFull  = new TextureRegion(texSoulFull);
        this.maskEmpty    = new TextureRegion(texMaskEmpty);
        this.maskFull     = new TextureRegion(texMaskFull);
    }

    public void resize(int width, int height) {
        hudViewport.update(width, height);
    }

    public void draw(SpriteBatch batch, Player player) {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        float topY = 720f - 110f;
        float rightOrbX = 1280f - 130f;
        batch.draw(soulOrbEmpty, rightOrbX, topY, 90, 90);
        float soulPct = player.getSoulPercentage();
        if (soulPct > 0) {
            float soulOffsetX = -8f;
            float soulOffsetY = 0f;
            float soulHeightExtra = 25f;

            int srcWidth = soulOrbFull.getRegionWidth();
            int srcHeight = soulOrbFull.getRegionHeight();
            int clippedHeightPixels = (int) (srcHeight * soulPct);
            int startYPixel = soulOrbFull.getRegionY() + (srcHeight - clippedHeightPixels);

            TextureRegion liquidSlice = new TextureRegion(
                soulOrbFull.getTexture(),
                soulOrbFull.getRegionX(),
                startYPixel,
                srcWidth,
                clippedHeightPixels
            );
            float finalX = rightOrbX + soulOffsetX;
            float finalY = topY + soulOffsetY;
            float finalWidth = 90f;
            float finalHeight = (90f + soulHeightExtra) * soulPct;
            batch.draw(
                liquidSlice,
                finalX,
                finalY,
                finalWidth * 2,
                finalHeight
            );
        }
        float startMaskX = rightOrbX - 50f;
        float maskSpacing = 40f;

        for (int i = 0; i < player.getMaxMasks(); i++) {
            TextureRegion selectedMask = (i < player.getCurrentMasks()) ? maskFull : maskEmpty;
            float currentX = startMaskX - (i * maskSpacing);
            batch.draw(selectedMask, currentX, topY + 15f, 35, 50);
        }

        batch.end();
    }

    public void dispose() {
        texSoulEmpty.dispose();
        texSoulFull.dispose();
        texMaskEmpty.dispose();
        texMaskFull.dispose();
    }

}
