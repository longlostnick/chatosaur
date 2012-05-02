package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;

// logger class to help us write stuff to a log in one central location
public class Log {

    private DataOutputStream out;

    public Log(String fileName) {
        createLogIfNo(fileName);

        try {
            this.out = new DataOutputStream(new FileOutputStream(fileName));
        } catch (IOException e) {
            System.out.println("Error while setting up log in/out streams.");
        }
    }

    public void write(String message) {
        try {
            out.writeBytes(message + "\n");
        } catch (IOException e) {
            System.out.println("Could not write to log.");
        }
    }

    // private

    // either reads existing, or creates new server log.
    private void createLogIfNo(String fileName) {
        try {
            File file = new File(fileName);

            if (file.exists()) {
                System.out.println("\n" + fileName + " found.");
            } else {
                file.createNewFile();
                System.out.println("\n" + fileName + " created.");
            }
        } catch (IOException e) {
            System.out.println("Error while creating/reading " + fileName);
        }
    }
}
