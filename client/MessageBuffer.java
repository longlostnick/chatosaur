package nick.client;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;

public class MessageBuffer implements Runnable {

    private BufferedReader in;
    private Thread thread;

    public MessageBuffer(BufferedReader in) {

        this.in = in;
        this.thread = new Thread(this);

        thread.start();
    }

    public void run() {
        try {
            while (true) {

                String message = in.readLine();

                if (message == null) {
                    System.out.println("The server disconnected.");
                    System.exit(0);
                }

                System.out.println(message);

                thread.sleep(1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
