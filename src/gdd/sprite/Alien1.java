package gdd.sprite;

public class Alien1 extends Enemy {

    public Alien1(int x, int y) {
        super(x, y);  // This handles bomb initialization
    }

    public void act(int direction) {
        this.y++;
    }
}
