/**
 * COMP 429 Chat Application Assignment
 * Project Group 3
 * Natalia Chavez
 * Kristina Dela Merced
 */

import java.io.IOException;
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
    // manges multiple peer connections
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
            // error handling invalid port num
            if (port < 1024 || port > 65535) {
                System.out.println("Error: Port must be between 1024 and 65535.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Port must be an integer.");
            return;
        }

        // initializes connection manager
        connectionManager = new ConnectionManager();

        try {
            // creates a server socket to listen on the specified port
            serverSocket = new ServerSocket(port);
            System.out.println("Chat application started on port " + port);
            System.out.println("Type 'help' for available commands.");

            // initialize command map with supported commands
            initializeCommands();

            // starts a separate thread to accept incoming connections from peers
            new Thread(() -> acceptConnections()).start();

            //  main loop to process user commands from the console
            processUserCommands();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // provides access to server socket for other parts of program
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

    // processes commands that include arguments, like connect
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
        System.out.println("terminate           - Terminate a connection"); // still buggy
        System.out.println("send                - Send a message to a connection"); // sorta eh
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

    // method to continuously accept incoming connections
    private static void acceptConnections() {
        while (true) {
            try {
                // wait for a new connections
                Socket socket = serverSocket.accept();
                int remoteListeningPort = -1;

                // parse the client's listening port
                Scanner input = new Scanner(socket.getInputStream());
                while (input.hasNextLine()) {
                    String line = input.nextLine();
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

                // Start a new thread to handle incoming messages
                new Thread(() -> handleIncomingMessages(handler)).start();

            } catch (IOException e) {
                System.out.println("Error accepting connection: " + e.getMessage());
            }
        }
    }

    // listens for incoming messages from a peer
    private static void handleIncomingMessages(ConnectionHandler handler) {
        try {
            System.out.println("Listening for messages from " + handler.getClientAddress());
            Scanner input = new Scanner(handler.getSocket().getInputStream());

            while (true) {  // continuously listen for incoming messages
                if (input.hasNextLine()) {
                    String message = input.nextLine();
                    System.out.println("Message received from " + handler.getClientAddress() + ": " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading messages from " + handler.getClientAddress() + ": " + e.getMessage());
        }
    }

    //// Process the terminate command - v1
    private static void processTerminateCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter connection ID to terminate: ");
        int connectionId = scanner.nextInt();
        connectionManager.terminateConnection(connectionId);
    }

    //// Process the send command - v1
    private static void processSendCommand() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter connection ID to send message: ");
        int connectionId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline
        System.out.print("Enter your message: ");
        String message = scanner.nextLine();

        // error handling
        if (message.length() > 100) {
            System.out.println("Error: Message exceeds 100 characters. Please shorten it.");
            return;
        }

        connectionManager.sendMessage(connectionId, message);
    }

    //// Process to "exit" - v1
    private static void exitApplication() {
        System.out.println("Closing all connections...");
        connectionManager.closeAllConnections();
        System.out.println("Exiting the application.");
        System.exit(0);
    }
}