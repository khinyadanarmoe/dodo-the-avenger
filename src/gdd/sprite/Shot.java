package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Shot extends Sprite {

    private static final int H_SPACE = 20;
    private static final int V_SPACE = 1;
    private int shotSpeed = 10; // Speed of the shot

    public Shot(int direction, Player player) {
        // Initialize shot direction based on player direction
        if (direction == Player.DIR_LEFT) {
            shotSpeed = -shotSpeed; // Move left
        } else if (direction == Player.DIR_RIGHT) {
            shotSpeed = shotSpeed; // Move right
        } else {
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }
        // Initialize shot position based on player position
        int x = player.getX() + (direction == Player.DIR_LEFT ? -H_SPACE : H_SPACE);
        int y = player.getY() + (player.getWidth() / 2);
        // Call the initShot method to set up the shot
        initShot(x, y);
    }

    public Shot(int x, int y) {

        initShot(x, y);
    }

    private void initShot(int x, int y) {
        // Create a 10x2 red pixel image
        int width = 10;
        int height = 5;
        java.awt.image.BufferedImage shotImage = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = shotImage.createGraphics();
        g2d.setColor(new java.awt.Color(0xa004d4));
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();

        setImage(shotImage);

        setX(x + H_SPACE);
        setY(y - V_SPACE);
    }

    @Override
    public void act() {
        this.x += shotSpeed;
        // Remove shot when it goes off-screen
        if (this.x > BOARD_WIDTH || this.y < 0) {
            this.visible = false;
        }
    }

    public void act(int direction) {
        this.x += shotSpeed * direction;
        // Remove shot when it goes off-screen
        if (this.x > BOARD_WIDTH || this.y < 0) {
            this.visible = false;
        }
    }
}
