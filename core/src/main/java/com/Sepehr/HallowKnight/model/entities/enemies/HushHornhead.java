package com.Sepehr.HallowKnight.model.entities.enemies;

import com.Sepehr.HallowKnight.model.entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class HushHornhead extends Enemy{
    private final float WALK_SPEED = 40f;
    private final float LUNGE_SPEED = 300f;
    private final float DETECT_RADIUS = 400f;

    private final float leftBound;
    private final float rightBound;
    private final TextureAtlas atlas;

    public enum State {
        IDLE,
        WALK,
        TURN,
        ATTACK_ANTICIPATE,
        ATTACK_LUNGE,
        ATTACK_COOLDOWN,
        DEATH_AIR,
        DEATH_LAND
    }
    private State currentState = State.WALK;
    private float stateTime = 0f;
    private float actionTimer = 0f;

    //animation
    private final Animation<TextureRegion> animIdle;
    private final Animation<TextureRegion> animWalk;
    private final Animation<TextureRegion> animTurn;
    private final Animation<TextureRegion> animAnticipate;
    private final Animation<TextureRegion> animLunge;
    private final Animation<TextureRegion> animCooldown;
    private final Animation<TextureRegion> animDeathAir;
    private final Animation<TextureRegion> animDeathLand;

    private boolean isFacingRight = false;

    public HushHornhead(float spawnX, float spawnY, float leftBound, float rightBound , String path) {
        super(spawnX, spawnY, 64, 40);
        this.atlas = new TextureAtlas(Gdx.files.internal(path));
        this.health = 3;
        this.leftBound = leftBound;
        this.rightBound = rightBound;

        animIdle       = new Animation<>(0.10f, atlas.findRegions("Idle"), Animation.PlayMode.LOOP);
        animWalk       = new Animation<>(0.10f, atlas.findRegions("Walk"), Animation.PlayMode.LOOP);
        animTurn       = new Animation<>(0.08f, atlas.findRegions("Turn"), Animation.PlayMode.NORMAL);
        animAnticipate = new Animation<>(0.05f, atlas.findRegions("Attack Anticipate"), Animation.PlayMode.NORMAL);
        animLunge      = new Animation<>(0.06f, atlas.findRegions("Attack Lunge"), Animation.PlayMode.LOOP);
        animCooldown   = new Animation<>(0.08f, atlas.findRegions("Attack Cooldown"), Animation.PlayMode.NORMAL);
        animDeathAir   = new Animation<>(0.07f, atlas.findRegions("Death Air"), Animation.PlayMode.NORMAL);
        animDeathLand  = new Animation<>(0.08f, atlas.findRegions("Death Land"), Animation.PlayMode.NORMAL);
    }
    @Override
    public void update(float delta) {

    }

    public void updateAI(float delta , Player player) {
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
            changeState(State.DEATH_AIR);
            return;
        }
        float distanceToPlayer = position.dst(player.getPosition());
        boolean playerInFront = (isFacingRight && player.getPosition().x > position.x) ||
            (!isFacingRight && player.getPosition().x < position.x);
        boolean sameVerticalLevel = Math.abs(position.y - player.getPosition().y) < 50;

        switch (currentState) {
            case IDLE:
                velocity.set(0, 0);
                actionTimer -= delta;

                if (distanceToPlayer < DETECT_RADIUS && playerInFront && sameVerticalLevel) {
                    changeState(State.ATTACK_ANTICIPATE);
                    actionTimer = 0.5f;
                    break;
                }

                if (actionTimer <= 0) {
                    changeState(State.WALK);
                }
                break;

            case WALK:
                velocity.x = isFacingRight ? WALK_SPEED : -WALK_SPEED;
                position.x += velocity.x * delta;

                if (distanceToPlayer < DETECT_RADIUS && playerInFront && sameVerticalLevel) {
                    changeState(State.ATTACK_ANTICIPATE);
                    actionTimer = 0.5f;
                    break;
                }

                actionTimer -= delta;
                if (actionTimer <= 0) {
                    changeState(State.IDLE);
                    actionTimer = 1.5f;
                    break;
                }

                if (position.x <= leftBound) {
                    position.x = leftBound;
                    changeState(State.TURN);
                } else if (position.x >= rightBound - hitbox.width) {
                    position.x = rightBound - hitbox.width;
                    changeState(State.TURN);
                }
                updateHitbox();
                break;

            case TURN:
                velocity.set(0, 0);
                if (animTurn.isAnimationFinished(stateTime)) {
                    isFacingRight = !isFacingRight;
                    changeState(State.IDLE);
                    actionTimer = 0.3f;
                }
                break;

            case ATTACK_ANTICIPATE:
                velocity.set(0, 0);
                actionTimer -= delta;
                if (actionTimer <= 0) {
                    changeState(State.ATTACK_LUNGE);
                }
                break;

            case ATTACK_LUNGE:
                velocity.x = isFacingRight ? LUNGE_SPEED : -LUNGE_SPEED;
                position.x += velocity.x * delta;

                if (position.x <= leftBound) {
                    position.x = leftBound;
                    isFacingRight = true;
                    changeState(State.ATTACK_COOLDOWN);
                    actionTimer = 0.4f;
                } else if (position.x >= rightBound - hitbox.width) {
                    position.x = rightBound - hitbox.width;
                    isFacingRight = false;
                    changeState(State.ATTACK_COOLDOWN);
                    actionTimer = 0.4f;
                }
                updateHitbox();
                break;

            case ATTACK_COOLDOWN:
                velocity.set(0, 0);
                actionTimer -= delta;
                if (actionTimer <= 0) {
                    changeState(State.IDLE);
                    actionTimer = 0.6f;
                }
                break;

            case DEATH_AIR:
                velocity.set(0, 0);
                if (animDeathAir.isAnimationFinished(stateTime)) {
                    changeState(State.DEATH_LAND);
                }
                break;

            case DEATH_LAND:
                velocity.set(0, 0);
                break;
        }
    }

    public void onPlayerHit() {
        this.velocity.set(0, 0);
        this.enemyKnockbackTimer = 0f;
        this.enemyKnockbackVelocityX = 0f;
        this.currentState = State.IDLE;
        this.stateTime = 0f;
    }

    private void changeState(State newState) {
        this.currentState = newState;
        this.stateTime = 0f;
        this.velocity.set(0, 0);

        if (newState == State.WALK) {
            this.actionTimer = 3f + (float) Math.random() * 3f;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = switch (currentState) {
            case IDLE -> animIdle.getKeyFrame(stateTime, true);
            case WALK -> animWalk.getKeyFrame(stateTime, true);
            case TURN -> animTurn.getKeyFrame(stateTime, false);
            case ATTACK_ANTICIPATE -> animAnticipate.getKeyFrame(stateTime, false);
            case ATTACK_LUNGE -> animLunge.getKeyFrame(stateTime, true);
            case ATTACK_COOLDOWN -> animCooldown.getKeyFrame(stateTime, false);
            case DEATH_AIR -> animDeathAir.getKeyFrame(stateTime, false);
            case DEATH_LAND -> animDeathLand.getKeyFrame(stateTime, false);
            default -> animIdle.getKeyFrame(stateTime, true);
        };

        if (currentFrame == null) return;

        boolean flipX = isFacingRight;
        float spriteScale = 0.35f;
        float drawnWidth = currentFrame.getRegionWidth() * spriteScale;
        float drawnHeight = currentFrame.getRegionHeight() * spriteScale;
        float drawX = position.x + (hitbox.width / 2f) - (drawnWidth / 2f);

        batch.draw(
            currentFrame.getTexture(),
            drawX, position.y,
            drawnWidth / 2f, drawnHeight / 2f,
            drawnWidth, drawnHeight,
            1f, 1f,
            0f,
            currentFrame.getRegionX(), currentFrame.getRegionY(),
            currentFrame.getRegionWidth(), currentFrame.getRegionHeight(),
            flipX, false
        );
    }

    @Override
    public boolean isDeadFinished() {
        return currentState == State.DEATH_LAND && animDeathLand.isAnimationFinished(stateTime);
    }

    @Override
    public void dispose() {
        if (atlas != null) {
            atlas.dispose();
        }
    }
}
