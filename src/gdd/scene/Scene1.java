package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.powerup.Shield;
import gdd.sprite.Robot;
import gdd.sprite.Cactus;
import gdd.sprite.Enemy;
import gdd.sprite.Enemy.Bomb;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import gdd.sprite.Tumbleweed;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import gdd.sprite.Obstacle;

public class Scene1 extends JPanel {

    // state overlay
    private boolean isPaused = false;
    private boolean isGameWon = false;

    private long lastUpdateTime = System.nanoTime();
    private double scrollPosition = 0;

    private int frame = 0;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private Player player;

    // obstacles
    private List<Obstacle> obstacles;

    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;

    final int BLOCKS_TO_DRAW = BOARD_HEIGHT / BLOCKHEIGHT;

    private int direction = -1;
    private int deaths = 0;
    private static final int defaultScrollSpeed = GLOBAL_SPEED * 50; // You can dynamically adjust globalSpeed
    private int scrollSpeed = defaultScrollSpeed; // Initial scroll speed

    private boolean inGame = true;
    private String gameOverMessage = "Game Over";

    private final Random randomizer = new Random();

    private Timer timer;
    private final Game game;
    private long lastDamageSoundTime = 0;

    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private AudioPlayer audioPlayer;

    // Dynamic spawning counters
    private int tumbleweedSpawnCounter = 0;
    private int cactusSpawnCounter = 0;
    private int robotSpawnCounter = 0;
    private int speedupCounter = 0;
    private int shieldCounter = 0;

    private static AudioPlayer damageSound;

    public Scene1(Game game) {
        this.game = game;
        // initBoard();
        // gameInit();
        loadSpawnDetails();
    }

    private void initAudio() {
        try {
            String filePath = "src/audio/scene1.wav";
            audioPlayer = new AudioPlayer(filePath, true); // Looping audio
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
        }

    }

    private void loadSpawnDetails() {
        // Initial spawns to get the game started
        // spawnMap.put(50, new SpawnDetails("Tumbleweed", BOARD_WIDTH, GROUND));
        // spawnMap.put(100, new SpawnDetails("Cactus", BOARD_WIDTH, GROUND));
        // spawnMap.put(110, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, 300));
        // spawnMap.put(130, new SpawnDetails("PowerUp-Shield", BOARD_WIDTH, 580));
        // spawnMap.put(120, new SpawnDetails("Alien1", BOARD_WIDTH, 100));

        // Debug: Print initial spawn map contents
        System.out.println("Initial spawn map loaded with " + spawnMap.size() + " entries:");
        for (Integer frame : spawnMap.keySet()) {
            SpawnDetails sd = spawnMap.get(frame);
            System.out.println("  Frame " + frame + ": " + sd.type + " at (" + sd.x + ", " + sd.y + ")");
        }
    }

