package inclassdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author Tim Barber
 */
public class Ex5_MatrixCipher {

    public static void main(String[] args) {
        demo();
    }

    private static void demo() {
        Scanner s = new Scanner("File not found / couldn't be read.");

        try {
            s = new Scanner(new File("rsrc/EX5.dat"));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        System.out.println(doStuff(s.nextLine(), 6, 7));
    }

    public static String doStuff(String input, int width, int height) {
        Character[][] matrix = new Character[height][width]; // insert "the matrix" joke here
        String inputButWithStars = input.replaceAll(" ", "*");
        String out = "";

        // place inputButWithStars into the matrix, so that you could read it LTR like normal
        int tempIndice = 0;
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                matrix[r][c] = inputButWithStars.charAt(tempIndice);
                tempIndice++;
            }
        }

        System.out.println(prettyPrintMatrix(matrix));

        // read the matrix top-down, then LTR to cipher it
        for (int c = 0; c < matrix[0].length; c++) {
            for (int r = 0; r < matrix.length; r++) {
                out += matrix[r][c];
            }
        }

        return out;
    }

    private static String prettyPrintMatrix(Object[][] matrix) { // the fact that this method isn't in the base library irks me
        String out = "";

        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                out += matrix[r][c].toString();
            }
        }

        return out;
    }
}
