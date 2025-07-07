package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Cactus;
import gdd.sprite.Enemy;
import gdd.sprite.Enemy.Bomb;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Tumbleweed;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import gdd.sprite.Obstacle;

public class Scene1 extends JPanel {

    // state overlay
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private boolean isGameWon = false;
    

    private int frame = 0;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private Player player;

    //obstacles
    private List<Obstacle> obstacles;

    final int BLOCKHEIGHT = 50;
    final int BLOCKWIDTH = 50;

    final int BLOCKS_TO_DRAW = BOARD_HEIGHT / BLOCKHEIGHT;

    private int direction = -1;
    private int deaths = 0;

    private boolean inGame = true;
    private String gameOverMessage = "Game Over";

    private final Dimension d = new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    private final Random randomizer = new Random();

    private Timer timer;
    private final Game game;

    private int currentRow = -1;
    // TODO load this map from a file
    private int mapOffset = 0;
    

    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private AudioPlayer audioPlayer;


    public Scene1(Game game) {
        this.game = game;
        // initBoard();
        // gameInit();
        loadSpawnDetails();
    }

    private void initAudio() {
        try {
            String filePath = "src/audio/scene1.wav";
            audioPlayer = new AudioPlayer(filePath);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing audio player: " + e.getMessage());
        }
    }

    private void loadSpawnDetails() {
        // TODO load this from a file
        // For side-scrolling: spawn from right side (BOARD_WIDTH) and move left

        //obstacles
        spawnMap.put(50, new SpawnDetails("Tumbleweed", BOARD_WIDTH, GROUND )); // Adjust for tumbleweed height
        spawnMap.put(100, new SpawnDetails("Cactus", BOARD_WIDTH, GROUND )); // Adjust for cactus height

        spawnMap.put(110, new SpawnDetails("PowerUp-SpeedUp", BOARD_WIDTH, 300));
        spawnMap.put(120, new SpawnDetails("Alien1", BOARD_WIDTH, 100));
        spawnMap.put(130, new SpawnDetails("Alien1", BOARD_WIDTH, 150));

        spawnMap.put(400, new SpawnDetails("Alien1", BOARD_WIDTH, 50));
        spawnMap.put(401, new SpawnDetails("Alien1", BOARD_WIDTH, 100));
        spawnMap.put(402, new SpawnDetails("Alien1", BOARD_WIDTH, 150));
        spawnMap.put(403, new SpawnDetails("Alien1", BOARD_WIDTH, 200));

        spawnMap.put(500, new SpawnDetails("Alien1", BOARD_WIDTH, 80));
        spawnMap.put(501, new SpawnDetails("Alien1", BOARD_WIDTH, 120));
        spawnMap.put(502, new SpawnDetails("Alien1", BOARD_WIDTH, 160));
        spawnMap.put(503, new SpawnDetails("Alien1", BOARD_WIDTH, 200));

        // Debug: Print spawn map contents
        System.out.println("Spawn map loaded with " + spawnMap.size() + " entries:");
        for (Integer frame : spawnMap.keySet()) {
            SpawnDetails sd = spawnMap.get(frame);
            System.out.println("  Frame " + frame + ": " + sd.type + " at (" + sd.x + ", " + sd.y + ")");
        }
    }

    private void initBoard() {

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

        // for (int i = 0; i < 4; i++) {
        // for (int j = 0; j < 6; j++) {
        // var enemy = new Enemy(ALIEN_INIT_X + (ALIEN_WIDTH + ALIEN_GAP) * j,
        // ALIEN_INIT_Y + (ALIEN_HEIGHT + ALIEN_GAP) * i);
        // enemies.add(enemy);
        // }
        // }
        player = new Player();
        // shot = new Shot();
    }

    private void drawBackground(Graphics g) {
        ImageIcon ii = new ImageIcon(IMG_BACKGROUND);
        int bgWidth = BOARD_WIDTH; // scale width to fit board
        int bgHeight = BOARD_HEIGHT; // scale height to fit board

        // Dynamic scroll speed
        int scrollSpeed = 2;
        int scrollOffset = (frame * scrollSpeed) % bgWidth;

        int x1 = -scrollOffset;
        int x2 = x1 + bgWidth;

        if (inGame) {
            g.drawImage(ii.getImage(), x1, 0, bgWidth, bgHeight, this);
            g.drawImage(ii.getImage(), x2, 0, bgWidth, bgHeight, this);
        }
    }

