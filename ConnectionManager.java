import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;

/**
 * ConnectionManager manages multiple client connections, allowing connection addition
 * and listing of active connections.
 */

public class ConnectionManager {
    // list to stre active connections
    private List<ConnectionHandler> connections = new ArrayList<>();

    // adds a new connection to the list
    public void addConnection(ConnectionHandler handler) {
        connections.add(handler);
    }

    // establishes a TCP connection to a specified IP and port with error handling
    public void connectToPeer(String ip, int port) {
        try {
            // validate IP address format and port number
            InetAddress address = InetAddress.getByName(ip);

            // retrieve local ip and port
            String localIP = InetAddress.getLocalHost().getHostAddress();
            int localPort = Chat.getServerSocket().getLocalPort();

            // check if trying to connect to self
            if (ip.equals(localIP) && port == localPort) {
                System.out.println("Error: Cannot connect to self.");
                return;
            }

            // check for duplicate connections
            for (ConnectionHandler handler : connections) {
                if (handler.getClientAddress().equals(ip) && handler.getClientPort() == port) {
                    System.out.println("Error: Duplicate connection to " + ip + " on port " + port + " is not allowed.");
                    return;
                }
            }

            // create a socket connection if all checks pass
            Socket socket = new Socket(address, port);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("LISTEN_PORT:" + localPort);

            ConnectionHandler handler = new ConnectionHandler(socket, port);
            addConnection(handler);

            System.out.println("Connected to " + ip + " on port " + port);
        } catch (IOException e) {
            System.out.println("Error: Unable to connect to " + ip + ":" + port);
        }
    }

    // Lists all active connections with their IDs, IP addresses, and ports
    public void listConnections() {
        System.out.println("id    IP address         Port No.");
        System.out.println("-----------------------------------");
        for (int i = 0; i < connections.size(); i++) {
            ConnectionHandler handler = connections.get(i);
            System.out.println((i + 1) + "    " + handler.getClientAddress() + "    " + handler.getClientPort());
        }
    }

    //// Terminates a connection by its index (1-based ID) -v1 haha i'm scared
    public void terminateConnection(int id) {
        if (id < 1 || id > connections.size()) {
            System.out.println("Error: Invalid connection ID.");
            return;
        }

        //// Get the connection handler
        ConnectionHandler handler = connections.get(id - 1);
        handler.closeConnection(); //// Close the socket
        connections.remove(id - 1); //// Remove from the list, it's a list right?
        System.out.println("Connection to " + handler.getClientAddress() + " terminated.");
    }

    //// Sends message to specific connection
    public void sendMessage(int id, String message) {
        if (id < 1 || id > connections.size()) {
            System.out.println("Error: Invalid connection ID.");
            return;
        }

        ConnectionHandler handler = connections.get(id - 1);
        if (handler != null) {
            System.out.println("Routing message to connection " + id + " (" + handler.getClientAddress() + ")");
            handler.sendMessageInner(message);
        } else {
            System.out.println("Error: Connection with ID " + id + " does not exist.");
        }
    }

    //// Related to Exit function and closes all connections
    public void closeAllConnections() {
        for (ConnectionHandler handler : connections) {
            handler.closeConnection();
        }
        connections.clear();
        System.out.println("All connections closed."); // Clear the list of connections
    }
}