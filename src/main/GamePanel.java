package main;

import ai.PathFinder;
import entiiy.Entity;
import entiiy.Player;
import tile.TileManager;
import tile_interactive.InteractiveTile;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GamePanel extends JPanel implements Runnable{

    // SCREEN SETTINGS
    final int  originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 20, maxScreenRow = 12;
    public final int ScreenWith = tileSize * maxScreenCol ;
    public final int ScreenHeight = tileSize * maxScreenRow;

    //WORLD SETTINGS
    public final int maxWorldCol = 50, maxWorldRow = 50;
    public final int maxMap = 10;
    public int currentMap = 0;

    //FOR FULL SCREEN
    int screenWith2 = ScreenWith;
    int screenHeight2 = ScreenHeight;
    BufferedImage tempScreen;
    Graphics2D g2;
    public boolean fullScreenOn = false;


    //FPS
    int FPS = 60;

    // SYSTEM
    public TileManager tileM = new TileManager(this);
    public KeyHandler KeyH = new KeyHandler(this);
    Sound music = new Sound();
    Sound se = new Sound();
    public CollisionChecker cChecker = new CollisionChecker(this);
    public AssetSetter aSetter = new AssetSetter((this));
    public UI ui = new UI(this);
    public EventHandler eHandler = new EventHandler(this);
    public PathFinder pFinder = new PathFinder(this);
    Thread gameThread;

    // ENTITY AND OBJECT
    public Player player = new Player(this, KeyH);
    public Entity obj[][] = new Entity[maxMap][20];
    public Entity npc[][] = new Entity[maxMap][10];
    public Entity monster[][] = new Entity[maxMap][20];
    public InteractiveTile iTile[][] = new InteractiveTile[maxMap][50];
    public Entity projectile[][] = new Entity[maxMap][20];
    public ArrayList<Entity> projectileList = new ArrayList<>();
    public ArrayList<Entity> particleList = new ArrayList<>();
    ArrayList<Entity> entityList = new ArrayList<>();

    //GAME STATE
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int  dialogueState = 3;
    public final int characterState = 4;
    public final int optionsState = 5;
    public final int gameOverState = 6;
    public final int transitionState = 7;

    //AREA
    public int currentArea;
    public int nextArea;
    public final int outside = 50;
    public final int indoor = 51;
    public final int dungeon = 52;


    public GamePanel() {
        this.setPreferredSize(new Dimension(ScreenWith, ScreenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(KeyH);
        this.setFocusable(true);
    }
    public void setupGame() {

        aSetter.setObject();
        aSetter.setNPC();
        aSetter.setMonster();
        aSetter.setInteractiveTile();
        playMusic(0);

        gameState = titleState;
        currentArea = outside;

        tempScreen = new BufferedImage(ScreenWith, ScreenHeight, BufferedImage.TYPE_INT_ARGB);
        g2 = (Graphics2D)tempScreen.getGraphics();

        //setFullScreen();
    }
    public void resetGame(boolean restart) {

        currentArea = outside;
        player.setDefaultPositions();
        player.restoreStatus();
        aSetter.setNPC();
        aSetter.setMonster();

        if (restart == true) {
            player.setDefaultValues();
            aSetter.setObject();
            aSetter.setInteractiveTile();
        }
    }
    public void setFullScreen() {
        //GET LOCAL SCREEN DEVICE
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        gd.setFullScreenWindow(main.window);

        //GET FULL SCREEN WIDTH AND HEIGHT
        screenWith2 = main.window.getWidth();
        screenHeight2 = main.window.getHeight();
    }
    public void startGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }
    @Override
    public void run() {
        double drawInterval = 1000000000/FPS; // 0.01666 sec
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

       while (gameThread != null){
           currentTime = System.nanoTime();
           delta += (currentTime - lastTime) / drawInterval;
           timer += (currentTime - lastTime);
           lastTime = currentTime;
           if (delta >= 1) {
               update();
               drawToTempScreen();
               drawToScreen();
               delta--;
               drawCount++;
           }
           if (timer >= 1000000000){
               System.out.println("FPS:" + drawCount);
               drawCount = 0;
               timer = 0;
           }
       }
    }
    public void update() {
        if (gameState == playState){
            //PLAYER
            player.update();
            //NPC
            for (int i =0 ; i < npc[1].length; i++) {
                if (npc[currentMap][i] != null)
                    npc[currentMap][i].update();
            }
            //MONSTER
            for (int i =0 ; i < monster[1].length; i++){
                if (monster[currentMap][i] != null) {
                    if (monster[currentMap][i].alive == true && monster[currentMap][i].dying == false) {
                        monster[currentMap][i].update();
                    }
                    if (monster[currentMap][i].alive == false) {
                        monster[currentMap][i].checkDrop();
                        monster[currentMap][i] = null;
                    }
                }
            }
            //PROJECTILE
            for (int i =0 ; i < projectile[1].length; i++){
                if (projectile[currentMap][i] != null) {
                    if (projectile[currentMap][i].alive == true) {
                        projectile[currentMap][i].update();
                    }
                    if (projectile[currentMap][i].alive == false) {
                        projectile[currentMap][i] = null;
                    }
                }
            }
            for (int i =0 ; i < particleList.size(); i++){
                if (particleList.get(i) != null) {
                    if (particleList.get(i).alive == true) {
                        particleList.get(i).update();
                    }
                    if (particleList.get(i).alive == false) {
                        particleList.remove(i);
                    }
                }
            }
            for (int i = 0; i < iTile[1].length; i++) {
                if (iTile[currentMap][i] != null) {
                    iTile[currentMap][i].update();
                }
            }
        }
        if (gameState == pauseState){

        }
    }
    public void drawToTempScreen() {
        //DEBUG
        long drawStart = 0;
        if (KeyH.checkDrawTime == true) {
            drawStart = System.nanoTime();
        }

        // TITLE STATE
        if (gameState == titleState) {
            ui.draw(g2);
        }
        // OTHERS
        else{
            //TILE
            tileM.draw(g2);

            //INTERACTIVE TILES
            for (int i = 0; i < iTile[1].length; i++) {
                if (iTile[currentMap][i] != null) {
                    iTile[currentMap][i].draw(g2);
                }
            }

            //ADD ENTITIES TO LIST
            entityList.add(player);

            for (int i =0 ; i < npc[1].length; i++){
                if (npc[currentMap][i] != null){
                    entityList.add(npc[currentMap][i]);
                }
            }
            for (int i =0 ; i < obj[1].length; i++){
                if (obj[currentMap][i] != null){
                    entityList.add(obj[currentMap][i]);
                }
            }
            for (int i =0 ; i < monster[1].length; i++){
                if (monster[currentMap][i] != null){
                    entityList.add(monster[currentMap][i]);
                }
            }
            for (int i =0 ; i < projectile[1].length; i++) {
                if (projectile[currentMap][i] != null) {
                    entityList.add(projectile[currentMap][i]);
                }
            }
            for (int i =0 ; i < particleList.size(); i++) {
                if (particleList.get(i) != null) {
                    entityList.add(particleList.get(i));
                }
            }

            //SORT
            Collections.sort(entityList, new Comparator<Entity>() {
                @Override
                public int compare(Entity e1, Entity e2) {
                    int result = Integer.compare(e1.worldX, e2.worldY);
                    return result;
                }
            });

            //DRAW ENTITIES
            for (int i = 0; i < entityList.size(); i++){
                entityList.get(i).draw(g2);
            }
            //REMOVE ENTITIES
            entityList.clear();

            //UI
            ui.draw(g2);
        }

        //DEBUG
        if (KeyH.checkDrawTime == true) {
            long drawEnd = System.nanoTime();
            long passed = drawEnd - drawStart;
            g2.setColor(Color.white);
            g2.drawString("Draw Time: " + passed, 10, 400);
            System.out.println("Draw Time: " + passed);
        }
    }
    public void drawToScreen() {
        Graphics g = getGraphics();
        g.drawImage(tempScreen, 0, 0, screenWith2, screenHeight2, null);
        g.dispose();
    }
    public void playMusic(int i) {
        music.setFile(i);
        music.play();
        music.loop();
    }
    public void stopMusic() {
        music.stop();
    }
    public void playSE(int i) {
        se.setFile(i);
        se.play();
    }
    public void changeArea() {

        stopMusic();
        if (nextArea != currentArea) {
            if (nextArea == outside) {
                playSE(0);
            }
            else if (nextArea == dungeon) {
                playSE(16);
            }
        }

        currentArea = nextArea;
        aSetter.setMonster();
    }
}
