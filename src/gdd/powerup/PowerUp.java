/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package gdd.powerup;

import static gdd.Global.globalSpeed;

import gdd.sprite.Player;
import gdd.sprite.Sprite;


abstract public class PowerUp extends Sprite {
    PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Add default act method for side-scrolling
    @Override
    public void act() {
        // Default behavior for side-scrolling - move left
        this.x -= globalSpeed; // Move left by global speed
    }

    abstract public void upgrade(Player player);
}
