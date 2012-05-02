package chatosaur.server;

import java.net.*;
import java.io.*;

// manages and shows the user the server administration
public class ServerInterface {

    private Server server;

    // constructor
    public ServerInterface(Server server) {
        this.server = server;
    }

    // show user the options
    private void showMenu() {

        String firstOption = "1. start server";

        if (server.running) {
            firstOption = "1. server status";
        }

        System.out.println("\n" + firstOption + "\n" +
                           "2. add new server to system\n" +
                           "3. show this menu\n" +
                           "0. Exit/Shutdown");
    }

    public void start() {

        System.out.println("\nWelcome to Chatosaur Server v0.0001");

        // show the user/administrator the menu
        showMenu();

        while (true) {

            System.out.print("\nChoose an option: ");

            switch(Integer.parseInt(getUserInput())) {
                case 1:
                    if (!server.running) {
                        promptStartServer();
                    } else {
                        System.out.println("\nThe server is running!");
                    }
                    break;
                case 2:
                    if (server.running) {
                        promptAddServer();
                    } else {
                        System.out.println("\nThe server must be running to do that!");
                    }
                    break;
                case 3:
                    showMenu();
                    break;
                case 0:
                    promptShutdownServer();
                    break;
                default:
                    System.out.println("\ncommand not recognized.");
            }
        }
    }

    // let's the user name the server and starts it
    private void promptStartServer() {

        System.out.print("\nSet the server port: ");
        server.setPort(Integer.parseInt(getUserInput()));

        if (server.start()) {
            System.out.println("\nServer started.");
        }
    }

    // prompt the user for a server and port to add to list
    private void promptAddServer() {

        System.out.print("\nServer host: ");
        String host = getUserInput();

        System.out.print("Server port: ");
        int port = Integer.parseInt(getUserInput());

        // test the connection
        if (testConnection(host, port)) {
            server.addServer(host, port);
        } else {
            System.out.println("Could not contact server.");
        }
    }

    // shutdown the server, but ask the user first
    private void promptShutdownServer() {
        System.out.print("\nAre you sure? (y/n): ");

        if (getUserInput().equals("y")) {
            server.gracefulShutdown();
            System.exit(0);
        }
    }

    // test a connection. returns true if connection successful
    private boolean testConnection(String host, int port) {
        Socket socket = null;
        boolean reachable = false;

        try {
            socket = new Socket(host, port);
            reachable = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }

        return reachable;
    }

    // nice compact method to take user input
    private String getUserInput() {
        String input = "";

        try {
            BufferedReader UserInput = new BufferedReader(new InputStreamReader(System.in));
            input = UserInput.readLine();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return input;
    }

}
