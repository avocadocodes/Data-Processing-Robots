import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;

public class MarsEnv extends Environment {

    public static final int GSize = 20; // grid size
    public static final int GARB  = 16; // garbage code in grid model
    public static final int GARBCOUNT  = 26; // garbage code in grid model

    public static final Term    ns = Literal.parseLiteral("next(slot)");
    public static final Term    pg = Literal.parseLiteral("pick(garb)");
    public static final Term    dg = Literal.parseLiteral("drop(garb)");
    public static final Term    bg = Literal.parseLiteral("burn(garb)");
    public static final Literal g1 = Literal.parseLiteral("garbage(r1)");
    public static final Literal g2 = Literal.parseLiteral("garbage(r2)");
    public static final Literal g3 = Literal.parseLiteral("garbage(r3)");

    public int[][] garbageCount;


    static Logger logger = Logger.getLogger(MarsEnv.class.getName());

    private MarsModel model;
    private MarsView  view;

    @Override
    public void init(String[] args) {
        model = new MarsModel();
        view  = new MarsView(model);
        model.setView(view);
        updatePercepts();
        // try {
        //     Thread.sleep(5000);
        // } catch (Exception e) {}
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
            if (action.equals(ns)) {
                model.nextSlot();
            } else if (action.getFunctor().equals("move_towards")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.moveTowards(x,y);
            } else if (action.equals(pg)) {
                model.pickGarb();
            } else if (action.equals(dg)) {
                model.dropGarb();
            } else if (action.equals(bg)) {
                model.burnGarb();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updatePercepts();

        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }

    /** creates the agents perception based on the MarsModel */
    void updatePercepts() {
        clearPercepts();

        Location r1Loc = model.getAgPos(0);
        Location r2Loc = model.getAgPos(1);
        Location r3Loc = model.getAgPos(2);

        Literal pos1 = Literal.parseLiteral("pos(r1," + r1Loc.x + "," + r1Loc.y + ")");
        Literal pos2 = Literal.parseLiteral("pos(r2," + r2Loc.x + "," + r2Loc.y + ")");
        Literal pos3 = Literal.parseLiteral("pos(r3," + r3Loc.x + "," + r3Loc.y + ")");
        Literal gridsize = Literal.parseLiteral("grid_size(" + (GSize-1) + "," + (GSize-1) + ")");
        
        addPercept(gridsize);
        addPercept(pos1);
        addPercept(pos2);
        addPercept(pos3);

        if (model.hasObject(GARB, r1Loc)) {
            addPercept(g1);
        }
        if (model.hasObject(GARB, r2Loc)) {
            addPercept(g2);
        }
        if (model.hasObject(GARB, r3Loc)) {
            addPercept(g3);
        }
    }

    class MarsModel extends GridWorldModel {

        public static final int MErr = 2; // max error in pick garb
        int nerr; // number of tries of pick garb
        boolean r1HasGarb = false; // whether r1 is carrying garbage or not
        public static final int MAX_GARBAGE_IN_CELL = 4;
        public static final int MIN_GARBAGE_IN_CELL = 1;
        // private int[][] garbageCount;

        Random random = new Random(System.currentTimeMillis());

        private MarsModel() {
            super(GSize, GSize, 3);
            garbageCount = new int[GSize][GSize];

            // initial location of agents
            try {
                setAgPos(0, 0, 0);

                Random ran = new Random();
                Location r2Loc = new Location(ran.nextInt(GSize), ran.nextInt(GSize));
                // Location r2Loc = new Location(2, 10);
                setAgPos(1, r2Loc);
                Location r3Loc = new Location(ran.nextInt(GSize), ran.nextInt(GSize));
                // Location r3Loc = new Location(17, 11);
                setAgPos(2, r3Loc);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Random ran = new Random();
            // initial location of garbage
            for(int i = -1; ++i < GARBCOUNT; ) {
                // add(GARB, ran.nextInt(GSize), ran.nextInt(GSize));
                int x = ran.nextInt(GSize);
                int y = ran.nextInt(GSize);
                add(GARB, x, y); // Add garbage to the grid
                garbageCount[x][y] = ran.nextInt(MAX_GARBAGE_IN_CELL - MIN_GARBAGE_IN_CELL + 1) + MIN_GARBAGE_IN_CELL;
            }
            // add(GARB, 3, 0);
            // add(GARB, GSize-1, 0);
            // add(GARB, 1, 2);
            // add(GARB, 0, GSize-2);
            // add(GARB, GSize-1, GSize-1);
        }

        void nextSlot() throws Exception {
            Location r1 = getAgPos(0);
            r1.x++;
            if (r1.x == getWidth()) {
                r1.x = 0;
                r1.y++;
            }
            // finished searching the whole grid
            if (r1.y == getHeight()) {
                return;
            }
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
            setAgPos(2, getAgPos(2)); // just to draw it in the view
        }

        void moveTowards(int x, int y) throws Exception {
            Location r1 = getAgPos(0);
            if (r1.x < x)
                r1.x++;
            else if (r1.x > x)
                r1.x--;
            if (r1.y < y)
                r1.y++;
            else if (r1.y > y)
                r1.y--;
            setAgPos(0, r1);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
            setAgPos(2, getAgPos(2)); // just to draw it in the view
        }

        void pickGarb() {
            // r1 location has garbage
            Location r1Loc = getAgPos(0);
            System.out.println(r1HasGarb+" "+garbageCount[r1Loc.x][r1Loc.y]);
            if (garbageCount[r1Loc.x][r1Loc.y] > 0) {
                garbageCount[r1Loc.x][r1Loc.y]--; // Decrease garbage count
                r1HasGarb = true;
                
                
                if (garbageCount[r1Loc.x][r1Loc.y] == 0) {
                    remove(GARB, r1Loc); // Remove garbage only after 3 picks      
                }

            }
        }
        // void pickGarb() {
        //     // r1 location has garbage
        //     if (model.hasObject(GARB, getAgPos(0))) {
        //         // sometimes the "picking" action doesn't work
        //         // but never more than MErr times
        //         if (random.nextBoolean() || nerr == MErr) {
        //             remove(GARB, getAgPos(0));
        //             nerr = 0;
        //             r1HasGarb = true;
        //         } else {
        //             nerr++;
        //         }
        //     }
        // }
        void dropGarb() {
            if (r1HasGarb) {
                r1HasGarb = false;
                add(GARB, getAgPos(0));
            }
        }
        void burnGarb() {
            // r2 location has garbage
            if (model.hasObject(GARB, getAgPos(1))) {
                remove(GARB, getAgPos(1));
            }
            if (model.hasObject(GARB, getAgPos(2))) {
                remove(GARB, getAgPos(2));
            }
        }
    }

    class MarsView extends GridWorldView {

        public MarsView(MarsModel model) {
            super(model, "Mars World", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
            case MarsEnv.GARB:
                drawGarb(g, x, y, garbageCount[x][y]);
                break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "T"+(id+1);
            c = Color.blue;
            if (id == 0) {
                c = Color.yellow;
                if (((MarsModel)model).r1HasGarb) {
                    label += " - D";
                    c = Color.orange;
                }
            } else {
                label = "P"+(id);
            }
            
            super.drawAgent(g, x, y, c, -1);
            if (id == 0) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.white);
            }
            super.drawString(g, x, y, defaultFont, label);
            repaint();
        }

        public void drawGarb(Graphics g, int x, int y, int count) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.white);
            drawString(g, x, y, defaultFont, "D"+count);
        }

    }
}
