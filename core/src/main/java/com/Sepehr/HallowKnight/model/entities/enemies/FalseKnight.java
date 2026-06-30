package com.Sepehr.HallowKnight.model.entities.enemies;

import com.Sepehr.HallowKnight.model.entities.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class FalseKnight extends Enemy{
    public enum State {
        IDLE,
        TURN,
        CHARGE_RUN_ANTIC,
        CHARGE_RUN,
        MACE_SLAM_ANTIC,
        MACE_SLAM,
        MACE_SLAM_RECOVER,
        LEAP_START,
        LEAP_AIR,
        LEAP_LAND,
        STUNNED,            // Move 6: Plays 'Body' (exposed maggot state)
        STUN_RECOVER,
        POWERED_SLAM_ANTIC, // Phase 2 Moves
        POWERED_SLAM,
        POWERED_RECOVER,
        DEATH_HIT,
        DEATH_FALL,
        DEATH_LAND
    }

    private State currentState = State.IDLE;
    private State lastExecutedMoveState = State.IDLE;

    private final float groundY;
    private float stateTime = 0f;
    private float actionTimer = 0f;
    private float decisionCooldown = 3f;
    private float decisionTimer = 0f;

    private final int maxHealth;
    private int phase = 1;
    private boolean completedStunSequence = false;
    private float currentSpeedMultiplier = 1.0f;

    private float damageWindowTimer = 0f;
    private int recentDamageTaken = 0;

    private final float leftBound;
    private final float rightBound;
    private final TextureAtlas atlas;
    private boolean isFacingRight = false;

    private final float RUN_SPEED = 140f;
    private final float JUMP_VELOCITY_Y = 760f;
    private final float GRAVITY = -1200f;
    private boolean isPoweredLeap = false;

    public boolean triggerCameraShakeRequest = false;
    public float cameraShakeIntensity = 0f;
    public float cameraShakeDuration = 0f;

    private final Animation<TextureRegion> animIdle;
    private final Animation<TextureRegion> animRunAntic;
    private final Animation<TextureRegion> animRun;
    private final Animation<TextureRegion> animTurn;
    private final Animation<TextureRegion> animAttackAntic;
    private final Animation<TextureRegion> animAttack;
    private final Animation<TextureRegion> animAttackRecover;
    private final Animation<TextureRegion> animJump;
    private final Animation<TextureRegion> animJumpAttack;
    private final Animation<TextureRegion> animLand;
    private final Animation<TextureRegion> animBody;
    private final Animation<TextureRegion> animStunRecover;
    private final Animation<TextureRegion> animDeathHit;
    private final Animation<TextureRegion> animDeathFall;
    private final Animation<TextureRegion> animDeathLand;

    public FalseKnight(float spawnX, float spawnY, float leftBound, float rightBound, String atlasPath, int maxHp) {
        super(spawnX, spawnY, 140, 190);
        this.groundY = spawnY;
        this.atlas = new TextureAtlas(Gdx.files.internal(atlasPath));
        this.maxHealth = maxHp;
        this.health = maxHp;
        this.leftBound = leftBound;
        this.rightBound = rightBound;

        float defaultFrameRate = 0.10f;
        animIdle          = new Animation<>(defaultFrameRate, atlas.findRegions("Idle"), Animation.PlayMode.LOOP);
        animRunAntic      = new Animation<>(defaultFrameRate, atlas.findRegions("Run Antic"), Animation.PlayMode.NORMAL);
        animRun           = new Animation<>(defaultFrameRate, atlas.findRegions("Run"), Animation.PlayMode.LOOP);
        animTurn          = new Animation<>(0.08f, atlas.findRegions("Turn"), Animation.PlayMode.NORMAL);
        animAttackAntic   = new Animation<>(defaultFrameRate, atlas.findRegions("Attack Antic"), Animation.PlayMode.NORMAL);
        animAttack        = new Animation<>(0.08f, atlas.findRegions("Attack"), Animation.PlayMode.NORMAL);
        animAttackRecover = new Animation<>(defaultFrameRate, atlas.findRegions("Attack Recover"), Animation.PlayMode.NORMAL);
        animJump          = new Animation<>(defaultFrameRate, atlas.findRegions("Jump"), Animation.PlayMode.NORMAL);
        animJumpAttack    = new Animation<>(defaultFrameRate, atlas.findRegions("Jump Attack"), Animation.PlayMode.LOOP);
        animLand          = new Animation<>(defaultFrameRate, atlas.findRegions("Land"), Animation.PlayMode.NORMAL);
        animBody          = new Animation<>(defaultFrameRate, atlas.findRegions("Body"), Animation.PlayMode.LOOP);
        animStunRecover   = new Animation<>(defaultFrameRate, atlas.findRegions("Stun Recover"), Animation.PlayMode.NORMAL);
        animDeathHit      = new Animation<>(defaultFrameRate, atlas.findRegions("DeathHit"), Animation.PlayMode.NORMAL);
        animDeathFall     = new Animation<>(defaultFrameRate, atlas.findRegions("DeathFall"), Animation.PlayMode.LOOP);
        animDeathLand     = new Animation<>(defaultFrameRate, atlas.findRegions("DeathLand"), Animation.PlayMode.NORMAL);
    }

    @Override
    public void update(float delta) {
    }

    public void updateAI(float delta, Player player) {
        if (enemyKnockbackTimer > 0 && !isStunnedOrDying()) {
            enemyKnockbackTimer -= delta;
            position.x += enemyKnockbackVelocityX * delta;
            enemyKnockbackVelocityX *= 0.85f;
            clampPositionToBounds();
            updateHitbox();
            stateTime += delta;
            return;
        }

        float acceleratedDelta = delta * currentSpeedMultiplier;
        stateTime += acceleratedDelta;
        decisionTimer += delta;

        if (damageWindowTimer > 0) {
            damageWindowTimer -= delta;
            if (damageWindowTimer <= 0) recentDamageTaken = 0;
        }

        if (player != null && canTrackPlayerDirection()) {
            isFacingRight = player.getPosition().x > position.x;
        }

        if (health <= maxHealth * 0.5f && !completedStunSequence && !isStunnedOrDying()) {
            changeState(State.STUNNED);
            actionTimer = 4.0f; // Stun Duration window
            return;
        }

        if (health <= 0 && !isDyingState()) {
            changeState(State.DEATH_HIT);
            return;
        }

        switch (currentState) {
            case IDLE:
                velocity.set(0, 0);
                if (decisionTimer >= decisionCooldown && player != null) {
                    evaluateNextMove(player);
                }
                break;

            case TURN:
                velocity.set(0, 0);
                if (animTurn.isAnimationFinished(stateTime)) {
                    isFacingRight = !isFacingRight;
                    changeState(State.IDLE);
                }
                break;

            case CHARGE_RUN_ANTIC:
                velocity.set(0, 0);
                if (animRunAntic.isAnimationFinished(stateTime)) {
                    changeState(State.CHARGE_RUN);
                    actionTimer = 1.5f;
                }
                break;

            case CHARGE_RUN:
                velocity.x = isFacingRight ? RUN_SPEED * 1.6f : -RUN_SPEED * 1.6f;
                position.x += velocity.x * delta;
                actionTimer -= delta;

                if (actionTimer <= 0 || hitWallBoundary()) {
                    changeState(State.IDLE);
                }
                clampPositionToBounds();
                updateHitbox();
                break;

            case MACE_SLAM_ANTIC:
                velocity.set(0, 0);
                if (animAttackAntic.isAnimationFinished(stateTime)) {
                    changeState(State.MACE_SLAM);
                }
                break;

            case MACE_SLAM:
                velocity.set(0, 0);
                if (animAttack.isAnimationFinished(stateTime)) {
                    requestCameraShake(6f, 0.22f);
                    changeState(State.MACE_SLAM_RECOVER);
                }
                break;

            case MACE_SLAM_RECOVER:
                velocity.set(0, 0);
                if (animAttackRecover.isAnimationFinished(stateTime)) {
                    changeState(State.IDLE);
                }
                break;

            case POWERED_SLAM_ANTIC:
                velocity.set(0, 0);
                if (animAttackAntic.isAnimationFinished(stateTime)) {
                    isPoweredLeap = true;
                    changeState(State.LEAP_START);

                    float forwardDir = isFacingRight ? 1f : -1f;
                    velocity.set(forwardDir * RUN_SPEED * 0.8f, JUMP_VELOCITY_Y * 1.1f);
                }
                break;

            case POWERED_SLAM:
                velocity.set(0, 0);
                if (animAttack.isAnimationFinished(stateTime)) {
                    requestCameraShake(12f, 0.45f);
                    // todo: Instantiate your ground shockwave entity pools here
                    changeState(State.POWERED_RECOVER);
                }
                break;

            case POWERED_RECOVER:
                velocity.set(0, 0);
                if (animAttackRecover.isAnimationFinished(stateTime)) {
                    changeState(State.IDLE);
                }
                break;

            case LEAP_START:
                position.x += velocity.x * delta;
                velocity.y += GRAVITY * delta;
                position.y += velocity.y * delta;

                if (velocity.y <= 0) {
                    changeState(State.LEAP_AIR);
                }
                if (position.y <= groundY) {
                    position.y = groundY;
                    velocity.set(0, 0);
                    if (isPoweredLeap) {
                        isPoweredLeap = false;
                        changeState(State.POWERED_SLAM);
                    } else {
                        requestCameraShake(8f, 0.25f);
                        changeState(State.LEAP_LAND);
                    }
                }
                clampPositionToBounds();
                updateHitbox();
                break;

            case LEAP_AIR:
                position.x += velocity.x * delta;
                velocity.y += GRAVITY * delta;
                position.y += velocity.y * delta;

                if (position.y <= groundY) {
                    position.y = groundY;
                    velocity.set(0, 0);
                    if (isPoweredLeap) {
                        isPoweredLeap = false;
                        changeState(State.POWERED_SLAM);
                    } else {
                        requestCameraShake(8f, 0.25f);
                        changeState(State.LEAP_LAND);
                    }
                }
                clampPositionToBounds();
                updateHitbox();
                break;

            case LEAP_LAND:
                velocity.set(0, 0);
                if (animLand.isAnimationFinished(stateTime)) {
                    changeState(State.IDLE);
                }
                break;

            case STUNNED:
                velocity.set(0, 0);
                actionTimer -= delta;
                if (actionTimer <= 0) {
                    changeState(State.STUN_RECOVER);
                }
                break;

            case STUN_RECOVER:
                velocity.set(0, 0);
                if (animStunRecover.isAnimationFinished(stateTime)) {
                    exitStunnedPhaseTransition();
                }
                break;

            case DEATH_HIT:
                velocity.set(0, 0);
                if (animDeathHit.isAnimationFinished(stateTime)) {
                    changeState(State.DEATH_FALL);
                    velocity.y = -200f;
                }
                break;

            case DEATH_FALL:
                position.y += velocity.y * delta;
                if (position.y <= groundY) {
                    position.y = groundY;
                    velocity.set(0, 0);
                    requestCameraShake(15f, 0.6f);
                    changeState(State.DEATH_LAND);
                }
                updateHitbox();
                break;

            case DEATH_LAND:
                velocity.set(0, 0);
                break;
        }
    }

    private void evaluateNextMove(Player player) {
        decisionTimer = 0f;
        float distance = position.dst(player.getPosition());

        float[] weights = new float[4];

        if (distance < 220f) {
            weights[0] = 0.65f;
            weights[1] = 0.10f;
            weights[2] = 0.10f;
            weights[3] = (phase == 2) ? 0.15f : 0.0f;
        } else {
            weights[0] = 0.05f;
            weights[1] = 0.45f;
            weights[2] = 0.35f;
            weights[3] = (phase == 2) ? 0.15f : 0.0f;
        }

        if (lastExecutedMoveState == State.MACE_SLAM_ANTIC) weights[0] = 0f;
        if (lastExecutedMoveState == State.CHARGE_RUN_ANTIC) weights[1] = 0f;
        if (lastExecutedMoveState == State.LEAP_START)       weights[2] = 0f;
        if (lastExecutedMoveState == State.POWERED_SLAM_ANTIC) weights[3] = 0f;

        float totalWeight = weights[0] + weights[1] + weights[2] + weights[3];
        if (totalWeight <= 0) {
            changeState(State.CHARGE_RUN_ANTIC);
            return;
        }

        float randomRoll = MathUtils.random(0f, totalWeight);
        float cumulativeSum = 0f;

        for (int i = 0; i < weights.length; i++) {
            cumulativeSum += weights[i];
            if (randomRoll <= cumulativeSum) {
                executeSelectedState(i, player);
                return;
            }
        }
    }

    private void executeSelectedState(int index, Player player) {
        if (index == 0) {
            changeState(State.MACE_SLAM_ANTIC);
            lastExecutedMoveState = State.MACE_SLAM_ANTIC;
        } else if (index == 1) {
            changeState(State.CHARGE_RUN_ANTIC);
            lastExecutedMoveState = State.CHARGE_RUN_ANTIC;
        } else if (index == 2) {
            isPoweredLeap = false;
            changeState(State.LEAP_START);
            lastExecutedMoveState = State.LEAP_START;

            float deltaX = player.getPosition().x - position.x;
            float estimatedAirTime = 2f * (JUMP_VELOCITY_Y / Math.abs(GRAVITY));
            float targetedXVelocity = deltaX / estimatedAirTime;

            targetedXVelocity = MathUtils.clamp(targetedXVelocity, -380f, 380f);
            velocity.set(targetedXVelocity, JUMP_VELOCITY_Y);
        } else if (index == 3) {
            changeState(State.POWERED_SLAM_ANTIC);
            lastExecutedMoveState = State.POWERED_SLAM_ANTIC;
        }
    }

    @Override
    public void takeDamage(int amount, boolean hitFromLeft) {
        if (isStunnedOrDying()) {
            this.health -= amount;
            return;
        }

        this.health -= amount;

        if (currentState == State.IDLE || currentState == State.CHARGE_RUN) {
            if (damageWindowTimer <= 0) damageWindowTimer = 1.0f;
            recentDamageTaken += amount;

            if (recentDamageTaken >= maxHealth * 0.15f) {
                recentDamageTaken = 0;
                isPoweredLeap = false;
                changeState(State.LEAP_START);
                float escapeDir = hitFromLeft ? 1f : -1f;
                velocity.set(escapeDir * RUN_SPEED * 1.5f, JUMP_VELOCITY_Y * 0.8f);
            }
        }
    }

    private void exitStunnedPhaseTransition() {
        completedStunSequence = true;
        phase = 2;
        currentSpeedMultiplier = 1.35f;
        decisionCooldown = 0.70f;

        changeState(State.IDLE);
    }

    private void changeState(State newState) {
        this.currentState = newState;
        this.stateTime = 0f;
        this.velocity.set(0, 0);
    }

    private void clampPositionToBounds() {
        if (position.x <= leftBound) {
            position.x = leftBound;
        } else if (position.x >= rightBound - hitbox.width) {
            position.x = rightBound - hitbox.width;
        }
    }

    private boolean hitWallBoundary() {
        return position.x <= leftBound || position.x >= rightBound - hitbox.width;
    }

    private boolean canTrackPlayerDirection() {
        return currentState == State.IDLE || currentState == State.CHARGE_RUN || currentState == State.LEAP_START;
    }

    private boolean isStunnedOrDying() {
        return currentState == State.STUNNED || currentState == State.STUN_RECOVER || isDyingState();
    }

    private boolean isDyingState() {
        return currentState == State.DEATH_HIT || currentState == State.DEATH_FALL || currentState == State.DEATH_LAND;
    }

    private void requestCameraShake(float intensity, float duration) {
        this.triggerCameraShakeRequest = true;
        this.cameraShakeIntensity = intensity;
        this.cameraShakeDuration = duration;
    }

    @Override
    public void onPlayerHit() {
        this.velocity.set(0, 0);
        this.enemyKnockbackTimer = 0f;
        this.enemyKnockbackVelocityX = 0f;
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

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame = switch (currentState) {
            case IDLE -> animIdle.getKeyFrame(stateTime, true);
            case TURN -> animTurn.getKeyFrame(stateTime, false);
            case CHARGE_RUN_ANTIC -> animRunAntic.getKeyFrame(stateTime, false);
            case CHARGE_RUN -> animRun.getKeyFrame(stateTime, true);
            case MACE_SLAM_ANTIC, POWERED_SLAM_ANTIC -> animAttackAntic.getKeyFrame(stateTime, false);
            case MACE_SLAM, POWERED_SLAM -> animAttack.getKeyFrame(stateTime, false);
            case MACE_SLAM_RECOVER, POWERED_RECOVER -> animAttackRecover.getKeyFrame(stateTime, false);
            case LEAP_START -> animJump.getKeyFrame(stateTime, false);
            case LEAP_AIR -> animJumpAttack.getKeyFrame(stateTime, true);
            case LEAP_LAND -> animLand.getKeyFrame(stateTime, false);
            case STUNNED -> animBody.getKeyFrame(stateTime, true);
            case STUN_RECOVER -> animStunRecover.getKeyFrame(stateTime, false);
            case DEATH_HIT -> animDeathHit.getKeyFrame(stateTime, false);
            case DEATH_FALL -> animDeathFall.getKeyFrame(stateTime, true);
            case DEATH_LAND -> animDeathLand.getKeyFrame(stateTime, false);
        };

        if (currentFrame == null) return;
        boolean flipX = isFacingRight;
        float spriteScale = 0.42f;
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
}
