package com.Sepehr.HallowKnight.model.entities;

import com.Sepehr.HallowKnight.Menu.CharmType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.miniaudio.MiniAudio;
import java.util.HashSet;

public class Player extends Entity{
    private Texture placeholderTexture;
    private float MOVE_SPEED = 300f;
    private float DASH_SPEED = 600f;
    private final float JUMP_VELOCITY = 600f;
    private final float GRAVITY = -1500f;
    private final float INVULNERABILITY_DURATION = 1.5f;
    private final float MAX_JUMP_TIME = 0.3f;
    private final int MAX_JUMPS = 2;
    private final float DASH_DURATION = 0.2f;
    private float DASH_COOLDOWN = 0.6f;
    private float ATTACK_COOLDOWN = 0.75f;
    private static final float SPAWN_PROTECTION_DURATION = 1f;
    private final float KNOCKBACK_DURATION = 0.5f;
    private int maxMasks = 5;
    private float maxSoul = 100f;
    private float currentSoul = 0f;
    private float SOUL_PER_HIT = 11f;
    private final float SOUL_COST_PER_HEAL = 33f;
    private float HEAL_FOCUS_DURATION = 1.50f;
    private int ATTACK_DAMAGE = 1;

    private Vector2 startPos;
    private int currentMasks = 5;
    private float knockbackTimer = 0f;
    private float spawnProtectionTimer = 0f;
    private int maxHealth = 5;
    private float invulnerabilityTimer = 0f;
    private boolean isGrounded = false;
    private boolean isWalledLeft = false;
    private boolean isWalledRight = false;
    private Vector2 lastSafePosition;
    //attack
    private boolean isAttacking = false;
    private float attackTimer = 0f;
    private float attackCooldownTimer = 0f;
    private float ATTACK_DURATION = 0.25f;
    private int attackDirection = 0;
    private boolean hasHitThisAttack = false;
    // dash
    private boolean isDashing = false;
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    private float dashDirectionX = 0f;
    private boolean hasDashedInAir = false;
    //jump
    private boolean isJumping = false;
    private float jumpTimer = 0f;
    private int jumpCount = 0;
    //animations
    private TextureAtlas atlas;
    private Animation<TextureRegion> animIdle;
    private Animation<TextureRegion> animRun;
    private Animation<TextureRegion> animAirborne;
    private Animation<TextureRegion> animFall;
    private Animation<TextureRegion> animDoubleJump;
    private Animation<TextureRegion> animWallSlide;
    private Animation<TextureRegion> animDash;
    private Animation<TextureRegion> animSlashSide;
    private Animation<TextureRegion> animSlashUp;
    private Animation<TextureRegion> animSlashDown;
    private Animation<TextureRegion> animHurt;
    private Animation<TextureRegion> animDeath;
    private Animation<TextureRegion> animFireballCast;
    private Animation<TextureRegion> animScreamCast;
    private Animation<TextureRegion> animFocus;

    //audio
    private games.rednblack.miniaudio.MiniAudio miniAudio;
    private games.rednblack.miniaudio.MASound soundJump;
    private games.rednblack.miniaudio.MASound soundDoubleJump;
    private games.rednblack.miniaudio.MASound soundWallJump;
    private games.rednblack.miniaudio.MASound soundWallSlide;
    private games.rednblack.miniaudio.MASound soundDash;
    private games.rednblack.miniaudio.MASound soundFootsteps;
    private games.rednblack.miniaudio.MASound soundDeath;
    private games.rednblack.miniaudio.MASound soundHurt;
    private games.rednblack.miniaudio.MASound soundAttack;

    private boolean isWallSlideSoundPlaying = false;
    private boolean isFootstepSoundPlaying = false;
    private boolean hasPlayedDeathSound = false;

    public enum State {
        IDLE, RUNNING, AIRBORNE, FALLING, DOUBLE_JUMPING, WALL_SLIDING, DASHING, ATTACKING, HURT, DEATH , CASTING
    }
    private State currentState = State.IDLE;
    private State previousState = State.IDLE;
    private float stateTime = 0f;

