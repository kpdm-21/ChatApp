import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ConnectionHandler manages a single client connection.
 * It sets up the socket, handles the output stream, and provides methods
 * for retrieving client information and closing the connection.
 */

public class ConnectionHandler {
    private Socket socket;
    private PrintWriter out;
    private String clientAddress;
    private int remoteListeningPort; // added cause list command is messing up >.<

    // constructor, sets up the output stream and client's IP
    public ConnectionHandler(Socket socket, int remoteListeningPort) {
        this.socket = socket;
        // retrieve and store the client's IP from socket
        this.clientAddress = socket.getInetAddress().getHostAddress();
        // to make sure it receives correct port
        this.remoteListeningPort = remoteListeningPort;
        try {
            // initializes output stream
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // returns IP from connected peer
    public String getClientAddress() {
        return clientAddress;
    }

    // returns port number from connected peer
    public int getClientPort() {
        return remoteListeningPort;
    }

    // returns port access from connected peer
    public Socket getSocket() {
        return socket;
    }

    //// implement terminate command
    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // sends message to connected peer by flushing the stream
    public void sendMessageInner(String message) {
        try {
            if (out != null) {
                out.println(message);  // send the message
                out.flush();           // ensure it’s sent immediately
            } else {
                System.out.println("Error: Output stream is not initialized.");
            }
        } catch (Exception e) {
            System.out.println("Error sending message to " + clientAddress + ": " + e.getMessage());
        }
    }
}
