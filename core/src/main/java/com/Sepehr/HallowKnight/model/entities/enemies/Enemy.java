package com.Sepehr.HallowKnight.model.entities.enemies;

import com.Sepehr.HallowKnight.model.entities.Entity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public abstract class Enemy extends Entity implements Disposable {

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
}
