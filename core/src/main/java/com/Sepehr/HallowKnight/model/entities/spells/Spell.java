package com.Sepehr.HallowKnight.model.entities.spells;

import com.Sepehr.HallowKnight.model.entities.enemies.Enemy;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Spell {
    protected float x, y;
    protected Rectangle hitbox;
    protected boolean active = true;

    public Spell(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.hitbox = new Rectangle(x, y, width, height);
    }

    public abstract void update(float delta);
    public abstract void draw(SpriteBatch batch);
    public abstract void handleEnemyCollision(Enemy enemy);

    public Rectangle getHitbox() { return hitbox; }
    public boolean isActive() { return active; }
    public void destroy() { this.active = false; }

    // Most spells pierce enemies, but override and return true if a wall should kill it
    public boolean shouldDestroyOnWalls() { return false; }
}
