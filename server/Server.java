package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;

// import my project's classes
import chatosaur.server.ServerInterface;
import chatosaur.server.Connection;
import chatosaur.server.Log;

public class Server {

    private int port;
    private ServerSocket server;
    private ArrayList<Connection> connections;
    private ArrayList<Server> server;
    private Log log;

    public Server(int port) {
        this.port = port;
        this.log = new Log("server.log");
    }

    public void start() {

        // we only want one client inserting/removing at a time
        // we don't care about reads
        Semaphore semaphore = new Semaphore(1);

        // establish ArrayList to hold connections
        connections = new ArrayList<Connection>();

        // bind server to port
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // continuously check for new connections
        while (true) {
            try {
                Socket socket = server.accept();
                connections.add(new Connection(this, socket, semaphore, log));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addServer(String name, String host, Int port) {
    }

    public void removeServer() {
    }

    public void killSocket(Connection conn) {
        for (int i=0; i<connections.size(); i++) {
            if (connections.get(i) == conn) {
                String goodbye = "<" + conn.clientName + "> left the room.";
                sendToAll(conn, goodbye);
                log.write(goodbye);
                connections.remove(i);
                break;
            }
        }
    }

    public void sendToAll(Connection from, String message) {
        for (int i=0; i<connections.size(); i++) {
            Connection conn = connections.get(i);
            if (conn == from) {
                continue;
            }
            conn.sendMessage(message);
        }
    }

    public static void main(String[] args) {

        // set default port
        int port = 7777;

        // override default port if provided
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        // create new server bind to port and start
        Server server = new Server(port);
        server.start();

        // start up the user interface so someone can manage the server
        // pass in the new server so we can have access to it later
        ServerInterface sinterface = new ServerInterface(server);
        interface.start();
    }
}
