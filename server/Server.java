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

    private String id;
    private String name;
    private String host;
    private int port;
    private ServerSocket server;
    private ArrayList<Connection> connections;
    private ArrayList<Server> serverList;
    private Log log;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
        this.log = new Log("server.log");
    }

    public void start() {

        // we only want one client inserting/removing at a time
        // we don't care about reads
        Semaphore semaphore = new Semaphore(1);

        // establish ArrayList to hold connections
        connections = new ArrayList<Connection>();

        // establish ArrayList to hold servers
        serverList = new ArrayList<Server>();

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

                // we're handling incoming servers and clients on the same port
                // so we determine which is which based on the initial message sent
                // server connects with "server", client connects with "client"
                if (readMessage(socket) == "server") {
                    new IncomingServerList(this, socket);
                } else {
                    connections.add(new Connection(this, socket, semaphore, log));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public void setName(String name) {
        this.name = name;
    }

    public void addServer(String name, String host, int port) {
        Server newServer = new Server(host, port);
        newServer.setName(name);

        serverList.add(newServer);
    }

    // this is the possible laggy part of the program so we'll do this in threads
    public void propagateList() {
        for (int i=0; i<serverList.size(); i++) {
            Server s = serverList.get(i);

            // make sure this isn't the current server
            if (server.host != s.host && server.port != s.port) {
                Socket serverSocket = new Socket(server.host, server.port);
                OutgoingServerList session = new OutgoingServerList(this, serverList, log);
            }
        }
    }

    public void setServerList(ArrayList<Server> newList) {
        serverList = newList;
    }

    // private

    private String readMessage(Socket incoming) {
        String message = null;
        try {
            this.in = new BufferedReader(new InputStreamReader(incoming.getInputStream())); 
            message = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    // main method

    public static void main(String[] args) {

        // set default port
        int port = 7777;

        // create new server bind to port and start
        Server server = new Server("localhost", port);

        // start up the user interface so someone can manage the server
        // pass in the new server so we can have access to it later
        ServerInterface sinterface = new ServerInterface(server);
        sinterface.start();
    }
}
