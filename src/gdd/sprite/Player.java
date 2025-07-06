package gdd.sprite;

import static gdd.Global.*;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static final int START_X = 100;
    private static final int START_Y = GROUND;
    private int frame = 0;
    private boolean isFiring = false;

    public static final int DIR_LEFT = 0;
    public static final int DIR_RIGHT = 1;
    private int facing = DIR_RIGHT; // Always facing right

    private static final String ACT_RUNNING = "running";
    private static final String ACT_JUMPING = "jumping";
    private static final String ACT_SHOOTING = "shooting";
    private static final String ACT_JUMPING_SHOOTING = "jumping_shooting";
    private static final String ACT_RUNNING_SHOOTING = "running_shooting";
    private String action = ACT_RUNNING; // Default to running right

    private int clipNo = 0;
    private final Rectangle[] clips = new Rectangle[] {
            new Rectangle(18, 20, 80, 90), // 0: stand still
            new Rectangle(110, 20, 80, 90), // 1: stand blink
            new Rectangle(294, 20, 90, 90), // 2: run 1
            new Rectangle(400, 20, 60, 90), // 3: run 2
            new Rectangle(470, 20, 80, 90), // 4: run 3
            new Rectangle(138, 230, 100, 110), // 5: jump 1, no firing
            new Rectangle(18, 230, 118, 110), // 6: jump 2, firing
            new Rectangle(128, 124, 124, 94), // 7: stand Shoot
            new Rectangle(248, 120, 118, 94), // 8: run shoot 1
            new Rectangle(372, 120, 118, 94), // 9: run shoot 2
            new Rectangle(486, 120, 118, 94), // 10: run shoot 3
    };

    public Player() {
        initPlayer();
    }

    public int getFrame() {
        return frame;
    }

    public boolean isFiring() {
        return isFiring;
    }

    public int getSpeed() {
        return 1; // Return constant speed for compatibility
    }

    public int setSpeed(int speed) {
        // Speed changes don't affect movement in side-scrolling game
        // Return the requested speed for compatibility with powerups
        return speed;
    }

    @Override
    public int getHeight() {
        return clips[clipNo].height;
    }

    public int getFacing() {
        return facing;
    }

    @Override
    public int getWidth() {
        return clips[clipNo].width;
    }

    @Override
    public Image getImage() {
        Rectangle bound = clips[clipNo];
        // TODO this can be cached.
        BufferedImage bImage = toBufferedImage(image);
        return bImage.getSubimage(bound.x, bound.y, bound.width, bound.height);
    }

    private void initPlayer() {
        var ii = new ImageIcon(IMG_PLAYER);
        setImage(ii.getImage());

        setX(START_X);
        setY(START_Y);
        
        // Player stays in same position - no horizontal movement needed
        clipNo = 2; // Start with running animation
    }

    public void act() {
        System.out.printf("Player action=%s frame=%d facing=%d\n", action, frame, facing);

        frame++;

        switch (action) {
            case ACT_RUNNING:
                if (frame <= 10) {
                    clipNo = 3;
                } else if (frame <= 20) {
                    clipNo = 2;
                } else if (frame <= 30) {
                    clipNo = 3;
                } else if (frame <= 40) {
                    clipNo = 4;
                } else {
                    clipNo = 3;
                    frame = 0;
                }
                break;

            case ACT_RUNNING_SHOOTING:
                if (frame <= 10) {
                    clipNo = 9;
                } else if (frame <= 20) {
                    clipNo = 8;
                } else if (frame <= 30) {
                    clipNo = 9;
                } else if (frame <= 40) {
                    clipNo = 10;
                } else {
                    clipNo = 9;
                    frame = 0;
                }

                break;
            case ACT_JUMPING:
            case ACT_JUMPING_SHOOTING:
                clipNo = (action.equals(ACT_JUMPING_SHOOTING)) ? 6 : 5;
                y += dy;

                if (frame > 30) {
                    dy = 10;
                }

                if (y + dy >= GROUND) {
                    y = GROUND;
                    // Always return to running when landing
                    action = ACT_RUNNING;
                    frame = 0;
                    clipNo = 2;
                    isFiring = false;
                }

                break;

        }

        // Player stays in same horizontal position - no dx movement needed

        // Keep player within screen bounds (though they shouldn't move horizontally)
        if (x <= 2) {
            x = 2;
        }

        if (x >= BOARD_WIDTH - getWidth()) {
            x = BOARD_WIDTH - getWidth();
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (action) {
            case ACT_RUNNING:
                if (key == KeyEvent.VK_SPACE) {
                    frame = 0;
                    action = ACT_JUMPING;
                    dy = -10;
                }

                if (key == KeyEvent.VK_ENTER) {
                    frame = 0;
                    isFiring = true;
                    action = ACT_RUNNING_SHOOTING;
                }

                break;

            case ACT_JUMPING:
                if (key == KeyEvent.VK_ENTER) {
                    isFiring = true;
                    action = ACT_JUMPING_SHOOTING;
                    clipNo = 6; // jump shoot
                }
                break;
                
            case ACT_SHOOTING:
                if (key == KeyEvent.VK_SPACE) {
                    frame = 0;
                    action = ACT_JUMPING;
                    dy = -10;
                }
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        switch (action) {
            case ACT_JUMPING:
                // No horizontal movement changes needed
                break;
                
            case ACT_RUNNING:
                if (key == KeyEvent.VK_ENTER) {
                    isFiring = false;
                    // Stay running - no movement changes needed
                }
                break;

            case ACT_SHOOTING:
                if (key == KeyEvent.VK_ENTER) {
                    isFiring = false;
                    action = ACT_RUNNING; // Return to running
                    frame = 0;
                    clipNo = 2;
                }
                break;

            case ACT_RUNNING_SHOOTING:
                if (key == KeyEvent.VK_ENTER) {
                    isFiring = false;
                    // Stay running
                    frame = 0;
                    clipNo = 2;
                }
                break;

            case ACT_JUMPING_SHOOTING:
                if (key == KeyEvent.VK_ENTER) {
                    isFiring = false;
                    action = ACT_JUMPING;
                    clipNo = 5; // Return to normal jump animation
                }
                break;
        }
    }
}
