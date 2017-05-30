import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;

/**
 * Solves Mazes.
 */
public class Solve implements Runnable {
    private static final int red = Color.RED.getRGB();
    private static final int black = Color.BLACK.getRGB();
    private static final int white = Color.WHITE.getRGB();
    private static int w;
    private static int h;
    private static int entryX;
    private static int entryY;
    private static int exitX;
    private static int exitY;
    private static IndexColorModel cm;
    private static BufferedImage quickestMaze;
    private static int[] quickestPixelMaze;
    private static int quickestMazePathSize;
    private BufferedImage thisMaze;
    private int x;
    private int y;

    private Solve(BufferedImage thisMaze, int x, int y) {
        this.thisMaze = thisMaze;
        this.x = x;
        this.y = y;
    }

    public static void main(String[] args) {
        argsProcessing(args);
//        try {
//            BufferedImage maze = getImage(args[2]);
//            setUp(maze);
////            getPixelColours(maze);
////            pixelSolver(quickestPixelMaze, (entryX + (w * entryY)));
////            output(quickestPixelMaze);
////            threadSolver(maze, entryX, entryY, 0);
////            output(quickestMaze);
//
//            runTests(maze, 1024);
//            output(quickestPixelMaze);
//        } catch (IOException e) {
//            System.out.println("Couldn't read/write: " + args[0] + "\n" + e);
//            System.exit(1);
//        } catch (ArrayIndexOutOfBoundsException e) {
//            System.out.println("No args, exiting");
//            e.printStackTrace();
//            System.exit(1);
//        }
        System.exit(0);

    }

