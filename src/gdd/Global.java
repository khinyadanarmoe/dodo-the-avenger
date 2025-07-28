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

    public static final int  ROBOT_HEIGHT = 24; // Doubled from 12
    public static final int  ROBOT_WIDTH = 24; // Doubled from 12
    public static final int  ROBOT_INIT_X = 300; // Doubled from 150
    public static final int  ROBOT_INIT_Y = 10; // Doubled from 5
    public static final int  ROBOT_GAP = 30; // Gap between  ROBOTs

    public static final int GO_DOWN = 30; // Doubled from 15
    public static final int NUMBER_OF_ROBOTS_TO_DESTROY = 5;
    public static final int CHANCE = 5;
    public static final int DELAY = 17;
    public static final int PLAYER_WIDTH = 15; // Doubled from 15
    public static final int PLAYER_HEIGHT = 10; // Doubled from 10
    public static final int GLOBAL_SPEED = 2; // Global speed for all sprites
    public static int globalSpeed = GLOBAL_SPEED; // Static variable for global speed
    

    // Images
    public static final String IMG_ENEMY = "src/dodoSprites/robot.png";
    public static final String IMG_BOSS = "src/dodoSprites/pharoh.png";
    public static final String IMG_PLAYER = "src/dodoSprites/dodoRun.png";
    public static final String IMG_PLAYER_TEMP = "src/img/megaman-sprite.png";
    public static final String IMG_SHOT = "src/images/shot.png";
    public static final String IMG_EXPLOSION = "src/dodoSprites/explosion.png";
    public static final String IMG_TITLE = "src/dodoSprites/titleScene.png";
    public static final String IMG_POWERUP_SPEEDUP = "src/dodoSprites/speedUp.png";
    public static final String IMG_POWERUP_ARMOR = "src/dodoSprites/armor.png";
    public static final String IMG_BOSS_ATTACK = "src/dodoSprites/bossAttack.png";

    
    // Obstacles
    public static final String IMG_TUMBLEWEED = "src/dodoSprites/tumbleweed.png";
    public static final String IMG_CACTUS = "src/dodoSprites/cactus.png"; 
    
    //background
    public static final String IMG_BACKGROUND = "src/dodoSprites/desertBG.png";
    public static final String IMG_BACKGROUND_FINAL = "src/dodoSprites/pyramid.png";

    // game pause
    public static final String IMG_PAUSE = "src/dodoSprites/paused.png";
    // game over
    public static final String IMG_GAME_OVER = "src/dodoSprites/gameOver.png";
    // game win
    public static final String IMG_GAME_WIN = "src/images/game_win.png";

    public static void setGlobalSpeed(int speed) {
        globalSpeed = speed;
    }
    public static int getGlobalSpeed() {
        return globalSpeed;
    }
    public static void resetGlobalSpeed() {
        globalSpeed = GLOBAL_SPEED; // Reset to default speed
    }


}
