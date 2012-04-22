package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;

// logger class to help us write stuff to a log in one central location
public class Log {

    private DataOutputStream out;

    public Log(String fileName) {
        createLogIfNoLog(fileName);

        try {
            this.out = new DataOutputStream(new FileOutputStream(fileName));
        } catch (IOException e) {
            System.out.println("Error while setting up log in/out streams.");
        }
    }

    public void write(String message) {
        out.writeBytes(message + "\n");
    }

    // private

    // either reads existing, or creates new server log.
    private void createLogIfNo(String fileName) {
        try {
            File file = new File(fileName);

            boolean success = file.createNewfile();

            if (success) {
                System.out.println(fileName + " created.");
            } else {
                System.out.println(fileName + " found.");
            }
        } catch (IOException e) {
            System.out.println("Error while creating/reading " + fileName);
        }
    }
}
