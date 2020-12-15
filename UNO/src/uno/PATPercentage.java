package uno;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author Tim Barber
 */
public class PATPercentage {

    public static void main(String[] args) {
        Long total = Long.MAX_VALUE;
        try {
            File myObj = new File("lastseed.dat");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                total += Long.valueOf(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.out.println("lows: " + total);
        Long highTemp = Long.MIN_VALUE;

        try {
            File myObj = new File("lastseedTop.dat");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                highTemp += Long.valueOf(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        System.out.println("highs: " + Math.abs(highTemp));
        Long midTemp = Long.valueOf(0);
        try {
            File myObj = new File("lastseedMid.dat");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                midTemp += Long.valueOf(data) * 2;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.out.println("mids: " + midTemp);

        Long unOverflow = Long.valueOf(25);
        total += Math.abs(highTemp) + midTemp;
        System.out.println("total: " + total);
        Long percentage = (total / unOverflow) / ((Long.MAX_VALUE / unOverflow) * 2) * 100 * unOverflow;
        System.out.println(percentage + "%");
        System.out.println("Long max val: " + Long.MAX_VALUE);
    }
}
