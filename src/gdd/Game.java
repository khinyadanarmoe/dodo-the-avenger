package gdd;

import gdd.scene.Scene1;
import gdd.scene.TitleScene;
import gdd.scene.Cutscene;
import gdd.scene.FinalScene;

import static gdd.Global.resetGlobalSpeed;

import javax.swing.JFrame;

public class Game extends JFrame  {

    TitleScene titleScene;
    Scene1 scene1;
    Cutscene cutscene;
    FinalScene finalScene;

    public Game() {
        titleScene = new TitleScene(this);
        scene1 = new Scene1(this);
        cutscene = new Cutscene(this);
        finalScene = new FinalScene(this);
        initUI();
        loadTitle();
        // loadScene2();
    }

    private void initUI() {

        setTitle("Dodo the Avenger");
        setSize(Global.BOARD_WIDTH, Global.BOARD_HEIGHT);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

    }

    public void loadTitle() {
        getContentPane().removeAll();
        // add(new Title(this));
        add(titleScene);
        titleScene.start();
        revalidate();
        repaint();
    }

    public void loadScene1() {
        // ....
    }

    public void loadScene2() {
        getContentPane().removeAll();
        add(scene1);
        titleScene.stop();
        scene1.start();
        revalidate();
        repaint();
    }

    public void loadCutscene() {
        getContentPane().removeAll();
        add(cutscene);
        scene1.stop();
        cutscene.start();
        revalidate();
        repaint();
    }

    public void loadFinalScene() {
        getContentPane().removeAll();
        add(finalScene);
        cutscene.stop();
        finalScene.start();
        revalidate();
        repaint();
        resetGlobalSpeed();
    }
}