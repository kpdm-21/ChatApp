import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
            int localPort = Chat.serverSocket.getLocalPort();

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
            ConnectionHandler handler = new ConnectionHandler(socket);
            addConnection(handler);

            System.out.println("Connected to " + ip + " on port " + port);
        } catch (UnknownHostException e) {
            System.out.println("Error: Invalid IP address.");
        } catch (IOException e) {
            System.out.println("Error: Could not connect to " + ip + " on port " + port + ". Please check the IP address and port.");
        }
    }

    // Lists all active connections with their IDs, IP addresses, and ports
    public void listConnections() {
        System.out.println("id    IP address         Port No.");
        System.out.println("-----------------------------------");
        for (int i = 0; i < connections.size(); i++) {
            ConnectionHandler handler = connections.get(i);
            System.out.println((i + 1) + "     " + handler.getClientAddress() + "        " + handler.getClientPort());
        }
    }
}