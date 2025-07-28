package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.Shield;
import gdd.powerup.SpeedUp;

import static gdd.Global.*;
import gdd.sprite.Player;
import gdd.sprite.Robot;
import gdd.sprite.Shot;
import gdd.sprite.Tumbleweed;
import gdd.sprite.pharohHead;
import gdd.sprite.Attack;
import gdd.sprite.Boss;
import gdd.sprite.Cactus;
import gdd.sprite.Enemy;
import gdd.sprite.Explosion;
import gdd.sprite.Obstacle;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;

public class FinalScene extends BaseGameScene {

    private Boss boss;
    private int attackCounter = 0;
    private int deaths = 0;
    private int direction = -1;
    private static AudioPlayer damageSound;
    private long lastDamageSoundTime = 0;

    private List<Attack> attacks;
    private List<Shot> shots;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;

    private int speedupCounter = 0;
    private int shieldCounter = 0;
    private int enemyCounter = 0;

    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();

    public FinalScene(Game game) {
        super(game);
    }

    @Override
    protected void gameInit() {
        // Create player in standing position (not running)
        player = new Player();

        // Reset player to default state
        player.resetHP();

        // // Position player on the left side of screen for boss fight
        // player.setX(150); // Move player to left side
        // player.setY(GROUND - PLAYER_HEIGHT);

        // Set player to standing mode for boss fight
        player.setToStandingMode();

        // Create the boss
        boss = new Boss();

        // Initialize attack list
        attacks = new ArrayList<>();
        powerups = new ArrayList<>();
        shots = new ArrayList<>();
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();

        if (damageSound == null) {
            try {
                damageSound = new AudioPlayer("src/audio/damaged.wav");
            } catch (Exception e) {
                System.err.println("Error loading damage sound: " + e.getMessage());
            }
        }
    }

