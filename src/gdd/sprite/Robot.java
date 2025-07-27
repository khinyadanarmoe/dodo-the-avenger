package gdd.sprite;

public class Robot extends Enemy {

    public Robot() {
        super(0, 0); // Provide default values for x and y
    }
    public Robot(int x, int y) {
        super(x, y);  // This handles bomb initialization
    }

    public void act(int direction) {
        this.x += direction;
    }
}
