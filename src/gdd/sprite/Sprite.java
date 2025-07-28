package gdd.sprite;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

abstract public class Sprite {

    protected boolean visible;
    protected Image image;
    protected boolean dying;
    protected int visibleFrames = 10;

    protected int x;
    protected int y;
    protected int dx;
    protected int dy; // Add dy for vertical movement

    public Sprite() {
        visible = true;
    }

    abstract public void act();

    public boolean collidesWith(Sprite other) {
        if (other == null || !this.isVisible() || !other.isVisible()) {
            return false;
        }

        // Shrink both sprites' bounds by 30% (15% from each side)
        double shrinkFactor = 0.3;
        double thisShrinkW = this.getWidth() * shrinkFactor;
        double thisShrinkH = this.getHeight() * shrinkFactor;
        double otherShrinkW = other.getWidth() * shrinkFactor;
        double otherShrinkH = other.getHeight() * shrinkFactor;

        double thisX = this.getX() + thisShrinkW / 2;
        double thisY = this.getY() + thisShrinkH / 2;
        double thisW = this.getWidth() * (1 - shrinkFactor);
        double thisH = this.getHeight() * (1 - shrinkFactor);

        double otherX = other.getX() + otherShrinkW / 2;
        double otherY = other.getY() + otherShrinkH / 2;
        double otherW = other.getWidth() * (1 - shrinkFactor);
        double otherH = other.getHeight() * (1 - shrinkFactor);

        return thisX < otherX + otherW
                && thisX + thisW > otherX
                && thisY < otherY + otherH
                && thisY + thisH > otherY;
    }

    public void die() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisibleFrames(int frames) {
        this.visibleFrames = frames;
    }

    public void visibleCountDown() {
        if (visibleFrames > 0) {
            visibleFrames--;
        } else {
            visible = false;
        }
    }

    protected void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    // Add width and height methods for better collision detection
    public int getWidth() {
        return image != null ? image.getWidth(null) : 0;
    }

    public int getHeight() {
        return image != null ? image.getHeight(null) : 0;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public void setDying(boolean dying) {
        this.dying = dying;
    }

    public boolean isDying() {
        return this.dying;
    }

    /**
     * Converts a given Image into a BufferedImage
     * Essential for sprite sheet animation and clipping
     */
    public BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(
            img.getWidth(null), 
            img.getHeight(null), 
            BufferedImage.TYPE_INT_ARGB
        );

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }
}

