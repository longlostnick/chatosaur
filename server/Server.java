package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.io.Serializable;

// import my project's classes
import chatosaur.server.ServerInterface;
import chatosaur.server.OutgoingServerMessage;
import chatosaur.server.OutgoingServerList;
import chatosaur.server.IncomingServerList;
import chatosaur.server.Connection;
import chatosaur.server.Log;

public class Server {

    private String id;
    private ArrayList<ConnectedServer> serverList;
    private ArrayList<Connection> connections;

    public boolean running = false;
    public Log log;
    public String host;
    public int port = -1;

    public Server(String host) {
        this.host = host;
    }

    public boolean start() {

        if (port > -1) {
            // set up our log based on the port that should have been set
            log = new Log("server_" + Integer.toString(port) + ".log");

            // establish ArrayList to hold connections
            connections = new ArrayList<Connection>();

            // establish ArrayList to hold servers
            serverList = new ArrayList<ConnectedServer>();

            // add ourseleves to the list
            serverList.add(new ConnectedServer(host, port));

            new Thread(new ConnectionBroker(this)).start();

            log.write("Server started on port " + Integer.toString(port) + ".");

            running = true;
        }

        return running;
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
        // send to all connections on this server
        for (int i=0; i<connections.size(); i++) {
            Connection conn = connections.get(i);
            if (conn == from) {
                continue;
            }
            conn.sendMessage(message);
        }

        // now send to other servers so they can send to their clients
        propagateMessage(from, message);
    }

    public void sendToAllFromOutside(String message) {
        // send to all connections on this server
        for (int i=0; i<connections.size(); i++) {
            Connection conn = connections.get(i);
            conn.sendMessage(message);
        }
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void addServer(String host, int port) {
        ConnectedServer newServer = new ConnectedServer(host, port);

        serverList.add(newServer);

        // send out an updated list to all the servers
        propagateList();
    }

    public void setServerList(ArrayList<ConnectedServer> newList) {
        serverList = newList;

        String list = serverList.get(0).getClientName();

        for (int i=1; i<serverList.size(); i++) {
            ConnectedServer s = serverList.get(i);
            list = list + ", " + s.getClientName();
        }

        log.write("Updated List: " + list);
    }

    public String readMessage(Socket incoming) {
        String message = null;

        try {
            BufferedReader sin = new BufferedReader(new InputStreamReader(incoming.getInputStream())); 
            message = sin.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    public void gracefulShutdown() {
        running = false;
        log.write("Server shutdown.");
    }

    // private

    private void propagateMessage(Connection from, String message) {
        ArrayList<ConnectedServer> cloneList = new ArrayList<ConnectedServer>(serverList);

        for (int i=0; i<cloneList.size(); i++) {
            ConnectedServer s = cloneList.get(i);

            // make sure this isn't the current server
            if (host != s.host && port != s.port) {
                log.write("Sending message to: <" + s.host + ":" + Integer.toString(s.port) + ">");
                new OutgoingServerMessage(this, s, from, message);
            }
        }
    }

    private void propagateList() {
        ArrayList<ConnectedServer> cloneList = new ArrayList<ConnectedServer>(serverList);

        for (int i=0; i<cloneList.size(); i++) {
            ConnectedServer s = cloneList.get(i);

            // make sure this isn't the current server
            if (host != s.host && port != s.port) {
                log.write("Sending list to: <" + s.host + ":" + Integer.toString(s.port) + ">");
                new OutgoingServerList(this, s, cloneList);
            }
        }
    }

    // main method

    public static void main(String[] args) {

        // create new server bind to port and start
        Server server = new Server("localhost");

        // start up the user interface so someone can manage the server
        // pass in the new server so we can have access to it later
        ServerInterface sinterface = new ServerInterface(server);
        sinterface.start();
    }
}

class ConnectionBroker implements Runnable {

    private Server server;
    private ServerSocket ss;

    public ConnectionBroker(Server server) {
        this.server = server;
    }

    public void run() {

        // bind server to port
        try {
            ss = new ServerSocket(server.port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // continuously check for new connections
        while (server.running) {
            try {
                Socket socket = ss.accept();

                String message = server.readMessage(socket);

                if (message != null) {

                    server.log.write("Received: " + message);

                    // we're handling incoming servers and clients on the same port
                    // so we determine which is which based on the initial message sent
                    // server connects with "server", client connects with "client"
                    if (message.equals("server")) {
                        server.log.write("Server connected.");
                        new Thread(new IncomingServerList(server, socket)).start();
                    } else if (message.matches("^servermessage:(.*)")) {
                        server.sendToAllFromOutside(message.replaceFirst("^servermessage:", ""));
                    } else if (message.equals("client")) {
                        server.getConnections().add(new Connection(server, socket));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ConnectedServer implements Serializable {

    public String host;
    public int port;

    public ConnectedServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getClientName() {
        return "<" + host + ":" + Integer.toString(port) + ">";
    }
}
