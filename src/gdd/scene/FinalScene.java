package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import gdd.sprite.Player;
import gdd.sprite.Boss;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

public class FinalScene extends BaseGameScene {

    private Boss boss;

    public FinalScene(Game game) {
        super(game);
    }

    @Override
    protected void gameInit() {
        // Create player in standing position (not running)
        player = new Player();
        
        // Reset player to default state
        player.resetHP();
        
        // Position player on the left side of screen for boss fight
        player.setX(150); // Move player to left side
        player.setY(GROUND - PLAYER_HEIGHT);
        
        // Set player to standing mode for boss fight
        player.setToStandingMode();
        
        // Create the boss
        boss = new Boss();
    }

    @Override
    protected void update() {
        // Update player (but in standing mode, no running animation)
        player.act();
        
        // Update boss
        if (boss != null && !boss.isDead()) {
            boss.act();
        }
        
        // Check collision between player and boss bombs
        checkBombCollisions();
        
        // Check for boss defeat
        if (boss != null && boss.isDead()) {
            triggerVictory();
        }
        
        // TODO: Add player attack mechanics (projectiles)
        // TODO: Add more boss attack patterns
    }
    
    private void checkBombCollisions() {
        if (boss == null || player == null) return;
        
        for (var bomb : boss.getBombs()) {
            if (!bomb.isDestroyed() && bomb.isVisible()) {
                if (player.collidesWith(bomb)) {
                    // Player hit by boss bomb
                    if (!player.isShieldActive()) {
                        player.takeDamage(1);
                        System.out.println("Player hit by boss bomb! HP: " + player.getCurrentHP());
                    }
                    bomb.setDestroyed(true);
                    
                    // Check if player is defeated
                    if (player.getCurrentHP() <= 0) {
                        triggerGameOver("Defeated by Boss!");
                    }
                }
            }
        }
    }

    @Override
    protected void drawScene(Graphics g) {
        drawStaticBackground(g);
        drawBoss(g);
        drawBossBombs(g);
        drawPlayer(g);
    }

    @Override
    protected void drawHUD(Graphics g) {
        // Call parent to draw HP bar
        super.drawHUD(g);
        
        // Draw boss HP bar
        if (boss != null && !boss.isDead()) {
            drawBossHPBar(g);
        }
        
        // Draw boss fight title
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String titleText = "BOSS FIGHT";
        int textWidth = g.getFontMetrics().stringWidth(titleText);
        g.drawString(titleText, (BOARD_WIDTH - textWidth) / 2, 70); // Moved down to make room for boss HP
        
        // // Instructions
        // g.setColor(Color.YELLOW);
        // g.setFont(new Font("Arial", Font.PLAIN, 12));
        // g.drawString("Arrow Keys: Move", 10, BOARD_HEIGHT - 60);
        // g.drawString("UP: Jump", 10, BOARD_HEIGHT - 45);
        // g.drawString("ENTER: Attack Boss (-10 HP)", 10, BOARD_HEIGHT - 30);
        // g.drawString("P: Pause", 10, BOARD_HEIGHT - 15);
    }

    @Override
    protected String getAudioFilePath() {
        return "src/audio/scene1.wav"; // Use Scene1 audio for now
    }

    @Override
    protected String getSceneName() {
        return "FINAL SCENE";
    }

    @Override
    protected String getVictoryMessage() {
        return "You defeated the boss!";
    }

    @Override
    protected KeyAdapter createKeyAdapter() {
        return new FinalSceneKeyAdapter();
    }

    private void drawStaticBackground(Graphics g) {
        // Draw static background (same as Scene1 but not scrolling)
        ImageIcon backgroundImage = new ImageIcon(IMG_BACKGROUND);
        g.drawImage(backgroundImage.getImage(), 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
        
        // Optional: Add a darker overlay to distinguish from Scene1
        g.setColor(new Color(0, 0, 0, 50)); // Semi-transparent black overlay
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
    }

    private void drawPlayer(Graphics g) {
        if (player.isVisible()) {
            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
            
            // Debug: Draw player bounds
            g.setColor(Color.BLUE);
            g.drawRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        }
    }

    private void drawBoss(Graphics g) {
        if (boss != null && boss.isVisible()) {
            g.drawImage(boss.getImage(), boss.getX(), boss.getY(), this);
            
            // Debug: Draw boss bounds
            g.setColor(Color.RED);
            g.drawRect(boss.getX(), boss.getY(), boss.getWidth(), boss.getHeight());
        }
    }

    private void drawBossBombs(Graphics g) {
        if (boss != null) {
            for (var bomb : boss.getBombs()) {
                if (bomb.isVisible() && !bomb.isDestroyed()) {
                    g.drawImage(bomb.getImage(), bomb.getX(), bomb.getY(), this);
                    
                    // Debug: Draw bomb bounds
                    g.setColor(Color.ORANGE);
                    g.drawRect(bomb.getX(), bomb.getY(), bomb.getWidth(), bomb.getHeight());
                }
            }
        }
    }

    private void drawBossHPBar(Graphics g) {
        // Boss HP bar dimensions and position (top center)
        int barWidth = 400;
        int barHeight = 30;
        int barX = (BOARD_WIDTH - barWidth) / 2; // Center horizontally
        int barY = 20; // Top of screen
        
        // Calculate HP percentage
        double hpPercentage = (double) boss.getCurrentHP() / boss.getMaxHP();
        int fillWidth = (int) (barWidth * hpPercentage);
        
        // Draw HP bar background
        g.setColor(new Color(30, 30, 30));
        g.fillRect(barX, barY, barWidth, barHeight);
        
        // Draw HP bar border
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barWidth, barHeight);
        
        // Draw HP fill based on percentage
        Color fillColor;
        if (hpPercentage > 0.6) {
            fillColor = new Color(255, 0, 0); // Red (boss color)
        } else if (hpPercentage > 0.3) {
            fillColor = new Color(255, 100, 0); // Orange
        } else {
            fillColor = new Color(255, 200, 0); // Yellow
        }
        
        g.setColor(fillColor);
        if (fillWidth > 0) {
            g.fillRect(barX + 2, barY + 2, fillWidth - 4, barHeight - 4);
        }
        
        // Draw boss name and HP text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        String bossText = "FINAL BOSS";
        int textWidth = g.getFontMetrics().stringWidth(bossText);
        g.drawString(bossText, (BOARD_WIDTH - textWidth) / 2, barY - 5);
        
        // Draw HP numbers
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String hpText = boss.getCurrentHP() + " / " + boss.getMaxHP();
        int hpTextWidth = g.getFontMetrics().stringWidth(hpText);
        g.drawString(hpText, barX + (barWidth - hpTextWidth) / 2, barY + barHeight + 15);
        
        // Draw warning border if boss is low HP
        if (hpPercentage <= 0.3) {
            g.setColor(Color.RED);
            for (int i = 0; i < 3; i++) {
                g.drawRect(barX - i, barY - i, barWidth + 2*i, barHeight + 2*i);
            }
        }
    }

    private class FinalSceneKeyAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            if (isPaused || !inGame) {
                return;
            }
            player.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // Handle common keys (pause, end game) first
            if (handleCommonKeys(e)) {
                return;
            }

            int key = e.getKeyCode();
            
            // Handle boss fight specific controls
            if (key == KeyEvent.VK_ENTER) {
                // Test attack - damage boss
                if (boss != null && !boss.isDead()) {
                    boss.takeDamage(10);
                }
                return;
            }

            // Pass input to player
            player.keyPressed(e);
        }
    }
}