    public void start() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new GameCycle());
        timer.start();

        gameInit();
        initAudio();
    }

    public void stop() {
        timer.stop();
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error closing audio player.");
        }
    }

    private void gameInit() {

        enemies = new ArrayList<>();
        powerups = new ArrayList<>();
        explosions = new ArrayList<>();
        obstacles = new ArrayList<>();
        shots = new ArrayList<>();

        player = new Player();

        if (damageSound == null) {
            try {
                damageSound = new AudioPlayer("src/audio/damaged.wav");
            } catch (Exception e) {
                System.err.println("Error loading damage sound: " + e.getMessage());
            }
        }

    }

    // private void drawBackground(Graphics g) {
    // ImageIcon ii = new ImageIcon(IMG_BACKGROUND);
    // int bgWidth = BOARD_WIDTH;
    // int bgHeight = BOARD_HEIGHT;

    // // Increase this for faster scrolling
    // int scrollOffset = (frame * scrollSpeed) % bgWidth;

    // int x1 = -scrollOffset;
    // int x2 = x1 + bgWidth;

    // if (inGame) {
    // g.drawImage(ii.getImage(), x1, 0, bgWidth, bgHeight, this);
    // g.drawImage(ii.getImage(), x2, 0, bgWidth, bgHeight, this);
    // }
    // }
    private void drawBackground(Graphics g) {
        ImageIcon ii = new ImageIcon(IMG_BACKGROUND);
        int bgWidth = BOARD_WIDTH;
        int bgHeight = BOARD_HEIGHT;

        int scrollOffset = (int) (scrollPosition % bgWidth); // smoother now

        int x1 = -scrollOffset;
        int x2 = x1 + bgWidth;

        if (inGame) {
            g.drawImage(ii.getImage(), x1, 0, bgWidth, bgHeight, this);
            g.drawImage(ii.getImage(), x2, 0, bgWidth, bgHeight, this);
        }
    }

    private void drawRobots(Graphics g) {

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

        if (player.isDying()) {
            // Player death handled in checkGameOver()
        }
    }

    private void drawShot(Graphics g) {

        for (Shot shot : shots) {

            if (shot.isVisible()) {
                g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);
            }
        }
    }

    private void drawBombing(Graphics g) {

        for (Enemy e : enemies) {
            Enemy.Bomb b = e.getBomb();
            if (!b.isDestroyed()) {
                g.drawImage(b.getImage(), b.getX(), b.getY(), this);

                // // Debug: Draw bomb bounds
                // g.setColor(Color.ORANGE);
                // g.drawRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
            }
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

    private void drawObstacles(Graphics g) {
        if (obstacles.size() > 0) {
            System.out.println("Drawing " + obstacles.size() + " obstacles");
        }
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isVisible()) {
                g.drawImage(obstacle.getImage(), obstacle.getX(), obstacle.getY(), this);

                // // Debug: Draw obstacle bounds
                // g.setColor(Color.RED);
                // g.drawRect(obstacle.getX(), obstacle.getY(), obstacle.getWidth(),
                // obstacle.getHeight());
            }

            if (obstacle.isDying()) {
                obstacle.die();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        if (inGame) {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0; // seconds

            scrollPosition += scrollSpeed * deltaTime; // pixels = speed * time

            lastUpdateTime = currentTime;
            drawBackground(g);
            drawExplosions(g);
            drawObstacles(g);
            drawPowerUps(g);
            drawShot(g);

            drawRobots(g);
            drawBombing(g); // Draw enemy bombs
            drawPlayer(g);
            drawHPBar(g); // Draw HP bar

            if (isPaused) {
                System.out.println("isPaused is true, calling drawPauseOverlay");
                drawPauseOverlay(g);
            }
        } else if (isGameWon) {
            drawStage1Complete(g);
        }

        else {
            drawGameOver(g);
        }

        // g.setColor(Color.black);
        // g.fillRect(0, 0, d.width, d.height);

        g.setColor(Color.BLACK);
        g.drawString("FRAME: " + frame, 10, 10);
        g.setColor(Color.green);

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawStage1Complete(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(0, 64, 128)); // Blue background
        g.fillRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);

        // Stage complete text
        var large = new Font("Helvetica", Font.BOLD, 36);
        var fontMetrics = this.getFontMetrics(large);
        g.setColor(Color.YELLOW);
        g.setFont(large);
        String completeText = "STAGE 1 COMPLETE!";
        g.drawString(completeText, (BOARD_WIDTH - fontMetrics.stringWidth(completeText)) / 2,
                BOARD_HEIGHT / 2 - 10);

        // Instructions
        g.setFont(new Font("Helvetica", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        g.drawString("Preparing for cutscene...", BOARD_WIDTH / 2 - 80, BOARD_HEIGHT / 2 + 30);
        g.drawString("Press SPACE to continue", BOARD_WIDTH / 2 - 75, BOARD_HEIGHT / 2 + 50);
    }

    private void drawGameOver(Graphics g) {
        // g.setColor(Color.black);
        // g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // g.setColor(new Color(0, 32, 48));
        // g.fillRect(50, BOARD_HEIGHT / 2 - 30, BOARD_WIDTH - 100, 50);
        // g.setColor(Color.white);
        // g.drawRect(50, BOARD_HEIGHT / 2 - 30, BOARD_WIDTH - 100, 50);

        // var large = new Font("Helvetica", Font.BOLD, 36);
        // var fontMetrics = this.getFontMetrics(large);

        // g.setColor(Color.white);
        // g.setFont(large);
        // g.drawString(gameOverMessage, (BOARD_WIDTH -
        // fontMetrics.stringWidth(gameOverMessage)) / 2,
        // BOARD_HEIGHT / 2);

        // // Draw instructions
        // g.setFont(new Font("Helvetica", Font.PLAIN, 12));
        // g.drawString("Press SPACE to restart", BOARD_WIDTH / 2 - 60, BOARD_HEIGHT / 2
        // + 30);
        // g.drawString("Press ESC to quit", BOARD_WIDTH / 2 - 50, BOARD_HEIGHT / 2 +
        // 50);
        ImageIcon gameOverOverlay = new ImageIcon(IMG_GAME_OVER);
        g.drawImage(gameOverOverlay.getImage(), 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
    }

    private void drawPauseOverlay(Graphics g) {
        System.out.println("Drawing pause overlay");

        // // Semi-transparent overlay
        // g.setColor(new Color(0, 0, 0, 128));
        // g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // // Pause box
        // g.setColor(new Color(0, 32, 48));
        // g.fillRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);
        // g.setColor(Color.white);
        // g.drawRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);
        g.drawImage(new ImageIcon(IMG_PAUSE).getImage(), 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);

        // // Pause text
        // var large = new Font("Helvetica", Font.BOLD, 36);
        // var fontMetrics = this.getFontMetrics(large);
        // g.setColor(Color.white);
        // g.setFont(large);
        // String pauseText = "PAUSED";
        // g.drawString(pauseText, (BOARD_WIDTH - fontMetrics.stringWidth(pauseText)) /
        // 2,
        // BOARD_HEIGHT / 2 - 20);

        // // Instructions
        // g.setFont(new Font("Helvetica", Font.PLAIN, 14));
        // g.drawString("Press P to resume", BOARD_WIDTH / 2 - 60, BOARD_HEIGHT / 2 +
        // 20);
        // g.drawString("Press ESC to quit", BOARD_WIDTH / 2 - 55, BOARD_HEIGHT / 2 +
        // 40);
    }

    private void drawHPBar(Graphics g) {
        // HP Bar dimensions and position (top right)
        int barWidth = 200;
        int barHeight = 20;
        int barX = BOARD_WIDTH - barWidth - 20; // 20 pixels from right edge
        int barY = 20; // 20 pixels from top

        // Get player HP
        int currentHP = player.getCurrentHP();
        int maxHP = player.getMaxHP();

        // Calculate HP percentage
        double hpPercentage = (double) currentHP / maxHP;
        int fillWidth = (int) (barWidth * hpPercentage);

        // Draw HP bar background (dark gray)
        g.setColor(new Color(50, 50, 50));
        g.fillRect(barX, barY, barWidth, barHeight);

        // Draw HP bar border
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barWidth, barHeight);

        // Draw HP fill based on percentage
        if (hpPercentage > 0.6) {
            g.setColor(Color.GREEN); // High HP - Green
        } else if (hpPercentage > 0.3) {
            g.setColor(Color.YELLOW); // Medium HP - Yellow
        } else {
            g.setColor(Color.RED); // Low HP - Red
        }

        if (fillWidth > 0) {
            g.fillRect(barX + 1, barY + 1, fillWidth - 2, barHeight - 2);
        }

        // Draw HP text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String hpText = "HP: " + currentHP + "/" + maxHP;
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(hpText);
        g.drawString(hpText, barX + (barWidth - textWidth) / 2, barY + barHeight + 15);

        // Draw invulnerability indicator
        if (player.isInvulnerable()) {
            g.setColor(Color.CYAN);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString("INVULNERABLE", barX, barY - 5);
        }

        // Draw shield indicator
        if (player.isShieldActive()) {
            g.setColor(Color.BLUE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String shieldText = "SHIELD: " + (player.getShieldTimer() / 60.0f) + "s";
            g.drawString(shieldText, barX, barY - 20);

            // Draw shield border around HP bar
            g.setColor(Color.BLUE);
            g.drawRect(barX - 2, barY - 2, barWidth + 4, barHeight + 4);
        }

        // Debug: Draw player position info
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        String posText = "Player: (" + player.getX() + "," + player.getY() + ") " +
                player.getWidth() + "x" + player.getHeight();
        g.drawString(posText, 10, 30);

        // Draw stage timer countdown
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        int remainingFrames = Math.max(0, 300 - frame);
        float remainingSeconds = remainingFrames / 60.0f;
        String timerText = String.format("Stage Complete in: %.1fs", remainingSeconds);
        g.drawString(timerText, 10, 50);
    }

    private void handleDynamicSpawning() {

        // Spawn Tumbleweed based on interval
        if (tumbleweedSpawnCounter >= SpawnDetails.getShortSpawnTime()) {
            SpawnDetails tumbleweedSpawn = new SpawnDetails("Tumbleweed", BOARD_WIDTH, GROUND);
            spawnEntity(tumbleweedSpawn);
            tumbleweedSpawnCounter = 0; // Reset counter
            System.out.println("Dynamic spawn: Tumbleweed at frame " + frame);
        }

        // Spawn Cactus based on interval
        if (cactusSpawnCounter >= SpawnDetails.getLongSpawnTime()) {
            SpawnDetails cactusSpawn = new SpawnDetails("Cactus", BOARD_WIDTH, GROUND);
            spawnEntity(cactusSpawn);
            cactusSpawnCounter = 0; // Reset counter
            System.out.println("Dynamic spawn: Cactus at frame " + frame);
        }

        // Spawn Robot based on interval
        if (robotSpawnCounter >= SpawnDetails.getShortSpawnTime()) {

            SpawnDetails robotSpawn = new SpawnDetails("Robot", BOARD_WIDTH, SpawnDetails.getSpawnY());
            spawnEntity(robotSpawn);
            robotSpawnCounter = 0; // Reset counter
            System.out.println("Dynamic spawn: Robot at frame " + frame + " Y=" + SpawnDetails.getSpawnY());
        }

        // Collectibles
        speedupCounter++;
        shieldCounter++;
        // Spawn SpeedUp power-up every 5 seconds (300 frames)
        if (speedupCounter >= SpawnDetails.getLongSpawnTime()) {
            SpawnDetails speedupSpawn = new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, SpawnDetails.getSpawnY());
            spawnEntity(speedupSpawn);
            speedupCounter = 0; // Reset counter
            System.out.println("Dynamic spawn: PowerUp-SpeedUp at frame " + frame);
        }

        if (shieldCounter >= SpawnDetails.getLongSpawnTime()) {
            SpawnDetails shieldSpawn = new SpawnDetails("PowerUp-Shield", BOARD_WIDTH, SpawnDetails.getSpawnY());
            spawnEntity(shieldSpawn);
            shieldCounter = 0; // Reset counter
            System.out.println("Dynamic spawn: PowerUp-Shield at frame " + frame);
        }
    }

    private void spawnEntity(SpawnDetails sd) {
        switch (sd.type) {
            case "Robot":
                Enemy enemy = new Robot(sd.x, sd.y);
                enemies.add(enemy);
                break;
            case "PowerUp-SpeedUp":
                PowerUp speedUp = new SpeedUp(sd.x, sd.y);
                powerups.add(speedUp);
                break;
            case "PowerUp-Shield":
                PowerUp shield = new Shield(sd.x, sd.y);
                powerups.add(shield);
                break;
            case "Tumbleweed":
                Obstacle tumbleweed = new Tumbleweed(sd.x, sd.y - 20);
                obstacles.add(tumbleweed);
                break;
            case "Cactus":
                Obstacle cactus = new Cactus(sd.x, sd.y - 200);
                obstacles.add(cactus);
                break;
            default:
                System.out.println("Unknown entity type: " + sd.type);
                break;
        }
    }

    private void update() throws UnsupportedAudioFileException, IOException, LineUnavailableException {

        // Check game over conditions first
        checkGameOver();
        checkStage1Complete(); // Add stage completion check

        if (!inGame || isPaused) {
            return; // Don't update if game is over or paused
        }

        if (scrollSpeed < globalSpeed * 50) {
            scrollSpeed += 10; // Ensure scroll speed is at least global speed
        }
        tumbleweedSpawnCounter++;
        cactusSpawnCounter++;
        robotSpawnCounter++;

        // Handle dynamic spawning based on counters
        handleDynamicSpawning();

        // Check fixed enemy spawn (for initial spawns only)
        // SpawnDetails sd = spawnMap.get(frame);
        // if (sd != null) {
        // System.out.println("Spawning at frame " + frame + ": " + sd.type + " at (" +
        // sd.x + ", " + sd.y + ")");
        // spawnEntity(sd);
        // }

        // player
        player.act();

        if (player.isFiring() && player.canFire()) {
            // Create a new shot and add it to the list
            Shot shot = new Shot(player.getX(), player.getY() + 64);
            shots.add(shot);
            // After creating and adding the shot
            try {
                AudioPlayer shotSound = new AudioPlayer("src/audio/robotshot.wav");
                shotSound.play();
            } catch (Exception e) {
                System.err.println("Shot sound error: " + e.getMessage());
            }
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

                    }
                }

                shot.act(); // Update shot position

                // if (shotX > BOARD_WIDTH || shotY < 0) {
                // shot.die();
                // shotsToRemove.add(shot);
                // } else {
                // shot.setX(shotX);
                // }
            }
        }
        shots.removeAll(shotsToRemove);
        enemies.removeAll(enemiesToRemove);

        // // Create rectangles for player and obstacle
        // Rectangle playerRect = new Rectangle(
        // player.getX(),
        // player.getY(),
        // player.getWidth(),
        // player.getHeight());

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

        // Obstacles
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isVisible()) {
                obstacle.act();

                // Check collision with player using Rectangle collision detection
                if (player.isVisible() && !player.isInvulnerable()) {

                    // Rectangle obstacleRect = new Rectangle(
                    // obstacle.getX(),
                    // obstacle.getY(),
                    // obstacle.getWidth(),
                    // obstacle.getHeight());

                    // // Check if rectangles intersect
                    // if (playerRect.intersects(obstacleRect)) {
                    // // Player collided with obstacle
                    // int damage = 1; // Default damage
                    // if (obstacle instanceof Tumbleweed) {
                    // damage = 1; // Tumbleweed does 1 damage
                    // } else if (obstacle instanceof Cactus) {
                    // damage = 2; // Cactus does 2 damage

                    // }
                    if (obstacle.collidesWith(player)) {
                        int damage = 1; // Default damage
                        if (obstacle instanceof Tumbleweed) {
                            damage = 1; // Tumbleweed does 1 damage
                        } else if (obstacle instanceof Cactus) {
                            damage = 2; // Cactus does 2 damage
                        }

                        player.takeDamage(damage);
                        // play taking damage sound
                        long now = System.currentTimeMillis();
                        if (damageSound != null  && now - lastDamageSoundTime > 500) { // 500ms cooldown
                            damageSound.stop(); // Stop if already playing, for rapid hits
                            damageSound.play();
                            lastDamageSoundTime = now;
                        }

                        System.out.println(
                                "Player hit " + obstacle.getClass().getSimpleName() + " for " + damage + " damage!");
                        // System.out.println("Player Rect: " + playerRect);
                        // System.out.println("Obstacle Rect: " + obstacleRect);
                    }
                }
            }
        }

        // // enemies
        // for (Enemy enemy : enemies) {
        // int x = enemy.getX();
        // if (x >= BOARD_WIDTH - BORDER_RIGHT && direction != -1) {
        // direction = -1;
        // for (Enemy e2 : enemies) {
        // e2.setY(e2.getY() + GO_DOWN);
        // }
        // }
        // if (x <= BORDER_LEFT && direction != 1) {
        // direction = 1;
        // for (Enemy e : enemies) {
        // e.setY(e.getY() + GO_DOWN);
        // }
        // }
        // }
        // for (Enemy enemy : enemies) {
        // if (enemy.isVisible()) {
        // // Enemy invasion check moved to checkGameOver()
        // enemy.act(direction);
        // }
        // }
        // bombs - collision detection
        // Bomb is with enemy, so it loops over enemies
        for (Enemy enemy : enemies) {

            int chance = randomizer.nextInt(15);
            Bomb bomb = enemy.getBomb();

            if (chance == CHANCE && enemy.isVisible() && bomb.isDestroyed()) {

                bomb.setDestroyed(false);
                bomb.setX(enemy.getX());
                bomb.setY(enemy.getY());
            }

            // Use Rectangle collision detection for bombs
            if (player.isVisible() && !bomb.isDestroyed()) {
                // Create rectangles for bombs

                // Rectangle bombRect = new Rectangle(
                // bomb.getX(),
                // bomb.getY(),
                // bomb.getWidth(),
                // bomb.getHeight());

                // // Check if rectangles intersect
                // if (playerRect.intersects(bombRect)) {
                // // Instead of instant game over, damage the player
                // if (!player.isInvulnerable()) {
                // player.takeDamage(2); // Bombs do 2 HP damage
                // System.out.println("Player hit by enemy bomb for 2 damage!");
                // System.out.println("Player Rect: " + playerRect);
                // System.out.println("Bomb Rect: " + bombRect);
                // }
                // bomb.setDestroyed(true);
                // }

                if (bomb.collidesWith(player)) {
                    // Instead of instant game over, damage the player
                    if (!player.isInvulnerable()) {
                        player.takeDamage(2); // Bombs do 2 HP damage
                        // play taking damage sound

                        long now = System.currentTimeMillis();
                        if (damageSound != null  && now - lastDamageSoundTime > 500) { // 500ms cooldown
                            damageSound.stop(); // Stop if already playing, for rapid hits
                            damageSound.play();
                            lastDamageSoundTime = now;
                        }
                        System.out.println("Player hit by enemy bomb for 2 damage!");
                    }
                    bomb.setDestroyed(true);
                }
            }

            if (!bomb.isDestroyed()) {
                bomb.setX(bomb.getX() - 4); // bombs fall left and fast
                if (bomb.getY() >= GROUND - BOMB_HEIGHT) {
                    bomb.setDestroyed(true);
                }
            }
        }
    }

    private void doGameCycle() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        // Only increment frame when not paused
        if (!isPaused) {
            frame++;
        }
        update();
        repaint();
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                doGameCycle();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private class TAdapter extends KeyAdapter {

        @Override
        public void keyReleased(KeyEvent e) {
            // Don't process key releases when paused
            if (isPaused) {
                return;
            }
            player.keyReleased(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            System.out.println("Scene2.keyPressed: " + e.getKeyCode());

            int key = e.getKeyCode();

            if (!inGame) {
                if (isGameWon) {
                    // Handle stage complete input
                    if (key == KeyEvent.VK_SPACE) {
                        // Transition to cutscene
                        transitionToCutscene();
                    } else if (key == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                } else {
                    // Handle game over input
                    if (key == KeyEvent.VK_SPACE) {
                        restartGame();
                    } else if (key == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                }
                return;
            }

            // Toggle pause when P is pressed
            if (key == KeyEvent.VK_P) {
                togglePause();
                return;
            } else if (key == KeyEvent.VK_SPACE && isPaused) {
                togglePause();
                return;
            }

            // Don't process other keys when paused
            if (isPaused) {
                return;
            }

            player.keyPressed(e);
        }
    }

    private void transitionToCutscene() {
        System.out.println("Transitioning to cutscene...");

        // Switch to cutscene in the main game window
        if (game != null) {
            game.loadCutscene();
        }
    }

    private void checkStage1Complete() {
        // Check if player has completed Stage 1
        if (deaths == NUMBER_OF_ROBOTS_TO_DESTROY) {
            // if (frame >= 3000) { // debug condition for stage completion
            isGameWon = true;
            inGame = false;
            gameOverMessage = "Stage 1 Complete!";

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

            System.out.println("Stage 1 complete!");
        }
    }

    private void checkGameOver() {
        // Check if player is dead
        if (player.isDying()) {
            player.die();
            triggerGameOver("Game Over");
            return;
        }

        // Check victory condition
        if (deaths == NUMBER_OF_ROBOTS_TO_DESTROY) {
            triggerGameOver("Game Won!");
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
                        player.takeDamage(2);
                        // play taking damage sound
                        long now = System.currentTimeMillis();
                        if (damageSound != null  && now - lastDamageSoundTime > 500) { // 500ms cooldown
                            damageSound.stop(); // Stop if already playing, for rapid hits
                            damageSound.play();
                            lastDamageSoundTime = now;
                        }
                    }

                }
            }
        }
    }

    private void triggerGameOver(String endMessage) {
        inGame = false;
        gameOverMessage = endMessage;

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

        // Play game over sound effect
        try {
            AudioPlayer gameOverSound = new AudioPlayer("src/audio/over.wav");
            gameOverSound.play();
        } catch (Exception e) {
            System.err.println("Game over sound error: " + e.getMessage());
        }

        // Print message for debugging
        System.out.println("Game Over: " + endMessage);
    }

    private void restartGame() {
        // Reset game state
        inGame = true;
        isPaused = false; // Reset pause state
        isGameWon = false; // Reset win state
        frame = 0;
        deaths = 0;
        direction = -1;
        resetGlobalSpeed();
        scrollSpeed = defaultScrollSpeed; // Reset scroll speed

        // Reset spawn counters
        tumbleweedSpawnCounter = 0;
        cactusSpawnCounter = 0;
        robotSpawnCounter = 0;
        speedupCounter = 0;
        shieldCounter = 0;

        // Clear all game objects
        if (enemies != null)
            enemies.clear();
        if (powerups != null)
            powerups.clear();
        if (explosions != null)
            explosions.clear();
        if (obstacles != null)
            obstacles.clear();

        // Reinitialize game
        gameInit();

        // Reset player HP
        player.resetHP();

        initAudio();

        // Restart timer
        if (timer != null) {
            timer.start();
        }
    }

    private void togglePause() {
        isPaused = !isPaused;
        System.out.println("Pause toggled: isPaused = " + isPaused);

        if (isPaused) {
            // Don't stop the timer - let it continue for repainting
            // Just pause audio
            try {
                if (audioPlayer != null) {
                    audioPlayer.pause();
                }
            } catch (Exception e) {
                System.err.println("Error pausing audio: " + e.getMessage());
            }
        } else {
            // Resume audio
            try {
                if (audioPlayer != null) {
                    audioPlayer.resumeAudio();
                }
            } catch (Exception e) {
                System.err.println("Error resuming audio: " + e.getMessage());
            }
        }

        // Force a repaint to show the pause overlay immediately
        repaint();
    }

}
