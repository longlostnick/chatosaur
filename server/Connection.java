package chatosaur.server;

import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;

public class Connection implements Runnable {

    private Server server;
    private Socket socket;
    private Semaphore semaphore;
    private BufferedReader in;
    private DataOutputStream out;
    public String clientName;
    private Thread thread;
    private log;

    public Connection(Server server, Socket socket, Semaphore semaphore, Log log) {
        this.server = server;
        this.socket = socket;
        this.semaphore = semaphore;
        this.clientName = socket.getInetAddress().getHostName() + ":" + socket.getPort();
        this.log = log;

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.thread = new Thread(this);

        thread.start();
    }

    public void run() {
        String welcome = "<" + clientName + "> joined the room.";
        System.out.println(welcome);
        server.sendToAll(this, welcome);
        try {
            while(true) {

                // receive message from client
                String message = (String) in.readLine();

                if (message == null) {
                    // client disconnected
                    break;
                }

                System.out.println("Message received from <" + clientName + ">: " + message);

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
            e.printStackTrace();
        }
    }
}
