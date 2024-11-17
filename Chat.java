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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class for the Chat application. Sets up the server, manages user commands,
 * and provides an interface for establishing peer-to-peer connections.
 */

public class Chat {
    // server socket to listen for incoming connections
    public static ServerSocket serverSocket;
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
        int port = Integer.parseInt(args[0]);
        connectionManager = new ConnectionManager();

        try {
            // creates a server socket to listen on the specified port
            serverSocket = new ServerSocket(port);
            System.out.println("Welcome to our ChatApp!");
            System.out.println("Type 'help' to see available commands.");

            // initialize command map with supported commands
            initializeCommands();

            // starts a separate thread to accept incoming connections from peers
            new Thread(() -> acceptConnections()).start();

            // main loop to process user commands from the console
            processUserCommands();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method to initialize all supported commands with corresponding actions
    private static void initializeCommands() {
        commands.put("myip", Chat::myip);
        commands.put("myport", () -> System.out.println("Listening on port: " + serverSocket.getLocalPort()));
        commands.put("list", connectionManager::listConnections);
        commands.put("help", Chat::displayHelp);
        commands.put("terminate", () -> processTerminateCommand());
        commands.put("send", () -> processSendCommand()); 
        commands.put("exit", () -> exitApplication());
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
            // split arguments for the connect command
            String[] connectArgs = args.split(" ");
            if (connectArgs.length == 2) {
                String ip = connectArgs[0];
                int port = Integer.parseInt(connectArgs[1]);
                connectionManager.connectToPeer(ip, port);
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
        System.out.println("terminate <ID>      - terminate connection"); //
        System.out.println("send <ID> <message> - Send a new message"); // 
        System.out.println("exit                - Exit the application"); // 

    }

    // method to display the local IP address
    private static void myip() {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            System.out.println("Your IP address: " + localAddress.getHostAddress());
        } catch (UnknownHostException e) {
            System.out.println("Error: Unable to determine IP address.");
        }
    }

    // method to continuously accept incoming connections
    private static void acceptConnections() {
        while (true) {
            try {
                // accepts an incoming connection
                Socket socket = serverSocket.accept();
                ConnectionHandler handler = new ConnectionHandler(socket);

                // adds the connection to the ConnectionManager
                connectionManager.addConnection(handler);

                System.out.println("Accepted connection from " + socket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                System.out.println("Error accepting connection: " + e.getMessage());
            }
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