package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class OutgoingServerList implements Runnable {

    private Server server;
    private ConnectedServer receiver;
    private Socket socket;
    private ArrayList<ConnectedServer> serverList;

    public OutgoingServerList(Server server, ConnectedServer serverToReceive, ArrayList<ConnectedServer> serverList) {
        this.server = server;
        this.receiver = serverToReceive;
        this.serverList = serverList;

        try {
            this.socket = new Socket(receiver.host, receiver.port);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // let the server know we're a server
            out.writeBytes("server\n");

        } catch (IOException e) {
            server.log.write("Could not connect to server!");
            System.exit(0);
        }


        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {

            ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());
            oo.writeObject(serverList);

            server.log.write("Server list sent to: <" + receiver.host + ":" + Integer.toString(receiver.port) + ">");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
