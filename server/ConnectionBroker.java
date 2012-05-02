package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.io.Serializable;

// pulled this out of the server into it's own thread so we can keep performing tasks while this runs
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
                    // "server" incoming server
                    // "client" incoming client
                    // "servermessage:[message]" incoming message to propagate from another server's client
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

