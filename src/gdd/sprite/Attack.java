package gdd.sprite;

import static gdd.Global.BOARD_HEIGHT;
import static gdd.Global.BOARD_WIDTH;
import static gdd.Global.IMG_BOSS_ATTACK;
import static gdd.Global.SCALE_FACTOR;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class Attack extends Sprite {
    private int side = 0; // Default side 0 = top, 1 = right, 2 = bottom, 3 = left
    private int length = 128; // Length of the attack sprite

    public Attack(int x, int y, int side) {
        initAttack(x, y, side);
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }

    private void initAttack(int x, int y, int side) {
    this.x = x;
    this.y = y;
    this.side = side;

    var ii = new ImageIcon(IMG_BOSS_ATTACK);
    var originalImage = ii.getImage();

    // Determine rotation angle
    int angle = switch (side) {
        case 0 -> 0; // Top
        case 1 -> 90; // Right
        case 2 -> 180; // Bottom
        case 3 -> 270; // Left
        default -> 0;
    };

    // Scale dimensions
    int origWidth = ii.getIconWidth() * SCALE_FACTOR;
    int origHeight = ii.getIconHeight() * SCALE_FACTOR;

    // Convert scaled image to a BufferedImage
    BufferedImage scaledBuffered = new BufferedImage(origWidth, origHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D gScale = scaledBuffered.createGraphics();
    gScale.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    gScale.drawImage(originalImage, 0, 0, origWidth, origHeight, null);
    gScale.dispose();

    // Determine rotated dimensions
    int width = (angle == 90 || angle == 270) ? origHeight : origWidth;
    int height = (angle == 90 || angle == 270) ? origWidth : origHeight;

    // Prepare final image with rotation
    BufferedImage rotated = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = rotated.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    
    // Rotate around center
    g2d.translate(width / 2.0, height / 2.0);
    g2d.rotate(Math.toRadians(angle));
    g2d.drawImage(scaledBuffered, -origWidth / 2, -origHeight / 2, null);
    g2d.dispose();

    setImage(rotated);   // Your method to assign the image
    setVisible(true);    // Your method to make it visible


    }

    @Override
    public void act() {

        // Move the attack based on its side
        switch (side) {
            case 0 -> this.y += 5; // Move down
            case 1 -> this.x -= 5; // Move right
            case 2 -> this.y -= 5; // Move up
            case 3 -> this.x += 5; // Move left
        }

        // Remove when off-screen
        if (this.x < 0 - length || this.x > BOARD_WIDTH + length || this.y < 0 - length || this.y > BOARD_HEIGHT + length) {
            this.visible = false;
        }
    }
}
