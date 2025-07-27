package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Shot extends Sprite {

    private static final int H_SPACE = 20;
    private static final int V_SPACE = 1;

    public Shot() {
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'act'");
    }
}