    private static void argsProcessing(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-test":
                        try {
                            setUp(args[i + 2]);
                            runTests(quickestMaze, Integer.parseInt(args[i + 1]));
                            System.exit(0);
                        } catch (IOException e) {
                            System.out.println("Couldn't read/write: " + args[0] + "\n" + e.getMessage());
                            System.exit(1);
                        }
                    case "-f":
                        try {
                            setUp(args[i + 1]);
                            getPixelColours(quickestMaze);
                            pixelSolver(quickestPixelMaze, (entryX + (w * entryY)));
                            System.exit(0);
                        } catch (IOException e) {
                            System.out.println("Couldn't read/write: " + args[0] + "\n" + e.getMessage());
                            System.exit(1);
                        }
                    default:
                        if (i == 1) {
                            try {
                                setUp(args[i]);
                                fastestSolver(quickestMaze, entryX, entryY);
                                System.exit(0);
                            } catch (IOException e) {
                                System.out.println("Couldn't read/write: " + args[0] + "\n" + e.getMessage());
                                System.exit(1);
                            }
                        }
                }
            }
        } catch (ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException e) {
            System.err.println("Invalid Arguments." + e.getMessage());
            System.exit(1);
        }
    }

    private static void setUp(String arg) throws NullPointerException, IOException {
        if (arg == null) throw new NullPointerException("No Maze File.");
        BufferedImage maze = getImage(arg);
        setUp(maze);
    }

    private static void setUp(BufferedImage bi) {
        findPoints(bi);
        quickestMaze = bi;
    }

    private static double memory() {
        return (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
    }

    private static void runTests(BufferedImage maze, float count) throws IOException {
        int cores = Runtime.getRuntime().availableProcessors();
        DecimalFormat df = new DecimalFormat("##.##");
        System.out.println("Running Threaded Test");
        float diffTotalThread = 0;
        double totalMemoryThread = 0;
        for (int i = 0; i < (int) count; i++) {
            Instant before = Instant.now();
            threadSolver(maze, entryX, entryY, cores);
            Instant after = Instant.now();

            if (i % (count / 100) == 0) {
                System.out.print("\r" + (int) (i * 100 / count) + "%");
                System.out.flush();
            }
            float diff = after.toEpochMilli() - before.toEpochMilli();
            diffTotalThread += diff;
            totalMemoryThread += memory();
//            System.out.println("\tTook " + diff + "ms");
            break;
        }
        System.out.print("\r100%\n");
        System.out.println((int) count + " Repetitions Took " + df.format(diffTotalThread / 1000) + "s at an Average of " + df.format(diffTotalThread / count) + "ms Per Repetition");
        System.out.println("Required: " + df.format(totalMemoryThread / (count)) + "MB on average\n");

        System.out.println("Running Fastest Test");

        float diffTotalFast = 0;
        double totalMemoryFast = 0;
        for (int i = 0; i < (int) count; i++) {
            Instant before = Instant.now();
            fastestSolver(maze, entryX, entryY);
            Instant after = Instant.now();
            if (i % ((int) (count / 100)) == 0) {
                System.out.print("\r");
                System.out.print((int) (i * 100 / count) + "%");
                System.out.flush();
//                System.out.print("\r");
            }
            float diff = after.toEpochMilli() - before.toEpochMilli();
            totalMemoryFast += memory();
            diffTotalFast += diff;
//            System.out.println("\tTook " + diff + "ms");
        }
        System.out.print("\r100%\n");
        System.out.println((int) count + " Repetitions Took " + df.format(diffTotalFast / 1000) + "s at an Average of " + df.format(diffTotalFast / count) + "ms Per Repetition");
        System.out.println("Required: " + df.format(totalMemoryFast / (count)) + "MB on average\n");

        float diffTotalPixels = 0;
        double totalMemoryPixels = 0;
        System.out.println("Before Pixels using " + memory() + "MB");
        System.out.println("Running Pixels Test");
        getPixelColours(maze);
        int[] cols = quickestPixelMaze;
        for (int i = 0; i < (int) count; i++) {
            Instant before = Instant.now();
            pixelSolver(cols, (entryX + (w * entryY)));
            Instant after = Instant.now();
            if (i % (count / 100) == 0) {
                System.out.print("\r" + (int) (i * 100 / count) + "%");
                System.out.flush();
            }
            float diff = after.toEpochMilli() - before.toEpochMilli();
            diffTotalPixels += diff;
            totalMemoryPixels += memory();
//            System.out.println("KB: " + ();
//            System.out.println("\tTook " + diff + "ms");
        }
        System.out.print("\r100%\n");
        System.out.println((int) count + " Repetitions Took " + df.format(diffTotalPixels / 1000) + "s at an Average of " + df.format(diffTotalPixels / count) + "ms Per Repetition");
        System.out.println("Required: " + df.format(totalMemoryPixels / (count)) + "MB on average\n");
    }

    private static void findPoints(BufferedImage maze) {

        w = maze.getWidth();
        h = maze.getHeight();

        System.out.println("image is " + w + "px wide and " + h + "px tall");

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
        System.out.println("Entrance (" + entryX + "," + entryY + ")");
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
        System.out.println("Exit (" + exitX + "," + exitY + ")");
        if (exitY == 0) {
            System.out.println("No exit opening");
            System.exit(1);
        }
    }

    private static void getPixelColours(BufferedImage maze) {
        quickestPixelMaze = maze.getRGB(0, 0, w, h, null, 0, w);
        quickestMazePathSize = pathSize(quickestPixelMaze);
    }

    private static int pathSize(BufferedImage maze) {
        int[] cols = maze.getRGB(0, 0, w, h, null, 0, w);
        return pathSize(cols);
    }

    private static int pathSize(int[] cols) {
        int count = 0;
        int total = 0;
        for (; total < cols.length; total++) {
            if (cols[total] == red) {
                count++;
            }
        }
        return count == 0 ? total : count;
    }

    private static void output(BufferedImage maze) throws IOException {
        ImageIO.write(maze, "png", new File("output.png"));
    }

    private static void output(int[] maze) throws IOException {
        if (quickestPixelMaze == null) {
            System.out.println("quickestPIxelMaze is NULL, can't output.");
            return;
        }
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        bi.getRaster().setDataElements(0, 0, w, h, maze);
        output(bi);
    }

    private static BufferedImage getImage(String Location) throws IOException {
        return ImageIO.read(new File(Location));
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private static int[] deepCopy(int[] bi) {
        int[] returnable = new int[bi.length];
        System.arraycopy(bi, 0, returnable, 0, bi.length);
        return returnable;
    }

    private static void draw(int startX, int startY, int endX, int endY, BufferedImage maze) {
//        System.out.println("startX = [" + startX + "], startY = [" + startY + "], endX = [" + endX + "], endY = [" + endY + "]");
        int xDiff = Math.abs(endX - startX);
        int yDiff = Math.abs(endY - startY);

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
//        try {
//            output(maze);
//        } catch (IOException e) {
//
//        }
//        System.out.println();
    }

    private static void draw(int start, int end, int[] maze) {
//        System.out.println("start = [" + start + "], end = [" + end + "], maze.length = [" + maze.length + "]");
        int xDiff = Math.abs(end % w - start % w);
        int yDiff = (int) Math.abs(Math.floor(end / w - start / w));

        int xMult = (xDiff >= 1) ? 1 : -1;
        int yMult = (yDiff >= 1) ? 1 : -1;

        if (yDiff > 0 && xDiff > 0) {
            System.out.println("Multiple Diffs");
            System.exit(1);
        }

        if (xDiff > 0) {
            for (int i = 0; i < xDiff; i++) {
                maze[start + (i)] = red;
            }
        } else if (yDiff > 0) {
            for (int i = 0; i < yDiff; i++) {
                maze[start + (w * i)] = red;
            }
        }
//        try {
//            output(maze);
//        } catch (IOException e) {
//
//        }
//        System.out.println();
    }

    private static void pixelSolver(int[] pixels, int loc) {
        int x = loc % w;
        int y = Math.floorDiv(loc, w);
//        System.out.println("x: " + x + " y: " + y);
        if (x < entryX || x > exitX || y >= h - 1 || y <= 0) {
            return;
        }
        if (x == exitX && y == exitY) {
            draw(loc, loc + 1, pixels);
            int size = pathSize(pixels);
            if (quickestPixelMaze == null) {
                quickestMazePathSize = size;
                quickestPixelMaze = pixels;
            } else if (quickestMazePathSize > size) {
                quickestPixelMaze = pixels;
                quickestMazePathSize = size;
            }
//            System.out.println("Finished! Size = " + size);
        }


        boolean up = true;
        boolean down = true;
        boolean left = true;
        boolean right = true;

        if (y > 1) {
            if (pixels[loc + w] == red || pixels[loc + w] == black) {
                up = false;
            }
        } else up = false;

        if (y < h - 1) {
            if (pixels[loc - w] == red || pixels[loc - w] == black) {
                down = false;
            }
        } else down = false;

        if (x > 1) {
            if (pixels[loc - 1] == red || pixels[loc - 1] == black) {
                left = false;
            }
        } else left = false;

        if (x < w - 1) {
            if (pixels[loc + 1] == red || pixels[loc + 1] == black) {
                right = false;
            }
        } else right = false;


        if (up) {
//            System.out.println("up");
            int[] mazeCpy = deepCopy(pixels);
            draw(loc, loc + w, mazeCpy);
            pixelSolver(mazeCpy, loc + w);
        }
        if (down) {
//            System.out.println("down");
            int[] mazeCpy = deepCopy(pixels);
            draw(loc, loc - w, mazeCpy);
            pixelSolver(mazeCpy, loc - w);
        }
        if (left) {
//            System.out.println("left");
            int[] mazeCpy = deepCopy(pixels);
            draw(loc, loc - 1, mazeCpy);
            pixelSolver(mazeCpy, loc - 1);
        }
        if (right) {
//            System.out.println("right");
            int[] mazeCpy = deepCopy(pixels);
            draw(loc, loc + 1, mazeCpy);
            pixelSolver(mazeCpy, loc + 1);
        }

    }

    private static void efficientSolver(BufferedImage maze, int x, int y) {
        System.out.println("x = [" + x + "], y = [" + y + "]");
        if (x < entryX || x >= w || y <= 0 || y >= h) {
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
//        int red = Color.RED.getRGB();
//        int black = Color.BLACK.getRGB();
        int white = Color.WHITE.getRGB();

        int fact = 10;

        int up = 0;
        int down = 0;
        int left = 0;
        int right = 0;
        try {
            for (int i = 1; i <= fact; i++) {
                if (maze.getRGB(x, y + i) == white) {
                    if (maze.getRGB(x - 1, y + i) != white || maze.getRGB(x + 1, y + i) != white) {
                        up = i;
                    } else break;
                } else {
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        try {
            for (int i = 1; i <= fact; i++) {
                if (maze.getRGB(x, y - i) == white) {
                    if (maze.getRGB(x - 1, y - i) != white || maze.getRGB(x + 1, y - i) != white) {
                        down = i;
                    } else break;
                } else {
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        try {
            for (int i = 1; i <= fact; i++) {
                if (maze.getRGB(x - i, y) == white) {
                    if (maze.getRGB(x - i, y + 1) != white || maze.getRGB(x - i, y - 1) != white) {
                        left = i;
                    } else break;
                } else {
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        try {
            for (int i = 1; i <= fact; i++) {
                if (maze.getRGB(x + i, y) == white) {
                    if (maze.getRGB(x + i, y + 1) != white || maze.getRGB(x + i, y - 1) != white) {
                        right = i;
                    } else break;
                } else {
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        if (up != 0) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x, y + up, mazeCpy);
            Solver(mazeCpy, x, y + up);
        }
        if (down != 0) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x, y - down, mazeCpy);
            Solver(mazeCpy, x, y - down);
        }
        if (left != 0) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x - left, y, mazeCpy);
            Solver(mazeCpy, x - left, y);
        }
        if (right != 0) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x + right, y, mazeCpy);
            Solver(mazeCpy, x + right, y);
        }

    }

    private static void threadSolver(BufferedImage maze, int x, int y, int cores) {
//        System.out.println("x = [" + x + "], y = [" + y + "]");
        if (x < entryX) {
            return;
        }
        if (x == exitX && y == exitY) {
            draw(x, y, x + 1, y, maze);
            int size = pathSize(maze);
            if (quickestMaze == null) {
                quickestMazePathSize = size;
                quickestMaze = maze;
            } else if (quickestMazePathSize > size) {
                quickestMaze = maze;
                quickestMazePathSize = size;
            }
//            System.out.println("Finished! Size = " + size);
        }


        int fact = 1;
        boolean up = true;
        boolean down = true;
        boolean left = true;
        boolean right = true;

        try {
            if (maze.getRGB(x, y + fact) == red || maze.getRGB(x, y + fact) == black) {
                up = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            up = false;
        }
        try {
            if (maze.getRGB(x, y - fact) == red || maze.getRGB(x, y - fact) == black) {
                down = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            down = false;
        }
        try {
            if (maze.getRGB(x - 1, y) == red || maze.getRGB(x - 1, y) == black) {
                left = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            left = false;
        }
        try {
            if (maze.getRGB(x + 1, y) == red || maze.getRGB(x + 1, y) == black) {
                right = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            right = false;
        }

        Thread upThread = null;
        Thread downThread = null;
        Thread leftThread = null;
        Thread rightThread = null;

        if (up) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x, y + fact, mazeCpy);
            upThread = new Thread(new Solve(mazeCpy, x, y + fact));
            upThread.start();
        }
        if (down) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x, y - fact, mazeCpy);
            downThread = new Thread(new Solve(mazeCpy, x, y - fact));
            downThread.start();
        }
        if (left) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x - fact, y, mazeCpy);
            leftThread = new Thread(new Solve(mazeCpy, x - fact, y));
            leftThread.start();
        }
        if (right) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x + fact, y, mazeCpy);
            rightThread = new Thread(new Solve(mazeCpy, x + fact, y));
            rightThread.start();
        }
        if (up) {
            try {
                upThread.join();
            } catch (InterruptedException e) {

            }
        }
        if (down) {
            try {
                downThread.join();
            } catch (InterruptedException e) {

            }
        }
        if (left) {
            try {
                leftThread.join();
            } catch (InterruptedException e) {

            }
        }
        if (right) {
            try {
                rightThread.join();
            } catch (InterruptedException e) {

            }
        }

    }

    private static void fastestSolver(BufferedImage maze, int x, int y) {
//        System.out.println("x = [" + x + "], y = [" + y + "]");
        if (x < entryX) {
            return;
        }
        if (x == exitX && y == exitY) {
            draw(x, y, x + 1, y, maze);
            int size = pathSize(maze);
            if (quickestMaze == null) {
                quickestMazePathSize = size;
                quickestMaze = maze;
            } else if (quickestMazePathSize > size) {
                quickestMaze = maze;
                quickestMazePathSize = size;
            }
//            System.out.println("Finished! Size = " + size);
        }


        int fact = 1;
        boolean up = true;
        boolean down = true;
        boolean left = true;
        boolean right = true;

        try {
            if (maze.getRGB(x, y + fact) == red || maze.getRGB(x, y + fact) == black) {
                up = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            up = false;
        }
        try {
            if (maze.getRGB(x, y - fact) == red || maze.getRGB(x, y - fact) == black) {
                down = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            down = false;
        }
        try {
            if (maze.getRGB(x - 1, y) == red || maze.getRGB(x - 1, y) == black) {
                left = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            left = false;
        }
        try {
            if (maze.getRGB(x + 1, y) == red || maze.getRGB(x + 1, y) == black) {
                right = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            right = false;
        }


        if (up) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x, y + fact, mazeCpy);
            fastestSolver(mazeCpy, x, y + fact);
        }
        if (down) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x, y - fact, mazeCpy);
            fastestSolver(mazeCpy, x, y - fact);
        }
        if (left) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x - fact, y, mazeCpy);
            fastestSolver(mazeCpy, x - fact, y);
        }
        if (right) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x + fact, y, mazeCpy);
            fastestSolver(mazeCpy, x + fact, y);
        }

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

        try {
            if (maze.getRGB(x, y + fact) == red || maze.getRGB(x, y + fact) == black) {
                up = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            up = false;
        }
        try {
            if (maze.getRGB(x, y - fact) == red || maze.getRGB(x, y - fact) == black) {
                down = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            down = false;
        }
        try {
            if (maze.getRGB(x - 1, y) == red || maze.getRGB(x - 1, y) == black) {
                left = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            left = false;
        }
        try {
            if (maze.getRGB(x + 1, y) == red || maze.getRGB(x + 1, y) == black) {
                right = false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            right = false;
        }


        if (up) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x, y + fact, mazeCpy);
            Solver(mazeCpy, x, y + fact);
        }
        if (down) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x, y - fact, mazeCpy);
            Solver(mazeCpy, x, y - fact);
        }
        if (left) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x - fact, y, mazeCpy);
            Solver(mazeCpy, x - fact, y);
        }
        if (right) {
            BufferedImage mazeCpy = deepCopy(maze);
            draw(x, y, x + fact, y, mazeCpy);
            Solver(mazeCpy, x + fact, y);
        }

    }

    public void run() {
        fastestSolver(thisMaze, x, y);
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
