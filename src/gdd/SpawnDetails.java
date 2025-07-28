package gdd;

import static gdd.Global.BOARD_WIDTH;
import static gdd.Global.GROUND;

public class SpawnDetails {
    public String type;
    public int x;
    public int y;

    public SpawnDetails(String type, int x, int y) {
      this.type = type;
      this.x = x;
      this.y = y;
    }

    public static int getShortSpawnTime() {
      return 100 + (int)(Math.random() * 300); // Random time between 1 to 4 seconds
    }

    public static int getLongSpawnTime() {
      return 300 + (int)(Math.random() * 500); // Random time between 3 to 8 seconds
    }

    public static int getVeryLongSpawnTime() {
      return 600 + (int)(Math.random() * 1000);
    }

    public static int getSpawnY() {
      // Returns a random Y position between the ground and the board height
      int min = 10;
      int groundY = GROUND;   
      return min + (int)(Math.random() * groundY);
    }

    public static int getSpawnX() {
      // Returns a random Y position between the ground and the board height
      int min = 0; 
      return min + (int)(Math.random() * BOARD_WIDTH);
    }

}
