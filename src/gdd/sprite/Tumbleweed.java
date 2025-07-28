package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

import gdd.Global;

public class Tumbleweed extends Obstacle {

    private int rotationAngle = 0;
    private int bounceHeight = 0;
    private int bounceDirection = 1;

    public Tumbleweed(int x, int y) {
        super(x, y);
        initTumbleweed();
    }

    private void initTumbleweed() {
        this.health = 1;
        this.destructible = false; // Cannot be destroyed in Scene1
        
        // Load tumbleweed image
        var ii = new ImageIcon(IMG_TUMBLEWEED);
        
        // Scale the image
        var scaledImage = ii.getImage().getScaledInstance(
            ii.getIconWidth() * SCALE_FACTOR,
            ii.getIconHeight() * SCALE_FACTOR,
            java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    @Override
    public void act() {
        // Tumbleweed-specific movement
        this.x -= globalSpeed + 1; // Move left faster than default obstacles
        
        // Add rolling/bouncing effect
        bounceHeight += bounceDirection;
        if (bounceHeight > 20) {
            bounceDirection = -1;
        } else if (bounceHeight < -20) {
            bounceDirection = 1;
        }
        
        // Apply bounce to Y position
        this.y += bounceDirection;
        
        // Rotation effect (visual only - for future animation)
        rotationAngle = (rotationAngle + 5) % 360;
        
        // // Remove when off-screen
        // if (this.x < -50) {
        //     this.visible = false;
        // }
    }

    @Override
    protected void destroy() {
        super.destroy();
        // TODO: Add tumbleweed destruction effect (particles, sound, etc.)
        System.out.println("Tumbleweed destroyed at position: " + x + ", " + y);
    }

    public int getRotationAngle() {
        return rotationAngle;
    }
}
