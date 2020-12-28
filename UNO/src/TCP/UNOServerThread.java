package TCP;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Pattern;
import uno.*;
import static uno.UNOConsoleDriver.*;

/**
 * This thread is responsible for handling client connections.
 *
 * @author www.codejava.net (base template)
 * @author Timothy Barber (logic)
 */
public class UNOServerThread extends Thread {

    private Socket socket;
    private UNOEngine engine;
    private UNOPlayer player;
    private PrintWriter writer;
    private ArrayList<String> pendingChats;
    private boolean hasOverriddenUsername = false;
    private boolean hasOverriddenID = false;
    private BufferedReader reader;

    public UNOServerThread(Socket socket, UNOEngine engine, UNOPlayer selfPlayer) {
        this.socket = socket;
        this.engine = engine;
        this.player = selfPlayer;
        pendingChats = new ArrayList<String>();
    }

    @Override
    public void run() {
        try {
            try {
                InputStream input = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                writer = new PrintWriter(output, true);

                String text = "";

                try {
                    do {
                        if (reader.ready()) {
                            text = reader.readLine();
                            log.debug("Received instruction from client " + socket.getRemoteSocketAddress().toString() + ", \"" + text + "\"");

                            if (text.startsWith("chat:")) {
                                pendingChats.add(player.getUsername() + ": " + text.substring(5));
                                System.out.println(pendingChats.get(pendingChats.size() - 1));
                            } else if (text.startsWith("username:")) {
                                log.log("Server thread has received username, " + player.getUsername() + " changed to \"" + text.substring(9) + "\"");
                                player.setUsername(text.substring(9));
                                hasOverriddenUsername = true;
                            } else if (text.startsWith("ID:")) {
                                log.log("Server thread has received username, " + player.getUsername() + " changed to \"" + text.substring(9) + "\"");
                                player.setID(text.substring(9));
                                hasOverriddenID = true;
                            }
                        }
                        if (engine.hasStarted() && engine.getCurrentPlayerID().equals(player.getID()) && !engine.hasCurrentPlayerDrawnOrPlayed()) {
                            writer.println("urturn");
                            //displayTopCard();
                            displayHand();
                            displayAndReceiveChoices();
                            engine.assignNextPlayer();
                            writer.println("noturturn");
                        } else if (engine.hasStarted() && engine.getCurrentPlayer() > 0) {
                            //System.out.println("current id: " + engine.getCurrentPlayerID() + ", player id: " + player.getID());
                        }

                    } while (!text.equals("exit"));
                } catch (java.net.SocketException ex) {
                    System.out.println(player.getUsername() + " has lost connection or been kicked.");
                }
                engine.removePlayer(player.getID());
                socket.close();
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }
            engine.removePlayer(player.getID());
            socket.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(UNOServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void kick() {
        writer.println("You have been kicked.");
        engine.removePlayer(player.getID());
        log.log("Server thread is kicking player " + player.getUsername() + " with ID " + player.getID() + ".");
        try {
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
            log.log("Server thread has finished kicking player " + player.getUsername() + " with ID " + player.getID() + ".");
        } catch (IOException ex) {

        }
    }

    public String getPlayerID() {
        return player.getID();
    }

    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    public boolean hasOverriddenCredentials() {
        return hasOverriddenUsername && hasOverriddenID;
    }

    public String getUsername() {
        return player.getUsername();
    }

    public void displayMessage(String ip, String message) {
        write(ip + " :: " + message);
    }

    private void displayHand() {
        UNOHand current = engine.getCurrentHand();
        write("Your hand: " + current.toString());
    }

    private void displayTopCard() {
        write("Top card: " + engine.getTopCard());
    }

    private void displayAndReceiveChoices() {
        boolean hasPlus2 = engine.getCurrentHand().hasPlus2();
        boolean hasPlus4 = engine.getCurrentHand().hasPlus4();
        boolean hasPlus = engine.getCurrentHand().hasPlus();

        if (engine.getPendingCardsToDraw() > 0) {
            if (engine.isRULE_STACKING_ALL()) {
                if (hasPlus) {
                    String out = "Your choices are to (d)raw " + engine.getPendingCardsToDraw() + " cards or to defer the penalty to the next player by (p)laying an additional ";
                    if (hasPlus2 && !hasPlus4) {
                        out += "+2 card from your hand on top.";
                    } else if (hasPlus2 && hasPlus4) {
                        out += "+2 or +4 card from your hand on top.";
                    } else if (!hasPlus2 && hasPlus4) {
                        out += "+4 card from your hand on top.";
                    }

                    write(out);

                    String response = getResponse("Valid responses: (d)raw or (p)lay: ", generateRegexForHand(engine.getCurrentPlayerMatches())); // regex for drawing or playing, valid responses include, but are certainly not limited to: draw, d, play red 3, pr3, p r3, play +2, play wild, pw, and so on and so forth
                    if (response.equals("d")) {
                        UNOConsoleDriver.doPendingDraw(engine);
                    } else if (response.charAt(0) == 'p') {
                        UNOCard chosen = parseCompactPlayCommand(response);
                        if (chosen.isWild()) {
                            engine.playCurrentPlayersWildCard(chosen, getNewColor(engine));
                        } else {
                            engine.playCurrentPlayersNonWildCard(chosen);
                        }
                    }
                } else {
                    write("You automatically drew " + engine.getPendingCardsToDraw() + " cards, given no other option.");
                    engine.addPendingMessage(player.getUsername() + " automatically drew " + engine.getPendingCardsToDraw() + " cards, given no other option.");
                    engine.receivePendingCards();
                }
            } else {
                if (engine.isRULE_STACKING_SAME()) {
                    if (engine.getNum2CardsToDraw() > 0) {
                        if (engine.getCurrentHand().hasPlus2()) {
                            write("Your choices are to (d)raw " + engine.getPendingCardsToDraw() + " cards or to defer to " + engine.getNextPlayer().getUsername() + " by (p)laying an additional +2 card from your hand on top.");
                        } else {
                            engine.receivePendingCards();
                        }
                    } else {
                        if (engine.getCurrentHand().hasPlus4()) {
                            write("Your choices are to (d)raw " + engine.getPendingCardsToDraw() + " cards or to defer to " + engine.getNextPlayer().getUsername() + " by (p)laying an additional +4 card from your hand on top.");
                        } else {
                            engine.receivePendingCards();
                        }
                    }
                } else {
                    engine.receivePendingCards();
                }
            }
        } else {
            ArrayList<UNOCard> options = engine.getCurrentPlayerMatches();
            String tempDebugOptions = "";
            for (UNOCard c : options) {
                tempDebugOptions += c.toString() + " ";
            }
            log.debug("Options for " + engine.getCurrentPlayerID() + " are \"" + tempDebugOptions + "\".");
            if (options.size() > 1) {
                write("The cards you can play are: " + listCards());
            } else if (options.isEmpty()) {
                write("You cannot play any cards. ");
                if (engine.isRULE_DRAW_TILL_PLAYABLE()) {
                    write("You must (d)raw until you have a playable card.");
                    UNOCard lastDrawn;
                    do {
                        lastDrawn = doDrawCard(engine);
                    } while (engine.getCurrentPlayerMatches().isEmpty());
                    displayHand();
                    if (lastDrawn.isWild()) {
                        write("You've played a wild card which means you get to (c)hoose the new color to match.");
                        log.debug("Result of playing " + lastDrawn.toString() + ": " + engine.playCurrentPlayersWildCard(lastDrawn, getNewColor(engine)));
                    } else {
                        log.debug("Result of playing " + lastDrawn.toString() + ": " + engine.playCurrentPlayersNonWildCard(lastDrawn));
                    }
                    return;
                } else {
                    write("You must draw a card.");
                    doDrawCard(engine);
                }
            } else {
                write("The only card you can play is: " + listCards());
            }

            String response = getResponse("Will you (d)raw or (p)lay a card? ", generateRegexForHand(engine.getCurrentPlayerMatches()));
            if (response.charAt(0) == 'd') {
                doDrawCard(engine);
                displayHand();
                return;
            } else if (response.charAt(0) == 'p') {
                log.debug("P" + (engine.getCurrentPlayer() + 1) + "'s response: \"" + response + "\"");
                UNOCard chosen = parseCompactPlayCommand(response);
                if (chosen.isWild()) {
                    write("You've played a wild card which means you get to (c)hoose the new color to match.");
                    log.debug("Result of playing " + chosen.toString() + ": " + engine.playCurrentPlayersWildCard(chosen, getNewColor(engine)));
                } else {
                    log.debug("Result of playing " + chosen.toString() + ": " + engine.playCurrentPlayersNonWildCard(chosen));
                }
            }
        }
    }

    public String getResponse(String prompt, String regex) {
        log.debug("Supplied regex: \"" + regex + "\"");
        String formatted = "";

        write(prompt);
        do {
            try {
                if (reader.ready()) {
                    String cmd = reader.readLine();
                    formatted = cmd.trim().toLowerCase();
                    log.debug("rawish response is \"" + formatted + "\"");
                    if (!Pattern.matches(regex, formatted)) {
                        write(prompt);
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getLocalizedMessage());
            }
        } while (!Pattern.matches(regex, formatted));
        log.debug("response was accepted as a regex match, returning as simplified version");
        String simplified = simplifyUserResponse(formatted);
        return simplified;
    }

    private String getPendingChat() {
        return pendingChats.remove(pendingChats.size() - 1);
    }

    private String listCards() {
        ArrayList<UNOCard> options = new ArrayList<>();
        options = engine.getCurrentPlayerMatches();
        String out = "";
        for (int i = 0; i < options.size() - 1; i++) {
            out += options.get(i).toString();
            if (options.size() > 2) {
                out += ", ";
            }
        }
        if (options.size() > 1) {
            if (options.size() == 2) {
                out += " ";
            }
            out += "and ";
        }
        out += options.get(options.size() - 1) + ".";
        return out;
    }

    public ArrayList<String> getPendingChats() {
        return pendingChats;
    }

    private void write(String message) {
        writer.println(message);
    }
}
