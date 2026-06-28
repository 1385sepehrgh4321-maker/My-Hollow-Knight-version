package com.Sepehr.HallowKnight.model.entities.enemies;

import com.Sepehr.HallowKnight.model.entities.Entity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public abstract class Enemy extends Entity implements Disposable {
    protected float enemyKnockbackTimer = 0f;
    protected float enemyKnockbackVelocityX = 0f;
    protected final float ENEMY_KNOCKBACK_DURATION = 0.4f;

    public Enemy(float x, float y, float width, float height) {
        super(x, y, width, height);
    }
    @Override
    public abstract void update(float delta);
    @Override
    public abstract void draw(SpriteBatch batch);

    public void takeDamage(int amount) {
        this.health -= amount;
    }

    public abstract boolean isDeadFinished();

    public abstract void dispose();

    public void takeDamage(int amount, boolean hitFromLeft) {
        this.health -= amount;
        float knockbackForce = 250f;
        this.enemyKnockbackVelocityX = hitFromLeft ? knockbackForce : -knockbackForce;
        this.enemyKnockbackTimer = ENEMY_KNOCKBACK_DURATION;
        this.velocity.x = this.enemyKnockbackVelocityX;
    }



    public int getHealth() { return this.health; }

    public Vector2 getPosition() { return this.position; }

    public void onPlayerHit() {
        this.velocity.x = 0;
    }

    public void setHealth(int health) { this.health = health; }
}
