package inclassdemo;

import java.util.ArrayList;
import java.util.Scanner;

/*
 *
 * @author Tim Barber
 */
public class Ex2 {

    public static void main(String[] args) {
        System.out.print("Enter a positive integer: ");
        Scanner s = new Scanner(System.in);
        int x = s.nextInt();
        ArrayList<Integer> sum = new ArrayList<>();
        String out = "The sum of the digits is++++";
        for (int i = 0; i < 5; i++) {
            sum.add(x % 10);
            x /= 10;
            out = out.substring(0, out.length() - (i * 4)) + " " + sum.get(i) + " " + out.substring(out.length() - (i * 4));
        }
        System.out.println(out + "= " + sum.stream().mapToInt(Integer::intValue).sum());
    }
}