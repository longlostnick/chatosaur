package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class OutgoingServerList implements Runnable {

    private Server server;
    private ConnectedServer receiver;
    private Socket socket;

    public OutgoingServerList(Server server, ConnectedServer serverToReceive) {
        this.server = server;
        this.receiver = serverToReceive;

        try {
            this.socket = new Socket(receiver.host, receiver.port);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // let the server know we're a server
            out.writeBytes("server\n");

            // the thread wont even start if we can't connect above
            Thread thread = new Thread(this);
            thread.start();

        } catch (IOException e) {
            // remove this server from the list since it isn't responding
            // hopefully all clients have already transparently been moved to another.
            // this honestly saves us a ton because we don't have to worry about removing servers
            // when they disconnect, or pinging them to make sure they're still alive
            server.removeConnectedServer(receiver);

            // log this incident
            server.log.write("Could not connect to server!");
        }
    }

    // if this runs, that means we successfully connected to the server
    public void run() {
        try {

            ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());
            oo.writeObject(server.getServerList());

            server.log.write("Server list sent to: <" + receiver.host + ":" + Integer.toString(receiver.port) + ">");

        } catch (IOException e) {
            server.log.write("Could not send server list to <" + receiver.host + ":" + Integer.toString(receiver.port) + ">");
        }
    }
}
