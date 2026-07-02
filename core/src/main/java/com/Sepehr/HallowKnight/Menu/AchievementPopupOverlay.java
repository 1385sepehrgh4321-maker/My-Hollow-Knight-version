package com.Sepehr.HallowKnight.Menu;

import com.Sepehr.HallowKnight.model.event.AchievementManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class AchievementPopupOverlay implements AchievementManager.AchievementPopupNotify{
    private final Stage stage;
    private final Skin skin;
    private Table containerTable;
    private Texture backgroundTexture;

    public AchievementPopupOverlay(Skin gameSkin) {
        this.skin = gameSkin;
        this.stage = new Stage(new FitViewport(1280, 720));

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.08f, 0.08f, 0.10f, 0.95f));
        pixmap.fill();
        this.backgroundTexture = new Texture(pixmap);
        pixmap.dispose();

        AchievementManager.getInstance().setNotificationListener(this);
    }

    @Override
    public void triggerPopup(final String titleText, final String descText) {
        if (containerTable != null) {
            containerTable.remove();
        }

        containerTable = new Table();
        containerTable.center().pad(15);
        containerTable.setBackground(new TextureRegionDrawable(backgroundTexture));
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get("Hollowfont", com.badlogic.gdx.graphics.g2d.BitmapFont.class), Color.GOLDENROD);
        Label.LabelStyle contentStyle = new Label.LabelStyle(skin.get("Hollowfont", com.badlogic.gdx.graphics.g2d.BitmapFont.class), Color.WHITE);

        Label banner = new Label("✨ ACHIEVEMENT UNLOCKED ✨", titleStyle);
        banner.setFontScale(0.7f);
        Label title = new Label(titleText, contentStyle);
        title.setFontScale(0.9f);
        Label desc = new Label(descText, contentStyle);
        desc.setFontScale(0.65f);
        desc.setColor(Color.LIGHT_GRAY);

        containerTable.add(banner).padBottom(6f).row();
        containerTable.add(title).padBottom(4f).row();
        containerTable.add(desc);

        // Position dimensions
        float width = 480f;
        float height = 110f;
        containerTable.setSize(width, height);

        // Start position below the viewport cutoff bounds
        containerTable.setPosition((1280f - width) / 2f, -height - 20f);
        stage.addActor(containerTable);

        // Slide up, hold, slide down, and automatically purge actor
        containerTable.addAction(Actions.sequence(
            Actions.moveTo((1280f - width) / 2f, 30f, 0.5f, com.badlogic.gdx.math.Interpolation.pow3Out),
            Actions.delay(3.5f),
            Actions.parallel(
                Actions.moveBy(0, -160f, 0.5f, com.badlogic.gdx.math.Interpolation.pow3In),
                Actions.fadeOut(0.5f)
            ),
            Actions.removeActor()
        ));
    }

    public void updateAndRender(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
