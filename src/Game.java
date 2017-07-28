import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class Game extends JFrame implements KeyListener{

    //window vars
    private final int MAX_FPS; //maximum refresh rate
    private final int WIDTH; //window width
    private final int HEIGHT; //window height

    enum GAME_STATES{
        MENU,
        PLAY,
        SCORE
    }
    GAME_STATES GameState = GAME_STATES.MENU;

    //double buffer strategy
    private BufferStrategy strategy;

    String sprite_file = "Textures\\link.png";
    BufferedImage sprite;

    String gif_file = "Textures\\Drifter_Sprite.gif";
    Image gif;

    String enemygif_file = "Textures\\enemy.gif";
    Image enemygif;

    private ArrayList<Integer> keys = new ArrayList<>();

    //loop variables
    private boolean isRunning = true; //is the window running
    private long rest = 0; //how long to sleep the main thread

    //timing variables
    private float dt; //delta time
    private long lastFrame; //time since last frame
    private long startFrame; //time since start of frame
    private int fps; //current fps

    Vector p;
    Vector v;
    Vector a;

    float friction = 0.95f;
    float push;
    Vector sz = new Vector(100, 100);

    Vector p2 = new Vector(250, 250);
    Vector sz2 = new Vector(100, 100);


    public Game(int width, int height, int fps){
        super("My Game");
        this.MAX_FPS = fps;
        this.WIDTH = width;
        this.HEIGHT = height;
    }

    /*
     * init()
     * initializes all variables needed before the window opens and refreshes
     */
    void init(){
        //initializes window size
        setBounds(0, 0, WIDTH, HEIGHT);
        setResizable(false);

        //set jframe visible
        setVisible(true);

        //set default close operation
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //create double buffer strategy
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        addKeyListener(this);
        setFocusable(true);

        //set initial lastFrame var
        lastFrame = System.currentTimeMillis();

        sprite = loadTexture(sprite_file);
        gif = loadTextureGif(gif_file);
        enemygif = loadTextureGif(enemygif_file);

        //set background window color
        setBackground(Color.BLUE);

        p = new Vector(WIDTH/2, HEIGHT/2);
        //v = new Vector(0, 0);
        v = new Vector(100, 100);
        a = new Vector(0, 0);
        sz = new Vector(100, 100);
        push = 1000;

        GameState = GAME_STATES.MENU;
    }

    public BufferedImage loadTexture(String filepath){
        try {
            return ImageIO.read(new File(filepath));
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Image loadTextureGif(String filepath){
        try {
            return new ImageIcon(new File(filepath).toURI().toURL()).getImage();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * update()
     * updates all relevant game variables before the frame draws
     */
    private void update(){
        //update current fps
        fps = (int)(1f/dt);

        handleKeys();

        switch(GameState){
            case MENU:
                break;
            case PLAY:
                if(p.x + sz.ix > WIDTH || p.x < 0){
                    v.setX(-v.x);
                }
                if(p.y + sz.ix > HEIGHT || p.y < 0){
                    v.setY(-v.y);
                }

                //if(isAABBcollision(p, sz, p2, sz2))
                if(isCircleCollision(p, p2, sz.ix/2, sz2.ix/2)) {
                    p.add(
                            Vector.mult(
                                    resolveCollision(
                                            Vector.add(p, Vector.div(sz, 2)),
                                            v,
                                            Vector.add(p2, Vector.div(sz2, 2))),
                                    dt)
                    );
                }


                //v += a * dt;
                //p += v * dt;
                v.add(Vector.mult(a, dt));
                v.mult(friction);
                p.add(Vector.mult(v, dt));

                a = new Vector(0, 0);


                break;
            case SCORE:
                break;
        }

    }

    /*
     * draw()
     * gets the canvas (Graphics2D) and draws all elements
     * disposes canvas and then flips the buffer
     */
    private void draw(){
        //get canvas
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

        //clear screen
        g.clearRect(0,0,WIDTH, HEIGHT);

        switch(GameState){
            case MENU:
                g.setColor(new Color(255, 200, 0));
                g.drawString("Hit Enter to Play!", WIDTH/2,HEIGHT/2);
                break;
            case PLAY:
                //g.setColor(Color.red);
                //g.setColor(new Color(0, 200, 255));
                //g.fillOval(p.ix, p.iy, sz.ix, sz.iy);
                g.drawImage(sprite, p.ix, p.iy, sz.ix, sz.iy, null);
                g.drawImage(gif, 300, 300, 300, 300, null);
                //g.setColor(Color.green);
                //g.fillOval(p2.ix, p2.iy, sz2.ix, sz2.iy);
                g.drawImage(enemygif, p2.ix, p2.iy, sz2.ix, sz2.iy, null);

                g.setColor(Color.red);
                //g.drawOval(p.ix, p.iy, sz.ix, sz.ix);
                //g.drawOval(p2.ix, p2.iy, sz2.ix, sz2.ix);

                //draw fps
                g.setColor(Color.GREEN);
                g.drawString(Long.toString(fps), 10, 40);
                break;
            case SCORE:
                break;
        }

        //release resources, show the buffer
        g.dispose();
        strategy.show();
    }

    private boolean isAABBcollision(Vector p1, Vector sz1, Vector p2, Vector sz2){
        return
                p1.x < p2.x + sz2.x &&
                p1.x + sz1.x > p2.x &&
                p1.y < p2.y + sz2.y &&
                p1.y + sz1.y > p2.y;
    }

    private boolean isCircleCollision(Vector p1, Vector p2, float r1, float r2){
        return Vector.sub(p1, p2).sqmag() < Math.pow(r1 + r2, 2);
    }

    private Vector resolveCollision(Vector p, Vector v, Vector p2){
        return Vector.mult(
                Vector.normalize(
                        Vector.sub(p, p2)), v.mag());
    }

    private void handleKeys(){
        for(int i = 0; i < keys.size(); i++){
            switch (GameState){
                case MENU:
                    switch(keys.get(i)){
                        case KeyEvent.VK_ENTER:
                            GameState = GAME_STATES.PLAY;
                            break;
                    }
                    break;
                case PLAY:
                    switch(keys.get(i)){
                        case KeyEvent.VK_UP:
                            a = Vector.unit2D((float)Math.toRadians(-90));
                            a.mult(push);
                            break;
                        case KeyEvent.VK_DOWN:
                            a = Vector.unit2D((float)Math.toRadians(90));
                            a.mult(push);
                            break;
                        case KeyEvent.VK_LEFT:
                            a = Vector.unit2D((float)Math.PI);
                            a.mult(push);
                            break;
                        case KeyEvent.VK_RIGHT:
                            a = Vector.unit2D(0);
                            a.mult(push);
                            break;
                    }
                    break;
                case SCORE:
                    break;
            }

        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if(!keys.contains(keyEvent.getKeyCode()))
            keys.add(keyEvent.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        for(int i = keys.size() - 1; i >= 0; i--){
            if(keyEvent.getKeyCode() == keys.get(i))
                keys.remove(i);
        }
    }

    /*
         * run()
         * calls init() to initialize variables
         * loops using isRunning
            * updates all timing variables and then calls update() and draw()
            * dynamically sleeps the main thread to maintain a framerate close to target fps
         */
    public void run(){
        init();

        while(isRunning){

            //new loop, clock the start
            startFrame = System.currentTimeMillis();

            //calculate delta time
            dt = (float)(startFrame - lastFrame)/1000;

            //update lastFrame for next dt
            lastFrame = startFrame;

            //call update and draw methods
            update();
            draw();

            //dynamic thread sleep, only sleep the time we need to cap the framerate
            //rest = (max fps sleep time) - (time it took to execute this frame)
            rest = (1000/MAX_FPS) - (System.currentTimeMillis() - startFrame);
            if(rest > 0){ //if we stayed within frame "budget", sleep away the rest of it
                try{ Thread.sleep(rest); }
                catch (InterruptedException e){ e.printStackTrace(); }
            }
        }

    }

    //entry point for application
    public static void main(String[] args){
        Game game = new Game(800, 600, 60);
        game.run();
    }

}
