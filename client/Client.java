package chatosaur.client;

import java.net.*;
import java.io.*;

public class Client {

    private static Socket socket;
    private static BufferedReader in;
    private static DataOutputStream out;

    public static void main(String[] args) {
        
        System.out.print("\nServer host: ");
        String host = getUserInput();

        System.out.print("Server port: ");
        int port = Integer.parseInt(getUserInput());

        if (args.length > 0) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            System.out.println("Could not connect to server!");
            System.exit(0);
        }

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            // let the server know we're a client
            out.writeBytes("client\n");

            MessageBuffer mbuff = new MessageBuffer(in);

            // just a blank line for formatting
            System.out.println("");

            while(true) {

                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

                String input = userInput.readLine();

                out.writeBytes(input + '\n');
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    // nice compact method to take user input
    private static String getUserInput() {
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
