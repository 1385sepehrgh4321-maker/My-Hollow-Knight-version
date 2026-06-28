package com.Sepehr.HallowKnight.model.entities.spells;

import com.Sepehr.HallowKnight.model.entities.enemies.Enemy;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;

public class HowlingWraiths extends Spell{
    private float elapsedLifetime = 0f;
    private int currentDamageTick = 0;
    private final Array<Enemy> hitEnemiesThisTick = new Array<>();
    private final Animation<TextureRegion> anim;

    public HowlingWraiths(float playerX, float playerY, float playerWidth, TextureAtlas atlas, MiniAudio miniAudio) {
        super(playerX - 28f, playerY + 40f, 80f, 90f);
        this.anim = new Animation<>(0.05f, atlas.findRegions("ShadowScream"), Animation.PlayMode.NORMAL);
        MASound soundCast = miniAudio.createSound("audio/col_spikes_snap_up.wav");
        if (soundCast != null) {
            soundCast.setVolume(0.6f);
            soundCast.play();
        }
    }

    @Override
    public void update(float delta) {
        elapsedLifetime += delta;
        if (anim.isAnimationFinished(elapsedLifetime)) {
            destroy();
            return;
        }
        float totalDuration = anim.getAnimationDuration();
        int calculatedTick = (int) (elapsedLifetime / (totalDuration / 3f));
        calculatedTick = Math.min(calculatedTick, 2);

        if (calculatedTick != currentDamageTick) {
            currentDamageTick = calculatedTick;
            hitEnemiesThisTick.clear();
        }
    }

    @Override
    public void handleEnemyCollision(Enemy enemy) {
        if (!hitEnemiesThisTick.contains(enemy, true)) {
            boolean hitFromLeft = this.x + (hitbox.width / 2f) < enemy.getPosition().x;
            enemy.takeDamage(2, hitFromLeft);
            hitEnemiesThisTick.add(enemy);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = anim.getKeyFrame(elapsedLifetime, false);
        if (currentFrame == null) return;
        batch.draw(currentFrame, x, y, hitbox.width, hitbox.height);
    }

    @Override
    public boolean shouldDestroyOnWalls() {
        return false;
    }
}
