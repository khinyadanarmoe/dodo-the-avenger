package gdd.sprite;
import static gdd.Global.*;

public class pharohHead extends Enemy {
    

    public pharohHead(int x, int y ) {
        super(x, y);
        // Initialize the pharoh head specific properties
        initPharohHead(x, y);
    }

    private void initPharohHead(int x, int y) {
        this.x = x;
        this.y = y;

        // Load the full image
        var fullImg = new javax.swing.ImageIcon(IMG_BOSS).getImage();
        // Calculate the top middle rectangle (64x64)
        int imgWidth = fullImg.getWidth(null);
        int xClip = (imgWidth - 64) / 2;
        int yClip = 0;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics g = img.getGraphics();
        g.drawImage(fullImg, 0, 0, 64, 64, xClip, yClip, xClip + 64, yClip + 64, null);
        g.dispose();

        setImage(img);

    }

        public void act(int direction) {
        this.x += direction;

        // Remove when off-screen
        if (this.x < -50 || this.x > BOARD_WIDTH + 50) {
            this.visible = false;
        }
    }
}
