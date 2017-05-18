import com.sun.istack.internal.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

/**
 * Created by kathancheema on 18/05/2017.
 */
public class Solve {

    private static BufferedImage maze;
    private static int w;
    private static int h;


    public static void main(String[] args) {

        try {
            maze = getImage(args[0]);
            Color colours[] = getPixelColours(maze);
            findPoints(maze);
//            for (int pixel: maze.getRGB(0,0, w, h, null, 0, w)) {
////                System.out.println("Pixel " + ++count + " " + new Color(pixel).toString());
//            }
        } catch (IOException e) {
            System.out.println("Couldn't read: " + args[0] + "\n" + e);
            System.exit(1);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No args, exiting");
            System.exit(1);
        }

    }

    private static void findPoints(BufferedImage maze) {

        int startX = 0;
        int startY = 0;
        int endX = 0;
        int endY = 0;
        for (int i = 0; i < w; i ++) {
            Color col = new Color(maze.getRGB(i, h/2));

            if (col != Color.WHITE) {
                System.out.println("Found black x = " + i + " y = " + h/2);
                break;
            }
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

    private static void outputMaze(Color maze[]) {
        int rbgs[] = new int[1];

        for (int i = 0; i < maze.length; i++) {
            rbgs[i] = maze[i].getRGB();
        }

        BufferedImage solved = new BufferedImage(Solve.maze.getWidth(), Solve.maze.getHeight(), Solve.maze.getType());

        try {
            ImageIO.write(solved, "jpg", new File("output.jpg"));
        } catch (IOException e) {
            System.out.println("Failed to output");
        }

    }

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
