package inclassdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This main program contains several methods for converting English speech into
 * "Pig Latin", a form of speech that can be tricky to decipher to those that
 * don't know the trick. Pig Latin involves taking any consonants from the
 * beginning of the word, adding them to the end, and then adding "ay" or "way",
 * whichever is phonetically appropriate.
 *
 * @author Tim Barber
 */
public class Ex4_PigLatin {

    private static final Character[] VOWELS = {'a', 'e', 'i', 'o', 'u'};
    private static final String DIALECT_HARD = "way";
    private static final String DIALECT_SOFT = "ay";

    public static void main(String[] args) {
        boolean demo = false;
        if (demo) {
            demo();
        } else {
            translateFromFile(new File("rsrc/piglatintest.dat"));
        }
    }

    private static void demo() {
        File userFile = new File("rsrc/EX4.dat");
        Scanner scan = new Scanner("Your file could not be found/read.");
        try {
            scan = new Scanner(userFile);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        while (scan.hasNext()) {
            String tempWord = scan.next();
            System.out.println(tempWord + "\t\t" + englishToPigLatin(tempWord).toUpperCase());
        }
    }

    private static void translateFromFile(File file) {
        Scanner scan = new Scanner("Your file could not be found/read.");
        try {
            scan = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        scan.useDelimiter("\\Z");

        String original = scan.next();
        System.out.println(original);
        System.out.println(englishToPigLatin(original, true));
    }

    public static String englishToPigLatin(String englishSentence) {
        return englishToPigLatin(englishSentence, false);
    }

    public static String englishToPigLatin(String englishSentence, boolean useProperCase) {
        Scanner s = new Scanner(englishSentence);
        String remaining = englishSentence;
        String out = "";
        while (s.hasNext()) {
            String next = s.next();
            out += remaining.substring(0, remaining.indexOf(next)) + englishWordToPigLatin(next, useProperCase);
            remaining = remaining.substring(remaining.indexOf(next) + next.length());
        }
        return out;
    }

    private static boolean isVowel(String letter, String before, String after) {
        if (contains(VOWELS, letter.charAt(0)) >= 0) {
            return true;
        } else {
            if (Character.toLowerCase(letter.charAt(0)) == 'y') {
                if ((before.isEmpty() && (contains(VOWELS, after.charAt(0))) == -1) || (after.isEmpty() && (contains(VOWELS, before.charAt(before.length() - 1)) == -1))) {
                    return true;
                }
                if (contains(VOWELS, after.charAt(0)) == -1 && contains(VOWELS, before.charAt(before.length() - 1)) == -1) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isVowel(char letter, String before, String after) {
        return isVowel(String.valueOf(letter), String.valueOf(before), String.valueOf(after));
    }

    public static String englishWordToPigLatin(String englishWord) {
        return englishWordToPigLatin(englishWord, false);
    }

    private static int contains(Object[] list, Object item) {
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public static String englishWordToPigLatin(String englishWord, boolean useProperCase) {
        // tempStr is the working buffer so that "englishWord" is never mutated
        String tempStr = englishWord;

        String nonLettersPre, nonLettersPost, pigPrefix, pigSuffix, dialectVariant;
        nonLettersPre = nonLettersPost = pigPrefix = pigSuffix = dialectVariant = "";

        //strip punctuation from beginning and save for later
        for (int i = 0; i < tempStr.length(); i++) {
            if (Character.isAlphabetic(tempStr.charAt(i))) {
                break;
            } else {
                nonLettersPre += tempStr.charAt(i);
                if (i == tempStr.length() - 1) { // there are no alphabetic characters, we've been played
                    return englishWord;
                }
            }
        }

        //strip punctuation from end and save for later
        for (int i = tempStr.length() - 1; i >= 0; i--) {
            if (Character.isAlphabetic(tempStr.charAt(i))) {
                break;
            } else {
                nonLettersPost = tempStr.charAt(i) + nonLettersPost;
            }
        }

        // chop off the non-alphabetics from our working variable
        tempStr = tempStr.substring(nonLettersPre.length(), tempStr.length() - nonLettersPost.length());

        // find the first vowel and use it to split the word into the 2 parts
        int i = 0; // index
        String before = "";
        String after = "";
        for (i = 0; i < tempStr.length(); i++) {
            if (i == 0) {
                before = "";
            } else {
                before = String.valueOf(tempStr.charAt(i - 1));
            }
            if (i == tempStr.length() - 1) {
                after = "";
            } else {
                after = String.valueOf(tempStr.charAt(i + 1));
            }
            if (isVowel(Character.toLowerCase(tempStr.charAt(i)), before, after)) {
                pigSuffix = tempStr.substring(0, i);
                pigPrefix = tempStr.substring(i);
                break;
            }
        }

        dialectVariant = (contains(VOWELS, tempStr.charAt(0)) >= 0 ? DIALECT_HARD : DIALECT_SOFT);

        if (useProperCase) {
            boolean allCaps = false;
            boolean capped = false;
            if (tempStr.length() >= 1 && Character.isUpperCase(tempStr.charAt(0))) {
                if (tempStr.length() >= 2 && Character.isUpperCase(tempStr.charAt(1))) {
                    allCaps = true;
                } else {
                    capped = true;
                }
            }
            if (allCaps) {
                pigPrefix = pigPrefix.toUpperCase();
                pigSuffix = pigSuffix.toUpperCase();
                dialectVariant = dialectVariant.toUpperCase();
            } else if (capped) {
                pigPrefix = Character.toUpperCase(pigPrefix.charAt(0)) + pigPrefix.substring(1).toLowerCase();
                pigSuffix = pigSuffix.toLowerCase();
            } else {
                pigPrefix = pigPrefix.toLowerCase();
                pigSuffix = pigSuffix.toLowerCase();
            }
        }
        return nonLettersPre + pigPrefix + pigSuffix + dialectVariant + nonLettersPost;
    }
}
