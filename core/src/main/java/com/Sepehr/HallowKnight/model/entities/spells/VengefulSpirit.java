package com.Sepehr.HallowKnight.model.entities.spells;

import com.Sepehr.HallowKnight.model.entities.enemies.Enemy;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;

public class VengefulSpirit extends Spell{
    private final float speed = 650f;
    private final float directionX;
    private float stateTime = 0f;

    private Animation<TextureRegion> animFly;
    private Array<Enemy> hitEnemies = new Array<>();

    public VengefulSpirit(float x, float y, boolean facingRight, TextureAtlas atlas, MiniAudio miniAudio) {
        super(x, y, 50f, 40f);
        this.directionX = facingRight ? 1f : -1f;
        this.animFly = new Animation<>(0.06f, atlas.findRegions("ShadowBall"), Animation.PlayMode.NORMAL);
        MASound soundCast = miniAudio.createSound("audio/hero_fireball.wav");
        if (soundCast != null) {
            soundCast.setVolume(0.5f);
            soundCast.play();
        }
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
        if (animFly.isAnimationFinished(stateTime)) {
            destroy();
            return;
        }
        x += speed * directionX * delta;
        hitbox.setPosition(x, y);
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = animFly.getKeyFrame(stateTime, false);
        if (currentFrame == null) return;

        boolean flipX = directionX < 0;
        batch.draw(
            currentFrame.getTexture(),
            x, y,
            hitbox.width / 2f, hitbox.height / 2f,
            hitbox.width, hitbox.height,
            1f, 1f,
            0f,
            currentFrame.getRegionX(), currentFrame.getRegionY(),
            currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
            flipX, false
        );
    }

    @Override
    public void handleEnemyCollision(Enemy enemy) {
        if (!hitEnemies.contains(enemy, true)) {
            boolean hitFromLeft = directionX > 0;
            enemy.takeDamage(2, hitFromLeft);
            hitEnemies.add(enemy);
        }
    }

    @Override
    public boolean shouldDestroyOnWalls() {
        return true;
    }
}
