package gdd;

public class Global {
    private Global() {
        // Prevent instantiation
    }

    public static final int SCALE_FACTOR = 2; // Scaling factor for sprites

    public static final int BOARD_WIDTH = 1280; // Doubled from 640
    public static final int BOARD_HEIGHT = 720; // Doubled from 360
    public static final int BORDER_RIGHT = 60; // Doubled from 30
    public static final int BORDER_LEFT = 10; // Doubled from 5

    public static final int GROUND = 580; // Doubled from 290
    public static final int BOMB_HEIGHT = 10; // Doubled from 5

    public static final int ALIEN_HEIGHT = 24; // Doubled from 12
    public static final int ALIEN_WIDTH = 24; // Doubled from 12
    public static final int ALIEN_INIT_X = 300; // Doubled from 150
    public static final int ALIEN_INIT_Y = 10; // Doubled from 5
    public static final int ALIEN_GAP = 30; // Gap between aliens

    public static final int GO_DOWN = 30; // Doubled from 15
    public static final int NUMBER_OF_ALIENS_TO_DESTROY = 24;
    public static final int CHANCE = 5;
    public static final int DELAY = 17;
    public static final int PLAYER_WIDTH = 15; // Doubled from 15
    public static final int PLAYER_HEIGHT = 10; // Doubled from 10

    // Images
    public static final String IMG_ENEMY = "src/images/alien.png";
    public static final String IMG_PLAYER = "src/dodoSprites/dodoRun.png";
    public static final String IMG_SHOT = "src/images/shot.png";
    public static final String IMG_EXPLOSION = "src/images/explosion.png";
    public static final String IMG_TITLE = "src/images/title.png";
    public static final String IMG_POWERUP_SPEEDUP = "src/images/powerup-s.png";
    
    // Obstacles
    public static final String IMG_TUMBLEWEED = "src/dodoSprites/tumbleweed.png";
    public static final String IMG_CACTUS = "src/dodoSprites/cactus.png"; 
    
    //background
    public static final String IMG_BACKGROUND = "src/dodoSprites/desertBG.png";
    //game start
    public static final String IMG_START = "src/images/game_start.png";
    // game pause
    public static final String IMG_PAUSE = "src/images/game_pause.png";
    // game over
    public static final String IMG_GAME_OVER = "src/images/game_over.png";
    // game win
    public static final String IMG_GAME_WIN = "src/images/game_win.png";


}
