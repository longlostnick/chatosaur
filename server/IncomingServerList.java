package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;

public class IncomingServerList implements Runnable {

    private Server server;
    private Socket socket;
    private ObjectInputStream in;

    public IncomingServerList(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        try {
            this.in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {

            // read in the new server list object
            server.setServerList(in.readObject());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
