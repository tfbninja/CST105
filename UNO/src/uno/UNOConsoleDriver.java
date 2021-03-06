package uno;

import TCP.UNOServerThread;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.json.*;

/**
 *
 * @author Tim Barber
 */
public class UNOConsoleDriver {

    public static Logger log = new Logger("rsrc/logs/");
    private static ArrayList<Boolean> isAI = new ArrayList<>();
    private static int numPlayers = 0;
    private static boolean ruleStackingSame = true;
    private static boolean ruleStackingAll = ruleStackingSame && true;
    private static boolean ruleDrawTillPlayable = true;
    private static boolean ruleSkipAfterDraw = true;
    private static int handSize = 7;
    private static UNODeck deck;
    private static UNOEngine engine;
    private static final int PORT = 42069;
    private static String hostname = "localhost";
    private static ArrayList<UNOServerThread> connections;
    private static String[] allowedUsernameCharacters = {"_", "!", "-", ".", "~"};
    private static String username = "";
    private static String ID = "";
    private static ArrayList<String> bannedIDList = new ArrayList<>();
    private static int chatDelayMillis = 500;

    public static void main(String[] args) {

        handSize = 7;
        deck = new UNODefaultDeck();
        printHeader();
        importSettings();

        connections = new ArrayList<>();

        Scanner stdin = new Scanner(System.in);
        String joinOrHost = "";
        while (!joinOrHost.equals("client") && !joinOrHost.equals("server") && !joinOrHost.equals("c") && !joinOrHost.equals("s") && !joinOrHost.equals("host") && !joinOrHost.equals("h") && !joinOrHost.equals("join") && !joinOrHost.equals("j")) {
            print("Would you like to host or join? ");
            joinOrHost = stdin.nextLine().trim().toLowerCase();
            if (joinOrHost.equals("exit")) {
                forceExitGame("User typed \"exit\"");
            }
        }
        if (joinOrHost.contains("c") || joinOrHost.contains("j")) {
            stdin = new Scanner(System.in);
            while (true) {
                try ( Socket socket = new Socket(hostname, PORT)) {
                    while (socket.isConnected()) {
                        boolean selfTurn = false;
                        long lastChatMillis = 0;
                        System.out.println("Connected to " + socket.getRemoteSocketAddress().toString());

                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);

                        sendCredentials(writer);

                        String command = "";

                        InputStream input = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                        do {
                            while (reader.ready()) {
                                String line = reader.readLine();
                                if (line.equals("urturn")) {
                                    selfTurn = true;
                                    log.log("turn");
                                } else if (line.equals("noturturn")) {
                                    selfTurn = false;
                                    log.log("Not turn");
                                } else {
                                    System.out.println(line);
                                }
                            }

                            if (stdin.hasNextLine()) {
                                command = stdin.nextLine().trim();
                                if (System.currentTimeMillis() - lastChatMillis < chatDelayMillis) {
                                    System.out.println("You are doing that too fast. Try again later.");
                                } else if (!command.isBlank()) {
                                    if (command.equals("exit")) {
                                        writer.println("exit");
                                        forceExitGame("User typed \"exit\".");
                                    } else if (!selfTurn) {
                                        writer.println("chat:" + command);
                                    } else {
                                        writer.println(command);
                                        writer.println();
                                    }
                                    lastChatMillis = System.currentTimeMillis();
                                }
                            } else {
                                try {
                                    stdin.nextLine();
                                } catch (NoSuchElementException ex) {
                                    System.out.println(ex.getMessage());
                                }
                            }
                        } while (!command.equals("exit"));

                        socket.close();
                        log.closeLog();
                    }
                    if (socket.isClosed()) {
                        System.out.println("Disconnected.");
                    }
                } catch (UnknownHostException ex) {
                    //System.out.println("Server not found: " + ex.getMessage());
                } catch (IOException ex) {
                    //System.out.println("I/O error: " + ex.getMessage());
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(UNOConsoleDriver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (joinOrHost.contains("s") || joinOrHost.contains("h")) {

            engine = new UNOEngine(ruleStackingSame, ruleStackingAll, ruleDrawTillPlayable, ruleSkipAfterDraw, handSize, deck);
            engine.addPlayer(new UNOPlayer().setID(ID).setUsername(username));
            try ( ServerSocket serverSocket = new ServerSocket(PORT)) {

                System.out.println("Server is listening on port " + PORT);
                boolean acceptingNewPlayers = true;
                while (acceptingNewPlayers) {
                    Socket socket = serverSocket.accept();
                    connections.add(new UNOServerThread(socket, engine, engine.addPlayer()));
                    connections.get(connections.size() - 1).start();
                    int iter = 100000; // this is just to be able to say the right username has connected, instead of the default name
                    while (iter > 0 && !connections.get(connections.size() - 1).hasOverriddenCredentials()) {
                        iter--;
                    }
                    if (isBanned(connections.get(connections.size() - 1).getPlayerID())) {
                        connections.get(connections.size() - 1).displayMessage("host", "You have been banned and cannot join.");
                        iter = 500;
                        while (iter > 0) {
                            iter--;
                        }
                        socket.close();
                        continue;
                    }
                    System.out.println(connections.get(connections.size() - 1).getUsername() + " connected");
                    System.out.println("Type start to begin the game, or kick to choose a player to kick.");
                    Scanner s = new Scanner(System.in);
                    while (s.hasNextLine()) {
                        String cmd = s.nextLine().trim().toLowerCase();
                        if (cmd.equals("start") || cmd.equals("s")) {
                            acceptingNewPlayers = false;
                            System.out.println("Server no longer listening for new connections.");
                            break;
                        } else if (cmd.equals("kick") || cmd.equals("k")) {
                            System.out.println("Server has paused listening for new players.");
                            System.out.println("# \tusername \taddress \t\tID");
                            int index = 1;
                            for (UNOServerThread server : connections) {
                                System.out.println(index + " \t" + server.getUsername() + " \t" + server.getRemoteSocketAddress().toString() + " \t" + server.getPlayerID());
                            }
                            System.out.println("Type the number of the player you would like kicked. You can also specify a range of players to kick with dash: \"3-7\" will kick players 3 through 7, inclusive.");
                            String response = s.nextLine().trim().toLowerCase();
                            if (response.contains("-")) {
                                String[] toKick = response.split("-");
                                try {
                                    for (int i = Integer.valueOf(toKick[1]) - 1; i > Integer.valueOf(toKick[0]) - 1; i--) {
                                        if (i < connections.size() && i >= 0) {
                                            String tempUsername = connections.get(i).getUsername();
                                            String tempID = connections.get(i).getPlayerID();
                                            String tempAddress = connections.get(i).getRemoteSocketAddress().toString();
                                            log.log("Initiating kick of player " + tempUsername + ".");
                                            connections.get(i).kick();
                                            banPlayer(connections.get(i).getPlayerID());
                                            connections.remove(i);
                                            System.out.println("Kicked player " + i + " with username " + tempUsername + ", ID " + tempID + ", and address " + tempAddress + ".");
                                        } else {
                                            System.out.println((i + 1) + " is an invalid player number.");
                                        }
                                    }
                                } catch (NumberFormatException ex) {
                                    System.out.println("Unable to kick players numbered \"" + toKick[0] + "\" through \"" + toKick[1] + "\". Only numbers and dashes are valid characters.");
                                }
                            } else {
                                try {
                                    int playerNum = Integer.valueOf(response) - 1;
                                    if (playerNum < connections.size() && playerNum >= 0) {
                                        String tempUsername = connections.get(playerNum).getUsername();
                                        String tempID = connections.get(playerNum).getPlayerID();
                                        String tempAddress = connections.get(playerNum).getRemoteSocketAddress().toString();
                                        log.log("Initiating kick of player " + tempUsername + ".");
                                        connections.get(playerNum).kick();
                                        banPlayer(connections.get(playerNum).getPlayerID());
                                        connections.remove(playerNum);
                                        System.out.println("Kicked player " + (playerNum + 1) + " with username " + tempUsername + ", ID " + tempID + ", and address " + tempAddress + ".");
                                    } else {
                                        System.out.println((playerNum + 1) + " is an invalid player number.");
                                    }
                                } catch (NumberFormatException ex) {
                                    System.out.println("Unable to kick player number \"" + response + "\". Only numbers and dashes are valid characters.");
                                }
                            }
                        }
                    }
                }
                try {
                    sendMessage("Game", "Preparing game");
                    engine.prepareGame();
                    for (int i = 0; i < engine.getNumPlayers(); i++) {
                        isAI.add(false); // FIXME, should add AI
                    }
                    sendMessage("Game", "Beginning game");
                    engine.beginGame();
                    displayTopCard(engine);

                    while (!engine.hasAPlayerEmptiedHand()) {
                        int tempCurrentPlayer = engine.getCurrentPlayer();
                        displayHand(engine);
                        displayAndReceiveChoices(engine);
                        long timeoutStart = System.currentTimeMillis();

                        while (!engine.hasCurrentPlayerDrawnOrPlayed() && engine.getCurrentPlayer() == tempCurrentPlayer) {
                            if (System.currentTimeMillis() - timeoutStart > 60000) {
                                log.debug("Current player (" + (engine.getCurrentPlayer() + 1) + ") timed out.");
                                if (engine.getCurrentPlayer() == 0) {
                                    sendMessage("Game", username + " timed out, drawing card for them.");

                                } else {
                                    sendMessage("Game", connections.get(engine.getCurrentPlayer() - 1).getUsername() + " timed out, drawing card for them.");
                                }
                                engine.drawCurrentPlayerCards(1);
                            }
                        }
                        log.log("Assigning next player");
                        engine.assignNextPlayer(true);
                        engine.assessPileSizes();
                    }
                    print("Player " + engine.getWinner() + " has won!");

                    log.closeLog();

                } catch (Exception ex) {
                    System.out.println("EXCEPTION OCCURRED");
                    exceptionOccurredLog(engine, ex);
                    throw ex;
                }

            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        /*
         * while (numPlayers < 2 || numPlayers > 10) { // response must be
         * between 2 & 10 inclusive
         * print("How many players? (2 - 10): ");
         * String response = stdin.nextLine().trim();
         * if (response.matches("^(\\d)+$")) { // response must be comprised
         * solely of digits
         * numPlayers = Integer.valueOf(response);
         * }
         * if (response.equals("exit")) {
         * forceExitGame("User typed \"exit\".");
         * }
         * }
         */

        //engine = new UNOEngine(numPlayers, ruleStackingSame, ruleStackingAll, ruleDrawTillPlayable, ruleSkipAfterDraw, handSize, deck);
        //isAI.add(false);
        //isAI.add(false);
        try {
            engine.prepareGame();
            engine.beginGame();
            while (!engine.hasAPlayerEmptiedHand()) {
                displayTopCard(engine);
                displayHand(engine);
                displayAndReceiveChoices(engine);
                if (engine.hasCurrentPlayerDrawnOrPlayed()) {
                    log.log("Assigning next player");
                    engine.assignNextPlayer();
                } else {
                    log.log("Next player has already been assigned, skipping additional assignment.");
                }
                engine.assessPileSizes();
            }
            print("Player " + engine.getWinner() + " has won!");

            log.closeLog();
        } catch (Exception e) {
            System.out.println("EXCEPTION OCCURRED");
            exceptionOccurredLog(engine, e);
            throw e;
        }
    }

    private static boolean isBanned(String tempID) {
        for (String s : bannedIDList) {
            if (s.equals(tempID)) {
                return true;
            }
        }
        return false;
    }

    private static void banPlayer(String banID) {
        File settings = new File("rsrc/settings.json"); // grabbed from https://www.w3schools.com/java/java_files_read.asp
        if (settings.exists()) {
            String data = "";
            try {
                Scanner myReader = new Scanner(settings);
                while (myReader.hasNextLine()) {
                    data += myReader.nextLine();
                }
                myReader.close();
                try {
                    JSONObject obj = new JSONObject(data); // ty to https://stackoverflow.com/a/18998203
                    List<Object> bannedIDsRaw = obj.getJSONArray("BANNED_IDS").toList();
                    ArrayList<String> bannedIDs = new ArrayList<>();
                    for (Object o : bannedIDsRaw) {
                        String temp = (String) o;
                        bannedIDs.add(temp);
                    }
                    bannedIDs.add(banID);
                    JSONArray jsonBan = new JSONArray();
                    for (String s : bannedIDs) {
                        jsonBan.put(s);
                    }
                    String jsonString = new JSONObject() // ty to https://stackoverflow.com/a/8876284
                            .put("username", username)
                            .put("ID", ID)
                            .put("BANNED_IDS", jsonBan)
                            .toString();
                    log.log("Created json String: " + jsonString);
                    try {
                        FileWriter writer = new FileWriter(settings);
                        writer.write(jsonString);
                        writer.close();
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(UNOConsoleDriver.class.getName()).log(Level.SEVERE, null, ex);
                        log.log("Could not write to settings.json file: " + ex.getMessage(), 1);
                    }
                    log.log("Wrote to settings.json successfully.");
                    return;
                } catch (org.json.JSONException ex) {
                    log.log("settings.json file unreadeable as JSON, raw data: \"" + data + "\"");
                }
            } catch (FileNotFoundException e) {
                log.log("Could not read from settings.json: " + e.getMessage());
            }
        }
        try {
            settings.createNewFile();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(UNOConsoleDriver.class.getName()).log(Level.SEVERE, null, ex);
            log.log("Could not create settings.json file: " + ex.getMessage(), 1);
        }
        String jsonString = new JSONObject() // ty to https://stackoverflow.com/a/8876284
                .put("username", username)
                .put("ID", UNOPlayer.generateID())
                .put("BANNED_IDS", "[" + banID + "]")
                .toString();
        log.log("Created json String: " + jsonString);
        try {
            FileWriter writer = new FileWriter(settings);
            writer.write(jsonString);
            writer.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(UNOConsoleDriver.class.getName()).log(Level.SEVERE, null, ex);
            log.log("Could not write to settings.json file: " + ex.getMessage(), 1);
        }
        log.log("Wrote to settings.json successfully.");
    }

    private static void printChats() {
        for (UNOServerThread s : connections) {
            for (String chat : s.getPendingChats()) {
                System.out.println(chat);
            }
        }
    }

    private static void importSettings() {
        File settings = new File("rsrc/settings.json"); // grabbed from https://www.w3schools.com/java/java_files_read.asp
        if (settings.exists()) {
            String data = "";
            try {
                Scanner myReader = new Scanner(settings);
                while (myReader.hasNextLine()) {
                    data += myReader.nextLine();
                }
                myReader.close();
                try {
                    JSONObject obj = new JSONObject(data); // ty to https://stackoverflow.com/a/18998203
                    username = obj.getString("username");
                    ID = obj.getString("ID");

                    List<Object> bannedIDsRaw = obj.getJSONArray("BANNED_IDS").toList();
                    for (Object o : bannedIDsRaw) {
                        String temp = (String) o;
                        bannedIDList.add(temp);
                    }
                    return;
                } catch (org.json.JSONException ex) {
                    log.log("settings.json file unreadeable as JSON, raw data: \"" + data + "\"");
                }
            } catch (FileNotFoundException e) {
                log.log("Could not read from settings.json: " + e.getMessage());
            }
        }
        try {
            settings.createNewFile();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(UNOConsoleDriver.class.getName()).log(Level.SEVERE, null, ex);
            log.log("Could not create settings.json file: " + ex.getMessage(), 1);
        }
        Scanner s = new Scanner(System.in);
        boolean isAllowed = false;
        String username = "";
        while (!isAllowed) {
            System.out.print("What username would you like to appear as? :: ");
            username = s.nextLine().trim();
            for (char c : username.toCharArray()) {
                if (!Arrays.asList(allowedUsernameCharacters).contains(Character.toString(c)) || Character.isLetterOrDigit(c)) {
                    continue;
                }
            }
            isAllowed = true;
        }
        String jsonString = new JSONObject() // ty to https://stackoverflow.com/a/8876284
                .put("username", username)
                .put("ID", UNOPlayer.generateID())
                .put("BANNED_IDS", new JSONArray())
                .toString();
        log.log("Created json String: " + jsonString);
        try {
            FileWriter writer = new FileWriter(settings);
            writer.write(jsonString);
            writer.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(UNOConsoleDriver.class.getName()).log(Level.SEVERE, null, ex);
            log.log("Could not write to settings.json file: " + ex.getMessage(), 1);
        }
        log.log("Wrote to settings.json successfully.");
    }

    private static void sendMessage(String ip, String message) {
        System.out.println(message);
        for (UNOServerThread c : connections) {
            c.displayMessage(ip, message);
            //System.out.println("sending message to client");
        }
    }

    private static void displayTopCard(UNOEngine e) {
        if (!isAI(e) && engine.getCurrentPlayer() == 0) {
            println("Card to play on: " + e.getTopCard());
        } else {
            silentPrint("Card to play on: " + e.getTopCard());
        }
    }

    private static void forceExitGame(String reason) {
        log.log("Exiting, reason: " + reason, 1);
        log.closeLog();
        System.exit(0);
    }

    private static String getResponse(String prompt, String regex, int currentPlayer, UNOEngine e) {
        if (isAI.get(currentPlayer)) {
            return getAIResponse(prompt.contains("(d)"), prompt.contains("(p)"), prompt.contains("(c)"), e.getCurrentHand(), e.getTopCard(), e.getCurrentPlayerMatches());
        }
        return getHumanResponse(prompt, regex);
    }

    private static String getAIResponse(boolean canDraw, boolean canPlay, boolean needToPickColor, UNOHand selfHand, UNOCard top, ArrayList<UNOCard> matches) {
        log.log("AI was told that it can" + (canDraw ? "" : "not") + " draw, it can" + (canPlay ? "" : "not") + " play and that it does" + (needToPickColor ? "" : " not") + " need to pick a color");
        log.log("AI does" + (!matches.isEmpty() ? "" : " not") + " believe that it has a match.");
        log.log("AI's hand: " + selfHand.toCompactString());
        Random rnd = new Random();
        if (needToPickColor) {
            log.log("AI has chosen to pick color.");
            ArrayList<Integer> colorCount = new ArrayList<>(4); // count of cards in hand by color, in alphabetical order (b,g,r,y)
            colorCount.add(0);
            colorCount.add(0);
            colorCount.add(0);
            colorCount.add(0);
            for (UNOCard c : selfHand.toArrayList()) {
                if (c.getColor().toLowerCase().charAt(0) == 'b') {
                    colorCount.set(0, colorCount.get(0) + 1);
                } else if (c.getColor().toLowerCase().charAt(0) == 'g') {
                    colorCount.set(1, colorCount.get(1) + 1);
                } else if (c.getColor().toLowerCase().charAt(0) == 'r') {
                    colorCount.set(2, colorCount.get(2) + 1);
                } else if (c.getColor().toLowerCase().charAt(0) == 'y') {
                    colorCount.set(3, colorCount.get(3) + 1);
                }
            }
            int highestIndex = colorCount.indexOf(Collections.max(colorCount));
            colorCount.remove(Collections.max(colorCount));
            int secondHighestIndex = colorCount.indexOf(Collections.max(colorCount));
            String bestColorForHand = UNOCard.COLORS[highestIndex];
            String secondBestColorForHand = UNOCard.COLORS[secondHighestIndex];

            if (rnd.nextInt(1) > 0.9) {
                ArrayList<String> colors = new ArrayList<>();
                colors.add("blue");
                colors.add("green");
                colors.add("red");
                colors.add("yellow");
                Collections.shuffle(colors);
                return colors.get(0);
            } else {
                if (rnd.nextInt(1) > 0.75) {
                    return secondBestColorForHand;
                } else {
                    return bestColorForHand;
                }
            }
        } else if (canPlay && !matches.isEmpty()) {
            if (rnd.nextInt(1) > 0.6) {
                Collections.shuffle(matches);
            }
            log.log("AI has chosen to play \"p" + matches.get(0).toCompactString() + "\"");
            return "p" + matches.get(0).toInstruction();
        } else if (canDraw) {
            log.log("AI has chosen to draw.");
            return "d";
        }
        System.out.println("No available choice, exiting.");
        System.exit(0);
        return "No available choice.";
    }

    private static String getHumanResponse(String prompt, String regex) {
        String response = "default";
        Scanner s = new Scanner(System.in);
        log.debug("Supplied regex: \"" + regex + "\"");

        do {
            if (!response.equals("default") && !response.isBlank()) {
                if (response.equals("exit")) {
                    forceExitGame("User typed \"exit\".");
                }
                print("\"" + response + "\" is not a valid choice. Use either the first letter or the whole word of the command you want to run. " + prompt);
            } else if (!response.isBlank()) {
                print(prompt);
            }
            response = s.nextLine().trim().toLowerCase();
            log.debug("rawish response is \"" + response + "\"");

        } while (!Pattern.matches(regex, response));
        log.debug("response was accepted as a regex match, returning as simplified version");
        String simplified = simplifyUserResponse(response);
        return simplified;
    }

    private static void flush() {
        log.flushCCBuffer();
    }

    private static void print(String message) {
        log.ccBuffer(message.replaceAll("\\n", " // "));
        System.out.print(message);
    }

    private static void println(String message) {
        print(message + "\n");
    }

    private static void sendCredentials(PrintWriter writer) {
        log.log("Sending username to host \"" + username + "\"");
        writer.println("username:" + username);
        log.log("Sending ID to host \"" + ID + "\"");
        writer.println("ID:" + ID);
    }

    private static void silentPrint(String message) {
        log.ccBuffer(message.replaceAll("\\n", " // "));
    }

    private static void displayAndReceiveChoices(UNOEngine e) {
        if (engine.getCurrentPlayer() == 0) {
            boolean hasPlus2 = e.getCurrentHand().hasPlus2();
            boolean hasPlus4 = e.getCurrentHand().hasPlus4();
            boolean hasPlus = e.getCurrentHand().hasPlus();
            if (!isAI(engine)) {
                print("P" + (e.getCurrentPlayer() + 1) + ": "); // for player 1, prints: "P1: "
            }
            if (e.getPendingCardsToDraw() > 0) {
                if (e.isRULE_STACKING_ALL()) {
                    if (hasPlus) {
                        String out = "Your choices are to (d)raw " + e.getPendingCardsToDraw() + " cards or to defer the penalty to the next player by (p)laying an additional ";
                        if (hasPlus2 && !hasPlus4) {
                            out += "+2 card from your hand on top.";
                        } else if (hasPlus2 && hasPlus4) {
                            out += "+2 or +4 card from your hand on top.";
                        } else if (!hasPlus2 && hasPlus4) {
                            out += "+4 card from your hand on top.";
                        }
                        if (!isAI(engine)) {
                            println(out);
                        } else {
                            silentPrint(out);
                        }
                        flush();

                        String response = getResponse("Valid responses: (d)raw or (p)lay: ", generateRegexForHand(e.getCurrentPlayerMatches()), e.getCurrentPlayer(), e); // regex for drawing or playing, valid responses include, but are certainly not limited to: draw, d, play red 3, pr3, p r3, play +2, play wild, pw, and so on and so forth
                        if (response.equals("d")) {
                            doPendingDraw(e);
                        } else if (response.charAt(0) == 'p') {
                            UNOCard chosen = parseCompactPlayCommand(response);
                            if (chosen.isWild()) {
                                e.playCurrentPlayersWildCard(chosen, getNewColor(engine));
                            } else {
                                e.playCurrentPlayersNonWildCard(chosen);
                            }
                        }
                    } else {
                        println("Automatically drew " + e.getPendingCardsToDraw() + " cards, given no other option.");
                        flush();
                        e.receivePendingCards();
                    }
                } else {
                    if (e.isRULE_STACKING_SAME()) {
                        if (e.getNum2CardsToDraw() > 0) {
                            if (e.getCurrentHand().hasPlus2()) {
                                if (!isAI(engine)) {
                                    println("Your choices are to (d)raw " + e.getPendingCardsToDraw() + " cards or to defer to the next player by (p)laying an additional +2 card from your hand on top.");
                                } else {
                                    silentPrint("Your choices are to (d)raw " + e.getPendingCardsToDraw() + " cards or to defer to the next player by (p)laying an additional +2 card from your hand on top.");
                                }
                            } else {
                                e.receivePendingCards();
                            }
                        } else {
                            if (e.getCurrentHand().hasPlus4()) {
                                if (!isAI(engine)) {
                                    println("Your choices are to (d)raw " + e.getPendingCardsToDraw() + " cards or to defer to the next player by (p)laying an additional +4 card from your hand on top.");
                                } else {
                                    silentPrint("Your choices are to (d)raw " + e.getPendingCardsToDraw() + " cards or to defer to the next player by (p)laying an additional +4 card from your hand on top.");
                                }
                            } else {
                                e.receivePendingCards();
                            }
                        }
                    } else {
                        e.receivePendingCards();
                    }
                    flush();
                }
            } else {
                ArrayList<UNOCard> options = e.getCurrentPlayerMatches();
                String tempDebugOptions = "";
                for (UNOCard c : options) {
                    tempDebugOptions += c.toString() + " ";
                }
                log.debug("Options for P" + (e.getCurrentPlayer() + 1) + " are \"" + tempDebugOptions + "\".");
                if (options.size() > 1) {
                    if (!isAI(engine)) {
                        print("The cards you can play are: ");
                        displayPlayableCards(e);
                    } else {
                        silentPrint("The cards you can play are: ");
                    }
                } else if (options.isEmpty()) {
                    if (!isAI(engine)) {
                        print("You cannot play any cards. ");
                    } else {
                        silentPrint("You cannot play any cards. ");
                    }
                    if (e.isRULE_DRAW_TILL_PLAYABLE()) {
                        if (!isAI(engine)) {
                            println("You must (d)raw until you have a playable card.");
                        } else {
                            silentPrint("You must (d)raw until you have a playable card.");
                        }
                        UNOCard lastDrawn;
                        do {
                            lastDrawn = doDrawCard(e);
                        } while (e.getCurrentPlayerMatches().isEmpty());
                        displayHand(e);
                        if (lastDrawn.isWild()) {
                            if (!isAI(engine)) {
                                println("You've played a wild card which means you get to (c)hoose the new color to match.");
                            } else {
                                silentPrint("You've played a wild card which means you get to (c)hoose the new color to match.");
                            }
                            e.playCurrentPlayersWildCard(lastDrawn, getNewColor(e));
                        } else {
                            e.playCurrentPlayersNonWildCard(lastDrawn);
                        }
                        return;
                    } else {
                        if (!isAI(engine)) {
                            println("You must draw a card.");
                        } else {
                            silentPrint("You must draw a card.");
                        }
                        doDrawCard(e);
                    }
                } else {
                    if (!isAI(engine)) {
                        print("The only card you can play is: ");
                        displayPlayableCards(e);
                    } else {
                        silentPrint("The only card you can play is: ");
                    }
                }
                flush();
                String response = getResponse("Will you (d)raw or (p)lay a card? ", generateRegexForHand(e.getCurrentPlayerMatches()), e.getCurrentPlayer(), e);
                if (response.charAt(0) == 'd') {
                    doDrawCard(e);
                    displayHand(e);
                    return;
                } else if (response.charAt(0) == 'p') {
                    log.debug("P" + (e.getCurrentPlayer() + 1) + "'s response: \"" + response + "\"");
                    UNOCard chosen = parseCompactPlayCommand(response);
                    if (chosen.isWild()) {
                        if (!isAI(engine)) {
                            println("You've played a wild card which means you get to (c)hoose the new color to match.");
                        } else {
                            silentPrint("You've played a wild card which means you get to (c)hoose the new color to match.");
                        }
                        e.playCurrentPlayersWildCard(chosen, getNewColor(e));
                    } else {
                        e.playCurrentPlayersNonWildCard(chosen);
                    }
                }
                flush();
            }
        } else {
            // Server thread should take care of it
        }
    }

    private static String getNewColorFromHuman() {
        String response = "default";
        if (engine.getCurrentPlayer() == 0) {
            Scanner s = new Scanner(System.in);
            do {
                println("Enter the new color, (b)lue, (g)reen, (r)ed, or (y)ellow: ");
                response = s.nextLine().trim().toLowerCase();
                log.debug("response to getNewColor(): \"" + response + "\"");
            } while ("bgry".indexOf(response.charAt(0)) == -1); // while the first char of the response is not one of the valid colors, ask for a valid response
            if (response.equals("exit")) {
                forceExitGame("User typed \"exit\".");
            }
        } else {
            response = connections.get(engine.getCurrentPlayer() - 1).getResponse("Enter the new color, (b)lue, (g)reen, (r)ed, or (y)ellow: ", "\\b([bgry])");
        }
        switch (response.charAt(0)) {
            case 'b':
                return "blue";
            case 'g':
                return "green";
            case 'r':
                return "red";
            case 'y':
                return "yellow";
        }
        return response;
    }

    private static boolean isAI(UNOEngine e) {
        return isAI.get(e.getCurrentPlayer());
    }

    private static String getNewColorFromAI(UNOEngine e) {
        return getAIResponse(false, false, true, e.getCurrentHand(), e.getTopCard(), e.getCurrentPlayerMatches());
    }

    public static String getNewColor(UNOEngine e) {
        if (isAI.get(e.getCurrentPlayer())) {
            return getNewColorFromAI(e);
        }
        return getNewColorFromHuman();
    }

    public static UNOCard parseCompactPlayCommand(String cmd) {
        switch (cmd) {
            case "pb0": // Thank you again, Dennis: https://bit.ly/2JOoayC
                return UNOCard.BLUE0;
            case "pb1":
                return UNOCard.BLUE1;
            case "pb2":
                return UNOCard.BLUE2;
            case "pb3":
                return UNOCard.BLUE3;
            case "pb4":
                return UNOCard.BLUE4;
            case "pb5":
                return UNOCard.BLUE5;
            case "pb6":
                return UNOCard.BLUE6;
            case "pb7":
                return UNOCard.BLUE7;
            case "pb8":
                return UNOCard.BLUE8;
            case "pb9":
                return UNOCard.BLUE9;
            case "pbs":
                return UNOCard.BLUESKIP;
            case "pbr":
                return UNOCard.BLUEREVERSE;
            case "pb+2":
                return UNOCard.BLUEPLUS2;
            case "pg0":
                return UNOCard.GREEN0;
            case "pg1":
                return UNOCard.GREEN1;
            case "pg2":
                return UNOCard.GREEN2;
            case "pg3":
                return UNOCard.GREEN3;
            case "pg4":
                return UNOCard.GREEN4;
            case "pg5":
                return UNOCard.GREEN5;
            case "pg6":
                return UNOCard.GREEN6;
            case "pg7":
                return UNOCard.GREEN7;
            case "pg8":
                return UNOCard.GREEN8;
            case "pg9":
                return UNOCard.GREEN9;
            case "pgs":
                return UNOCard.GREENSKIP;
            case "pgr":
                return UNOCard.GREENREVERSE;
            case "pg+2":
                return UNOCard.GREENPLUS2;
            case "pr0":
                return UNOCard.RED0;
            case "pr1":
                return UNOCard.RED1;
            case "pr2":
                return UNOCard.RED2;
            case "pr3":
                return UNOCard.RED3;
            case "pr4":
                return UNOCard.RED4;
            case "pr5":
                return UNOCard.RED5;
            case "pr6":
                return UNOCard.RED6;
            case "pr7":
                return UNOCard.RED7;
            case "pr8":
                return UNOCard.RED8;
            case "pr9":
                return UNOCard.RED9;
            case "prs":
                return UNOCard.REDSKIP;
            case "prr":
                return UNOCard.REDREVERSE;
            case "pr+2":
                return UNOCard.REDPLUS2;
            case "py0":
                return UNOCard.YELLOW0;
            case "py1":
                return UNOCard.YELLOW1;
            case "py2":
                return UNOCard.YELLOW2;
            case "py3":
                return UNOCard.YELLOW3;
            case "py4":
                return UNOCard.YELLOW4;
            case "py5":
                return UNOCard.YELLOW5;
            case "py6":
                return UNOCard.YELLOW6;
            case "py7":
                return UNOCard.YELLOW7;
            case "py8":
                return UNOCard.YELLOW8;
            case "py9":
                return UNOCard.YELLOW9;
            case "pys":
                return UNOCard.YELLOWSKIP;
            case "pyr":
                return UNOCard.YELLOWREVERSE;
            case "py+2":
                return UNOCard.YELLOWPLUS2;
            case "pw":
                return UNOCard.WILD;
            case "p+4":
                return UNOCard.PLUS4; // Welcome to the shadow realm, Jimbo.
        }
        return null;
    }

    public static ArrayList<UNOCard> doPendingDraw(UNOEngine e) {
        ArrayList<UNOCard> out = e.receivePendingCards();
        for (UNOCard c : out) {
            if (engine.getCurrentPlayer() == 0) {
                System.out.println("P" + (e.getCurrentPlayer() + 1) + " drew a " + c.toString() + ".");
            } else {
                connections.get(engine.getCurrentPlayer() - 1).displayMessage("Game", "P" + (e.getCurrentPlayer() + 1) + " drew a " + c.toString() + ".");
            }
        }
        return out;
    }

    public static UNOCard doDrawCard(UNOEngine e) {
        UNOCard out = e.drawCurrentPlayerCards(1).get(0);
        if (!isAI(e)) {
            println("P" + (e.getCurrentPlayer() + 1) + " drew a " + out.toString() + ".");
        } else {
            println("P" + (e.getCurrentPlayer() + 1) + " drew a card.");
        }
        return out;
    }

    private static void exceptionOccurredLog(UNOEngine e, Exception x) {
        log.log("---EXCEPTION OCCURRED---", 1);
        log.log("Gen message: " + x.getMessage());
        log.log("Localized message: " + x.getLocalizedMessage());
        log.log("Engine state: ");
        log.log(e.getState());
        log.closeLog();
    }

    private static void displayPlayableCards(UNOEngine e) {
        ArrayList<UNOCard> options = e.getCurrentPlayerMatches();
        for (int i = 0; i < options.size() - 1; i++) {
            print(options.get(i).toString());
            if (options.size() > 2) {
                print(", ");
            }
        }
        if (options.size() > 1) {
            if (options.size() == 2) {
                print(" ");
            }
            print("and ");
        }
        println(options.get(options.size() - 1) + ".");
    }

    public static String simplifyUserResponse(String userResponse) {
        String compactUserResponse = userResponse;
        log.debug("simplifyUserResponse() received \"" + userResponse + "\" as the only argument, proceeding with abbreviations and removing whitespace.");
        compactUserResponse = compactUserResponse.replaceFirst("(play)", "p");
        compactUserResponse = compactUserResponse.replaceFirst("(draw)", "d");
        compactUserResponse = compactUserResponse.replaceFirst("(blue)", "b");
        compactUserResponse = compactUserResponse.replaceFirst("(green)", "g");
        compactUserResponse = compactUserResponse.replaceFirst("(red)", "r");
        compactUserResponse = compactUserResponse.replaceFirst("(yellow)", "y");
        compactUserResponse = compactUserResponse.replaceFirst("(plus)", "+");
        compactUserResponse = compactUserResponse.replaceFirst("(two)", "2");
        compactUserResponse = compactUserResponse.replaceFirst("(four)", "4");
        compactUserResponse = compactUserResponse.replaceFirst("(wild)", "w");
        compactUserResponse = compactUserResponse.replaceFirst("(skip)", "s");
        compactUserResponse = compactUserResponse.replaceFirst("(reverse)", "r");
        compactUserResponse = compactUserResponse.replaceAll(" ", "");
        log.debug("simplifyUserResponse() has finished abbreviating and will be returning a final result of \"" + compactUserResponse + "\"");
        return compactUserResponse;
    }

    private static void displayHand(UNOEngine e) {
        if (!isAI(engine)) {
            UNOHand current = e.getCurrentHand();
            if (engine.getCurrentPlayer() == 0) {
                println("Player " + (e.getCurrentPlayer() + 1) + " " + current.toString());
            } else {
                // server thread can take care of it
                //connections.get(engine.getCurrentPlayer() - 1).displayMessage("Game", ("Player " + (e.getCurrentPlayer() + 1) + " " + current.toString()));
                System.out.println("server will take care of it");
            }
        }
    }

    public static void tell(String message) {
        for (UNOServerThread s : connections) {
            s.displayMessage("Game", message);
        }
    }

    public static String generateRegexForHand(ArrayList<UNOCard> cards) {
        String regex = "";

        String drawPartition = "\\b(d|draw)\\b|";
        regex += drawPartition;
        String contents = UNOHand.toCompactString(cards);
        String[] colors = {"", "", "", ""}; // color order is alphabetical, b, g, r, y
        String wilds = "";
        boolean blues = false;
        boolean greens = false;
        boolean reds = false;
        boolean yellows = false;
        String plus2SubPartition = "";

        for (int i = 0; i < contents.length() / 2; i++) {
            String card = contents.substring(i * 2, i * 2 + 2);
            char color = card.charAt(0);
            char type = card.charAt(1);
            if (color == 'w') {
                wilds = wilds + type;
            } else {
                if (type == '+') {
                    plus2SubPartition = "|(\\+2|\\+)";
                }
                switch (color) {
                    case 'b':
                        colors[0] = colors[0] + type;
                        blues = true;
                        break;
                    case 'g':
                        colors[1] = colors[1] + type;
                        greens = true;
                        break;
                    case 'r':
                        colors[2] = colors[2] + type;
                        reds = true;
                        break;
                    case 'y':
                        colors[3] = colors[3] + type;
                        yellows = true;
                        break;
                }
            }
        }

        String playPartition = "(\\b((p|play){1}\\s*(";
        for (int i = 0; i < 4; i++) {
            String colorSet = colors[i];
            if (!colorSet.isEmpty()) {
                String actualColor = "";
                switch (i) {
                    case 0:
                        actualColor = "blue";
                        break;
                    case 1:
                        actualColor = "green";
                        break;
                    case 2:
                        actualColor = "red";
                        break;
                    case 3:
                        actualColor = "yellow";
                        break;
                }
                playPartition += "((" + actualColor.charAt(0) + "|" + actualColor + ")\\s*([" + colorSet.replaceAll("\\+", "") + "]";
                playPartition = playPartition.replaceAll("\\[\\]", "");
                if (colorSet.contains("s")) {
                    playPartition += "|skip";
                }
                if (colorSet.contains("r")) {
                    playPartition += "|reverse";
                }
                playPartition += plus2SubPartition;
                playPartition += "){1})";
                boolean isLastColor = (i == 0 && !greens && !reds && !yellows && wilds.isEmpty()) || (i == 1 && !reds && !yellows && wilds.isEmpty()) || (i == 2 && !yellows && wilds.isEmpty()) || (i == 3 && wilds.isEmpty());
                if (!isLastColor) {
                    playPartition += "|";
                }
            }
        }
        if (!wilds.isEmpty()) {
            playPartition += "(";
            for (int i = 0; i < wilds.length(); i++) {
                if (wilds.charAt(i) == '+') { // card is +4
                    playPartition += "((w|wild(\\+4|[+4]))|(\\+4))";
                } else { // card is normal wild card
                    playPartition += "(w|wild)";
                }
                if (i < wilds.length() - 1) {
                    playPartition += "|";
                }
            }
            playPartition += ")";
        }
        playPartition += "))\\b)";
        regex += playPartition;
        return regex;
    }

    private static UNOHand generateHand(int handSize) {
        UNODefaultDeck deck = new UNODefaultDeck();
        deck.setNumPlayers(1);
        deck.shuffleDrawPile();
        deck.dealCards(handSize);
        return deck.getCurrentHand(0);
    }

    private static void printHeader() {
        System.out.println(" ▄████████  ▄██████▄  ███▄▄▄▄      ▄████████  ▄██████▄   ▄█          ▄████████      ███    █▄  ███▄▄▄▄    ▄██████▄  \n"
                + "███    ███ ███    ███ ███▀▀▀██▄   ███    ███ ███    ███ ███         ███    ███      ███    ███ ███▀▀▀██▄ ███    ███ \n"
                + "███    █▀  ███    ███ ███   ███   ███    █▀  ███    ███ ███         ███    █▀       ███    ███ ███   ███ ███    ███ \n"
                + "███        ███    ███ ███   ███   ███        ███    ███ ███        ▄███▄▄▄          ███    ███ ███   ███ ███    ███ \n"
                + "███        ███    ███ ███   ███ ▀███████████ ███    ███ ███       ▀▀███▀▀▀          ███    ███ ███   ███ ███    ███ \n"
                + "███    █▄  ███    ███ ███   ███          ███ ███    ███ ███         ███    █▄       ███    ███ ███   ███ ███    ███ \n"
                + "███    ███ ███    ███ ███   ███    ▄█    ███ ███    ███ ███▌    ▄   ███    ███      ███    ███ ███   ███ ███    ███ \n"
                + "████████▀   ▀██████▀   ▀█   █▀   ▄████████▀   ▀██████▀  █████▄▄██   ██████████      ████████▀   ▀█   █▀   ▀██████▀  \n"
                + "                                                        ▀                                                           ");
        String updateDate = "Dec 14, 2020";
        String PAT = new StringBuilder("066ad6b91d2587eda0f88530775a690925d69de9").reverse().toString(); // FIXME
        String command = "curl -i -u tfbninja:" + PAT + " https://api.github.com/repos/tfbninja/CST105/contents/UNO"; // this command painfully pieced together after arriving at https://docs.github.com/en/free-pro-team@latest/rest/reference/repos#get-repository-content
        String returned = "";
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" ")); // these next few beautiful lines of code brought to you in part by https://www.baeldung.com/java-curl
        //processBuilder.directory(new File("/home/"));
        try {
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            Reader inputStreamReader = new InputStreamReader(inputStream);
            int data = 0;
            while (data != -1) {
                data = inputStreamReader.read();
                returned += (char) data;
            }
            //System.out.println(returned);
            int exitCode = process.exitValue();
            process.destroy();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(UNOConsoleDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!returned.isEmpty()) {
            if (returned.contains("Last-Modified:") && returned.contains("X-OAuth-Scopes:")) {
                int lastModifiedLineIndex = returned.indexOf("Last-Modified:") + 15;
                int endIndex = returned.indexOf("X-OAuth-Scopes:");
                String lastModified = returned.substring(lastModifiedLineIndex, endIndex).trim();
                //lastModified = lastModified.substring(0, lastModified.length() - 4);
                //System.out.println("\"" + lastModified + "\"");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM uuuu HH:mm:ss zzz");

                LocalDateTime toMST = LocalDateTime.parse(lastModified, formatter).minusHours(7);

                DateTimeFormatter newFormatter;
                if (toMST.getYear() != LocalDateTime.now().getYear()) {
                    newFormatter = DateTimeFormatter.ofPattern("MMMM dd, uuuu, HH:mm");
                } else {
                    newFormatter = DateTimeFormatter.ofPattern("MMMM dd, HH:mm");
                }

                updateDate = toMST.format(newFormatter) + " MST";
            } else if (returned.contains("message")) {
                int messageIndex = returned.indexOf("\"message\": ") + 12;
                int endIndex = returned.indexOf("\"documentation_url\":") - 5;
                String message = returned.substring(messageIndex, endIndex);
                log.log("Could not retrieve last modified date, reason: " + message);
                log.log("--Full Message--");
                log.log(returned);
            }
        }
        System.out.println("\nConsole UNO, created by Tim Barber for CST-105\nUpdated " + updateDate + "\nThis line left intentionally blank. (except for this (and that))\n"); //Header
    }

}
