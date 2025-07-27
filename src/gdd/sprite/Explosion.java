package gdd.sprite;

import static gdd.Global.*;

import java.awt.Image;

import javax.swing.ImageIcon;

public class Explosion extends Sprite {


    public Explosion(int x, int y) {

        initExplosion(x, y);
    }

    private void initExplosion(int x, int y) {

        this.x = x;
        this.y = y;

        var ii = new ImageIcon(IMG_EXPLOSION);

        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(
                ii.getIconWidth() * 3,
                ii.getIconHeight() * 3,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    public Image getImage() {
        // Return the explosion image
        return image;
    }

    @Override
    public void act() {
        // Explosion doesn't move, so this can be empty
    }
}
