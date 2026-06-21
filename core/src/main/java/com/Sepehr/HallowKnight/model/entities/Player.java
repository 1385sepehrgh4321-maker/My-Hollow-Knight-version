package com.Sepehr.HallowKnight.model.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player extends Entity{
    private Texture placeholderTexture;
    private final float MOVE_SPEED = 300f;
    private final float JUMP_VELOCITY = 600f;
    private final float GRAVITY = -1500f;
    private final float INVULNERABILITY_DURATION = 1.5f;
    private final float MAX_JUMP_TIME = 0.3f;
    private final int MAX_JUMPS = 2;

    private int maxHealth = 5;
    private float invulnerabilityTimer = 0f;
    private boolean isGrounded = false;
    private Vector2 lastSafePosition;

    private boolean isJumping = false;
    private float jumpTimer = 0f;
    private int jumpCount = 0;

    private int keyLeft;
    private int keyRight;
    private int keyJump;

    public Player(float x, float y) {
        super(x, y, 24, 40);
        loadKeyBindings();
        this.health = maxHealth;
        this.lastSafePosition = new Vector2(x, y);
        //will be deleted in the future
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.placeholderTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void loadKeyBindings() {
        Preferences prefs = Gdx.app.getPreferences("HollowKnightSettings");
        this.keyLeft = prefs.getInteger("key_left", Input.Keys.LEFT);
        this.keyRight = prefs.getInteger("key_right", Input.Keys.RIGHT);
        this.keyJump = prefs.getInteger("key_jump", Input.Keys.Z);
    }

    @Override
    public void update(float delta) {
        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer -= delta;
        }

        if (isGrounded && velocity.y == 0f) {
            this.lastSafePosition.set(position.x, position.y + 2);
        }

        handleInput();

        if(isJumping)
            jumpTimer += delta;

        velocity.y += GRAVITY * delta;
    }

    private void handleInput() {
        velocity.x = 0;

        if (Gdx.input.isKeyPressed(keyLeft)) {
            velocity.x = -MOVE_SPEED;
            isFacingRight = false;
        }

        if (Gdx.input.isKeyPressed(keyRight)) {
            velocity.x = MOVE_SPEED;
            isFacingRight = true;
        }

        if (Gdx.input.isKeyJustPressed(keyJump)) {
            if (!isGrounded && jumpCount == 0) {
                jumpCount = 1;
            }

            if (jumpCount < MAX_JUMPS) {
                isJumping = true;
                isGrounded = false;
                jumpTimer = 0f;
                velocity.y = JUMP_VELOCITY;
                jumpCount++;
            }
        }

        if (Gdx.input.isKeyPressed(keyJump) && isJumping) {
            if (jumpTimer < MAX_JUMP_TIME) {
                velocity.y = JUMP_VELOCITY;
            } else {
                isJumping = false;
            }
        }

        if (!Gdx.input.isKeyPressed(keyJump)) {
            isJumping = false;
        }
    }

    public void takeHazardDamage(int damage) {
        if (invulnerabilityTimer > 0) return;
        this.health -= damage;

        if (this.health <= 0) {
            handleDeath();
        } else {
            respawnAtLastSafeGround();
        }
    }

    private void handleDeath() {
        System.out.println("Game Over! The Knight perished.");
        // TODO: Trigger full room reload screen, or reset health and send to a Bench checkpoint!
        this.health = maxHealth;
        respawnAtLastSafeGround();
    }

    private void respawnAtLastSafeGround() {
        this.position.set(lastSafePosition.x, lastSafePosition.y);
        this.velocity.set(0, 0);
        updateHitbox();

        this.invulnerabilityTimer = INVULNERABILITY_DURATION;
    }

    @Override
    public void draw(SpriteBatch batch) {
//        Color originalColor = batch.getColor();
        batch.setColor(Color.WHITE);

        if (isInvulnerable()) {
            if ((int)(invulnerabilityTimer * 15) % 2 == 0) {
                batch.setColor(com.badlogic.gdx.graphics.Color.RED);
            } else {
                batch.setColor(1, 1, 1, 0.3f);
            }
        } else {
            batch.setColor(0.3f, 0.6f, 0.9f, 1.0f);
        }

        batch.draw(placeholderTexture, position.x, position.y, hitbox.width, hitbox.height);

        batch.setColor(Color.WHITE);

//        batch.setColor(originalColor);
    }

    public void dispose() {
        if (placeholderTexture != null) placeholderTexture.dispose();
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;

        if(isGrounded)
            jumpCount = 0;
    }
    public Vector2 getVelocity() { return velocity; }
    public Vector2 getPosition() { return position; }
    public boolean isInvulnerable() { return invulnerabilityTimer > 0; }
    public Vector2 getLastSafePosition() { return lastSafePosition; }
}
