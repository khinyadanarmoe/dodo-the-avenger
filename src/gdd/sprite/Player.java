package gdd.sprite;

import static gdd.Global.*;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static final int START_X = 100;
    private static final int START_Y = GROUND - PLAYER_HEIGHT; // Position player so bottom is at ground level
    private int frame = 0;
    private boolean isFiring = false;
    
    // HP System
    private int maxHP = 10;
    private int currentHP = 10;
    private boolean invulnerable = false;
    private int invulnerabilityTimer = 0;
    private static final int INVULNERABILITY_DURATION = 60; // 1 second at 60 FPS

    // Jumping physics
    private int dy = 0; // Vertical velocity
    // private int dx = 0; // Horizontal velocity during jumping
    private boolean isOnGround = true;
    private static final int JUMP_STRENGTH = -20; // Negative for upward movement
    private static final int GRAVITY = 1; // Gravity pulls player down
    // private static final int HORIZONTAL_SPEED = 2; // Horizontal movement speed during jump

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
        
        // Handle invulnerability timer
        if (invulnerable && invulnerabilityTimer > 0) {
            invulnerabilityTimer--;
            if (invulnerabilityTimer <= 0) {
                invulnerable = false;
                System.out.println("Player is no longer invulnerable");
            }
        }

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
                    frame = 0;
                    clipNo = 2;
                }
                break;

            case ACT_JUMPING:
            case ACT_JUMPING_SHOOTING:
                // Apply gravity and vertical movement
                dy += GRAVITY;
                y += dy;
                
                // Apply horizontal movement during jumping
                x += dx;
                
                // Keep player within screen bounds
                if (x < 0) {
                    x = 0;
                } else if (x > BOARD_WIDTH - getWidth()) {
                    x = BOARD_WIDTH - getWidth();
                }
                
                // Check if player lands back on ground
                if (y >= START_Y) {
                    y = START_Y;
                    dy = 0;
                    dx = 0; // Stop horizontal movement when landing
                    isOnGround = true;
                    // Return to running when landing
                    if (action == ACT_JUMPING) {
                        action = ACT_RUNNING;
                        frame = 0;
                        clipNo = 2;
                    } else { // ACT_JUMPING_SHOOTING
                        action = ACT_RUNNING_SHOOTING;
                        frame = 0;
                        clipNo = 8;
                    }
                } else {
                    // Still in air - use jumping animation
                    if (action == ACT_JUMPING) {
                        clipNo = (dy < 0) ? 5 : 6; // Rising or falling
                    } else { // ACT_JUMPING_SHOOTING
                        clipNo = 6; // Jump shooting animation
                    }
                }
                break;

            case ACT_SHOOTING:
                if (frame <= 10) {
                    clipNo = 7;
                } else {
                    frame = 0;
                    clipNo = 7;
                }
                break;

            case ACT_RUNNING_SHOOTING:
                if (frame <= 10) {
                    clipNo = 8;
                } else if (frame <= 20) {
                    clipNo = 9;
                } else if (frame <= 30) {
                    clipNo = 10;
                } else {
                    frame = 0;
                    clipNo = 8;
                }
                break;

            default:
                // Default to running
                action = ACT_RUNNING;
                clipNo = 2;
                break;
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (action) {
            case ACT_RUNNING:
                if (key == KeyEvent.VK_UP && isOnGround) {
                    action = ACT_JUMPING;
                    frame = 0;
                    clipNo = 5;
                    dy = JUMP_STRENGTH; // Start jump with upward velocity
                    // dx = HORIZONTAL_SPEED; // Automatically move forward when jumping
                    isOnGround = false;
                } else if (key == KeyEvent.VK_ENTER) {
                    action = ACT_RUNNING_SHOOTING;
                    frame = 0;
                    isFiring = true;
                    clipNo = 8;
                }
                break;

            case ACT_JUMPING:
                if (key == KeyEvent.VK_ENTER) {
                    action = ACT_JUMPING_SHOOTING;
                    frame = 0;
                    isFiring = true;
                    clipNo = 6;
                } else if (key == KeyEvent.VK_LEFT) {
                    // dx = -HORIZONTAL_SPEED; // Move left while jumping
                } else if (key == KeyEvent.VK_RIGHT) {
                    // dx = HORIZONTAL_SPEED; // Move right while jumping
                }
                break;

            case ACT_SHOOTING:
                // Player is already shooting, handle other keys
                if (key == KeyEvent.VK_UP && isOnGround) {
                    action = ACT_JUMPING_SHOOTING;
                    frame = 0;
                    clipNo = 6;
                    dy = JUMP_STRENGTH; // Start jump with upward velocity
                    // dx = HORIZONTAL_SPEED; // Automatically move forward when jumping
                    isOnGround = false;
                }
                break;

            case ACT_JUMPING_SHOOTING:
                // Player is jumping and shooting - can still move horizontally
                if (key == KeyEvent.VK_LEFT) {
                    // dx = -HORIZONTAL_SPEED; // Move left while jumping
                } else if (key == KeyEvent.VK_RIGHT) {
                    // dx = HORIZONTAL_SPEED; // Move right while jumping
                }
                break;

            case ACT_RUNNING_SHOOTING:
                if (key == KeyEvent.VK_UP && isOnGround) {
                    action = ACT_JUMPING_SHOOTING;
                    frame = 0;
                    clipNo = 6;
                    dy = JUMP_STRENGTH; // Start jump with upward velocity
                    // dx = HORIZONTAL_SPEED; // Automatically move forward when jumping
                    isOnGround = false;
                }
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        switch (action) {
            case ACT_JUMPING:
                // Don't change action until player lands (handled in act() method)
                break;

            case ACT_SHOOTING:
                if (key == KeyEvent.VK_ENTER) {
                    action = ACT_RUNNING;
                    isFiring = false;
                    frame = 0;
                    clipNo = 2;
                }
                break;

            case ACT_JUMPING_SHOOTING:
                if (key == KeyEvent.VK_ENTER) {
                    action = ACT_JUMPING;
                    isFiring = false;
                    frame = 0;
                    clipNo = 5;
                }
                // Don't handle UP key here - let player land naturally
                break;

            case ACT_RUNNING_SHOOTING:
                if (key == KeyEvent.VK_ENTER) {
                    isFiring = false;
                    action = ACT_RUNNING;
                    frame = 0;
                    clipNo = 2;
                }
                break;
        }
    }

    // HP System Methods
    public int getCurrentHP() {
        return currentHP;
    }
    
    public int getMaxHP() {
        return maxHP;
    }
    
    public void takeDamage(int damage) {
        if (!invulnerable && currentHP > 0) {
            currentHP -= damage;
            invulnerable = true;
            invulnerabilityTimer = INVULNERABILITY_DURATION;
            
            System.out.println("Player took " + damage + " damage. HP: " + currentHP + "/" + maxHP);
            
            if (currentHP <= 0) {
                currentHP = 0;
                setDying(true);
                System.out.println("Player died!");
            }
        }
    }
    
    public void heal(int amount) {
        currentHP = Math.min(currentHP + amount, maxHP);
        System.out.println("Player healed " + amount + " HP. HP: " + currentHP + "/" + maxHP);
    }
    
    public boolean isInvulnerable() {
        return invulnerable;
    }
    
    public void resetHP() {
        currentHP = maxHP;
        invulnerable = false;
        invulnerabilityTimer = 0;
        
        // Reset jumping state
        dy = 0;
        dx = 0;
        isOnGround = true;
        y = START_Y;
        x = START_X; // Reset to starting position
        action = ACT_RUNNING;
        frame = 0;
        clipNo = 2;
    }
}
