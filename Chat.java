/**
 * COMP 429 Chat Application Assignment
 * Project Group 3
 * Natalia Chavez
 * Kristina Dela Merced
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class for the Chat application. Sets up the server, manages user commands,
 * and provides an interface for establishing peer-to-peer connections.
 */

public class Chat {
    // server socket to listen for incoming connections
    private static ServerSocket serverSocket;
    // manages multiple peer connections
    private static ConnectionManager connectionManager;
    // map of commands to their corresponding actions
    private static Map<String, Runnable> commands = new HashMap<>();

    // sets up the server and starts listening for commands and connections
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Chat <port>");
            return;
        }

        // parse the provided port number
        int port;
        try {
            port = Integer.parseInt(args[0]);
            if (port < 1024 || port > 65535) {
                System.out.println("Error: Port must be between 1024 and 65535.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Port must be an integer.");
            return;
        }

        // initialize connection manager
        connectionManager = new ConnectionManager();

        try {
            // create a server socket to listen on the specified port
            serverSocket = new ServerSocket(port);
            System.out.println("Chat application started on port " + port);
            System.out.println("Type 'help' for available commands.");

            // initialize command map with supported commands
            initializeCommands();

            // start a separate thread to accept incoming connections from peers
            new Thread(Chat::acceptConnections).start();

            // main loop to process user commands from the console
            processUserCommands();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // provides access to the server socket for other parts of the program
    public static ServerSocket getServerSocket() {
        return serverSocket;
    }

    // method to initialize all supported commands with corresponding actions
    private static void initializeCommands() {
        commands.put("myip", Chat::myip);
        commands.put("myport", () -> System.out.println("Listening on port: " + serverSocket.getLocalPort()));
        commands.put("list", connectionManager::listConnections);
        commands.put("help", Chat::displayHelp);
        commands.put("terminate", Chat::processTerminateCommand);
        commands.put("send", Chat::processSendCommand);
        commands.put("exit", Chat::exitApplication);
    }

    // main loop to handle user commands and execute relevant actions
    private static void processUserCommands() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter command: ");
            String command = scanner.nextLine();

            // splits the command to check for arguments
            String[] parts = command.split(" ", 2);
            Runnable cmd = commands.get(parts[0]);

            // executes command if recognized
            if (cmd != null) {
                cmd.run();
            } else {
                processCommandWithArgs(parts[0], parts.length > 1 ? parts[1] : "");
            }
        }
    }

    // processes commands that include arguments, like "connect"
    private static void processCommandWithArgs(String command, String args) {
        if ("connect".equals(command)) {
            String[] connectArgs = args.split(" ");
            if (connectArgs.length == 2) {
                try {
                    String ip = connectArgs[0];
                    int port = Integer.parseInt(connectArgs[1]);
                    connectionManager.connectToPeer(ip, port);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Port must be an integer.");
                }
            } else {
                System.out.println("Usage: connect <IP address> <port>");
            }
        } else {
            System.out.println("Unknown command. Type 'help' for a list of available commands.");
        }
    }

    // displays the list of available commands
    private static void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("help                - Display available commands");
        System.out.println("myip                - Display your IP address");
        System.out.println("myport              - Display the port you are listening on");
        System.out.println("connect <IP> <port> - Connect to another peer");
        System.out.println("list                - List active connections");
        System.out.println("terminate           - Terminate a connection");
        System.out.println("send                - Send a message to a connection");
        System.out.println("exit                - Exit the application");
    }

    // method to display the local IP address
    private static void myip() {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            System.out.println("Your IP address: " + localAddress.getHostAddress());
        } catch (IOException e) {
            System.out.println("Error: Unable to determine IP address.");
        }
    }

    // method to continiously accept incoming connections
    private static void acceptConnections() {
        while (true) {
            try {
                // wait for a new connection
                Socket socket = serverSocket.accept();
                int remoteListeningPort = -1;

                // parse the client's listening port
                // changed to bufferreader, might be more reliable IDK AT THIS POINT
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    if (line.startsWith("LISTEN_PORT:")) {
                        remoteListeningPort = Integer.parseInt(line.split(":")[1]);
                        break;
                    }
                }

                // create a handler for this connection
                ConnectionHandler handler = new ConnectionHandler(socket, remoteListeningPort);
                connectionManager.addConnection(handler);

                // confirms connection output
                System.out.println("Accepted connection from " + socket.getInetAddress().getHostAddress());

                // start a new thread to handle incoming messages
                new Thread(() -> handleIncomingMessages(handler)).start();

            } catch (IOException e) {
                System.out.println("Error accepting connection: " + e.getMessage());
            }
        }
    }

    // listens for incoming messages from a peer
    public static void handleIncomingMessages(ConnectionHandler handler) {
        try {
            System.out.println("Listening for messages from " + handler.getClientAddress());
            BufferedReader reader = new BufferedReader(new InputStreamReader(handler.getSocket().getInputStream()));

            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equals("TERMINATE")) {
                    // receiving side
                    System.out.println("Connection terminated by " + handler.getClientAddress());
                    handler.closeConnection(); // close the connection locally
                    connectionManager.removeConnection(handler); // remove it from the list
                    break; // exit the listening loop
                } else {
                    // display the message along with sender information
                    System.out.println("\nMessage received from " + handler.getClientAddress());
                    System.out.println("Sender’s Port: " + handler.getClientPort());
                    System.out.println("Message: \"" + message + "\"");
                }
            }
        } catch (IOException e) {
            // fix error during intentional termination
            if (!e.getMessage().equals("Socket closed")) {
                System.out.println("Error reading messages from " + handler.getClientAddress() + ": " + e.getMessage());
            }
        }
    }

    //// Process the terminate command
    private static void processTerminateCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter connection ID to terminate: ");
        int connectionId = scanner.nextInt();
        connectionManager.terminateConnection(connectionId);
    }

    //// Process the send command
    private static void processSendCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter connection ID to send message: ");
        String inputId = scanner.nextLine();

        // error handling connection ID
        int connectionId;
        try {
            connectionId = Integer.parseInt(inputId);
        } catch (NumberFormatException e) {
            System.out.println("Error: Connection ID must be an integer.");
            return;
        }

        if (!isValidConnectionId(connectionId)) {
            System.out.println("Error: Invalid connection ID.");
            return;
        }

        // prompt user for message
        System.out.print("Enter your message: ");
        String message = scanner.nextLine();

        // validate message
        if (!isValidMessage(message)) {
            System.out.println("Error: Message must be 1-100 characters long.");
            return;
        }

        // send the message
        sendMessageToConnection(connectionId, message);
    }

    // validate the connection ID
    private static boolean isValidConnectionId(int connectionId) {
        return connectionId >= 1 && connectionId <= connectionManager.getActiveConnectionCount();
    }

    // validate the message
    private static boolean isValidMessage(String message) {
        return message != null && !message.trim().isEmpty() && message.length() <= 100;
    }

    // send the message to the connection
    private static void sendMessageToConnection(int connectionId, String message) {
        try {
            connectionManager.sendMessage(connectionId, message);
        } catch (Exception e) {
            System.out.println("Error: Failed to send message. " + e.getMessage());
        }
    }

    //// Process the "exit" -v1
    private static void exitApplication() {
        System.out.println("Closing all connections...");
        connectionManager.closeAllConnections();
        System.out.println("Exiting the application.");
        System.exit(0);
    }
}