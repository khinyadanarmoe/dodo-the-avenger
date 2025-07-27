package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;

public class Enemy extends Sprite {

    private Bomb bomb;

    public Enemy(int x, int y) {

        initEnemy(x, y);
    }

    private void initEnemy(int x, int y) {

        this.x = x;
        this.y = y;

        bomb = new Bomb(x, y);

        var ii = new ImageIcon(IMG_ENEMY);

        // Scale the image to use the global scaling factor
        var scaledImage = ii.getImage().getScaledInstance(ii.getIconWidth() * SCALE_FACTOR,
                ii.getIconHeight() * SCALE_FACTOR,
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
    }

    @Override
    public void act() {
        // Default implementation of the abstract method
    }

    public void act(int direction) {
        this.x += direction;

        // Remove when off-screen
        if (this.x < -50) {
            this.visible = false;
        }
    }
 
    public Bomb getBomb() {
    return bomb;
}


    public class Bomb extends Sprite {

        private boolean destroyed;

        public Bomb(int x, int y) {

            initBomb(x, y);
        }

        private void initBomb(int x, int y) {

            setDestroyed(true);

            this.x = x;
            this.y = y;

            var bombImg = "src/images/bomb.png";
            var ii = new ImageIcon(bombImg);
            // Scale the bomb image to 3x its original size
            var scaledImage = ii.getImage().getScaledInstance(
                ii.getIconWidth() * 3,
                ii.getIconHeight() * 3,
                java.awt.Image.SCALE_SMOOTH
            );
            setImage(scaledImage);
        }

        public void setDestroyed(boolean destroyed) {

            this.destroyed = destroyed;
        }

        public boolean isDestroyed() {

            return destroyed;
        }

        @Override
        public void act() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'act'");
        }
    }

}
