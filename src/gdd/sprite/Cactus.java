package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Cactus extends Obstacle {

    private int spikeDamage = 2;
    private boolean hasSpikes = true;

    public Cactus(int x, int y) {
        super(x, y);
        initCactus();
    }

    private void initCactus() {
        this.health = 3; // Cactus is tougher than tumbleweed
        this.destructible = false; // Cannot be destroyed in Scene1 (only avoided)
        
        // Load cactus image
        var ii = new ImageIcon(IMG_CACTUS);
        
        // Scale the image - make cactus bigger than tumbleweed
        var scaledImage = ii.getImage().getScaledInstance(
            ii.getIconWidth() * SCALE_FACTOR,
            ii.getIconHeight() * SCALE_FACTOR,
            java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    @Override
    public void act() {
        // Cactus moves slower than tumbleweed
        this.x -= 2;
        
        // Cactus stays at ground level (no bouncing)
        // You could add slight swaying animation here
        
        // Remove when off-screen
        if (this.x < -50) {
            this.visible = false;
        }
        
    }

   

    @Override
    protected void destroy() {
        super.destroy();
        // TODO: Add cactus destruction effect (breaking into pieces, etc.)
        System.out.println("Cactus destroyed at position: " + x + ", " + y);
    }

    @Override
    public void takeDamage(int damage) {
        if (destructible) {
            health -= damage;
            System.out.println("Cactus took " + damage + " damage. Health remaining: " + health);
            
            if (health <= 0) {
                destroy();
            } else {
                // Cactus could lose spikes when damaged
                if (health == 1) {
                    hasSpikes = false;
                    spikeDamage = 0;
                    System.out.println("Cactus lost its spikes!");
                }
            }
        }
    }

    public int getSpikeDamage() {
        return spikeDamage;
    }

    public boolean hasSpikes() {
        return hasSpikes;
    }

    // Method to handle player collision with cactus
    public int getCollisionDamage() {
        return hasSpikes ? spikeDamage : 1; // Less damage if spikes are gone
    }
}
