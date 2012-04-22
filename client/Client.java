package chatosaur.client;

import java.net.*;
import java.io.*;

public class Client {

    private static Socket socket;
    private static BufferedReader in;
    private static DataOutputStream out;

    public static void main(String[] args) {
        String host = "localhost";
        int port = 7777;

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

            MessageBuffer mbuff = new MessageBuffer(in);

            while(true) {

                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

                String input = userInput.readLine();

                out.writeBytes(input + '\n');
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
