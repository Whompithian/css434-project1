/**
 *
 * @file    ChatServer.java
 *           Includes elements from:
 *           uw1-320-lab.uwb.edu:~css434/hw1/ChatClient.java by Munehiro Fukuda
 * @brief   This class acts as a server to mediate consistent ordering of
 *           messages passed between multiple chat clients. It is meant to be
 *           used in conjunction with ChatClient, cited above.
 * @author  Brendan Sweeney, SID 1161836
 * @date    October 16, 2012
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;

public class ChatServer {
    private ServerSocket socket;     // a socket connection to accept clients
    private LinkedList<Connection> clientList;  // list of chat clients

    /**
     * Quick sanity check of and parse of arguments, then instantiate the
     *  ChatServer object to do everything else.
     * @param args  the command line arguments; expect a single argument, port
     *               number.
     */
    public static void main(String[] args) {
        // Check # args.
        if (args.length != 1) {
            System.err.println("Usage: java ChatServer <port>");
            System.exit(1);
        } // end if (args.length != 1)

        // convert args[0] into an integer that will be used as port.
        int port = Integer.parseInt(args[0]);

        // instantiate the main body of ChatServer application.
        new ChatServer(port);
    } // end main(String[])
    
    
    /**
     * Establishes a server socket and listens for connections from clients.
     *  Each client is added to a list, which is traversed to check for new
     *  messages. If a message is ready, it is retrieved and passed on to each
     *  client, including the sender.
     * @param  port  The port on which this server will listen for connections.
     */
    public ChatServer(int port) {
        clientList           = new LinkedList<Connection>();    // client list
        Socket       client  = null;    // for establishing data connections
        Connection   next;              // keeps track of connection variables
        StringBuffer message;           // message retrieved from a client
        // establish socket and set timeout
        try {
            socket = new ServerSocket(port);
        } catch(IOException e) {
            System.err.println(e.getStackTrace());
            System.exit(1);
        } // end try socket = new ServerSocket(port)
        try {
            socket.setSoTimeout(500);
        } catch(SocketException e) {
            System.err.println(e.getStackTrace());
            System.exit(1);
        } // end try socket.setSoTimeout(500)
        while(true) {
            // accept new client socket connection
            try {
                client = socket.accept();
            } catch(Exception e) {
            } // end try client = socket.accept()
            // establish a connection if a client was accepted
            if (client != null) {
                next = new Connection(client);
                client = null;
                // add connection to client list
                clientList.add(next);
            } // end if (client != null)
            // check for pending messages from each client
            for (Iterator<Connection> i = clientList.iterator();
                    i.hasNext(); ) {
                next = i.next();
                // read a message, if ready
                message = next.readMessage();
                if (message != null) {
                    // write message to all clients
                    for (Iterator<Connection> j = clientList.iterator();
                            j.hasNext();
                            j.next().writeMessage(message.toString())) {
                    } // end for ( ; j.hasNext(); )
                } // end if (message != null)
                // check for errors, remove connection if found
                if (next.hasError) {
                    clientList.remove(next);
                } // end if (next.hasError)
            } // end for ( ; i.hasNext(); )
        } // end while(true)
    } // end ChatServer(String, int)
    
    
    /**
     * Maintains the state of a client connection, including a name, error
     *  state, and data streams.
     */
    private class Connection {
        private DataInputStream  in;        // input stream from the client
        private DataOutputStream out;       // output stream to the client
        private String           name;      // name received from client
        private StringBuffer     message;   // message received from client
        public  Boolean          hasError;  // if errors were encountered
        
        public Connection(Socket client) {
            hasError = false;
            message  = null;
            out      = null;
            
            try {
                // Create an input and an output stream.
                in   = new DataInputStream(client.getInputStream());
                out  = new DataOutputStream(client.getOutputStream());
                name = in.readUTF();
                System.out.println(name + " connected.");
            } catch(IOException e) {
                System.err.print(e.getStackTrace());
                hasError = true;
            } // end try in = new DataInputStream()
        } // end Connection(Socket)
        
        public StringBuffer readMessage() {
            try {
                if (in.available() > 0) {
                    message = new StringBuffer();
                    // add client name to front of message
                    message.append(name);
                    message.append(": ");
                    message.append(in.readUTF());
                } // end if (in.available() > 0)
                else {
                    message = null;
                } // end else in.available() > 0
            } catch(IOException e) {
                System.err.print(e.getStackTrace());
                hasError   = true;
            } // end try message = new StringBuffer()
            
            return message;
        } // end readMessage()
        
        public void writeMessage(String toWrite) {
            try {
                out.writeUTF(toWrite);
            } catch(IOException e) {
                System.err.print(e.getStackTrace());
                hasError = true;
            } // end try out.writeUTF(toWrite)
        } // end writeMessage()
    } // end class Connection
} // end class ChatServer