    @Override
    protected void update() {

        // Check game over conditions first
        checkGameOver();
        checkStageComplete(); // Add stage completion check

        // Update player (but in standing mode, no running animation)
        player.act();

        if (player.isFiring() && player.canFire()) {
            // Create a new shot and add it to the list
            Shot shot = new Shot(player.getDirection(), player);
            shots.add(shot);
            player.setShotCooldown();
        }
        // Shots
        List<Shot> shotsToRemove = new ArrayList<>();
        List<Enemy> enemiesToRemove = new ArrayList<>();
        for (Shot shot : shots) {

            // int shotX = shot.getX();
            // int shotY = shot.getY();

            if (shot.isVisible()) {

                for (Enemy enemy : enemies) {

                    if (enemy.isVisible() && shot.isVisible() && enemy.collidesWith(shot)) {

                        var ii = new Explosion(enemy.getX(), enemy.getY());
                        enemy.setImage(ii.getImage());
                        enemy.setDying(true);
                        explosions.add(ii);
                        shot.die();
                        shotsToRemove.add(shot);
                        enemiesToRemove.add(enemy);
                        deaths++;
                        try {
                            AudioPlayer bossAttackSound = new AudioPlayer("src/audio/bossshot.wav");
                            bossAttackSound.play();
                        } catch (Exception e) {
                            System.err.println("Boss attack sound error: " + e.getMessage());
                        }

                        // Reduce boss HP by one when an enemy is destroyed
                        if (boss != null && !boss.isDead()) {
                            boss.takeDamage(2);
                            System.out.println("Boss HP reduced by 2! Current HP: " + boss.getCurrentHP());
                        }
                    }
                }

                shot.act();

            }
        }
        shots.removeAll(shotsToRemove);
        enemies.removeAll(enemiesToRemove);

        // Update boss
        if (boss != null && !boss.isDead()) {
            boss.act();
        }

        // Spawn attacks
        attackCounter++;
        handleDynamicSpawning();

        for (Attack attack : attacks) {
            attack.act();
        }

        // Check attacks collisions with player
        for (Attack attack : attacks) {
            if (attack.isVisible() && player.collidesWith(attack)) {
                // Player hit by attack
                if (!player.isShieldActive()) {
                    player.takeDamage(1);

                    long now = System.currentTimeMillis();
                    if (damageSound != null && now - lastDamageSoundTime > 500) {
                        damageSound.stop();
                        damageSound.play();
                        lastDamageSoundTime = now;
                    }
                    System.out.println("Player hit by attack! HP: " + player.getCurrentHP());
                }
            }
        }

        // Check collision between player and boss bombs
        // checkBombCollisions();

        // Check for boss defeat
        if (boss != null && boss.isDead()) {
            triggerVictory();
        }

        // Power-ups
        for (PowerUp powerup : powerups) {
            if (powerup.isVisible()) {
                powerup.act();
                if (powerup.collidesWith(player)) {
                    powerup.upgrade(player);
                    try {
                        AudioPlayer powerupSound = new AudioPlayer(
                                "src/audio/mixkit-winning-a-coin-video-game-2069.wav");
                        powerupSound.play();
                    } catch (Exception e) {
                        System.err.println("Power-up sound error: " + e.getMessage());
                    }
                }
            }
        }

        // Enemies
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                enemy.act(direction);
            }
        }

    }

    private void handleDynamicSpawning() {

        int side = (int) (Math.random() * 4); // Randomly choose side 0-3

        if (attackCounter >= SpawnDetails.getShortSpawnTime()) {
            if (side == 0) {
                // Top side attack
                Attack attack = new Attack(SpawnDetails.getSpawnX(), 0 - 128, side);
                attacks.add(attack);
            } else if (side == 1) {
                // Right side attack
                Attack attack = new Attack(BOARD_WIDTH, SpawnDetails.getSpawnY(), side);
                attacks.add(attack);
            } else if (side == 2) {
                // Bottom side attack
                Attack attack = new Attack(SpawnDetails.getSpawnX(), BOARD_HEIGHT + 128, side);
                attacks.add(attack);
            } else if (side == 3) {
                // Left side attack
                Attack attack = new Attack(0 - 128, SpawnDetails.getSpawnY(), side);
                attacks.add(attack);
            }
            attackCounter = 0; // Reset counter

        }

        speedupCounter++;
        shieldCounter++;
        enemyCounter++;

        if (speedupCounter >= SpawnDetails.getVeryLongSpawnTime()) {
            SpawnDetails speedupSpawn = new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, SpawnDetails.getSpawnY());
            spawnEntity(speedupSpawn);
            speedupCounter = 0; // Reset counter
            System.out.println("Dynamic spawn: PowerUp-SpeedUp at frame " + frame);
        }

        if (shieldCounter >= SpawnDetails.getVeryLongSpawnTime()) {
            SpawnDetails shieldSpawn = new SpawnDetails("PowerUp-Shield", BOARD_WIDTH, SpawnDetails.getSpawnY());
            spawnEntity(shieldSpawn);
            shieldCounter = 0; // Reset counter
            System.out.println("Dynamic spawn: PowerUp-Shield at frame " + frame);
        }
        // Spawn enemy at random side (left or right)
        if (enemyCounter >= SpawnDetails.getLongSpawnTime()) {
            int enemySide = (int) (Math.random() * 2); // 0 = left, 1 = right
            int enemyX = (enemySide == 0) ? 0 : BOARD_WIDTH;
            SpawnDetails enemySpawn = new SpawnDetails("Enemy", enemyX, SpawnDetails.getSpawnY());
            spawnEntity(enemySpawn);
            enemyCounter = 0; // Reset counter
            System.out.println(
                    "Dynamic spawn: Enemy at frame " + frame + " (side: " + (enemySide == 0 ? "left" : "right") + ")");
        }
    }

    private void spawnEntity(SpawnDetails sd) {
        switch (sd.type) {
            case "PowerUp-SpeedUp":
                PowerUp speedUp = new SpeedUp(sd.x, sd.y);
                powerups.add(speedUp);
                break;
            case "PowerUp-Shield":
                PowerUp shield = new Shield(sd.x, sd.y);
                powerups.add(shield);
                break;
            case "Enemy":
                direction = (sd.x == 0) ? 1 : -1;
                Enemy enemy = new pharohHead(sd.x, sd.y);
                enemies.add(enemy);
                break;
            default:
                System.out.println("Unknown entity type: " + sd.type);
                break;
        }
    }

    private void drawPowerUps(Graphics g) {

        for (PowerUp p : powerups) {

            if (p.isVisible()) {

                g.drawImage(p.getImage(), p.getX(), p.getY(), this);
            }

            if (p.isDying()) {

                p.die();
            }
        }
    }

    private void drawShot(Graphics g) {

        for (Shot shot : shots) {

            if (shot.isVisible()) {
                g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
            }
        }
    }

    private void drawEnemies(Graphics g) {

        for (Enemy enemy : enemies) {

            if (enemy.isVisible()) {

                g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
            }

            if (enemy.isDying()) {

                enemy.die();
            }
            // // Debug: Draw enemy bounds
            // g.setColor(Color.RED);
            // g.drawRect(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
        }
    }

    private void drawExplosions(Graphics g) {

        List<Explosion> toRemove = new ArrayList<>();

        for (Explosion explosion : explosions) {

            if (explosion.isVisible()) {
                g.drawImage(explosion.getImage(), explosion.getX(), explosion.getY(), this);
                explosion.visibleCountDown();
                if (!explosion.isVisible()) {
                    toRemove.add(explosion);
                }
            }
        }

        explosions.removeAll(toRemove);
    }

    @Override
    protected void drawScene(Graphics g) {
        drawStaticBackground(g);
        drawBoss(g);
        drawBossBombs(g);
        drawPlayer(g);
        drawAttacks(g);
        drawPowerUps(g);
        drawShot(g);
        drawExplosions(g);
        drawEnemies(g);
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
        // g.setColor(Color.RED);
        // g.setFont(new Font("Arial", Font.BOLD, 24));
        // String titleText = "BOSS FIGHT";
        // int textWidth = g.getFontMetrics().stringWidth(titleText);
        // g.drawString(titleText, (BOARD_WIDTH - textWidth) / 2, 70); // Moved down to
        // make room for boss HP

        // Instructions
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Arrow Keys: Move", 10, BOARD_HEIGHT - 60);
        g.drawString("UP: Jump", 10, BOARD_HEIGHT - 45);
        g.drawString("ENTER: Attack Boss (-10 HP)", 10, BOARD_HEIGHT - 30);
        g.drawString("P: Pause", 10, BOARD_HEIGHT - 15);
    }

    @Override
    protected String getAudioFilePath() {
        return "src/audio/bossscene.wav"; // Use Scene1 audio for now
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
        ImageIcon backgroundImage = new ImageIcon(IMG_BACKGROUND_FINAL);
        g.drawImage(backgroundImage.getImage(), 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);

        // Optional: Add a darker overlay to distinguish from Scene1
        g.setColor(new Color(0, 0, 0, 50)); // Semi-transparent black overlay
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
    }

    private void drawPlayer(Graphics g) {
        if (player.isVisible()) {
            int x = player.getX();
            int y = player.getY();
            int w = player.getWidth();
            int h = player.getHeight();

            g.drawImage(player.getImage(), x, y, w, h, this);

            // Draw player image with red tint if invulnerable (recently took damage)
            if (player.isInvulnerable()) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.translate(x, y);
                g2d.setComposite(java.awt.AlphaComposite.SrcAtop.derive(1.0f));
                // Apply red tint using a ColorConvertOp or RescaleOp
                java.awt.image.RescaleOp redTint = new java.awt.image.RescaleOp(
                        new float[] { 1.0f, 0.3f, 0.3f, 1.0f }, // R, G, B, A scale
                        new float[] { 0f, 0f, 0f, 0f }, // offsets
                        null);
                g2d.drawImage(redTint.filter(
                        ((java.awt.image.BufferedImage) player.getImage()), null),
                        0, 0, w, h, null);
                g2d.dispose();
            }

            // // Debug: Draw player bounds
            // g.setColor(Color.BLUE);
            // g.drawRect(x, y, w, h);
        }
    }

    private void drawBoss(Graphics g) {
        if (boss != null && boss.isVisible()) {
            Image bossImg = boss.getImage();
            int scaledWidth = (int) (boss.getWidth() * SCALE_FACTOR);
            int scaledHeight = (int) (boss.getHeight() * SCALE_FACTOR);

            // Center horizontally, above ground
            int x = (BOARD_WIDTH - scaledWidth) / 2;
            int y = GROUND - scaledHeight + 50;

            g.drawImage(bossImg, x, y, scaledWidth, scaledHeight, this);

            // // Debug: Draw boss bounds
            // g.setColor(Color.RED);
            // g.drawRect(x, y, scaledWidth, scaledHeight);
        }
    }

    private void drawAttacks(Graphics g) {
        // Draw all attacks (if any)
        for (Attack attack : attacks) {
            if (attack.isVisible()) {
                g.drawImage(attack.getImage(), attack.getX(), attack.getY(), this);

                // // Debug: Draw attack bounds
                // g.setColor(Color.GREEN);
                // g.drawRect(attack.getX(), attack.getY(), attack.getWidth(),
                // attack.getHeight());
            }
        }
    }

    private void drawBossBombs(Graphics g) {
        if (boss != null) {
            for (var bomb : boss.getBombs()) {
                if (bomb.isVisible() && !bomb.isDestroyed()) {
                    g.drawImage(bomb.getImage(), bomb.getX(), bomb.getY(), this);

                    // // Debug: Draw bomb bounds
                    // g.setColor(Color.ORANGE);
                    // g.drawRect(bomb.getX(), bomb.getY(), bomb.getWidth(), bomb.getHeight());
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
                g.drawRect(barX - i, barY - i, barWidth + 2 * i, barHeight + 2 * i);
            }
        }
    }

    private void checkGameOver() {
        // Check if player is dead
        if (player.isDying()) {
            player.die();
            triggerGameOver("Game Over");
            return;
        }

        // Check enemy invasion (enemies reach the ground)
        // for (Enemy enemy : enemies) {
        // if (enemy.isVisible()) {
        // int y = enemy.getY();
        // if (y > GROUND - ALIEN_HEIGHT) {
        // triggerGameOver("Invasion!");
        // return;
        // }
        // }
        // }

        // Check enemy-player collision (direct contact = game over)
        for (Enemy enemy : enemies) {
            if (enemy.isVisible() && player.isVisible()) {
                // int enemyX = enemy.getX();
                // int enemyY = enemy.getY();
                // int playerX = player.getX();
                // int playerY = player.getY();

                // Check if enemy and player are colliding
                // if (enemyX < playerX + PLAYER_WIDTH &&
                // enemyX + ALIEN_WIDTH > playerX &&
                // enemyY < playerY + PLAYER_HEIGHT &&
                // enemyY + ALIEN_HEIGHT > playerY) {

                if (enemy.isVisible()) {
                    if (enemy.collidesWith(player)) {
                        // Enemy touched player directly - reduce HP by 2
                        if (!player.isShieldActive()) {
                            player.takeDamage(2);

                            long now = System.currentTimeMillis();
                            if (damageSound != null && now - lastDamageSoundTime > 500) {
                                damageSound.stop();
                                damageSound.play();
                                lastDamageSoundTime = now;
                            }
                            System.out.println("Player hit by enemy! HP: " + player.getCurrentHP());
                        }
                        // Optionally, you can add a knockback or invincibility effect here
                        if (player.getCurrentHP() <= 0) {
                            player.setDying(true);
                        }
                        return;
                    }
                }
            }
        }
    }

    private void checkStageComplete() {
        // Check if player has completed Final Stage

        if (boss.getCurrentHP() == 0) { // debug condition for stage completion
            triggerVictory();
            inGame = false;
            gameOverMessage = "GAME WON!";

            // Stop the timer
            if (timer != null && timer.isRunning()) {
                timer.stop();
            }

            // Stop audio
            try {
                if (audioPlayer != null) {
                    audioPlayer.stop();
                }
            } catch (Exception e) {
                System.err.println("Error stopping audio: " + e.getMessage());
            }

            System.out.println("Game won! Boss defeated.");
        }

    }

    @Override
    protected void drawGameOverScreen(Graphics g) {
        // Draw gameOver.png over the whole screen
        ImageIcon gameOverImg = new ImageIcon("src/dodoSprites/gameOver.png");
        g.drawImage(gameOverImg.getImage(), 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);

        // Draw additional info
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        int y = BOARD_HEIGHT / 2 + 20;
        if (gameOverMessage != null && !gameOverMessage.isEmpty()) {
            int infoWidth = g.getFontMetrics().stringWidth(gameOverMessage);
            g.drawString(gameOverMessage, (BOARD_WIDTH - infoWidth) / 2, y);
            y += 40;
        }
        // Show player/boss/enemy/speed stats
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        String stats = String.format(
                "Player HP: %d   Boss HP: %d   Enemies Destroyed: %d   Player Speed: %d",
                player.getCurrentHP(),
                (boss != null ? boss.getCurrentHP() : 0),
                deaths,
                player.getSpeed());
        int statsWidth = g.getFontMetrics().stringWidth(stats);
        g.drawString(stats, (BOARD_WIDTH - statsWidth) / 2, y + 10);
        y += 40;

        String prompt = "Press Space to restart or ESC to quit";
        int promptWidth = g.getFontMetrics().stringWidth(prompt);
        g.drawString(prompt, (BOARD_WIDTH - promptWidth) / 2, y + 20);
    }

    @Override
    protected void drawVictoryScreen(Graphics g) {

        // draw background
        ImageIcon victoryImg = new ImageIcon("src/dodoSprites/desertBG.png");
        g.drawImage(victoryImg.getImage(), 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
        // Draw additional info
        g.setFont(new Font("Arial", Font.PLAIN, 40));
        int y = BOARD_HEIGHT / 2 + 20;
        if (super.getVictoryMessage() != null && !super.getVictoryMessage().isEmpty()) {
            int infoWidth = g.getFontMetrics().stringWidth(super.getVictoryMessage());
            g.drawString(super.getVictoryMessage(), (BOARD_WIDTH - infoWidth) / 2, y);
            y += 40;
        }

        // Show player/boss/enemy/speed stats
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        String stats = String.format(
                "Player HP: %d   Boss HP: %d   Enemies Destroyed: %d   Player Speed: %d",
                player.getCurrentHP(),
                (boss != null ? boss.getCurrentHP() : 0),
                deaths,
                player.getSpeed());
        int statsWidth = g.getFontMetrics().stringWidth(stats);
        g.drawString(stats, (BOARD_WIDTH - statsWidth) / 2, y + 10);
        y += 40;

        String prompt = "Press Space to restart or ESC to quit";
        int promptWidth = g.getFontMetrics().stringWidth(prompt);
        g.drawString(prompt, (BOARD_WIDTH - promptWidth) / 2, y + 20);
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
            /*
             * int key = e.getKeyCode();
             * 
             * // Handle boss fight specific controls
             * if (key == KeyEvent.VK_ENTER) {
             * // Test attack - damage boss
             * if (boss != null && !boss.isDead()) {
             * boss.takeDamage(10);
             * }
             * return;
             * }
             */

            // Pass input to player
            player.keyPressed(e);
        }
    }
}