    //controllers
    private int keyLeft, keyRight, keyJump, keyDash, keyAttack, keyUp, keyFocusHeal , keyDown , keySpellVengefulSpirit , keySpellHowlingWraiths;

    //spells
    public enum SpellType { NONE, VENGEFUL_SPIRIT, HOWLING_WRAITHS , HEAL }
    private SpellType activeCastType = SpellType.NONE;
    private float castAnimationLockTimer = 0f;
    private SpellType spellSpawningBuffer = SpellType.NONE;

    //charms
    private final HashSet<CharmType> equippedCharms = new HashSet<>();

    public Player(float x, float y , MiniAudio miniAudio) {
        startPos = new Vector2(x , y);
        super(x, y, 24, 40);
        this.miniAudio = miniAudio;
        loadKeyBindings();
        this.health = maxHealth;
        this.lastSafePosition = new Vector2(x, y);

        this.atlas = new TextureAtlas(Gdx.files.internal("New folder/knight"));

        animIdle        = new Animation<>(0.12f, atlas.findRegions("Idle"), Animation.PlayMode.LOOP);
        animRun         = new Animation<>(0.07f, atlas.findRegions("Run"), Animation.PlayMode.LOOP);
        animAirborne    = new Animation<>(0.10f, atlas.findRegions("Airborne"), Animation.PlayMode.LOOP);
        animFall        = new Animation<>(0.10f, atlas.findRegions("Fall"), Animation.PlayMode.LOOP);
        animDoubleJump  = new Animation<>(0.06f, atlas.findRegions("Double Jump"), Animation.PlayMode.NORMAL);
        animWallSlide   = new Animation<>(0.10f, atlas.findRegions("Wall Slide"), Animation.PlayMode.LOOP);
        animDash        = new Animation<>(0.04f, atlas.findRegions("Dash"), Animation.PlayMode.NORMAL);
        animSlashSide   = new Animation<>(0.05f, atlas.findRegions("Slash"), Animation.PlayMode.NORMAL);
        animSlashUp     = new Animation<>(0.05f, atlas.findRegions("UpSlash"), Animation.PlayMode.NORMAL);
        animSlashDown   = new Animation<>(0.05f, atlas.findRegions("DownSlash"), Animation.PlayMode.NORMAL);
        animHurt        = new Animation<>(0.10f, atlas.findRegions("Idle Hurt"), Animation.PlayMode.LOOP);
        animDeath       = new Animation<>(0.10f, atlas.findRegions("Death"), Animation.PlayMode.NORMAL);
        animFireballCast = new Animation<>(0.05f, atlas.findRegions("Fireball Cast"), Animation.PlayMode.NORMAL);
        animScreamCast   = new Animation<>(0.06f, atlas.findRegions("Scream"), Animation.PlayMode.NORMAL);
        animFocus        = new Animation<>(0.08f, atlas.findRegions("Focus"), Animation.PlayMode.LOOP);

        soundJump       = miniAudio.createSound("audio/hero_jump.wav");
        soundDoubleJump = miniAudio.createSound("audio/hero_wings.wav");
        soundWallJump   = miniAudio.createSound("audio/hero_wall_jump.wav");
        soundWallSlide  = miniAudio.createSound("audio/hero_wall_slide.wav");
        soundDash       = miniAudio.createSound("audio/hero_super_dash_burst.wav");
        soundHurt       = miniAudio.createSound("audio/hero_damage.wav");
        soundFootsteps = miniAudio.createSound("audio/hero_run_footsteps_grass.wav");
        soundDeath     = miniAudio.createSound("audio/hero_death_v2.wav");
        soundAttack = miniAudio.createSound("audio/hero_double_damage.wav");
        soundFootsteps.setLooping(true);
        soundWallSlide.setLooping(true);
        //will be deleted in the future
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.placeholderTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void loadKeyBindings() {
        Preferences prefs = Gdx.app.getPreferences("HollowKnightControls");
        this.keyLeft  = prefs.getInteger("key_left", Input.Keys.LEFT);
        this.keyRight = prefs.getInteger("key_right", Input.Keys.RIGHT);
        this.keyJump  = prefs.getInteger("key_jump", Input.Keys.Z);
        this.keyDash  = prefs.getInteger("key_dash", Input.Keys.C);
        this.keyAttack = prefs.getInteger("key_attack", Input.Keys.X);
        this.keyUp    = prefs.getInteger("key_up", Input.Keys.UP);
        this.keyDown  = prefs.getInteger("key_down", Input.Keys.DOWN);
        this.keySpellVengefulSpirit = prefs.getInteger("key_spell_vengeful", Input.Keys.S);
        this.keySpellHowlingWraiths = prefs.getInteger("key_spell_wraiths", Input.Keys.D);
        this.keyFocusHeal = prefs.getInteger("key_focus_heal", Input.Keys.A);
    }

    @Override
    public void update(float delta) {
        stateTime += delta;


        if (invulnerabilityTimer > 0) invulnerabilityTimer -= delta;
        if (dashCooldownTimer > 0) dashCooldownTimer -= delta;
        if (attackCooldownTimer > 0) attackCooldownTimer -= delta;

        if (knockbackTimer > 0) {
            knockbackTimer -= delta;
            velocity.x *= 0.92;
        }

        if (health <= 0) {
            currentState = State.DEATH;
            velocity.set(0, 0);
            if (isFootstepSoundPlaying) { soundFootsteps.stop(); isFootstepSoundPlaying = false; }
            if (isWallSlideSoundPlaying) { soundWallSlide.stop(); isWallSlideSoundPlaying = false; }

            if (!hasPlayedDeathSound) {
                soundDeath.setVolume(0.7f);
                soundDeath.play();
                hasPlayedDeathSound = true;
            }

            if (animDeath.isAnimationFinished(stateTime)) {
                handleDeath();
            }
            return;
        }

        if (isAttacking) {
            attackTimer -= delta;
            if (attackTimer <= 0) isAttacking = false;
        }

        if (isDashing) {
            dashTimer -= delta;
            velocity.x = dashDirectionX * DASH_SPEED;
            velocity.y = 0;
            if (dashTimer <= 0) {
                isDashing = false;
                velocity.x = 0;
            }
            currentState = State.DASHING;
            syncStateTimeline();
            return;
        }

        if (activeCastType != SpellType.NONE) {
            castAnimationLockTimer -= delta;
            velocity.set(0, 0);
            currentState = State.CASTING;

            if (castAnimationLockTimer <= 0) {
                if (activeCastType == SpellType.HEAL) {
                    tryHeal();
                }
                activeCastType = SpellType.NONE;
            }
            syncStateTimeline();
            return;
        }

        if (isGrounded && velocity.y == 0f) {
            this.lastSafePosition.set(position.x, position.y + 2);
        }

        handleInput();

        if (spawnProtectionTimer > 0) {
            spawnProtectionTimer -= delta;
            velocity.set(0, 0);
        }
        else {

            if ((isWalledLeft || isWalledRight) && velocity.y < 0) {
                jumpCount = 0;
                velocity.y += (GRAVITY * 0.35f) * delta;
            } else {
                velocity.y += GRAVITY * delta;
            }
        }
        evaluateStates();
        syncStateTimeline();
        if (currentState == State.WALL_SLIDING && velocity.y < 0) {
            if (!isWallSlideSoundPlaying) {
                soundWallSlide.setVolume(1f);
                soundWallSlide.play();
                isWallSlideSoundPlaying = true;
            }
        } else {
            if (isWallSlideSoundPlaying) {
                soundWallSlide.stop();
                isWallSlideSoundPlaying = false;
            }
        }
        if (currentState == State.RUNNING && isGrounded) {
            if (!isFootstepSoundPlaying) {
                soundFootsteps.setVolume(0.4f);
                soundFootsteps.play();
                isFootstepSoundPlaying = true;
            }
        } else {
            if (isFootstepSoundPlaying) {
                soundFootsteps.stop();
                isFootstepSoundPlaying = false;
            }
        }
        if (currentState == State.DEATH) {
            if (!hasPlayedDeathSound) {
                soundDeath.setVolume(0.7f);
                soundDeath.play();
                hasPlayedDeathSound = true;
            }
        }
    }

    private void handleInput() {
        if(knockbackTimer <= 0) {
            // X-Axis Kinematics Logic
            velocity.x = 0;
            if (Gdx.input.isKeyPressed(keyLeft)) {
                velocity.x = -MOVE_SPEED;
                isFacingRight = false;
            }
            if (Gdx.input.isKeyPressed(keyRight)) {
                velocity.x = MOVE_SPEED;
                isFacingRight = true;
            }
            // Dash Trigger Core
            if (Gdx.input.isKeyJustPressed(keyDash) && dashCooldownTimer <= 0) {
                isDashing = true;
                dashTimer = DASH_DURATION;
                dashCooldownTimer = DASH_COOLDOWN;
                dashDirectionX = isFacingRight ? 1f : -1f;
                soundDash.setVolume(0.6f);
                soundDash.play();
                return;
            }
            // Jump Mechanics Mapping
            if (Gdx.input.isKeyJustPressed(keyJump)) {
                if (isGrounded) {
                    velocity.y = JUMP_VELOCITY;
                    jumpCount = 1;
                    isGrounded = false;
                    soundJump.setVolume(0.5f);
                    soundJump.play();
                } else if ((isWalledLeft || isWalledRight) && !isGrounded) {
                    velocity.y = JUMP_VELOCITY * 0.9f;
               velocity.x = isWalledLeft ? MOVE_SPEED : -MOVE_SPEED;
               isFacingRight = isWalledLeft;
                    jumpCount = 1;
                    soundWallJump.setVolume(0.6f);
                    soundWallJump.play();
                } else if (jumpCount < MAX_JUMPS) {
                    velocity.y = JUMP_VELOCITY * 0.9f;
                    jumpCount++;
                    currentState = State.DOUBLE_JUMPING;
                    stateTime = 0f;
                    soundDoubleJump.setVolume(0.6f);
                    soundDoubleJump.play();
                }
            }
            // Attack Mechanics Mapping
            if (Gdx.input.isKeyJustPressed(keyAttack) && !isAttacking && attackCooldownTimer <= 0) {
                isAttacking = true;
                attackTimer = ATTACK_DURATION;
                attackCooldownTimer = ATTACK_COOLDOWN;
                stateTime = 0f;
                this.hasHitThisAttack = false;
                if (soundAttack != null) {
                    soundAttack.stop();
                    soundAttack.setVolume(0.5f);
                    soundAttack.play();
                }
                if (Gdx.input.isKeyPressed(keyUp)) {
                    attackDirection = 1; // Upward Slash
                } else if (Gdx.input.isKeyPressed(keyDown) && !isGrounded) {
                    attackDirection = 2; // Downward Pogo Slash
                } else {
                    attackDirection = 0; // Neutral Facing Slash
                }
            }

            if (Gdx.input.isKeyJustPressed(keySpellVengefulSpirit) && activeCastType == SpellType.NONE && !isDashing && !isAttacking) {
                if (currentSoul >= SOUL_COST_PER_HEAL) {
                    currentSoul -= SOUL_COST_PER_HEAL;
                    activeCastType = SpellType.VENGEFUL_SPIRIT;
                    castAnimationLockTimer = 0.35f;
                    spellSpawningBuffer = SpellType.VENGEFUL_SPIRIT;
                    stateTime = 0f;
                    return;
                }
            }

            if (Gdx.input.isKeyJustPressed(keySpellHowlingWraiths) && activeCastType == SpellType.NONE && !isDashing && !isAttacking) {
                if (currentSoul >= SOUL_COST_PER_HEAL) {
                    currentSoul -= SOUL_COST_PER_HEAL;
                    activeCastType = SpellType.HOWLING_WRAITHS;
                    castAnimationLockTimer = 0.45f;
                    spellSpawningBuffer = SpellType.HOWLING_WRAITHS;
                    stateTime = 0f;
                    return;
                }
            }

            if (Gdx.input.isKeyJustPressed(keyFocusHeal) && activeCastType == SpellType.NONE && !isDashing && !isAttacking) {
                if (currentSoul >= SOUL_COST_PER_HEAL && currentMasks < maxMasks) {
                    activeCastType = SpellType.HEAL;
                    castAnimationLockTimer = HEAL_FOCUS_DURATION;
                    stateTime = 0f;
                }
            }
        }
    }

    private void evaluateStates() {
        if (invulnerabilityTimer > (INVULNERABILITY_DURATION - 0.25f)) {
            currentState = State.HURT;
        } else if (isAttacking) {
            currentState = State.ATTACKING;
        } else if ((isWalledLeft || isWalledRight) && !isGrounded && velocity.y < 0) {
            currentState = State.WALL_SLIDING;
        } else if (velocity.y > 0) {
            currentState = (jumpCount == 2) ? State.DOUBLE_JUMPING : State.AIRBORNE;
        } else if (velocity.y < 0 && !isGrounded) {
            currentState = State.FALLING;
        } else if (velocity.x != 0) {
            currentState = State.RUNNING;
        } else {
            currentState = State.IDLE;
        }
    }

    private void syncStateTimeline() {
        if (currentState != previousState) {
            stateTime = 0f;
        }
        previousState = currentState;
    }

    public void takeHazardDamage(int damage) {
        if (invulnerabilityTimer > 0 || health <= 0) return;

        this.health -= damage;
        loseMask(damage);

        if (this.health <= 0) {
            this.health = 0;
            this.currentState = State.DEATH;
            this.stateTime = 0f;
            this.velocity.set(0, 0);
        } else {
            this.invulnerabilityTimer = INVULNERABILITY_DURATION;
            this.stateTime = 0f;
            if (soundHurt != null) {
                soundHurt.setVolume(0.6f);
                soundHurt.play();
            }
            respawnAtLastSafeGround();
        }
    }

    private void handleDeath() {
        this.health = maxHealth;
        this.currentMasks = maxMasks;
        this.position.set(startPos.x , startPos.y);
        this.lastSafePosition.set(startPos.x, startPos.y + 2);

        this.velocity.set(0, 0);
        this.isDashing = false;
        this.isAttacking = false;

        this.currentState = State.IDLE;
        this.stateTime = 0f;
        this.hasPlayedDeathSound = false;

        updateHitbox();
    }

    private void respawnAtLastSafeGround() {
        this.position.set(lastSafePosition.x, lastSafePosition.y);
        this.velocity.set(0, 0);
        this.isDashing = false;
        this.isAttacking = false;
        updateHitbox();
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.setColor(Color.WHITE);

        if (invulnerabilityTimer > 0 && (int)(invulnerabilityTimer * 12) % 2 == 0) {
            batch.setColor(Color.RED);
        }

        TextureRegion currentFrame;
        switch (currentState) {
            case DEATH:
                currentFrame = animDeath.getKeyFrame(stateTime, false);
                break;
            case CASTING:
                if (activeCastType == SpellType.VENGEFUL_SPIRIT) {
                    currentFrame = animFireballCast.getKeyFrame(stateTime, false);
                } else if (activeCastType == SpellType.HOWLING_WRAITHS) {
                    currentFrame = animScreamCast.getKeyFrame(stateTime, false);
                }
                else if (activeCastType == SpellType.HEAL) {
                    currentFrame = animFocus.getKeyFrame(stateTime , true);
                } else {
                    currentFrame = animIdle.getKeyFrame(stateTime, true);
                }
                break;
            case HURT:
                currentFrame = animHurt.getKeyFrame(stateTime, true);
                break;
            case DASHING:
                currentFrame = animDash.getKeyFrame(stateTime, false);
                break;
            case ATTACKING:
                currentFrame = (attackDirection == 1) ? animSlashUp.getKeyFrame(stateTime, false) :
                    (attackDirection == 2) ? animSlashDown.getKeyFrame(stateTime, false) :
                        animSlashSide.getKeyFrame(stateTime, false);
                break;
            case WALL_SLIDING:
                currentFrame = animWallSlide.getKeyFrame(stateTime, true);
                break;
            case DOUBLE_JUMPING:
                currentFrame = animDoubleJump.getKeyFrame(stateTime, false);
                break;
            case AIRBORNE:
                currentFrame = animAirborne.getKeyFrame(stateTime, true);
                break;
            case FALLING:
                currentFrame = animFall.getKeyFrame(stateTime, true);
                break;
            case RUNNING:
                currentFrame = animRun.getKeyFrame(stateTime, true);
                break;
            case IDLE:
            default:
                currentFrame = animIdle.getKeyFrame(stateTime, true);
                break;
        }

        if (currentFrame == null) return;

        boolean flipX = isFacingRight;

        // Draw perfectly mapped to match your bounding boxes
        float spriteScale = 0.5f;

        float drawnWidth = currentFrame.getRegionWidth() * spriteScale;
        float drawnHeight = currentFrame.getRegionHeight() * spriteScale;
        float drawX = position.x + (hitbox.width / 2f) - (drawnWidth / 2f);
        float drawY = position.y;

        batch.draw(
            currentFrame.getTexture(),
            drawX, drawY,                                  // Screen Placement
            drawnWidth / 2f, drawnHeight / 2f,             // Origin Point for rotation/scaling
            drawnWidth, drawnHeight,                       // Width and Height dimensions on screen
            1f, 1f,                                        // Scale dimensions factors
            0f,                                            // Rotation degrees
            currentFrame.getRegionX(), currentFrame.getRegionY(), // Coordinates inside texture pack sheet
            currentFrame.getRegionWidth(), currentFrame.getRegionHeight(), // Dimensions inside texture pack sheet
            flipX, false                                   // Flipping fields (Horizontally, Vertically)
        );
        batch.setColor(Color.WHITE);
    }

    public void takeDamage(int amount, boolean knockLeft) {
        if (invulnerabilityTimer > 0 || spawnProtectionTimer > 0 || health <= 0) return;

        this.health -= amount;
        loseMask(1);

        if (this.health <= 0) {
            this.health = 0;
            this.currentState = State.DEATH;
            this.stateTime = 0f;
            this.velocity.set(0, 0);
        } else {
            this.invulnerabilityTimer = INVULNERABILITY_DURATION;
            this.knockbackTimer = KNOCKBACK_DURATION;
            this.stateTime = 0f;
            this.velocity.y = 220f;
            this.velocity.x = knockLeft ? -300f : 300f;

            if (soundHurt != null) {
                soundHurt.setVolume(0.6f);
                soundHurt.play();
            }
        }
    }

    public Rectangle getAttackHitbox() {
        if (!isAttacking || hasHitThisAttack) return null;

        Rectangle attackBox = new Rectangle();
        float nailReach = 64f;
        float nailThickness = 48f;

        if (attackDirection == 0) {
            attackBox.width = nailReach;
            attackBox.height = nailThickness;
            attackBox.y = position.y + (hitbox.height / 2f) - (nailThickness / 2f);
            if (isFacingRight) {
                attackBox.x = position.x + hitbox.width;
            } else {
                attackBox.x = position.x - nailReach;
            }
        } else if (attackDirection == 1) {
            attackBox.width = hitbox.width + 12f;
            attackBox.height = nailReach;
            attackBox.x = position.x - 6f;
            attackBox.y = position.y + hitbox.height;
        } else if (attackDirection == 2) {
            attackBox.width = hitbox.width + 12f;
            attackBox.height = nailReach;
            attackBox.x = position.x - 6f;
            attackBox.y = position.y - nailReach;
        }

        return attackBox;
    }

    public void onNailConnect() {
        this.hasHitThisAttack = true;
        if (attackDirection == 2) {
            this.velocity.y = JUMP_VELOCITY * 0.85f;
            this.isGrounded = false;
            this.jumpCount = 1;
            this.hasDashedInAir = false;
        } else if (attackDirection == 0) {
            this.velocity.x = isFacingRight ? -200f : 200f;
            this.knockbackTimer = 0.07f;
        }
    }

    public void dispose() {
        if (placeholderTexture != null) placeholderTexture.dispose();
        atlas.dispose();
        if (soundJump != null) soundJump.dispose();
        if (soundDoubleJump != null) soundDoubleJump.dispose();
        if (soundWallJump != null) soundWallJump.dispose();
        if (soundWallSlide != null) soundWallSlide.dispose();
        if (soundDash != null) soundDash.dispose();
        if (soundHurt != null) soundHurt.dispose();
        if (soundFootsteps != null) soundFootsteps.dispose();
        if (soundDeath != null) soundDeath.dispose();
        if (soundAttack != null) soundAttack.dispose();
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
        if(isGrounded) {
            jumpCount = 0;
            this.hasDashedInAir = false;
        }
    }

    public void setWallStates(boolean left, boolean right) {
        this.isWalledLeft = left;
        this.isWalledRight = right;
    }

    public Vector2 getVelocity() { return velocity; }

    public Vector2 getPosition() { return position; }

    public boolean isInvulnerable() { return invulnerabilityTimer > 0; }

    public Vector2 getLastSafePosition() { return lastSafePosition; }

    public void resetSpawnProtection() {
        this.spawnProtectionTimer = SPAWN_PROTECTION_DURATION;
        this.velocity.set(0, 0);
    }

    public void gainSoul() {
        currentSoul = Math.min(maxSoul, currentSoul + SOUL_PER_HIT);
    }

    public void loseMask(int damage) {
        currentMasks = Math.max(0, currentMasks - damage);
        if (currentMasks <= 0) {
        }
    }

    public void tryHeal() {
        if (currentSoul >= SOUL_COST_PER_HEAL && currentMasks < maxMasks) {
            currentSoul -= SOUL_COST_PER_HEAL;
            health++;
            currentMasks++;
        }
    }

    public int getCurrentMasks() { return currentMasks; }

    public int getMaxMasks() { return maxMasks; }

    public int getAttackDamage() {
        return this.ATTACK_DAMAGE;
    }

    public float getSoulPercentage() { return currentSoul / maxSoul; }

    public SpellType pollPendingSpell() {
        SpellType type = spellSpawningBuffer;
        spellSpawningBuffer = SpellType.NONE;
        return type;
    }

    public TextureAtlas getAtlas() {
        return this.atlas;
    }

    public boolean isFacingRight() {
        return this.isFacingRight;
    }

    public boolean isCharmEquipped(CharmType type) {
        return equippedCharms.contains(type);
    }

    public boolean toggleCharmState(CharmType type) {
        if (equippedCharms.contains(type)) {
            equippedCharms.remove(type);
        } else {
            equippedCharms.add(type);
        }
        recalculateCharmModifiers();
        return true;
    }

    private void recalculateCharmModifiers() {
        this.MOVE_SPEED = 300f;
        this.DASH_SPEED = 600f;
        this.DASH_COOLDOWN = 0.6f;
        this.ATTACK_DURATION = 0.25f;
        this.SOUL_PER_HIT = 11f;
        this.ATTACK_COOLDOWN = 0.75f;
        this.HEAL_FOCUS_DURATION = 1.50f;
        this.ATTACK_DAMAGE = 1;
        if (equippedCharms.contains(CharmType.QUICK_SLASH)) {
            this.ATTACK_DURATION = 0.14f;
            this.ATTACK_COOLDOWN = 0.5f;
        }

        if (equippedCharms.contains(CharmType.DASHMASTER)) {
            this.DASH_COOLDOWN = 0.35f;
        }

        if (equippedCharms.contains(CharmType.SOUL_CATCHER)) {
            this.SOUL_PER_HIT = 17f;
        }

        if (equippedCharms.contains(CharmType.QUICK_FOCUS)) {
            this.HEAL_FOCUS_DURATION = 0.95f;
        }

        if(equippedCharms.contains(CharmType.UNBREAKABLE_STRENGTH)) {
            this.ATTACK_DAMAGE = 2;
        }

    }
}
