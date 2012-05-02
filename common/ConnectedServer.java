package chatosaur.common;

import java.net.*;
import java.io.*;
import java.io.Serializable;

// class to define a connected server
public class ConnectedServer implements Serializable {

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
