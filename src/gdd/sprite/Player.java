package gdd.sprite;

import static gdd.Global.*;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static final int START_X = 100;
    private static final int START_Y = GROUND - PLAYER_HEIGHT - 50; // Position player so bottom is at ground level
    private int frame = 0;
    private boolean isFiring = false;
    
    // HP System
    private int maxHP = 10;
    private int currentHP = 10;
    private boolean invulnerable = false;
    private int invulnerabilityTimer = 0;
    private static final int INVULNERABILITY_DURATION = 60; // 1 second at 60 FPS
    
    // Shield System
    private boolean shieldActive = false;
    private int shieldTimer = 0;

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

    // Player actions
    private static final String ACT_STANDING = "standing";
    private static final String ACT_RUNNING = "running";
    private static final String ACT_JUMPING = "jumping";
    private static final String ACT_SHOOTING = "shooting";
    private static final String ACT_JUMPING_SHOOTING = "jumping_shooting";
    private static final String ACT_RUNNING_SHOOTING = "running_shooting";
    private String action = ACT_RUNNING; // Default to running right
    private String defaultAction = ACT_RUNNING; // Default action when idle
    
    // Scene type for different player behaviors
    public static final String SCENE_SIDE_SCROLLING = "side_scrolling";
    public static final String SCENE_BOSS_FIGHT = "boss_fight";
    private String sceneType = SCENE_SIDE_SCROLLING; // Default to side-scrolling
    
    // Movement variables
    private int dx = 0; // Horizontal velocity

    private int clipNo = 0;
    // private final Rectangle[] clips = new Rectangle[] {
    //         new Rectangle(18, 20, 80, 90), // 0: stand still
    //         new Rectangle(110, 20, 80, 90), // 1: stand blink
    //         new Rectangle(294, 20, 90, 90), // 2: run 1
    //         new Rectangle(400, 20, 60, 90), // 3: run 2
    //         new Rectangle(470, 20, 80, 90), // 4: run 3
    //         new Rectangle(138, 230, 100, 110), // 5: jump 1, no firing
    //         new Rectangle(18, 230, 118, 110), // 6: jump 2, firing
    //         new Rectangle(128, 124, 124, 94), // 7: stand Shoot
    //         new Rectangle(248, 120, 118, 94), // 8: run shoot 1
    //         new Rectangle(372, 120, 118, 94), // 9: run shoot 2
    //         new Rectangle(486, 120, 118, 94), // 10: run shoot 3
    // };

        private final Rectangle[] clips = new Rectangle[] {
            new Rectangle(0, 0, 64, 64), // 0: stand still
            new Rectangle(64, 0, 64, 64), // 1: run 1
            new Rectangle(0, 64, 64, 64), // 2: run 2
            new Rectangle(64, 64, 64, 64), // 3: run 3
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
        return clips[clipNo].height * SCALE_FACTOR;
    }

    public int getFacing() {
        return facing;
    }

    @Override
    public int getWidth() {
        return clips[clipNo].width * SCALE_FACTOR;
    }

    @Override
    public Image getImage() {
        Rectangle bound = clips[clipNo];
        // TODO this can be cached.
        BufferedImage bImage = toBufferedImage(image);
        BufferedImage subImage = bImage.getSubimage(bound.x, bound.y, bound.width, bound.height);
        
        // Flip the image horizontally if facing left
        if (facing == DIR_LEFT) {
            BufferedImage flippedImage = new BufferedImage(subImage.getWidth(), subImage.getHeight(), subImage.getType());
            java.awt.Graphics2D g2d = flippedImage.createGraphics();
            g2d.drawImage(subImage, subImage.getWidth(), 0, -subImage.getWidth(), subImage.getHeight(), null);
            g2d.dispose();
            return flippedImage;
        }
        
        return subImage;
    }

    private void initPlayer() {
        var ii = new ImageIcon(IMG_PLAYER);
        setImage(ii.getImage());

        setX(START_X);
        setY(START_Y);
        
        // Player stays in same position - no horizontal movement needed
        clipNo = 1;
    }

    public void act() {
        System.out.printf("Player action=%s frame=%d facing=%d sceneType=%s\n", action, frame, facing, sceneType);

        frame++;
        
        // Handle horizontal movement (only in boss fight scenes)
        if (sceneType == SCENE_BOSS_FIGHT) {
            x += dx;
            
            // Keep player within screen bounds
            if (x < 0) {
                x = 0;
            } else if (x > BOARD_WIDTH - getWidth()) {
                x = BOARD_WIDTH - getWidth();
            }
        }
        
        // Handle invulnerability timer
        if (invulnerable && invulnerabilityTimer > 0) {
            invulnerabilityTimer--;
            if (invulnerabilityTimer <= 0) {
                invulnerable = false;
                System.out.println("Player is no longer invulnerable");
            }
        }
        
        // Handle shield timer
        if (shieldActive && shieldTimer > 0) {
            shieldTimer--;
            if (shieldTimer <= 0) {
                shieldActive = false;
                System.out.println("Shield protection has expired");
            }
        }

        switch (action) {

            // case ACT_STANDING:
            //     if (frame <= 10) {
            //         clipNo = 0; // Standing still
            //     } else {
            //         frame = 0; // Reset frame for standing animation
            //         clipNo = 1; // Blink animation
            //     }
            //     break;

            case ACT_RUNNING:
                if (frame <= 10) { 
                    clipNo = 2;
                } else if (frame <= 20) {
                    clipNo = 1;
                } else if (frame <= 30) {
                    clipNo = 2;
                } else if (frame <= 40) {
                    clipNo = 3;
                } else {
                    frame = 0;
                    clipNo = 1;
                }
                break;

            // case ACT_JUMPING:
            // case ACT_JUMPING_SHOOTING:
            //     // Apply gravity and vertical movement
            //     dy += GRAVITY;
            //     y += dy;
                
            //     // Apply horizontal movement during jumping
            //     x += dx;
                
            //     // Keep player within screen bounds
            //     if (x < 0) {
            //         x = 0;
            //     } else if (x > BOARD_WIDTH - getWidth()) {
            //         x = BOARD_WIDTH - getWidth();
            //     }
                
            //     // Check if player lands back on ground
            //     if (y >= START_Y) {
            //         y = START_Y;
            //         dy = 0;
            //         dx = 0; // Stop horizontal movement when landing
            //         isOnGround = true;
            //         // Return to default action when landing
            //         if (action == ACT_JUMPING) {
            //             action = defaultAction;
            //             frame = 0;
            //             clipNo = (defaultAction == ACT_STANDING) ? 0 : 2;
            //         } else { // ACT_JUMPING_SHOOTING
            //             action = (defaultAction == ACT_STANDING) ? ACT_SHOOTING : ACT_RUNNING_SHOOTING;
            //             frame = 0;
            //             clipNo = (defaultAction == ACT_STANDING) ? 7 : 8;
            //         }
            //     } else {
            //         // Still in air - use jumping animation
            //         if (action == ACT_JUMPING) {
            //             clipNo = (dy < 0) ? 5 : 6; // Rising or falling
            //         } else { // ACT_JUMPING_SHOOTING
            //             clipNo = 6; // Jump shooting animation
            //         }
            //     }
            //     break;

            // case ACT_SHOOTING:
            //     if (frame <= 10) {
            //         clipNo = 7;
            //     } else {
            //         frame = 0;
            //         clipNo = 7;
            //     }
            //     break;

            // case ACT_RUNNING_SHOOTING:
            //     if (frame <= 10) {
            //         clipNo = 8;
            //     } else if (frame <= 20) {
            //         clipNo = 9;
            //     } else if (frame <= 30) {
            //         clipNo = 10;
            //     } else {
            //         frame = 0;
            //         clipNo = 8;
            //     }
            //     break;

            // default:
            //     // Default to running
            //     action = ACT_RUNNING;
            //     clipNo = 2;
            //     break;
        }
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (action) {
            case ACT_STANDING:
                if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_SPACE) && isOnGround) {
                    action = ACT_JUMPING;
                    frame = 0;
                    clipNo = 5;
                    dy = JUMP_STRENGTH; // Start jump with upward velocity
                    isOnGround = false;
                } else if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow shooting and left/right movement
                    if (key == KeyEvent.VK_ENTER) {
                        action = ACT_SHOOTING;
                        frame = 0;
                        isFiring = true;
                        clipNo = 7;
                    } else if (key == KeyEvent.VK_LEFT) {
                        dx = -3; // Move left
                        facing = DIR_LEFT;
                        action = ACT_RUNNING;
                        frame = 0;
                        clipNo = 1;
                    } else if (key == KeyEvent.VK_RIGHT) {
                        dx = 3; // Move right
                        facing = DIR_RIGHT;
                        action = ACT_RUNNING;
                        frame = 0;
                        clipNo = 1;
                    }
                }
                // Side-scrolling: No left/right movement or shooting - only jumping allowed
                break;
                
            case ACT_RUNNING:
                if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_SPACE) && isOnGround) {
                    action = ACT_JUMPING;
                    frame = 0;
                    clipNo = 5;
                    dy = JUMP_STRENGTH; // Start jump with upward velocity
                    isOnGround = false;
                } else if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow shooting and left/right movement
                    if (key == KeyEvent.VK_ENTER) {
                        action = ACT_RUNNING_SHOOTING;
                        frame = 0;
                        isFiring = true;
                        clipNo = 8;
                    } else if (key == KeyEvent.VK_LEFT) {
                        dx = -3; // Move left
                        facing = DIR_LEFT;
                    } else if (key == KeyEvent.VK_RIGHT) {
                        dx = 3; // Move right
                        facing = DIR_RIGHT;
                    }
                }
                // Side-scrolling: No left/right movement or shooting - only jumping allowed
                break;

            case ACT_JUMPING:
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow shooting and left/right movement while jumping
                    if (key == KeyEvent.VK_ENTER) {
                        action = ACT_JUMPING_SHOOTING;
                        frame = 0;
                        isFiring = true;
                        clipNo = 6;
                    } else if (key == KeyEvent.VK_LEFT) {
                        dx = -3; // Move left while jumping
                    } else if (key == KeyEvent.VK_RIGHT) {
                        dx = 3; // Move right while jumping
                    }
                }
                // Side-scrolling: No left/right movement or shooting while jumping
                break;

            case ACT_SHOOTING:
                // Player is already shooting, handle other keys
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow jumping while shooting
                    if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_SPACE) && isOnGround) {
                        action = ACT_JUMPING_SHOOTING;
                        frame = 0;
                        clipNo = 6;
                        dy = JUMP_STRENGTH; // Start jump with upward velocity
                        isOnGround = false;
                    }
                }
                // Side-scrolling: No actions while shooting (shooting not allowed in side-scrolling)
                break;

            case ACT_JUMPING_SHOOTING:
                // Player is jumping and shooting
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow left/right movement while jumping and shooting
                    if (key == KeyEvent.VK_LEFT) {
                        dx = -3; // Move left while jumping
                    } else if (key == KeyEvent.VK_RIGHT) {
                        dx = 3; // Move right while jumping
                    }
                }
                // Side-scrolling: No left/right movement while jumping and shooting
                break;

            case ACT_RUNNING_SHOOTING:
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow jumping while running and shooting
                    if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_SPACE) && isOnGround) {
                        action = ACT_JUMPING_SHOOTING;
                        frame = 0;
                        clipNo = 6;
                        dy = JUMP_STRENGTH; // Start jump with upward velocity
                        isOnGround = false;
                    }
                }
                // Side-scrolling: No actions while running and shooting (shooting not allowed in side-scrolling)
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        switch (action) {
            case ACT_STANDING:
                // Player stays in standing mode until movement keys are pressed
                break;
                
            case ACT_JUMPING:
                // Don't change action until player lands (handled in act() method)
                break;

            case ACT_SHOOTING:
                if (key == KeyEvent.VK_ENTER) {
                    action = defaultAction; // Return to default action (standing or running)
                    isFiring = false;
                    frame = 0;
                    clipNo = (defaultAction == ACT_STANDING) ? 0 : 2;
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
                    action = defaultAction; // Return to default action
                    frame = 0;
                    clipNo = (defaultAction == ACT_STANDING) ? 0 : 2;
                }
                break;

            case ACT_RUNNING:
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow stopping movement when keys are released
                    if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
                        dx = 0; // Stop horizontal movement
                        action = ACT_STANDING; // Switch to standing mode
                        frame = 0;
                        clipNo = 0; // Standing still
                    }
                }
                // Side-scrolling: Player always runs, no key release handling for movement
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
        // Check if shield is active - shield blocks all damage
        if (shieldActive) {
            System.out.println("Shield blocked " + damage + " damage!");
            return;
        }
        
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
        
        // Reset shield state
        shieldActive = false;
        shieldTimer = 0;
        
        // Reset jumping state
        dy = 0;
        dx = 0;
        isOnGround = true;
        y = START_Y;
        x = START_X; // Reset to starting position
        action = defaultAction; // Use default action instead of always running
        frame = 0;
        clipNo = (defaultAction == ACT_STANDING) ? 0 : 2;
    }
    
    // Shield System Methods
    public void activateShield(int duration) {
        shieldActive = true;
        shieldTimer = duration;
        System.out.println("Shield activated for " + duration + " frames");
    }
    
    public boolean isShieldActive() {
        return shieldActive;
    }
    
    public int getShieldTimer() {
        return shieldTimer;
    }
    
    // Method to set player to standing mode (for boss fight)
    public void setToStandingMode() {
        action = ACT_STANDING;
        defaultAction = ACT_STANDING;
        sceneType = SCENE_BOSS_FIGHT; // Set scene type to boss fight
        frame = 0;
        clipNo = 0; // Standing still clip
        dy = 0;
        dx = 0;
        isOnGround = true;
        isFiring = false;
        System.out.println("Player set to standing mode (boss fight)");
    }
    
    // Method to set player to running mode (for side-scrolling)
    public void setToRunningMode() {
        action = ACT_RUNNING;
        defaultAction = ACT_RUNNING;
        sceneType = SCENE_SIDE_SCROLLING; // Set scene type to side-scrolling
        frame = 0;
        clipNo = 1; // Running clip
        dy = 0;
        dx = 0;
        isOnGround = true;
        isFiring = false;
        System.out.println("Player set to running mode (side-scrolling)");
    }
}
