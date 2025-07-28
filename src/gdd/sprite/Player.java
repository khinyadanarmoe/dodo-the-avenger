package gdd.sprite;

import static gdd.Global.*;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class Player extends Sprite {

    private static int ground = GROUND;

    private static final int START_X = 100;
    private static final int START_Y = ground - PLAYER_HEIGHT - 50; // Position player so bottom is at ground level
    private int frame = 0;
    private boolean isFiring = false;
    private Shot shot;
    private int shotCooldown = 0; // Frames until next shot allowed
    private static final int SHOT_COOLDOWN_FRAMES = 15; // 15 frames = 0.25s at 60 FPS

    // HP System
    private int maxHP = 20;
    private int currentHP = 20;
    private boolean invulnerable = false;
    private int invulnerabilityTimer = 0;
    private static final int INVULNERABILITY_DURATION = 60; // 1 second at 60 FPS

    // Shield System
    private boolean shieldActive = false;
    private int shieldTimer = 0;

    // Jumping physics
    private float dy = 0; // Vertical velocity
    private int dx = 0;
    private boolean isOnGround = true;
    private static final int JUMP_STRENGTH = -15; // Negative for upward movement
    private static final float GRAVITY = 0.5f; // Gravity pulls player down
    private static final int PLAYER_SPEED = 2; // Horizontal movement dx during jump

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
    private static final String ACT_FLYING = "flying";
    private static final String ACT_FLYING_SHOOTING = "flying_shooting";
    private String action = ACT_RUNNING; // Default to running right
    private String defaultAction = ACT_RUNNING; // Default action when idle

    // Scene type for different player behaviors
    public static final String SCENE_SIDE_SCROLLING = "side_scrolling";
    public static final String SCENE_BOSS_FIGHT = "boss_fight";
    private String sceneType = SCENE_SIDE_SCROLLING; // Default to side-scrolling

    // Movement variables
    public static final int MAX_PLAYER_SPEED = 8; // Maximum speed of the player
    private int speed = PLAYER_SPEED; // Horizontal velocity
    private float jumpStrength = JUMP_STRENGTH; // Jump strength for compatibility

    private int clipNo = 0;

    private final Rectangle[] clips = new Rectangle[] {
            /*
             * // Row 0
             * new Rectangle( 0, 0, 64, 64), // 0 standing
             * new Rectangle( 64, 0, 64, 64), // 1 running 1
             * new Rectangle( 128, 0, 64, 64), // 2 flying 1
             * new Rectangle(192, 0, 64, 64), // 3 flying 3
             * // Row 1
             * new Rectangle( 0, 64, 64, 64), // 4 running 2
             * new Rectangle( 64, 64, 64, 64), // 5 running 3
             * new Rectangle( 128, 64, 64, 64), // 6 flying 2
             * new Rectangle( 192, 64, 64, 64), // 7 fly-shooting 1
             * // Row 2
             * new Rectangle( 0, 128, 64, 64), // 8 run-shooting 1
             * new Rectangle( 64, 128, 64, 64), // 9 run-shooting 2
             * new Rectangle( 128, 128, 64, 64), // 10 fly-shooting 3
             * new Rectangle( 192, 128, 64, 64), // 11 fly-shooting 2
             * // Row 3
             * new Rectangle( 0, 192, 64, 64), // 12 run-shooting 3
             * new Rectangle( 64, 192, 64, 64), // 13 stand-shooting
             * new Rectangle( 128, 192, 64, 64), // 14 jumping
             * new Rectangle( 192, 192, 64, 64) // 15 jump-shooting
             */

            // standing
            new Rectangle(0, 0, 64, 64), // 0 standing
            // running
            new Rectangle(64, 0, 64, 64), // 1 running 1
            new Rectangle(0, 64, 64, 64), // 2 running 2
            new Rectangle(64, 64, 64, 64), // 3 running 3
            // jumping
            new Rectangle(128, 192, 64, 64), // 4 jumping
            // flying
            new Rectangle(128, 0, 64, 64), // 5 flying 1
            new Rectangle(128, 64, 64, 64), // 6 flying 2
            new Rectangle(192, 0, 64, 64), // 7 flying 3
            // run-shooting
            new Rectangle(0, 128, 64, 64), // 8 run-shooting 1
            new Rectangle(64, 128, 64, 64), // 9 run-shooting 2
            new Rectangle(0, 192, 64, 64), // 10 run-shooting 3
            // fly-shooting
            new Rectangle(192, 64, 64, 64), // 11 fly-shooting 1
            new Rectangle(192, 128, 64, 64), // 12 fly-shooting 2
            new Rectangle(128, 128, 64, 64), // 13 fly-shooting 3
            // stand-shooting
            new Rectangle(64, 192, 64, 64), // 14 stand-shooting
            // jumping-shooting
            new Rectangle(192, 192, 64, 64) // 15 jump-shooting

    };

    public Player() {
        initPlayer();
    }

    public String getSceneType() {
        return sceneType;
    }

    public int getFrame() {
        return frame;
    }

    public boolean isFiring() {
        return isFiring;
    }

    public boolean canFire() {
        return shotCooldown == 0; // Can fire if cooldown is over
    }

    public int getSpeed() {
        return this.speed; // Return constant speed for compatibility
    }

    public void setSpeed(int speed) {
        this.speed = speed; // Set horizontal speed
        System.out.println("Player speed set to " + speed);
    }

    public float getJumpStrength() {
        return jumpStrength;
    }

    public void setJumpStrength(float jumpStrength) {
        this.jumpStrength = jumpStrength;
    }

    public Shot getShot() {
        return shot;
    }

    public void setShotCooldown() {
        shotCooldown = SHOT_COOLDOWN_FRAMES;
    }

    public int getDirection() {
        return facing; // Return current direction
    }

    public static int getGround(String sceneType) {
    if ("boss_fight".equals(sceneType)) {
        return ground + 100;
    }
    return ground;
}

    

    @Override
    public int getWidth() {
        float scale = SCALE_FACTOR;
        if (sceneType.equals(SCENE_BOSS_FIGHT)) {
            scale = (float) (SCALE_FACTOR / 1.5); // 2x smaller
            if (scale < 1) scale = 1; // Prevent zero or negative scale
        }
        return (int) (clips[clipNo].width * scale);
    }

    @Override
    public int getHeight() {
        float scale = SCALE_FACTOR;
        if (sceneType.equals(SCENE_BOSS_FIGHT)) {
            scale = (float) (SCALE_FACTOR / 1.5); // 2x smaller
            if (scale < 1) scale = 1;
        }
        return (int) (clips[clipNo].height * scale);
    }

    @Override
    public Image getImage() {
        Rectangle bound = clips[clipNo];
  
        BufferedImage bImage = toBufferedImage(image);
        BufferedImage subImage = bImage.getSubimage(bound.x, bound.y, bound.width, bound.height);

        // Flip the image horizontally if facing left
        if (facing == DIR_LEFT) {
            BufferedImage flippedImage = new BufferedImage(subImage.getWidth(),
                    subImage.getHeight(), subImage.getType());
            java.awt.Graphics2D g2d = flippedImage.createGraphics();
            g2d.drawImage(subImage, subImage.getWidth(), 0, -subImage.getWidth(),
                    subImage.getHeight(), null);
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

    }

    public void act() {
        System.out.printf("Player action=%s frame=%d facing=%d sceneType=%s\n", action, frame, facing, sceneType);

        frame++;

        // Handle horizontal movement (only in boss fight scenes)
        // if (sceneType == SCENE_BOSS_FIGHT) {
        //     x += dx;

        //     // Keep player within screen bounds
        //     if (x < 0) {
        //         x = 0;
        //     } else if (x > BOARD_WIDTH - getWidth()) {
        //         x = BOARD_WIDTH - getWidth();
        //     }
        // }

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

        if (shotCooldown > 0) {
            shotCooldown--;
        }

        // If player is not at START_X and is on ground, move back incrementally by
        // speed
        if (x != START_X && isOnGround && sceneType == SCENE_SIDE_SCROLLING) {
            if (x > START_X) {
                x -= Math.min(3, x - START_X);
                if (x < START_X)
                    x = START_X;
            } else {
                x += Math.min(3, START_X - x);
                if (x > START_X)
                    x = START_X;
            }
        }

        switch (action) {

            case ACT_STANDING:
                // Player is standing still
                clipNo = 0; // Standing animation
                dx = 0; // No horizontal movement
                break;

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
                }

                // Player is running
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Allow horizontal movement while running in boss fight
                    if (facing == DIR_LEFT) {
                        x -= dx; // Move left
                    } else {
                        x += dx; // Move right
                    }
                }
                // Keep player within screen bounds
                if (x < 0) {
                    x = 0;
                } else if (x > BOARD_WIDTH - getWidth()) {
                    x = BOARD_WIDTH - getWidth();
                }

                break;

            case ACT_JUMPING:
                // Apply gravity and vertical movement
                dy += GRAVITY;
                y += dy;

                if (facing == DIR_RIGHT) {
                    x += dx;
                } else {
                    x -= dx;
                }
                

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
                    isOnGround = true;

                    // Return to default action when landing
                    action = defaultAction;
                    frame = 0;
                    clipNo = (defaultAction == ACT_STANDING) ? 0 : 2;
                } else {
                    // Still in air - use jumping animation
                    clipNo = 4;

                }
                break;

            case ACT_FLYING:

                // Apply vertical movement
                y += dy;

                // Player is running
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Allow horizontal movement while running in boss fight
                    if (facing == DIR_LEFT) {
                        x -= dx; // Move left
                    } else {
                        x += dx; // Move right
                    }
                }

                // Keep player within screen bounds
                if (x < 0) {
                    x = 0;
                } else if (x > BOARD_WIDTH - getWidth()) {
                    x = BOARD_WIDTH - getWidth();
                } else if (y < 0) {
                    y = 0; // Prevent flying above screen
                } else if (y > getGround(sceneType) - getHeight()) {
                    y = getGround(sceneType) - getHeight(); // Prevent flying below ground
                }

                // Use flying animation
                if (frame <= 10) {
                    clipNo = 6;
                } else if (frame <= 20) {
                    clipNo = 5;
                } else if (frame <= 30) {
                    clipNo = 6;
                } else if (frame <= 40) {
                    clipNo = 7;
                } else {
                    frame = 0;
                }
                break;

            case ACT_FLYING_SHOOTING:
                // Apply vertical movement
                y += dy;

                // Player is running
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Allow horizontal movement while running in boss fight
                    if (facing == DIR_LEFT) {
                        x -= dx; // Move left
                    } else {
                        x += dx; // Move right
                    }
                }

                // Keep player within screen bounds
                if (x < 0) {
                    x = 0;
                } else if (x > BOARD_WIDTH - getWidth()) {
                    x = BOARD_WIDTH - getWidth();
                } else if (y < 0) {
                    y = 0; // Prevent flying above screen
                } else if (y > getGround(sceneType) - getHeight()) {
                    y = getGround(sceneType) - getHeight(); // Prevent flying below ground
                }

                if (frame <= 10) {
                    clipNo = 12;
                } else if (frame <= 20) {
                    clipNo = 11;
                } else if (frame <= 30) {
                    clipNo = 12;
                } else if (frame <= 40) {
                    clipNo = 13;
                } else {
                    frame = 0;
                }
                break;

            case ACT_JUMPING_SHOOTING:
                // Apply gravity and vertical movement
                dy += GRAVITY;
                y += dy;

                // Apply horizontal movement during jumping
                x += speed;

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
                    isOnGround = true;
                    // Return to default action when landing
                    action = (defaultAction == ACT_STANDING) ? ACT_SHOOTING : ACT_RUNNING_SHOOTING;
                    frame = 0;
                    clipNo = (defaultAction == ACT_STANDING) ? 14 : 9;
                } else {
                    // Still in air - use jumping shooting animation
                    clipNo = 15;
                    frame = 0;
                }
                break;

            case ACT_SHOOTING:
                // Player is shooting while standing
                clipNo = 14; // Stand shooting animation
                dx = 0; // No horizontal movement
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
                    frame = 0;
                }

                // Player is running
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Allow horizontal movement while running in boss fight
                    if (facing == DIR_LEFT) {
                        x -= dx; // Move left
                    } else {
                        x += dx; // Move right
                    }
                }

                // Keep player within screen bounds
                if (x < 0) {
                    x = 0;
                } else if (x > BOARD_WIDTH - getWidth()) {
                    x = BOARD_WIDTH - getWidth();
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
            case ACT_STANDING:
                if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_SPACE) && isOnGround) {
                    action = ACT_JUMPING;
                    frame = 0;
                    clipNo = 1;
                    dy = jumpStrength; // Start jump with upward velocity
                    isOnGround = false;
                } else if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow shooting and left/right movement
                    if (key == KeyEvent.VK_ENTER) {
                        action = ACT_SHOOTING;
                        frame = 0;
                        isFiring = true;
                        clipNo = 1;
                    } else if (key == KeyEvent.VK_LEFT) {
                    facing = DIR_LEFT;
                    dx = speed; // Set positive speed
                    action = ACT_RUNNING;
                    frame = 0;
                    clipNo = 1;
                } else if (key == KeyEvent.VK_RIGHT) {
                    facing = DIR_RIGHT;
                    dx = speed;
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
                    clipNo = 1;
                    dy = jumpStrength; // Start jump with upward velocity
                    isOnGround = false;
                } else if (key == KeyEvent.VK_ENTER) {
                    action = ACT_RUNNING_SHOOTING;
                    frame = 0;
                    clipNo = 1; // Running shooting animation
                    isFiring = true;
                } else if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow shooting and left/right movement
                    if (key == KeyEvent.VK_ENTER) {
                        action = ACT_RUNNING_SHOOTING;
                        frame = 0;
                        isFiring = true;
                        clipNo = 1;
                    } else if (key == KeyEvent.VK_LEFT) {
                        dx = speed; // Move left
                        facing = DIR_LEFT;
                    } else if (key == KeyEvent.VK_RIGHT) {
                        dx = speed; // Move right
                        facing = DIR_RIGHT;
                    }
                }
                // Side-scrolling: No left/right movement or shooting - only jumping allowed
                break;

            case ACT_JUMPING:
                // Player is already jumping, handle other keys
                if (key == KeyEvent.VK_ENTER && !isOnGround) {
                    action = ACT_JUMPING_SHOOTING;
                    frame = 0;
                    isFiring = true;
                    clipNo = 15; // Jump shooting animation
                } else if (key == KeyEvent.VK_SPACE && !isOnGround) {
                    action = ACT_FLYING;
                    frame = 0;
                    dy = -3;
                    clipNo = 5; // Flying animation
                } 

                else if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow shooting and left/right movement while jumping
                    if (key == KeyEvent.VK_ENTER) {
                        action = ACT_JUMPING_SHOOTING;
                        frame = 0;
                        isFiring = true;
                        clipNo = 1;
                    } else if (key == KeyEvent.VK_LEFT) {
                        dx = speed; // Move left while jumping
                        facing = DIR_LEFT;
                    } else if (key == KeyEvent.VK_RIGHT) {
                        dx = speed; // Move right while jumping
                        facing = DIR_RIGHT;
                    }
                }
                break;

            case ACT_FLYING:
                // Player is already flying, handle other keys
                if (key == KeyEvent.VK_ENTER) {
                    action = ACT_FLYING_SHOOTING;
                    frame = 0;
                    isFiring = true;
                    clipNo = 11; // Flying shooting animation

                } else if (key == KeyEvent.VK_SPACE && !isOnGround) {
                    action = ACT_JUMPING; // Switch back to jumping
                    frame = 0;
                    clipNo = 4; // Jumping animation
                } else if (key == KeyEvent.VK_UP) {
                    dy = -3; // Move up while flying
                } else if (key == KeyEvent.VK_DOWN) {
                    dy = 3; // Move down while flying
                } else if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow left/right movement while flying
                    if (key == KeyEvent.VK_LEFT) {
                        dx = speed; // Move left while flying
                        facing = DIR_LEFT;
                    } else if (key == KeyEvent.VK_RIGHT) {
                        dx = speed; // Move right while flying
                        facing = DIR_RIGHT;
                    }
                }
                break;

            case ACT_FLYING_SHOOTING:
                // Player is already flying and shooting, handle other keys
                if (key == KeyEvent.VK_SPACE) {
                    action = ACT_JUMPING; // Switch back to jumping
                    frame = 0;
                    clipNo = 4; // Jumping animation
                } else if (key == KeyEvent.VK_UP) {
                    dy = -3; // Move up while flying
                } else if (key == KeyEvent.VK_DOWN) {
                    dy = 3; // Move down while flying
                } else if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow left/right movement while flying
                    if (key == KeyEvent.VK_LEFT) {
                        dx = speed; // Move left while flying
                        facing = DIR_LEFT;
                    } else if (key == KeyEvent.VK_RIGHT) {
                        dx = speed; // Move right while flying
                        facing = DIR_RIGHT;
                    }
                }
                break;

            case ACT_SHOOTING:
                // Player is already shooting, handle other keys
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow jumping while shooting
                    if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_SPACE) && isOnGround) {
                        action = ACT_JUMPING_SHOOTING;
                        frame = 0;
                        clipNo = 1;
                        dy = jumpStrength; // Start jump with upward velocity
                        isOnGround = false;
                    }
                }
                // Side-scrolling: No actions while shooting (shooting not allowed in
                // side-scrolling)
                break;

            case ACT_JUMPING_SHOOTING:
                // Player is jumping and shooting
                if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow left/right movement while jumping and shooting
                    if (key == KeyEvent.VK_LEFT) {
                        dx = speed; // Move left while jumping
                    } else if (key == KeyEvent.VK_RIGHT) {
                        dx = speed; // Move right while jumping
                    }
                }
                // Side-scrolling: No left/right movement while jumping and shooting
                break;

            case ACT_RUNNING_SHOOTING:
                if (key == KeyEvent.VK_SPACE) {
                    action = ACT_JUMPING_SHOOTING;
                    frame = 0;
                    isFiring = true;
                    clipNo = 15; // Jump shooting animation
                    isOnGround = false; // Start jump
                    dy = jumpStrength; // Start jump with upward velocity
                } else if (sceneType == SCENE_BOSS_FIGHT) {
                    // Boss fight: Allow jumping while running and shooting
                    if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_SPACE) && isOnGround) {
                        action = ACT_JUMPING_SHOOTING;
                        frame = 0;
                        clipNo = 1;
                        dy = jumpStrength; // Start jump with upward velocity
                        isOnGround = false;
                    }
                }
                // Side-scrolling: No actions while running and shooting (shooting not allowed
                // in side-scrolling)
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
                    clipNo = 1;
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
            case ACT_FLYING_SHOOTING:
                if (key == KeyEvent.VK_ENTER) {
                    isFiring = false;
                    action = ACT_FLYING; // Switch back to flying without shooting
                    frame = 0;
                    clipNo = 5; // Flying animation
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
        speed = PLAYER_SPEED;
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

    public void speedUpPlayer() {
        if (sceneType == SCENE_BOSS_FIGHT) {
            if (speed < MAX_PLAYER_SPEED) {
                speed++; // Increase speed by 1
                System.out.println("Player speed increased to " + speed);
            } else {
                System.out.println("Player is already at maximum speed");
            }
        }

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
        speed = PLAYER_SPEED;
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

    // public class Shot extends Sprite {

    // private boolean destroyed;

    // public Shot(int x, int y) {
    // initShot(x, y);
    // }

    // public void initShot(int x, int y) {
    // setDestroyed(true);

    // this.x = x;
    // this.y = y;

    // BufferedImage shotImg = new BufferedImage(6, 10,
    // BufferedImage.TYPE_INT_ARGB);
    // java.awt.Graphics2D g2d = shotImg.createGraphics();
    // g2d.setColor(java.awt.Color.RED);
    // g2d.fillRect(0, 0, 10, 2);
    // g2d.dispose();
    // setImage(shotImg);
    // }

    // public void setDestroyed(boolean destroyed) {

    // this.destroyed = destroyed;
    // }

    // public boolean isDestroyed() {

    // return destroyed;
    // }

    // @Override
    // public void act() {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method 'act'");
    // }
    // }
}
