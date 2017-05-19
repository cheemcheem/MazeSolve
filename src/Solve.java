import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Solves Mazes.
 */
public class Solve {

    private static int w;
    private static int h;

    private static int entryX;
    private static int entryY;
    private static int exitX;
    private static int exitY;

    public static void main(String[] args) {
        try {
            BufferedImage maze = getImage(args[0]);
            Color colours[] = getPixelColours(maze);
            findPoints(maze);
            Solver(maze, entryX, entryY);

//            draw(0,350,40,350,maze);
//            ImageIO.write(maze, "jpg", new File("output.jpg"));

//            for (int pixel: maze.getRGB(0,0, w, h, null, 0, w)) {
////                System.out.println("Pixel " + ++count + " " + new Color(pixel).toString());
//            }
        } catch (IOException e) {
            System.out.println("Couldn't read/write: " + args[0] + "\n" + e);
            System.exit(1);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No args, exiting");
            System.exit(1);
        }

    }

    private static void output(BufferedImage maze) throws IOException {
        ImageIO.write(maze, "png", new File("output.png"));
    }

    private static void draw(int startX, int startY, int endX, int endY, BufferedImage maze) {
        int xDiff = endX - startX;
        int yDiff = endY - startY;

        int xMult = (xDiff >= 1) ? 1 : -1;
        int yMult = (yDiff >= 1) ? 1 : -1;

        if (yDiff > 0 && xDiff > 0) {
            System.out.println("Multiple Diffs");
            System.exit(1);
        }

        if (xDiff > 0) {
            for (int i = 0; i < xDiff; i++) {
                maze.setRGB(startX + (i * xMult), startY, Color.RED.getRGB());
            }
        } else if (yDiff > 0) {
            for (int i = 0; i < yDiff; i++) {
                maze.setRGB(startX, startY + (i * yMult), Color.RED.getRGB());
            }
        }
    }

    private static void findPoints(BufferedImage maze) {

        // Looking for x,y on left side for entrance
        for (int x = 0; x < w; x++) {
            Color col = new Color(maze.getRGB(x, h / 2));
            if (col.equals(Color.BLACK)) {
                System.out.println("Found entry wall x = " + x + " y = " + h / 2);
                entryX = x;
                break;
            }
        }
        if (entryX == 0) {
            System.out.println("No entrance wall" + h + " " + w);
            System.exit(1);
        }
        boolean passedBottomGap = false;
        for (int y = 0; y < h; y++) {
            Color col = new Color(maze.getRGB(entryX, y));
            if (passedBottomGap) {

                if (col.equals(Color.WHITE)) {
                    if (entryY == 0) {
                        System.out.println("Found entry opening x = " + entryX + " y = " + y);
                        entryY = y;
                        break;
                    }
                }

            } else {
                if (col.equals(Color.BLACK)) {
                    passedBottomGap = true;
                }
            }
        }

        if (entryY == 0) {
            System.out.println("No entrance opening");
            System.exit(1);
        }

        // Looking for x,y on right side for exit
        for (int x = 0; x < w; x++) {
            Color col = new Color(maze.getRGB(w - x - 1, h / 2));
            if (col.equals(Color.BLACK)) {
                System.out.println("Found entry wall x = " + (w - x - 1) + " y = " + h / 2);
                exitX = w - x - 1;
                break;
            }
        }
        if (exitX == 0) {
            System.out.println("No exit wall");
            System.exit(1);
        }

        passedBottomGap = false;
        for (int y = 0; y < h; y++) {
            Color col = new Color(maze.getRGB(exitX, h - y - 1));
            if (passedBottomGap) {
                if (col.equals(Color.WHITE)) {
                    if (exitY == 0) {
                        System.out.println("Found exit opening x = " + exitX + " y = " + (h - y - 1));
                        exitY = h - y - 1;
                        break;
                    }
                }

            } else {
                if (col.equals(Color.BLACK)) {
                    passedBottomGap = true;
                }
            }
        }

        if (exitY == 0) {
            System.out.println("No exit opening");
            System.exit(1);
        }
    }

