package gdd.sprite;

import static gdd.Global.*;
import javax.swing.ImageIcon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Boss extends Enemy {

    private int maxHP;
    private int currentHP;
    private boolean isDead;
    private int attackTimer;
    private int attackCooldown;
    
    // Boss bomb system
    private List<BossBomb> bombs;
    private static final int BOMBS_PER_ATTACK = 4;
    private static final int[] BOMB_Y_POSITIONS = {150, 250, 350, 450, 550}; // Fixed Y positions
    
    // Boss dimensions (takes up most of the right side of screen)
    private static final int BOSS_WIDTH = BOARD_WIDTH / 7;
    private static final int BOSS_HEIGHT = BOARD_HEIGHT - 100;
    private static final int BOSS_X = BOARD_WIDTH - BOSS_WIDTH - 50;
    private static final int BOSS_Y = 50;
    
    // HP constants
    private static final int DEFAULT_MAX_HP = 100;
    private static final int ATTACK_INTERVAL = 180; // 3 seconds at 60 FPS

    public Boss() {
        super(BOSS_X, BOSS_Y);
        initBoss();
    }

    private void initBoss() {
        // Initialize HP
        this.maxHP = DEFAULT_MAX_HP;
        this.currentHP = maxHP;
        this.isDead = false;
        
        // Initialize attack timing
        this.attackTimer = 0;
        this.attackCooldown = ATTACK_INTERVAL;
        
        // Initialize bomb list
        this.bombs = new ArrayList<>();
        
        // Load and scale alien image to boss size
        var ii = new ImageIcon(IMG_ENEMY);
        var scaledImage = ii.getImage().getScaledInstance(BOSS_WIDTH, BOSS_HEIGHT, 
                java.awt.Image.SCALE_SMOOTH);
        setImage(scaledImage);
        
        setVisible(true);
        
        System.out.println("Boss created at (" + x + ", " + y + ") with size " + 
                          getWidth() + "x" + getHeight() + " and " + maxHP + " HP");
    }

    @Override
    public void act() {
        if (isDead) {
            return;
        }
        
        // Boss is static - no movement
        // Update attack timer
        attackTimer++;
        
        // Update bombs
        updateBombs();
        
        // Attack pattern: spawn multiple bombs at fixed intervals
        if (attackTimer >= attackCooldown) {
            performAttack();
            attackTimer = 0;
        }
    }
    
    private void updateBombs() {
        // Remove bombs that have gone off screen or are destroyed
        bombs.removeIf(bomb -> bomb.getY() > BOARD_HEIGHT || bomb.isDestroyed());
        
        // Update remaining bombs
        for (BossBomb bomb : bombs) {
            if (!bomb.isDestroyed()) {
                bomb.act();
            }
        }
    }
    
    private void performAttack() {
        System.out.println("Boss attacking with " + BOMBS_PER_ATTACK + " bombs!");
        
        // Spawn bombs at fixed Y positions using BossBomb class
        for (int i = 0; i < BOMBS_PER_ATTACK && i < BOMB_Y_POSITIONS.length; i++) {
            BossBomb bomb = new BossBomb(x - 50, BOMB_Y_POSITIONS[i]); // Spawn from left edge of boss
            bomb.setDestroyed(false); // Make bomb active
            bombs.add(bomb);
        }
    }
    
    public void takeDamage(int damage) {
        if (isDead) {
            return;
        }
        
        currentHP -= damage;
        System.out.println("Boss takes " + damage + " damage! HP: " + currentHP + "/" + maxHP);
        
        if (currentHP <= 0) {
            currentHP = 0;
            isDead = true;
            setVisible(false);
            System.out.println("Boss defeated!");
        }
    }
    
    // Custom Bomb class for boss that moves left
    public class BossBomb extends Bomb {
        public BossBomb(int x, int y) {
            super(x, y);
        }
        
        @Override
        public void act() {
            // Move bomb left towards player
            x -= 3; // Speed of bomb movement
        }
    }
    
    // Collision detection for boss
    public Rectangle getBounds() {
        return new Rectangle(x, y, getWidth(), getHeight());
    }
    
    // Getters
    public int getCurrentHP() { return currentHP; }
    public int getMaxHP() { return maxHP; }
    public boolean isDead() { return isDead; }
    public boolean canAttack() { return attackTimer >= attackCooldown; }
    public List<BossBomb> getBombs() { return bombs; }
    
    // Static getters for boss positioning
    public static int getBossX() { return BOSS_X; }
    public static int getBossY() { return BOSS_Y; }
    public static int getBossWidth() { return BOSS_WIDTH; }
    public static int getBossHeight() { return BOSS_HEIGHT; }
}
