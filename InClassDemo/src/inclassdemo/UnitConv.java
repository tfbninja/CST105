package inclassdemo;

import java.util.Scanner;

/**
 *
 * @author Tim Barber
 */
public class UnitConv {

    public static void main(String[] args) {
        System.out.print("Enter a Farenheit temperature: ");
        Scanner s = new Scanner(System.in);
        double f_to_c = s.nextDouble();
        System.out.println(f_to_c + "F is equivalent to " + ((f_to_c - 32) * 5 / 9.0) + "C");
        System.out.print("Enter a Celsius temperature: ");
        double c_to_f = s.nextDouble();
        System.out.println(c_to_f + "C is equivalent to " + (c_to_f * 9 / 5.0 + 32) + "F");
    }
}