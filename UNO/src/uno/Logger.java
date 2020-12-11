package uno;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Basic logging class, valid priorities for messages are -1 for low, 0 for
 * normal, and 1 for high.
 *
 * @author Tim Barber
 */
public class Logger {

    private final String SAVELOCATION;
    private File saveLocationFile;
    private String filename;

    public Logger(String saveLocation) {
        SAVELOCATION = saveLocation;
        saveLocationFile = new File(SAVELOCATION);
        if (!createLogFile()) {
            if (!createLogFile()) {
                if (!createLogFile()) {
                    System.out.println("Unable to create log file after 3 attempts.");
                }

            }
        }
        removeFilesOlderThan1Day(); // That's right, I delete old log files in the logging constructor. deal with it.
    }

    public String getSAVELOCATION() {
        return SAVELOCATION;
    }

    private boolean createLogFile() {
        // The following code is a minimally modified excerpt from https://www.w3schools.com/java/java_files_create.asp
        try {
            filename = SAVELOCATION + "UNO-log-file-" + LocalDateTime.now().toString().replaceAll(":", "") + "-" + System.currentTimeMillis() + ".txt";
            //System.out.println("filename: \"" + filename + "\"");
            File myObj = new File(filename);
            if (myObj.createNewFile()) {
                //System.out.println("File created: " + myObj.getName());
                return true;
            } else {
                System.out.println("File already exists.");
                return false;
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return false;
        }
    }

    private String priorityToString(int priority) {
        switch (priority) {
            case -1:
                return "low";
            case 0:
                return "normal";
            case 1:
                return "HIGH";
            default:
                return String.valueOf(priority);
        }
    }

    public void log(String message) {
        log(message, 0);
    }

    public void log(String message, int priority) {
        // The following code is a minimally modified excerpt from https://www.w3schools.com/java/java_files_create.asp
        try {
            try (FileWriter myWriter = new FileWriter(filename)) {
                myWriter.write(LocalDateTime.now().toString() + " (priority " + priorityToString(priority) + ") ->" + message);
            }
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred while logging.");
            e.printStackTrace();
        }
    }

    public void removeFilesOlderThan1Day() {
        // The following code is a minimally modified excerpt from https://stackoverflow.com/questions/48386912/determine-file-age-in-days/48386985
        // It's 5 am, alright? I ain't aboutta read the javadocs on 'iNsTaNtS' when I can just google the answer
        for (File f : saveLocationFile.listFiles()) {
            Instant fileInstant = getCreationDetails(f).toInstant();
            Instant now = java.time.Clock.systemDefaultZone().instant(); // Where clock is a java.time.Clock, for testability
            Duration difference = Duration.between(fileInstant, now);
            long days = difference.toDays();
            System.out.println(days);
            if (days > 1) {
                //f.delete(); //FIXME, notyet
            }
        }

    }

    public FileTime getCreationDetails(File file) {
        // The following code is a minimally modified excerpt from https://stackoverflow.com/questions/10824027/get-the-metadata-of-a-file
        try {
            Path p = Paths.get(file.getAbsolutePath());
            BasicFileAttributes view
                    = Files.getFileAttributeView(p, BasicFileAttributeView.class)
                            .readAttributes();
            FileTime fileTime = view.creationTime();
            return fileTime;
            //return ("" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format((fileTime.toMillis())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return "";
        return null;
    }
}
