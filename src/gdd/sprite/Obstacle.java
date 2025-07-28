package gdd.sprite;

import static gdd.Global.globalSpeed;

import gdd.Global;

public abstract class Obstacle extends Sprite {

    protected int health;
    protected boolean destructible;

    public Obstacle(int x, int y) {
        initObstacle(x, y);
    }

    protected void initObstacle(int x, int y) {
        this.x = x;
        this.y = y;
        this.health = 1; // Default health
        this.destructible = false; // Default is indestructible
    }

    @Override
    public void act() {
        // Default obstacle behavior - move left for side-scrolling
        this.x -= globalSpeed; // Move left by global speed
        
        // Remove obstacle when it goes off-screen
        if (this.x < -50) {
            this.visible = false;
        }
    }

    public boolean isDestructible() {
        return destructible;
    }

    public void setDestructible(boolean destructible) {
        this.destructible = destructible;
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int damage) {
        if (destructible) {
            health -= damage;
            if (health <= 0) {
                destroy();
            }
        }
    }

    protected void destroy() {
        this.visible = false;
        // Override in subclasses for specific destruction effects
    }

   
}
