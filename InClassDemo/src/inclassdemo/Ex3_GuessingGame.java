package inclassdemo;

import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Tim Barber
 *
 * This program will generate a random integer between 1 & 10000 inclusive and
 * repeatedly ask the user for a guess until they get it exactly right, letting
 * them know if they are too high or low and keeping track of the valid guesses
 * for them.
 */
public class Ex3_GuessingGame {

    private static final int LOW_BOUND = 1;
    private static final int HIGH_BOUND = 10000;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        Random rand = new Random();

        int lowBound = LOW_BOUND; // inclusive
        int highBound = HIGH_BOUND; // inclusive
        int userGuess = lowBound - 1; // hopefully the lowBound isn't the lowest possible value for its type

        int secretNum = rand.nextInt(highBound - lowBound) + lowBound; // the secret number will be a pseudorandom int between lowbound and highbound, inclusive

        while (userGuess != secretNum) { // loop while the user has not guessed the number exactly
            System.out.print("The secret number is between " + lowBound + " and " + highBound + ". (inclusive) Your guess: ");
            userGuess = scan.nextInt(); // get user guess

            if (userGuess >= lowBound && userGuess <= highBound) { // if the guess is valid
                if (userGuess < secretNum) { // if they guessed too low
                    lowBound = userGuess + 1;
                    System.out.println("Too low, try again.");
                } else if (userGuess > secretNum) { // if they guessed too high
                    highBound = userGuess - 1;
                    System.out.println("Too high, try again.");
                } else {
                    System.out.print("You got it!. The secret number was " + secretNum + ". Would you like to try again? (y/n): ");
                    boolean again = scan.next().toLowerCase().equals("y"); // valid choices to play again are "y" and "Y"
                    if (again) {
                        // reset all of the runtime vars
                        lowBound = LOW_BOUND;
                        highBound = HIGH_BOUND;
                        userGuess = LOW_BOUND - 1;
                        rand.setSeed(System.nanoTime());
                        secretNum = rand.nextInt(highBound - lowBound) + lowBound;
                    } else { // if the user typed something other than "y" or "Y",
                        System.exit(0); // exit the program
                    }
                }
            }
        }
    }
}