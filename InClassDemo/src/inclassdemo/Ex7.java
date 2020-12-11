package inclassdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * I created this for Ex6, but I was super excited to find out I had also
 * completed Ex7 at the same time!
 *
 * @author Tim Barber
 *
 * All work herein is exclusively the product of the author, unless otherwise
 * stated.
 */
public class Ex7 {

    public static void main(String[] args) {
        int count = 0;
        int start = 1;
        int end = 100001; // first 100,000 *positive* integers, and 0 is not positive or negative

        if (!testIsPrime()) {
            System.out.println("Unable to verify authenticity of isPrime() function, proceeding as normal.\n");
        }
        if (!testIsPalindromic()) {
            System.out.println("Unable to verify authenticity of isPalindromic() function, proceeding as normal.\n");
        }

        System.out.println("Palindromic primes between " + start + " and " + end + "\n\nCount\tValue");
        for (int i = start; i <= end; i++) { // loop between start and end, inclusive
            if (isPalindromic(i, false) && isPrime(i)) { // if number is palindromic and prime...
                count++; // count it
                System.out.println(count + ": \t" + i); // and print it
            }
        }
    }

    public static boolean isPalindromic(int number, boolean debug) {
        number = Math.abs(number); // support for negative numbers
        int numDigits = (int) (Math.floor(Math.log10(number)) + 1.0); // the number of digits is equal to the floor( of the base10 log of a number + 1)
        if (debug) {
            System.out.println("\tnumDigits in " + number + ": " + numDigits);
            System.out.println("\t\tCalculated by taking the log base 10 of " + number + " which returns " + Math.log10(number) + " then flooring that which makes " + Math.floor(Math.log10(number)) + " then adding 1 to finally give " + (Math.floor(Math.log10(number)) + 1) + ".");
        }
        for (int i = 0; i < Math.floor(numDigits / 2); i++) { // loop through the outer two digits, moving inwards as we go, ignoring a middle number if there is one
            int leftDigit = (int) (Math.floor(number % Math.pow(10, numDigits - i) / Math.pow(10, numDigits - (i + 1)))); // i'd rather not share how long these two lines took to perfect
            int rightDigit = (int) (Math.floor(number % Math.pow(10, (i + 1)) / Math.pow(10, i)));
            if (leftDigit != rightDigit) { // if the outer digits are not the same, it's not a palindrome
                if (debug) {
                    System.out.println("\tDigits " + (i + 1) + " and " + (numDigits - i) + " from the left in " + number + " which are " + leftDigit + " and " + rightDigit + " respectively, are NOT considered matching.");
                    System.out.println("\ti is " + i);
                }
                return false;
            }
            if (debug) {
                System.out.println("\tDigits " + (i + 1) + " and " + (numDigits - i) + " from the left in " + number + " which are " + leftDigit + " and " + rightDigit + " respectively, are considered matching.");
                System.out.println("\ti is " + i);
            }
        }
        return true;
    }

    public static boolean testIsPalindromic() {
        int[] testCases = {0, 1, 6, 9, 11, 13, 17, 22, 28, 33, 54, 66, 98, 99, 100, 101, 102, 121, 125, 189, 191, 999, 1000, 1001, 1010, 1100, 1110, 1111, 2002, 2202, 2222, 2552, 9998, 9999, 10000, 10001, 12345, 14541, 45654, 99988999};
        boolean[] expectedResults = {true, true, true, true, true, false, false, true, false, true, false, true, false, true, false, true, false, true, false, false, true, true, false, true, false, false, false, true, true, false, true, true, false, true, false, true, false, true, true, true};
        int passedTests = 0;
        int failedTests = 0;
        String result = "";
        System.out.println("Beginning testing of isPalindromic()");
        for (int i = 0; i < testCases.length; i++) {
            if (isPalindromic(testCases[i], false) != expectedResults[i]) {
                System.out.println("\nFAILED isPalindromic(" + testCases[i] + "), expected " + expectedResults[i] + ", received " + isPalindromic(testCases[i], false) + ". Debug notes: ");
                isPalindromic(testCases[i], true);
                failedTests++;
            } else {
                System.out.print(". ");
                passedTests++;
            }
        }
        System.out.println("\nTesting of isPalindromic() is complete.");
        if (failedTests > 0) {
            result = "FAILED";
        } else {
            result = "PASSED";
        }
        System.out.println("\nTests failed: " + failedTests + "\nTests passed: " + passedTests + "\nPercentage: " + (passedTests * 100.0 / (passedTests + failedTests + 0.0)) + "\nisPalindromic() test " + result + "\n");
        return failedTests == 0;
    }

    public static void takeInputForIsPalindromic() {
        Scanner s = new Scanner(System.in);
        String lastCommand = "";
        System.out.println("Input a number to test as a palindrome, type \'exit\' to end testing.");
        while (!lastCommand.equals("exit")) {
            System.out.print("isPalindromic: ");
            lastCommand = s.nextLine();
            int input = Integer.valueOf(lastCommand);
            String output = "";
            if (isPalindromic(input, false)) {
                output = " is a palindrome.";
            } else {
                output = " is not a palindrome.";
            }
            System.out.println(input + output);
        }
    }

    public static boolean isPrime(int number) { // no boolean for debug as this method is very straighforward and that would be overly complicated
        for (int i = 2; i < number; i++) { // starting at 2, no prime number can be equally divised by any positive integer except itself
            if (number % i == 0) { // if the number is equally divised...
                return false; // it ain't prime
            }
        }
        if (number > 1 && number / number == 1) { // double check that the number is positive and divisible by itself (order is important, otherwise will error on 0)
            return true; // it's prime
        }
        return false; // number is negative or 0
    }

    public static boolean testIsPrime() {
        File primesTo100k = new File("rsrc/primes-to-100k-no-commas.dat");
        Scanner primes = new Scanner("File \"rsrc/primes-to-100k-no-commas.dat\" not found!");
        try {
            primes = new Scanner(primesTo100k);
        } catch (FileNotFoundException x) {
            System.out.println("Testing of isPrime() unable to proceed, data file not found.");
            return false;
        }
        System.out.println("Beginning testing of isPrime()");
        int nextPrime = primes.nextInt();
        int passedTests = 0;
        int failedTests = 0;
        String result = "";
        for (int i = 0; i < 10000; i++) {
            if (i > nextPrime) {
                if (primes.hasNextInt()) {
                    nextPrime = primes.nextInt();
                } else {
                    break;
                }
            }
            if (isPrime(i) != (nextPrime == i)) {
                System.out.println("\nFAILED isPrime(" + i + "), expected " + (nextPrime == i) + " but received " + isPrime(i) + ".");
                failedTests++;
            } else {
                System.out.print(".");
                passedTests++;
            }
        }
        System.out.println("\nTesting of isPrime() is complete.");
        if (failedTests > 0) { // our standard for prime testing is very high UwU
            result = "FAILED";
        } else {
            result = "PASSED";
        }
        System.out.println("\nTests failed: " + failedTests + "\nTests passed: " + passedTests + "\nPercentage: " + (passedTests * 100.0 / (passedTests + failedTests + 0.0)) + "\nisPrime() test " + result + "\n");
        return failedTests == 0;
    }
}
