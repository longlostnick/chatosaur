package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class OutgoingServerList implements Runnable {

    private Server server;
    private Socket socket;
    private ArrayList<Server> serverList;
    private Log log;

    public OutgoingServerList(Server server, ArrayList<Server> serverList, Log log) {
        this.server = server;
        this.log = log;
        this.serverList = serverList;

        try {
            this.socket = new Socket(host, port);
        } catch (IOException e) {
            log.wirte("Could not connect to server!");
            System.exit(0);
        }

        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes("server\n");

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(serverList);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
