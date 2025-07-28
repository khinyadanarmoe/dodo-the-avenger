package gdd.scene;

import gdd.AudioPlayer;
import gdd.Game;
import static gdd.Global.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Cutscene extends JPanel {

    private int frame = 0;
    private Timer timer;
    private AudioPlayer audioPlayer;
    
    // Dialogue system
    private String[] dialogueLines = {
        "Now you are going in pyramid",
        "Ready to fight the boss"
    };
    
    private int currentDialogueIndex = 0;
    private int dialogueCharIndex = 0;
    private String currentDisplayText = "";
    private boolean dialogueComplete = false;
    private boolean cutsceneComplete = false;
    
    // Timing constants
    private static final float CHARS_PER_FRAME = 0.1f; // Characters revealed per frame (adjust for speed)
    private static final int DIALOGUE_PAUSE_FRAMES = 20; // 2 seconds pause between lines
    private static final int CUTSCENE_END_PAUSE = 20; // 3 seconds pause at end
    
    private int pauseCounter = 0;
    private boolean isPaused = false;
    private final Game game;

    public Cutscene(Game game) {
        this.game = game;
    }

    private void initAudio() {
        try {
            // Use the same audio as Scene1 or add cutscene-specific audio
            String filePath = "src/audio/cutscene.wav";
            audioPlayer = new AudioPlayer(filePath);
            audioPlayer.play();
        } catch (Exception e) {
            System.err.println("Error initializing cutscene audio: " + e.getMessage());
        }
    }

    public void start() {
        addKeyListener(new CutsceneKeyAdapter());
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.black);

        timer = new Timer(1000 / 60, new CutsceneGameCycle());
        timer.start();

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
            System.err.println("Error stopping cutscene audio.");
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCutscene(g);
    }

    private void drawCutscene(Graphics g) {
        // Draw cutscene background
        ImageIcon cutsceneImage = new ImageIcon("src/dodoSprites/cutscene.png");
        g.drawImage(cutsceneImage.getImage(), 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);

        // Draw dialogue box
        drawDialogueBox(g);

        // Draw current dialogue text
        drawDialogueText(g);

        // Draw instructions
        drawInstructions(g);

        // Debug frame counter
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("FRAME: " + frame, 10, 20);

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawDialogueBox(Graphics g) {
        // Semi-transparent dialogue box at bottom
        int boxHeight = 120;
        int boxY = BOARD_HEIGHT - boxHeight - 20;
        
        // Background
        g.setColor(new Color(0, 0, 0, 180)); // Semi-transparent black
        g.fillRect(20, boxY, BOARD_WIDTH - 40, boxHeight);
        
        // Border
        g.setColor(Color.WHITE);
        g.drawRect(20, boxY, BOARD_WIDTH - 40, boxHeight);
        
        // Inner border for style
        g.setColor(new Color(100, 100, 100));
        g.drawRect(22, boxY + 2, BOARD_WIDTH - 44, boxHeight - 4);
    }

    private void drawDialogueText(Graphics g) {
        if (currentDisplayText.isEmpty()) {
            return;
        }

        int boxHeight = 120;
        int boxY = BOARD_HEIGHT - boxHeight - 20;
        int textX = 40;
        int textY = boxY + 40;

        // Set font for dialogue
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));

        // Draw the current text being animated
        FontMetrics fm = g.getFontMetrics();
        
        // Split text into words for word wrapping
        String[] words = currentDisplayText.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int lineY = textY;
        int lineHeight = fm.getHeight() + 5;

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            
            if (fm.stringWidth(testLine) <= BOARD_WIDTH - 80) {
                // Word fits on current line
                currentLine = new StringBuilder(testLine);
            } else {
                // Draw current line and start new line
                if (currentLine.length() > 0) {
                    g.drawString(currentLine.toString(), textX, lineY);
                    lineY += lineHeight;
                }
                currentLine = new StringBuilder(word);
            }
        }
        
        // Draw the last line
        if (currentLine.length() > 0) {
            g.drawString(currentLine.toString(), textX, lineY);
        }

        // Draw blinking cursor if dialogue is complete for current line
        if (dialogueCharIndex >= dialogueLines[currentDialogueIndex].length()) {
            if ((frame / 30) % 2 == 0) { // Blink every 0.5 seconds
                g.setColor(Color.YELLOW);
                g.drawString("▶", textX + fm.stringWidth(currentDisplayText) + 10, lineY);
            }
        }
    }

    private void drawInstructions(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        
        if (cutsceneComplete) {
            g.drawString("Press SPACE to continue to Boss Fight", BOARD_WIDTH / 2 - 120, 30);
        } else if (dialogueCharIndex >= dialogueLines[currentDialogueIndex].length()) {
            g.drawString("Press SPACE to continue", BOARD_WIDTH / 2 - 80, 30);
        } else {
            g.drawString("Press SPACE to skip text animation", BOARD_WIDTH / 2 - 100, 30);
        }
        
        g.drawString("Press ESC to skip cutscene", BOARD_WIDTH / 2 - 80, 50);
    }

    private void update() {
        frame++;

        if (cutsceneComplete) {
            return; // Cutscene finished, waiting for input
        }

        if (isPaused) {
            pauseCounter++;
            if (pauseCounter >= DIALOGUE_PAUSE_FRAMES) {
                isPaused = false;
                pauseCounter = 0;
                
                // Move to next dialogue line
                currentDialogueIndex++;
                if (currentDialogueIndex >= dialogueLines.length) {
                    // All dialogue complete
                    dialogueComplete = true;
                    pauseCounter = 0;
                    isPaused = true; // Pause before ending cutscene
                } else {
                    // Reset for next line
                    dialogueCharIndex = 0;
                    currentDisplayText = "";
                }
            }
            return;
        }

        if (dialogueComplete && pauseCounter < CUTSCENE_END_PAUSE) {
            pauseCounter++;
            if (pauseCounter >= CUTSCENE_END_PAUSE) {
                cutsceneComplete = true;
            }
            return;
        }

        // Animate current dialogue line
        if (currentDialogueIndex < dialogueLines.length) {
            String currentLine = dialogueLines[currentDialogueIndex];
            
            if (dialogueCharIndex < currentLine.length()) {
                // Add characters gradually
                for (int i = 0; i < CHARS_PER_FRAME && dialogueCharIndex < currentLine.length(); i++) {
                    dialogueCharIndex++;
                }
                currentDisplayText = currentLine.substring(0, dialogueCharIndex);
            } else {
                // Current line complete, wait for input or auto-advance
                if (frame % 180 == 0) { // Auto-advance after 3 seconds
                    advanceDialogue();
                }
            }
        }
    }

    private void advanceDialogue() {
        if (currentDialogueIndex < dialogueLines.length) {
            String currentLine = dialogueLines[currentDialogueIndex];
            
            if (dialogueCharIndex < currentLine.length()) {
                // Skip to end of current line
                dialogueCharIndex = currentLine.length();
                currentDisplayText = currentLine;
            } else {
                // Move to next line or end cutscene
                if (currentDialogueIndex < dialogueLines.length - 1) {
                    isPaused = true;
                    pauseCounter = 0;
                } else {
                    dialogueComplete = true;
                    pauseCounter = 0;
                }
            }
        }
    }

    private void skipCutscene() {
        cutsceneComplete = true;
        System.out.println("Cutscene skipped");
    }

    public boolean isCutsceneComplete() {
        return cutsceneComplete;
    }

    private class CutsceneGameCycle implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            update();
            repaint();
        }
    }

    private class CutsceneKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_SPACE) {
                if (cutsceneComplete) {
                    // Transition to FinalScene (Boss Fight)
                    System.out.println("Transitioning to Boss Fight...");
                    stop();
                    game.loadFinalScene();
                } else {
                    advanceDialogue();
                }
            } else if (key == KeyEvent.VK_ESCAPE) {
                skipCutscene();
            }
        }
    }
}
