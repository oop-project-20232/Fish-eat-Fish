package game;

import music.*;
import menu.*;
import menu.Menu;
import entity.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

public class Panel extends JPanel implements Runnable {
    public Graphics2D g2d;
    private int B_WIDTH;
    private int B_HEIGHT;
    private int score = 0;

    private float frameRate;
    private Thread thread;
    private final int DELAY = 16;
    private int maxPlayerNameLength = 12;

    private Player player;
    private Background bg;

    private SpeedUp pUp;
    private EatShark SharkEat;
    private Menu menu;
    private HighScores highScore;
    private int prevPlayerlevel = 1;

    private String playerName;
    private int newPlayerlevel;

    private ArrayList<Fish> fish;
    private ArrayList<Shark> sharks;

    private boolean inGame, inMenu, inTutorial, inHighScore, inEnterName, GameOver, quitGame, printDuration,
            sharkDuration;
    private Font font;
    private Music gameMusic = new Music("resources/sounds/gameMusic.wav", 0.3);
    private MusicThread eatSound = new MusicThread("resources/sounds/eat.wav", 0.3);
    private MusicThread gameOverSound = new MusicThread("resources/sounds/gameOver.wav", 0.3);
    private MusicThread powerUpSound = new MusicThread("resources/sounds/powerUp.wav", 0.3);

    /*
     * public void pauseGame(int durationMillis) {
     * DelayThread delayThread = new DelayThread(durationMillis);
     * delayThread.start();
     * 
     * try {
     * delayThread.join();
     * } catch (InterruptedException e) {
     * e.printStackTrace();
     * }
     * }
     */

    public Panel(int width, int height) {
        addMouseListener(new mouseAdapter());
        addKeyListener(new keyAdapter());
        setFocusable(true);
        B_WIDTH = width;
        B_HEIGHT = height;

        inMenu = true;
        inGame = false;
        GameOver = false;
        quitGame = false;
        inTutorial = false;
        inHighScore = false;

        setBackground(Color.BLACK);
        setDoubleBuffered(true);

        font = new Font("Comic Sans MS", Font.BOLD, 45);

        initGame();
    }

