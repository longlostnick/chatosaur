package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class IncomingServerList implements Runnable {

    private Server server;
    private Socket socket;
    private String clientName;

    public IncomingServerList(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.clientName = socket.getInetAddress().getHostName() + ":" + socket.getPort();
    }

    public void run() {
        try {

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // read in the new server list object
            ArrayList<ConnectedServer> incomingServerList = (ArrayList<ConnectedServer>)in.readObject();

            server.log.write("Server list received from: <" + clientName + ">");

            // tell our server to update it's list with the new one
            server.setServerList(incomingServerList);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