    private void drawAliens(Graphics g) {

        for (Enemy enemy : enemies) {

            if (enemy.isVisible()) {

                g.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
            }

            if (enemy.isDying()) {

                enemy.die();
            }
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
            g.drawImage(player.getImage(), player.getX(), player.getY(), this);
            
            // Debug: Draw player bounds
            g.setColor(Color.BLUE);
            g.drawRect(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        }

        if (player.isDying()) {
            // Player death handled in checkGameOver()
        }
    }

    private void drawBombing(Graphics g) {

        for (Enemy e : enemies) {
            Enemy.Bomb b = e.getBomb();
            if (!b.isDestroyed()) {
                g.drawImage(b.getImage(), b.getX(), b.getY(), this);
                
                // Debug: Draw bomb bounds
                g.setColor(Color.ORANGE);
                g.drawRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
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
                
                // Debug: Draw obstacle bounds
                g.setColor(Color.RED);
                g.drawRect(obstacle.getX(), obstacle.getY(), obstacle.getWidth(), obstacle.getHeight());
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
            drawBackground(g);
            drawExplosions(g);
            drawObstacles(g);
            drawPowerUps(g);
            drawAliens(g);
            drawBombing(g); // Draw enemy bombs
            drawPlayer(g);
            drawHPBar(g); // Draw HP bar
            
            if (isPaused) {
                System.out.println("isPaused is true, calling drawPauseOverlay");
                drawPauseOverlay(g);
            }
        } else {
            drawGameOver(g);
        }

        // g.setColor(Color.black);
        // g.fillRect(0, 0, d.width, d.height);

        g.setColor(Color.white);
        g.drawString("FRAME: " + frame, 10, 10);
        g.setColor(Color.green);

        Toolkit.getDefaultToolkit().sync();
    }



    private void drawGameOver(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(0, 32, 48));
        g.fillRect(50, BOARD_HEIGHT / 2 - 30, BOARD_WIDTH - 100, 50);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_HEIGHT / 2 - 30, BOARD_WIDTH - 100, 50);

        var large = new Font("Helvetica", Font.BOLD, 36);
        var fontMetrics = this.getFontMetrics(large);

        g.setColor(Color.white);
        g.setFont(large);
        g.drawString(gameOverMessage, (BOARD_WIDTH - fontMetrics.stringWidth(gameOverMessage)) / 2,
                BOARD_HEIGHT / 2);

        // Draw instructions
        g.setFont(new Font("Helvetica", Font.PLAIN, 12));
        g.drawString("Press SPACE to restart", BOARD_WIDTH / 2 - 60, BOARD_HEIGHT / 2 + 30);
        g.drawString("Press ESC to quit", BOARD_WIDTH / 2 - 50, BOARD_HEIGHT / 2 + 50);
    }

    private void drawPauseOverlay(Graphics g) {
        System.out.println("Drawing pause overlay");
        
        // Semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // Pause box
        g.setColor(new Color(0, 32, 48));
        g.fillRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);
        g.drawImage(new ImageIcon(IMG_PAUSE).getImage(), 50, BOARD_HEIGHT / 2 - 200, this);
        

        // Pause text
        var large = new Font("Helvetica", Font.BOLD, 36);
        var fontMetrics = this.getFontMetrics(large);
        g.setColor(Color.white);
        g.setFont(large);
        String pauseText = "PAUSED";
        g.drawString(pauseText, (BOARD_WIDTH - fontMetrics.stringWidth(pauseText)) / 2,
                BOARD_HEIGHT / 2 - 20);

        // Instructions
        g.setFont(new Font("Helvetica", Font.PLAIN, 14));
        g.drawString("Press P to resume", BOARD_WIDTH / 2 - 60, BOARD_HEIGHT / 2 + 20);
        g.drawString("Press ESC to quit", BOARD_WIDTH / 2 - 55, BOARD_HEIGHT / 2 + 40);
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
        
