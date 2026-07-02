package com.Sepehr.HallowKnight.model.entities.enemies;

import com.Sepehr.HallowKnight.model.entities.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Mosscreep extends Enemy{
    private final float WALK_SPEED = 50f;
    private final float DETECT_RADIUS = 130f;
    private final TextureAtlas atlas;

    private final float leftBound;
    private final float rightBound;

    public enum State { PATROLLING, TURNING, DEATH_AIR, DEATH_LAND }
    private State currentState = State.PATROLLING;
    private float stateTime = 0f;

    private final Animation<TextureRegion> animWalk;
    private final Animation<TextureRegion> animTurn;
    private final Animation<TextureRegion> animDeathLand;
    private final Animation<TextureRegion> animDeathAir;

    private boolean isFacingRight = false;
    private final int maxHealth = 2;

    public Mosscreep(String mobName , float spawnX, float spawnY, float leftBound, float rightBound , String path) {
        System.out.println("x:" + spawnX + "y:" + spawnY);
        super(mobName , spawnX , spawnY, 32, 24);
        atlas = new  TextureAtlas(Gdx.files.internal(path));
        this.health = maxHealth;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        animWalk      = new Animation<>(0.10f, atlas.findRegions("Walk"), Animation.PlayMode.LOOP);
        animTurn      = new Animation<>(0.08f, atlas.findRegions("Turn"), Animation.PlayMode.NORMAL);
        animDeathAir  = new Animation<>(0.07f, atlas.findRegions("Death Air"), Animation.PlayMode.NORMAL);
        animDeathLand = new Animation<>(0.08f, atlas.findRegions("Death Land"), Animation.PlayMode.NORMAL);
    }

    @Override
    public void update(float delta) {
        if (enemyKnockbackTimer > 0) {
            enemyKnockbackTimer -= delta;
            position.x += enemyKnockbackVelocityX * delta;
            enemyKnockbackVelocityX *= 0.85f;
            if (position.x <= leftBound) {
                position.x = leftBound;
            } else if (position.x >= rightBound - hitbox.width) {
                position.x = rightBound - hitbox.width;
            }
            updateHitbox();
            stateTime += delta;
            return;
        }
        stateTime += delta;

        if (health <= 0 && currentState != State.DEATH_AIR && currentState != State.DEATH_LAND) {
            currentState = State.DEATH_AIR;
            die();
            stateTime = 0f;
            velocity.set(0, 0);
            return;
        }

        switch (currentState) {
            case PATROLLING:
                velocity.x = isFacingRight ? WALK_SPEED : -WALK_SPEED;
                position.x += velocity.x * delta;
                if (position.x <= leftBound) {
                    position.x = leftBound;
                    startTurning(true);
                } else if (position.x >= rightBound - hitbox.width) {
                    position.x = rightBound - hitbox.width;
                    startTurning(false);
                }

                updateHitbox();
                break;

            case TURNING:
                velocity.set(0, 0);
                if (animTurn.isAnimationFinished(stateTime)) {
                    currentState = State.PATROLLING;
                    stateTime = 0f;
                }
                break;

            case DEATH_AIR:
                velocity.set(0, 0);
                if (animDeathAir.isAnimationFinished(stateTime)) {
                    currentState = State.DEATH_LAND;
                    stateTime = 0f;
                }
                break;

            case DEATH_LAND:
                velocity.set(0, 0);
                break;
        }
    }

    private void startTurning(boolean faceRightNext) {
        currentState = State.TURNING;
        stateTime = 0f;
        this.isFacingRight = faceRightNext;
    }

    public void onPlayerHit() {
        this.velocity.set(0, 0);
        this.enemyKnockbackTimer = 0f;
        this.enemyKnockbackVelocityX = 0f;
        this.currentState = State.PATROLLING;
        this.stateTime = 0f;
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame;

        switch (currentState) {
            case DEATH_AIR:
                currentFrame = animDeathAir.getKeyFrame(stateTime, false);
                break;
            case DEATH_LAND:
                currentFrame = animDeathLand.getKeyFrame(stateTime, false);
                break;
            case TURNING:
                currentFrame = animTurn.getKeyFrame(stateTime, false);
                break;
            case PATROLLING:
            default:
                currentFrame = animWalk.getKeyFrame(stateTime, true);
                break;
        }
        if (currentFrame == null) return;

        boolean flipX = isFacingRight;
        float spriteScale = 0.35f;
        float drawnWidth = currentFrame.getRegionWidth() * spriteScale;
        float drawnHeight = currentFrame.getRegionHeight() * spriteScale;

        float drawX = position.x + (hitbox.width / 2f) - (drawnWidth / 2f);
        float drawY = position.y;

        batch.draw(
            currentFrame.getTexture(),
            drawX, drawY,
            drawnWidth / 2f, drawnHeight / 2f,
            drawnWidth, drawnHeight,
            1f, 1f,
            0f,
            currentFrame.getRegionX(), currentFrame.getRegionY(),
            currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
            flipX, false
        );
    }

    public boolean isDeadFinished() {
        return currentState == State.DEATH_LAND && animDeathLand.isAnimationFinished(stateTime);
    }

    public State getCurrentState() { return currentState; }

    @Override
    public void dispose() {
        atlas.dispose();
    }
}
