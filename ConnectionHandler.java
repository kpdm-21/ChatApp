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

    // constructor, sets up the output stream and client's IP
    public ConnectionHandler(Socket socket) {
        this.socket = socket;
        // retrieve and store the client's IP from socket
        this.clientAddress = socket.getInetAddress().getHostAddress();
        try {
            // initializes output stream
            out = new PrintWriter(socket.getOutputStream(), true);
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
        return socket.getPort();
    }

    // implement terminate command
    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}