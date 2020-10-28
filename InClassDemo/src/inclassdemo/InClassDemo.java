package inclassdemo;

import java.util.Scanner;

/**
 *
 * @author Tim Barber
 */
public class InClassDemo {

    public static void main(String[] args) {
        System.out.println("\nTim Barber\tSep 10, 2020\tCST-105"); //Header

        double taxRate = 0.065;
        double cost = 0;
        Scanner s = new Scanner(System.in);
        System.out.println("Enter an item cost : $");
        cost = s.nextDouble();
        double totalTax = cost * (1 + taxRate);
        System.out.println("The total price after tax is $" + totalTax + ".");

    }

}
