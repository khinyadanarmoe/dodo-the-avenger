## Scene Refactoring Blueprint

### ✅ COMPLETED: BaseGameScene Class

Created `BaseGameScene.java` that provides common functionality:
- **Player management**: HP bar, shield indicator
- **Game state**: pause, victory, game over screens
- **Audio management**: start, stop, pause, resume
- **Timer and frame management**: 60 FPS game loop
- **Common key handling**: pause (P), end game (SPACE/ESC)

### ✅ COMPLETED: FinalScene Refactor

**Before**: 432 lines with lots of duplicate code
**After**: 140 lines, clean and focused

**Removed duplicate code:**
- HP bar rendering (94 lines → inherited)
- Pause overlay (30 lines → inherited)
- Victory/Game Over screens (60 lines → inherited)
- Audio management (40 lines → inherited)
- Timer and game loop (30 lines → inherited)
- Common key handling (20 lines → inherited)

**FinalScene now only contains:**
- Boss fight specific drawing
- Static background rendering
- Player positioning for boss fight
- Boss fight specific controls

### 🚧 RECOMMENDED: Scene1 Refactor

**Current Scene1**: 968 lines with complex enemy/powerup systems
**Potential refactor**: Could reduce to ~600-700 lines

**What could be extracted to BaseGameScene:**
- ✅ HP bar (already done)
- ✅ Pause system (already done)
- ✅ Victory/Game Over screens (already done)
- ✅ Audio management (already done)
- ✅ Timer management (already done)

**What stays Scene1-specific:**
- Enemy spawning system
- Powerup system
- Collision detection
- Dynamic background scrolling
- Complex game mechanics

**Benefits of refactoring Scene1:**
1. **Consistency**: Same base behavior across all scenes
2. **Maintainability**: Bug fixes in BaseGameScene benefit all scenes
3. **Cleaner code**: Scene1 focuses only on gameplay logic
4. **Extensibility**: Easy to add new scenes with common functionality

### Example Scene1 Refactor Structure:

```java
public class Scene1 extends BaseGameScene {
    // Scene1-specific fields
    private List<Enemy> enemies;
    private List<PowerUp> powerups;
    private List<Obstacle> obstacles;
    // ... spawning logic, etc.
    
    @Override
    protected void gameInit() {
        // Initialize player, enemies, powerups
    }
    
    @Override
    protected void update() {
        // Scene1-specific update logic
        // Enemy spawning, collision, powerups
    }
    
    @Override
    protected void drawScene(Graphics g) {
        // Draw scrolling background
        // Draw enemies, powerups, obstacles
        // Draw player
    }
    
    @Override
    protected void drawHUD(Graphics g) {
        super.drawHUD(g); // HP bar
        // Scene1-specific HUD elements
    }
    
    // Scene1-specific methods remain unchanged
    private void spawnEnemies() { ... }
    private void handleCollisions() { ... }
    private void updatePowerups() { ... }
}
```

### Current State Summary:

✅ **BaseGameScene**: Complete blueprint for all game scenes
✅ **FinalScene**: Fully refactored, 68% code reduction
⚡ **Scene1**: Ready for refactoring if desired (optional)

**Result**: Much cleaner, more maintainable codebase with shared functionality!
