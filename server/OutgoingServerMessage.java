package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class OutgoingServerMessage implements Runnable {

    private Server server;
    private ConnectedServer receiver;
    private Connection from;
    private String message;
    private Socket socket;

    public OutgoingServerMessage(Server server, ConnectedServer serverToReceive, Connection from, String message) {
        this.server = server;
        this.receiver = serverToReceive;
        this.from = from;
        this.message = message;

        try {
            this.socket = new Socket(receiver.host, receiver.port);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // we want to send a message to a server's clients
            out.writeBytes("servermessage:" + message + "\n");

        } catch (IOException e) {
            server.log.write("Could not connect to server!");
            System.exit(0);
        }

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes(message);

            server.log.write("Message sent to: <" + receiver.host + ":" + Integer.toString(receiver.port) + ">");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