    public void initGame() {
        menu = new Menu(B_WIDTH, B_HEIGHT);
        bg = new Background(0, 0);
        pUp = new SpeedUp(0, 0, 0);
        SharkEat = new EatShark(0, 0, 0);
        fish = new ArrayList();
        sharks = new ArrayList();
        player = new Player(300, 200, 2, 1);
        highScore = new HighScores();
        printDuration = false;
        playerName = "";

        frameRate = 0;
        score = 0;
        pUp.setAlive(false);
        SharkEat.setAlive(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        thread = new Thread(this);
        thread.start();
    }

    public void genFish() {
        Fish temp = new Fish(-100, -100, 0, "L", 0);
        int spawn = temp.getHeight();
        temp.setVisible(false);

        Random gen = new Random();
        String direction = "";
        int x, y, speed, fishlevel;
        int numFish = gen.nextInt(10) + 10;

        for (int i = 0; i < numFish; i++) {
            y = spawn + gen.nextInt(B_HEIGHT - spawn * 3);
            speed = gen.nextInt(5) - 2;
            fishlevel = gen.nextInt(7);
            if (speed > 0) {
                speed += player.getLevel() - 1;
                x = gen.nextInt(1000) + B_WIDTH;
                direction = "L";
            } else {
                speed -= player.getLevel();
                x = gen.nextInt(1000) - (B_WIDTH + 400);
                direction = "R";
            }
            fish.add(new Fish(x, y, speed, direction, fishlevel));
        }
    }

    public void genSharks() {
        Shark temp = new Shark(-100, -100, 0, "L");
        int spawn = temp.getHeight();

        Random gen = new Random();
        String direction = "";
        int x, y, speed;
        int numSharks = gen.nextInt(5) + 1;

        for (int i = 0; i < numSharks; i++) {
            y = spawn + gen.nextInt(B_HEIGHT - spawn * 3);
            speed = gen.nextInt(5) - 2;
            if (speed > 0) {
                speed += player.getLevel() - 1;
                x = gen.nextInt(1000) + B_WIDTH;
                direction = "L";
            } else {
                speed -= player.getLevel();
                x = gen.nextInt(1000) - (B_WIDTH + 400);
                direction = "R";
            }
            sharks.add(new Shark(x, y, speed, direction));
        }
    }

    public void genSpeedUp() {
        Random gen = new Random();
        int x = (pUp.getWidth() + gen.nextInt(B_WIDTH - pUp.getWidth() * 3)),
                y = -gen.nextInt(B_HEIGHT) - 500,
                speed = gen.nextInt(5) + 3;

        pUp = new SpeedUp(x, y, speed);
    }

    public void genEatShark() {
        Random gen = new Random();
        int x = (SharkEat.getWidth() + gen.nextInt(B_WIDTH - SharkEat.getWidth() * 3)),
                y = -gen.nextInt(B_HEIGHT) - 500,
                speed = gen.nextInt(5) + 3;
        boolean eat = new Random().nextBoolean();
        if (eat) {
            SharkEat = new EatShark(x, y, speed);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        g2d = (Graphics2D) g;

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        rh.put(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g2d.addRenderingHints(rh);

        if (inGame) {
            bg.paint(g);
            player.paint(g);
            for (Object fish1 : fish) {
                Fish f = (Fish) fish1;
                f.paint(g);
            }

            for (Object shark1 : sharks) {
                Shark sh = (Shark) shark1;
                sh.paint(g);
            }

            Font scoreFont = new Font("Comic Sans MS", Font.BOLD, 45);
            Font notiFont = new Font("Comic Sans MS", Font.BOLD, 30);
            Color customColor = new Color(0x00, 0x97, 0xB2);
            Color notiColor = new Color(0xFF, 0x57, 0x57);

            boolean pUpInfoDrawn = false;
            boolean sharkEatInfoDrawn = false;

            if (pUp.isAlive() && pUp.isVisible()) {
                pUp.paint(g);
                pUpInfoDrawn = true;
            }
            if (printDuration && pUp.isAlive() && !pUpInfoDrawn) {
                g.setFont(notiFont);
                g.setColor(notiColor);
                g.drawString(pUp.getName() + " in: " + pUp.getDuration() / 63 + "s", 800, 60);
            }

            if (SharkEat.isAlive() && SharkEat.isVisible()) {
                SharkEat.paint(g);
                sharkEatInfoDrawn = true;
            }
            if (sharkDuration && SharkEat.isAlive() && !sharkEatInfoDrawn) {
                g.setColor(notiColor);
                g.setFont(notiFont);
                g.drawString(SharkEat.getName() + " in: " + SharkEat.getDuration() / 63 + "s", 800, 35);
            }
            g.setColor(customColor);
            g.setFont(scoreFont);
            g.drawString(" " + score, 130, 45);
            g.drawString("  " + convertScore2level(score), 450, 45);
        }

        else if (inMenu) {
            g2d.drawImage(menu.getMenuBackgroundImage("welcome"), 0, 0, this);
        } else if (inTutorial) {
            g2d.drawImage(menu.getMenuBackgroundImage("tutorial"), 0, 0, this);
        } else if (inHighScore) {
            g2d.drawImage(menu.getMenuBackgroundImage("highScore"), 0, 0, this);
            g2d.setColor(Color.yellow);
            g2d.setFont(font);
            int textWidth = g2d.getFontMetrics().stringWidth(highScore.getName(0));
            int horizontalPosition = 655 - (textWidth / 2);
            int verticalPosition = 280;
            g2d.drawString(highScore.getName(0), horizontalPosition, verticalPosition);
            textWidth = g2d.getFontMetrics().stringWidth(highScore.getName(1));
            horizontalPosition = 435 - (textWidth / 2);
            verticalPosition = 370;
            g2d.drawString(highScore.getName(1), horizontalPosition, verticalPosition);
            textWidth = g2d.getFontMetrics().stringWidth(highScore.getName(2));
            horizontalPosition = 860 - (textWidth / 2);
            verticalPosition = 420;
            g2d.drawString(highScore.getName(2), horizontalPosition, verticalPosition);

            textWidth = g2d.getFontMetrics().stringWidth(highScore.getScore(0));
            horizontalPosition = 655 - (textWidth / 2);
            verticalPosition = 350;
            g2d.drawString(highScore.getScore(0), horizontalPosition, verticalPosition);
            textWidth = g2d.getFontMetrics().stringWidth(highScore.getScore(1));
            horizontalPosition = 435 - (textWidth / 2);
            verticalPosition = 440;
            g2d.drawString(highScore.getScore(1), horizontalPosition, verticalPosition);
            textWidth = g2d.getFontMetrics().stringWidth(highScore.getScore(2));
            horizontalPosition = 860 - (textWidth / 2);
            verticalPosition = 490;
            g2d.drawString(highScore.getScore(2), horizontalPosition, verticalPosition);
        } else if (GameOver) {
            g2d.drawImage(menu.getMenuBackgroundImage("gameOver"), 0, 0, this);

            g2d.setColor(Color.yellow);
            g2d.setFont(font);
            g2d.drawString("" + score, B_WIDTH / 2 - 10, B_HEIGHT / 2 - 50);

            g2d.setFont(new Font("Helvetica", Font.PLAIN, 120));

        } else if (inEnterName) {
            g2d.drawImage(menu.getMenuBackgroundImage("enterName"), 0, 0, this);

            g2d.setColor(Color.yellow);
            g2d.setFont(font);

            int textWidth = g2d.getFontMetrics().stringWidth("" + score);
            int horizontalPosition = (B_WIDTH / 2) - (textWidth / 2);
            int verticalPosition = B_HEIGHT / 2;
            g2d.drawString("" + score, horizontalPosition, verticalPosition);

            g2d.setColor(Color.black);
            textWidth = g2d.getFontMetrics().stringWidth(playerName);
            horizontalPosition = (B_WIDTH / 2) - (textWidth / 2);
            verticalPosition = 580;

            g2d.drawString(playerName, horizontalPosition, verticalPosition);

            g2d.setFont(new Font("Helvetica", Font.PLAIN, 120));
        }

        g2d.setColor(Color.white);
        // g2d.drawString("Framerate = " + frameRate, B_WIDTH / 2, 15);

        Toolkit.getDefaultToolkit().sync();
        g.dispose();

    }

    public void run() {
        long beforeTime, timeDiff, sleep, frameRateTimer;
        int frameRateCounter = 0;
        Random gen = new Random();

        beforeTime = System.currentTimeMillis();
        frameRateTimer = System.currentTimeMillis();

        while (!quitGame) {
            if (inGame) {
                gameMusic.loop();
                if (pUp.isAlive()) {
                    pUp.move();
                }

                if (SharkEat.isAlive()) {
                    SharkEat.move();
                }

                for (int i = 0; i < fish.size(); i++) {
                    Fish f = (Fish) fish.get(i);
                    if (f.isVisible()) {
                        if ((f.getSpeed() < 0 && f.getX() > -30) || (f.getSpeed() > 0 && f.getX() < 1280)) {
                            f.move();
                        } else {
                            fish.remove(i);
                        }
                    } else {
                        fish.remove(i);
                    }
                }

                for (int i = 0; i < sharks.size(); i++) {
                    Shark s = (Shark) sharks.get(i);
                    if (s.isVisible()) {
                        if ((s.getSpeed() < 0 && s.getX() > -100) || (s.getSpeed() > 0 && s.getX() < 1280)) {
                            s.move();
                        } else {
                            sharks.remove(i);
                        }
                    } else {
                        sharks.remove(i);
                    }
                }

                if (fish.size() < 3) {
                    genFish();
                }
                if (fish.size() > 5 && gen.nextInt(100) > 50 && sharks.isEmpty()) {
                    genSharks();
                }
                if (pUp.isAlive() == false && gen.nextInt(200) > 15) {
                    genSpeedUp();
                }
                if (pUp.getDuration() < 0) {
                    player.setSpeedUp(false);
                    printDuration = false;
                    pUp.setAlive(false);
                }

                if (SharkEat.isAlive() == false && gen.nextInt(200) > 15) {
                    genEatShark();
                }
                if (SharkEat.getDuration() < 0) {
                    player.setSharkEat(false);
                    sharkDuration = false;
                    SharkEat.setAlive(false);
                }
                player.move();
                checkCollisions();
            } else {
                gameMusic.stop();
            }

            repaint();

            frameRateCounter++;
            timeDiff = System.currentTimeMillis() - frameRateTimer;
            if (timeDiff >= 1000) {
                frameRate = (float) (frameRateCounter * 1000) / timeDiff;
                frameRateCounter = 0;
                frameRateTimer = System.currentTimeMillis();
            }

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = DELAY - timeDiff;
            if (sleep < 0) {
                sleep = 2;
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }

            beforeTime = System.currentTimeMillis();
        }
    }

    // có 6 loại cá, set 6 level từ 1 đến 6
    public int convertScore2level(int score) {
        if (score < 6) {
            return 1;
        } else if (6 <= score && score < 50) {
            return 2;
        } else if (50 <= score && score < 200) {
            return 3;
        } else if (200 <= score && score < 1000) {
            return 4;
        } else if (1000 <= score && score < 3000) {
            return 5;
        } else {
            return 6;
        }
    }

    // enlarge playerfish
    // update hinh anh playerfish cac level o day
    public void enlargePlayer(int playerlevel) {
        switch (playerlevel) {
            case 1:
                player.setIcon("resources/fish/player1");
                break;
            case 2:
                player.setIcon("resources/fish/player2");
                break;
            case 3:
                player.setIcon("resources/fish/player3");
                break;
            case 4:
                player.setIcon("resources/fish/player4");
                break;
            case 5:
                player.setIcon("resources/fish/player5");
                break;
            case 6:
                player.setIcon("resources/fish/player6");
                break;
        }
    }

    public void checkCollisions() {

        for (int i = 0; i < fish.size(); i++) {
            Fish f = (Fish) fish.get(i);

            if (player.EllipseCollision(f)) {
                // chi co the an fish co level nho hon
                if (player.getLevel() > f.getLevel()) {
                    eatSound.play();
                    f.setVisible(false);
                    prevPlayerlevel = convertScore2level(score);
                    score += f.getPoints();
                    newPlayerlevel = convertScore2level(score);

                    // update level va tang kich thuoc neu level up
                    if (prevPlayerlevel < convertScore2level(score)) {

                        enlargePlayer(newPlayerlevel);
                        player.setlevel(newPlayerlevel);

                    }

                    player.setSpeed(player.getLevel() + 1);
                } else {
                    player.setVisible(false);
                    inGame = false;
                    if (highScore.checkHighScore(score)) {
                        inEnterName = true;
                        menu.setCurrentMenu("enterName");
                    } else {
                        GameOver = true;
                        menu.setCurrentMenu("gameOver");
                    }
                    gameOverSound.play();
                }
            }
        }

        for (int i = 0; i < sharks.size(); i++) {
            Shark s = (Shark) sharks.get(i);

            if (player.EllipseCollision(s)) {
                if (player.getSharkEat()) {
                    eatSound.play();
                    score += s.getPoints();
                    player.setlevel(convertScore2level(score));
                    enlargePlayer(convertScore2level(score));
                    player.setSpeed(player.getLevel() + 1);
                    s.setVisible(false);
                } else {
                    player.setVisible(false);
                    inGame = false;
                    if (highScore.checkHighScore(score)) {
                        inEnterName = true;
                        menu.setCurrentMenu("enterName");
                    } else {
                        GameOver = true;
                        menu.setCurrentMenu("gameOver");
                    }
                    gameOverSound.play();
                }
            }
        }

        if (player.EllipseCollision(pUp)) {
            powerUpSound.play();
            player.setSpeedUp(true);
            pUp.setVisible(false);
            pUp.setY(-10);
            printDuration = true;
        }

        if (player.EllipseCollision(SharkEat)) {
            powerUpSound.play();
            player.setSharkEat(true);
            SharkEat.setVisible(false);
            SharkEat.setY(-10);
            sharkDuration = true;
        }
    }

    private class keyAdapter extends KeyAdapter {
        public void keyReleased(KeyEvent e) {
            if (inGame)
                player.keyReleased(e);
            if (inEnterName) {
                int keyCode = e.getKeyCode();
                char keyChar = e.getKeyChar();
                if (65 <= keyCode && keyCode <= 90 && playerName.length() < maxPlayerNameLength)// a->z A->Z
                    playerName += keyChar;
                if (keyCode == 32 && playerName.length() < maxPlayerNameLength) // space
                    playerName += keyChar;
                if (keyCode == 8 && playerName.length() >= 1) // backspace
                    playerName = playerName.substring(0, playerName.length() - 1);
            }
        }

        public void keyPressed(KeyEvent e) {
            if (inGame)
                player.keyPressed(e);
        }
    }

    private class mouseAdapter extends MouseAdapter {
        public void mouseReleased(MouseEvent m) {

        }

        public void mousePressed(MouseEvent m) {
            int mouseDown = m.getButton();
            if (mouseDown == m.BUTTON1) {
                if (inMenu) {
                    if (menu.startPressed(m)) {
                        inGame = true;
                        menu.setCurrentMenu("inGame");
                        inMenu = false;
                        initGame();
                    }
                    if (menu.tutorialPressed(m)) {
                        inTutorial = true;
                        menu.setCurrentMenu("tutorial");
                        inMenu = false;
                    }
                    if (menu.highScorePressed(m)) {
                        inHighScore = true;
                        menu.setCurrentMenu("highScore");
                        inMenu = false;
                    }
                }

                if (GameOver) {

                    if (menu.replayPressed(m)) {
                        inGame = true;
                        menu.setCurrentMenu("inGame");
                        GameOver = false;
                        initGame();
                    }
                    if (menu.menuPressed(m)) {
                        inMenu = true;
                        menu.setCurrentMenu("welcome");
                        GameOver = false;
                        player.setVisible(true);
                    }
                }
                if (inEnterName) {
                    if (menu.submitNamePressed(m)) {
                        inHighScore = true;
                        highScore.addHighScore(playerName, score);
                        menu.setCurrentMenu("highScore");
                        inEnterName = false;
                    }
                }
                if (inHighScore) {
                    if (menu.menuPressed(m)) {
                        inMenu = true;
                        menu.setCurrentMenu("welcome");
                        inHighScore = false;
                    }
                    if (menu.replayPressed(m)) {
                        inGame = true;
                        menu.setCurrentMenu("inGame");
                        inHighScore = false;
                        initGame();
                    }
                }
                if (inTutorial) {
                    if (menu.menuPressed(m)) {
                        inMenu = true;
                        menu.setCurrentMenu("welcome");
                        inTutorial = false;
                    }
                    if (menu.startPressed(m)) {
                        inGame = true;
                        menu.setCurrentMenu("inGame");
                        inTutorial = false;
                        initGame();
                    }
                }
            }
        }

    }
}
