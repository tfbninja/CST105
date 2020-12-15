package uno;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Tim Barber
 */
public class PATTester {

    public static void main(String[] args) {

        String PAT = "be22e06bf0fb2c206c03c531c97e57da1d614718";
        String generated = "";

        ArrayList<Character> list = new ArrayList<>();
        for (char c : PAT.toCharArray()) {
            list.add(c);
        }
        Collections.shuffle(list, new Random(1000));
        String randomized = "";
        for (Character c : list) {
            randomized += c;
        }
        System.out.println("randomized: " + randomized);

        ArrayList<Long> millisAv = new ArrayList<>();
        long last = System.currentTimeMillis();
        int movingAvSize = 50;
        int total = 0;

        long seed = Long.MIN_VALUE;

        try {
            File myObj = new File("lastseed.dat");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                seed = Long.valueOf(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        int updatePeriod = 500000;
        long predictedWait = 0;
        int unOverflower = 10000000;

        while (!generated.equals(PAT)) {
            generated = "";
            list.clear();
            for (char c : PAT.toCharArray()) {
                list.add(c);
            }
            Collections.shuffle(list, new Random(1000));
            Random rand = new Random(seed);
            Collections.shuffle(list, rand);
            for (Character c : list) {
                generated += c;
            }

            seed++;
            if (seed % updatePeriod == 0) {

                millisAv.add(Math.abs(System.currentTimeMillis() - last));
                last = System.currentTimeMillis();
                if (millisAv.size() > movingAvSize) {
                    millisAv.remove(0);
                }
                for (Long l : millisAv) {
                    total += l;
                }
                predictedWait = ((((long) (total / (double) millisAv.size()) * ((Long.MAX_VALUE / unOverflower) - (seed / unOverflower))) / updatePeriod) / 1000) / 60 * unOverflower / 3; // predicted number of minutes until finish
                System.out.println(seed + "  predicted minutes until all longs have been tried: " + predictedWait);
                // The following code is a minimally modified excerpt from https://www.w3schools.com/java/java_files_create.asp
                try {
                    try (FileWriter myWriter = new FileWriter("lastseed.dat", false)) {

                        myWriter.write(seed + "");

                        myWriter.flush();
                        myWriter.close();
                    }
                } catch (IOException e) {
                    System.out.println("An error occurred while writing.");
                    e.printStackTrace();

                }
            }
        }
        System.out.println("PAT: " + PAT);
        System.out.println("gen: " + generated);
        System.out.println(seed);
    }
}
