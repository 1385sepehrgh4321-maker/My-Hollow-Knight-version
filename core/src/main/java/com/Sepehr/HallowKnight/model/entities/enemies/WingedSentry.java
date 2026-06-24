package com.Sepehr.HallowKnight.model.entities.enemies;

import com.Sepehr.HallowKnight.model.entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class WingedSentry extends Enemy{
    private final float HOVER_SPEED = 50f;
    private final float DASH_SPEED = 350f;
    private final float DETECT_RADIUS = 500f;
    private final Vector2 spawnPosition = new Vector2();

    private final float leftBound;
    private final float rightBound;
    private final TextureAtlas atlas;

    public enum State {
        IDLE,
        TURN,
        ATTACK_ANTICIPATE,
        ATTACK_DASH,
        ATTACK_COOLDOWN,
        DEATH_AIR,
        DEATH_LAND
    }
    private State currentState = State.IDLE;
    private float stateTime = 0f;
    private float actionTimer = 0f;
    private boolean isFacingRight = false;

    private final Vector2 targetPosition = new Vector2();
    private final Vector2 dashDirection = new Vector2();

    private final Animation<TextureRegion> animIdle;
    private final Animation<TextureRegion> animTurn;
    private final Animation<TextureRegion> animAnticipate;
    private final Animation<TextureRegion> animDash;
    private final Animation<TextureRegion> animDeathAir;
    private final Animation<TextureRegion> animDeathLand;

    public WingedSentry(float spawnX , float spawnY , float leftBound , float rightBound , String path) {
        super(spawnX, spawnY, 64, 50);
        spawnPosition.x = (leftBound + rightBound)/2;
        spawnPosition.y = spawnY;
        this.atlas = new TextureAtlas(Gdx.files.internal(path));
        this.health = 2;
        this.leftBound = leftBound;
        this.rightBound = rightBound;

        animIdle       = new Animation<>(0.10f, atlas.findRegions("Idle"), Animation.PlayMode.LOOP);
        animTurn       = new Animation<>(0.08f, atlas.findRegions("Turn To Fly"), Animation.PlayMode.NORMAL);
        animAnticipate = new Animation<>(0.06f, atlas.findRegions("Charge Antic"), Animation.PlayMode.NORMAL);
        animDash       = new Animation<>(0.06f, atlas.findRegions("Charge"), Animation.PlayMode.LOOP);
        animDeathAir   = new Animation<>(0.07f, atlas.findRegions("Death Air"), Animation.PlayMode.NORMAL);
        animDeathLand  = new Animation<>(0.08f, atlas.findRegions("Death Land"), Animation.PlayMode.NORMAL);

        this.actionTimer = 2f + (float) Math.random() * 2f;
    }
    @Override
    public void update(float delta) {

    }

    public void updateAI(float delta , Player player) {
        stateTime += delta;

        if (health <= 0 && currentState != State.DEATH_AIR && currentState != State.DEATH_LAND) {
            changeState(State.DEATH_AIR);
            return;
        }

        float distanceToPlayer = position.dst(player.getPosition());
        boolean playerInFront = (isFacingRight && player.getPosition().x > position.x) ||
            (!isFacingRight && player.getPosition().x < position.x);
        boolean genericVerticalAggro = Math.abs(position.y - player.getPosition().y) < 120;

        switch (currentState) {
            case IDLE:
                float distanceToSpawn = position.dst(spawnPosition);

                if (distanceToSpawn > 5f) {
                    dashDirection.set(spawnPosition).sub(position).nor();
                    isFacingRight = dashDirection.x > 0;

                    velocity.set(dashDirection).scl(HOVER_SPEED * 1.5f);
                    position.add(velocity.x * delta, velocity.y * delta);
                    updateHitbox();
                } else {
                    if (velocity.len2() > 0) {
                        position.set(spawnPosition);
                        velocity.set(0, 0);
                        updateHitbox();
                        this.actionTimer = 1.5f + (float) Math.random() * 2.5f;
                    }

                    if (distanceToPlayer < DETECT_RADIUS && playerInFront && genericVerticalAggro) {
                        targetPosition.set(player.getPosition().x, this.position.y);
                        changeState(State.ATTACK_ANTICIPATE);
                        actionTimer = 0.5f;
                        break;
                    }

                    actionTimer -= delta;
                    if (actionTimer <= 0) {
                        changeState(State.TURN);
                    }
                }
                break;

            case TURN:
                velocity.set(0, 0);
                if (animTurn.isAnimationFinished(stateTime)) {
                    isFacingRight = !isFacingRight;
                    changeState(State.IDLE);
                }
                break;

            case ATTACK_ANTICIPATE:
                velocity.set(0, 0);
                actionTimer -= delta;
                if (actionTimer <= 0) {
                    dashDirection.set(targetPosition).sub(position).nor();
                    changeState(State.ATTACK_DASH);
                    float distanceToTarget = position.dst(targetPosition);
                    actionTimer = (distanceToTarget / DASH_SPEED) + 0.3f;
                }
                break;

            case ATTACK_DASH:
                velocity.set(dashDirection).scl(DASH_SPEED);
                position.add(velocity.x * delta, velocity.y * delta);

                actionTimer -= delta;
                if (actionTimer <= 0 || position.dst(targetPosition) < 15f) {
                    changeState(State.ATTACK_COOLDOWN);
                    actionTimer = 0.6f;
                }
                updateHitbox();
                break;

            case ATTACK_COOLDOWN:
                velocity.set(0, 0);
                actionTimer -= delta;
                if (actionTimer <= 0) {
                    changeState(State.IDLE);
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

    private void changeState(State newState) {
        this.currentState = newState;
        this.stateTime = 0f;
        this.velocity.set(0, 0);

        if (newState == State.IDLE) {
            this.actionTimer = 1.5f + (float) Math.random() * 2.5f;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = switch (currentState) {
            case IDLE -> animIdle.getKeyFrame(stateTime, true);
            case TURN -> animTurn.getKeyFrame(stateTime, false);
            case ATTACK_ANTICIPATE -> animAnticipate.getKeyFrame(stateTime, false);
            case ATTACK_DASH -> animDash.getKeyFrame(stateTime, true);
            case ATTACK_COOLDOWN -> animIdle.getKeyFrame(stateTime, true);
            case DEATH_AIR -> animDeathAir.getKeyFrame(stateTime, false);
            case DEATH_LAND -> animDeathLand.getKeyFrame(stateTime, false);
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
