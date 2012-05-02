package chatosaur.server;

import java.net.*;
import java.io.*;

public class Connection implements Runnable {

    private Server server;
    private Socket socket;
    private BufferedReader in;
    private DataOutputStream out;
    public String clientName;
    private Thread thread;

    public Connection(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.clientName = socket.getInetAddress().getHostName() + ":" + socket.getPort();

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            this.out = new DataOutputStream(socket.getOutputStream());

            String welcome = "<" + clientName + "> joined the room.";
            server.log.write(welcome);

            // let everyone know a new client has joined
            // this will also take care of removing any disconnected servers
            // so we'll be sending a fresh list to the client
            server.sendToAll(this, welcome);

            // need to sent the client the server list
            // this is the only time we're doing it. hopefully at least one of the
            // servers in the list is still up when a client needs to re-connect.
            // with good server uptime. clients would have to stay connected longer than
            // all the server's uptime in the list before we'd have problems. even then
            // the client can just reconnect worst case.
            sendServerList();

            this.thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            server.log.write("Client <" + clientName + "> could not connect.");
        }
    }

    public void sendServerList() {
        try {
            ObjectOutputStream oo = new ObjectOutputStream(socket.getOutputStream());
            oo.writeObject(server.getServerList());

            server.log.write("Server list sent to client: <" + clientName + ">");
        } catch (IOException e) {
            server.log.write("Could not send server list to client: <" + clientName + ">");
        }
    }

    public void run() {

        try {
            while(true) {

                // receive message from client
                String message = (String) in.readLine();

                if (message == null || message.equals("null")) {
                    // client disconnected
                    break;
                }

                server.log.write("Message received from <" + clientName + ">: " + message);

                server.sendToAll(this, "<" + clientName + ">: " + message);

                thread.sleep(1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                in.close();
                out.close();
                server.killSocket(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            thread.interrupt();
        }
    }

    // send message to this connection
    public void sendMessage(String message) {
        try {
            out.writeBytes(message + '\n');
        } catch(IOException e) {
            // client is no longer connected, let's kill this sucker
            server.killSocket(this);
        }
    }
}
