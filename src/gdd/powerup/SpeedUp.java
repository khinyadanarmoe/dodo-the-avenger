package gdd.powerup;

import static gdd.Global.*;
import gdd.sprite.Player;
import javax.swing.ImageIcon;



public class SpeedUp extends PowerUp {

    public SpeedUp(int x, int y) {
        super(x, y);
        // Set image
        ImageIcon ii = new ImageIcon(IMG_POWERUP_SPEEDUP);
        var scaledImage = ii.getImage().getScaledInstance(
                ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public void act() {
        // SpeedUp specific behavior for side-scrolling
        // Move left across the screen like enemies
        this.x -= 2; // Move left by 2 pixels each frame
    }

    public void upgrade(Player player) {
        // Upgrade the player with speed boost
        player.setJumpStrength(-20); // Increase jump strength by 4
        System.out.println("Jump strength increased! Player can jump higher now.");
        this.die(); // Remove the power-up after use
    }

}