        // Debug: Draw player position info
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        String posText = "Player: (" + player.getX() + "," + player.getY() + ") " + 
                        player.getWidth() + "x" + player.getHeight();
        g.drawString(posText, 10, 30);
    }

    private void update() {

        // Check game over conditions first
        checkGameOver();

        if (!inGame || isPaused) {
            return; // Don't update if game is over or paused
        }

        // Check enemy spawn
        // TODO this approach can only spawn one enemy at a frame
        SpawnDetails sd = spawnMap.get(frame);
        if (sd != null) {
            System.out.println("Spawning at frame " + frame + ": " + sd.type + " at (" + sd.x + ", " + sd.y + ")");
            // Create a new enemy based on the spawn details
            switch (sd.type) {
                case "Alien1":
                    Enemy enemy = new Alien1(sd.x, sd.y);
                    enemies.add(enemy);
                    break;
                // Add more cases for different enemy types if needed
                case "Alien2":
                    // Enemy enemy2 = new Alien2(sd.x, sd.y);
                    // enemies.add(enemy2);
                    break;
                case "PowerUp-SpeedUp":
                    // Handle speed up item spawn
                    PowerUp speedUp = new SpeedUp(sd.x, sd.y);
                    powerups.add(speedUp);
                    break;
                
                //obstacles
                case "Tumbleweed":
                    System.out.println("Creating Tumbleweed at (" + sd.x + ", " + sd.y + ")");
                    Obstacle tumbleweed = new Tumbleweed(sd.x, sd.y);
                    obstacles.add(tumbleweed);
                    System.out.println("Tumbleweed added. Total obstacles: " + obstacles.size());
                    break;
                case "Cactus":
                    Obstacle cactus = new Cactus(sd.x, sd.y);
                    obstacles.add(cactus);
                    break;
                default:
                    System.out.println("Unknown enemy type: " + sd.type);
                    break;
            }
        }

        checkGameOver();

        // player
        player.act();

        // Power-ups
        for (PowerUp powerup : powerups) {
            if (powerup.isVisible()) {
                powerup.act();
                if (powerup.collidesWith(player)) {
                    powerup.upgrade(player);
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
                    // Create rectangles for player and obstacle
                    Rectangle playerRect = new Rectangle(
                        player.getX(), 
                        player.getY(), 
                        player.getWidth(), 
                        player.getHeight()
                    );
                    
                    Rectangle obstacleRect = new Rectangle(
                        obstacle.getX(), 
                        obstacle.getY(), 
                        obstacle.getWidth(), 
                        obstacle.getHeight()
                    );
                    
                    // Check if rectangles intersect
                    if (playerRect.intersects(obstacleRect)) {
                        // Player collided with obstacle
                        int damage = 1; // Default damage
                        if (obstacle instanceof Tumbleweed) {
                            damage = 1; // Tumbleweed does 1 damage
                        } else if (obstacle instanceof Cactus) {
                            damage = 2; // Cactus does 2 damage
                        }
                        
                        player.takeDamage(damage);
                        System.out.println("Player hit " + obstacle.getClass().getSimpleName() + " for " + damage + " damage!");
                        System.out.println("Player Rect: " + playerRect);
                        System.out.println("Obstacle Rect: " + obstacleRect);
                    }
                }
            }
        }

        // enemies
        for (Enemy enemy : enemies) {
            int x = enemy.getX();
            if (x >= BOARD_WIDTH - BORDER_RIGHT && direction != -1) {
                direction = -1;
                for (Enemy e2 : enemies) {
                    e2.setY(e2.getY() + GO_DOWN);
                }
            }
            if (x <= BORDER_LEFT && direction != 1) {
                direction = 1;
                for (Enemy e : enemies) {
                    e.setY(e.getY() + GO_DOWN);
                }
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                // Enemy invasion check moved to checkGameOver()
                enemy.act(direction);
            }
        }
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
                // Create rectangles for player and bomb
                Rectangle playerRect = new Rectangle(
                    player.getX(), 
                    player.getY(), 
                    player.getWidth(), 
                    player.getHeight()
                );
                
                Rectangle bombRect = new Rectangle(
                    bomb.getX(), 
                    bomb.getY(), 
                    bomb.getWidth(), 
                    bomb.getHeight()
                );
                
                // Check if rectangles intersect
                if (playerRect.intersects(bombRect)) {
                    // Instead of instant game over, damage the player
                    if (!player.isInvulnerable()) {
                        player.takeDamage(2); // Bombs do 2 HP damage
                        System.out.println("Player hit by enemy bomb for 2 damage!");
                        System.out.println("Player Rect: " + playerRect);
                        System.out.println("Bomb Rect: " + bombRect);
                    }
                    bomb.setDestroyed(true);
                }
            }

            if (!bomb.isDestroyed()) {
                bomb.setX(bomb.getX() - 4);
                if (bomb.getY() >= GROUND - BOMB_HEIGHT) {
                    bomb.setDestroyed(true);
                }
            }
        }
    }

    private void doGameCycle() {
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
            doGameCycle();
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
                // Handle game over input
                if (key == KeyEvent.VK_SPACE) {
                    restartGame();
                } else if (key == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                return;
            }

            // Toggle pause when P is pressed
            if (key == KeyEvent.VK_P) {
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

    private void checkGameOver() {
        // Check if player is dead
        if (player.isDying()) {
            player.die();
            triggerGameOver("Game Over");
            return;
        }

        // Check victory condition
        if (deaths == NUMBER_OF_ALIENS_TO_DESTROY) {
            triggerGameOver("Game Won!");
            return;
        }

        // Check enemy invasion (enemies reach the ground)
        for (Enemy enemy : enemies) {
            if (enemy.isVisible()) {
                int y = enemy.getY();
                if (y > GROUND - ALIEN_HEIGHT) {
                    triggerGameOver("Invasion!");
                    return;
                }
            }
        }

        // Check enemy-player collision (direct contact = game over)
        for (Enemy enemy : enemies) {
            if (enemy.isVisible() && player.isVisible()) {
                int enemyX = enemy.getX();
                int enemyY = enemy.getY();
                int playerX = player.getX();
                int playerY = player.getY();

                // Check if enemy and player are colliding
                if (enemyX < playerX + PLAYER_WIDTH &&
                        enemyX + ALIEN_WIDTH > playerX &&
                        enemyY < playerY + PLAYER_HEIGHT &&
                        enemyY + ALIEN_HEIGHT > playerY) {

                    // Enemy touched player directly - Game Over
                    var ii = new ImageIcon(IMG_EXPLOSION);
                    player.setImage(ii.getImage());
                    player.setDying(true);
                    triggerGameOver("Game Over");
                    return;
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

        // Print message for debugging
        System.out.println("Game Over: " + endMessage);
    }

    private void restartGame() {
        // Reset game state
        inGame = true;
        isPaused = false; // Reset pause state
        frame = 0;
        deaths = 0;
        direction = -1;

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
