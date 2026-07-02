package com.Sepehr.HallowKnight.model.entities.enemies;

import com.Sepehr.HallowKnight.model.entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Crystallized extends Enemy{
    private final float RUN_SPEED = 55f;
    private final float EVADE_SPEED = 140f;
    private final float DETECT_RADIUS = 220f;
    private final float TOO_CLOSE_RADIUS = 60f;

    private final float leftBound;
    private final float rightBound;
    private final TextureAtlas atlas;

    public enum State {
        IDLE,
        RUN,
        TURN,
        SHOOT,
        EVADE,
        DEATH_AIR,
        DEATH_LAND
    }

    private State currentState = State.RUN;
    private float stateTime = 0f;
    private float actionTimer = 0f;
    private boolean isFacingRight = false;
    private boolean hasFiredProjectile = false;

    private final Animation<TextureRegion> animIdle;
    private final Animation<TextureRegion> animRun;
    private final Animation<TextureRegion> animTurn;
    private final Animation<TextureRegion> animShoot;
    private final Animation<TextureRegion> animEvade;
    private final Animation<TextureRegion> animDeathAir;
    private final Animation<TextureRegion> animDeathLand;

    public Crystallized(float spawnX, float spawnY, float leftBound, float rightBound, String path) {
        super(spawnX, spawnY, 56, 40);
        this.atlas = new TextureAtlas(Gdx.files.internal(path));
        this.health = 3;
        this.leftBound = leftBound;
        this.rightBound = rightBound;

        animIdle      = new Animation<>(0.12f, atlas.findRegions("Idle"), Animation.PlayMode.LOOP);
        animRun       = new Animation<>(0.09f, atlas.findRegions("Run"), Animation.PlayMode.LOOP);
        animTurn      = new Animation<>(0.08f, atlas.findRegions("Turn"), Animation.PlayMode.NORMAL);
        animShoot     = new Animation<>(0.07f, atlas.findRegions("Shoot"), Animation.PlayMode.NORMAL);
        animEvade     = new Animation<>(0.06f, atlas.findRegions("Evade"), Animation.PlayMode.NORMAL);
        animDeathAir  = new Animation<>(0.07f, atlas.findRegions("Death Air"), Animation.PlayMode.NORMAL);
        animDeathLand = new Animation<>(0.08f, atlas.findRegions("Death Land"), Animation.PlayMode.NORMAL);

        this.actionTimer = 2f + (float) Math.random() * 3f; // Initial walk time selection
    }
    @Override
    public void update(float delta) {

    }

    public void updateAI(float delta, Player player) {
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
        boolean sameVerticalLevel = Math.abs(position.y - player.getPosition().y) < 60;

        switch (currentState) {
            case IDLE:
                velocity.set(0, 0);
                actionTimer -= delta;

                if (distanceToPlayer < DETECT_RADIUS && playerInFront && sameVerticalLevel) {
                    if (distanceToPlayer < TOO_CLOSE_RADIUS) {
                        changeState(State.EVADE);
                    } else {
                        changeState(State.SHOOT);
                    }
                    break;
                }

                if (actionTimer <= 0) {
                    changeState(State.RUN);
                }
                break;

            case RUN:
                velocity.x = isFacingRight ? RUN_SPEED : -RUN_SPEED;
                position.x += velocity.x * delta;

                if (distanceToPlayer < DETECT_RADIUS && playerInFront && sameVerticalLevel) {
                    if (distanceToPlayer < TOO_CLOSE_RADIUS) {
                        changeState(State.EVADE);
                    } else {
                        changeState(State.SHOOT);
                    }
                    break;
                }

                actionTimer -= delta;
                if (actionTimer <= 0) {
                    changeState(State.IDLE);
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
                }
                break;

            case SHOOT:
                velocity.set(0, 0);
                actionTimer -= delta;
                if (actionTimer <= 0) {
                    changeState(State.IDLE);
                }
                break;

            case EVADE:
                velocity.x = isFacingRight ? -EVADE_SPEED : EVADE_SPEED;
                position.x += velocity.x * delta;


                if (position.x <= leftBound) {
                    position.x = leftBound;
                    changeState(State.TURN);
                    break;
                } else if (position.x >= rightBound - hitbox.width) {
                    position.x = rightBound - hitbox.width;
                    changeState(State.TURN);
                    break;
                }

                updateHitbox();

                if (animEvade.isAnimationFinished(stateTime)) {
                    changeState(State.SHOOT);
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

    @Override
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

        if (newState == State.IDLE) {
            this.actionTimer = 1.0f + (float) Math.random() * 1.5f; // Rest duration
        } else if (newState == State.RUN) {
            this.actionTimer = 3.0f + (float) Math.random() * 3.0f; // Run duration
        } else if (newState == State.SHOOT) {
            this.hasFiredProjectile = false;
            this.actionTimer = 2.0f; // NEW: The laser beam sequence lasts exactly 2.0 seconds!
        }
    }

    private void spawnCrystalShardProjectile(Player player) {
        // Hook this method up to your GameWorld's object initialization list array!
        // System.out.println("Crystal Shard Fired out towards: " + player.getPosition().x);
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = switch (currentState) {
            case IDLE -> animIdle.getKeyFrame(stateTime, true);
            case RUN -> animRun.getKeyFrame(stateTime, true);
            case TURN -> animTurn.getKeyFrame(stateTime, false);
            case SHOOT -> animShoot.getKeyFrame(stateTime, false);
            case EVADE -> animEvade.getKeyFrame(stateTime, false);
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

    public boolean isLaserActive() {
        return currentState == State.SHOOT && animShoot.getKeyFrameIndex(stateTime) >= 3;
    }

    public boolean isFacingRight() {
        return this.isFacingRight;
    }
}