    private static Color[] getPixelColours(BufferedImage maze) {
        w = maze.getWidth();
        h = maze.getHeight();
        System.out.println("image is " + w + "px wide and " + h + "px tall");

        int count = 0;
        int pixels[] = maze.getRGB(0,0, w, h, null, 0, w);
        Color colours[] = new Color[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            colours[i] = new Color(pixels[i]);
        }
        return colours;
    }

    private static BufferedImage getImage(String Location) throws IOException {
        return ImageIO.read(new File(Location));
    }

    private static void Solver(BufferedImage maze, int x, int y) {
        System.out.println("x = [" + x + "], y = [" + y + "]");
        if (x < entryX) {
            return;
        }
        if (x == exitX && y == exitY) {
            System.out.println("Finished!");
            try {
                output(maze);
            } catch (IOException e) {
                System.out.println("Couldn't output" + e);
            }
            System.exit(0);
        }
        int red = Color.RED.getRGB();
        int black = Color.BLACK.getRGB();

        int fact = 1;
        boolean up = true;
        boolean down = true;
        boolean left = true;
        boolean right = true;

        for (int i = 1; i <= fact; i++) {
            try {
                if (up && (maze.getRGB(x, y + i) == red || maze.getRGB(x, y + i) == black)) {
                    up = false;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                up = false;
            }
            try {
                if (down && (maze.getRGB(x, y - i) == red || maze.getRGB(x, y - i) == black)) {
                    down = false;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                down = false;
            }
            try {
                if (left && (maze.getRGB(x - 1, y) == red || maze.getRGB(x - 1, y) == black)) {
                    left = false;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                left = false;
            }
            try {
                if (right && (maze.getRGB(x + 1, y) == red || maze.getRGB(x + 1, y) == black)) {
                    right = false;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                right = false;
            }
        }

        if (up) {
            BufferedImage mazeCpy = maze.getSubimage(0, 0, w, h);
            draw(x, y, x, y + fact, mazeCpy);
            Solver(mazeCpy, x, y + fact);
        }
        if (down) {
            BufferedImage mazeCpy = maze.getSubimage(0, 0, w, h);
            draw(x, y, x, y - fact, mazeCpy);
            Solver(mazeCpy, x, y - fact);
        }
        if (left) {
            BufferedImage mazeCpy = maze.getSubimage(0, 0, w, h);
            draw(x, y, x - fact, y, mazeCpy);
            Solver(mazeCpy, x - fact, y);
        }
        if (right) {
            BufferedImage mazeCpy = maze.getSubimage(0, 0, w, h);
            draw(x, y, x + fact, y, mazeCpy);
            Solver(mazeCpy, x + fact, y);
        }

    }

//    private static void outputMaze(Color maze[]) {
//        int rbgs[] = new int[maze.length];
//
//        for (int i = 0; i < maze.length; i++) {
//            rbgs[i] = maze[i].getRGB();
//        }
//
//        BufferedImage solved = new BufferedImage(Solve.maze.getWidth(), Solve.maze.getHeight(), Solve.maze.getType());
//
//        try {
//            ImageIO.write(solved, "jpg", new File("output.jpg"));
//        } catch (IOException e) {
//            System.out.println("Failed to output");
//        }
//
//    }

//    private static boolean checkMaze(Color maze[]) {
//
//    }
//
//    private static boolean Solver(Color maze[], int x, int y) {
//        if (checkMaze(maze)) {
//            outputMaze(maze);
//            return true;
//        } else {
//            boolean returnable = false;
//            for(int i = -1; i < 2; i++) {
//                for(int j = -1; j < 2; j++) {
//                    if (j == 0 && i == 0) {continue;}
//                    Color mazeCopy[] = maze.clone();
//                }
//            }
//        }
//    }
}
