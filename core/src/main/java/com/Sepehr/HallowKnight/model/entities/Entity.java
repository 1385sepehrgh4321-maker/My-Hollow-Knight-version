package com.Sepehr.HallowKnight.model.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity {
    protected Vector2 position;
    protected Vector2 velocity;
    protected Rectangle hitbox;
    protected int health;
    protected boolean isFacingRight;

    public Entity(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.hitbox = new Rectangle(x, y, width, height);
        this.isFacingRight = true;
    }

    public abstract void update(float delta);
    public abstract void draw(SpriteBatch batch);

    public void updateHitbox() {
        hitbox.setPosition(position.x, position.y);
    }

    public Rectangle getHitbox() { return hitbox; }
}
