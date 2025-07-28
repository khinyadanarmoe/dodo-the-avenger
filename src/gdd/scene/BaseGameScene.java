package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;
import gdd.sprite.Player;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Base class for all game scenes providing common functionality like:
 * - Player management
 * - HP bar rendering
 * - Pause system
 * - Game over/victory screens
 * - Audio management
 * - Timer and frame management
 */
public abstract class BaseGameScene extends JPanel {

    // Core game state
    protected int frame = 0;
    protected Timer timer;
    protected final Game game;
    protected AudioPlayer audioPlayer;

    // Player and game state
    protected Player player;
    protected boolean inGame = true;
    protected boolean isPaused = false;
    protected boolean isGameOver = false;
    protected boolean isVictory = false;
    protected String gameOverMessage = "Game Over";

    public BaseGameScene(Game game) {
        this.game = game;
    }

    // Abstract methods that subclasses must implement
    protected abstract void gameInit();

    protected abstract void update();

    protected abstract void drawScene(Graphics g);

    protected abstract String getAudioFilePath();

    protected abstract KeyAdapter createKeyAdapter();

    // Common initialization
    public void start() {
        addKeyListener(createKeyAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new BaseGameCycle());
        timer.start();

        gameInit();
        initAudio();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error stopping audio: " + e.getMessage());
        }
    }

    protected void initAudio() {
        try {
            String filePath = getAudioFilePath();
            audioPlayer = new AudioPlayer(filePath, true); // Looping audio
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing audio: " + e.getMessage());
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        if (inGame) {
            drawScene(g);
            drawHUD(g);

            if (isPaused) {
                drawPauseOverlay(g);
            }
        } else if (isVictory) {
            drawVictoryScreen(g);
        } else {
            drawGameOverScreen(g);
        }

        // Debug frame counter
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString(getSceneName() + " - FRAME: " + frame, 10, 20);

        Toolkit.getDefaultToolkit().sync();
    }

    // Override this in subclasses to customize HUD
    protected void drawHUD(Graphics g) {
        drawHPBar(g);
    }

    // Common HP bar implementation
    protected void drawHPBar(Graphics g) {
        if (player == null)
            return;

        // HP Bar dimensions and position (top right)
        int barWidth = 200;
        int barHeight = 20;
        int barX = BOARD_WIDTH - barWidth - 20;
        int barY = 80;

        // Get player HP
        int currentHP = player.getCurrentHP();
        int maxHP = player.getMaxHP();

        // Calculate HP percentage
        double hpPercentage = (double) currentHP / maxHP;
        int fillWidth = (int) (barWidth * hpPercentage);

        // Draw HP bar background
        g.setColor(new Color(50, 50, 50));
        g.fillRect(barX, barY, barWidth, barHeight);

        // Draw HP bar border
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barWidth, barHeight);

        // Draw HP fill based on percentage
        if (hpPercentage > 0.6) {
            g.setColor(Color.GREEN);
        } else if (hpPercentage > 0.3) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.RED);
        }

        if (fillWidth > 0) {
            g.fillRect(barX + 1, barY + 1, fillWidth - 2, barHeight - 2);
        }

        // Draw HP text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String hpText = "DODO HP: " + currentHP + "/" + maxHP;
        g.drawString(hpText, barX, barY + barHeight + 15);

        // Draw shield indicator
        if (player.isShieldActive()) {
            g.setColor(Color.BLUE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String shieldText = "SHIELD: " + String.format("%.1f", player.getShieldTimer() / 60.0f) + "s";
            g.drawString(shieldText, barX, barY - 5);

            // Draw shield border around HP bar
            g.setColor(Color.BLUE);
            g.drawRect(barX - 2, barY - 2, barWidth + 4, barHeight + 4);
        }
    }

    // Common pause overlay
    protected void drawPauseOverlay(Graphics g) {
        // Semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        // Pause box
        g.setColor(new Color(0, 32, 48));
        g.fillRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);

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

    // Common victory screen
    protected void drawVictoryScreen(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(0, 128, 0)); // Green background
        g.fillRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);

        var large = new Font("Helvetica", Font.BOLD, 36);
        var fontMetrics = this.getFontMetrics(large);
        g.setColor(Color.YELLOW);
        g.setFont(large);
        String victoryText = "VICTORY!";
        g.drawString(victoryText, (BOARD_WIDTH - fontMetrics.stringWidth(victoryText)) / 2,
                BOARD_HEIGHT / 2 - 10);

        g.setFont(new Font("Helvetica", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        g.drawString(getVictoryMessage(), BOARD_WIDTH / 2 - 80, BOARD_HEIGHT / 2 + 20);
        g.drawString("Press SPACE to continue", BOARD_WIDTH / 2 - 70, BOARD_HEIGHT / 2 + 40);
    }

    // Common game over screen
    protected void drawGameOverScreen(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(128, 0, 0)); // Red background
        g.fillRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);
        g.setColor(Color.white);
        g.drawRect(50, BOARD_HEIGHT / 2 - 60, BOARD_WIDTH - 100, 120);

        var large = new Font("Helvetica", Font.BOLD, 36);
        var fontMetrics = this.getFontMetrics(large);
        g.setColor(Color.WHITE);
        g.setFont(large);
        g.drawString(gameOverMessage, (BOARD_WIDTH - fontMetrics.stringWidth(gameOverMessage)) / 2,
                BOARD_HEIGHT / 2 - 10);

        g.setFont(new Font("Helvetica", Font.PLAIN, 14));
        g.drawString("Press SPACE to restart", BOARD_WIDTH / 2 - 70, BOARD_HEIGHT / 2 + 20);
        g.drawString("Press ESC to quit", BOARD_WIDTH / 2 - 55, BOARD_HEIGHT / 2 + 40);
    }

    // Common pause functionality
    protected void togglePause() {
        isPaused = !isPaused;
        System.out.println(getSceneName() + " pause toggled: isPaused = " + isPaused);

        if (isPaused) {
            try {
                if (audioPlayer != null) {
                    audioPlayer.pause();
                }
            } catch (Exception e) {
                System.err.println("Error pausing audio: " + e.getMessage());
            }
        } else {
            try {
                if (audioPlayer != null) {
                    audioPlayer.resumeAudio();
                }
            } catch (Exception e) {
                System.err.println("Error resuming audio: " + e.getMessage());
            }
        }

        repaint();
    }

    // Common victory trigger
    protected void triggerVictory() {
        inGame = false;
        isVictory = true;

        try {
            AudioPlayer winSound = new AudioPlayer("src/audio/win.wav");
            winSound.play();
        } catch (Exception e) {
            System.err.println("Victory sound error: " + e.getMessage());
        }

        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error stopping audio: " + e.getMessage());
        }

        System.out.println(getSceneName() + " victory!");
    }

    // Common game over trigger
    protected void triggerGameOver(String message) {
        inGame = false;
        isGameOver = true;
        isVictory = false;
        gameOverMessage = message;

        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        // Play game over sound
        try {
            AudioPlayer gameOverSound = new AudioPlayer("src/audio/over.wav");
            gameOverSound.play();
        } catch (Exception e) {
            System.err.println("Game over sound error: " + e.getMessage());
        }

        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            }
        } catch (Exception e) {
            System.err.println("Error stopping audio: " + e.getMessage());
        }

        System.out.println("Game Over: " + message);
    }

    // Common restart functionality
    protected void restartScene() {
        inGame = true;
        isPaused = false;
        isGameOver = false;
        isVictory = false;
        frame = 0;

        gameInit();
        initAudio();

        if (timer != null) {
            timer.start();
        }
    }

    // Methods for subclasses to override
    protected String getSceneName() {
        return "SCENE";
    }

    protected String getVictoryMessage() {
        return "Game Won!";
    }

    // Common game cycle
    private class BaseGameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isPaused && inGame) {
                frame++;
                update();
            }
            repaint();
        }
    }

    // Common key handling for pause and end game states
    protected boolean handleCommonKeys(KeyEvent e) {
        int key = e.getKeyCode();

        if (!inGame) {
            // Handle end game input
            if (key == KeyEvent.VK_SPACE) {
                handleEndGameSpace();
                return true;
            } else if (key == KeyEvent.VK_ESCAPE) {
                System.exit(0);
                return true;
            }
            return true; // Consume all input when not in game
        }

        // Toggle pause
        if (key == KeyEvent.VK_P) {
            togglePause();
            return true;
        }

        if (isPaused) {
            return true; // Consume all input when paused
        }

        return false; // Let subclass handle the key
    }

    // Override this in subclasses to handle space key in end game state
    protected void handleEndGameSpace() {
        if (isVictory) {
            // Default: restart scene
            restartScene();
        } else {
            // Default: restart scene
            restartScene();
        }
    }
}
