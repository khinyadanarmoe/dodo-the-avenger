package gdd.powerup;

import gdd.sprite.Player;

import static gdd.Global.IMG_POWERUP_ARMOR;
import static gdd.Global.SCALE_FACTOR;

import java.awt.Image;
import javax.swing.ImageIcon;

public class Shield extends PowerUp {
    
    public Shield(int x, int y) {
        super(x, y);
        // Use an appropriate shield image - you might need to create or find one
        // For now, using the powerup image as placeholder
        ImageIcon imageIcon = new ImageIcon(IMG_POWERUP_ARMOR);
        Image scaledImage = imageIcon.getImage().getScaledInstance(
            imageIcon.getIconWidth() * SCALE_FACTOR, 
            imageIcon.getIconHeight() * SCALE_FACTOR, 
            Image.SCALE_SMOOTH
        );
        this.setImage(scaledImage);
    }

    @Override
    public void act() {
        // Move left at same speed as other powerups
        this.x -= 2;
    }

    @Override
    public void upgrade(Player player) {
        // Activate shield protection for 2 seconds (120 frames at 60 FPS)
        player.activateShield(360);
        System.out.println("Shield activated! Player protected for 2 seconds.");
        this.die(); // Remove the powerup after collection
    }
}
