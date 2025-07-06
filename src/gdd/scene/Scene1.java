package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.SpawnDetails;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Enemy;
import gdd.sprite.Enemy.Bomb;
import gdd.sprite.Explosion;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
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

public class Scene1 extends JPanel {

    // state overlay
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private boolean isGameWon = false;

    private int frame = 0;
    private List<PowerUp> powerups;
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<Shot> shots;
    private Player player;
    // private Shot shot;

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
    private final int[][] MAP = {
            { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
            { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }
    };

    private HashMap<Integer, SpawnDetails> spawnMap = new HashMap<>();
    private AudioPlayer audioPlayer;
    private int lastRowToShow;
    private int firstRowToShow;

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
        spawnMap.put(50, new SpawnDetails("PowerUp-SpeedUp", 100, 0));
        spawnMap.put(200, new SpawnDetails("Alien1", 200, 0));
        spawnMap.put(300, new SpawnDetails("Alien1", 300, 0));

        spawnMap.put(400, new SpawnDetails("Alien1", 400, 0));
        spawnMap.put(401, new SpawnDetails("Alien1", 450, 0));
        spawnMap.put(402, new SpawnDetails("Alien1", 500, 0));
        spawnMap.put(403, new SpawnDetails("Alien1", 550, 0));

        spawnMap.put(500, new SpawnDetails("Alien1", 100, 0));
        spawnMap.put(501, new SpawnDetails("Alien1", 150, 0));
        spawnMap.put(502, new SpawnDetails("Alien1", 200, 0));
        spawnMap.put(503, new SpawnDetails("Alien1", 350, 0));
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
        shots = new ArrayList<>();

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

    private void drawPowreUps(Graphics g) {

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

        // for (Enemy e : enemies) {
        // Enemy.Bomb b = e.getBomb();
        // if (!b.isDestroyed()) {
        // g.drawImage(b.getImage(), b.getX(), b.getY(), this);
        // }
        // }
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
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        g.setColor(Color.black);
        g.fillRect(0, 0, d.width, d.height);

        g.setColor(Color.white);
        g.drawString("FRAME: " + frame, 10, 10);

        g.setColor(Color.green);

        if (inGame) {
            drawBackground(g);
            drawExplosions(g);
            drawPowreUps(g);
            drawAliens(g);
            drawPlayer(g);
            drawShot(g);
            
            if (isPaused) {
                System.out.println("isPaused is true, calling drawPauseOverlay");
                drawPauseOverlay(g);
            }
        } else {
            drawGameOver(g);
        }

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

        // shot
        List<Shot> shotsToRemove = new ArrayList<>();
        for (Shot shot : shots) {

            if (shot.isVisible()) {
                int shotX = shot.getX();
                int shotY = shot.getY();

                for (Enemy enemy : enemies) {
                    // Collision detection: shot and enemy
                    int enemyX = enemy.getX();
                    int enemyY = enemy.getY();

                    if (enemy.isVisible() && shot.isVisible()
                            && shotX >= (enemyX)
                            && shotX <= (enemyX + ALIEN_WIDTH)
                            && shotY >= (enemyY)
                            && shotY <= (enemyY + ALIEN_HEIGHT)) {

                        var ii = new ImageIcon(IMG_EXPLOSION);
                        enemy.setImage(ii.getImage());
                        enemy.setDying(true);
                        explosions.add(new Explosion(enemyX, enemyY));
                        deaths++;
                        shot.die();
                        shotsToRemove.add(shot);
                    }
                }

                int y = shot.getY();
                // y -= 4;
                y -= 20;

                if (y < 0) {
                    shot.die();
                    shotsToRemove.add(shot);
                } else {
                    shot.setY(y);
                }
            }
        }
        shots.removeAll(shotsToRemove);

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

            int bombX = bomb.getX();
            int bombY = bomb.getY();
            int playerX = player.getX();
            int playerY = player.getY();

            if (player.isVisible() && !bomb.isDestroyed()
                    && bombX >= (playerX)
                    && bombX <= (playerX + PLAYER_WIDTH)
                    && bombY >= (playerY)
                    && bombY <= (playerY + PLAYER_HEIGHT)) {

                var ii = new ImageIcon(IMG_EXPLOSION);
                player.setImage(ii.getImage());
                player.setDying(true);
                bomb.setDestroyed(true);
            }

            if (!bomb.isDestroyed()) {
                bomb.setY(bomb.getY() + 1);
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

            int x = player.getX();
            int y = player.getY();

            if (key == KeyEvent.VK_SPACE && inGame) {
                System.out.println("Shots: " + shots.size());
                if (shots.size() < 4) {
                    // Create a new shot and add it to the list
                    Shot shot = new Shot(x, y);
                    shots.add(shot);
                }
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
        if (shots != null)
            shots.clear();

        // Reinitialize game
        gameInit();
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
