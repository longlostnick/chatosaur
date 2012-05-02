package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

import chatosaur.common.ConnectedServer;

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

            Thread thread = new Thread(this);
            thread.start();

        } catch (IOException e) {
            // remove this server from the list since it isn't responding
            // hopefully all clients have already transparently been moved to another.
            // this honestly saves us a ton because we don't have to worry about removing servers
            // when they disconnect, or pinging them to make sure they're still alive
            server.removeConnectedServer(receiver);

            // log this incident
            server.log.write("Could not connect to server for outgoing servermessage!");
        }
    }

    // if this runs, that means we successfully connected to the server
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
